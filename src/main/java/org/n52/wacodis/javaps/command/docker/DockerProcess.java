/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command.docker;

import com.github.dockerjava.api.command.CreateContainerResponse;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.ToolExecutionProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * runs docker container
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class DockerProcess implements ToolExecutionProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerProcess.class);

    private DockerController dockerController;
    private DockerContainer container;
    private DockerRunCommandConfiguration containerRunConfig;

    public DockerProcess(DockerController dockerController, DockerContainer container, DockerRunCommandConfiguration containerRunConfig) {
        this.dockerController = dockerController;
        this.container = container;
        this.containerRunConfig = containerRunConfig;
    }

    public DockerContainer getContainer() {
        return container;
    }

    public void setContainer(DockerContainer container) {
        this.container = container;
    }

    public DockerRunCommandConfiguration getContainerRunConfig() {
        return containerRunConfig;
    }

    public void setContainerRunConfig(DockerRunCommandConfiguration containerRunConfig) {
        this.containerRunConfig = containerRunConfig;
    }

    public DockerController getDockerController() {
        return dockerController;
    }

    public void setDockerController(DockerController dockerController) {
        this.dockerController = dockerController;
    }

    /**
     * execute docker run command synchronously,
     * thread blocks until executed container dies (stopped, finished),
     * container is removed after execution
     * @return ProcessResult containing container exit code and container log
     * @throws InterruptedException 
     */
    @Override
    public ProcessResult execute() throws InterruptedException {
        //create container
        CreateContainerResponse createdContainer = this.dockerController.createDockerContainer(this.container, this.containerRunConfig);
        String containerID = createdContainer.getId();
        
        //run container synchronously, remove container after execution
        ProcessResult results = this.dockerController.runDockerContainer_Sync(containerID);
        this.dockerController.removeDockerContainer(containerID);
        
        return results;
    }
}
