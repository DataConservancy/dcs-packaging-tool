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
import com.thoughtworks.xstream.converters.Converter;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes configuration options that are not made available by the Spring XStreamMarshaller class.  These options
 * are set on the underlying XStream instance before being returned wrapped by an XStreamMarshaller.
 */
public class XStreamMarshallerFactory {

    /**
     * The encoding used by Spring OXM code paths when reading and writing using XStream.
     */
    private String encoding = "UTF-8";

    /**
     * Encapsulates the properties of a "local" XStream converter: the class it is defined on, the field containing
     * the instance to be converted, and the Converter itself.
     */
    protected List<LocalConverterHolder> localConverters = new ArrayList<>();

    /**
     * Creates a new instance of an XStream marshaller.
     *
     * @return a new instance
     */
    public XStreamMarshaller newInstance() {
        ConfiguredXStreamMarshaller m = new ConfiguredXStreamMarshaller();
        m.setEncoding(encoding);
        return m;
    }

    /**
     * Consumes the {@link #localConverters} {@code List} and registers each {@link LocalConverterHolder} on the
     * supplied XStream instance.
     *
     * @param x the XStream instance
     */
    protected void registerLocalConverters(XStream x) {
        localConverters.stream().forEach(c -> x.registerLocalConverter(c.definedInClass, c.fieldName, c.converter));
    }

    public List<LocalConverterHolder> getLocalConverters() {
        return localConverters;
    }

    public void setLocalConverters(List<LocalConverterHolder> localConverters) {
        this.localConverters = localConverters;
    }

    /**
     * The encoding used when writing to and reading from streams.  By default this is set to {@code UTF-8}.  All
     * {@code XStreamMarshaller} instances produced by this factory will have their encoding set.
     *
     * @return the character encoding used by XStreamMarshallers when reading from or writing to streams
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * The encoding used when writing to and reading from streams.  By default this is set to {@code UTF-8}.  All
     * {@code XStreamMarshaller} instances produced by this factory will have their encoding set.
     *
     * @param encoding the character encoding used by XStreamMarshallers when reading from or writing to streams
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public class ConfiguredXStreamMarshaller extends XStreamMarshaller {
        @Override
        protected void customizeXStream(XStream xstream) {
            super.customizeXStream(xstream);
            registerLocalConverters(xstream);
            xstream.setMode(XStream.NO_REFERENCES);
        }
    }

    /**
     * Encapsulates the properties of a "local" XStream converter: the Converter instance itself, the Class containing
     * the field to be converted, and the name of the field to be converted.
     * <p>
     * See also: {@link XStream#registerLocalConverter(Class, String, Converter)}
     * </p>
     */
    public class LocalConverterHolder {
        private Class definedInClass;
        private String fieldName;
        private Converter converter;

        /**
         * The converter instance to be registered as a local converter
         *
         * @return the converter
         */
        public Converter getConverter() {
            return converter;
        }

        /**
         * The converter instance to be registered as a local converter
         *
         * @param converter the converter
         */
        public void setConverter(Converter converter) {
            this.converter = converter;
        }

        /**
         * The class containing the field to be converted by {@link #setConverter(Converter) the converter}.
         *
         * @return the class containing the field to be converted
         */
        public Class getDefinedInClass() {
            return definedInClass;
        }

        /**
         * The class containing the field to be converted by {@link #setConverter(Converter) the converter}.
         *
         * @param definedInClass the class containing the field to be converted
         */
        public void setDefinedInClass(Class definedInClass) {
            this.definedInClass = definedInClass;
        }

        /**
         * The name of the field to be converted, contained in {@link #setDefinedInClass(Class) the class to be
         * converted}.
         *
         * @return the field name
         */
        public String getFieldName() {
            return fieldName;
        }

        /**
         * The name of the field to be converted, contained in {@link #setDefinedInClass(Class) the class to be
         * converted}.
         *
         * @param fieldName the field name
         */
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
    }
}
