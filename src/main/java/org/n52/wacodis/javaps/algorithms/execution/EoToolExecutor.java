/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.execution;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import java.util.List;
import java.util.Map;
import org.n52.wacodis.javaps.command.CommandParameter;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.docker.DockerContainer;
import org.n52.wacodis.javaps.command.docker.DockerController;
import org.n52.wacodis.javaps.command.docker.DockerProcess;
import org.n52.wacodis.javaps.command.docker.DockerRunCommandConfiguration;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.tools.ArgumentConfig;
import org.n52.wacodis.javaps.configuration.tools.CommandConfig;
import org.n52.wacodis.javaps.configuration.tools.DockerConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * execute any processing tool inside a docker container
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class EoToolExecutor {
    
    private static final String CONTAINERDIRECTORY = "/public";
    
    @Autowired
    private WacodisBackendConfig backendConfig;
    

    /**
     * excecute tool as docker container synchronously
     * @param input
     * @param config defines docker image and run command parameters
     * @return
     * @throws InterruptedException 
     */
    public ProcessResult executeTool(Map<String, String> input, ToolConfig config) throws InterruptedException {
        DockerConfig dockerConfig = config.getDocker();
        CommandConfig cmdConfig = config.getCommand();
        
        DockerController dockerController = initDockerController(dockerConfig);
        DockerRunCommandConfiguration dockerRunConfig = initRunConfiguration(cmdConfig.getArguments());
        DockerContainer dockerContainer = new DockerContainer(dockerConfig.getContainer(), dockerConfig.getImage());  //TODO use prefix in container name
        
        DockerProcess toolProcess = new DockerProcess(dockerController, dockerContainer, dockerRunConfig);
        ProcessResult processResult = toolProcess.execute();
        
        return processResult;
    }

    private DockerController initDockerController(DockerConfig dockerConfig) {
        DefaultDockerClientConfig hostConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerConfig.getHost()).build();

        return new DockerController(hostConfig);
    }

    private DockerRunCommandConfiguration initRunConfiguration(List<ArgumentConfig> cmdArguments) {
        DockerRunCommandConfiguration runConfig = new DockerRunCommandConfiguration();

        //set cmd parameters
        cmdArguments.forEach(cmdArgument -> runConfig.addCommandParameter(new CommandParameter(cmdArgument.getName(), cmdArgument.getValue())));
        
        //set volumes bindings
        runConfig.addVolumeBinding(concatVolumeBinding(this.backendConfig.getWorkingDirectory(),CONTAINERDIRECTORY)); //ToDo parameter for working directory, parameter for container directory

        return runConfig;
    }

    private String concatVolumeBinding(String hostFolder, String containerFolder) {
        return hostFolder + ":" + containerFolder;
    }
}
