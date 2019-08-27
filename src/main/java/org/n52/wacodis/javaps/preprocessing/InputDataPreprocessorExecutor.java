/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.io.File;
import java.util.List;
import org.n52.wacodis.javaps.WacodisProcessingException;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class InputDataPreprocessorExecutor<T> {

    private T inputData;
    private InputDataPreprocessor<T> preprocessor;
    private String outputDirectoryPath;
    private String epsg;
    private String processInputId;

    public InputDataPreprocessorExecutor(T inputData, InputDataPreprocessor<T> preprocessor, String outputDirectoryPath, String epsg, String processInputId) {
        this.inputData = inputData;
        this.preprocessor = preprocessor;
        this.outputDirectoryPath = outputDirectoryPath;
        this.epsg = epsg;
        this.processInputId = processInputId;
    }

    public String getProcessInputId() {
        return processInputId;
    }

    public List<File> execute() throws WacodisProcessingException {
        return preprocessor.preprocess(inputData, outputDirectoryPath, epsg);
    }
}
