/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.execution;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.command.CommandParameter;
import org.n52.wacodis.javaps.command.MultipleCommandValue;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.SingleCommandValue;
import org.n52.wacodis.javaps.command.docker.DockerContainer;
import org.n52.wacodis.javaps.command.docker.DockerController;
import org.n52.wacodis.javaps.command.docker.DockerProcess;
import org.n52.wacodis.javaps.command.docker.DockerRunCommandConfiguration;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.tools.ArgumentConfig;
import org.n52.wacodis.javaps.configuration.tools.CommandConfig;
import org.n52.wacodis.javaps.configuration.tools.DockerConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * execute any processing tool inside a docker container
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EoToolExecutor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EoToolExecutor.class);

    @Autowired
    private WacodisBackendConfig config;

    /**
     * excecute tool as docker container synchronously
     *
     * @param input
     * @param config defines docker image and run command parameters
     * @return
     * @throws InterruptedException
     */
    public ProcessResult executeTool(Map<String, AbstractCommandValue> input, ToolConfig config) throws InterruptedException {
        DockerConfig dockerConfig = config.getDocker();
        CommandConfig cmdConfig = config.getCommand();

        DockerController dockerController = initDockerController(dockerConfig);
        DockerRunCommandConfiguration dockerRunConfig = initRunConfiguration(cmdConfig, input);
        dockerRunConfig.addVolumeBinding(concatVolumeBinding(this.config.getWorkingDirectory(), dockerConfig.getWorkDir()));

        DockerContainer dockerContainer = new DockerContainer(dockerConfig.getContainer(), dockerConfig.getImage());  //TODO use prefix in container name

        LOGGER.info("executing tool inside docker container " + dockerContainer.getContainerName() + ", image: " + dockerContainer.getImageName() + ", run cmd: " + runCmdAsString(dockerRunConfig));

        DockerProcess toolProcess = new DockerProcess(dockerController, dockerContainer, dockerRunConfig);
        ProcessResult processResult = toolProcess.execute();

        return processResult;
    }

    private DockerController initDockerController(DockerConfig dockerConfig) {
        DefaultDockerClientConfig hostConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerConfig.getHost()).build();

        return new DockerController(hostConfig);
    }

    private DockerRunCommandConfiguration initRunConfiguration(CommandConfig cmdConfig, Map<String, AbstractCommandValue> input) {
        DockerRunCommandConfiguration runConfig = new DockerRunCommandConfiguration();

        runConfig.addCommandParameter(new CommandParameter("", cmdConfig.getFolder()));
        runConfig.addCommandParameter(new CommandParameter("", cmdConfig.getName()));

        //TODO different handling of different command parameter types
        //TODO strategy for output naming
        //set cmd parameters
        cmdConfig.getArguments().forEach(cmdArgument -> {
            if (cmdArgument.getType().equals(ArgumentConfig.TypeValues.WPS_PROCESS_REFERENCE.getName())) {
                if (cmdArgument.getQuantity().equals(ArgumentConfig.QuantityValues.MULTIPLE.getName())) {
                    MultipleCommandValue value = (MultipleCommandValue) input.get(cmdArgument.getValue());
                    String valueString = StringUtils.join(value.getCommandValue(), ",");

                    runConfig.addCommandParameter(new CommandParameter(cmdArgument.getName(), valueString));
                } else {
                    SingleCommandValue value = (SingleCommandValue) input.get(cmdArgument.getValue());

                    runConfig.addCommandParameter(new CommandParameter(cmdArgument.getName(), value.getCommandValue()));
                }
            } else {
                runConfig.addCommandParameter(new CommandParameter(cmdArgument.getName(), cmdArgument.getValue()));
            }
        });

        return runConfig;
    }

    private String concatVolumeBinding(String hostFolder, String containerFolder) {
        return hostFolder + ":" + containerFolder;
    }

    private String runCmdAsString(DockerRunCommandConfiguration runConfig) {
        StringBuilder runCMD = new StringBuilder();

        for (CommandParameter param : runConfig.getCommandParameters()) {
            runCMD.append(param.toString());
            runCMD.append(" ");
        }

        return runCMD.toString();
    }

}
