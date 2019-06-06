/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.metadata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.esa.snap.core.datamodel.Product;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SentinelProductMetadataCreator implements ProductMetadataCreator<Product> {

    @Override
    public ProductMetadata createProductMetadataBinding(Product product) {
        ProductMetadata metadata = new ProductMetadata();
        metadata.setSourceType("CopernicusDataEnvelope");
        metadata.setTimeFrame(createTimeFrame(product));
        return metadata;
    }

    private TimeFrame createTimeFrame(Product product) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setStartTime(df.format(product.getStartTime().getAsDate()));
        timeFrame.setEndTime(df.format(product.getEndTime().getAsDate()));
        return timeFrame;
    }

}
