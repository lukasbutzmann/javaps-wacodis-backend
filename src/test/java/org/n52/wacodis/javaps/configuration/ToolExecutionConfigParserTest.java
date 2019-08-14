/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfigParser;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ToolExecutionConfigParserTest {

    @Test
    public void testConfigParsing() throws IOException {
        ToolConfigParser parser = new ToolConfigParser();
        ToolConfig config = parser.parse(ClassLoader.getSystemResourceAsStream("land-cover-classification.yml"));

        Assert.assertEquals("land-cover-classification", config.getId());
        Assert.assertFalse(config.getCommand().getArguments().isEmpty());
    }

}
