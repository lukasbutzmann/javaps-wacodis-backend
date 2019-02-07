/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command;

import java.util.ArrayList;
import java.util.List;

/**
 * converts AbstractProcessComand to ProcessBuilder needed to start a command as process
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class CommandParser {

    private AbstractProcessCommand command;

    public AbstractProcessCommand getCommand() {
        return command;
    }

    public void setCommand(AbstractProcessCommand command) {
        this.command = command;
    }

    public CommandParser(AbstractProcessCommand command) {
        this.command = command;
    }
    
    public CommandParser(){}
    
    /**
     * creates ProcessBuilder for this.command
     * @return
     */
    public ProcessBuilder parseCommand() {
        List<String> parametersStr = new ArrayList<>();
        
        
        parametersStr.add(this.command.getProcessApplication());

        this.command.getParameter().forEach(p -> {
            
            //paramater might be a unnamed parameter (value only)
            if(p.getParameter() != null && !p.getParameter().trim().isEmpty()){
                parametersStr.add(p.getParameter());
            }

            //parameter might be a flag without value
            if (p.getValue() != null && !p.getValue().trim().isEmpty()) {
                parametersStr.add(p.getValue());
            }
        });

        handleWindowsOS(parametersStr);

        return new ProcessBuilder(parametersStr);
    }

    /**
     * windows needs to run command with cmd.exe
     *
     * @param parameters
     */
    private void handleWindowsOS(List<String> parameters) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            parameters.add(0, "cmd.exe");
            parameters.add(1, "/c");
        }
    }

}
