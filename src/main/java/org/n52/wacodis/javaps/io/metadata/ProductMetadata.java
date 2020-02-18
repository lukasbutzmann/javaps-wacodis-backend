/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.metadata;

import java.time.LocalDateTime;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ProductMetadata {

    private TimeFrame timeFrame;
    private String sourceType;
    private AreaOfInterest areaOfInterest;
    private LocalDateTime crated;

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

    public void setAreaOfInterest(AreaOfInterest areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    public LocalDateTime getCrated() {
        return crated;
    }

    public void setCreated(LocalDateTime crated) {
        this.crated = crated;
    }
}
