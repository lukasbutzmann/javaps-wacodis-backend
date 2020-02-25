/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.javaps.algorithm.annotation.*;
import org.n52.javaps.gt.io.data.binding.complex.GTVectorDataBinding;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.io.data.binding.complex.GeotiffFileDataBinding;
import org.n52.wacodis.javaps.io.data.binding.complex.ProductMetadataBinding;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;
import org.n52.wacodis.javaps.preprocessing.graph.*;
import org.n52.wacodis.javaps.utils.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.sealing_factor",
        title = "Sealing Factor Calculation",
        abstrakt = "Perform a sealing factor calculation for optical images.",
        version = "0.0.1",
        storeSupported = true,
        statusSupported = true)
public class SealingFactorAlgorithm extends AbstractAlgorithm {

    private static final String TIFF_EXTENSION = ".tif";
    private static final String RESULTNAMEPREFIX = "sealing_factor_result";
    private static final String TOOL_CONFIG = "sealing-factor.yml";
    private static final String GPF_FILE = "S2_GeoTIFF_Composition.xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(SealingFactorAlgorithm.class);

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    private String opticalImagesSourceType;
    private String opticalImagesSource;
    private String maskingDataType;
    private SimpleFeatureCollection maskingData;
    private ProductMetadata productMetadata;
    private Product sentinelProduct;

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
            identifier = "MASKING_DATA_TYPE",
            title = "Masking data type",
            abstrakt = "The type of the reference data",
            minOccurs = 1,
            maxOccurs = 1,
            defaultValue = "MASK_VECTOR_DATA",
            allowedValues = {"MASK_VECTOR_DATA","MASK_RASTER_DATA"})
    public void setMaskingDataType(String value) {
        this.maskingDataType = value;
    }

    @ComplexInput(
            identifier = "MASKING_DATA",
            title = "Masking data",
            abstrakt = "Masking data for sealing factor",
            minOccurs = 1,
            maxOccurs = 1,
            binding = GTVectorDataBinding.class
    )
    public void setMaskingData(SimpleFeatureCollection value) {
        this.maskingData = value;
    }

    @ComplexOutput(
            identifier = "PRODUCT",
            binding = GeotiffFileDataBinding.class
    )
    public GenericFileData getOutput() throws WacodisProcessingException {
        return this.createProductOutput(this.getResultFile());
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
        this.productMetadata = this.createProductMetadata(Collections.singletonList(this.sentinelProduct));
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
    public Map<String, AbstractCommandValue> createInputArgumentValues(String basePath) throws WacodisProcessingException {
        Map<String, AbstractCommandValue> inputArgumentValues = new HashMap();

        inputArgumentValues.put("RAW_OPTICAL_IMAGES_SOURCES", this.createInputValue(basePath, this.preprocessOpticalImages(), true));
        inputArgumentValues.put("MASKING_DATA", this.createInputValue(basePath, this.preprocessReferenceData(), true));
        inputArgumentValues.put("RESULT_PATH", this.getResultPath(basePath));

        return inputArgumentValues;
    }

    private File preprocessOpticalImages() throws WacodisProcessingException {
        try {
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    this.opticalImagesSource,
                    this.getBackendConfig().getWorkingDirectory());
            this.sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());

            return sentinelFile;

        } catch (IOException ex) {
            LOGGER.debug("Error while reading Sentinel file: {}", this.opticalImagesSource, ex);
            throw new WacodisProcessingException("Could not preprocess Sentinel product", ex);
        }
    }

    private File preprocessReferenceData() throws WacodisProcessingException {

        String fileIdentifier = (this.getNamingSuffix() != null) ? this.getNamingSuffix() : UUID.randomUUID().toString();
        InputDataWriter shapeWriter = new ShapeWriter(new File(this.getBackendConfig().getWorkingDirectory(), "wacodis_maskingdata_" + fileIdentifier + ".shp"));

        InputDataOperator reprojectingOperator = new ReprojectingOperator(GeometryUtils.DEFAULT_INPUT_EPSG);
        List<InputDataOperator> referenceDataOperatorList = new ArrayList<>();
        referenceDataOperatorList.add(reprojectingOperator);

        PreprocessingExecutor referencePreprocessor = new PreprocessingExecutor(shapeWriter, referenceDataOperatorList);
        File preprocessedReferenceData = referencePreprocessor.executeOperators(this.maskingData);

        return preprocessedReferenceData;
    }

}
