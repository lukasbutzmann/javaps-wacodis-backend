/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import org.apache.commons.io.FileUtils;
import org.geotools.GML;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LukasButzmann
 */
public class ShapeWriterTest {

    private static final String REFERENCE_DATA_FILE_NAME = "test-reference-data.xml";
    private static final String TMP_SHAPE_DIR_PREFIX = "tmp-ref-dir";

    private SimpleFeatureCollection featureCollection;
    private ShapeWriter shapeWriter;
    private File tempTargetFile;
    private Path tmpShapeDir;

    @Before
    public void init() throws IOException, ParserConfigurationException, SAXException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(REFERENCE_DATA_FILE_NAME);
        GML gml = new GML(GML.Version.WFS1_1);
        this.featureCollection = gml.decodeFeatureCollection(input);
        this.tmpShapeDir = Files.createTempDirectory(TMP_SHAPE_DIR_PREFIX);
        this.tempTargetFile = File.createTempFile("testFile", ".shp", tmpShapeDir.toFile());
        this.shapeWriter = new ShapeWriter(tempTargetFile);
    }

    @Test
    public void testPreprocessingForValidFeatureCollection() throws WacodisProcessingException, MalformedURLException, IOException, FactoryException {
        File resultFile = this.shapeWriter.write(this.featureCollection);
        SimpleFeatureCollection resultCollection = this.shpToFeatureCollection(resultFile);

        Assert.assertFalse(resultCollection.isEmpty());
        Assert.assertTrue(resultFile.exists());
        Assert.assertEquals("the_geom", resultCollection.getSchema().getGeometryDescriptor().getLocalName());
    }

    private SimpleFeatureCollection shpToFeatureCollection(File result) throws MalformedURLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("url", result.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source
                = dataStore.getFeatureSource(typeName);
        SimpleFeatureCollection collection = (SimpleFeatureCollection) source.getFeatures();

        return collection;
    }

    @After
    public void shutdown() throws IOException {
        FileUtils.deleteDirectory(this.tmpShapeDir.toFile());
    }
}
