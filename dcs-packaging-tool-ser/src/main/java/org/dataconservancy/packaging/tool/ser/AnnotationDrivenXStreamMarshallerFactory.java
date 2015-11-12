/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.packaging.tool.ser;

import com.thoughtworks.xstream.XStream;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.StreamId;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import static org.dataconservancy.packaging.tool.ser.SerializationAnnotationUtil.getStreamDescriptors;

/**
 *
 */
public class AnnotationDrivenXStreamMarshallerFactory extends XStreamMarshallerFactory {

    private Map<StreamId, PropertyDescriptor> streamDescriptors = getStreamDescriptors(PackageState.class);

    private Map<StreamId, AbstractPackageToolConverter> converters = new HashMap<>();

    @Override
    protected void registerLocalConverters(XStream x) {
        streamDescriptors.forEach((s, pd) -> {
            AbstractPackageToolConverter c;
            if ((c = converters.get(s)) != null) {
                x.registerLocalConverter(pd.getPropertyType(), pd.getName(), c);
            }
        });
    }

    /**
     * The converters
     *
     * @return the converter map, keyed by stream id
     */
    public Map<StreamId, AbstractPackageToolConverter> getConverters() {
        return converters;
    }

    /**
     * The converters
     *
     * @param converters the converter map, keyed by stream id
     */
    public void setConverters(Map<StreamId, AbstractPackageToolConverter> converters) {
        this.converters = converters;
    }

}
