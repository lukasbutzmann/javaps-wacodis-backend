/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration;

import javax.imageio.ImageIO;

import org.esa.snap.runtime.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Configuration
@PropertySource("classpath:wacodis-javaps.properties")
public class WacodisBackendConfig implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(WacodisBackendConfig.class);

    private Engine engine;

    @Value("${wacodis.javaps.workdir}")
    private String workingDirectory;

    @Value("${wacodis.javaps.toolconfigdir}")
    private String toolConfigDirectory;

    @Value("${wacodis.javaps.gpfdir}")
    private String gpfDir;

    @Value("${wacodis.javaps.epsg}")
    private String epsg;

    @Value("${wacodis.javaps.sentineltestfile:}")
    private String sentinelTestFile;

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

    public String getSentinelTestFile() {
        return sentinelTestFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info("Working directory: {}", workingDirectory);
        LOG.info("Tool config directory: {}", toolConfigDirectory);
        LOG.info("GPF directory: {}", gpfDir);
        LOG.info("Reference CRS: {}", epsg);

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
