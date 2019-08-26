/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.http;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.wacodis.javaps.configuration.OpenAccessHubConfig;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OpenAccessHubConfig.class, SentinelFileDownloader.class, WacodisBackendConfig.class})
public class SentinelFileDownloaderIT {

    private static final String SENTINEL_FILE_URL = "https://scihub.copernicus.eu/dhus/odata/v1/Products('305585dc-7a22-4e40-9260-e19b9b804f36')/$value";
    private static final String NON_VALID_URL = "non/valid/url";

    @Autowired
    private OpenAccessHubConfig openAccessHubConfig;

    @Autowired
    private WacodisBackendConfig backendConfig;

    @Autowired
    private SentinelFileDownloader downloader;

    @Test
    public void testSentinelFileDownloadAndUnzip() throws IOException {
        File sentinelFile = downloader.downloadSentinelFile(SENTINEL_FILE_URL,
                backendConfig.getWorkingDirectory(), true);

        Assert.assertTrue(sentinelFile.isDirectory());
        Assert.assertTrue(sentinelFile.getName().endsWith(".SAFE"));
    }

    @Test
    public void testSentinelFileDownloadWithouUnzip() throws IOException {
        File sentinelFile = downloader.downloadSentinelFile(SENTINEL_FILE_URL,
                backendConfig.getWorkingDirectory(), false);

        Assert.assertTrue(sentinelFile.isFile());
        Assert.assertTrue(sentinelFile.getName().endsWith(".ZIP"));
    }

    @Test(expected = IOException.class)
    public void testSentinelFileDownloadWithNonValidUrl() throws IOException {

        downloader.downloadSentinelFile(NON_VALID_URL,
                backendConfig.getWorkingDirectory(), false);

    }
}
