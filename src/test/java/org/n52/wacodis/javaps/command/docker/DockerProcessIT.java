/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command.docker;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.n52.wacodis.javaps.command.ProcessResult;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class DockerProcessIT {
    
    public DockerProcessIT() {
    }


    /**
     * Test of execute method, of class DockerProcess.
     * @throws java.lang.Exception
     */
    @Test
    @Ignore
    public void testExecute() throws Exception {
        //controller with default connection
        DockerController controller = new DockerController();
        
        DockerContainer container = new DockerContainer("hello-world-container", "hello-world:latest");
        DockerRunCommandConfiguration config = new DockerRunCommandConfiguration();
        
        DockerProcess helloWorldProcess = new DockerProcess(controller, container, config);
        ProcessResult results = helloWorldProcess.execute();
        
        System.out.println("run docker container 1: "+ System.lineSeparator() + "exitCode: " + results.getResultCode()+ " log: " + System.lineSeparator() + results.getOutputMessage());
        
        //run twice to see if container was removed
        results = helloWorldProcess.execute();
        System.out.println("run docker container 2: "+ System.lineSeparator() + "exitCode: " + results.getResultCode()+ " log: " + System.lineSeparator() + results.getOutputMessage());
    }
    
}
