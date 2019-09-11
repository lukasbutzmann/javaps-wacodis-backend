/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.gpt;

import java.util.Map;
import org.esa.snap.core.gpf.main.GPT;
import org.springframework.stereotype.Component;

/**
 * Executor for GPF graphs
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class GptExecutor {

    /**
     * Executes a GPF graph via GPT
     *
     * @param arguments {@link GptArguments} for executing the GPF graph via GPT
     * @throws Exception
     */
    public void executeGraph(GptArguments arguments) throws Exception {
        GPT.run(this.prepareArguments(arguments));
    }

    /**
     * Prepares the GPT arguments by creating an array of String arguments
     *
     * @param arguments {@link GptArguments} to prepare
     * @return Array of String arguments
     */
    public String[] prepareArguments(GptArguments arguments) {
        String[] args = new String[arguments.getParameters().size() + 1];
        int i = 0;
        args[i] = arguments.getGraphFile();

        for (Map.Entry<String, String> entry : arguments.getParameters().entrySet()) {
            args[++i] = "-P" + entry.getKey() + "=" + entry.getValue();
        }
        return args;
    }
}
