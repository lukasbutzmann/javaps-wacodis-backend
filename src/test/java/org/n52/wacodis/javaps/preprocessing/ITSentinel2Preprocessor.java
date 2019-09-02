/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.esa.snap.runtime.Engine;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.wacodis.javaps.WacodisProcessingException;
import org.n52.wacodis.javaps.preprocessing.InputDataPreprocessor;
import org.n52.wacodis.javaps.preprocessing.Sentinel2Preprocessor;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ITSentinel2Preprocessor {

    @Test
    public void testPreprocess() throws WacodisProcessingException {
        // Starts the runtime engine and installs thir-party libraries and driver
        Engine engine = Engine.start();
        // Scans for plugins that will be registered with the IIORegistry
        ImageIO.scanForPlugins();

        File file = new File("C:/Users/Sebastian/Entwicklung/Projekte/HSBO/wacodis/data/Sentinel-2_Example/S2B_MSIL2A_20190302T105029_N0211_R051_T32ULB_20190302T135930.zip");
        String outPath = "C:/Users/Sebastian/Entwicklung/Projekte/HSBO/wacodis/data/Sentinel-2_Example";

        InputDataPreprocessor preprocessor = new Sentinel2Preprocessor(true);
        preprocessor.preprocess(file.getPath(), outPath);

        engine.stop();

    }

}
