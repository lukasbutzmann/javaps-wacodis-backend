/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.datahandler.generator;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.n52.javaps.annotation.Properties;
import org.n52.javaps.description.TypedProcessOutputDescription;
import org.n52.javaps.io.AbstractPropertiesInputOutputHandler;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.EncodingException;
import org.n52.javaps.io.OutputHandler;
import org.n52.shetland.ogc.wps.Format;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;
import org.n52.wacodis.javaps.io.data.binding.complex.ProductMetadataBinding;

/**
 * Generates product metadata in JSON format
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Properties(
        defaultPropertyFileName = "product-metadata.default.json",
        propertyFileName = "product-metadata.json")
public class ProductMetadataJsonGenerator extends AbstractPropertiesInputOutputHandler implements OutputHandler {

    private ObjectMapper objectMapper;

    public ProductMetadataJsonGenerator() {
        super();
        addSupportedBinding(ProductMetadataBinding.class);
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JSR310Module());
    }

    @Override
    public InputStream generate(TypedProcessOutputDescription<?> description, Data<?> data, Format format) throws IOException, EncodingException {
        ProductMetadata metadata = ((ProductMetadataBinding) data).getPayload();
        
        return new ByteArrayInputStream(objectMapper.writeValueAsBytes(metadata));
    }

}
