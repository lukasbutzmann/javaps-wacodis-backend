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
import java.util.Map;

import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexOutput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.io.data.binding.complex.GeotiffFileDataBinding;
import org.n52.wacodis.javaps.io.data.binding.complex.ProductMetadataBinding;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;
import org.n52.wacodis.javaps.io.metadata.ProductMetadataCreator;
import org.n52.wacodis.javaps.io.metadata.SentinelProductMetadataCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.forest_vitality_change",
        title = "Forest Vitality Change",
        abstrakt = "Perform a Forest Vitality Change calculation between two times.",
        version = "0.0.1",
        storeSupported = true,
        statusSupported = true)
public class ForestVitalityChangeAlgorithm extends AbstractAlgorithm {

    private static final String TIFF_EXTENSION = ".tif";
    private static final String RESULTNAMEPREFIX = "forest-vitality-change_result";
    private static final String TOOL_CONFIG = "forest-vitality-change.yml";

    private static final Logger LOGGER = LoggerFactory.getLogger(ForestVitalityChangeAlgorithm.class);

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    private String opticalImagesSourceType;
    private String opticalImagesSource1, opticalImagesSource2;
    private ProductMetadata productMetadata;
    private Product sentinelProduct1, sentinelProduct2;

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
            identifier = "OPTICAL_IMAGES_SOURCES_1",
            title = "Optical images sources (first time frame)",
            abstrakt = "Sources for the first (time frame) optical images",
            minOccurs = 1,
            maxOccurs = 1)
    public void setOpticalImagesSources(String value) {
        this.opticalImagesSource1 = value;
    }

    @LiteralInput(
            identifier = "OPTICAL_IMAGES_SOURCES_2",
            title = "Optical images sources (second time frame)",
            abstrakt = "Sources for the second (time frame) optical images to compare with 1",
            minOccurs = 1,
            maxOccurs = 1)
    public void setOpticalImagesSources2(String value2) {
        this.opticalImagesSource2 = value2;
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
        this.productMetadata = this.createProductMetadata(Arrays.asList(this.sentinelProduct1, this.sentinelProduct2));
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
        return null;
    }

    @Override
    public Map<String, AbstractCommandValue> createInputArgumentValues(String basePath) throws WacodisProcessingException {
        Map<String, AbstractCommandValue> inputArgumentValues = new HashMap();

        inputArgumentValues.put("RAW_OPTICAL_IMAGES_SOURCES_1", this.createInputValue(basePath, this.preprocessOpticalImages1(), true));
        inputArgumentValues.put("RAW_OPTICAL_IMAGES_SOURCES_2", this.createInputValue(basePath, this.preprocessOpticalImages2(), true));
        inputArgumentValues.put("RESULT_PATH", this.getResultPath(basePath));

        return inputArgumentValues;
    }

    private File preprocessOpticalImages1() throws WacodisProcessingException {
        try {
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    this.opticalImagesSource1,
                    this.getBackendConfig().getWorkingDirectory());
            this.sentinelProduct1 = ProductIO.readProduct(sentinelFile.getPath());

            return sentinelFile;

        } catch (IOException ex) {
            LOGGER.debug("Error while reading Sentinel file: {}", this.opticalImagesSource1, ex);
            throw new WacodisProcessingException("Could not preprocess Sentinel product", ex);
        }
    }

    private File preprocessOpticalImages2() throws WacodisProcessingException {
        try {
            File sentinelFile2 = sentinelDownloader.downloadSentinelFile(
                    this.opticalImagesSource2,
                    this.getBackendConfig().getWorkingDirectory());
            this.sentinelProduct2 = ProductIO.readProduct(sentinelFile2.getPath());

            return sentinelFile2;

        } catch (IOException ex) {
            LOGGER.debug("Error while reading Sentinel file: {}", this.opticalImagesSource2, ex);
            throw new WacodisProcessingException("Could not preprocess Sentinel product", ex);
        }
    }

}
