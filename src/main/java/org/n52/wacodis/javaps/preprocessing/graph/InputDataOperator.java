/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import java.util.Map;
import org.n52.wacodis.javaps.WacodisProcessingException;

/**
 * Operator for performing a single unit of preprocessing for certain input
 * data.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class InputDataOperator<T> {

    /**
     * The the unique name for this operator;
     *
     * @return The operator's unique name.
     */
    public abstract String getName();

    /**
     * Processes certain input data.
     *
     * @param input the input data to process.
     * @param parameters {@link Map} of processing parameters.
     * @return The processed input data.
     * @throws WacodisProcessingException
     */
    public abstract T process(T input, Map parameters) throws WacodisProcessingException;

    /**
     * Selects those additional parameters from a parameter {@link Map} that are
     * required for processing the input data.
     *
     * @param parameters {@link Map} of parameters that contains different
     * processing parameters.
     * @return A subset of the passed parameters that contains only the required
     * parameters for this processor.
     */
    public abstract Map getProcessingParameters(Map parameters) throws WacodisProcessingException;

    /**
     * Gets the class name that is supported for processing.
     *
     * @return Class name that is supported for processing.
     */
    public abstract String getSupportedClassName();
}
