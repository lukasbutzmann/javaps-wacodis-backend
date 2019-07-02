/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.io.data.binding.complex;

import org.n52.javaps.io.GenericFileData;
import org.n52.javaps.io.complex.ComplexData;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class GeotiffFileDataBinding implements ComplexData<GenericFileData> {

    protected transient GenericFileData payload;

    public GeotiffFileDataBinding(GenericFileData fileData) {
        this.payload = fileData;
    }

    @Override
    public GenericFileData getPayload() {
        return payload;
    }

    @Override
    public Class<GenericFileData> getSupportedClass() {
        return GenericFileData.class;
    }

}
