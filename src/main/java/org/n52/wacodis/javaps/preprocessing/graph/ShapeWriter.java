/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;

import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.preprocessing.ReferenceDataPreprocessor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes out a {@link SimpleFeatureCollection} as a Shape file
 *
 * @author LukasButzmann, <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class ShapeWriter extends InputDataWriter<SimpleFeatureCollection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataPreprocessor.class);

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

            //check if params are suitable for creating shapefile
            boolean canProcess = dataStoreFactory.canProcess(params);
            if (!canProcess) {
                String msg = "cannot create datastore, params insufficient for datastore factory of class " + dataStoreFactory.getClass().getSimpleName();
                LOGGER.debug(msg + ", params: " + System.lineSeparator() + params.toString());
                throw new WacodisProcessingException(msg);
            }

            //create new shapefile datastore with schema
            DataStore dataStore = dataStoreFactory.createNewDataStore(params);
            SimpleFeatureType outputSchema = constructOutputSchema(inputCollection.getSchema());
            dataStore.createSchema(outputSchema);

            //write features
            SimpleFeatureCollection outputCollection = transferFeaturesToOutputSchema(inputCollection, outputSchema);
            writeFeaturesToDataStore(dataStore, outputCollection, this.getTargetFile()); //write features to shapefile
            LOGGER.info("Reference data preprocessing succesfully finished for FeatureType: {}",
                    inputCollection.getSchema().getTypeName());

            return this.getTargetFile();
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating shape file.", ex);
        }
    }

    private void writeFeaturesToDataStore(DataStore dataStore, SimpleFeatureCollection inputCollection, File referenceDataFile) throws IOException {
        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
                LOGGER.debug("starting transaction for file " + referenceDataFile.getName());

                featureStore.addFeatures(inputCollection);
                transaction.commit();

                LOGGER.debug("successfully commited to file " + referenceDataFile.getName());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                LOGGER.debug("error while writing to file " + referenceDataFile.getName() + ", rollback transaction", e);
                transaction.rollback();
            } finally {
                LOGGER.debug("closing transaction for file " + referenceDataFile.getName());
                transaction.close();
            }
        } else {
            LOGGER.error("cannot write features, unexpected feature source type " + featureSource.getClass().getSimpleName());
        }
    }

    /**
     * derive output schema from input schema (FeatureType)
     *
     * @param inputSchema
     * @return derived output schema
     */
    private SimpleFeatureType constructOutputSchema(SimpleFeatureType inputSchema) {
        List<AttributeDescriptor> outputAttributes = new ArrayList<>();
        GeometryType geomType = null;

        //find geometry attribute, carry over all other attributes
        for (AttributeDescriptor inputAttribute : inputSchema.getAttributeDescriptors()) {
            AttributeType type = inputAttribute.getType();
            if (type instanceof GeometryType && type.equals(inputSchema.getGeometryDescriptor().getType())) { //e.g. gml feature collections contain a second geometry column "location", treat this columns as non-geometry attribute
                geomType = (GeometryType) type;
            } else {
                outputAttributes.add(inputAttribute);
            }
        }

        //derive geometry type from input schema's geometry type and add to output schema attributes
        GeometryDescriptor outputGeometryDescriptor = createGeometryDescriptor(geomType, inputSchema.getGeometryDescriptor());
        outputAttributes.add(0, outputGeometryDescriptor); //should be the first item
        //create derived output schema
        SimpleFeatureType outputSchema = new SimpleFeatureTypeImpl(
                inputSchema.getName(), outputAttributes, outputGeometryDescriptor,
                inputSchema.isAbstract(), inputSchema.getRestrictions(),
                inputSchema.getSuper(), inputSchema.getDescription());

        return outputSchema;
    }

    private GeometryDescriptor createGeometryDescriptor(GeometryType geomType, GeometryDescriptor inputGeometryDescriptor) {
        GeometryTypeImpl gt = new GeometryTypeImpl(
                new NameImpl("the_geom"), geomType.getBinding(), //needs to be "the_geom" for shape files
                geomType.getCoordinateReferenceSystem(),
                geomType.isIdentified(), geomType.isAbstract(),
                geomType.getRestrictions(), geomType.getSuper(),
                geomType.getDescription());

        GeometryDescriptor outputGeometryDescriptor = new GeometryDescriptorImpl(
                gt, new NameImpl("the_geom"),
                inputGeometryDescriptor.getMinOccurs(), inputGeometryDescriptor.getMaxOccurs(),
                inputGeometryDescriptor.isNillable(), inputGeometryDescriptor.getDefaultValue());

        return outputGeometryDescriptor;
    }

    private SimpleFeatureCollection transferFeaturesToOutputSchema(SimpleFeatureCollection inputFeatures, SimpleFeatureType outputSchema) {
        List<SimpleFeature> outputFeatures = new ArrayList<>();

        //transfer every feature from input schema to output schema
        try (SimpleFeatureIterator featureIterator = inputFeatures.features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature inputFeature = featureIterator.next();
                SimpleFeature outputFeature = SimpleFeatureBuilder.build(outputSchema, inputFeature.getAttributes(), "");
                copyGeometryToGeometryColumn(inputFeature, outputFeature); //for whatever reason the geometry is not added to the correct geometry attribute by default

                outputFeatures.add(outputFeature);
            }
        }

        return new ListFeatureCollection(outputSchema, outputFeatures);
    }

    /**
     * copy geometry from input feature's geometry column to output feature's
     * geometry column
     *
     * @param inputFeature
     * @param outputFeature
     */
    private void copyGeometryToGeometryColumn(SimpleFeature inputFeature, SimpleFeature outputFeature) {
        int inputGeomAttrIndex = getGeometryAttributeIndex(inputFeature.getFeatureType());
        int outputGeomAttrIndex = getGeometryAttributeIndex(outputFeature.getFeatureType());

        Object geometry = inputFeature.getAttribute(inputGeomAttrIndex);
        outputFeature.setAttribute(outputGeomAttrIndex, geometry);
    }

    /**
     * @param schema
     * @return the index of the geometry attribute of a schema (FeatureType)
     */
    private int getGeometryAttributeIndex(SimpleFeatureType schema) {
        GeometryDescriptor geomDesc = schema.getGeometryDescriptor();
        Name columnName = geomDesc.getName();

        int index = schema.indexOf(columnName);

        return index;
    }

    @Override
    public String getSupportedClassName() {
        return SimpleFeatureCollection.class.getName();
    }

}
