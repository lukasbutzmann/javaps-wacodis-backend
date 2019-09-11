/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.n52.wacodis.javaps.WacodisProcessingException;

/**
 * Executor for several combined input data operators.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class PreprocessingExecutor<T> {

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
     * @param parameters Paramters that will be used for executing the
     * operators.
     * @throws WacodisProcessingException
     */
    public void executeOperators(T input, Map parameters) throws WacodisProcessingException {
        T preprocessedInput = input;
        for (InputDataOperator<T> op : this.operatorList) {
            preprocessedInput = op.process(input, op.getProcessingParameters(parameters));
        }
        this.writer.write(preprocessedInput, this.writer.getTargetDirecetory(parameters),
                this.writer.getAdditionalWritingParameters(parameters));
    }

}
