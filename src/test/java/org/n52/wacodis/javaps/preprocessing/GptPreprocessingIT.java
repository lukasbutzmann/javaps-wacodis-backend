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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.wacodis.javaps.WacodisConfigurationException;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.configuration.OpenAccessHubConfig;
import org.n52.wacodis.javaps.io.http.SentinelFileDownloader;
import org.opengis.referencing.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {OpenAccessHubConfig.class, SentinelFileDownloader.class, WacodisBackendConfig.class})
public class GptPreprocessingIT {

    private static final String SENTINEL_FILE_PATH = "path/to/sentinelfile.SAFE";
    private static final String GPT_FILE = "test-gpt-graph.xml";
    private static final String AREA_PARAM = "area";
    private static final String AREA_VALUE = "POLYGON ((1.6887879371643066 46.937259674072266, 1.6887879371643066 46.94419479370117, 1.697301983833313 46.94419479370117, 1.697301983833313 46.937259674072266, 1.6887879371643066 46.937259674072266))";
    private static final String INVALID_AREA_VALUE = "POLYGON ((1.6887879371643066 46.937259674072266, 1.6887879371643066 46.94419479370117, 1.697301983833313 46.94419479370117, 1.697301983833313 46.937259674072266))";
    private static final String TIFF_EXTENSION = ".tif";
    private static final String TMP_IMAGE_DIR_PREFIX = "tmp-image-dir";

    @Autowired
    private OpenAccessHubConfig openAccessHubConfig;

    @Autowired
    private SentinelFileDownloader downloader;

    private Product sentinelProduct;
    private InputDataPreprocessor<Product> imagePreprocessor;
    private Path tmpImageDir;
    private String gptPath;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws IOException {
        gptPath = this.getClass().getClassLoader()
                .getResource(GPT_FILE).getPath();
        this.sentinelProduct = ProductIO.readProduct(SENTINEL_FILE_PATH);

        this.tmpImageDir = Files.createTempDirectory(TMP_IMAGE_DIR_PREFIX);
    }

    @Test
    public void testGptPreprocessing() throws WacodisProcessingException, IOException {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(AREA_PARAM, AREA_VALUE);
        this.imagePreprocessor = new GptPreprocessor(gptPath, parameters, TIFF_EXTENSION, "");
        File result = this.imagePreprocessor.preprocess(this.sentinelProduct, this.tmpImageDir.toString()).get(0);
        Product resProduct = ProductIO.readProduct(result.getPath());

        Assert.assertEquals(3, resProduct.getNumBands());
        Assert.assertTrue(resProduct.getSceneRasterWidth() < this.sentinelProduct.getSceneRasterWidth());
        Assert.assertTrue(resProduct.getSceneRasterHeight() < this.sentinelProduct.getSceneRasterHeight());
        resProduct.closeIO();
        resProduct.dispose();
    }

    @Test
    public void testGptPreprocessingForInvalidParamezer() throws WacodisProcessingException {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(AREA_PARAM, INVALID_AREA_VALUE);
        this.imagePreprocessor = new GptPreprocessor(gptPath, parameters, TIFF_EXTENSION, "");

        exception.expect(WacodisProcessingException.class);
        this.imagePreprocessor.preprocess(this.sentinelProduct, this.tmpImageDir.toString()).get(0);
    }

    @After
    public void shutdown() throws IOException {
        FileUtils.deleteDirectory(this.tmpImageDir.toFile());
    }

}
