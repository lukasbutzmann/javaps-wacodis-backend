package org.n52.wacodis.javaps.io.metadata;

import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.Arrays;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SentinelProductMetadataCreatorTest {

    private SentinelProductMetadataCreator creator;

    @Before
    public void init() throws IOException {
        creator = new SentinelProductMetadataCreator();
    }

    @Test
    public void testCreateAreaOfInterest() {
        Product product = new Product("testName", "testType", 5000, 5000);
        GeoCoding geoCoding = Mockito.mock(GeoCoding.class);
        Product spyProduct = Mockito.spy(product);
        Mockito.when(spyProduct.getSceneGeoCoding()).thenReturn(geoCoding);
        GeoPos geoPos01 = Mockito.mock(GeoPos.class);
        Mockito.when(geoPos01.getLon()).thenReturn(7.123);
        Mockito.when(geoPos01.getLat()).thenReturn(53.123);
        GeoPos geoPos02 = Mockito.mock(GeoPos.class);
        Mockito.when(geoPos02.getLon()).thenReturn(7.987);
        Mockito.when(geoPos02.getLat()).thenReturn(52.123);
        Mockito.when(geoCoding.getGeoPos(Mockito.any(PixelPos.class), Mockito.any())).thenReturn(geoPos01, geoPos02);

        AreaOfInterest aoi = creator.createAreaOfInterest(spyProduct);

        Assert.assertEquals(7.123, aoi.getExtent()[0], 0.001);
        Assert.assertEquals(52.123, aoi.getExtent()[1], 0.001);
        Assert.assertEquals(7.987, aoi.getExtent()[2], 0.001);
        Assert.assertEquals(53.123, aoi.getExtent()[3], 0.001);
    }

    @Test
    public void testCreateAreaOfInterestShouldReturnNull() {
        Product product = Mockito.mock(Product.class);

        AreaOfInterest aoi = creator.createAreaOfInterest(product);

        Assert.assertNull(aoi);
    }

    @Test
    public void testCreateAreaOfInterestForProductList() {
        Product product = new Product("testName", "testType", 5000, 5000);
        GeoCoding geoCoding = Mockito.mock(GeoCoding.class);
        Product spyProduct1 = Mockito.spy(product);
        Mockito.when(spyProduct1.getSceneGeoCoding()).thenReturn(geoCoding);
        GeoPos geoPos01 = Mockito.mock(GeoPos.class);
        Mockito.when(geoPos01.getLon()).thenReturn(7.123);
        Mockito.when(geoPos01.getLat()).thenReturn(53.123);
        GeoPos geoPos02 = Mockito.mock(GeoPos.class);
        Mockito.when(geoPos02.getLon()).thenReturn(7.987);
        Mockito.when(geoPos02.getLat()).thenReturn(52.123);
        Mockito.when(geoCoding.getGeoPos(Mockito.any(PixelPos.class), Mockito.any())).thenReturn(geoPos01, geoPos02);

        Product product2 = new Product("testName", "testType", 5000, 5000);
        GeoCoding geoCoding2 = Mockito.mock(GeoCoding.class);
        Product spyProduct2 = Mockito.spy(product2);
        Mockito.when(spyProduct2.getSceneGeoCoding()).thenReturn(geoCoding2);
        GeoPos geo2Pos01 = Mockito.mock(GeoPos.class);
        Mockito.when(geo2Pos01.getLon()).thenReturn(7.000);
        Mockito.when(geo2Pos01.getLat()).thenReturn(53.123);
        GeoPos geo2Pos02 = Mockito.mock(GeoPos.class);
        Mockito.when(geo2Pos02.getLon()).thenReturn(8.987);
        Mockito.when(geo2Pos02.getLat()).thenReturn(52.000);
        Mockito.when(geoCoding2.getGeoPos(Mockito.any(PixelPos.class), Mockito.any())).thenReturn(geo2Pos01, geo2Pos02);

        AreaOfInterest aoi = creator.createAreaOfInterest(Arrays.asList(spyProduct1, spyProduct2));

        Assert.assertEquals(7.000, aoi.getExtent()[0], 0.001);
        Assert.assertEquals(52.000, aoi.getExtent()[1], 0.001);
        Assert.assertEquals(8.987, aoi.getExtent()[2], 0.001);
        Assert.assertEquals(53.123, aoi.getExtent()[3], 0.001);

    }

    @Test
    public void testCreateTimeFrame() throws ParseException {
        Product product = Mockito.mock(Product.class);
        Mockito.when(product.getStartTime()).thenReturn(ProductData.UTC.parse("2020-01-01T10:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        Mockito.when(product.getEndTime()).thenReturn(ProductData.UTC.parse("2020-01-15T20:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));

        TimeFrame timeFrame = creator.createTimeFrame(product);

        Assert.assertEquals("2020-01-01T10:00", timeFrame.getStartTime().toString());
        Assert.assertEquals("2020-01-15T20:00", timeFrame.getEndTime().toString());
    }

    @Test
    public void testCreateTimeFrameShouldReturnNull() throws ParseException {
        Product product = Mockito.mock(Product.class);

        TimeFrame timeFrame = creator.createTimeFrame(product);

        Assert.assertNull(timeFrame);
    }

    @Test
    public void testCreateTimeFrameForProductList() throws ParseException {
        Product product01 = Mockito.mock(Product.class);
        Mockito.when(product01.getStartTime()).thenReturn(ProductData.UTC.parse("2020-01-01T10:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        Mockito.when(product01.getEndTime()).thenReturn(ProductData.UTC.parse("2020-01-15T20:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));

        Product product02 = Mockito.mock(Product.class);
        Mockito.when(product02.getStartTime()).thenReturn(ProductData.UTC.parse("2020-01-01T13:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        Mockito.when(product02.getEndTime()).thenReturn(ProductData.UTC.parse("2020-02-16T15:00:00Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));

        TimeFrame timeFrame = creator.createTimeFrame(Arrays.asList(product01, product02));

        Assert.assertEquals("2020-01-01T10:00", timeFrame.getStartTime().toString());
        Assert.assertEquals("2020-02-16T15:00", timeFrame.getEndTime().toString());
    }

}
