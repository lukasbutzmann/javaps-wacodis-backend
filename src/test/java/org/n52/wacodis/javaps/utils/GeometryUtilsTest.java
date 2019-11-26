package org.n52.wacodis.javaps.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.wacodis.javaps.GeometryParseException;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GeometryUtilsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testDecodeCRSForValidEpsgCode() throws GeometryParseException {
        String validEpsg = "EPSG:32632";
        CoordinateReferenceSystem crs = GeometryUtils.decodeCRS(validEpsg);

        Assert.assertEquals("WGS 84 / UTM zone 32N", crs.getName().getCode());
    }

    @Test
    public void testDecodeCRSThrowsExceptionForInvalidEpsgCode() throws GeometryParseException {
        String validEpsg = "eps:12345";

        thrown.expect(GeometryParseException.class);
        CoordinateReferenceSystem crs = GeometryUtils.decodeCRS(validEpsg);
    }
}
