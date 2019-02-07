/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

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

    private String referenceData;
    private String product;

    @Autowired
    private RestTemplate openAccessHubService;

    @LiteralInput(
            identifier = "SENTINEL_DATA",
            title = "Sentinel data",
            abstrakt = "Sentinel data from Open Access Hub",
            minOccurs = 1,
            maxOccurs = 1
    )
    public void setReferenceData(String value) {
        this.referenceData = value;
    }

    @Execute
    public void execute() {
        //TODO Resolve ID for optical images and fetch images as GeoTIFF

        // Optional Accept header
        RequestCallback callback = (ClientHttpRequest request) -> {
            request.getHeaders()
                    .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        };

        ResponseExtractor<String> responseExtractor = (ClientHttpResponse response) -> {
//            File file = Files.response.
            return response.getStatusText();
        };

        String code = openAccessHubService.execute(referenceData, HttpMethod.GET, callback, responseExtractor);
        this.product = code;
    }

    @LiteralOutput(identifier = "PRODUCT")
    public String getOutput() {
        return this.product;
    }

}
