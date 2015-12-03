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
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Insures that the PackageMetadataConverter produces correct serializations, and can de-serialize what it produces.
 */
public class PackageMetadataConverterTest extends AbstractRoundTripConverterTest {

    private LinkedHashMap<String, List<String>> testMap = new LinkedHashMap<String, List<String>>() {
        {
            put("foo", Arrays.asList("bar", "biz"));
            put("baz", Collections.singletonList("bar"));
        }
    };

    private ClassPathResource serialization = new ClassPathResource(
            "org/dataconservancy/packaging/tool/ser/package-metadata-v1.ser");

    private PackageMetadataConverter underTest = new PackageMetadataConverter();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        underTest.setStreamId(StreamId.PACKAGE_METADATA.name());
    }

    @Override
    public InputStream getSerializationInputStream() throws IOException {
        return serialization.getInputStream();
    }

    @Override
    public Object getSerializationObject() {
        return testMap;
    }

    @Override
    public AbstractPackageToolConverter getUnderTest() {
        return underTest;
    }

    @Test
    public void testCanConvert() throws Exception {
        assertTrue(underTest.canConvert(new LinkedHashMap<String, List<String>>().getClass()));

        // this can happen due to type erasure.  We could defend against runtime exceptions
        // by using String.valueOf(...) every time we read an element of the map, but at
        // this juncture we don't do that.
        assertTrue(underTest.canConvert(new LinkedHashMap<Number, List<Number>>().getClass()));
    }

    @Test
    public void testMarshal() throws Exception {
        StringWriter writer = new StringWriter();
        underTest.marshal(testMap, new PrettyPrintWriter(writer), getMarshalingContext());

        assertTrue(writer.getBuffer().length() > 1);

        String result = writer.getBuffer().toString();

        assertTrue(result.contains(PackageMetadataConverter.E_PACKAGE_METADATA));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
        assertTrue(result.contains("baz"));
    }

    @Test(expected = ClassCastException.class)
    public void testMarshalWithNonStringMap() throws Exception {
        StringWriter writer = new StringWriter();

        Map<Number, Number> testMap = new LinkedHashMap<Number, Number>() {
            {
                put(1, 2);
                put(3, 4);
            }
        };

        underTest.marshal(testMap, new PrettyPrintWriter(writer), getMarshalingContext());
    }

    @Test(expected = ConversionException.class)
    public void testMarshalWithHashMap() throws Exception {
        StringWriter writer = new StringWriter();

        Map<Number, Number> testMap = new HashMap<Number, Number>() {
            {
                put(1, 2);
                put(3, 4);
            }
        };

        underTest.marshal(testMap, new PrettyPrintWriter(writer), getMarshalingContext());
    }

    @Test
    public void testUnmarshal() throws Exception {
        XmlPullParser parser = getPullParser();
        InputStreamReader reader = new InputStreamReader(getSerializationInputStream());

        Object result = underTest.unmarshal(new XppReader(reader, parser), getUnmarshallingContext());

        assertNotNull(result);
        assertTrue(Map.class.isAssignableFrom(result.getClass()));

        @SuppressWarnings("unchecked")
        Map<String, List<String>> metadata = (Map<String, List<String>>) result;

        assertEquals(testMap, metadata);
    }

}