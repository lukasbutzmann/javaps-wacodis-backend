/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.algorithms.execution.EoToolExecutor;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfigParser;
import org.n52.wacodis.javaps.preprocessing.InputDataPreprocessorExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(LandCoverClassificationAlgorithm.class);

    private static final String TIFF_EXTENSION = ".tif";

    @Autowired
    private WacodisBackendConfig config;

    private String namingSuffix;
    private String productName;

    public void executeProcess() throws WacodisProcessingException {

        this.namingSuffix = "_" + System.currentTimeMillis();

        Map<String, String> rawInputMap = new HashMap();

        List<InputDataPreprocessorExecutor> preprocessorExecutionList = this.defineInputdataPreprocessing();
        preprocessorExecutionList.forEach(p -> {
            try {
                List<File> preprocessedInputData = p.execute();
                if (preprocessedInputData.isEmpty()) {
                    throw new WacodisProcessingException("No input data available for processing");
                }
                rawInputMap.put(p.getProcessInputId(), preprocessedInputData.get(0).getPath());
            } catch (WacodisProcessingException ex) {
                String message = "Error while preprocessing input data";
                LOGGER.debug(message, ex);
            }
        });

        String resultFileName = this.getResultNamePrefix() + UUID.randomUUID().toString() + this.namingSuffix + TIFF_EXTENSION;
        ToolConfig toolConfig;
        try {
            toolConfig = this.parseToolConfig();
        } catch (IOException ex) {
            String message = "Error while reading tool configuration";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }

        String containerName = toolConfig.getDocker().getContainer() + this.namingSuffix;

        ProcessResult result;
        try {
            result = new EoToolExecutor().executeTool(rawInputMap, toolConfig);
        } catch (Exception ex) {
            String message = "Error while executing docker process";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        if (result.getResultCode() == 0) { //tool returns Result Code 0 if finished successfully
            this.productName = resultFileName;
        } else { //non-zero Result Code, error occured during tool execution
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

    public ToolConfig parseToolConfig() throws IOException {
        ToolConfigParser parser = new ToolConfigParser();
        ToolConfig toolConfig = parser.parse(this.getToolConfigPath());

        return toolConfig;
    }

    public String getNamingSuffix() {
        return namingSuffix;
    }

    public String getProductName() {
        return productName;
    }

    public abstract String getToolConfigPath();

    public abstract String getResultNamePrefix();

    public abstract List<InputDataPreprocessorExecutor> defineInputdataPreprocessing() throws WacodisProcessingException;
}
