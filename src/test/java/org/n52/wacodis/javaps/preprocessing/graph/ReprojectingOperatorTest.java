/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.opengis.referencing.FactoryException;
/**
 *
 * @author LukasButzmann
 */
public class ReprojectingOperatorTest {
    
    private static final String REFERENCE_DATA_FILE_NAME = "test-reference-data.json";
    private static final String TARGET_EPSG_CODE = "EPSG:4326";

    private SimpleFeatureCollection featureCollection;
    private ReprojectingOperator operator;
    
    @Before
    public void init() throws IOException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(REFERENCE_DATA_FILE_NAME);
        this.featureCollection = (SimpleFeatureCollection) new FeatureJSON().readFeatureCollection(input);
        this.operator = new ReprojectingOperator(TARGET_EPSG_CODE);
    }

    @Test
    public void testPreprocessingForValidFeatureCollection() throws WacodisProcessingException, MalformedURLException, IOException, FactoryException {
        SimpleFeatureCollection resultCollection = this.operator.process(this.featureCollection);

        Assert.assertFalse(resultCollection.isEmpty());
        Assert.assertEquals(CRS.decode(TARGET_EPSG_CODE).toWKT(),
                resultCollection.getSchema().getCoordinateReferenceSystem().toWKT());
    }
}
