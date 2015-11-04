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

import java.util.HashMap;
import java.util.Map;

/**
 * Converts the package-level metadata from a {@code Map&lt;String,String>} to a serialized form and back.
 */
public class PackageMetadataConverter extends AbstractPackageToolConverter {

    static final String E_PACKAGE_METADATA = "packageMetadata";

    @Override
    void marshalInternal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) source;

        writer.startNode(E_PACKAGE_METADATA);

        map.forEach((k, v) -> {
            writer.startNode(k);
            writer.setValue(v);
            writer.endNode();
        });

        writer.endNode(); // E_PACKAGE_METADATA
    }

    @Override
    Object unmarshalInternal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        HashMap<String, String> result = new HashMap<>();

        if (!E_PACKAGE_METADATA.equals(reader.getNodeName())) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ELEMENT, E_PACKAGE_METADATA, reader.getNodeName()));
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            result.put(reader.getNodeName(), reader.getValue());
            reader.moveUp();
        }

        return result;
    }

    @Override
    public boolean canConvert(Class type) {
        if (!Map.class.isAssignableFrom(type)) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<Map<String, String>> foo = (Class<Map<String, String>>) type;
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
