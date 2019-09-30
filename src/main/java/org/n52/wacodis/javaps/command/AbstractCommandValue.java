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
public abstract class AbstractCommandValue<T> {

    private T commandValue;

    public AbstractCommandValue(T commandValue) {
        this.commandValue = commandValue;
    }

    public T getCommandValue() {
        return commandValue;
    }

    public void setCommandValue(T value) {
        this.commandValue = value;
    }
}
