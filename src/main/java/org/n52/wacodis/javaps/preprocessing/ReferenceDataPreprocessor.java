/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class ReferenceDataPreprocessor implements InputDataPreprocessor<SimpleFeatureCollection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataPreprocessor.class);

    private static final String OUTPUT_FILENAME_PREFIX = "wacodis_traindata";
    private static final String[] SHAPEFILE_EXTENSIONS = new String[]{"shp", "shx", "dbf", "prj", "fix"};

    private String sourceEpsg;
    private String targetEpsg;
    private String outputFilenamesSuffix;

    /**
     * Instantiates a preprocessor for vector reference data.
     *
     * @param targetEpsg set srs for resulting shapefile, assumes WGS84 if not
     * set
     */
    public ReferenceDataPreprocessor(String targetEpsg) {
        this.targetEpsg = targetEpsg;
    }

    /**
     * Instantiates a preprocessor for vector reference data.
     *
     * @param sourceEpsg set srs for origin SimpleFeatureCollection
     * @param targetEpsg set srs for resulting shapefile, assumes WGS84 if not
     * set
     */
    public ReferenceDataPreprocessor(String sourceEpsg, String targetEpsg) {
        this.sourceEpsg = sourceEpsg;
        this.targetEpsg = targetEpsg;
    }

    /**
     * Instantiates a preprocessor for vector reference data.
     *
     * @param sourceEpsg set srs for origin SimpleFeatureCollection
     * @param targetEpsg set srs for resulting shapefile, assumes WGS84 if not
     * set
     * @param outputFilenamesSuffix the suffix for the resulting shape file name
     */
    public ReferenceDataPreprocessor(String sourceEpsg, String targetEpsg, String outputFilenamesSuffix) {
        this.sourceEpsg = sourceEpsg;
        this.targetEpsg = targetEpsg;
        this.outputFilenamesSuffix = outputFilenamesSuffix;
    }

    public String getTargetEpsg() {
        return targetEpsg;
    }

    public String getOutputFilenamesSuffix() {
        return outputFilenamesSuffix;
    }

    /**
     * set suffix for output file names, random uuid if not set
     *
     * @param outputFilenamesSuffix
     */
    public void setOutputFilenamesSuffix(String outputFilenamesSuffix) {
        this.outputFilenamesSuffix = outputFilenamesSuffix;
    }

    /**
     * set srs for resulting shapefile, assumes WGS84 if not set
     *
     * @param targetEpsg
     */
    public void setTargetEpsg(String targetEpsg) {
        this.targetEpsg = targetEpsg;
    }

    /**
     * reprojects and writes a SimpleFeatureCollection to a Shapefile,
     *
     * @param inputCollection features must have a attribute 'class' of type
     * Long or Integer
     * @param outputDirectoryPath specifies the directory in which to store the
     * resulting files
     * @return array of File comprising all parts of a shapefile, order: [shp,
     * shx, dbf, prj, fix], filenames are randomized
     * @throws WacodisProcessingException
     */
    @Override
    public List<File> preprocess(SimpleFeatureCollection inputCollection, String outputDirectoryPath) throws WacodisProcessingException {
        DataStore dataStore = null;

        LOGGER.info("Start reference data preprocessing for FeatureType: {}",
                inputCollection.getSchema().getTypeName());
        CoordinateReferenceSystem targetCrs = decodeCRS(targetEpsg);

        if (sourceEpsg != null && !sourceEpsg.equals(targetEpsg)) {
            CoordinateReferenceSystem sourceCrs = decodeCRS(sourceEpsg);
            inputCollection = new ReprojectingFeatureCollection(inputCollection, sourceCrs, targetCrs);
        } else {
            inputCollection = new ReprojectingFeatureCollection(inputCollection, targetCrs);
        }

        try {
            File[] outputFiles = generateOutputFileNames(outputDirectoryPath);
            File referenceDataShapefile = outputFiles[0]; //.shp
            URL fileURL = referenceDataShapefile.toURI().toURL();

            //get datastore factory for shapefiles
            FileDataStoreFactorySpi dataStoreFactory = FileDataStoreFinder.getDataStoreFactory(SHAPEFILE_EXTENSIONS[0]); //.shp

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

            //create new shapefile datastore for reference data
            dataStore = dataStoreFactory.createNewDataStore(params);
            //derive and create output schema from input schema
            SimpleFeatureType outputSchema = constructOutputSchema(inputCollection.getSchema());
            dataStore.createSchema(outputSchema);

            //write features
            SimpleFeatureCollection outputCollection = transferFeaturesToOutputSchema(inputCollection, outputSchema);
            writeFeaturesToDataStore(dataStore, outputCollection, referenceDataShapefile); //write features to shapefile
            LOGGER.info("Reference data preprocessing succesfully finished for FeatureType: {}",
                    inputCollection.getSchema().getTypeName());
            return Arrays.asList(outputFiles);
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating shape file.", ex);
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }
        }
    }

    private void writeFeaturesToDataStore(DataStore dataStore, SimpleFeatureCollection features, File referenceDataFile) throws IOException {
        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
                LOGGER.debug("starting transaction for file " + referenceDataFile.getName());

                featureStore.addFeatures(features);
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

    private CoordinateReferenceSystem decodeCRS(String epsg) {
        CoordinateReferenceSystem crs;

        try {
            crs = CRS.decode(epsg);
        } catch (FactoryException ex) {
            LOGGER.error("could not decode epsg code " + epsg + ", using default crs WGS84", ex);
            crs = DefaultGeographicCRS.WGS84;
        }

        return crs;
    }

    /**
     * generate randomized file identifiers for output files (parts of the
     * shapefile)
     *
     * @param outputDirectoryPath
     * @return
     */
    private File[] generateOutputFileNames(String outputDirectoryPath) {
        String fileIdentifier = (this.outputFilenamesSuffix != null) ? this.outputFilenamesSuffix : UUID.randomUUID().toString();
        File[] outputFiles = new File[SHAPEFILE_EXTENSIONS.length];
        String fileName;

        for (int i = 0; i < outputFiles.length; i++) {
            fileName = OUTPUT_FILENAME_PREFIX + fileIdentifier + "." + SHAPEFILE_EXTENSIONS[i];
            outputFiles[i] = getOutputFile(outputDirectoryPath, fileName);
        }

        return outputFiles;
    }

    private File getOutputFile(String outputDirectoryPath, String fileName) {
        File file1 = new File(outputDirectoryPath);
        File file2 = new File(file1, fileName);

        return file2;
    }
}
