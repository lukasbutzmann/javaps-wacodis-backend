/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractProcessCommand implements ToolExecutionProcess{

    private String processApplication;

    private List<CommandParameter> parameters;

    public AbstractProcessCommand(String processApplication) {
        this.parameters = new ArrayList<>();
        this.processApplication = processApplication;
    }

    @Override
    public abstract ProcessResult execute() throws InterruptedException;

    public String getProcessApplication() {
        return processApplication;
    }

    public void setProcessApplication(String processApplication) {
        this.processApplication = processApplication;
    }

    public List<CommandParameter> getParameter() {
        return parameters;
    }

    public void setParameter(List<CommandParameter> parameter) {
        this.parameters = parameter;
    }

    public void addParameter(CommandParameter param) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        this.parameters.add(param);
    }

}
