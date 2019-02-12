/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Configuration
@PropertySource("classpath:wacodis-javaps.properties")
public class WacodisBackendConfig {

    @Value("${wacodis.javaps.workdir}")
    private String workingDirectory;

    public String getWorkingDirectory() {
        return workingDirectory;
    }

}
