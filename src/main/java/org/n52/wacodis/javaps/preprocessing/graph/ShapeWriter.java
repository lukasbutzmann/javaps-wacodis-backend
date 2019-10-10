/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import org.geotools.data.simple.SimpleFeatureCollection;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;

import org.n52.wacodis.javaps.WacodisProcessingException;


/**
 *
 * @author LukasButzmann
 */
public class ShapeWriter extends InputDataWriter<SimpleFeatureCollection> {

    private static final String SHAPEFILE_EXTENSIONS = "shp";

    public ShapeWriter(File targetFile) {
        super(targetFile);
    }

    @Override
    public String getWriterName() {
        return "org.wacodis.writer.Shapewriter";
    }

    @Override
    public File write(SimpleFeatureCollection inputCollection) throws WacodisProcessingException {

        try {
            URL fileURL = super.getTargetFile().toURI().toURL();

            //get datastore factory for shapefiles
            FileDataStoreFactorySpi dataStoreFactory = FileDataStoreFinder.getDataStoreFactory(SHAPEFILE_EXTENSIONS);

            //params for creating shapefile
            Map<String, Serializable> params = new HashMap<>();
            params.put("url", fileURL); //filename
            params.put("create spatial index", Boolean.TRUE);

            //create new shapefile datastore with schema
            DataStore dataStore = dataStoreFactory.createNewDataStore(params);
            dataStore.createSchema(inputCollection.getSchema());

            writeFeaturesToDataStore(dataStore, inputCollection); //write features to shapefile


            return super.getTargetFile();
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating shape file.", ex);
        }
    }

    private void writeFeaturesToDataStore(DataStore dataStore, SimpleFeatureCollection inputCollection) throws IOException {
        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {

                featureStore.addFeatures(inputCollection);
                transaction.commit();

            } catch (IOException e) {
                transaction.rollback();
            } finally {
                transaction.close();
            }
        }
    }

    @Override
    public String getSupportedClassName() {
        return SimpleFeatureCollection.class.getName();
    }

}
