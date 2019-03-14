/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexInput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.algorithms.execution.LandCoverClassificationExecutor;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.LandCoverClassificationConfig;
import org.n52.wacodis.javaps.io.data.binding.complex.FeatureCollectionBinding;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.n52.wacodis.javaps.preprocessing.InputDataPreprocessor;
import org.n52.wacodis.javaps.preprocessing.ReferenceDataPreprocessor;
import org.n52.wacodis.javaps.preprocessing.Sentinel2Preprocessor;
import org.openide.util.Exceptions;
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
public class LandCoverClassificationAlgorithm {

    private static final String TIFF_EXTENSION = ".tif";

    private static final Logger LOGGER = LoggerFactory.getLogger(LandCoverClassificationAlgorithm.class);

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    @Autowired
    private WacodisBackendConfig config;

    @Autowired
    private LandCoverClassificationConfig toolConfig;

    private String opticalImagesSourceType;
    private List<String> opticalImagesSources;
    private String referenceDataType;
    private SimpleFeatureCollection referenceData;
    private List<String> products;

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
            maxOccurs = 10)
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
            binding = FeatureCollectionBinding.class
    )
    public void setReferenceData(SimpleFeatureCollection value) {
        this.referenceData = value;
    }

    @Execute
    public void execute() throws WacodisProcessingException {
        this.products = new ArrayList();
        String workingDirectory = config.getWorkingDirectory();

        //TODO preprocess training data
        String trainingData = "path/to/trainingdata";
        InputDataPreprocessor referencePreprocessor = new ReferenceDataPreprocessor();
        try {
            List<File> referenceDataFiles = referencePreprocessor.preprocess(this.referenceData, workingDirectory);
            File refData = referenceDataFiles.get(0); //.shp
        } catch (IOException ex) {
            String message = "Error while preprocessing reference data";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }

        // Download satellite data
        opticalImagesSources.forEach(imageSource -> {
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    imageSource,
                    workingDirectory);

            InputDataPreprocessor imagePreprocessor = new Sentinel2Preprocessor(false);
            try {
                // convert sentinel images to GeoTIFF files
                List<File> outputs = imagePreprocessor.preprocess(
                        sentinelFile.getPath(),
                        workingDirectory);

                if (!outputs.isEmpty()) {
                    String resultId = UUID.randomUUID().toString();
                    LandCoverClassificationExecutor executor
                            = new LandCoverClassificationExecutor(
                                    workingDirectory,
                                    outputs.get(0).getName(),
                                    trainingData,
                                    resultId + TIFF_EXTENSION, toolConfig);
                    ProcessResult result = executor.executeTool();
                    if (result.getResultCode() == 0) {
                        this.products.add(resultId);
                    }
                    LOGGER.info("Land classification docker process finished "
                            + "executing with result code: {}", result.getResultCode());
                    LOGGER.debug(result.getOutputMessage());
                }

            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                LOGGER.debug("Error while processing sentinel file: "
                        + sentinelFile.getName(), ex);
            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage());
                LOGGER.debug("Error while executing land cover docker process", ex);
            }
        });

    }

    @LiteralOutput(identifier = "PRODUCT")
    public String getOutput() {
        return this.products.get(0); //ToDo return List
    }

}
