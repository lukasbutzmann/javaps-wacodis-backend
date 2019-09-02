/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.IOException;
import java.util.Map;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.algorithms.execution.EoToolExecutor;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(LandCoverClassificationAlgorithm.class);

    @Autowired
    private WacodisBackendConfig config;

    @Autowired
    private ToolConfigParser toolConfigParser;
    
    @Autowired
    private EoToolExecutor eoToolExecutor;

    private String namingSuffix;


    public void executeProcess() throws WacodisProcessingException {

        this.namingSuffix = "_" + System.currentTimeMillis();
        Map<String, AbstractCommandValue> inputArgumentValues = this.createInputArgumentValues();

        ToolConfig toolConfig;
        try {
            toolConfig = toolConfigParser.parse(this.getToolConfigPath());
        } catch (IOException ex) {
            String message = "Error while reading tool configuration";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }

        String containerName = toolConfig.getDocker().getContainer() + this.getNamingSuffix();

        ProcessResult result;
        try {
            result = eoToolExecutor.executeTool(inputArgumentValues, toolConfig);
        } catch (Exception ex) {
            String message = "Error while executing docker process";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        if (result.getResultCode() != 0) { //tool returns Result Code 0 if finished successfully
            throw new WacodisProcessingException("EO tool (container: "
                    + containerName
                    + " )exited with a non-zero result code, result code was "
                    + result.getResultCode()
                    + ", consult tool specific documentation for details");
        }
        LOGGER.info("landcover classification docker process finished "
                + "executing with result code: {}", result.getResultCode());
        LOGGER.debug(result.getOutputMessage());
    }

    public String getNamingSuffix() {
        return namingSuffix;
    }

    private String getToolConfigPath() {
        return this.config.getToolConfigDirectory() + "/" + this.getToolConfigName();
    }

    public abstract String getToolConfigName();

    public abstract String getResultNamePrefix();

    public abstract Map<String, AbstractCommandValue> createInputArgumentValues() throws WacodisProcessingException;
}