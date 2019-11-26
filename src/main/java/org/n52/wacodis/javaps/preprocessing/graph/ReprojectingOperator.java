/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.n52.wacodis.javaps.GeometryParseException;
import org.n52.wacodis.javaps.WacodisProcessingException;

import static org.n52.wacodis.javaps.utils.GeometryUtils.decodeCRS;

import org.n52.wacodis.javaps.utils.GeometryUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reprojects a {@link SimpleFeatureCollection} into a target CRS specified by a
 * certain EPSG code
 *
 * @author LukasButzmann
 */
public class ReprojectingOperator extends InputDataOperator<SimpleFeatureCollection> {

    private static final Logger LOG = LoggerFactory.getLogger(ReprojectingOperator.class);

    //    private CoordinateReferenceSystem targetCrs;
    private String targetEpsg;

    public ReprojectingOperator(String targetEpsg) {
        this.targetEpsg = targetEpsg;
    }

    @Override
    public String getName() {
        return "org.wacodis.operator.ReprojectingOperator";
    }

    @Override
    public SimpleFeatureCollection process(SimpleFeatureCollection input) throws WacodisProcessingException {
        CoordinateReferenceSystem targetCrs = null;
        try {
            targetCrs = decodeCRS(targetEpsg);
        } catch (GeometryParseException ex) {
            LOG.error("Could not decode epsg code " + this.targetEpsg);
            throw new WacodisProcessingException("Error while decoding epsg code", ex);
        }

        SimpleFeatureCollection output;
        CoordinateReferenceSystem sourceCrs = input.getSchema().getCoordinateReferenceSystem();

        if (sourceCrs != null && !sourceCrs.equals(targetCrs)) {
            output = new ReprojectingFeatureCollection(input, sourceCrs, targetCrs);
        } else {
            output = new ReprojectingFeatureCollection(input, targetCrs);
        }
        return output;
    }

    @Override
    public String getSupportedClassName() {
        return SimpleFeatureCollection.class.getName();
    }

}
