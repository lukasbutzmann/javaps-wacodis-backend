package org.n52.wacodis.dockerjava;

import java.util.*;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.apache.commons.lang.SystemUtils;

public class DockerController {

    private static final String DEFAULT_CONNECTION = "localhost";
    private Map<String, DockerClient> connections = new HashMap<>();

    // see: https://www.baeldung.com/docker-java-api
    private DockerClient dockerClient = getDefaultConnection();

    private CreateContainerResponse container = null;
    private String imagename = null;
    private String containername = null;

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public String getImagename() {
        return imagename;
    }

    public void setContainername(String containername) {
        this.containername = containername;
    }

    public String getContainername() {
        return containername;
    }

    public void startDockerWithVolume(String path, List<String> params) {
        if (imagename == null || containername == null) {
            System.err.println("org.n52.wacodis.dockerjava.DockerController.startDocker invalid containername or imagename");
        }
        ArrayList<String> parameters = new ArrayList<>(params);
        container = createDockerWithVolume(path, parameters);
        dockerClient.startContainerCmd(container.getId()).exec();
        System.out.println(container.toString());
    }

    public void stopDocker() {
        if (container != null) {
            dockerClient.stopContainerCmd(container.getId()).exec();
            System.out.println(container.toString());
        }
    }

    public void removeDocker() {
        if (container != null) {
            dockerClient.removeContainerCmd(container.getId()).exec();
            System.out.println(container.toString());
        }
    }

    private CreateContainerResponse createDockerWithVolume(String path2path, ArrayList<String> parameters) {
        Bind bind = createBinding(path2path);

        return dockerClient.createContainerCmd(imagename)
                .withCmd(parameters)
                .withName(containername)
                .withHostName("localhost")
                //.withEnv("Environment=Value")
                //.withPortBindings(PortBinding.parse("8080:8080"))
                .withBinds(bind)
                .exec();
    }

    public void inspectContainer() {
        InspectContainerResponse inspect = dockerClient.inspectContainerCmd(container.getId()).exec();
        System.out.println("Container running? " + inspect.getState().getRunning());
    }

    private DockerClient getDefaultConnection() {
        return connections.computeIfAbsent(DEFAULT_CONNECTION, id -> {
            DockerClientConfig config = null;

            if (SystemUtils.IS_OS_WINDOWS) {
                config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost("tcp://localhost:2375")//.withDockerTlsVerify(true)//.withDockerTlsVerify("1")
                        .build();
            }
            if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_LINUX || config == null) {  // assume Unix
                config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost("unix:///var/run/docker.sock")
                        .build();
            }

            return DockerClientBuilder.getInstance(config)
                    .withDockerCmdExecFactory(new NettyDockerCmdExecFactory()).build();
        });
    }

    private Bind createBinding(String path2path) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            String bind = path2path.substring(0, path2path.lastIndexOf(":"));
            String volume = path2path.substring(path2path.lastIndexOf(":")+1);
            
            return new Bind(bind, new Volume(volume));
        } else {
            return Bind.parse(path2path);
        }
    }
}
