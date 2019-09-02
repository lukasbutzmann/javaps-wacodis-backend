/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import com.vividsolutions.jts.geom.MultiPolygon;
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
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Arrays;
import java.util.UUID;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class ReferenceDataPreprocessor implements InputDataPreprocessor<SimpleFeatureCollection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataPreprocessor.class);

    private static final String OUTPUT_FILENAME_PREFIX = "wacodis_traindata_";
    private static final String LANDCOVERCLASS_ATTRIBUTE = "class";
    private static final String[] SHAPEFILE_EXTENSIONS = new String[]{"shp", "shx", "dbf", "prj", "fix"};

    private String sourceEpsg;
    private String targetEpsg;
    private String outputFilenamesSuffix;

    /**
     * @param sourceEpsg set srs for origin SimpleFeatureCollection
     * @param targetEpsg set srs for resulting shapefile, assumes WGS84 if not
     * set
     */
    public ReferenceDataPreprocessor(String sourceEpsg, String targetEpsg) {
        this.sourceEpsg = sourceEpsg;
        this.targetEpsg = targetEpsg;
    }

    public ReferenceDataPreprocessor(String sourceEpsg, String targetEpsg, String outputFilenamesSuffix) {
        this.sourceEpsg = sourceEpsg;
        this.targetEpsg = targetEpsg;
        this.outputFilenamesSuffix = outputFilenamesSuffix;
    }

    public ReferenceDataPreprocessor() {
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

        CoordinateReferenceSystem targetCrs = decodeCRS(targetEpsg);
        if (!sourceEpsg.equals(targetEpsg)) {
            //reproject inputCollection
            CoordinateReferenceSystem sourceCrs = decodeCRS(sourceEpsg);

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

            boolean isInputSchemaValid = validateInputSchema(inputCollection.getSchema());
            if (!isInputSchemaValid) {
                String msg = "cannot write features, input schema is invalid";
                LOGGER.debug(msg);
                throw new WacodisProcessingException(msg);
            }

            //create new shapefile datastore with schema for trainig data
            DataStore dataStore = dataStoreFactory.createNewDataStore(params);
            Class geometryBinding = getGeometryTypeFromSchema(inputCollection.getSchema());
            SimpleFeatureType outputSchema = createReferenceDataFeatureType(targetCrs, geometryBinding); //traning data schema
            dataStore.createSchema(outputSchema);

            writeFeaturesToDataStore(dataStore, inputCollection, referenceDataShapefile); //write features to shapefile
            return Arrays.asList(outputFiles);
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating shape file.", ex);
        }
    }

    private void writeFeaturesToDataStore(DataStore dataStore, SimpleFeatureCollection reprojectInputCollection, File referenceDataFile) throws IOException {
        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
                LOGGER.debug("starting transaction for file " + referenceDataFile.getName());

                featureStore.addFeatures(reprojectInputCollection);
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
     * schema must include attribute 'class' of type Integer or Long
     *
     * @param inputSchema
     * @return
     */
    private boolean validateInputSchema(SimpleFeatureType inputSchema) {
        AttributeDescriptor classAttribute = inputSchema.getDescriptor(LANDCOVERCLASS_ATTRIBUTE);

        if (classAttribute == null) { //check if class attribute exits
            LOGGER.warn("input schema does not contain mandatory attribute " + LANDCOVERCLASS_ATTRIBUTE);
            return false;
        } else { //check datatype of class attribute
            AttributeType type = inputSchema.getType(LANDCOVERCLASS_ATTRIBUTE);
            Class binding = type.getBinding();

            if (!binding.equals(Integer.class) && !binding.equals(Long.class)) {
                LOGGER.warn("attribute " + LANDCOVERCLASS_ATTRIBUTE + " is of type " + binding.getSimpleName() + ", expected Integer or Long");
                return false;
            }
        }

        return true;
    }

    private Class getGeometryTypeFromSchema(SimpleFeatureType schema) {
        GeometryDescriptor geomAttribute = schema.getGeometryDescriptor();
        GeometryType geomType = geomAttribute.getType();
        Class geomBinding = geomType.getBinding();

        LOGGER.debug("get geometry attribute " + geomAttribute.getLocalName() + " of type " + geomBinding.getSimpleName());

        if (!geomBinding.equals(Polygon.class) && !geomBinding.equals(MultiPolygon.class)) {
            LOGGER.warn("geometry attribute " + geomAttribute.getLocalName() + " is of datatype " + geomBinding.getSimpleName() + " , expected Polygon or MultiPolygon");
        }

        return geomBinding;
    }

    /**
     * get schema for landclassification training data
     *
     * @return
     */
    public SimpleFeatureType retrieveDefaultSchema() {
        CoordinateReferenceSystem crs = determineCRS();
        return createReferenceDataFeatureType(crs, null); //MultiPolygon
    }

    /**
     * schema for landcover classification training data
     *
     * @param crs
     * @param geometryBinding binding for geometry attribute, assume
     * MultiPolygon if null
     * @return
     */
    private SimpleFeatureType createReferenceDataFeatureType(CoordinateReferenceSystem crs, Class geometryBinding) {
        if (geometryBinding == null) {
            geometryBinding = MultiPolygon.class;
            LOGGER.warn("geometry binding not set, assume MultiPolygon");
        }

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("referencedata");
        builder.setCRS(crs);

        builder.add("the_geom", geometryBinding); //geometry attribute
        builder.add("id", Integer.class);
        builder.add(LANDCOVERCLASS_ATTRIBUTE, Integer.class); //landcover classification

        return builder.buildFeatureType();
    }

    /**
     * @return CoordinateReferenceSystem for this.epsg or WGS84 if this.epsg is
     * not set or unparsable
     */
    private CoordinateReferenceSystem determineCRS() {
        CoordinateReferenceSystem crs;

        if (this.targetEpsg != null) {
            crs = decodeCRS(this.targetEpsg);
        } else {
            LOGGER.warn("targetEpsg is not set, assume default crs WGS84");
            crs = DefaultGeographicCRS.WGS84;
        }

        return crs;
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
