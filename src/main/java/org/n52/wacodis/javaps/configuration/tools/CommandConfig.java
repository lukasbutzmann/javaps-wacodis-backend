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
public class CommandConfig {

    private String folder;
    private String name;
    private List<ArgumentConfig> arguments;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ArgumentConfig> getArguments() {
        return arguments;
    }

    public void setArguments(List<ArgumentConfig> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "CommandConfig{" + "folder=" + folder + ", name="
                + name + ", arguments=" + arguments + '}';
    }

}
