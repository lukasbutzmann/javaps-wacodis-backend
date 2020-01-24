/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Parser for config files that define the execution of EO command line tools
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ToolConfigParser {

    /**
     * Parses the given YAML config file
     *
     * @param path path to the YAML config file
     * @return {@link ToolConfig}
     * @throws IOException
     */
    public ToolConfig parse(String path) throws IOException {
        return this.parse(new FileInputStream(path));
    }

    /**
     * Parses the given YAML config file
     *
     * @param input {@link InputStream} for the YAML config file
     * @return {@link ToolConfig}
     * @throws IOException
     */
    public ToolConfig parse(InputStream input) throws IOException {
        Yaml yaml = new Yaml(new Constructor(ToolConfig.class));

        ToolConfig config = yaml.load(input);
        return config;
    }
}
