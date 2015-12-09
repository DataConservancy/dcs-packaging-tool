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

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Statement;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Contains common utility methods and variables for testing Jena serialization.
 */
public class AbstractJenaTest {

    /**
     * Sink to hold results of Jena serialization.
     */
    protected ByteArrayOutputStream sink = new ByteArrayOutputStream();

    /**
     * Sink to hold results of Spring OXM (i.e. Marshaller) serialization.
     */
    protected StreamResult streamResult = new StreamResult(new ByteArrayOutputStream());

    /**
     * Source of Spring OXM (i.e. Unmarshaller) deserialization.
     */
    protected StreamSource streamSource = new StreamSource();

    /**
     * Our unicode character that we are encoding:
     * LATIN SMALL LETTER N WITH TILDE: 0xF1
     * UTF-8 encoded as: 0xC3B1
     *
     * No matter the platform encoding, we always expect that
     * this literal will be encoded as the UTF-8 byte sequence in {@link #expected}
     */
    final protected String tildeLiteral = "\u00F1";

    /**
     * UTF-8 encoded byte sequence of {@link #tildeLiteral}.  No matter the platform encoding, we always expect that
     * {@link #tildeLiteral} will be encoded as UTF-8.
     */
    final protected byte[] expected = {(byte) 0xC3, (byte) 0xB1};
    
    protected boolean modelContainsObject(Model m, String object) {
        NodeIterator nodeIterator = m.listObjectsOfProperty(m.createProperty("http://domain.com/p"));
        assertTrue("Expected one object.", nodeIterator.hasNext());
        assertEquals(object, nodeIterator.next().asResource().toString());
        return true;
    }

    protected boolean contains(byte[] candidates, ByteArrayOutputStream sink) {
        byte[] sinkBytes = sink.toByteArray();


        OUTER:
        for (int i = 0; i < sinkBytes.length; i++) {
            for (int m = 0; m < candidates.length; m++) {
                if ((0x000000FF & candidates[m]) == (0x000000FF & sinkBytes[i])) {
                    if (m + 1 < candidates.length && i + 1 < sinkBytes.length) {
                        if ((0x000000FF & candidates[m+1]) == (0x000000FF & sinkBytes[i+1])) {
                            return true;
                        } else {
                            m = 0;
                            continue OUTER;
                        }
                    } else if (m + 1 >= candidates.length) {
                        // we've exhausted candidate bytes
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected void print(byte[] b, OutputStream outputStream) throws IOException {
        byte[] nl = System.getProperty("line.separator").getBytes();
        outputStream.write("Array length: ".getBytes());
        outputStream.write(String.valueOf(b.length).getBytes());
        outputStream.write(nl);
        int index = 0;
        for (byte a : b) {
            outputStream.write(String.valueOf(index++).getBytes());
            outputStream.write(" ".getBytes());
            outputStream.write(Integer.toHexString(a).getBytes());
            outputStream.write(" ".getBytes());
            outputStream.write(a);
            outputStream.write(nl);
        }
    }

    protected void print(StreamResult streamResult, OutputStream outputStream) throws IOException {
        IOUtils.copy(fromResult(streamResult).getInputStream(), outputStream);
    }

    protected Model populateAndWrite(Model m, org.apache.jena.rdf.model.Resource object, OutputStream sink) {
        return populateAndWrite(m, object, sink, null);
    }

    protected Model populateAndWrite(Model m, org.apache.jena.rdf.model.Resource object, OutputStream sink, String lang) {
        return populateAndWrite(m, object, sink, lang, null);
    }

    protected Model populateAndWrite(Model m, org.apache.jena.rdf.model.Resource object, OutputStream sink, String lang, String base) {
        Statement s = m.createStatement(m.createResource("http://domain.com/s"), m.createProperty("http://domain.com/p"), object);
        m.add(s);
        m.write(sink, lang, base);
        return m;
    }

    protected Model createAndPopulate(InputStream in) {
        return createAndPopulate(in, null);
    }

    protected Model createAndPopulate(InputStream in, String base) {
        return createAndPopulate(in, base, null);
    }

    protected Model createAndPopulate(InputStream in, String base, String lang) {
        return ModelFactory.createDefaultModel().read(in, base, lang);
    }

    protected StreamSource fromResult(StreamResult result) {
        return new StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream) result.getOutputStream()).toByteArray()));
    }
    
}
