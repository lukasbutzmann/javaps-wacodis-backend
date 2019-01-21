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
public class ProcessResult {

    private int resultCode;

    private String outputMessage;

    public ProcessResult(int code, String message) {
        this.resultCode = code;
        this.outputMessage = message;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getOutputMessage() {
        return outputMessage;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public void setOutputMessage(String outputMessage) {
        this.outputMessage = outputMessage;
    }

}
