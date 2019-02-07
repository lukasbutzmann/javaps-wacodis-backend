/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command;

import org.junit.Ignore;
import org.junit.Test;
import org.n52.wacodis.dockerjava.DockerController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 * @author <a href="mailto:adrian.klink@eftas.com">Adrian Klink</a>
 */
public class TestExecuteDockerJavaTestTool {

    public TestExecuteDockerJavaTestTool() {
    }

    /**
     * Test of execute method, of class DockerController.
     */
    @Test
    @Ignore
    public void testExecute() /*throws Exception*/ {
        DockerController dock = new org.n52.wacodis.dockerjava.DockerController();
        dock.setImagename("dlm_docker:wacodis-eo-hackathon");
        dock.setContainername("wacodis-eo-dlm");
        String volumemapping = "/home/ak/Develop/wacodis/public:/public";
        List<String> params = Arrays.asList(
                "-input","S2B_MSIL2A_20181010T104019_N0209_R008_T32ULB_20181010T171128.tif",
                "-result","result.tif",
                "-training","traindata/wacodis_traindata");
        ArrayList<String> parameters = new ArrayList<>(params);
        parameters.add(0, "/bin/ash");
        parameters.add(1,"/eo.sh");
        dock.startDockerWithVolume(volumemapping, parameters);
        dock.listContainers();
        dock.stopDocker();
        dock.removeDocker();
    }
    
}
