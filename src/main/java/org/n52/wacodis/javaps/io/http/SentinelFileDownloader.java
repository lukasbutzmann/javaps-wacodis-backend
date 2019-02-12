/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.http;

import java.io.File;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * Helper class for downloading Sentinel image files.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SentinelFileDownloader {

    @Autowired
    private WacodisBackendConfig config;

    @Autowired
    private RestTemplate openAccessHubService;

    /**
     * Downloads a Sentinel-2 image file from the specified URL and writes it to
     * the default working directory.
     *
     * @param url URL for the Sentinel-2 image.
     * @param outPath Output file path
     * @return the file that contains the image
     */
    public File downloadSentinelFile(String url) {
        return downloadSentinelFile(url, config.getWorkingDirectory());
    }

    /**
     * Downloads a Sentinel-2 image file from the specified URL and writes it to
     * the specified location.
     *
     * @param url URL for the Sentinel-2 image.
     * @param outPath Output file path
     * @return the file that contains the image
     */
    public File downloadSentinelFile(String url, String outPath) {

        // Optional Accept header
        RequestCallback callback = (ClientHttpRequest request) -> {
            request.getHeaders()
                    .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        };

        ResponseExtractor<File> responseExtractor = (ClientHttpResponse response) -> {
            ;
            File imageFile = new File(outPath + "/"
                    + response.getHeaders().getContentDisposition().getFilename());
            FileUtils.copyInputStreamToFile(response.getBody(), imageFile);
            return imageFile;
        };

        File imageFile = openAccessHubService.execute(url, HttpMethod.GET, callback, responseExtractor);

        return imageFile;
    }

}
