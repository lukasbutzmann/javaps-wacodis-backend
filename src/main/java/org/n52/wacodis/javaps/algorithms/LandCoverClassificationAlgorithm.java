/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexInput;
import org.n52.javaps.algorithm.annotation.ComplexOutput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.algorithms.execution.EoToolExecutor;
import org.n52.wacodis.javaps.algorithms.execution.LandCoverClassificationExecutor;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.LandCoverClassificationConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfigParser;
import org.n52.wacodis.javaps.io.data.binding.complex.FeatureCollectionBinding;
import org.n52.wacodis.javaps.io.data.binding.complex.GeotiffFileDataBinding;
import org.n52.wacodis.javaps.io.data.binding.complex.ProductMetadataBinding;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;
import org.n52.wacodis.javaps.io.metadata.ProductMetadataCreator;
import org.n52.wacodis.javaps.io.metadata.SentinelProductMetadataCreator;
import org.n52.wacodis.javaps.preprocessing.InputDataPreprocessor;
import org.n52.wacodis.javaps.preprocessing.ReferenceDataPreprocessor;
import org.n52.wacodis.javaps.preprocessing.Sentinel2Preprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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
public class LandCoverClassificationAlgorithm implements InitializingBean {

    private static final String TIFF_EXTENSION = ".tif";
    private static final String REFERENCEDATA_EPSG = "EPSG:32632";
    private static final String RESULTNAMEPREFIX = "land_cover_classification_result";
    private static final String TOOLCONFIGPATH = "land-cover-classification.yml";

    private static final Logger LOGGER = LoggerFactory.getLogger(LandCoverClassificationAlgorithm.class);

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    @Autowired
    private WacodisBackendConfig config;

    @Autowired
    private LandCoverClassificationConfig toolConfig;

    private String opticalImagesSourceType;
    private String opticalImagesSource;
    private String referenceDataType;
    private SimpleFeatureCollection referenceData;
    private String productName;
    private ProductMetadata productMetadata;

    private Map<String, String> rawInputMap;

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
        //rawInputMap.put("OPTICAL_IMAGES_SOURCES", //tool needs geotiff input instead of sentinel safe
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
        //rawInputMap.put("REFERENCE_DATA", value); //tool needs path to reference data not actual data
    }

    @ComplexOutput(
            identifier = "PRODUCT",
            binding = GeotiffFileDataBinding.class
    )
    public GenericFileData getOutput() throws WacodisProcessingException {
        return this.createProductOutput(this.productName);
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

        String workingDirectory = config.getWorkingDirectory();
        String namingSuffix = "_" + System.currentTimeMillis(); //common suffix for output files and containers

        List<File> imageData;
        File refData;

        //TODO preprocess training data for each input image (need to adjust wps input)
        InputDataPreprocessor referencePreprocessor = new ReferenceDataPreprocessor(REFERENCEDATA_EPSG, namingSuffix); //set spatial reference system and output filename suffix, TODO: detect srs from input data
        ProductMetadataCreator metadataCreator = new SentinelProductMetadataCreator();
        try {
            List<File> referenceDataFiles = referencePreprocessor.preprocess(this.referenceData, workingDirectory);
            refData = referenceDataFiles.get(0); //.shp
            this.rawInputMap.put("REFERENCE_DATA", refData.getPath()); 

            // Download satellite data
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    opticalImagesSource,
                    workingDirectory);

            InputDataPreprocessor imagePreprocessor = new Sentinel2Preprocessor(false, namingSuffix);

            Product sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());
            this.productMetadata = metadataCreator.createProductMetadataBinding(sentinelProduct);
            // convert sentinel images to GeoTIFF files
            imageData = imagePreprocessor.preprocess(
                    sentinelProduct,
                    workingDirectory);
            this.rawInputMap.put("OPTICAL_IMAGES_SOURCES", imageData.get(0).getName());
        } catch (WacodisProcessingException | IOException ex) {
            String message = "Error while preprocessing input data";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }

        if (imageData.isEmpty()) {
            throw new WacodisProcessingException("No image data for processing available");
        }

        String resultFileName = RESULTNAMEPREFIX + UUID.randomUUID().toString() + namingSuffix + TIFF_EXTENSION;
        String containerName = this.toolConfig.getDockerContainerName() + namingSuffix;


        ProcessResult result;
        try {
            this.rawInputMap.put("WORKINGDIRECTORY", this.config.getWorkingDirectory());
            this.rawInputMap.put("PRODUCT", resultFileName);
            
            result = new EoToolExecutor().executeTool(this.rawInputMap, parseToolConfig());
        } catch (Exception ex) {
            String message = "Error while executing land cover docker process";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        if (result.getResultCode() == 0) { //tool returns Result Code 0 if finished successfully
            this.productName = resultFileName;
        } else { //non-zero Result Code, error occured during tool execution
            throw new WacodisProcessingException("landcover classification tool (container: "
                    + containerName
                    + " )exited with a non-zero result code, result code was "
                    + result.getResultCode()
                    + ", consult tool specific documentation for details");
        }
        LOGGER.info("landcover classification docker process finished "
                + "executing with result code: {}", result.getResultCode());
        LOGGER.debug(result.getOutputMessage());
    }
    
    public Map<String, String> preprocessInputs(Map<String, Object> inputs) {
        // TODO implement process input preprocessing
        return null;
    }

    private GenericFileData createProductOutput(String fileName) throws WacodisProcessingException {
        try {
            return new GenericFileData(new File(this.config.getWorkingDirectory(), fileName), "image/geotiff");
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating generic file data.", ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.rawInputMap = new HashMap();
    }

    
    private ToolConfig parseToolConfig() throws IOException{
        ToolConfigParser parser = new ToolConfigParser();        
        ToolConfig toolConfig = parser.parse(TOOLCONFIGPATH);
        
        return toolConfig;
    }
    
}
