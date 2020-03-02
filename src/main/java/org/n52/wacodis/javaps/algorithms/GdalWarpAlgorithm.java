package org.n52.wacodis.javaps.algorithms;

import org.apache.commons.io.FilenameUtils;
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
import org.n52.wacodis.javaps.preprocessing.GptPreprocessor;
import org.n52.wacodis.javaps.preprocessing.InputDataPreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WPS process that reprojects a Sentinel-2 image by performing a GDAL warp and delivers the reprojected image as
 * band GeoTIFF composite with bands B2, B3 and B8.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.gdal_warp",
        title = "GDAL warp process",
        abstrakt = "Perform a GDAL warp for reprojecting a Sentinel-2 image.",
        version = "1.0.0",
        storeSupported = true,
        statusSupported = true)
public class GdalWarpAlgorithm extends AbstractAlgorithm {

    private static final String GPF_FILE = "S2_GeoTIFF_Composition.xml";
    private static final String RESULTNAMEPREFIX = "gdal_reprojection_result";
    private static final String TIFF_EXTENSION = ".tif";

    private static final Logger LOG = LoggerFactory.getLogger(GdalWarpAlgorithm.class);

    private String opticalImagesSource;
    private Product sentinelProduct;
    private String epsg;

    private ProductMetadata productMetadata;
    private File resultFile;

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    @LiteralInput(
            identifier = "OPTICAL_IMAGES_SOURCE",
            title = "Optical images source",
            abstrakt = "Source for the optical image",
            minOccurs = 1,
            maxOccurs = 1)
    public void setOpticalImagesSources(String value) {
        this.opticalImagesSource = value;
    }

    @LiteralInput(
            identifier = "EPSG",
            title = "EPSG code",
            abstrakt = "EPSG code that shall be used for reprojecting the Sentinel-2 image (e.g.: EPSG:32632).",
            minOccurs = 1,
            maxOccurs = 1)
    public void setEpsg(String value) {
        this.epsg = value;
    }

    @ComplexOutput(
            identifier = "PRODUCT",
            binding = GeotiffFileDataBinding.class
    )
    public GenericFileData getOutput() throws WacodisProcessingException {
        return this.createProductOutput(resultFile);
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
        File sentinelFile = downloadSentinelImage();
        File preprocessedSentinelFile = preprocessSentinelImage();
        resultFile = this.executeGdalWarp(preprocessedSentinelFile, epsg);

        ProductMetadataCreator<Product> metadataCreator = new SentinelProductMetadataCreator();
        try {
            Product resultProduct = ProductIO.readProduct(resultFile);
            productMetadata = metadataCreator.createProductMetadata(resultProduct, Collections.singletonList(sentinelProduct));
        } catch (IOException ex) {
            LOG.error("Product metadata creation for result product failed: {}", ex.getMessage());
            LOG.debug("Could not read result as SNAP Product.", ex);
        }
        if (productMetadata == null) {
            productMetadata = metadataCreator.createProductMetadata(Collections.singletonList(sentinelProduct));
        }
    }

    private File preprocessSentinelImage() throws WacodisProcessingException {
        HashMap<String, String> parameters = new HashMap();
        parameters.put("area", "[0,0,0,0]");

        InputDataPreprocessor imagePreprocessor = new GptPreprocessor(FilenameUtils.concat(this.getBackendConfig().getGpfDir(), GPF_FILE),
                parameters,
                TIFF_EXTENSION,
                this.getNamingSuffix());
        List<File> preprocessedFiles = imagePreprocessor.preprocess(sentinelProduct, this.getBackendConfig().getWorkingDirectory());
        return preprocessedFiles.get(0);
    }

    private File downloadSentinelImage() throws WacodisProcessingException {
        try {
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    this.opticalImagesSource,
                    this.getBackendConfig().getWorkingDirectory());
            this.sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());
            return sentinelFile;
        } catch (IOException ex) {
            LOG.debug("Error while reading Sentinel file: {}", this.opticalImagesSource, ex);
            throw new WacodisProcessingException("Could not preprocess Sentinel product", ex);
        }
    }

    @Override
    public String getGpfConfigName() {
        return GPF_FILE;
    }

    @Override
    public String getResultNamePrefix() {
        return RESULTNAMEPREFIX;
    }

    @Override
    public String getToolConfigName() {
        return null;
    }

    @Override
    public Map<String, AbstractCommandValue> createInputArgumentValues(String basePath) throws WacodisProcessingException {
        return null;
    }
}
