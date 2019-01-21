/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.algorithms;

import java.util.List;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexInput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.GroupInput;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.n52.wacodis.javaps.io.data.binding.complex.FeatureCollectionBinding;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Algorithm(
        identifier = "de.hsbo.wacodis.land_cover_classification",
        title = "Land Cover Classification",
        abstrakt = "Perform a land cover classification for optical images.",
        version = "0.0.1",
        storeSupported = true,
        statusSupported = true)
public class LandCoverClassificationAlgorithm {

    private List<String> opticalImageGroupInputs;
    private List<String> referenceDataGroupInputs;
    private List<String> opticalImagesSourceType;
    private List<String> opticalImagesSources;
    private List<String> referenceDataType;
    private List<SimpleFeatureCollection> referenceData;
    private String product;

    @GroupInput(
            identifier = "OPTICAL_IMAGES",
            abstrakt = "the source for the optical images",
            title = "Optical images source",
            minOccurs = 1,
            maxOccurs = 10
    )
    public void setOpticalImageGroupInputs(List<String> opticalImageGroupInputs) {
        this.opticalImageGroupInputs = opticalImageGroupInputs;
    }

    @GroupInput(
            identifier = "REFERENCE_DATA",
            abstrakt = "Reference data for land cover classification",
            title = "Reference data",
            minOccurs = 1,
            maxOccurs = 10
    )
    public void setReferenceDataGroupInputs(List<String> referenceDataGroupInputs) {
        this.referenceDataGroupInputs = referenceDataGroupInputs;
    }

    @LiteralInput(
            identifier = "OPTICAL_IMAGES_TYPE",
            title = "Optical images source type",
            abstrakt = "The type of the source for the optical images",
            minOccurs = 1,
            maxOccurs = 1,
            defaultValue = "Sentinel-2",
            allowedValues = {"Sentinel-2", "Aerial_Image"},
            group = "OPTICAL_IMAGES")
    public void setOpticalImagesSourceType(List<String> value) {
        this.opticalImagesSourceType = value;
    }

    @LiteralInput(
            identifier = "OPTICAL_IMAGES_SOURCE",
            title = "Optical images sources",
            abstrakt = "Sources for the optical images",
            minOccurs = 1,
            maxOccurs = 10,
            defaultValue = "Sentinel-2",
            group = "OPTICAL_IMAGES")
    public void setOpticalImagesSources(List<String> value) {
        this.opticalImagesSources = value;
    }

    @LiteralInput(
            identifier = "REFERENCE_DATA_TYPE",
            title = "Reference data type",
            abstrakt = "The type of the reference data",
            minOccurs = 1,
            maxOccurs = 1,
            defaultValue = "ATKIS",
            allowedValues = {"ATKIS", "MANUAL"},
            group = "REFERENCE_DATA")
    public void setReferenceDataType(List<String> value) {
        this.referenceDataType = value;
    }

    @ComplexInput(
            identifier = "REFERENCE_DATA_SOURCE",
            title = "Reference data",
            abstrakt = "Reference data for land cover classification",
            minOccurs = 1,
            maxOccurs = 1,
            binding = FeatureCollectionBinding.class,
            group = "REFERENCE_DATA")
    public void setReferenceData(List<SimpleFeatureCollection> value) {
        this.referenceData = value;
    }

    @Execute
    public void execute() {
        //TODO Resolve ID for optical images and fetch images as GeoTIFF

        //TODO Resolve 
        String sources = String.join(",", opticalImagesSources);
        String references = referenceData.get(0).getBounds().toString();
        this.product = String.join("|",
                "Optical images source type:" + opticalImagesSourceType,
                "Optical images source:" + sources,
                "Reference data type:" + referenceDataType,
                "Reference data bounding box:" + references);
    }

    @LiteralOutput(identifier = "PRODUCT")
    public String getOutput() {
        return this.product;
    }

}
