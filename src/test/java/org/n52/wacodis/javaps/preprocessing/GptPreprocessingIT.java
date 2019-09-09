/*
 * Copyright 2019 <a href="mailto:s.drost@52north.org">Sebastian Drost</a>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.configuration.OpenAccessHubConfig;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.opengis.referencing.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {OpenAccessHubConfig.class, SentinelFileDownloader.class, WacodisBackendConfig.class})
public class GptPreprocessingIT {

    private static final String SENTINEL_FILE_PATH = "path/to/example_sentinel2_file.SAFE";
    private static final String GPT_FILE = "test-gpt-graph.xml";
    private static final String TARGET_EPSG_CODE = "EPSG:4326";
    private static final String TIFF_EXTENSION = ".tif";
    private static final String TMP_IMAGE_DIR_PREFIX = "tmp-image-dir";

    @Autowired
    private OpenAccessHubConfig openAccessHubConfig;

    @Autowired
    private SentinelFileDownloader downloader;

    private Product sentinelProduct;
    private InputDataPreprocessor<Product> imagePreprocessor;
    private Path tmpImageDir;

    @Before
    public void init() throws IOException {
        String input = this.getClass().getClassLoader()
                .getResource(GPT_FILE).getPath();
        this.sentinelProduct = ProductIO.readProduct(SENTINEL_FILE_PATH);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("epsg", TARGET_EPSG_CODE);
        this.imagePreprocessor = new GptPreprocessor(input, parameters, TIFF_EXTENSION, "");

        this.tmpImageDir = Files.createTempDirectory(TMP_IMAGE_DIR_PREFIX);
    }

    @Test
    public void testGptPreprocessing() throws WacodisProcessingException, MalformedURLException, IOException, FactoryException {
        File result = this.imagePreprocessor.preprocess(this.sentinelProduct, this.tmpImageDir.toString()).get(0);
        Product resProduct = ProductIO.readProduct(result.getPath());

        Assert.assertEquals(3, resProduct.getNumBands());
        Assert.assertEquals(CRS.decode(TARGET_EPSG_CODE).toWKT(), resProduct.getSceneCRS().toWKT());
        resProduct.closeIO();
        resProduct.dispose();
    }

    @After
    public void shutdown() throws IOException {
        FileUtils.deleteDirectory(this.tmpImageDir.toFile());
    }

}
