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
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.preprocessing.gpt.GptArguments;
import org.n52.wacodis.javaps.preprocessing.gpt.GptExecutor;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GptPreprocessor implements InputDataPreprocessor<Product> {

    private final static String INPUT_KEY = "input";
    private final static String OUTPUT_KEY = "output";

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
    public List<File> preprocess(Product product, String outputDirectoryPath) throws WacodisProcessingException {
        Map<String, String> params = new HashMap();
        params.put(INPUT_KEY, product.getFileLocation().getPath());
        params.put(OUTPUT_KEY, FilenameUtils.concat(outputDirectoryPath,
                product.getName() + this.outputFilenamesSuffix + this.outputFileExtension));

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
