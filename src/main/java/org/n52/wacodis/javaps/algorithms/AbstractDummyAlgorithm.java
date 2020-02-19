/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import org.apache.commons.io.FileUtils;
import org.n52.javaps.io.GenericFileData;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Abstract dummy algorithm that can be extended for testing purposes.
 * <p>
 * The abstract dummy algorithm just overwrites some methods in order to provide a
 * resulting product without the necessity of executing a docker container.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractDummyAlgorithm extends AbstractAlgorithm {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    protected void executeDockerTool(Map<String, AbstractCommandValue> inputArgumentValues, ToolConfig toolConfig) throws WacodisProcessingException {
        LOGGER.info("Execute dummy docker process...");
    }

    @Override
    public GenericFileData createProductOutput(File file) throws WacodisProcessingException {
        try {
            File copiedFile = new File(this.getBackendConfig().getWorkingDirectory(), file.getName());
            FileUtils.copyFile(file, copiedFile);
            return new GenericFileData(copiedFile, "application/zip");
        } catch (IOException ex) {
            throw new WacodisProcessingException("Error while creating generic file data.", ex);
        }
    }

    @Override
    public File getResultFile() {
        return new File(this.getBackendConfig().getSentinelTestFile());
    }

}
