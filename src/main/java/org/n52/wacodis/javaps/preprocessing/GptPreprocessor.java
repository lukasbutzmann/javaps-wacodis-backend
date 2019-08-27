/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.esa.snap.core.datamodel.Product;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.preprocessing.gpt.GptArguments;
import org.n52.wacodis.javaps.preprocessing.gpt.GptExecutor;
import org.opengis.referencing.FactoryException;
import org.openide.util.Exceptions;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GptPreprocessor implements InputDataPreprocessor<Product> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GptPreprocessor.class);

    private final static String INPUT_KEY = "input";
    private final static String OUTPUT_KEY = "output";
    private final static String EPSG_KEY = "epsg";

    private GptExecutor executor;
    private String graphFile;
    private Map<String, String> parameters;
    private String outputFileExtension;
    private String outputFilenamesSuffix;

    /**
     * Creates a new GptPreprocesser that uses a GPF graph for preprocessing
     * input data.
     *
     * @param graphFile Path to the file that contains the definition for the
     * GPF graph.
     * @param outputFileExtension File extension for output filetype definition
     * @param outputFileNameSuffix Suffix for the output file
     */
    public GptPreprocessor(String graphFile, String outputFileExtension, String outputFileNameSuffix) {
        this.executor = new GptExecutor();
        this.graphFile = graphFile;
        this.outputFileExtension = outputFileExtension;
        this.outputFilenamesSuffix = outputFileNameSuffix;
    }

    /**
     * Creates a new GptPreprocesser that uses a GPF graph for preprocessing
     * input data.
     *
     * @param graphFile Path to the file that contains the definition for the
     * GPF graph.
     * @param parameters Parameters (without input and output parameters) that
     * are required for executing a GPF graph
     * @param outputFileExtension File extension for output filetype definition
     * @param outputFileNameSuffix Suffix for the output file
     */
    public GptPreprocessor(String graphFile, Map<String, String> parameters, String outputFileExtension, String outputFileNameSuffix) {
        this.executor = new GptExecutor();
        this.graphFile = graphFile;
        this.parameters = parameters;
        this.outputFileExtension = outputFileExtension;
        this.outputFilenamesSuffix = outputFileNameSuffix;
    }
    
    @Override
    public List<File> preprocess(Product product, String outputDirectoryPath, String epsg) throws WacodisProcessingException {
        Map<String, String> params = new HashMap();
        params.put(INPUT_KEY, product.getFileLocation().getPath());
        params.put(OUTPUT_KEY, FilenameUtils.concat(outputDirectoryPath,
                product.getName() + this.outputFilenamesSuffix + this.outputFileExtension));
        try {
            params.put(EPSG_KEY, (CRS.decode(epsg)).toWKT());
        } catch (FactoryException ex) {
            LOGGER.error("could not decode epsg code " + epsg + ", using default crs WGS84", ex);
            params.put(EPSG_KEY, DefaultGeographicCRS.WGS84.toWKT());
            Exceptions.printStackTrace(ex);
        }

        if (this.parameters != null) {
            params.putAll(this.parameters);
        }

        GptArguments arguments = new GptArguments(graphFile, params);
        try {
            executor.executeGraph(arguments);
        } catch (Exception ex) {
            throw new WacodisProcessingException("Error while executing GPF graph: " + arguments.getGraphFile(), ex);
        }
        return Arrays.asList(new File(params.get(OUTPUT_KEY)));
    }

}
