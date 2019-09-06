/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.io.data.binding.complex.FeatureCollectionBinding;
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

    @Autowired
    private WacodisBackendConfig config;

    private String opticalImagesSourceType;
    private String opticalImagesSource;
    private String referenceDataType;
    private SimpleFeatureCollection referenceData;
    private ProductMetadata productMetadata;
    private Product sentinelProduct;
    private String productName;

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
            maxOccurs = 1)
    public void setOpticalImagesSources(String value) {
        this.opticalImagesSource = value;
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
        this.productMetadata = metadataCreator.createProductMetadataBinding(this.sentinelProduct);
    }

    private GenericFileData createProductOutput(String fileName) throws WacodisProcessingException {
        try {
            return new GenericFileData(new File(this.config.getWorkingDirectory(), fileName), "image/geotiff");
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating generic file data.", ex);
        }
    }

    public String getProductName() {
        return productName;
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
    public Map<String, AbstractCommandValue> createInputArgumentValues() throws WacodisProcessingException {
        Map<String, AbstractCommandValue> inputArgumentValues = new HashMap();

        inputArgumentValues.put("OPTICAL_IMAGES_SOURCES", this.preprocessOpticalImages());
        inputArgumentValues.put("REFERENCE_DATA", this.preprocessReferenceData());
        inputArgumentValues.put("RESULT_PATH", this.getResultPath());

        return inputArgumentValues;
    }

    private AbstractCommandValue preprocessOpticalImages() throws WacodisProcessingException {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("epsg", this.config.getEpsg());
        InputDataPreprocessor imagePreprocessor = new GptPreprocessor(FilenameUtils.concat(this.config.getGpfDir(), GPF_FILE), parameters, TIFF_EXTENSION, this.getNamingSuffix());

        try {
            // Download satellite data
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    this.opticalImagesSource,
                    this.config.getWorkingDirectory());
            this.sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());

        } catch (IOException ex) {
            String message = "Error while retrieving Sentinel file";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        List<File> preprocessedImages = imagePreprocessor.preprocess(this.sentinelProduct, this.config.getWorkingDirectory());

        MultipleCommandValue value = new MultipleCommandValue();
        value.setCommandValue(Arrays.asList(preprocessedImages.get(0).getName()));
        return value;

    }

    private AbstractCommandValue preprocessReferenceData() throws WacodisProcessingException {
        InputDataPreprocessor referencePreprocessor = new ReferenceDataPreprocessor(GeometryUtils.DEFAULT_INPUT_EPSG, this.config.getEpsg(), this.getNamingSuffix());

        List<File> preprocessedReferenceData = referencePreprocessor.preprocess(this.referenceData, this.config.getWorkingDirectory());

        SingleCommandValue value = new SingleCommandValue();
        value.setCommandValue(preprocessedReferenceData.get(0).getName());
        return value;
    }

    private AbstractCommandValue getResultPath() {
        this.productName = this.getResultNamePrefix() + UUID.randomUUID().toString() + this.getNamingSuffix() + TIFF_EXTENSION;

        SingleCommandValue value = new SingleCommandValue();
        value.setCommandValue(this.productName);
        return value;
    }

}
