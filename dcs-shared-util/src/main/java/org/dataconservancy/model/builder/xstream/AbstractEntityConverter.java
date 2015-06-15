/*
 * Copyright 2014 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.model.builder.xstream;

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;import java.lang.*;import java.lang.Class;import java.lang.IllegalArgumentException;import java.lang.Object;import java.lang.Override;import java.lang.String;

/**
 * Abstract XStream converter for the DCS object model.  Encapsulates common logic for XStream {@link Converter converters}
 * such as nullity checks, XML namespace declarations, and {@link javax.xml.namespace.QName} handling.  All Data Conservancy
 * converters are expected to extend this class.
 */
public abstract class AbstractEntityConverter implements Converter {

    /**
     * The XML namespace this converter knows how to handle.  It can only (de)serialize Data Conservancy entities
     * with this namespace.
     */
    //static final String XMLNS = DcpModelVersion.VERSION_1_0.getXmlns();

    /**
     * Logger
     */
    final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Ensures {@code source} is not {@code null}.
     * @throws IllegalArgumentException if {@code source} is {@code null}
     */
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (source == null) {
            final String msg = "Source object was null.";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Returns the current element name.
     *
     * If {@code reader} implements {@link NsAwareStream},
     * {@link NsAwareStream#getQname()} is called, and the local part of the {@link javax.xml.namespace.QName}
     * is returned. Otherwise, {@link com.thoughtworks.xstream.io.HierarchicalStreamReader#getNodeName()} is returned.
     *
     * @param reader the reader instance
     * @return the current element name
     * @see NsAwareStream#getQname()
     * @see com.thoughtworks.xstream.io.HierarchicalStreamReader#getNodeName()
     */
    protected String getElementName(HierarchicalStreamReader reader) {
        final String name;
        final HierarchicalStreamReader underlyingReader = reader.underlyingReader();
        final java.lang.Class readerClass = underlyingReader.getClass();
        if (NsAwareStream.class.isAssignableFrom(readerClass)) {
            name = ((NsAwareStream) underlyingReader).getQname().getLocalPart();
        } else {
            name = reader.getNodeName();
        }
        return name;
    }

    /**
     * Returns the current element as a {@link javax.xml.namespace.QName}.
     *
     * If {@code reader} implements {@link NsAwareStream},
     * {@link NsAwareStream#getQname()} is returned. Otherwise, a new {@code QName} is constructed using
     * {@link com.thoughtworks.xstream.io.HierarchicalStreamReader#getNodeName()}.
     *
     * @param reader the reader instance
     * @return the current element QName
     * @see NsAwareStream#getQname()
     * @see com.thoughtworks.xstream.io.HierarchicalStreamReader#getNodeName()
     */
    QName getElementQname(HierarchicalStreamReader reader) {
        final QName name;
        final HierarchicalStreamReader underlyingReader = reader.underlyingReader();
        final Class readerClass = underlyingReader.getClass();
        if (NsAwareStream.class.isAssignableFrom(readerClass)) {
            name = ((NsAwareStream) underlyingReader).getQname();
        } else {
            name = new QName(reader.getNodeName());
        }
        return name;
    }    

}
