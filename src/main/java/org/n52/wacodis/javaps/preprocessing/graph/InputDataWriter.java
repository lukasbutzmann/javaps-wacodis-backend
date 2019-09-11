/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import java.io.File;
import java.util.Map;
import org.n52.wacodis.javaps.WacodisProcessingException;

/**
 * Interface for writing an input in a certain file format to a target
 * directory.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class InputDataWriter<T> {

    private final String targetDirectory;

    /**
     * Instantiates a new writer for input data that stores the data to the
     * specified target directory.
     *
     * @param targetDirectory The directory to which the input data will be
     * written.
     */
    public InputDataWriter(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    /**
     * Get the unique name for this writer.
     *
     * @return The writer's unique name
     */
    public abstract String getWriterName();

    /**
     * Writes the input to a specified target directory.
     *
     * @param input The input to write out.
     * @return {@link File} that contains the preprocessed input data.
     * @throws WacodisProcessingException
     */
    public abstract File write(T input) throws WacodisProcessingException;

    /**
     * Gets the class name that is supported for writing out input data.
     *
     * @return Class name that is supported for writing out input data.
     */
    public abstract String getSupportedClassName();

}
