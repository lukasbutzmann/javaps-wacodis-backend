/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command.docker;

import java.util.ArrayList;
import java.util.List;
import org.n52.wacodis.javaps.command.CommandParameter;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.ToolExecutionProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class DockerProcess implements ToolExecutionProcess{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerProcess.class);

    public DockerProcess(DockerController dockerController) {

    }


    @Override
    public ProcessResult execute() throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
