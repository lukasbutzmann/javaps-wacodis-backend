/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wacodis.javaps.preprocessing.gpt.GptArguments;
import org.n52.wacodis.javaps.preprocessing.gpt.GptExecutor;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GptExecutorTest {

    private static final String INPUT_PARAM_KEY = "input";
    private static final String INPUT_PARAM_VALUE = ".path/to/input.zip";
    private static final String OUTPUT_PARAM_KEY = "output";
    private static final String OUTPUT_PARAM_VALUE = ".path/to/output.zip";
    private static final String GRAPH_FILE = "./path/to/graphFile.xml";

    private GptArguments arguments;

    @Before
    public void setup() {
        Map<String, String> parameters = new HashMap();
        parameters.put(INPUT_PARAM_KEY, INPUT_PARAM_VALUE);
        parameters.put(OUTPUT_PARAM_KEY, OUTPUT_PARAM_VALUE);
        this.arguments = new GptArguments(GRAPH_FILE, parameters);
    }

    @Test
    public void testPrepareArguments() {

        String[] resultArgs = new String[3];
        resultArgs[0] = GRAPH_FILE;
        resultArgs[1] = "-P" + INPUT_PARAM_KEY + "=" + INPUT_PARAM_VALUE;
        resultArgs[2] = "-P" + OUTPUT_PARAM_KEY + "=" + OUTPUT_PARAM_VALUE;
        Arrays.sort(resultArgs);

        GptExecutor executor = new GptExecutor();
        String[] preparedArguments = executor.prepareArguments(arguments);
        Arrays.sort(preparedArguments);

        Assert.assertArrayEquals(resultArgs, preparedArguments);
    }

}
