/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.datahandler.generator;

import org.n52.javaps.annotation.Properties;
import org.n52.javaps.io.datahandler.generator.GenericFileGenerator;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Properties(
        defaultPropertyFileName = "geotiff.default.json",
        propertyFileName = "geotiff.json")
public class GeotiffGenerator extends GenericFileGenerator {

    public GeotiffGenerator() {
        super();
    }

}
