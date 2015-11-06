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

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.packaging.tool.model.ser.StreamId;

/**
 * Responsible for serializing the name of a package, a simple string.
 */
public class PackageNameConverter extends AbstractPackageToolConverter {

    static final String E_PACKAGE_NAME = "packageName";

    public PackageNameConverter() {
        setStreamId(StreamId.PACKAGE_NAME.name());
    }

    @Override
    public boolean canConvert(Class type) {
        return String.class.equals(type);
    }

    @Override
    public void marshalInternal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        String packageName = (String) source;

        writer.startNode(E_PACKAGE_NAME);
        writer.setValue(packageName);
        writer.endNode();
    }

    @Override
    public Object unmarshalInternal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (!E_PACKAGE_NAME.equals(reader.getNodeName())) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ELEMENT, E_PACKAGE_NAME, reader.getNodeName()));
        }

        if (reader.getValue() == null) {
            throw new ConversionException(String.format(ERR_NULL_ELEMENT_VALUE, reader.getNodeName()));
        }

        return String.valueOf(reader.getValue());
    }
}
