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
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Provides methods for testing the round-tripping of objects and serializations.  All converter tests are expected
 * to inherit from this test class.
 */
public abstract class AbstractRoundTripConverterTest extends AbstractConverterTest {

    /**
     * Subclasses are expected to return an InputStream to a serialization of the object they convert.  The
     * serialization should be equivalent to the object provided to {@link #getSerializationObject()}.
     *
     * @return an InputStream to the serialization of the object provided by {@link #getSerializationObject()}
     * @throws IOException
     */
    public abstract InputStream getSerializationInputStream() throws IOException;

    /**
     * Subclasses are expected to return an object that they convert.  The object should be equivalent to the
     * serialization provided by {@link #getSerializationInputStream()}.
     *
     * @return an Object equivalent to the serialization provided by {@link #getSerializationInputStream()}
     */
    public abstract Object getSerializationObject();

    /**
     * Subclasses are expected to return a configured converter, ready to be tested.  A new instance should be returned
     * each time.
     *
     * @return a new instance of a fully configured converter
     */
    public abstract AbstractPackageToolConverter getUnderTest();

    /**
     * Insures that the serialization can be round-tripped to an object and back.
     *
     * @throws Exception
     */
    @Test
    public void testRoundTripUnmarshalFirst() throws Exception {
        XmlPullParser parser = getPullParser();
        StringWriter writer = new StringWriter();
        Reader reader = new InputStreamReader(getSerializationInputStream());
        AbstractPackageToolConverter underTest = getUnderTest();

        // Unmarshal the supplied serialization
        final Object firstResult = underTest.unmarshal(new XppReader(reader, parser), getUnmarshallingContext());
        assertNotNull(firstResult);

        // Marshal the result
        underTest.marshal(firstResult, new PrettyPrintWriter(writer), getMarshalingContext());
        assertTrue(writer.getBuffer().length() > 1);

        // Unmarshal the results of marshalling
        final Object secondResult = underTest.unmarshal(
                new XppReader(new StringReader(writer.getBuffer().toString()), parser),
                getUnmarshallingContext());
        assertNotNull(secondResult);

        assertEquals(firstResult, secondResult);
    }

    /**
     * Insures that the object can be round-tripped to a serialization and back.
     *
     * @throws Exception
     */
    @Test
    public void testRoundTripMarshalFirst() throws Exception {
        XmlPullParser parser = getPullParser();
        StringWriter firstWriter = new StringWriter();
        StringWriter secondWriter = new StringWriter();
        AbstractPackageToolConverter underTest = getUnderTest();


        // Marshal first
        underTest.marshal(getSerializationObject(), new PrettyPrintWriter(firstWriter), getMarshalingContext());
        assertTrue(firstWriter.getBuffer().length() > 0);

        // Unmarshal
        Object unmarshalResult = underTest.unmarshal(
                new XppReader(new StringReader(firstWriter.getBuffer().toString()), parser),
                getUnmarshallingContext());
        assertNotNull(unmarshalResult);

        // Marshal
        underTest.marshal(unmarshalResult, new PrettyPrintWriter(secondWriter), getMarshalingContext());
        assertTrue(secondWriter.getBuffer().length() > 0);

        assertEquals(firstWriter.getBuffer().toString(), secondWriter.getBuffer().toString());
    }

    /**
     * Sanity check insuring that the supplied serialization and the supplied object are equal.
     *
     * @throws Exception
     */
    @Test
    public void testAssertSerializationAndObjectEqual() throws Exception {
        AbstractPackageToolConverter underTest = getUnderTest();
        StringWriter writer = new StringWriter();

        assertEquals(getSerializationObject(), underTest.unmarshal(
                new XppReader(
                        new InputStreamReader(
                                getSerializationInputStream(), "UTF-8"), getPullParser()), getUnmarshallingContext()));
        underTest.marshal(getSerializationObject(), new PrettyPrintWriter(writer), getMarshalingContext());
        assertEquals(IOUtils.toString(getSerializationInputStream()), writer.toString());
    }
}
