/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import java.util.Map;
import org.n52.wacodis.javaps.WacodisProcessingException;

/**
 * Interface for writing an input in a certain file format to a target
 * directory.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface InputDataWriter<T> {

    /**
     * Get the unique name for this writer.
     *
     * @return The writer's unique name
     */
    public String getWriterName();

    /**
     * Writes the input to a specified target directory.
     *
     * @param input The input to write out.
     * @param targetDirectory Directory where the input will be stored.
     * @param parameters
     * @throws WacodisProcessingException
     */
    public abstract void write(T input, String targetDirectory, Map parameters) throws WacodisProcessingException;

    /**
     * Selects those additional parameters from a parameter {@link Map} that are
     * required for writing out the input data.
     *
     * @param parameters {@link Map} of parameters that contains different
     * processing parameters.
     * @return A subset of the passed parameters that contains only the required
     * parameters for this writer.
     */
    public abstract Map getAdditionalWritingParameters(Map parameters);

    /**
     * Selects the target directory from a {@link Ma} of various paramaters.
     *
     * @param parameters {@link Map} of parameters that contains different
     * processing parameters.
     * @return The target directory to use for writing out the input data
     */
    public abstract String getTargetDirecetory(Map parameters);

    /**
     * Gets the class name that is supported for writing out input data.
     *
     * @return Class name that is supported for writing out input data.
     */
    public abstract String getSupportedClassName();

}
