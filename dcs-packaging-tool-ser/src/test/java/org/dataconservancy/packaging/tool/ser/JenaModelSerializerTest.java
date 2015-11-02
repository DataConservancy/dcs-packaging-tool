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
import org.apache.jena.shared.NoReaderForLangException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class JenaModelSerializerTest {

    /**
     * An example namespace to test with.
     */
    private final String IPM_NS = "http://www.dataconservancy.org/internal-package-model#";

    /**
     * Serializer under test
     */
    private JenaModelSerializer underTest;

    /**
     * Sample RDF file to deserialize
     */
    private Resource rdf = new ClassPathResource("org/dataconservancy/packaging/tool/ser/imp.owl");

    @Before
    public void setUp() throws Exception {
        // Mock the model factory to return the default Jena Model
        JenaModelFactory modelFactory = mock(JenaModelFactory.class);
        when(modelFactory.newModel()).thenReturn(ModelFactory.createDefaultModel());

        // Instantiate the serializer with the mocked JenaModelFactory
        underTest = new JenaModelSerializer(modelFactory);
    }

    /**
     * Insures serialization with a null language and null base URI doesn't throw an exception.
     *
     * @throws Exception
     */
    @Test
    public void testSerializeNullLangAndNullBase() throws Exception {
//        File out = File.createTempFile("JenaModelSerializerTest", ".out");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Create a simple model to serialize
        Model m = ModelFactory.createDefaultModel();
        m.add(m.createStatement(m.createResource(IPM_NS + "subject"),
                m.createProperty(IPM_NS, "predicate"), m.createResource(IPM_NS + "object")));

        // Set a null base URI
        assertNull(underTest.getBase());
        // Set a null language
        assertNull(underTest.getLang());

        // Perform the test
        underTest.marshalOutputStream(m, out);

        // Assert that we have some output
        assertTrue(out.size() > 1);
    }

    /**
     * Insures that serialization with a null language doesn't throw an exception
     *
     * @throws Exception
     */
    @Test
    public void testSerializeNullLang() throws Exception {
//        File out = File.createTempFile("JenaModelSerializerTest", ".out");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Create a simple model to serialize
        Model m = ModelFactory.createDefaultModel();
        m.add(m.createStatement(m.createResource(IPM_NS + "subject"),
                m.createProperty(IPM_NS, "predicate"), m.createResource(IPM_NS + "object")));

        // Set a base URI
        underTest.setBase(IPM_NS);
        // Set a null language
        assertNull(underTest.getLang());

        // Perform the test
        underTest.marshalOutputStream(m, out);

        // Assert that we have some output
        assertTrue(out.size() > 1);
    }

    /**
     * Insures that serialization with a null base URI doesn't throw an exception
     *
     * @throws Exception
     */
    @Test
    public void testSerializeNullBase() throws Exception {
//        File out = File.createTempFile("JenaModelSerializerTest", ".out");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Create a simple model to serialize
        Model m = ModelFactory.createDefaultModel();
        m.add(m.createStatement(m.createResource(IPM_NS + "subject"),
                m.createProperty(IPM_NS, "predicate"), m.createResource(IPM_NS + "object")));

        // Set a null base URI
        assertNull(underTest.getBase());
        // Set a language
        underTest.setLang("RDF/XML");

        // Perform the test
        underTest.marshalOutputStream(m, out);

        // Assert that we have some output
        assertTrue(out.size() > 1);
    }

    @Test
    public void testDeserializeNullLangAndNullBase() throws Exception {
        assertNull(underTest.getLang());
        assertNull(underTest.getBase());
        Model result = (Model) underTest.unmarshalInputStream(rdf.getInputStream());
        assertNotNull(result);
        assertNotNull(result.getResource(IPM_NS + "Node"));
    }

    @Test
    public void testDeserializeNullBase() throws Exception {
        assertNull(underTest.getLang());
        underTest.setBase(IPM_NS);
        Model result = (Model) underTest.unmarshalInputStream(rdf.getInputStream());
        assertNotNull(result);
        assertNotNull(result.getResource(IPM_NS + "Node"));
    }

    @Test
    public void testDeserialzeNullBase() throws Exception {
        underTest.setLang("RDF/XML");
        assertNull(underTest.getBase());
        Model result = (Model) underTest.unmarshalInputStream(rdf.getInputStream());
        assertNotNull(result);
        assertNotNull(result.getResource(IPM_NS + "Node"));
    }

    @Test(expected = NoReaderForLangException.class)
    public void testDeserializeInvalidLang() throws Exception {
        underTest.setLang("gibberish");
        underTest.unmarshalInputStream(rdf.getInputStream());
    }

    @Test
    public void testZeroLengthStringLang() throws Exception {
        underTest.setLang("");
        Model result = (Model) underTest.unmarshalInputStream(rdf.getInputStream());
        assertNotNull(result);
        assertNotNull(result.getResource(IPM_NS + "Node"));
    }

    @Test(expected = NoReaderForLangException.class)
    public void testEmptyStringLang() throws Exception {
        underTest.setLang("     ");
        underTest.unmarshalInputStream(rdf.getInputStream());
    }

}