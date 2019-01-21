/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ProcessCommand extends AbstractProcessCommand {

    @Override
    public int execute() {
        int returnCode = -1;
        
        try {
            String cmd = "mvn -version";
            
            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            if (isWindows) {
                cmd = "cmd.exe /c " + cmd;
            }
            
            System.out.println("Command: " + cmd);
            
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(cmd);
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line = null;
                    
                    try {
                        while ((line = input.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            
            returnCode = pr.waitFor();
            
            System.out.println("Return Code: " + returnCode);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return returnCode;
    }

}
