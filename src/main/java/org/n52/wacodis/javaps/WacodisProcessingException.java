/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class WacodisProcessingException extends Exception {

    public WacodisProcessingException() {
    }

    public WacodisProcessingException(String message) {
        super(message);
    }

    public WacodisProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WacodisProcessingException(Throwable cause) {
        super(cause);
    }

}
