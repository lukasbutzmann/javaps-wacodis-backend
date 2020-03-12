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
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.water_mask",
        title = "Water mask calculation",
        abstrakt = "Perform a water mask calculation.",
        version = "0.0.1",
        storeSupported = true,
        statusSupported = true)
public class WaterMaskAlgorithm extends AbstractAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaterMaskAlgorithm.class);

    private static final String TIFF_EXTENSION = ".tif";
    private static final String RESULTNAMEPREFIX = "water-mask_result";
    private static final String TOOL_CONFIG = "water-mask.yml";
    private static final String GPF_FILE = "S1_GeoTIFF-BigTIFF_Composition_GRD.xml";

    @Autowired
    private SentinelFileDownloader sentinelDownloader;

    private String opticalImagesSource;
    private Product sentinelProduct;
    private ProductMetadata productMetadata;

    @LiteralInput(
            identifier = "RADAR_IMAGE_SOURCE",
            title = "Radar image source",
            abstrakt = "Source for the radar image",
            minOccurs = 1,
            maxOccurs = 1)
    public void setOpticalImagesSources(String value) {
        this.opticalImagesSource = value;
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
    public String getGpfConfigName() {
        return GPF_FILE;
    }

    @Override
    public String getResultNamePrefix() {
        return RESULTNAMEPREFIX;
    }

    @Override
    public Map<String, AbstractCommandValue> createInputArgumentValues(String basePath) throws WacodisProcessingException {
        Map<String, AbstractCommandValue> inputArgumentValues = new HashMap();

        inputArgumentValues.put("RADAR_IMAGE_SOURCE", this.createInputValue(basePath, this.preprocessRadarImage(), true));
        inputArgumentValues.put("RESULT_PATH", this.getResultPath(basePath));

        return inputArgumentValues;
    }

    private File preprocessRadarImage() throws WacodisProcessingException {
        HashMap<String, String> parameters = new HashMap();

        //TODO set parameters for GPT processing
        //TODO consider CRS param (see below)

        InputDataPreprocessor imagePreprocessor = new GptPreprocessor(FilenameUtils.concat(
                this.getBackendConfig().getGpfDir(), GPF_FILE),
                parameters,
                TIFF_EXTENSION,
                this.getNamingSuffix());

        try {
            File sentinelFile = sentinelDownloader.downloadSentinelFile(
                    this.opticalImagesSource,
                    this.getBackendConfig().getWorkingDirectory());
            this.sentinelProduct = ProductIO.readProduct(sentinelFile.getPath());

            //TODO determine CRS from Sentinel as GPT preprocessing param

            List<File> preprocessedImages = imagePreprocessor.preprocess(sentinelProduct, this.getBackendConfig().getWorkingDirectory());
            if (preprocessedImages == null && preprocessedImages.isEmpty()) {
                throw new WacodisProcessingException("No preprocessed Sentinel files available.");
            }
            return executeGdalWarp(preprocessedImages.get(0), this.getBackendConfig().getEpsg());

        } catch (IOException ex) {
            LOGGER.debug("Error while reading Sentinel file: {}", this.opticalImagesSource, ex);
            throw new WacodisProcessingException("Could not preprocess Sentinel product", ex);
        }
    }
}
