/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.n52.wacodis.javaps.WacodisProcessingException;
import static org.n52.wacodis.javaps.utils.GeometryUtils.decodeCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author LukasButzmann
 */
public class ReprojectingFeatureCollectionOperator extends InputDataOperator<FeatureCollection>{

    private CoordinateReferenceSystem targetCrs;

    public ReprojectingFeatureCollectionOperator(String targetEpsg) {
        this.targetCrs = decodeCRS(targetEpsg);
        //Keine Parameter im Konstruktor! --> ParameterMap Sebastian fragen
    }
    
    @Override
    public String getName() {
        return "org.wacodis.operator.ReprojectingFeatureCollection";
    }

    @Override
    public FeatureCollection process(FeatureCollection input) throws WacodisProcessingException {
        
        FeatureCollection output;
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
        return FeatureCollection.class.getName();
    }
    
    
}
