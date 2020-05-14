/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.utils;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
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

    public static final String BBOX_REGEX = "(\\[)([+-]?([0-9]*[.])?[0-9]+)(,\\s*[+-]?([0-9]*[.])?[0-9]+){3}(\\])";

    public static final String EXPECTED_EPSG_FORMAT = "e.g. EPSG:4326";

    public static final String EXPECTED_BBOX_FORMAT = "[minLon, minLat, maxLon, maxLat]";

    public static CoordinateReferenceSystem decodeCRS(String epsg) throws GeometryParseException {
        try {
            return CRS.decode(epsg);
        } catch (FactoryException e) {
            throw new GeometryParseException("Can not parse bounding EPSG code: " + epsg, EXPECTED_EPSG_FORMAT);
        }
    }

    public static String geoJsonBboxToWkt(String bBox) throws GeometryParseException {
        if (!bBox.matches(BBOX_REGEX)) {
            throw new GeometryParseException("Can not parse bounding box: " + bBox, EXPECTED_BBOX_FORMAT);
        }
        String[] coord = bBox.substring(1, bBox.length() - 1).split(",");
        String wkt = "POLYGON (("
                + Double.parseDouble(coord[0]) + " " + Double.parseDouble(coord[1]) + ","
                + Double.parseDouble(coord[0]) + " " + Double.parseDouble(coord[3]) + ","
                + Double.parseDouble(coord[2]) + " " + Double.parseDouble(coord[3]) + ","
                + Double.parseDouble(coord[2]) + " " + Double.parseDouble(coord[1]) + ","
                + Double.parseDouble(coord[0]) + " " + Double.parseDouble(coord[1]) + "))";

        return wkt;
    }
    
        public static CoordinateList getCoordinatesOfBbox(String bBox) throws GeometryParseException{
        if (!bBox.matches(BBOX_REGEX)) {
            throw new GeometryParseException("Can not parse bounding box: " + bBox, EXPECTED_BBOX_FORMAT);
        }
        String[] coord = bBox.substring(1, bBox.length() - 1).split(",");
        
        CoordinateList coordinatelist = new CoordinateList();
        
        coordinatelist.add(new Coordinate(Double.parseDouble(coord[0]), Double.parseDouble(coord[1])));
        coordinatelist.add(new Coordinate(Double.parseDouble(coord[0]), Double.parseDouble(coord[3])));
        coordinatelist.add(new Coordinate(Double.parseDouble(coord[2]), Double.parseDouble(coord[1])));
        coordinatelist.add(new Coordinate(Double.parseDouble(coord[2]), Double.parseDouble(coord[3])));
        
        return coordinatelist;
    }
}
