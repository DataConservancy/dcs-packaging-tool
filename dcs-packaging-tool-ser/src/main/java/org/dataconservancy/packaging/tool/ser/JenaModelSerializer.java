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

import org.apache.jena.rdf.model.Model;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.support.AbstractMarshaller;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Spring (Un)marshaller for Jena {@code Model} instances.  (Un)marshalling from input and output streams are supported,
 * but currently DOM and SAX are not.  A {@link JenaModelFactory} must be supplied on construction, which supplies
 * configured {@code Model} instances for deserialization.
 *
 * @see <a href="http://jena.apache.org/documentation/io/index.html">Reading and Writing RDF in Apache Jena</a>
 * @see Model#read(InputStream, String, String)
 * @see Model#write(OutputStream, String, String)
 */
public class JenaModelSerializer extends AbstractMarshaller {

    /**
     * The language of the serialization.
     */
    private String lang;

    /**
     * The base uri to be used when converting relative URI's to absolute URI's.
     */
    private String base;

    /**
     * Responsible for creating new Model instances used to contain the results of unmarshalled RDF streams
     */
    private JenaModelFactory modelFactory;

    public JenaModelSerializer(JenaModelFactory modelFactory) {
        if (modelFactory == null) {
            throw new IllegalArgumentException("JenaModelFactory must not be null.");
        }
        this.modelFactory = modelFactory;
    }

    /**
     * The base uri to be used when converting relative URI's to absolute URI's.
     *
     * @return the base uri, may be {@code null} or the empty string.
     * @see Model#read(InputStream, String, String)
     */
    public String getBase() {
        return base;
    }

    /**
     * The base uri to be used when converting relative URI's to absolute URI's.
     *
     * @param base the base uri, may be {@code null} or the empty string.
     * @see Model#read(InputStream, String, String)
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * The language of the serialization.
     *
     * @return the language being used, may be {@code null}.
     * @see Model#read(InputStream, String, String)
     */
    public String getLang() {
        return lang;
    }

    /**
     * The language of the serialization.
     *
     * @param lang the language being used, may be {@code null}.
     * @see Model#read(InputStream, String, String)
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supports {@link Model} and its sub classes.
     * </p>
     *
     * @param aClass {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return Model.class.equals(aClass) || Model.class.isAssignableFrom(aClass);
    }

    /**
     * {@inheritDoc}
     * @param o {@inheritDoc}
     * @param outputStream {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    protected void marshalOutputStream(Object o, OutputStream outputStream) throws XmlMappingException, IOException {
        Model model = (Model) o;
        model.write(outputStream, lang, base);
    }

    /**
     * {@inheritDoc}
     * @param inputStream {@inheritDoc}
     * @return {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        Model model = modelFactory.newModel();
        model.read(inputStream, base, lang);
        return model;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param o {@inheritDoc}
     * @param node {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected void marshalDomNode(Object o, org.w3c.dom.Node node) throws XmlMappingException {
        throw new UnsupportedOperationException("(Un)marshalling using org.w3c.dom is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param o {@inheritDoc}
     * @param xmlEventWriter {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws UnsupportedOperationException  always
     */
    @Override
    protected void marshalXmlEventWriter(Object o, XMLEventWriter xmlEventWriter) throws XmlMappingException {
        throw new UnsupportedOperationException("(Un)marshalling using javax.xml.stream is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param o {@inheritDoc}
     * @param xmlStreamWriter {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected void marshalXmlStreamWriter(Object o, XMLStreamWriter xmlStreamWriter) throws XmlMappingException {
        throw new UnsupportedOperationException("(Un)marshalling using javax.xml.stream is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param o {@inheritDoc}
     * @param contentHandler {@inheritDoc}
     * @param lexicalHandler {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected void marshalSaxHandlers(Object o, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        throw new UnsupportedOperationException("(Un)marshalling using org.xml.sax is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param o {@inheritDoc}
     * @param writer {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     * @throws UnsupportedOperationException  always
     */
    @Override
    protected void marshalWriter(Object o, Writer writer) throws XmlMappingException, IOException {
        throw new UnsupportedOperationException("Marshalling using java.io.Writer is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param node {@inheritDoc}
     * @return {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected Object unmarshalDomNode(org.w3c.dom.Node node) throws XmlMappingException {
        throw new UnsupportedOperationException("(Un)marshalling using org.w3c.dom is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param xmlEventReader {@inheritDoc}
     * @return {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected Object unmarshalXmlEventReader(XMLEventReader xmlEventReader) throws XmlMappingException {
        throw new UnsupportedOperationException("(Un)marshalling using javax.xml.stream is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param xmlStreamReader {@inheritDoc}
     * @return {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected Object unmarshalXmlStreamReader(XMLStreamReader xmlStreamReader) throws XmlMappingException {
        throw new UnsupportedOperationException("(Un)marshalling using javax.xml.stream is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param xmlReader {@inheritDoc}
     * @param inputSource {@inheritDoc}
     * @return {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource) throws XmlMappingException,
            IOException {
        throw new UnsupportedOperationException("(Un)marshalling using org.xml.sax is unsupported.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently unsupported; throws {@link UnsupportedOperationException}.
     * </p>
     *
     * @param reader {@inheritDoc}
     * @return {@inheritDoc}
     * @throws XmlMappingException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     * @throws UnsupportedOperationException always
     */
    @Override
    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        throw new UnsupportedOperationException("Unmarshalling using java.io.Reader is unsupported.");
    }

}
