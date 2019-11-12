/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration.tools;

/**
 *
 * @author LukasButzmann
 */
public class ParameterConfig {

    private String inputEpsg;

    public String getInputEpsg() {
        return inputEpsg;
    }

    public void setInputEpsg(String inputEpsg) {
        this.inputEpsg = inputEpsg;
    }

    @Override
    public String toString() {
        return "ParameterConfig{" + "inputEpsg=" + inputEpsg + '}';
    }

}
