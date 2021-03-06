/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/**
 * Adds column "class" to a {@link SimpleFeatureCollection} with TrainData 
 * for LandCoverClassification, if it does not already exist
 * 
 * @author LukasButzmann
 */
public class TrainDataOperator extends InputDataOperator<SimpleFeatureCollection> {

    private String category;
    private String attributeName;

    public TrainDataOperator(String attributeName) {
        this.attributeName = attributeName;
        this.category = "class";
    }
    
    public TrainDataOperator(String attributeName, String category) {
        this.attributeName = attributeName;
        this.category = category;
    }

    @Override
    public String getName() {
        return "org.wacodis.writer.TrainDataOperator";
    }

    @Override
    public SimpleFeatureCollection process(SimpleFeatureCollection input) throws WacodisProcessingException {

        
        if (hasDescriptor(attributeName, input)) {
            
            if (attributeName != category){
                //NEW SCHEMA
                SimpleFeatureType schema = input.getSchema();
                // create new schema
                SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                builder.setName(schema.getName());
                builder.setSuperType((SimpleFeatureType) schema.getSuper());
                builder.addAll(schema.getAttributeDescriptors());
                // add new attribute
                builder.add(category, String.class);
                // build new schema
                SimpleFeatureType nSchema = builder.buildFeatureType();

                // loop through features adding new attribute
                List<SimpleFeature> features = new ArrayList<>();
                SimpleFeatureIterator iterator = input.features();
                Map<Object, Integer> attributeMap = new HashMap<>();

                try {
                  int i = 1;
                  while (iterator.hasNext()) {

                    SimpleFeature f = iterator.next();
                    Object key = f.getAttribute(attributeName);


                    if (!attributeMap.containsKey(key)) {
                        attributeMap.put(key, i++);
                    }

                    //copy feature
                    //attributeMap.put(key, attributeMap.get(key));

                    SimpleFeature f2 = DataUtilities.reType(nSchema, f);
                    f2.setAttribute(category, attributeMap.get(key));
                    features.add(f2);
                  }
                } finally {
                    iterator.close();
                }

                SimpleFeatureCollection simpleCollection = DataUtilities.collection(features);
                return simpleCollection;
            }else{
                return(input);
            }
        }else{
            throw new WacodisProcessingException("The Features in InputCollection don't have the Attribute <"+attributeName+">!");
        }
    }

    
    public boolean hasDescriptor(String descriptorName, SimpleFeatureCollection input){
        for(AttributeDescriptor descriptor : input.getSchema().getAttributeDescriptors()){
            if (descriptor.getLocalName().equalsIgnoreCase(attributeName)){
                return true;
            }
        }       
        return false;
    }
    
    @Override
    public String getSupportedClassName() {
        return SimpleFeatureCollection.class.getName();
    }

}
