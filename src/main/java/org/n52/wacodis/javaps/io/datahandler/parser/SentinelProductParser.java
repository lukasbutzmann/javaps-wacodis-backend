/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.datahandler.parser;

import java.io.IOException;
import java.io.InputStream;
import org.n52.javaps.annotation.Properties;
import org.n52.javaps.description.TypedProcessInputDescription;
import org.n52.javaps.io.AbstractPropertiesInputOutputHandler;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.DecodingException;
import org.n52.javaps.io.InputHandler;
import org.n52.shetland.ogc.wps.Format;
import org.n52.wacodis.javaps.io.data.binding.complex.SentinelProductBinding;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Properties(defaultPropertyFileName = "sentinel.properties")
public class SentinelProductParser extends AbstractPropertiesInputOutputHandler implements InputHandler {

    public SentinelProductParser() {
        super();
        addSupportedBinding(SentinelProductBinding.class);
    }

    @Override
    public Data<?> parse(TypedProcessInputDescription<?> description, InputStream input, Format format) throws IOException, DecodingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
