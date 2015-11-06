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
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ApplicationVersionConverterTest extends AbstractRoundTripConverterTest {

    private ClassPathResource serialization =
            new ClassPathResource("org/dataconservancy/packaging/tool/ser/application-version-v1.ser");

    private ApplicationVersion versionInfo = new ApplicationVersion();

    private ApplicationVersionConverter underTest = new ApplicationVersionConverter();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        versionInfo.setBuildRevision("ee7326fefa2b38933bda12c2ba0a45e859fd565e");
        versionInfo.setBuildTimeStamp("1446650377");
        versionInfo.setBuildNumber("1");

        underTest.setStreamId(StreamId.APPLICATION_VERSION.name());
    }

    @Override
    public InputStream getSerializationInputStream() throws IOException {
        return serialization.getInputStream();
    }

    @Override
    public Object getSerializationObject() {
        return versionInfo;
    }

    @Override
    public AbstractPackageToolConverter getUnderTest() {
        return underTest;
    }

    @Test
    public void testCanConvert() throws Exception {
        assertFalse(underTest.canConvert(String.class));
        assertTrue(underTest.canConvert(ApplicationVersion.class));
    }

    @Test
    public void testMarshal() throws Exception {
        StringWriter writer = new StringWriter();

        underTest.marshal(versionInfo, new PrettyPrintWriter(writer), getMarshalingContext());
        assertTrue(writer.getBuffer().length() > 1);

        String result = writer.getBuffer().toString();
        assertTrue(result.contains(ApplicationVersionConverter.E_APPLICATION_VERSION));
        assertTrue(result.contains(ApplicationVersionConverter.E_BUILDNO));
        assertTrue(result.contains(ApplicationVersionConverter.E_BUILDREV));
        assertTrue(result.contains(ApplicationVersionConverter.E_BUILDTS));
    }

    @Test
    public void testUnmarshal() throws Exception {
        InputStreamReader reader = new InputStreamReader(getSerializationInputStream());

        Object result = underTest.unmarshal(new XppReader(reader, getPullParser()), getUnmarshallingContext());
        assertNotNull(result);

        assertTrue(result instanceof ApplicationVersion);
        assertEquals("1", ((ApplicationVersion) result).getBuildNumber());
        assertEquals("ee7326fefa2b38933bda12c2ba0a45e859fd565e", ((ApplicationVersion) result).getBuildRevision());
        assertEquals("1446650377", ((ApplicationVersion) result).getBuildTimeStamp());
    }
}