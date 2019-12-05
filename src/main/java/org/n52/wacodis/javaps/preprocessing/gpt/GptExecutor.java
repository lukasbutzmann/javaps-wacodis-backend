/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.gpt;

import java.util.Map;
import org.esa.snap.core.gpf.main.GPT;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Executor for GPF graphs
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class GptExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GptExecutor.class);

    /**
     * Executes a GPF graph via GPT
     *
     * @param arguments {@link GptArguments} for executing the GPF graph via GPT
     * @throws WacodisProcessingException
     */
    public void executeGraph(GptArguments arguments) throws WacodisProcessingException {
        LOGGER.info("Execute processing graph: {}", arguments.getGraphFile());
        try {
            GPT.run(this.prepareArguments(arguments));
        } catch (Exception ex) {
            LOGGER.error("Processing graph execution unexpectedly terminated.", ex);
            throw new WacodisProcessingException("Error while executing processing graph.", ex);
        }
    }

    /**
     * Prepares the GPT arguments by creating an array of String arguments
     *
     * @param arguments {@link GptArguments} to prepare
     * @return Array of String arguments
     */
    public String[] prepareArguments(GptArguments arguments) {
        String[] args = new String[arguments.getParameters().size() + 2];
        int i = 0;
        args[i] = arguments.getGraphFile();
        args[++i] = "-e";

        for (Map.Entry<String, String> entry : arguments.getParameters().entrySet()) {
            args[++i] = "-P" + entry.getKey() + "=" + entry.getValue();
        }
        return args;
    }
}
