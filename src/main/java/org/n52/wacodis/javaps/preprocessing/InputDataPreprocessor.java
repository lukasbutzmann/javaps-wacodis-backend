/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface InputDataPreprocessor<T> {

    /**
     * Applies preprocessing on input data and stores the preprocessed at
     *
     * @param inputFilePath path to the input data
     * @param outputDirectoryPath path to the directory for storing the
     * preprocessed data
     * @return list of preprocessed files
     * @throws IOException
     */
    public List<File> preprocess(T inputFilePath, String outputDirectoryPath) throws IOException;
}
