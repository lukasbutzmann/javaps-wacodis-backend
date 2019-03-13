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
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class LandCoverClassificationExecutor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LandCoverClassificationExecutor.class);

    private String hostDataHolder;
    private String input;
    private String training;
    private String result;

    private LandCoverClassificationConfig toolConfig;

    public LandCoverClassificationExecutor(String hostDataHolder, String input, String training, String result,  LandCoverClassificationConfig toolConfig) {
        this.input = input;
        this.training = training;
        this.result = result;
        this.toolConfig = toolConfig;
        this.hostDataHolder = hostDataHolder;
    }

    public ProcessResult executeTool() throws InterruptedException {
        DockerController controller = initDockerController();
        DockerRunCommandConfiguration runConfig = initRunConfiguration();
        DockerContainer container = new DockerContainer(this.toolConfig.getDockerContainerName(), this.toolConfig.getDockerImage());

        DockerProcess toolProcess = new DockerProcess(controller, container, runConfig);

        ProcessResult pr = toolProcess.execute();
        return pr;
    }

    //@Execute
    //public void execute(){}
    private DockerRunCommandConfiguration initRunConfiguration() {
        DockerRunCommandConfiguration runConfig = new DockerRunCommandConfiguration();

        //cmd 
        runConfig.addCommandParameter(new CommandParameter("", "/bin/ash"));
        runConfig.addCommandParameter(new CommandParameter("", "/eo.sh"));
        runConfig.addCommandParameter(new CommandParameter("-input", this.input));
        runConfig.addCommandParameter(new CommandParameter("-result", this.result));
        runConfig.addCommandParameter(new CommandParameter("-training", this.training));

        //volumes
        runConfig.addVolumeBinding(concatVolumeBinding(this.hostDataHolder, "/public"));

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
