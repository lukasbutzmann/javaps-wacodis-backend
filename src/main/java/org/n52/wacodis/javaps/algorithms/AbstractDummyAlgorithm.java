/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Abstract dummy algorithm that can be extended for testing purposes
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractDummyAlgorithm {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String TIFF_EXTENSION = ".tif";

    @Autowired
    private WacodisBackendConfig config;

    @Autowired
    private ToolConfigParser toolConfigParser;

    @Autowired
    private EoToolExecutor eoToolExecutor;

    private String namingSuffix;

    private String productName;

    public void executeDummyProcess(String inputKey, String outputKey) throws WacodisProcessingException {
        LOGGER.info("Dummy tool is executed.");
        this.namingSuffix = "_" + System.currentTimeMillis();
        ToolConfig toolConfig;
        try {
            toolConfig = toolConfigParser.parse(this.getToolConfigPath());
        } catch (IOException ex) {
            String message = "Error while reading tool configuration";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        Map<String, AbstractCommandValue> inputArgumentValues = this.createInputArgumentValues(toolConfig.getDocker().getWorkDir());
        String input = ((List<String>) inputArgumentValues.get(inputKey).getCommandValue()).get(0);
        String output = (String) inputArgumentValues.get(outputKey).getCommandValue();
        try {
            FileUtils.copyFile(new File(input), new File(output));
        } catch (IOException ex) {
            LOGGER.error(String.format("Error copying file '%s' to '%s'", input, output), ex);
        }
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
    public AbstractCommandValue getResultPath(String basePath) {
        this.productName = this.getResultNamePrefix() + "_" + UUID.randomUUID().toString() + this.getNamingSuffix() + TIFF_EXTENSION;

        SingleCommandValue value = new SingleCommandValue(FilenameUtils.concat(this.getBackendConfig().getWorkingDirectory(), productName));
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
    public AbstractCommandValue createInputValue(String basePath, List<File> inputData) throws WacodisProcessingException {
        MultipleCommandValue value = new MultipleCommandValue(
                inputData.stream()
                        .map(sF -> sF.getPath())
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
    public AbstractCommandValue createInputValue(String basePath, File inputData) throws WacodisProcessingException {
        SingleCommandValue value = new SingleCommandValue(inputData.getPath());
        return value;
    }

    public String getProductName() {
        return productName;
    }

    private String getToolConfigPath() {
        return this.config.getToolConfigDirectory() + "/" + this.getToolConfigName();
    }

    public abstract String getToolConfigName();

    public abstract String getGpfConfigName();

    public abstract String getResultNamePrefix();

    public abstract Map<String, AbstractCommandValue> createInputArgumentValues(String basePath) throws WacodisProcessingException;
}
