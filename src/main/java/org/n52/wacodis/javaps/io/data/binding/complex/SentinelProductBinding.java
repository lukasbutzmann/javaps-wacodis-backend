/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.data.binding.complex;

import org.esa.snap.core.datamodel.Product;
import org.n52.javaps.io.complex.ComplexData;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SentinelProductBinding implements ComplexData<Product> {

    protected transient Product payload;

    @Override
    public Product getPayload() {
        return this.payload;
    }

    @Override
    public Class<?> getSupportedClass() {
        return Product.class;
    }

}
