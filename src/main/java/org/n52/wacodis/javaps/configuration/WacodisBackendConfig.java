/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration;

import javax.imageio.ImageIO;
import org.esa.snap.runtime.Engine;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Configuration
@PropertySource("classpath:wacodis-javaps.properties")
public class WacodisBackendConfig implements InitializingBean, DisposableBean {

    private Engine engine;

    @Value("${wacodis.javaps.workdir}")
    private String workingDirectory;

    @Value("${wacodis.javaps.toolConfigDir}")
    private String toolConfigDirectory;

    @Value("${wacodis.javaps.gpfDir}")
    private String gpfDir;
    
    @Value("${wacodis.javaps.epsg}")
    private String epsg;

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public String getToolConfigDirectory() {
        return toolConfigDirectory;
    }

    public String getGpfDir() {
        return gpfDir;
    }
    
    public String getEpsg() {
        return epsg;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Starts the runtime engine and installs third-party libraries and driver  
        this.engine = Engine.start();
        // Scans for plugins that will be registered with the IIORegistry
        ImageIO.scanForPlugins();
    }

    @Override
    public void destroy() throws Exception {
        engine.stop();
    }

}
