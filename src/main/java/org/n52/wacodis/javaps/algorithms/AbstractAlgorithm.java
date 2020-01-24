/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.algorithms.execution.EoToolExecutor;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.command.MultipleCommandValue;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.SingleCommandValue;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractAlgorithm {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String TIFF_EXTENSION = ".tif";
    private static final String GDAL_CONFIG = "gdal-warp.yml";
    private static final String GDAL_RESULT_POSTFIX = "_warped";

    @Autowired
    private WacodisBackendConfig config;

    @Autowired
    private ToolConfigParser toolConfigParser;

    @Autowired
    private EoToolExecutor eoToolExecutor;

    private String namingSuffix;

    private String productName;

    public void executeProcess() throws WacodisProcessingException {
        this.namingSuffix = "_" + System.currentTimeMillis();

        ToolConfig toolConfig = this.getToolConfig(this.getToolConfigPath(this.getToolConfigName()));
        Map<String, AbstractCommandValue> inputArgumentValues = this.createInputArgumentValues(toolConfig.getDocker().getWorkDir());

        this.executeDockerTool(inputArgumentValues, toolConfig);
    }

    protected void executeDockerTool(Map<String, AbstractCommandValue> inputArgumentValues, ToolConfig toolConfig) throws WacodisProcessingException {
        ProcessResult result;
        try {
            result = eoToolExecutor.executeTool(inputArgumentValues, toolConfig);
        } catch (Exception ex) {
            String message = "Error while executing docker process";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        if (result.getResultCode() != 0) { //tool returns Result Code 0 if finished successfully
            throw new WacodisProcessingException(String.format("EO tool (container: %s) exited with non-zero result code (%s)." +
                            " Cause: %s. Consult tool specific documentation for details",
                    toolConfig.getDocker().getContainer(),
                    result.getResultCode(),
                    result.getOutputMessage()));
        }
        LOGGER.info("EO tool docker process finished "
                + "executing with result code: {}", result.getResultCode());
        LOGGER.debug(result.getOutputMessage());

    }

    protected ToolConfig getToolConfig(String toolConfigPath) throws WacodisProcessingException {
        ToolConfig toolConfig = null;
        try {
            toolConfig = toolConfigParser.parse(toolConfigPath);
            toolConfig.getDocker().setContainer(toolConfig.getDocker().getContainer().trim() + this.namingSuffix); //add unique suffix to container name to prevent naming conflicts
            return toolConfig;
        } catch (IOException ex) {
            String message = "Error while reading tool configuration";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
    }

    protected File executeGdalWarp(File file) throws WacodisProcessingException {
        ToolConfig toolConfig = this.getToolConfig(this.getToolConfigPath(GDAL_CONFIG));
        String resultName = FilenameUtils.concat(this.getBackendConfig().getWorkingDirectory(),
                FilenameUtils.getBaseName(file.getName())
                        + GDAL_RESULT_POSTFIX
                        + "."
                        + FilenameUtils.getExtension(file.getName()));
        File outFile = new File(resultName);
        Map<String, AbstractCommandValue> inputArgumentValues = this.createGdalInputArgumentValues(file, toolConfig.getDocker().getWorkDir(), outFile);

        this.executeDockerTool(inputArgumentValues, toolConfig);

        return outFile;
    }

    protected Map<String, AbstractCommandValue> createGdalInputArgumentValues(File inFile, String basePath, File outFile) {
        Map<String, AbstractCommandValue> inputArgumentValues = new HashMap();

        inputArgumentValues.put("INPUT", this.createInputValue(basePath, inFile, true));
        inputArgumentValues.put("TARGET_EPSG", new SingleCommandValue(this.getBackendConfig().getEpsg()));
        inputArgumentValues.put("OUTPUT", this.createInputValue(basePath, outFile, true));

        return inputArgumentValues;
    }

    public String getNamingSuffix() {
        return namingSuffix;
    }

    public WacodisBackendConfig getBackendConfig() {
        return this.config;
    }

    public GenericFileData createProductOutput(String fileName) throws WacodisProcessingException {
        try {
            return new GenericFileData(new File(this.config.getWorkingDirectory(), fileName), "image/geotiff");
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating generic file data.", ex);
        }
    }

    /**
     * Creates an input argument value for the EO process result path
     *
     * @param basePath base path where to store the process result file
     * @return {@link AbstractCommandValue} that encapsulates the EO process
     * result path
     */
    AbstractCommandValue getResultPath(String basePath) {
        this.productName = this.getResultNamePrefix() + "_" + UUID.randomUUID().toString() + this.getNamingSuffix() + TIFF_EXTENSION;

        SingleCommandValue value = new SingleCommandValue(FilenameUtils.concat(basePath, productName));
        return value;
    }

    /**
     * Creates input argument values from a {@link List} of input data
     * {@link File} objects
     *
     * @param basePath  base path where to read the input data from
     * @param inputData input data as {@link List} of {@link File} objects
     * @return {@link AbstractCommandValue} that encapsulates a {@link List} of
     * input data file paths
     * @throws WacodisProcessingException
     */
    AbstractCommandValue createInputValue(String basePath, List<File> inputData, boolean forUnix) {
        MultipleCommandValue value = new MultipleCommandValue(
                inputData.stream()
                        .map(sF -> {
                            String path = FilenameUtils.concat(basePath, sF.getName());
                            return forUnix ? FilenameUtils.separatorsToUnix(path) : path;
                        })
                        .collect(Collectors.toList()));
        return value;
    }

    /**
     * Creates input argument values from an input data {@link File}
     *
     * @param basePath  base path where to read the input data from
     * @param inputData input data as {@link File}
     * @return {@link AbstractCommandValue} that encapsulates an input data file
     * path
     * @throws WacodisProcessingException
     */
    AbstractCommandValue createInputValue(String basePath, File inputData, boolean forUnix) {
        String path = FilenameUtils.concat(basePath, inputData.getName());
        return forUnix ? new SingleCommandValue(FilenameUtils.separatorsToUnix(path)) : new SingleCommandValue(path);
    }

    public String getProductName() {
        return productName;
    }

    protected String getToolConfigPath(String toolConfigName) {
        return this.config.getToolConfigDirectory() + "/" + toolConfigName;
    }

    public abstract String getToolConfigName();

    public abstract String getGpfConfigName();

    public abstract String getResultNamePrefix();

    public abstract Map<String, AbstractCommandValue> createInputArgumentValues(String basePath) throws WacodisProcessingException;

}
