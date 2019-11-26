/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.utils;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.n52.wacodis.javaps.GeometryParseException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

/**
 * Helper class for handling geometries and Strings containing geometries
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GeometryUtils {

    public static final String DEFAULT_INPUT_EPSG = "EPSG:32632";

    public static final String EXPECTED_EPSG_FORMAT = "e.g. EPSG:4326";

    public static CoordinateReferenceSystem decodeCRS(String epsg) throws GeometryParseException {
        try {
            return CRS.decode(epsg);
        } catch (FactoryException e) {
            throw new GeometryParseException("Can not parse bounding EPSG code: " + epsg, EXPECTED_EPSG_FORMAT);
        }
    }
}
