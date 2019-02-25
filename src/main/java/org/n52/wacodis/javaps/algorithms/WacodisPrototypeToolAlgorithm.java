/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.wacodis.javaps.command.AbstractProcessCommand;
import org.n52.wacodis.javaps.command.CommandParameter;
import org.n52.wacodis.javaps.command.ProcessCommand;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.configuration.WacodisTestToolConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class WacodisPrototypeToolAlgorithm {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WacodisPrototypeToolAlgorithm.class);

    private static final String APPLICATION = "docker run";

    private String input;
    private String training;
    private String result;

    @Autowired
    private WacodisTestToolConfig toolConfig;

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
        AbstractProcessCommand cmd = new ProcessCommand(APPLICATION);
        cmd.setParameter(getParameters());

        ProcessResult pr = cmd.execute();
        return pr;
    }
    
    //@Execute
    //public void execute(){}
    
    private List<CommandParameter> getParameters() {
        List<CommandParameter> parameters = new ArrayList<>();

        parameters.add(new CommandParameter("--name", this.toolConfig.getDockerContainerName())); //name
        parameters.add(new CommandParameter("-v", concatVolumeMapping(this.toolConfig.getHostDataFolder(), "/public"))); //volume binding
        parameters.add(new CommandParameter("-i", "")); //interactive (flag)
        if (this.toolConfig.isRemoveDockerContainer()) { //remove (flag)
            parameters.add(new CommandParameter("--rm",""));
        }
        parameters.add(new CommandParameter("", this.toolConfig.getDockerImage())); //image (unnamed parameter)
        parameters.add(new CommandParameter("","bin/ash /eo.sh"));  //command (unamed parameter)
        parameters.add(new CommandParameter("-input", this.input)); //command_input
        parameters.add(new CommandParameter("-result", this.result)); //command_result
        parameters.add(new CommandParameter("-training", this.training)); //command_trainig
        
        LOGGER.debug("created parameter list for WacodisTestTool: " + parameters.toString());

        return parameters;
    }
    
    private String concatVolumeMapping(String hostFolder, String containerFolder){
        return hostFolder + ":" + containerFolder;
    }
    
}