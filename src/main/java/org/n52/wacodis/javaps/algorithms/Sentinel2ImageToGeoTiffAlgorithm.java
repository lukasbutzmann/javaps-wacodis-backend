/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexOutput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.io.data.binding.complex.GeotiffFileDataBinding;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;
import org.n52.wacodis.javaps.io.data.binding.complex.ProductMetadataBinding;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.n52.wacodis.javaps.io.metadata.ProductMetadataCreator;
import org.n52.wacodis.javaps.io.metadata.SentinelProductMetadataCreator;
import org.n52.wacodis.javaps.preprocessing.InputDataPreprocessor;
import org.n52.wacodis.javaps.preprocessing.Sentinel2Preprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This example process shows how to use the SNAP Engine and Sentinel-2 Toolbox
 * Java API for reading, resampling and writing different product image types.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.snap.s2togeotiff",
        title = "Sentinel-2 Download to GeoTiff Process",
        abstrakt = "Perform a Sentinel-2 file download, resample the image and "
        + "write it out as GeoTiff.",
        version = "0.0.1",
        storeSupported = true,
        statusSupported = true)
public class Sentinel2ImageToGeoTiffAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sentinel2ImageToGeoTiffAlgorithm.class);

    @Autowired
    private WacodisBackendConfig config;

    @Autowired
    private SentinelFileDownloader fileDownloader;

    private String imageUrl;
    private GenericFileData product;
    private ProductMetadata metadata;

    @LiteralInput(
            identifier = "SENTINEL-2_URL",
            title = "Sentinel-2 URL",
            abstrakt = "Sentinel-2 URL from Open Access Hub",
            minOccurs = 1,
            maxOccurs = 1
    )
    public void setReferenceData(String value) {
        this.imageUrl = value;
    }

    @Execute
    public void execute() {
        try {
            File sentinelFile = fileDownloader.downloadSentinelFile(imageUrl);
            Product sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());
            
            LOGGER.info("Converting Sentinel product to GeoTIFF");
            this.product = createProductOutput(sentinelProduct);

            ProductMetadataCreator metadataCreator = new SentinelProductMetadataCreator();
            LOGGER.info("Creating metadata for Sentinel product");
            this.metadata = metadataCreator.createProductMetadata(sentinelProduct);
        } catch (WacodisProcessingException ex) {
            LOGGER.error(ex.getMessage());
            LOGGER.debug("Error while creating output", ex);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            LOGGER.debug("Error while reading sentinel data", ex);
        }
    }

    @ComplexOutput(
            identifier = "PRODUCT",
            binding = GeotiffFileDataBinding.class
    )
    public GenericFileData getOutput() {
        return this.product;
    }

    @ComplexOutput(
            identifier = "METADATA",
            binding = ProductMetadataBinding.class
    )
    public ProductMetadata getMetadata() {
        return this.metadata;
    }

    private GenericFileData createProductOutput(Product sentinelProduct) throws WacodisProcessingException {
        String targetDirectory = config.getWorkingDirectory();
        InputDataPreprocessor preprocessor = new Sentinel2Preprocessor(false);

        List<File> outputs = preprocessor.preprocess(sentinelProduct, targetDirectory);

        try {
            return new GenericFileData(outputs.get(0), "image/geotiff");
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating generic file data.", ex);
        }
    }
}
