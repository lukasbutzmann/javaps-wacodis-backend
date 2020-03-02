/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.snap;

import java.util.HashMap;
import java.util.Map;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexOutput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.io.data.binding.complex.GeotiffFileDataBinding;
import org.n52.wacodis.javaps.io.data.binding.complex.ProductMetadataBinding;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.snap.ndvi",
        title = " Normalized Difference Vegetation Index",
        abstrakt = "Calculate the NDVI for a Sentinel-2 image.",
        version = "1.0.0",
        storeSupported = true,
        statusSupported = true)
public class NdviSnapAlgorithm extends AbstractSnapAlgorithm {

    private static final String NDVI_OPERATOR_NAME = "NdviOp";
    private static final String RESULT_NAME_PREFIX = "snap_ndvi_result";

    @LiteralInput(
            identifier = "SENTINEL_2_IMAGE_SOURCE",
            title = "Sentinel-2 image source",
            abstrakt = "Sources for the Sentinel-2 scene",
            minOccurs = 1,
            maxOccurs = 1)
    public void setOpticalImagesSources(String value) {
        this.setSentinel2ImageSource(value);
    }

    @ComplexOutput(
            identifier = "PRODUCT",
            binding = GeotiffFileDataBinding.class
    )
    public GenericFileData getOutput() throws WacodisProcessingException {
        return this.createProductOutput(this.getResultPath());
    }

    @ComplexOutput(
            identifier = "METADATA",
            binding = ProductMetadataBinding.class
    )
    public ProductMetadata getMetadata() {
        return this.getProductMetadata();
    }

    @Execute
    public void callExecute() throws WacodisProcessingException {
        this.execute();
    }

    public String getResultNamePrefix() {
        return RESULT_NAME_PREFIX;
    }

    public String getOperatorName() {
        return NDVI_OPERATOR_NAME;
    }

    public Map<String, Object> prepareOperationParameters() {
        Map<String, Object> parameters = new HashMap();
        parameters.put("redFactor", 1.0f);
        parameters.put("nirFactor", 1.0f);
        parameters.put("redSourceBand", "B4");
        parameters.put("nirSourceBand", "B8");
        return parameters;
    }

}
