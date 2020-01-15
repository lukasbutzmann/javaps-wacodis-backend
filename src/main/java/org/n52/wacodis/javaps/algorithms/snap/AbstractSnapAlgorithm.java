/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.snap;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.LoggerProgressMonitor;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;
import org.n52.wacodis.javaps.io.metadata.ProductMetadataCreator;
import org.n52.wacodis.javaps.io.metadata.SentinelProductMetadataCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for executing SNAP operators by the use of the Graph
 * Processing Framework (GPF)
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractSnapAlgorithm {

    private static final String GEOTIFF_TYPE = "GeoTIFF";
    private static final String TIFF_EXTENSION = ".tif";

    private static final Logger LOGGER = LoggerFactory.getLogger(NdviSnapAlgorithm.class);

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    private String sentinel2ImageSource;
    private String resultPath;
    private ProductMetadata productMetadata;

    @Autowired
    private WacodisBackendConfig config;
    
       

    public void execute() throws WacodisProcessingException {

        Product sentinelProduct = this.fetchInput(this.sentinel2ImageSource);
        LOGGER.info("Succesfully downloaded Sentinel-2 scene: {}", sentinelProduct.getName());
        
        Map<String, Object> parameters = this.prepareOperationParameters();
        Product result = GPF.createProduct(this.getOperatorName(), parameters, sentinelProduct);
        LOGGER.info("Succesfully finished operation: ", this.getOperatorName());

        this.resultPath = this.writeResultProduct(result);
        LOGGER.info("Succesfully wrote result product as GeoTIFF file: {}.", this.resultPath);

        ProductMetadataCreator metadataCreator = new SentinelProductMetadataCreator();
        this.productMetadata = metadataCreator.createProductMetadata(sentinelProduct);
    }

    /**
     * Fetches a Sentinel-2 scene for the specified URL from the Copernicus Open
     * Access Hub
     *
     * @param imageUrl the URL for the Sentinel-2 scene
     * @return the {@link Product} that represents the Sentinel-2 scene
     * @throws WacodisProcessingException
     */
    protected Product fetchInput(String imageUrl) throws WacodisProcessingException {
        Product sentinelProduct;
        try {
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    this.sentinel2ImageSource,
                    this.config.getWorkingDirectory());

            sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());
        } catch (IOException ex) {
            String message = "Error while reading input data";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        return sentinelProduct;
    }

    /**
     * Writes the created result into the working directory
     *
     * @param result Result to write
     * @return Path of the written result file
     * @throws WacodisProcessingException
     */
    protected String writeResultProduct(Product result) throws WacodisProcessingException {
        LoggerProgressMonitor monitor = new LoggerProgressMonitor(LOGGER);

        String namingSuffix = "_" + System.currentTimeMillis();
        String resultFileName = this.getResultNamePrefix() + "_" + UUID.randomUUID().toString() + namingSuffix + TIFF_EXTENSION;
        String outFilePath = FilenameUtils.concat(this.config.getWorkingDirectory(), resultFileName);

        try {
            ProductIO.writeProduct(
                    result,
                    outFilePath,
                    GEOTIFF_TYPE,
                    monitor
            );
        } catch (IOException ex) {
            String message = "Error while writing output data";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        return outFilePath;
    }

    /**
     * Creates a {@link GenericFileData} from the result file path for the
     * output binding
     *
     * @param filePath result file path
     * @return {@link GenericFileData} for the result product
     * @throws WacodisProcessingException
     */
    protected GenericFileData createProductOutput(String filePath) throws WacodisProcessingException {
        try {
            return new GenericFileData(new File(filePath), "image/geotiff");
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating generic file data.", ex);
        }
    }

    public SentinelFileDownloader getSentinelDownloader() {
        return sentinelDownloader;
    }

    public void setSentinelDownloader(SentinelFileDownloader sentinelDownloader) {
        this.sentinelDownloader = sentinelDownloader;
    }

    public String getSentinel2ImageSource() {
        return sentinel2ImageSource;
    }

    public void setSentinel2ImageSource(String sentinel2ImageSource) {
        this.sentinel2ImageSource = sentinel2ImageSource;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public ProductMetadata getProductMetadata() {
        return productMetadata;
    }

    public void setProductMetadata(ProductMetadata productMetadata) {
        this.productMetadata = productMetadata;
    }

    public WacodisBackendConfig getConfig() {
        return config;
    }

    public void setConfig(WacodisBackendConfig config) {
        this.config = config;
    }

    public abstract String getResultNamePrefix();

    public abstract String getOperatorName();

    public abstract Map<String, Object> prepareOperationParameters();
}
