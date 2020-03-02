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
 * @author LukasButzmann
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.snap.wdvi",
        title = "Weighted Difference Vegetation Index",
        abstrakt = "Weighted Difference Vegetation Index retrieves the Isovegetation lines parallel to soil line. Soil line has an arbitrary slope and passes through origin.",
        version = "1.0.0",
        storeSupported = true,
        statusSupported = true)
public class WdviSnapAlgorithm extends AbstractSnapAlgorithm {

    private static final String WDVI_OPERATOR_NAME = "WdviOp";
    private static final String RESULT_NAME_PREFIX = "snap_wdvi_result";

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
        return WDVI_OPERATOR_NAME;
    }

    public Map<String, Object> prepareOperationParameters() {
        Map<String, Object> parameters = new HashMap();
        parameters.put("redFactor", 1.0f);
        parameters.put("nirFactor", 1.0f);
        parameters.put("slopeSoilLine", 1.5f);
        parameters.put("redSourceBand", "B4");
        parameters.put("nirSourceBand", "B8");
        return parameters;
    }

}
