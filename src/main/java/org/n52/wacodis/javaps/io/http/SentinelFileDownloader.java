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
import org.apache.commons.io.FileUtils;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(SentinelFileDownloader.class);
    
    private final Map<String, File> productToFileCache = new HashMap<>();

    private WacodisBackendConfig config;

    private RestTemplate openAccessHubService;

    @Autowired
    public void setConfig(WacodisBackendConfig config) {
        this.config = config;
    }
    
    @Autowired
    public void setOpenAccessHubService(RestTemplate openAccessHubService) {
        this.openAccessHubService = openAccessHubService;
    }

    /**
     * Downloads a Sentinel-2 image file from the specified URL and writes it to
     * the default working directory.
     *
     * @param url URL for the Sentinel-2 image.
     * @return the file that contains the image
     * @throws IOException if internal file handling fails for some reason
     */
    public File downloadSentinelFile(String url) throws IOException {
        return downloadSentinelFile(url, config.getWorkingDirectory());
    }

    /**
     * Downloads a Sentinel-2 image file from the specified URL and writes it to
     * the specified location.
     *
     * @param url URL for the Sentinel-2 image.
     * @param outPath Path to the directory to save the image file in
     * @return the file that contains the image
     * @throws IOException if internal file handling fails for some reason
     */
    public File downloadSentinelFile(String url, String outPath) throws IOException {
        LOG.info("Downloading Sentinel product: {}", url);
        File cached = this.resolveProductFromCache(url);
        if (cached != null) {
            LOG.info("Returning cached version of the product: {}", url);
            return cached;
        }

        // Optional Accept header
        RequestCallback callback = (ClientHttpRequest request) -> {
            request.getHeaders()
                    .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        };

        ResponseExtractor<File> responseExtractor = (ClientHttpResponse response) -> {
            String fileName = response.getHeaders().getContentDisposition().getFilename();
            File imageFile = new File(outPath + "/" + fileName);
            FileUtils.copyInputStreamToFile(response.getBody(), imageFile);
            return imageFile;
        };
        
        File imageFile = openAccessHubService.execute(url, HttpMethod.GET, callback, responseExtractor);
        
        LOG.info("Downloading of Sentinel product successful: {}", url);
        
        synchronized (SentinelFileDownloader.this) {
            productToFileCache.put(url, imageFile);
        }

        return imageFile;
    }
    

    private synchronized File resolveProductFromCache(String url) {
        if (this.productToFileCache.containsKey(url)) {
            File candidateFile = this.productToFileCache.get(url);
            if (candidateFile != null && candidateFile.exists()) {
                return candidateFile;
            } else {
                // file does not exist any longer, remove it
                this.productToFileCache.remove(url);
            }
        }
        
        // no match found
        return null;
    }

}
