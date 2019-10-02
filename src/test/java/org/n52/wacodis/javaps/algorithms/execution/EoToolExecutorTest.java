/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms.execution;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.wacodis.javaps.WacodisConfigurationException;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.command.MultipleCommandValue;
import org.n52.wacodis.javaps.command.SingleCommandValue;
import org.n52.wacodis.javaps.command.docker.DockerRunCommandConfiguration;
import org.n52.wacodis.javaps.configuration.tools.ArgumentConfig;
import org.n52.wacodis.javaps.configuration.tools.CommandConfig;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class EoToolExecutorTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static final String SOURCES_KEY = "OPTICAL_IMAGES_SOURCES";
    private static final String REF_DATA_KEY = "REFERENCE_DATA";
    private static final String ARG_QUATITY_MULTIPLE = "multiple";
    private static final String ARG_QUANTITY_SINGLE = "single";
    private static final String ARG_TYPE_REF = "wps-process-reference";
    private static final String ARG_TYPE_STATIC = "static-option";

    private EoToolExecutor exec;
    private CommandConfig cmdConfig;
    private Map<String, AbstractCommandValue> inputValueMap;

    @Before
    public void init() {
        this.exec = new EoToolExecutor();

        this.cmdConfig = new CommandConfig();
        this.cmdConfig.setFolder("/eo/tool/folder");
        this.cmdConfig.setName("eo.sh");

        ArgumentConfig argConfig1 = new ArgumentConfig();
        argConfig1.setName("-input");
        argConfig1.setType(ARG_TYPE_REF);
        argConfig1.setValue(SOURCES_KEY);
        argConfig1.setQuantity(ARG_QUATITY_MULTIPLE);

        ArgumentConfig argConfig2 = new ArgumentConfig();
        argConfig2.setName("-training");
        argConfig2.setType(ARG_TYPE_REF);
        argConfig2.setValue(REF_DATA_KEY);
        argConfig2.setQuantity(ARG_QUANTITY_SINGLE);

        ArgumentConfig argConfig3 = new ArgumentConfig();
        argConfig3.setName("-epsg");
        argConfig3.setType(ARG_TYPE_STATIC);
        argConfig3.setValue("EPSG:4326");
        argConfig3.setQuantity(ARG_QUANTITY_SINGLE);

        this.cmdConfig.setArguments(Lists.newArrayList(argConfig1, argConfig2, argConfig3));

        String pathSource1 = "path/to/source1";
        String pathSource2 = "path/to/source2";
        String pathTrain = "path/to/trainData";

        Map<String, AbstractCommandValue> inputValueMap = this.inputValueMap = new HashMap();
        this.inputValueMap.put(SOURCES_KEY,
                new MultipleCommandValue(Lists.newArrayList(pathSource1, pathSource2)));
        this.inputValueMap.put(REF_DATA_KEY, new SingleCommandValue(pathTrain));
    }

    @Test
    public void testInitRunConfigurationForValidArguments() throws WacodisConfigurationException {

        DockerRunCommandConfiguration runConfig = this.exec.initRunConfiguration(cmdConfig, inputValueMap);

        List expectedParamList
                = Lists.newArrayList(
                        "/eo/tool/folder",
                        "eo.sh",
                        "-input",
                        String.join(",", "path/to/source1", "path/to/source2"),
                        "-training",
                        "path/to/trainData",
                        "-epsg",
                        "EPSG:4326");

        List actualParamList = new ArrayList();
        runConfig.getCommandParameters().forEach(cmdParam -> {
            if (!cmdParam.getParameter().equals("")) {
                actualParamList.add(cmdParam.getParameter());
            }
            actualParamList.add(cmdParam.getValue());
        });

        Assert.assertEquals(expectedParamList, actualParamList);
    }

    @Test
    public void testInitRunConfigurationForInvalidArgumentType() throws WacodisConfigurationException {
        ArgumentConfig inValidArgConfig = new ArgumentConfig();
        inValidArgConfig.setName("-foo");
        inValidArgConfig.setType("invalid-type");
        inValidArgConfig.setValue("bar");
        inValidArgConfig.setQuantity(ARG_QUANTITY_SINGLE);
        this.cmdConfig.getArguments().add(inValidArgConfig);

        exception.expect(WacodisConfigurationException.class);
        this.exec.initRunConfiguration(cmdConfig, inputValueMap);
    }

    @Test
    public void testInitRunConfigurationForNonExistingInputValue() throws WacodisConfigurationException {
        ArgumentConfig inValidArgConfig = new ArgumentConfig();
        inValidArgConfig.setName("-foo");
        inValidArgConfig.setType(ARG_TYPE_REF);
        inValidArgConfig.setValue("NON_EXISTING_INPUT");
        inValidArgConfig.setQuantity(ARG_QUANTITY_SINGLE);
        this.cmdConfig.getArguments().add(inValidArgConfig);

        exception.expect(WacodisConfigurationException.class);
        this.exec.initRunConfiguration(cmdConfig, inputValueMap);
    }
}
