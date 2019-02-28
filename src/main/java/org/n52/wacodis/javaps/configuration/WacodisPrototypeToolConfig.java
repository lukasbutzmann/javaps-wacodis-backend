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
@PropertySource("classpath:wacodisprototypetool.properties")
public class WacodisPrototypeToolConfig {
    
    @Value("${docker.image}")
    private String dockerImage;
    
    @Value("${docker.container.name}")
    private String dockerContainerName;
    
    @Value("${docker.data.hostfolder}")
    private String hostDataFolder;
    
    @Value("${docker.host}")
    private String dockerHost;
    
     @Value("${docker.container.volumes}")
    private String dockerVolumes;

    public String getDockerImage() {
        return dockerImage;
    }

    public String getDockerContainerName() {
        return dockerContainerName;
    }

    public String getHostDataFolder() {
        return hostDataFolder;
    }

    public String getDockerHost() {
        return dockerHost;
    }   

    public String[] getDockerVolumes() {
        return dockerVolumes.split(",");
    }

}
