/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.http;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Lukas Butzmann
 */
@Component
public class WCSFileDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(WCSFileDownloader.class);

    private final Map<String, File> productToFileCache = new HashMap<>();
    
    private WacodisBackendConfig config;

    private RestTemplate basicHttpService;

    @Autowired
    public void setConfig(WacodisBackendConfig config) {
        this.config = config;
    }

    @Autowired
    public void setWCSService(@Qualifier("basicHttpService")RestTemplate basicHttpService) {
        this.basicHttpService = basicHttpService;
    }
    
     /**
     * Downloads a WCS image file from the specified URL and writes it to
     * the default working directory.
     *
     * @param url getCoverage URL for the WCS.
     * @return the file that contains the image
     * @throws IOException if internal file handling fails for some reason
     */
    public File downloadWCSFile(String url) throws IOException {
        return downloadWCSFile(url, config.getWorkingDirectory());
    }
    
    /**
     * Downloads a WCS image file from the specified URL and writes it to
     * the specified location.
     *
     * @param url getCoverage URL for the WCS.
     * @param outPath Path to the directory to save the image file in
     * @return the file that contains the image
     * @throws IOException if internal file handling fails for some reason
     */
    public File downloadWCSFile(String url, String outPath) throws IOException {

        LOG.info("Downloading WCS product: {}", url);

        // Optional Accept header
        RequestCallback callback = (ClientHttpRequest request) -> {
            request.getHeaders()
                    .setAccept(Arrays.asList(MediaType.valueOf("image/tiff")));
        };

        ResponseExtractor<File> responseExtractor = (ClientHttpResponse response) -> {
            String fileName = response.getHeaders().getContentDisposition().getFilename();
            File imageFile = new File(outPath + "/" + UUID.randomUUID().toString() + "_"+ fileName);
            FileUtils.copyInputStreamToFile(response.getBody(), imageFile);
            return imageFile;
        };

        try {
            File imageFile = basicHttpService.execute(url, HttpMethod.GET, callback, responseExtractor);

            LOG.info("Downloading of WCS product successful: {}", url);

            synchronized (WCSFileDownloader.this) {
                productToFileCache.put(url, imageFile);
            }

            return imageFile;
        } catch (HttpStatusCodeException ex) {
            LOG.error("GET request for WCS file {} returned status code: {}.",
                    ex.getStatusCode());
            throw new IOException(ex);
        } catch (RestClientException ex) {
            LOG.error("Unexpected client error while requesting WCS file ", ex.getMessage());
            throw new IOException(ex);
        }
    }
}
