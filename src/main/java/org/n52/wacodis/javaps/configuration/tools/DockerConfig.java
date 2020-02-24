/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.configuration.tools;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DockerConfig {

    private String host;
    private String image;
    private String container;
    private String workDir;
    private String hostWorkDir;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getHostWorkDir() {
        return hostWorkDir;
    }

    public void setHostWorkDir(String hostWorkDir) {
        this.hostWorkDir = hostWorkDir;
    }

    @Override
    public String toString() {
        return "DockerConfig{" + "host=" + host + ", image=" + image
                + ", container=" + container + ", hostWorkDir=" + hostWorkDir + ", workDir=" + workDir + '}';
    }

}
