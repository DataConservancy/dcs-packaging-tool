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

package org.dataconservancy.packaging.tool.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.dataconservancy.packaging.tool.ser.AbstractXstreamTest;
import org.dataconservancy.packaging.tool.ser.ApplicationVersionConverter;
import org.dataconservancy.packaging.tool.ser.DefaultModelFactory;
import org.dataconservancy.packaging.tool.ser.JenaModelSerializer;
import org.dataconservancy.packaging.tool.ser.PackageMetadataConverter;
import org.dataconservancy.packaging.tool.ser.PackageNameConverter;
import org.dataconservancy.packaging.tool.ser.StreamMarshaller;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.applicationVersion;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.domainObjectsRDF;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.domainProfileUris;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageMetadata;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageName;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestResources.APPLICATION_VERSION_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 *
 */
public class AnnotationDrivenPackageStateSerializerTest {

    /**
     * A mock ArchiveStreamFactory, producing mock ArchiveOutputStream and mock ArchiveEntry objects.
     */
    private ArchiveStreamFactory arxFactory;

    /**
     * A mock ArchiveOutputStream
     */
    private ArchiveOutputStream arxOs;

    /**
     * A mock ArchiveEntry
     */
    private ArchiveEntry arxEntry;

    /**
     * A live PackageState object, typically populated by objects in the
     * {@link AbstractXstreamTest.TestObjects} class.
     */
    private PackageState state = new PackageState();

    /**
     * A map of StreamIds to live (not mocked) StreamMarshallers
     */
    private Map<StreamId, StreamMarshaller> liveMarshallerMap = new HashMap<StreamId, StreamMarshaller>() {
        {
            put(StreamId.APPLICATION_VERSION, new StreamMarshaller() {
                {
                    setStreamId(StreamId.APPLICATION_VERSION);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
            put(StreamId.PACKAGE_METADATA, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_METADATA);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
            put(StreamId.PACKAGE_NAME, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_NAME);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
//            put(StreamId.PACKAGE_TREE, new StreamMarshaller() {
//                {
//                    setStreamId(StreamId.PACKAGE_TREE);
//                    setMarshaller(new JenaModelSerializer(new DefaultModelFactory()));
//                    setUnmarshaller(new JenaModelSerializer(new DefaultModelFactory()));
//                }
//            });
            put(StreamId.DOMAIN_PROFILE_LIST, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_PROFILE_LIST);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
            put(StreamId.DOMAIN_OBJECTS, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_OBJECTS);
                    setMarshaller(new JenaModelSerializer(new DefaultModelFactory()));
                    setUnmarshaller(new JenaModelSerializer(new DefaultModelFactory()));
                }
            });
        }
    };

    /**
     * A map of StreamIds to mocked StreamMarshallers
     */
    private Map<StreamId, StreamMarshaller> mockedMarshallerMap = new HashMap<StreamId, StreamMarshaller>() {
        {
            put(StreamId.APPLICATION_VERSION, new StreamMarshaller() {
                {
                    setStreamId(StreamId.APPLICATION_VERSION);
                    setMarshaller(mock(Marshaller.class));
                    setUnmarshaller(mock(Unmarshaller.class));
                }
            });
            put(StreamId.PACKAGE_METADATA, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_METADATA);
                    setMarshaller(mock(Marshaller.class));
                    setUnmarshaller(mock(Unmarshaller.class));
                }
            });
            put(StreamId.PACKAGE_NAME, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_NAME);
                    setMarshaller(mock(Marshaller.class));
                    setUnmarshaller(mock(Unmarshaller.class));
                }
            });
//            put(StreamId.PACKAGE_TREE, new StreamMarshaller() {
//                {
//                    setStreamId(StreamId.PACKAGE_TREE);
//                    setUnmarshaller(mock(Unmarshaller.class));
//                    setMarshaller(mock(Marshaller.class));
//                }
//            });
            put(StreamId.DOMAIN_PROFILE_LIST, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_PROFILE_LIST);
                    setUnmarshaller(mock(Unmarshaller.class));
                    setMarshaller(mock(Marshaller.class));
                }
            });
            put(StreamId.DOMAIN_OBJECTS, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_OBJECTS);
                    setUnmarshaller(mock(Unmarshaller.class));
                    setMarshaller(mock(Marshaller.class));
                }
            });
        }
    };

    /**
     * The instance under test.
     */
    private AnnotationDrivenPackageStateSerializer underTest = new AnnotationDrivenPackageStateSerializer();

    @Before
    public void setUp() throws Exception {

        /*
         * Mock the classes related to archiving support
         */
        arxEntry = mock(ArchiveEntry.class);
        arxOs = mock(ArchiveOutputStream.class);
        arxFactory = mock(ArchiveStreamFactory.class);
        when(arxFactory.newArchiveOutputStream(any(OutputStream.class))).thenReturn(arxOs);

        /*
         * Populate the live package state object with test objects
         */
        state.setCreationToolVersion(applicationVersion);
        state.setPackageName(packageName);
        state.setPackageMetadataList(packageMetadata);
        state.setDomainProfileIdList(domainProfileUris);
        state.setDomainObjectRDF(domainObjectsRDF);

        /*
         * Configure the live stream marshalling map with XStream converters
         */
        ((XStreamMarshaller) liveMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller())
                .setConverters(new ApplicationVersionConverter());
        ((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_NAME).getMarshaller())
                .setConverters(new PackageNameConverter());
        ((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_METADATA).getMarshaller())
                .setConverters(new PackageMetadataConverter());
        ((XStreamMarshaller) liveMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller())
                .setConverters(new ApplicationVersionConverter());
        ((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_NAME).getUnmarshaller())
                .setConverters(new PackageNameConverter());
        ((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_METADATA).getUnmarshaller())
                .setConverters(new PackageMetadataConverter());

        /*
         * Configure the class under test with the mocked marshaller map, and the mock archive
         * stream factory.  Individual tests can set their marshallers, like using a live marshaller map.
         */
        underTest.setMarshallerMap(mockedMarshallerMap);
        underTest.setArxStreamFactory(arxFactory);
    }

    @Test
    public void testSerializeSimple() throws Exception {
        underTest.setArchive(false);
        underTest.serialize(state, StreamId.APPLICATION_VERSION, new NullOutputStream());

        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller())
                .marshal(eq(applicationVersion), isNotNull(Result.class));
        verifyZeroInteractions(arxFactory);
    }

    @Test
    public void testDeserializeSimple() throws Exception {
        state = new PackageState();
        when(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller().unmarshal(any(Source.class))).thenReturn(applicationVersion);
        underTest.deserialize(state, StreamId.APPLICATION_VERSION, new BufferedInputStream(APPLICATION_VERSION_1.getInputStream()));
        assertNotNull(state.getCreationToolVersion());
        assertEquals(applicationVersion, state.getCreationToolVersion());
        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller()).unmarshal(any(Source.class));
    }

    @Test
    public void testDeserializeZipArchiveSimple() throws Exception {
        // produce a zip archive containing a single serialized stream for this test.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(StreamId.APPLICATION_VERSION.name());
        zipOut.putArchiveEntry(zipEntry);
        IOUtils.copy(APPLICATION_VERSION_1.getInputStream(), zipOut);
        zipOut.closeArchiveEntry();
        zipOut.close();

        state = new PackageState();
        when(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller().unmarshal(any(Source.class))).thenReturn(applicationVersion);
        ByteArrayInputStream zipIn = new ByteArrayInputStream(baos.toByteArray());
        underTest.deserialize(state, StreamId.APPLICATION_VERSION, zipIn);

        assertNotNull(state.getCreationToolVersion());
        assertEquals(applicationVersion, state.getCreationToolVersion());
        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller()).unmarshal(any(Source.class));
    }

    @Test
    public void testSerializeStreamWithNullFieldInPackageState() throws Exception {
        state = new PackageState();
        StreamResult result = new StreamResult(new NullOutputStream());  // we're using mocks, so nothing will be
                                                                         // written to the output stream
        assertNull(state.getCreationToolVersion());

        underTest.serializeToResult(state, StreamId.APPLICATION_VERSION, result);

        // Nothing was serialized, the application version field for the state was null.
        verifyZeroInteractions(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller());

        // Set a non-null value, and try again
        state.setCreationToolVersion(applicationVersion);
        underTest.serializeToResult(state, StreamId.APPLICATION_VERSION, result);

        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller()).marshal(applicationVersion, result);
    }

    @Test
    public void testMarshalEntireState() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        underTest.setArchive(false);
        underTest.serialize(state, sink);

        AtomicInteger verifiedStreamCount = new AtomicInteger(0);
        mockedMarshallerMap.entrySet().forEach(entry -> {
            StreamId streamId = entry.getKey();
            StreamMarshaller streamMarshaller = entry.getValue();

            try {
                switch (streamId) {

                    case APPLICATION_VERSION:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(applicationVersion), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case PACKAGE_NAME:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(packageName), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case PACKAGE_METADATA:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(packageMetadata), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case DOMAIN_PROFILE_LIST:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(domainProfileUris), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case DOMAIN_OBJECTS:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(domainObjectsRDF), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                }
            } catch (IOException e) {
                fail("Encountered IOE: " + e.getMessage());
            }
        });

        assertEquals(mockedMarshallerMap.size(), verifiedStreamCount.intValue());

    }

    @Test
    public void testSimpleArchive() throws Exception {
        underTest.setArchive(true);

        underTest.serialize(state, StreamId.APPLICATION_VERSION, new NullOutputStream());

        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller())
                .marshal(eq(applicationVersion), isNotNull(Result.class));
        verify(arxFactory).newArchiveOutputStream(any(OutputStream.class));
//        verify(arxFactory).newArchiveEntry(eq(StreamId.APPLICATION_VERSION.name()), any(), any(), any(), eq(0644));
    }

    @Test
    public void testSerializeApplicationVersionWithLiveMarshallers() throws Exception {
        underTest.setArchive(false);
        underTest.setMarshallerMap(liveMarshallerMap);

        // Set a spy on the ApplicationVersionConverter
        ApplicationVersionConverter applicationVersionConverter = spy(new ApplicationVersionConverter());
        XStreamMarshaller xsm = (XStreamMarshaller) underTest.getMarshallerMap()
                .get(StreamId.APPLICATION_VERSION).getMarshaller();
        xsm.setConverters(applicationVersionConverter);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        underTest.serialize(state, StreamId.APPLICATION_VERSION, result);

        verify(applicationVersionConverter, atLeastOnce()).canConvert(ApplicationVersion.class);
        // cant verify the marshal(...) method b/c it's final
        verify(applicationVersionConverter).
                marshalInternal(eq(applicationVersion),
                        any(HierarchicalStreamWriter.class), any(MarshallingContext.class));
        assertTrue(result.size() > 1);
    }

    @Test
    public void testArchiveSerializeApplicationVersion() throws Exception {
        underTest.setArchive(true);
        underTest.setMarshallerMap(liveMarshallerMap);

        // Set a spy on the ApplicationVersionConverter
        ApplicationVersionConverter applicationVersionConverter = spy(new ApplicationVersionConverter());
        XStreamMarshaller xsm = (XStreamMarshaller) underTest.getMarshallerMap()
                .get(StreamId.APPLICATION_VERSION).getMarshaller();
        xsm.setConverters(applicationVersionConverter);

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        when(arxFactory.newArchiveOutputStream(result)).thenAnswer(invocationOnMock ->
                new ArchiveOutputStream() {
                    @Override
                    public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
                    }

                    @Override
                    public void closeArchiveEntry() throws IOException {
                    }

                    @Override
                    public void finish() throws IOException {
                    }

                    @Override
                    public ArchiveEntry createArchiveEntry(File file, String s) throws IOException {
                        return mock(ArchiveEntry.class);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        result.write(b, off, len);
                    }
                });


        underTest.serialize(state, StreamId.APPLICATION_VERSION, result);

        verify(applicationVersionConverter, atLeastOnce()).canConvert(ApplicationVersion.class);
        // cant verify the marshal(...) method b/c it's final
        verify(applicationVersionConverter).
                marshalInternal(eq(applicationVersion),
                        any(HierarchicalStreamWriter.class), any(MarshallingContext.class));
        assertTrue(result.size() > 1);
    }

    @Test
    public void testArchiveZipLive() throws Exception {
        underTest.setArchive(true);
        underTest.setArxStreamFactory(new ZipArchiveStreamFactory());

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        underTest.serialize(state, StreamId.APPLICATION_VERSION, result);

        assertTrue(result.size() > 1);
    }

}