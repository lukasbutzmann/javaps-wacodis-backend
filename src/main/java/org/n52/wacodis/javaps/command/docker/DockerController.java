package org.n52.wacodis.javaps.command.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.EventsResultCallback;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CountDownLatch;
import org.n52.wacodis.javaps.command.ProcessResult;

public class DockerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerController.class);

    private final DockerClient dockerClient;

    public DockerController() {
        this.dockerClient = getDefaultConnection();
    }

//    public DockerController(Object config) {
//        //this.dockerClient = getCustomConnection();
//    }

    /**
     *
     * @param container
     * @param runConfig
     * @return
     */
    public CreateContainerResponse createDockerContainer(DockerContainer container, DockerRunCommandConfiguration runConfig) {
        CreateContainerResponse createdContainer = this.dockerClient.createContainerCmd(container.getImageName())
                .withName(container.getContainerName())
                .withBinds(runConfig.getVolumeBindings().stream().map(vb -> createBinding(vb)).collect(Collectors.toList()))
                .withPortBindings(runConfig.getPortBindings().stream().map(pb -> PortBinding.parse(pb)).collect(Collectors.toList()))
                .withCmd(runConfig.getCommandParameters().stream().map(param -> param.toString()).collect(Collectors.toList()))
                //.withHostName("localhost")
                .exec();

        return createdContainer;
    }

    /**
     *
     * @param containerID
     */
    public void runDockerContainer(String containerID) {
        LOGGER.info("run docker container winth cotainer id " + containerID);
        this.dockerClient.startContainerCmd(containerID).exec();
    }

    public void runDockerContainer(String containerID, EventsResultCallback containerDiesHandler) {
        this.runDockerContainer(containerID, containerDiesHandler, "die");
    }

    public void runDockerContainer(String containerID, EventsResultCallback containerEventHandler, String... eventFilter) {
        this.dockerClient.eventsCmd().withContainerFilter(containerID).withEventFilter(eventFilter).exec(containerEventHandler);
        runDockerContainer(containerID);
    }

    public ProcessResult runDockerContainer_Sync(String containerID, boolean removeContainer) {
        String log;
        int exitCode;
        ProcessResult containerResult;
        CountDownLatch runLatch = new CountDownLatch(1);
        RunContainerSyncCallback dieActionHandler = new RunContainerSyncCallback(runLatch);

        runDockerContainer(containerID, dieActionHandler); //start container
        try {
            runLatch.await(); //wait until container dies (stopped, finished)
            exitCode = dieActionHandler.getExitCode();
        } catch (InterruptedException e) {
            LOGGER.error("waiting for container " + containerID + " interrupted, container might still be runnig", e);
            throw new RuntimeException(e);
        }
        log = retrieveDockerContainerLog_Sync(containerID);

        containerResult = new ProcessResult(exitCode, log);

        return containerResult;
    }

    public void removeDockerContainer(String containerID) {
        LOGGER.info("remove docker container with container id " + containerID);
        this.dockerClient.removeContainerCmd(containerID).exec();
    }

    public void stopDockerContainer(String containerID) {
        LOGGER.info("stop docker container with container id " + containerID);
        this.dockerClient.stopContainerCmd(containerID).exec();
    }

    public InspectContainerResponse inspectDockerContainer(String containerID) {
        return this.dockerClient.inspectContainerCmd(containerID).exec();
    }

    public void retrieveDockerContainerLog(String containerID, ResultCallback<Frame> logHandler) {
        this.dockerClient.logContainerCmd(containerID)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(true)
                .exec(logHandler);
    }

    public String retrieveDockerContainerLog_Sync(String containerID) {
        CountDownLatch logLatch = new CountDownLatch(1); //wait until command returns
        StringBuilder logBuilder = new StringBuilder();
        RetrieveContainerLogCallback logHandler = new RetrieveContainerLogCallback(containerID, logBuilder, logLatch);

        retrieveDockerContainerLog(containerID, logHandler); //retrieve log

        try {
            logLatch.await(); //waiting until onComplete/on Error of logHandler
        } catch (InterruptedException e) {
            LOGGER.error("retrieving log for container " + containerID + " interrupted, returned log might be incomplete", e);
        }

        return logBuilder.toString();
    }

    public void close() {
        try {
            this.dockerClient.close();
        } catch (IOException ex) {
            LOGGER.warn("could not close docker client");
        }
    }

    private DockerClient getDefaultConnection() {
        DockerClientConfig config = null;

        if (SystemUtils.IS_OS_WINDOWS) { //windows
            config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("tcp://localhost:2375")//.withDockerTlsVerify(true)//.withDockerTlsVerify("1")  //TLS yet supported for Docker for Windows
                    .build();
        }
        if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_LINUX || config == null) {  // Unix or Mac
            config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("unix:///var/run/docker.sock")
                    .build();
        }

        return DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory()).build();
    }

    /**
     * @param path2path expect: "hostPath:containerPath"
     * @return
     */
    private Bind createBinding(String path2path) {
        if (SystemUtils.IS_OS_WINDOWS) { //Bind.parse(string) does not work correctly with Docker for Windows
            String bind = path2path.substring(0, path2path.lastIndexOf(":"));
            String volume = path2path.substring(path2path.lastIndexOf(":") + 1);

            return new Bind(bind, new Volume(volume));
        } else {
            return Bind.parse(path2path);
        }
    }

    
    

    /**
     * only use for EventType 'CONTAINER' and Action 'die' add filters for
     * container id, EventType and Action when registering callback does not
     * apply further checks
     */
    private class RunContainerSyncCallback extends EventsResultCallback {

        private final CountDownLatch runLatch;
        private int exitCode;

        public RunContainerSyncCallback(CountDownLatch runLatch) {
            this.runLatch = runLatch;
        }

        public int getExitCode() {
            return exitCode;
        }

        @Override
        public void onNext(Event event) {
            String containerID = event.getId();

            LOGGER.info("received die event for container " + containerID + System.lineSeparator() + event.toString());

            if (event.getActor().getAttributes() != null && event.getActor().getAttributes().containsKey("exitCode")) {
                this.exitCode = Integer.parseInt(event.getActor().getAttributes().get("exitCode"));
            } else {
                LOGGER.warn("exit code for container " + containerID + "unknown, using default value " + exitCode);
            }

            this.runLatch.countDown();
        }
    }

    private class RetrieveContainerLogCallback implements ResultCallback<Frame> {

        private final String containerID;
        private final CountDownLatch logLatch;
        private final StringBuilder logBuilder;

        public RetrieveContainerLogCallback(String containerID, StringBuilder logBuilder, CountDownLatch logLatch) {
            this.containerID = containerID;
            this.logBuilder = logBuilder;
            this.logLatch = logLatch;
        }

        @Override
        public void onStart(Closeable closeable) {
            LOGGER.debug("retrieving log for container " + containerID);
        }

        @Override
        public void onNext(Frame frame) {
            logBuilder.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
        }

        @Override
        public void onError(Throwable throwable) {
            LOGGER.error("error occured while retrieving log for container: " + containerID, throwable);
            logLatch.countDown();
        }

        @Override
        public void onComplete() {
            LOGGER.debug("successfully retrieved log for container " + containerID);
            logLatch.countDown();
        }

        @Override
        public void close() throws IOException {
        }
    }

}
