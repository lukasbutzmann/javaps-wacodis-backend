/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.execution;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.n52.wacodis.javaps.WacodisConfigurationException;
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
     * Excecute tool as docker container synchronously
     *
     * @param input {@link Map<K,V>] that holds input tool argument values as {@link AbstractCommandValue}
     * @param config defines docker image and run command parameters
     * @return
     * @throws InterruptedException
     * @throws org.n52.wacodis.javaps.WacodisConfigurationException
     */
    public ProcessResult executeTool(Map<String, AbstractCommandValue> input, ToolConfig config) throws InterruptedException, WacodisConfigurationException {
        LOGGER.debug("Configure execution of EO tool: " + config.getId()
                + "\nDocker config: " + config.getDocker()
                + "\nCommand config: " + config.getCommand()
                + "\nInput parameter values: " + input
        );
        DockerConfig dockerConfig = config.getDocker();
        CommandConfig cmdConfig = config.getCommand();

        DockerController dockerController = initDockerController(dockerConfig);
        DockerRunCommandConfiguration dockerRunConfig = this.initRunConfiguration(cmdConfig, input);
        dockerRunConfig.addVolumeBinding(concatVolumeBinding(this.config.getWorkingDirectory(), dockerConfig.getWorkDir()));

        DockerContainer dockerContainer = new DockerContainer(dockerConfig.getContainer(), dockerConfig.getImage());

        LOGGER.info("executing tool inside docker container " 
                + dockerContainer.getContainerName() 
                + ", image: " + dockerContainer.getImageName()
                + ", volume bindings: " + dockerRunConfig.getVolumeBindings()
                + ", run cmd: " + runCmdAsString(dockerRunConfig));

        DockerProcess toolProcess = new DockerProcess(dockerController, dockerContainer, dockerRunConfig);
        ProcessResult processResult = toolProcess.execute();

        return processResult;
    }

    /**
     * Initializes a {@link DockerRunCommandConfiguration} from a
     * {@link CommandConfig} and a corresponding {@link Map<K,V>} with
     * {@link AbstractCommandValue} values.
     *
     * @param cmdConfig {@link CommandConfig} that holds tool command arguments.
     * @param input {@link Map<K,V>} that holds appropriate tool command
     * argument {@link AbstractCommandValue} values
     * @return {@link DockerRunCommandConfiguration}
     * @throws WacodisConfigurationException either if an argument type is not
     * valid or an input value is not available.
     */
    public DockerRunCommandConfiguration initRunConfiguration(CommandConfig cmdConfig, Map<String, AbstractCommandValue> input) throws WacodisConfigurationException {
        DockerRunCommandConfiguration runConfig = new DockerRunCommandConfiguration();

        runConfig.addCommandParameter(new CommandParameter("", cmdConfig.getFolder()));
        runConfig.addCommandParameter(new CommandParameter("", cmdConfig.getName()));

        //TODO different handling of different command parameter types
        //TODO strategy for output naming
        //set cmd parameters
        for (ArgumentConfig cmdArgument : cmdConfig.getArguments()) {
            if (cmdArgument.getType().equals(ArgumentConfig.TypeValues.WPS_PROCESS_REFERENCE.getName())) {
                if (!input.containsKey(cmdArgument.getValue())) {
                    throw new WacodisConfigurationException("No input value is available for argument: " + cmdArgument.getValue());
                }
                if (cmdArgument.getQuantity().equals(ArgumentConfig.QuantityValues.MULTIPLE.getName())) {
                    MultipleCommandValue value = (MultipleCommandValue) input.get(cmdArgument.getValue());
                    String valueString = "";
                    if (cmdArgument.getSeparator() == null) {
                        valueString = StringUtils.join(value.getCommandValue(), ",");
                    } else {
                        valueString = StringUtils.join(value.getCommandValue(), cmdArgument.getSeparator());
                    }

                    runConfig.addCommandParameter(new CommandParameter(cmdArgument.getName(), valueString));
                } else {
                    SingleCommandValue value = (SingleCommandValue) input.get(cmdArgument.getValue());

                    runConfig.addCommandParameter(new CommandParameter(cmdArgument.getName(), value.getCommandValue()));
                }
            } else if (cmdArgument.getType().equals(ArgumentConfig.TypeValues.STATIC_OPTION.getName())) {
                runConfig.addCommandParameter(new CommandParameter(cmdArgument.getName(), cmdArgument.getValue()));
            } else {
                throw new WacodisConfigurationException("No valid EO tool argument type: + cmdArgument.getType()");
            }
        }
        return runConfig;
    }

    private DockerController initDockerController(DockerConfig dockerConfig) {
        DefaultDockerClientConfig hostConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerConfig.getHost()).build();

        return new DockerController(hostConfig);
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