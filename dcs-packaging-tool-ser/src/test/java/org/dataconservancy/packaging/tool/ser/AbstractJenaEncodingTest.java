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
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public abstract class AbstractJenaEncodingTest extends AbstractJenaTest {

    /**
     * Spring OXM Marshaller/Unmarshaller for Jena Models.
     */
    protected JenaModelSerializer modelSerializer;

    /**
     * A fresh Jena Model used by the tests.
     */
    private Model m = ModelFactory.createDefaultModel();
    
    @Before
    public void setUp() throws Exception {
        this.modelSerializer = getModelSerializerUnderTest();
    }

    /**
     * Obtains a fresh instance of the JenaModelSerializer.
     *
     * @return a JenaModelSerializer instance for testing
     */
    protected abstract JenaModelSerializer getModelSerializerUnderTest();

    /**
     * Insures that a unicode character is property encoded as UTF-8 when written to
     * RDF/XML, and that it can be read back.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultModelSerialization() throws Exception {
        // Test default serialization with Jena
        populateAndWrite(m, m.createResource("http://domain.com/" + tildeLiteral), sink);
        assertTrue(contains(expected, sink));

        Model mPrime = createAndPopulate(new ByteArrayInputStream(sink.toByteArray()));
        assertTrue(modelContainsObject(mPrime, "http://domain.com/" + tildeLiteral));
    }

    @Test
    public void testDefaultModelSerializationWithSpringOxm() throws Exception {
        // Test default serialization with the Spring Marshaller interface
        modelSerializer.marshal(populateAndWrite(m, m.createResource("http://domain.com/" + tildeLiteral), sink), streamResult);
        assertTrue(contains(expected, sink));
        assertTrue(contains(expected, (ByteArrayOutputStream) streamResult.getOutputStream()));

        streamSource = fromResult(streamResult);
        Model mPrime = (Model) modelSerializer.unmarshal(streamSource);
        assertTrue(modelContainsObject(mPrime, "http://domain.com/" + tildeLiteral));
    }

    @Test
    public void testTurtleModelSerialization() throws Exception {
        populateAndWrite(m, m.createResource("http://domain.com/" + tildeLiteral), sink, "TTL");
        assertTrue(contains(expected, sink));

        Model mPrime = createAndPopulate(new ByteArrayInputStream(sink.toByteArray()), null, "TTL");
        assertTrue(modelContainsObject(mPrime, "http://domain.com/" + tildeLiteral));
    }

    @Test
    public void testTurtleModelSerializationWithSpringOxm() throws Exception {
        modelSerializer.setLang("TTL");
        modelSerializer.marshal(populateAndWrite(m, m.createResource("http://domain.com/" + tildeLiteral), sink, "TTL"), streamResult);
        assertTrue(contains(expected, sink));
        assertTrue(contains(expected, (ByteArrayOutputStream) streamResult.getOutputStream()));

        streamSource = fromResult(streamResult);
        Model mPrime = (Model) modelSerializer.unmarshal(streamSource);
        assertTrue(modelContainsObject(mPrime, "http://domain.com/" + tildeLiteral));
    }

    @Test
    public void testRdfXmlAbbrevModelSerialization() throws Exception {
        populateAndWrite(m, m.createResource("http://domain.com/" + tildeLiteral), sink, "RDF/XML-ABBREV");
        assertTrue(contains(expected, sink));

        Model mPrime = createAndPopulate(new ByteArrayInputStream(sink.toByteArray()), null, "RDF/XML-ABBREV");
        assertTrue(modelContainsObject(mPrime, "http://domain.com/" + tildeLiteral));
    }

    @Test
    public void testRdfXmlAbbrevModelSerializationWithSpringOxm() throws Exception {
        // Test RDF/XML serialization explicitly against the Spring Marshaller interface
        modelSerializer.setLang("RDF/XML-ABBREV");
        m = ModelFactory.createDefaultModel();
        modelSerializer.marshal(populateAndWrite(m, m.createResource("http://domain.com/" + tildeLiteral), sink, "RDF/XML-ABBREV"), streamResult);
        assertTrue(contains(expected, sink));
        assertTrue(contains(expected, (ByteArrayOutputStream) streamResult.getOutputStream()));

        streamSource = fromResult(streamResult);
        Model mPrime = (Model) modelSerializer.unmarshal(streamSource);
        assertTrue(modelContainsObject(mPrime, "http://domain.com/" + tildeLiteral));
    }

}
