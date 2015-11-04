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
import java.io.Reader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PackageNameConverterTest extends AbstractRoundTripConverterTest {

    private static final String PACKAGE_NAME = "a package name";

    private ClassPathResource serialization = new ClassPathResource(
            "org/dataconservancy/packaging/tool/ser/package-name-v1.ser");

    private PackageNameConverter underTest = new PackageNameConverter();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        underTest.setStreamId(StreamId.PACKAGE_NAME.name());
    }

    @Override
    public InputStream getSerializationInputStream() throws IOException {
        return serialization.getInputStream();
    }

    @Override
    public Object getSerializationObject() {
        return PACKAGE_NAME;
    }

    @Override
    public AbstractPackageToolConverter getUnderTest() {
        return underTest;
    }

    @Test
    public void testCanConvert() throws Exception {
        assertTrue(underTest.canConvert(String.class));
        assertFalse(underTest.canConvert(Number.class));
    }

    @Test
    public void testMarshal() throws Exception {
        StringWriter writer = new StringWriter();
        underTest.marshal(PACKAGE_NAME, new PrettyPrintWriter(writer), getMarshalingContext());

        assertTrue(writer.getBuffer().length() > 0);
        String result = writer.getBuffer().toString();

        assertTrue(result.contains(PACKAGE_NAME));
        assertTrue(result.contains(PackageNameConverter.E_PACKAGE_NAME));
    }

    @Test
    public void testUnmarshal() throws Exception {
        XmlPullParser parser = getPullParser();
        Reader reader = new InputStreamReader(getSerializationInputStream());

        Object o = underTest.unmarshal(new XppReader(reader, parser), getUnmarshallingContext());

        assertNotNull(o);
        assertTrue(o instanceof String);

        String result = String.valueOf(o);

        assertEquals(PACKAGE_NAME, result);
    }

}