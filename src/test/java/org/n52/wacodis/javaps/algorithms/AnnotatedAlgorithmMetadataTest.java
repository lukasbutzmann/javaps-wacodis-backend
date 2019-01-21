package org.n52.wacodis.javaps.algorithms;

/*
 * Copyright 2016-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.n52.javaps.algorithm.annotation.AnnotatedAlgorithmMetadata;
import org.n52.shetland.ogc.wps.Format;
import org.n52.shetland.ogc.wps.description.ProcessDescription;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.InputHandler;
import org.n52.javaps.io.InputHandlerRepository;
import org.n52.javaps.io.OutputHandler;
import org.n52.javaps.io.OutputHandlerRepository;
import org.n52.javaps.io.literal.LiteralType;
import org.n52.javaps.io.literal.LiteralTypeRepository;
import org.n52.javaps.io.literal.xsd.LiteralIntType;
import org.n52.javaps.io.literal.xsd.LiteralStringType;
import org.n52.shetland.ogc.wps.description.GroupInputDescription;
import org.n52.shetland.ogc.wps.description.ProcessInputDescription;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class AnnotatedAlgorithmMetadataTest {

    private static final Set<Format> FORMATS = new HashSet<>(Arrays.asList(
            new Format("text/xml", "UTF-8", "http://www.opengis.net/gml/3.2"),
            new Format("text/xml", "UTF-16", "http://www.opengis.net/gml/3.2"),
            new Format("text/xml", (String)null, "http://www.opengis.net/gml/3.2"),
            new Format("text/xml"),
            new Format("text/xml", "UTF-8"),
            new Format("text/xml", "UTF-16")));

    @Rule
    public final ErrorCollector errors = new ErrorCollector();

    @Test
    public void testGroupParsing() {
        IORepo repo = new IORepo();
        AnnotatedAlgorithmMetadata metadata = new AnnotatedAlgorithmMetadata(LandCoverClassificationAlgorithm.class,
                repo, repo,
                new LiteralDataManagerImpl());
        ProcessDescription processDescription = metadata.getDescription();

        Assert.assertThat(processDescription.getInputDescriptions().size(), is(2));
        Assert.assertThat(processDescription.getOutputDescriptions().size(), is(1));

        Assert.assertThat(processDescription.getInput("REFERENCE_DATA"), notNullValue());
        ProcessInputDescription groupDescription = processDescription.getInput("REFERENCE_DATA");
        GroupInputDescription asGroup = groupDescription.asGroup();

        Assert.assertThat(asGroup.getInput("REFERENCE_DATA_SOURCE"), notNullValue());
        ProcessInputDescription input1 = asGroup.getInput("REFERENCE_DATA_SOURCE");
        Assert.assertThat(input1.isComplex(), is(true));
    }

    private static class IORepo implements OutputHandlerRepository, InputHandlerRepository {

        @Override
        public Set<OutputHandler> getOutputHandlers() {
            return Collections.emptySet();
        }

        @Override
        public Optional<OutputHandler> getOutputHandler(
                Format format, Class<? extends Data<?>> binding) {
            return Optional.empty();
        }

        @Override
        public Set<InputHandler> getInputHandlers() {
            return Collections.emptySet();
        }

        @Override
        public Optional<InputHandler> getInputHandler(
                Format format, Class<? extends Data<?>> binding) {
            return Optional.empty();
        }

        @Override
        public Set<Format> getSupportedFormats() {
            return Collections.unmodifiableSet(FORMATS);
        }

        @Override
        public Set<Format> getSupportedFormats(
                Class<? extends Data<?>> binding) {
            return getSupportedFormats();
        }
    }

    static class LiteralDataManagerImpl implements LiteralTypeRepository {

        @Override
        @SuppressWarnings("unchecked")
        public <T> LiteralType<T> getLiteralType(
                Class<? extends LiteralType<?>> literalType, Class<?> payloadType) {

            if (literalType == null || literalType.equals(LiteralType.class)) {
                if (payloadType != null) {
                    if (payloadType.equals(String.class)) {
                        return (LiteralType<T>) new LiteralStringType();
                    } else if (payloadType.equals(Integer.class)) {
                        return (LiteralType<T>) new LiteralIntType();
                    } else {
                        throw new Error("Unsupported payload type");
                    }
                } else {
                    throw new Error("Neither payload type nro literal type given");
                }
            } else {
                try {
                    return (LiteralType<T>) literalType.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new Error(ex);
                }
            }
        }
    }
}
