/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.preprocessing;

import com.bc.ceres.core.PrintWriterProgressMonitor;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.esa.s2tbx.s2msi.resampler.S2Resampler;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.ProductUtils;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class Sentinel2Preprocessor implements InputDataPreprocessor {

    @Override
    public void preprocess(String inputFilePath, String outputFilePath) throws IOException {
        Product inProduct = ProductIO.readProduct(inputFilePath);

        S2Resampler resampler = new S2Resampler(inProduct.getSceneRasterWidth(), inProduct.getSceneRasterHeight());
        Product resampledProduct = resampler.resample(inProduct);
        Band[] bands = resampledProduct.getBands();

        Product targetProduct = new Product(FilenameUtils
                .removeExtension(FilenameUtils.getName(inputFilePath)), "GeoTIFF");

        int numberBands = inProduct.getNumBands();
        for (int i = 0; i < numberBands; i++) {
            if (bands[i].getSpectralBandIndex() != -1) {
                ProductUtils.copyBand(bands[i].getName(), resampledProduct, targetProduct, true);
            }
        }
        ProductUtils.copyGeoCoding(resampledProduct, targetProduct);
        ProductUtils.copyMasks(resampledProduct, targetProduct);
        ProductUtils.copyMetadata(resampledProduct, targetProduct);

        PrintWriterProgressMonitor monitor = new PrintWriterProgressMonitor(System.out);

        ProductIO.writeProduct(targetProduct, outputFilePath, "GeoTIFF", monitor);
    }

}
