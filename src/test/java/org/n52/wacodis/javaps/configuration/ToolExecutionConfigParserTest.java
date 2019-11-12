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
        ToolConfig config = parser.parse(ClassLoader.getSystemResourceAsStream("land-cover-classification-test.yml"));

        Assert.assertEquals("land-cover-classification", config.getId());
        
        Assert.assertEquals("unix:///var/run/docker.sock", config.getDocker().getHost());
        Assert.assertEquals("dlm_docker:wacodis-eo-hackathon", config.getDocker().getImage());
        Assert.assertEquals("wacodis-eo-dlm", config.getDocker().getContainer());
        Assert.assertEquals("/public", config.getDocker().getWorkDir());
        
        Assert.assertEquals("4326", config.getParameter().getInputEpsg());
        
        Assert.assertEquals("/bin/ash", config.getCommand().getFolder());
        Assert.assertEquals("/eo.sh", config.getCommand().getName());
        Assert.assertFalse(config.getCommand().getArguments().isEmpty());
    }

}
