/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.n52.wacodis.javaps.command.CommandParameter;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.docker.DockerContainer;
import org.n52.wacodis.javaps.command.docker.DockerController;
import org.n52.wacodis.javaps.command.docker.DockerProcess;
import org.n52.wacodis.javaps.command.docker.DockerRunCommandConfiguration;
import org.n52.wacodis.javaps.configuration.WacodisPrototypeToolConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class WacodisPrototypeToolAlgorithm {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WacodisPrototypeToolAlgorithm.class);

    private String input;
    private String training;
    private String result;

    @Autowired
    private WacodisPrototypeToolConfig toolConfig;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getTraining() {
        return training;
    }

    public void setTraining(String training) {
        this.training = training;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
        runConfig.addVolumeBinding(concatVolumeBinding(this.toolConfig.getHostDataFolder(), "/public"));

        return runConfig;
    }

    private DockerController initDockerController() {
        DefaultDockerClientConfig hostConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost(this.toolConfig.getDockerHost()).build();

        return new DockerController(hostConfig);
    }
    
    private String concatVolumeBinding(String hostFolder, String containerFolder){
        return hostFolder + ":" + containerFolder;
    }
    
}
