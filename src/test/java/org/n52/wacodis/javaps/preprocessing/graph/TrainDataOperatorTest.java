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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.opengis.referencing.FactoryException;

/**
 *
 * @author Lukas Butzmann
 */
public class TrainDataOperatorTest {
    
    private static final String REFERENCE_DATA_FILE_NAME = "test-reference-data.json";
    private static final String REFERENCE_DATA_FILE_NAME_WITHOUT_CLASS_ATTRIBUTE = "test-reference-data-without-class.json";

    private SimpleFeatureCollection featureCollectionWithClassAttribute;
    private SimpleFeatureCollection featureCollectionWithoutClassAttribute;
    private TrainDataOperator operatorWithClass;
    private TrainDataOperator operatorWithoutClass;

    @Before
    public void init() throws IOException {
        InputStream inputWithClass = this.getClass().getClassLoader().getResourceAsStream(REFERENCE_DATA_FILE_NAME);
        this.featureCollectionWithClassAttribute = (SimpleFeatureCollection) new FeatureJSON().readFeatureCollection(inputWithClass);
        this.operatorWithClass = new TrainDataOperator("class");
        
        InputStream inputWithoutClass = this.getClass().getClassLoader().getResourceAsStream(REFERENCE_DATA_FILE_NAME_WITHOUT_CLASS_ATTRIBUTE);
        this.featureCollectionWithoutClassAttribute = (SimpleFeatureCollection) new FeatureJSON().readFeatureCollection(inputWithoutClass);
        this.operatorWithoutClass = new TrainDataOperator("noclass");
    }

    @Test
    public void testPreprocessingForValidFeatureCollectionWithClass() throws WacodisProcessingException, MalformedURLException, IOException, FactoryException {
        SimpleFeatureCollection resultCollection = this.operatorWithClass.process(this.featureCollectionWithClassAttribute);
        Assert.assertFalse(resultCollection.isEmpty());
    }
    
    @Test
    public void testPreprocessingForValidFeatureCollectionWithoutClass() throws WacodisProcessingException, MalformedURLException, IOException, FactoryException {
        SimpleFeatureCollection resultCollection2 = this.operatorWithoutClass.process(this.featureCollectionWithoutClassAttribute);
        Assert.assertEquals("class", resultCollection2.getSchema().getDescriptor("class").getLocalName());
        Assert.assertFalse(resultCollection2.isEmpty());
    }
}
