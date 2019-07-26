/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration.tools;

import java.io.IOException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ToolConfigParser {

    public ToolConfig parse(String source) throws IOException{
        Yaml yaml = new Yaml(new Constructor(ToolConfig.class));
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(source);
        ToolConfig config = yaml.load(inputStream);
        return config;
    }
}
