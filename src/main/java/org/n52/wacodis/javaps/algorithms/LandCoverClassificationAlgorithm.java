/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexInput;
import org.n52.javaps.algorithm.annotation.ComplexOutput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.gt.io.data.binding.complex.GTVectorDataBinding;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.command.MultipleCommandValue;
import org.n52.wacodis.javaps.command.SingleCommandValue;
import org.n52.wacodis.javaps.io.data.binding.complex.GeotiffFileDataBinding;
import org.n52.wacodis.javaps.io.data.binding.complex.ProductMetadataBinding;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;
import org.n52.wacodis.javaps.io.metadata.ProductMetadataCreator;
import org.n52.wacodis.javaps.io.metadata.SentinelProductMetadataCreator;
import org.n52.wacodis.javaps.preprocessing.GptPreprocessor;
import org.n52.wacodis.javaps.preprocessing.InputDataPreprocessor;
import org.n52.wacodis.javaps.preprocessing.ReferenceDataPreprocessor;
import org.n52.wacodis.javaps.utils.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.land_cover_classification",
        title = "Land Cover Classification",
        abstrakt = "Perform a land cover classification for optical images.",
        version = "0.0.1",
        storeSupported = true,
        statusSupported = true)
public class LandCoverClassificationAlgorithm extends AbstractAlgorithm {

    private static final String TIFF_EXTENSION = ".tif";
    private static final String RESULTNAMEPREFIX = "land_cover_classification_result";
    private static final String TOOL_CONFIG = "land-cover-classification.yml";
    private static final String GPF_FILE = "S2_GeoTIFF_Composition.xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(LandCoverClassificationAlgorithm.class);

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    private String opticalImagesSourceType;
    private List<String> opticalImagesSources;
    private String referenceDataType;
    private SimpleFeatureCollection referenceData;
    private ProductMetadata productMetadata;
    private List<Product> sentinelProductList;

    @LiteralInput(
            identifier = "OPTICAL_IMAGES_TYPE",
            title = "Optical images source type",
            abstrakt = "The type of the source for the optical images",
            minOccurs = 1,
            maxOccurs = 1,
            defaultValue = "Sentinel-2",
            allowedValues = {"Sentinel-2", "Aerial_Image"})
    public void setOpticalImagesSourceType(String value) {
        this.opticalImagesSourceType = value;

    }

    @LiteralInput(
            identifier = "OPTICAL_IMAGES_SOURCES",
            title = "Optical images sources",
            abstrakt = "Sources for the optical images",
            minOccurs = 1,
            maxOccurs = 6)
    public void setOpticalImagesSources(List<String> value) {
        this.opticalImagesSources = value;
    }

    @LiteralInput(
            identifier = "REFERENCE_DATA_TYPE",
            title = "Reference data type",
            abstrakt = "The type of the reference data",
            minOccurs = 1,
            maxOccurs = 1,
            defaultValue = "ATKIS",
            allowedValues = {"ATKIS", "MANUAL"})
    public void setReferenceDataType(String value) {
        this.referenceDataType = value;
    }

    @ComplexInput(
            identifier = "REFERENCE_DATA",
            title = "Reference data",
            abstrakt = "Reference data for land cover classification",
            minOccurs = 1,
            maxOccurs = 1,
            binding = GTVectorDataBinding.class
    )
    public void setReferenceData(SimpleFeatureCollection value) {
        this.referenceData = value;
    }

    @ComplexOutput(
            identifier = "PRODUCT",
            binding = GeotiffFileDataBinding.class
    )
    public GenericFileData getOutput() throws WacodisProcessingException {
        return this.createProductOutput(this.getProductName());
    }

    @ComplexOutput(
            identifier = "METADATA",
            binding = ProductMetadataBinding.class
    )
    public ProductMetadata getMetadata() {
        return this.productMetadata;
    }

    @Execute
    public void execute() throws WacodisProcessingException {
        this.executeProcess();

        ProductMetadataCreator metadataCreator = new SentinelProductMetadataCreator();
        this.productMetadata = metadataCreator.createProductMetadataBinding(this.sentinelProductList);
    }

    @Override
    public String getToolConfigName() {
        return TOOL_CONFIG;
    }

    @Override
    public String getResultNamePrefix() {
        return RESULTNAMEPREFIX;
    }

    @Override
    public String getGpfConfigName() {
        return GPF_FILE;
    }

    @Override
    public Map<String, AbstractCommandValue> createInputArgumentValues() throws WacodisProcessingException {
        Map<String, AbstractCommandValue> inputArgumentValues = new HashMap();

        inputArgumentValues.put("OPTICAL_IMAGES_SOURCES", this.preprocessOpticalImages());
        inputArgumentValues.put("REFERENCE_DATA", this.preprocessReferenceData());
        inputArgumentValues.put("RESULT_PATH", this.getResultPath());

        return inputArgumentValues;
    }

    private AbstractCommandValue preprocessOpticalImages() throws WacodisProcessingException {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("epsg", this.getBackendConfig().getEpsg());
        InputDataPreprocessor imagePreprocessor = new GptPreprocessor(FilenameUtils.concat(this.getBackendConfig().getGpfDir(), GPF_FILE), parameters, TIFF_EXTENSION, this.getNamingSuffix());

        this.sentinelProductList = new ArrayList();
        List<File> preprocessedImages = new ArrayList();
        this.opticalImagesSources.forEach(ois -> {
            try {
                // Download satellite data
                File sentinelFile = sentinelDownloader.downloadSentinelFile(
                        ois,
                        this.getBackendConfig().getWorkingDirectory());
                Product sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());
                this.sentinelProductList.add(sentinelProduct);
                preprocessedImages.addAll(
                        imagePreprocessor.preprocess(sentinelProduct, this.getBackendConfig().getWorkingDirectory()));
            } catch (IOException ex) {
                LOGGER.debug("Error while retrieving Sentinel file: {}", ois, ex);
            } catch (WacodisProcessingException ex) {
                LOGGER.debug("Error while preprocessing Sentinel file: {}", ois, ex);
            }
        });
        if (preprocessedImages.isEmpty()) {
            throw new WacodisProcessingException("No preprocessed Sentinel files available.");
        }

        MultipleCommandValue value = new MultipleCommandValue(
                preprocessedImages.stream()
                        .map(sF -> sF.getPath())
                        .collect(Collectors.toList()));
        return value;

    }

    private AbstractCommandValue preprocessReferenceData() throws WacodisProcessingException {
        InputDataPreprocessor referencePreprocessor = new ReferenceDataPreprocessor(GeometryUtils.DEFAULT_INPUT_EPSG, this.getBackendConfig().getEpsg(), this.getNamingSuffix());

        List<File> preprocessedReferenceData = referencePreprocessor.preprocess(this.referenceData, this.getBackendConfig().getWorkingDirectory());

        SingleCommandValue value = new SingleCommandValue(preprocessedReferenceData.get(0).getName());
        return value;
    }
}
