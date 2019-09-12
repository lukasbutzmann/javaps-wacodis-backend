/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for several combined input data operators.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class PreprocessingExecutor<T> {
    
    private static final Logger LOG = LoggerFactory.getLogger(PreprocessingExecutor.class);

    private InputDataWriter<T> writer;
    private List<InputDataOperator<T>> operatorList;

    public PreprocessingExecutor(InputDataWriter<T> writer, List<InputDataOperator<T>> operatorList) {
        this.writer = writer;
        this.operatorList = operatorList;
    }

    public PreprocessingExecutor(InputDataWriter<T> writer) {
        this.writer = writer;
        this.operatorList = new ArrayList();
    }

    public InputDataWriter<T> getWriter() {
        return writer;
    }

    public List<InputDataOperator<T>> getOperatorList() {
        return operatorList;
    }

    public void addOperator(InputDataOperator<T> operator) {
        this.operatorList.add(operator);
    }

    /**
     * Executes the list of input data operators for a certain input with
     * certain processing parameters.
     *
     * @param input The input that will be processed by the operators.
     * @return {@link File} that contains the preprocessed input data.
     * @throws WacodisProcessingException
     */
    public File executeOperators(T input) throws WacodisProcessingException {
        T preprocessedInput = input;
        for (InputDataOperator<T> op : this.operatorList) {
            LOG.info("Executing operator: {}.", op.getName());
            preprocessedInput = op.process(input);
        }
        LOG.info("Executing writer: {}.", writer.getWriterName());
        return this.writer.write(preprocessedInput);
    }

}
