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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.CoordinateList;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.n52.wacodis.javaps.GeometryParseException;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.algorithms.execution.EoToolExecutor;
import org.n52.wacodis.javaps.command.AbstractCommandValue;
import org.n52.wacodis.javaps.command.MultipleCommandValue;
import org.n52.wacodis.javaps.command.ProcessResult;
import org.n52.wacodis.javaps.command.SingleCommandValue;
import org.n52.wacodis.javaps.configuration.WacodisBackendConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfig;
import org.n52.wacodis.javaps.configuration.tools.ToolConfigParser;
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
    private static final String GDAL_CONFIG = "gdal-merge-geotiffs.yml";
    private static final String GDAL_RESULT_POSTFIX = "_merged";
    
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
    private ToolConfigParser toolConfigParser;

    @Autowired
    private EoToolExecutor eoToolExecutor;
    
    private String namingSuffix = "_" + System.currentTimeMillis();
    
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
        
        executeGdalMergeGeotiffs(wcsFileList);
    }
    
    public String wcsRequestBuilder(double xMin, double xMax, double yMin, double yMax){
        String subset = "&SUBSET=x("+xMin+","+xMax+")&SUBSET=y("+yMin+","+yMax+")"
                + "&SUBSETTINGCRS="+SUBSETTINGCRS
                + "&SCALEFACTOR="+SCALEFACTOR;
        String wcsrequest = wcsUrl+subset;                
        return wcsrequest;
    }
    
    protected void executeDockerTool(Map<String, AbstractCommandValue> inputArgumentValues, ToolConfig toolConfig) throws WacodisProcessingException {
        ProcessResult result;
        try {
            result = eoToolExecutor.executeTool(inputArgumentValues, toolConfig);
        } catch (Exception ex) {
            String message = "Error while executing docker process";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
        if (result.getResultCode() != 0) { //tool returns Result Code 0 if finished successfully
            throw new WacodisProcessingException(String.format("EO tool (container: %s) exited with non-zero result code (%s)." +
                            " Cause: %s. Consult tool specific documentation for details",
                    toolConfig.getDocker().getContainer(),
                    result.getResultCode(),
                    result.getOutputMessage()));
        }
        LOGGER.info("EO tool docker process finished "
                + "executing with result code: {}", result.getResultCode());
        LOGGER.debug(result.getOutputMessage());

    }
    
    protected ToolConfig getToolConfig(String toolConfigPath) throws WacodisProcessingException {
        ToolConfig toolConfig = null;
        try {
            toolConfig = toolConfigParser.parse(toolConfigPath);
            toolConfig.getDocker().setContainer(toolConfig.getDocker().getContainer().trim() + this.namingSuffix); //add unique suffix to container name to prevent naming conflicts
            return toolConfig;
        } catch (IOException ex) {
            String message = "Error while reading tool configuration";
            LOGGER.debug(message, ex);
            throw new WacodisProcessingException(message, ex);
        }
    }
    
    protected File executeGdalMergeGeotiffs(List<File> files) throws WacodisProcessingException {
        ToolConfig toolConfig = this.getToolConfig(this.getToolConfigPath(GDAL_CONFIG));
        String resultName = FilenameUtils.concat(config.getWorkingDirectory(),
                "WCS"
                + GDAL_RESULT_POSTFIX
                + "."
                + FilenameUtils.getExtension(files.get(0).getName()));
        File outFile = new File(resultName);
        Map<String, AbstractCommandValue> inputArgumentValues = this.createGdalInputArgumentValues(files, toolConfig.getDocker().getWorkDir(), outFile);

        this.executeDockerTool(inputArgumentValues, toolConfig);

        return outFile;
    }

    protected Map<String, AbstractCommandValue> createGdalInputArgumentValues(List<File> inFiles, String basePath, File outFile) {
        Map<String, AbstractCommandValue> inputArgumentValues = new HashMap();

        inputArgumentValues.put("INPUTS", this.createInputValue(basePath, inFiles, true));
        inputArgumentValues.put("OUTPUT", this.createInputValue(basePath, outFile, true));

        return inputArgumentValues;
    }
    
        /**
     * Creates input argument values from a {@link List} of input data
     * {@link File} objects
     *
     * @param basePath  base path where to read the input data from
     * @param inputData input data as {@link List} of {@link File} objects
     * @return {@link AbstractCommandValue} that encapsulates a {@link List} of
     * input data file paths
     * @throws WacodisProcessingException
     */
    protected AbstractCommandValue createInputValue(String basePath, List<File> inputData, boolean forUnix) {
        MultipleCommandValue value = new MultipleCommandValue(
                inputData.stream()
                        .map(sF -> {
                            String path = FilenameUtils.concat(basePath, sF.getName());
                            return forUnix ? FilenameUtils.separatorsToUnix(path) : path;
                        })
                        .collect(Collectors.toList()));
        return value;
    }

     /**
     * Creates input argument values from an input data {@link File}
     *
     * @param basePath  base path where to read the input data from
     * @param inputData input data as {@link File}
     * @return {@link AbstractCommandValue} that encapsulates an input data file
     * path
     * @throws WacodisProcessingException
     */
    protected AbstractCommandValue createInputValue(String basePath, File inputData, boolean forUnix) {
        String path = FilenameUtils.concat(basePath, inputData.getName());
        return forUnix ? new SingleCommandValue(FilenameUtils.separatorsToUnix(path)) : new SingleCommandValue(path);
    }
    
    protected String getToolConfigPath(String toolConfigName) {
        return this.config.getToolConfigDirectory() + "/" + toolConfigName;
    }
}