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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class PackageNameConverterTest {

    private static final String PACKAGE_NAME = "a package name";

    private PackageNameConverter underTest = new PackageNameConverter();

    @Before
    public void setUp() throws Exception {
        underTest.setStreamId(StreamId.PACKAGE_NAME.name());
    }

    @Test
    public void testCanConvert() throws Exception {
        assertTrue(underTest.canConvert(String.class));
        assertFalse(underTest.canConvert(Number.class));
    }

    @Test
    public void testMarshal() throws Exception {
        StringWriter writer = new StringWriter();
        underTest.marshal(PACKAGE_NAME, new PrettyPrintWriter(writer), mock(MarshallingContext.class));

        assertTrue(writer.getBuffer().length() > 0);
        String result = writer.getBuffer().toString();

        assertTrue(result.contains(PACKAGE_NAME));
        assertTrue(result.contains(PackageNameConverter.E_PACKAGE_NAME));
    }

    @Test
    public void testUnmarshal() throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        Reader reader = new InputStreamReader(
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/package-name-v1.ser").getInputStream());

        Object o = underTest.unmarshal(new XppReader(reader, parser), mock(UnmarshallingContext.class));

        assertNotNull(o);
        assertTrue(o instanceof String);

        String result = String.valueOf(o);

        assertEquals(PACKAGE_NAME, result);
    }

    @Test
    public void testRoundTripUnmarshalFirst() throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        Reader reader = new InputStreamReader(
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/package-name-v1.ser").getInputStream());
        StringWriter writer = new StringWriter();


        // Unmarshal first
        final String firstResult = (String)
                underTest.unmarshal(new XppReader(reader, parser), mock(UnmarshallingContext.class));
        assertNotNull(firstResult);

        // Marshal
        underTest.marshal(firstResult, new PrettyPrintWriter(writer), mock(MarshallingContext.class));
        assertTrue(writer.getBuffer().length() > 0);

        // Unmarshal
        final String secondResult = (String)
                underTest.unmarshal(
                        new XppReader(new StringReader(writer.getBuffer().toString()), parser),
                        mock(UnmarshallingContext.class));
        assertNotNull(secondResult);

        assertEquals(firstResult, secondResult);
    }

    @Test
    public void testRoundTripMarshalFirst() throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        StringWriter firstWriter = new StringWriter();
        StringWriter secondWriter = new StringWriter();

        // Marshal first
        underTest.marshal(PACKAGE_NAME, new PrettyPrintWriter(firstWriter), mock(MarshallingContext.class));
        assertTrue(firstWriter.getBuffer().length() > 0);

        // Unmarshal
        String unmarshalResult = (String) underTest.unmarshal(
                new XppReader(new StringReader(firstWriter.getBuffer().toString()), parser),
                mock(UnmarshallingContext.class));
        assertNotNull(unmarshalResult);

        // Marshal
        underTest.marshal(unmarshalResult, new PrettyPrintWriter(secondWriter), mock(MarshallingContext.class));
        assertTrue(secondWriter.getBuffer().length() > 0);

        assertEquals(firstWriter.getBuffer().toString(), secondWriter.getBuffer().toString());
    }
}