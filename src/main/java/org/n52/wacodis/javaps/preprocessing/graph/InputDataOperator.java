/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

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
     * @return The processed input data.
     * @throws WacodisProcessingException
     */
    public abstract T process(T input) throws WacodisProcessingException;

    /**
     * Gets the class name that is supported for processing.
     *
     * @return Class name that is supported for processing.
     */
    public abstract String getSupportedClassName();
}
