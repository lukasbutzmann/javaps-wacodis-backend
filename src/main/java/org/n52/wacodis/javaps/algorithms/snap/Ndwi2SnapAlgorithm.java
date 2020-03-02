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
        identifier = "de.hsbo.wacodis.snap.ndwi2",
        title = " Normalized Difference Water Index 2",
        abstrakt = "Calculate the NDVWI2 for a Sentinel-2 image, allowing for the measurement of surface water extent",
        version = "1.0.0",
        storeSupported = true,
        statusSupported = true)
public class Ndwi2SnapAlgorithm extends AbstractSnapAlgorithm{
    
    private static final String NDWI2_OPERATOR_NAME = "Ndwi2Op";
    private static final String RESULT_NAME_PREFIX = "snap_ndwi2_result";

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
        return NDWI2_OPERATOR_NAME;
    }

    @Override
    public Map<String, Object> prepareOperationParameters() {
        Map<String, Object> parameters = new HashMap();
        parameters.put("greenFactor", 1.0f);
        parameters.put("nirFactor", 1.0f);
        parameters.put("greenSourceBand", "B3");
        parameters.put("nirSourceBand", "B8");
        return parameters;
    }
    
}
