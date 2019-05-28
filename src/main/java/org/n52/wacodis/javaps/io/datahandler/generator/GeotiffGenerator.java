/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.datahandler.generator;

import java.io.IOException;
import java.io.InputStream;
import org.n52.javaps.annotation.Properties;
import org.n52.javaps.description.TypedProcessOutputDescription;
import org.n52.javaps.io.AbstractPropertiesInputOutputHandler;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.EncodingException;
import org.n52.javaps.io.OutputHandler;
import org.n52.shetland.ogc.wps.Format;
import org.n52.wacodis.javaps.io.data.binding.complex.GeotiffFileDataBinding;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Properties(
        defaultPropertyFileName = "geotiff.default.json",
        propertyFileName = "geotiff.json")
public class GeotiffGenerator extends AbstractPropertiesInputOutputHandler implements OutputHandler {

    public GeotiffGenerator() {
        super();
        addSupportedBinding(GeotiffFileDataBinding.class);
    }

    @Override
    public InputStream generate(TypedProcessOutputDescription<?> description,
            Data<?> data,
            Format format) throws IOException, EncodingException {
        InputStream theStream = ((GeotiffFileDataBinding) data).getPayload().getDataStream();
        return theStream;
    }

}
