/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.CoordinateList;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.n52.wacodis.javaps.GeometryParseException;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.io.http.WCSFileDownloader;
import org.n52.wacodis.javaps.utils.GeometryUtils;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Lukas Butzmann
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.wcs_test_algorithm",
        title = "WCS Test Algorithm",
        abstrakt = "Downloads Geotiffs in an Area of Interests and aggrigate them.",
        version = "1.0.0",
        storeSupported = true,
        statusSupported = true)
public class WCSTestAlgorithm{
    
    private static final double MAX_RASTER_RANGE_WCS_RESPONSE = 2000;
    private static final double SCALEFACTOR = 0.1;
    private static final String SUBSETTINGCRS = "EPSG:4326";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WCSTestAlgorithm.class);
    
    private String wcsUrl;
    private String areaOfInterest;
    private List<String> wcsRequestList;
    String[][] requests;
    
    private WacodisBackendConfig config;
    
    @Autowired
    public void setConfig(WacodisBackendConfig config) {
        this.config = config;
    }

    @Autowired
    private WCSFileDownloader wcsDownloader;
    
    @LiteralInput(
            identifier = "WCS_DATA",
            title = "WCS",
            abstrakt = "WCS",
            minOccurs = 1,
            maxOccurs = 1
    )
    public void setWCSUrl(String value) {
        this.wcsUrl = value;
    }
    
    @LiteralInput(
            identifier = "AREA_OF_INTEREST",
            title = "Area of interest",
            abstrakt = "Area of interest of the optical image in GeoJSON-Format e.g. [7.1234, 52.1234, 7.9876, 52.9876]. [0,0,0,0] uses the entire area of the image.",
            minOccurs = 1,
            maxOccurs = 1
    )
    public void setAreaOfInterest(String value) {
        this.areaOfInterest = value;
    }
    
    //TODO: Output merged Geotiff
    @LiteralOutput(
            identifier = "PRODUCT"
    )
    public String getOutput() {
        return Arrays.toString(requests);
    }
    
    @Execute
    public void execute()throws WacodisProcessingException, GeometryParseException{
        
        //BBOX to CoordinateList
        CoordinateList coordListBbox = GeometryUtils.getCoordinatesOfBbox(areaOfInterest);
        
        //EPSG Code to CRS
        CoordinateReferenceSystem crs = GeometryUtils.decodeCRS(SUBSETTINGCRS);
        
        //Extend of images
        double width = 0;
        double height = 0;
        try{
            width = JTS.orthodromicDistance(coordListBbox.get(0), coordListBbox.get(2), crs);
            height = JTS.orthodromicDistance(coordListBbox.get(0), coordListBbox.get(1), crs);
        }catch (TransformException ex) {
            LOGGER.error("Error while calculate orthodromicDistance: {}", ex);
            throw new WacodisProcessingException("Error while trying to calculate orthodromicDistance: {}", ex);
        }
        
        //Number of images
        int numberOfImagesWidth =  (int) (Math.ceil(width/(MAX_RASTER_RANGE_WCS_RESPONSE/SCALEFACTOR)));
        int numberOfImagesHeight =  (int) (Math.ceil(height/(MAX_RASTER_RANGE_WCS_RESPONSE/SCALEFACTOR)));
        //int numberOfImages = numberOfImagesWidth*numberOfImagesHeight;
        
        double minX = coordListBbox.get(0).x;
        double maxX = coordListBbox.get(2).x;
        double minY = coordListBbox.get(0).y;
        double maxY = coordListBbox.get(1).y;
        
        double imageWidth = (maxX - minX) / numberOfImagesWidth;
        double imageHeight = (maxY - minY) / numberOfImagesHeight;

        requests = new String[numberOfImagesWidth][numberOfImagesHeight];
        this.wcsRequestList = new ArrayList();
        //Coordinates of picturers
        for (int i = 0; numberOfImagesWidth > i; i++) {
            double xMin = minX + i * (imageWidth);
            double xMax = minX + (i + 1) * (imageWidth);
            for (int j = 0; numberOfImagesHeight > j; j++) {
                double yMin = minY + j * imageHeight;
                double yMax = minY + (j + 1) * imageHeight;
                wcsRequestList.add(wcsRequestBuilder(xMin, xMax, yMin, yMax));
            }
        }

        List<File> wcsFileList = new ArrayList();
        
        this.wcsRequestList.forEach(wcsr -> {
            try {
                // Download wcs data
                File wcsFile = wcsDownloader.downloadWCSFile(wcsr, config.getWorkingDirectory());
                wcsFileList.add(wcsFile);
            } catch (IOException ex) {
                LOGGER.error("Error while retrieving WCS file: {}", wcsr, ex);
            }
        });
    }
    
    public String wcsRequestBuilder(double xMin, double xMax, double yMin, double yMax){
        String subset = "&SUBSET=x("+xMin+","+xMax+")&SUBSET=y("+yMin+","+yMax+")"
                + "&SUBSETTINGCRS="+SUBSETTINGCRS
                + "&SCALEFACTOR="+SCALEFACTOR;
        String wcsrequest = wcsUrl+subset;                
        return wcsrequest;
    }
    
}