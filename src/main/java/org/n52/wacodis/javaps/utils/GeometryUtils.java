/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.utils;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GeometryUtils {

    public static final String DEFAULT_INPUT_EPSG = "EPSG:32632";

    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryUtils.class.getName());

    public static CoordinateReferenceSystem decodeCRS(String epsg) {
        CoordinateReferenceSystem crs;

        try {
            crs = CRS.decode(epsg);
        } catch (FactoryException ex) {
            LOGGER.error("Could not decode epsg code " + epsg + ", using default crs WGS84", ex);
            crs = DefaultGeographicCRS.WGS84;
        }

        return crs;
    }

}
