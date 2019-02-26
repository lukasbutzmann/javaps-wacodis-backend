/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class CommandParameter {

    private String name;
    private String value;

    public CommandParameter() {
    }

    public CommandParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getParameter() {
        return name;
    }

    public void setParameter(String parameter) {
        this.name = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{param: " + this.name + ", val: " + this.value + "}";
    }
}
