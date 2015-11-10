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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DomainProfileUriListConverter extends AbstractPackageToolConverter {

    static final String E_DOMAINURIS = "domainProfileUris";

    static final String E_URI = "uri";

    public DomainProfileUriListConverter() {
        setStreamId(StreamId.DOMAIN_PROFILE_LIST.name());
    }

    @Override
    public void marshalInternal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        @SuppressWarnings("unchecked")
        List<URI> uris = (List<URI>) source;

        writer.startNode(E_DOMAINURIS);

        uris.stream().forEach(u -> {
            writer.startNode(E_URI);
            writer.setValue(u.toString());
            writer.endNode();
        });

        writer.endNode(); // E_DOMAINURIS
    }

    @Override
    public Object unmarshalInternal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        List<URI> uris = new ArrayList<>();

        if (!E_DOMAINURIS.equals(reader.getNodeName())) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ELEMENT, E_DOMAINURIS, reader.getNodeName()));
        }

        try {
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                if (E_URI.equals(reader.getNodeName())) {
                    uris.add(new URI(reader.getValue()));
                }
                reader.moveUp();
            }
        } catch (URISyntaxException e) {
            throw new ConversionException(e.getMessage(), e);
        }

        return uris;
    }

    @Override
    public boolean canConvert(Class type) {
        return List.class.isAssignableFrom(type);
    }
}
