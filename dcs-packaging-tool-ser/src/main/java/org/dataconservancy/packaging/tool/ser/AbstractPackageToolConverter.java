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
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import static org.dataconservancy.packaging.tool.ser.XstreamUtil.hasAttribute;

/**
 * Abstract XStream {@link Converter converter} for Package Tool GUI serialization.  Any XStream converters are expected
 * to inherit from this class.
 * <p>
 * This converter is responsible for encapsulating all XStream serializations with serialization metadata, and providing
 * access to the metadata to sub classes via the marshalling contexts {@link MarshallingContext} and
 * {@link UnmarshallingContext}.  The metadata include:
 * </p>
 * <dl>
 * <dt>version - context key {@link #version}, context value {@code Integer} <em>(since version 1)</em></dt>
 * <dd>an integer indicating the version of the serialization format</dd>
 * <dt>converterClass - context key {@link #A_CONVERTER_CLASS}, context value {@code String} <em>(since version 1)</em></dt>
 * <dd>fully qualified name of the Java class that produced the serialization</dd>
 * <dt>stream identifier context key {@link #A_STREAMID}, context value {@code StreamId} <em>(since version 1)</em></dt>
 * <dd>symbolic name identifying the serialized stream</dd>
 * </dl>
 * <p>
 * Additional metadata can be added in the future.  Metadata documentation should include the field name and the
 * version that the metadata was added.  Metadata are meant to enable forward compatability of the serializations
 * consumed and produced by this converter.  Future versions of the Package Tool GUI should be able to read
 * serializations produced by previous versions.
 * </p>
 */
public abstract class AbstractPackageToolConverter implements Converter {

    /**
     * The default value for the serialization format; currently set to '1'
     */
    static final int DEFAULT_VERSION = 1;

    /**
     * Placeholders: Expected element name, actual element name
     */
    static final String ERR_MISSING_EXPECTED_ELEMENT =
            "Error unmarshaling serialization: missing expected XML element '%s', but found '%s'";

    /**
     * Placeholders: Expected attribute name, expected value name
     */
    static final String ERR_MISSING_EXPECTED_ATTRIBUTE_VALUE =
            "Error unmarshaling serialization: missing expected XML attribute '%s' with value '%s'";

    /**
     * Placeholder: Expected attribute name
     */
    static final String ERR_MISSING_EXPECTED_ATTRIBUTE =
            "Error unmarshaling serialization: missing expected XML attribute '%s'";

    /**
     * Placeholder: XML element name
     */
    static final String ERR_NULL_ELEMENT_VALUE =
            "Error unmarshaling serialization: expected a non-null value for XML element '%s'";

    static final String ERR_NO_STREAMID = "Error (un)marshaling serialization: the stream id must not be empty or " +
            "null.  Set a stream identifier using 'setStreamId(String)' first.";

    /**
     * Placeholder: instance of class that failed conversion, conversion class
     */
    static final String ERR_CANNOT_CONVERT = "Error (un)marshaling instance of class '%s': the converter '%s' does " +
            "not support instances of this type.";

    /**
     * Serialization element name
     */
    static final String E_SERIALIZATION = "serialization";

    /**
     * Converter class attribute name
     */
    static final String A_CONVERTER_CLASS = "converterClass";

    /**
     * Version attribute name
     */
    static final String A_VERSION = "version";

    /**
     * Stream identifier attribute name
     */
    static final String A_STREAMID = "streamId";

    /**
     * The value of the version of the serialization, initialized to the default
     */
    private int version = DEFAULT_VERSION;

    /**
     * The symbolic name of the stream being (de)serialized.
     */
    private String streamId;

    /**
     * The version of the serialization format, typically serialized as an attribute on the root XML element of the
     * serialization.  Versions can be managed on a per-converter basis.  By default this returns {@code 1}.
     *
     * @return the version of the serialization format
     */
    int getVersion() {
        return version;
    }

    /**
     * The version of the serialization format, typically serialized as an attribute on the root XML element of the
     * serialization.  By default this field is {@code 1}.
     *
     * @param version the version of the serialization format
     * @throws IllegalArgumentException if the version number is less than 1
     */
    void setVersion(int version) {
        if (version < 1) {
            throw new IllegalArgumentException("Version number must be an integer greater than 0");
        }
        this.version = version;
    }

    /**
     * The symbolic name of the stream being (de)serialized.
     *
     * @return the stream identifier
     */
    public String getStreamId() {
        return streamId;
    }

    /**
     * The symbolic name of the stream being (de)serialized.
     * TODO: change signature to use StreamId enum?
     * @param streamId the name of the stream
     * @throws IllegalArgumentException if the stream identifier is null or the empty string
     */
    public void setStreamId(String streamId) {
        if (streamId == null || streamId.trim().length() == 0) {
            throw new IllegalArgumentException("Stream identifier must not be null or the empty string.");
        }
        this.streamId = streamId;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation wraps calls to {@link #beforeMarshal(Object, HierarchicalStreamWriter, MarshallingContext)},
     * {@link #marshalInternal(Object, HierarchicalStreamWriter, MarshallingContext)}, and
     * {@link #afterMarshal(Object, HierarchicalStreamWriter, MarshallingContext)}.
     * </p>
     *
     * @param source  {@inheritDoc}
     * @param writer  {@inheritDoc}
     * @param context {@inheritDoc}
     */
    @Override
    final public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        checkStreamId();
        beforeMarshal(source, writer, context);
        marshalInternal(source, writer, context);
        afterMarshal(source, writer, context);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation wraps calls to {@link #beforeUnmarshal(HierarchicalStreamReader, UnmarshallingContext)},
     * {@link #unmarshalInternal(HierarchicalStreamReader, UnmarshallingContext)}, and
     * {@link #afterUnmarshal(HierarchicalStreamReader, UnmarshallingContext)}.
     * </p>
     *
     * @param reader  {@inheritDoc}
     * @param context {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    final public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        checkStreamId();
        beforeUnmarshal(reader, context);
        Object o = unmarshalInternal(reader, context);
        afterUnmarshal(reader, context);
        return o;
    }

    /**
     * Responsible for wrapping serializations with metadata, and including the metadata in the
     * {@code MarshallingContext}.
     *
     * @param source the object to be marshalled
     * @param writer a stream to write to
     * @param context a context that allows nested objects to be processed by XStream.
     */
    void beforeMarshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (!canConvert(source.getClass())) {
            throw new ConversionException(String.format(
                    ERR_CANNOT_CONVERT, source.getClass().getName(), this.getClass().getName()));
        }

        writer.startNode(E_SERIALIZATION);
        writer.addAttribute(A_VERSION, String.valueOf(version));
        writer.addAttribute(A_CONVERTER_CLASS, this.getClass().getName());
        writer.addAttribute(A_STREAMID, streamId);

        context.put(A_VERSION, version);
        context.put(A_CONVERTER_CLASS, this.getClass().getName());
        context.put(A_STREAMID, streamId);
    }

    /**
     * Responsible for wrapping serializations with metadata.
     *
     * @param source the object to be marshalled
     * @param writer a stream to write to
     * @param context a context that allows nested objects to be processed by XStream.
     */
    void afterMarshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.endNode();  // end E_SERIALIZATION
    }

    /**
     * Responsible for metadata deserialization, and including the metadata in the
     * {@code UnmarshallingContext}.
     *
     * @param reader The stream to read the text from.
     * @param context a context that allows nested objects to be processed by XStream.
     */
    void beforeUnmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (!E_SERIALIZATION.equals(reader.getNodeName())) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ELEMENT, E_SERIALIZATION, reader.getNodeName()));
        }

        if (!hasAttribute(A_VERSION, String.valueOf(DEFAULT_VERSION), reader)) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ATTRIBUTE_VALUE, A_VERSION, DEFAULT_VERSION));
        }

        if (!hasAttribute(A_CONVERTER_CLASS, reader)) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ATTRIBUTE, A_CONVERTER_CLASS));
        }

        // We could be more strict here, and check for the specific value of the stream id
        if (!hasAttribute(A_STREAMID, reader)) {
            throw new ConversionException(String.format(ERR_MISSING_EXPECTED_ATTRIBUTE, A_STREAMID));
        }

        context.put(A_VERSION, version);
        context.put(A_CONVERTER_CLASS, this.getClass().getName());
        context.put(A_STREAMID, streamId);

        reader.moveDown();
    }

    /**
     * Responsible for metadata deserialization.
     *
     * @param reader the stream to read the text from.
     * @param context a context that allows nested objects to be processed by XStream.
     */
    void afterUnmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        reader.moveUp(); // end E_SERIALIZATION
    }

    void checkStreamId() {
        if (streamId == null || streamId.length() == 0) {
            throw new IllegalStateException(ERR_NO_STREAMID);
        }
    }

    /**
     * Implemented by concrete subclasses.  Same contract as
     * {@link Converter#marshal(Object, HierarchicalStreamWriter, MarshallingContext)}.
     *
     * @param source the object to be marshalled
     * @param writer a stream to write to
     * @param context a context that allows nested objects to be processed by XStream.
     */
    public abstract void marshalInternal(Object source, HierarchicalStreamWriter writer, MarshallingContext context);


    /**
     * Implemented by concrete subclasses.  Same contract as
     * {@link Converter#unmarshal(HierarchicalStreamReader, UnmarshallingContext)}.
     *
     * @param reader the stream to read the text from.
     * @param context a context that allows nested objects to be processed by XStream.
     * @return the resulting object
     */
    public abstract Object unmarshalInternal(HierarchicalStreamReader reader, UnmarshallingContext context);

}
