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
public class CommandExecutor {

    private AbstractProcessCommand command;

    public AbstractProcessCommand getCommand() {
        return command;
    }

    public void setCommand(AbstractProcessCommand command) {
        this.command = command;
    }
    
    public void executeProcessCommand(){
        
    }

}
