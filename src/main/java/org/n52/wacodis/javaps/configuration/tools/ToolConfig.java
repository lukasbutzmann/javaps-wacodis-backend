/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration.tools;

import java.util.List;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ToolConfig {

    private String id;
    private DockerConfig docker;
    private CommandConfig command;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DockerConfig getDocker() {
        return docker;
    }

    public void setDocker(DockerConfig docker) {
        this.docker = docker;
    }

    public CommandConfig getCommand() {
        return command;
    }

    public void setCommand(CommandConfig command) {
        this.command = command;
    }   

}
