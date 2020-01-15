/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.metadata;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SentinelProductMetadataCreator implements ProductMetadataCreator<Product> {

    private static final String DATA_ENVELOPE_TYPE = "WacodisProductDataEnvelope";

    @Override
    public ProductMetadata createProductMetadata(Product product) {
        ProductMetadata metadata = new ProductMetadata();
        metadata.setSourceType(DATA_ENVELOPE_TYPE);
        metadata.setTimeFrame(createTimeFrame(product));
        metadata.setAreaOfInterest(createAreaOfInterest(product));
        metadata.setCreated(LocalDateTime.now(ZoneOffset.UTC));
        return metadata;
    }

    @Override
    public ProductMetadata createProductMetadata(List<Product> productList) {
        ProductMetadata metadata = new ProductMetadata();
        metadata.setSourceType("CopernicusDataEnvelope");
        metadata.setTimeFrame(createTimeFrame(productList));
        metadata.setAreaOfInterest(createAreaOfInterest(productList));
        metadata.setCreated(LocalDateTime.now(ZoneOffset.UTC));
        return metadata;
    }

    @Override
    public ProductMetadata createProductMetadata(Product resultProduct, List<Product> sourceProducts) {
        ProductMetadata metadata = createProductMetadata(resultProduct);
        if (metadata.getAreaOfInterest() == null) {
            metadata.setAreaOfInterest(createAreaOfInterest(sourceProducts));
        }
        if (metadata.getTimeFrame() == null) {
            metadata.setTimeFrame(createTimeFrame(sourceProducts));
        }
        return metadata;
    }

    protected AreaOfInterest createAreaOfInterest(Product product) {
        if (product.getSceneGeoCoding() == null) {
            return null;
        }
        GeoPos minNorthWest = product.getSceneGeoCoding().getGeoPos(new PixelPos(0, 0), null);
        GeoPos maxSouthEast = product.getSceneGeoCoding().getGeoPos(
                new PixelPos(product.getSceneRasterWidth(),
                        product.getSceneRasterHeight()), null);
        AreaOfInterest result = new AreaOfInterest();
        result.setExtent(new double[]{
                minNorthWest.getLon(), maxSouthEast.getLat(),
                maxSouthEast.getLon(), minNorthWest.getLat()
        });
        return result;
    }

    protected AreaOfInterest createAreaOfInterest(List<Product> productList) {
        AreaOfInterest aoi = createAreaOfInterest(productList.get(0));
        double minLon = aoi.getExtent()[0];
        double minLat = aoi.getExtent()[1];
        double maxLon = aoi.getExtent()[2];
        double maxLat = aoi.getExtent()[3];
        for (Product p : productList) {
            aoi = createAreaOfInterest(p);
            if (aoi.getExtent()[0] < minLon) {
                minLon = aoi.getExtent()[0];
            }
            if (aoi.getExtent()[1] < minLat) {
                minLat = aoi.getExtent()[1];
            }
            if (aoi.getExtent()[2] > maxLon) {
                maxLon = aoi.getExtent()[2];
            }
            if (aoi.getExtent()[3] > maxLat) {
                maxLat = aoi.getExtent()[3];
            }
        }
        aoi.setExtent(new double[]{minLon, minLat, maxLon, maxLat});
        return aoi;
    }

    protected TimeFrame createTimeFrame(Product product) {
        if (!(product.getStartTime() != null && product.getEndTime() != null)) {
            return null;
        }

        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setStartTime(product.getStartTime().getAsDate().toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime());

        timeFrame.setEndTime(product.getEndTime().getAsDate().toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime());

        return timeFrame;
    }

    protected TimeFrame createTimeFrame(List<Product> productList) {
        TimeFrame timeFrame = new TimeFrame();
        Date startDate = null;
        Date endDate = null;
        for (Product p : productList) {
            if (startDate == null || p.getStartTime().getAsDate().before(startDate)) {
                startDate = p.getStartTime().getAsDate();
            }
            if (endDate == null || p.getEndTime().getAsDate().after(endDate)) {
                endDate = p.getEndTime().getAsDate();
            }
        }
        timeFrame.setStartTime(startDate.toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime());
        timeFrame.setEndTime(endDate.toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime());

        return timeFrame;
    }

}
