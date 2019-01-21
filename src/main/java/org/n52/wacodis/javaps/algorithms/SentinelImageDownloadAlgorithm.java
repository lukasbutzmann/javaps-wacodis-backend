/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexInput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.n52.javaps.io.GenericFileData;
import org.n52.javaps.io.data.binding.complex.GenericFileDataBinding;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.sentinel_download_process",
        title = "Sentinel Download Process",
        abstrakt = "Perform a Sentinel file download.",
        version = "0.0.1",
        storeSupported = true,
        statusSupported = true)
public class SentinelImageDownloadAlgorithm {

    private GenericFileData referenceData;
    private String product;

    @ComplexInput(
            identifier = "SENTINEL_DATA",
            title = "Sentinel data",
            abstrakt = "Sentinel data from Open Access Hub",
            minOccurs = 1,
            maxOccurs = 1,
            binding = GenericFileDataBinding.class
    )
    public void setReferenceData(GenericFileData value) {
        this.referenceData = value;
    }

    @Execute
    public void execute() {
        //TODO Resolve ID for optical images and fetch images as GeoTIFF

        //TODO Resolve 
        product = referenceData.getBaseFile(false).getName();

    }

    @LiteralOutput(identifier = "PRODUCT")
    public String getOutput() {
        return this.product;
    }

}
