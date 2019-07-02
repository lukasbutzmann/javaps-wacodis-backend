/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.execution;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.n52.wacodis.javaps.command.CommandParameter;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.docker.DockerContainer;
import org.n52.wacodis.javaps.command.docker.DockerController;
import org.n52.wacodis.javaps.command.docker.DockerProcess;
import org.n52.wacodis.javaps.command.docker.DockerRunCommandConfiguration;
import org.n52.wacodis.javaps.configuration.LandCoverClassificationConfig;
import org.slf4j.LoggerFactory;

/**
 * executes docker container running landcover classification algorithm
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class LandCoverClassificationExecutor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LandCoverClassificationExecutor.class);

    private final String hostDataFolder;
    private final String input;
    private final String training;
    private final String result;
    private final String containerName;
    private final LandCoverClassificationConfig toolConfig;  

    /**
     * @param hostDataFolder specifies working directory
     * @param input relative path from hostDataFolder to input file (imagery), corresponds with -input parameter 
     * @param training relative path from hostDataFolder to trainig data, corresponds with -training parameter
     * @param result relative path from hostDataFolder to location where output data should be stored, corresponds with -result parameter
     * @param toolConfig configure container image and docker host (container name is overridden by containerName) 
     * @param containerName overrides toolConfig.dockerContainerName
     */
    public LandCoverClassificationExecutor(String hostDataFolder, String input, String training, String result,  LandCoverClassificationConfig toolConfig, String containerName) {
        this.input = input;
        this.training = training;
        this.result = result;
        this.toolConfig = toolConfig;
        this.hostDataFolder = hostDataFolder;
        this.containerName = containerName;
    }

    /**
     * construct instance with container name specified in toolConfig
     * @param hostDataFolder specifies working directory
     * @param input relative path from hostDataFolder to input file (imagery), corresponds with -input parameter
     * @param training relative path from hostDataFolder to trainig data, corresponds with -training parameter
     * @param result relative path from hostDataFolder to location where output data should be stored, corresponds with -result parameter
     * @param toolConfig configure container image container name and docker host
     */
    public LandCoverClassificationExecutor(String hostDataFolder, String input, String training, String result, LandCoverClassificationConfig toolConfig) {
        this(hostDataFolder, input, training, result, toolConfig, toolConfig.getDockerContainerName());
    }

    /**
     * executes Landcover Classification Tool synchronously
     * @return container output (status code, container log) 
     * (output file is specified by result parameter of the constructor)
     * @throws InterruptedException 
     */
    public ProcessResult executeTool() throws InterruptedException {
        DockerController controller = initDockerController();
        DockerRunCommandConfiguration runConfig = initRunConfiguration();
        DockerContainer container = new DockerContainer(this.containerName, this.toolConfig.getDockerImage());

        DockerProcess toolProcess = new DockerProcess(controller, container, runConfig);

        ProcessResult pr = toolProcess.execute();
        return pr;
    }

    private DockerRunCommandConfiguration initRunConfiguration() {
        DockerRunCommandConfiguration runConfig = new DockerRunCommandConfiguration();

        //set cmd parameters
        runConfig.addCommandParameter(new CommandParameter("", "/bin/ash"));
        runConfig.addCommandParameter(new CommandParameter("", "/eo.sh"));
        runConfig.addCommandParameter(new CommandParameter("-input", this.input));
        runConfig.addCommandParameter(new CommandParameter("-result", this.result));
        runConfig.addCommandParameter(new CommandParameter("-training", this.training));

        //set volumes bindings
        runConfig.addVolumeBinding(concatVolumeBinding(this.hostDataFolder, "/public"));

        return runConfig;
    }

    private DockerController initDockerController() {
        DefaultDockerClientConfig hostConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(this.toolConfig.getDockerHost()).build();

        return new DockerController(hostConfig);
    }

    private String concatVolumeBinding(String hostFolder, String containerFolder) {
        return hostFolder + ":" + containerFolder;
    }

}
