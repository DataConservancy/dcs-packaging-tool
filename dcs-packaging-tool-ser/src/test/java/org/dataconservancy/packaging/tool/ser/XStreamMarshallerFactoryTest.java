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

import org.junit.Rule;
import org.junit.Test;

import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for the XStreamMarshallerFactory, including encoding tests (insuring that the encoding setting is used
 * through the Spring OXM code path).
 */
public class XStreamMarshallerFactoryTest {

    private XStreamMarshallerFactory underTest = new XStreamMarshallerFactory();

    /**
     * The default encoding should be UTF-8 unless you know what you are doing.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultEncoding() throws Exception {
        assertEquals("UTF-8", underTest.getEncoding());
    }

    /**
     * Insures that the XStreamMarshaller produced by the XStreamMarshallerFactory uses the specified encoding setting;
     * that is, the platform default is <em>not</em> being used.
     *
     * @throws Exception
     */
    @Test
    public void testEncodingTest() throws Exception {
        // Holders
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(sink);
        StreamSource source = new StreamSource();

        // Byte sequences for XML elements
        byte[] openString = "<string>".getBytes();
        byte[] closeString = "</string>".getBytes();

        underTest.setEncoding("UTF-8");
        XStreamMarshaller utf8Marshaller = underTest.newInstance();

        underTest.setEncoding("ISO-8859-1");
        XStreamMarshaller iso8859Marshaller = underTest.newInstance();

        // Our literal, a "LATIN SMALL LETTER N WITH TILDE"
        // The two byte sequence 0xC3 0xB1 in UTF-8
        // The single byte sequence 0xF1 in ISO-8859-1
        // Java Unicode literal: \u00F1

        String literal = "\u00F1";

        // "<string>".length = 8 characters, so check the 8th and 9th element of the
        // byte array

        // 0xF1 written in UTF-8 should be 0xC3B1
        utf8Marshaller.marshal(literal, streamResult);
        assertEquals(0xC3, 0x000000FF & sink.toByteArray()[openString.length]);
        assertEquals(0xB1, 0x000000FF & sink.toByteArray()[openString.length + 1]);

        sink.reset();

        // 0xF1 written in ISO-8859-1 should be 0xF1
        iso8859Marshaller.marshal(literal, streamResult);
        assertEquals(0xF1, 0x000000FF & sink.toByteArray()[openString.length]);

        // Bytes representing 0xF1 encoded as UTF-8
        byte[] utf8SourceBytes = new byte[openString.length + 2 + closeString.length];
        System.arraycopy(openString, 0, utf8SourceBytes, 0, openString.length);
        utf8SourceBytes[openString.length] = (byte) 0xc3;
        utf8SourceBytes[openString.length + 1] = (byte) 0xb1;
        System.arraycopy(closeString, 0, utf8SourceBytes, openString.length + 2, closeString.length);

        // Bytes representing 0xF1 encoded as ISO-8859-1
        byte[] iso88591bytes = new byte[openString.length + 2 + closeString.length];
        System.arraycopy(openString, 0, iso88591bytes, 0, openString.length);
        iso88591bytes[openString.length] = (byte) 0xF1;
        System.arraycopy(closeString, 0, iso88591bytes, openString.length + 1, closeString.length);

        // The UTF-8 configured marshaller should be able to unmarshal the utf-8 bytes
        source.setInputStream(new ByteArrayInputStream(utf8SourceBytes));
        assertEquals(literal, utf8Marshaller.unmarshal(source));

        // The ISO-8859-1 configured marshaller should be able to unmarshal the ISO-8859-1 bytes
        source.setInputStream(new ByteArrayInputStream(iso88591bytes));
        assertEquals(literal, iso8859Marshaller.unmarshal(source));

        // But if the ISO-8859-1 marshaller tries to unmarshal utf-8 bytes...
        source.setInputStream(new ByteArrayInputStream(utf8SourceBytes));
        assertNotEquals(literal, iso8859Marshaller.unmarshal(source));

        // Or if the UTF-8 marshaller tries to unmarshal iso-8859-1 bytes...
        source.setInputStream(new ByteArrayInputStream(iso88591bytes));
        assertNotEquals(literal, utf8Marshaller.unmarshal(source));
    }
}