
package org.n52.wacodis.javaps.io.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class SentinelFileDownloaderTest {
    
    @Test
    public void testCaching() throws IOException {
        RestTemplate rt = Mockito.mock(RestTemplate.class);
        Mockito.when(
                rt.execute(Mockito.any(String.class),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any()))
                .thenReturn(Files.createTempFile("tmp", "-del").toFile());
        
        WacodisBackendConfig config = Mockito.mock(WacodisBackendConfig.class);
        Mockito.when(config.getWorkingDirectory()).thenReturn(Files.createTempDirectory("wacodis-tmp").toFile().getAbsolutePath());
        
        SentinelFileDownloader sfd = new SentinelFileDownloader();
        sfd.setOpenAccessHubService(rt);
        sfd.setConfig(config);
        
        File f1 = sfd.downloadSentinelFile("https://test.file/test-product-id");
        
        RestTemplate rt2 = Mockito.mock(RestTemplate.class);
        Mockito.when(
                rt2.execute(Mockito.any(String.class),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any()))
                .thenReturn(Files.createTempFile("tmp", "-del").toFile());
        sfd.setOpenAccessHubService(rt2);
        
        File f2 = sfd.downloadSentinelFile("https://test.file/test-product-id");
        
        // second product is the same, should be same file
        Assert.assertThat(f1.getAbsolutePath(), CoreMatchers.equalTo(f2.getAbsolutePath()));
        
        RestTemplate rt3 = Mockito.mock(RestTemplate.class);
        Mockito.when(
                rt3.execute(Mockito.any(String.class),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any()))
                .thenReturn(Files.createTempFile("tmp", "-del").toFile());
        sfd.setOpenAccessHubService(rt3);
        
        File f3 = sfd.downloadSentinelFile("https://test.file/test-another-product-id");
        
        // another product, another file
        Assert.assertThat(f3.getAbsolutePath(), CoreMatchers.not(CoreMatchers.equalTo(f2.getAbsolutePath())));
        
        RestTemplate rt4 = Mockito.mock(RestTemplate.class);
        Mockito.when(
                rt4.execute(Mockito.any(String.class),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any()))
                .thenReturn(Files.createTempFile("tmp", "-del").toFile());
        sfd.setOpenAccessHubService(rt4);
        
        // remove the file
        f3.delete();
        
        File f4 = sfd.downloadSentinelFile("https://test.file/test-another-product-id");
        
        // same product, but file got removed
        Assert.assertThat(f3.getAbsolutePath(), CoreMatchers.not(CoreMatchers.equalTo(f4.getAbsolutePath())));
    }

}
