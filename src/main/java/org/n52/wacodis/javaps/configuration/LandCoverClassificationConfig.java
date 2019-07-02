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
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
@Configuration
@PropertySource("classpath:landcoverclassification.properties")
public class LandCoverClassificationConfig {
    
    @Value("${docker.image}")
    private String dockerImage;
    
    @Value("${docker.container.name}")
    private String dockerContainerName;
   
    @Value("${docker.host}")
    private String dockerHost;

    public String getDockerImage() {
        return dockerImage;
    }

    public String getDockerContainerName() {
        return dockerContainerName;
    }

    public String getDockerHost() {
        return dockerHost;
    }   

}
