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
public class WacodisConfigurationException extends Exception {

    public WacodisConfigurationException() {
    }

    public WacodisConfigurationException(String message) {
        super(message);
    }

    public WacodisConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WacodisConfigurationException(Throwable cause) {
        super(cause);
    }
}
