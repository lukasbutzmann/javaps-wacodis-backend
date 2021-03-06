/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.metadata;

import java.util.List;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface ProductMetadataCreator<T> {

    public ProductMetadata createProductMetadata(T product);
    
    public ProductMetadata createProductMetadata(List<T> productList);

    public ProductMetadata createProductMetadata(T resultProduct, List<T> sourceProducts);
}
