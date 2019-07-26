/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.execution;

import java.util.List;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ToolExecutionConfig {

    private String id;
    private DockerConfig dockerConfig;

    private class DockerConfig {

        private String host;
        private String image;
        private String container;

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
    }

    private class CommandConfig {

        private String folder;
        private String name;
        private List<ArgumentConfig> argument;
    }

    public class ArgumentConfig {

    }

}
