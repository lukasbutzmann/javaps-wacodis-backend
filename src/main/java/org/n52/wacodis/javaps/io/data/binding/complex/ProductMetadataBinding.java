/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.data.binding.complex;

import org.n52.javaps.io.complex.ComplexData;
import org.n52.wacodis.javaps.io.metadata.ProductMetadata;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ProductMetadataBinding implements ComplexData<ProductMetadata> {

    protected transient ProductMetadata productMetadata;

    public ProductMetadataBinding(ProductMetadata payload) {
        this.productMetadata = payload;
    }

    @Override
    public ProductMetadata getPayload() {
        return this.productMetadata;
    }

    @Override
    public Class<?> getSupportedClass() {
        return ProductMetadata.class;
    }

}
