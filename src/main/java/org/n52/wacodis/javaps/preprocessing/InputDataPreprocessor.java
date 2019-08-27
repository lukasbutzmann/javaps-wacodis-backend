/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.n52.wacodis.javaps.WacodisProcessingException;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface InputDataPreprocessor<T> {

    /**
     * Applies preprocessing on input data and stores the preprocessed at
     *
     * @param input input data
     * @param outputDirectoryPath path to the directory for storing the
     * @param epsg code for crs
     * preprocessed data
     * @return list of preprocessed files
     * @throws IOException
     */
    public List<File> preprocess(T input, String outputDirectoryPath, String epsg) throws WacodisProcessingException;
}