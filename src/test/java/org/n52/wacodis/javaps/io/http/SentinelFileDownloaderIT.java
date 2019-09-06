/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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

    private static final String SENTINEL_FILE_URL = "https://scihub.copernicus.eu/dhus/odata/v1/Products('62b81eaa-2ee3-4771-8016-d3c5f15357cb')/$value";
    private static final String NON_VALID_URL = "non/valid/url";
    private static final String TMP_IMG_DIR_PREFIX = "tmp-images-dir";

    @Autowired
    private OpenAccessHubConfig openAccessHubConfig;

    @Autowired
    private SentinelFileDownloader downloader;

    private Path tmpImageDir;

    @Before
    public void init() throws IOException {
        this.tmpImageDir = Files.createTempDirectory(TMP_IMG_DIR_PREFIX);
    }

    @Test
    public void testSentinelFileDownloadAndUnzip() throws IOException {
        File sentinelFile = downloader.downloadSentinelFile(SENTINEL_FILE_URL,
                tmpImageDir.toString(), true);

        Assert.assertTrue(sentinelFile.isDirectory());
        Assert.assertTrue(sentinelFile.getName().endsWith(".SAFE"));
    }

    @Test
    public void testSentinelFileDownloadWithouUnzip() throws IOException {
        File sentinelFile = downloader.downloadSentinelFile(SENTINEL_FILE_URL,
                tmpImageDir.toString(), false);

        Assert.assertTrue(sentinelFile.isFile());
        Assert.assertTrue(sentinelFile.getName().endsWith(".zip"));
    }

    @Test(expected = IOException.class)
    public void testSentinelFileDownloadWithNonValidUrl() throws IOException {

        downloader.downloadSentinelFile(NON_VALID_URL,
                tmpImageDir.toString(), false);

    }

    @After
    public void shutdown() throws IOException {
        FileUtils.deleteDirectory(this.tmpImageDir.toFile());
    }
}
