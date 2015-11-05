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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.dataconservancy.packaging.tool.ser.StreamMarshaller;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.Result;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class DefaultPackageStateSerializerTest {

    private StreamMarshaller streamMarshaller;

    private Marshaller serializer;

    private Unmarshaller deserializer;

    private ArchiveStreamFactory arxFactory;

    private ArchiveOutputStream arxOs;

    private ArchiveEntry arxEntry;

    private DefaultPackageStateSerializer underTest = new DefaultPackageStateSerializer();

    @Before
    public void setUp() throws Exception {
        streamMarshaller = new StreamMarshaller();
        serializer = mock(Marshaller.class);
        deserializer = mock(Unmarshaller.class);
        streamMarshaller.setMarshaller(serializer);
        streamMarshaller.setUnmarshaller(deserializer);

        arxFactory = mock(ArchiveStreamFactory.class);
        arxOs = mock(ArchiveOutputStream.class);
        arxEntry = mock(ArchiveEntry.class);

        when(arxFactory.newArchiveOutputStream(any(OutputStream.class))).thenReturn(arxOs);

        underTest.setMarshallerMap(
                new HashMap<StreamId, StreamMarshaller>() {
                    {
                        put(StreamId.APPLICATION_VERSION, streamMarshaller);
                    }
                }
        );

        underTest.setArxStreamFactory(arxFactory);
    }

    @Test
    public void testSimple() throws Exception {
        ApplicationVersion versionInfo = new ApplicationVersion();
        versionInfo.setBuildNumber("1");
        versionInfo.setBuildRevision("abcdefg");
        versionInfo.setBuildTimeStamp(String.valueOf(Calendar.getInstance().getTimeInMillis()));

        PackageState state = new PackageState();
        state.setCreationToolVersion(versionInfo);

        underTest.setArchive(false);
        underTest.serialize(state, StreamId.APPLICATION_VERSION, new NullOutputStream());

        verify(serializer).marshal(eq(versionInfo), isNotNull(Result.class));
        verify(arxFactory, never());
    }

    @Test
    public void testSimpleArchive() throws Exception {
        ApplicationVersion versionInfo = new ApplicationVersion();
        versionInfo.setBuildNumber("1");
        versionInfo.setBuildRevision("abcdefg");
        versionInfo.setBuildTimeStamp(String.valueOf(Calendar.getInstance().getTimeInMillis()));

        PackageState state = new PackageState();
        state.setCreationToolVersion(versionInfo);

        underTest.setArchive(true);
        underTest.serialize(state, StreamId.APPLICATION_VERSION, new NullOutputStream());

        verify(serializer).marshal(eq(versionInfo), isNotNull(Result.class));
        verify(arxFactory).newArchiveOutputStream(any(OutputStream.class));
//        verify(arxFactory).newArchiveEntry(eq(StreamId.APPLICATION_VERSION.name()), any(), any(), any(), eq(0644));
    }
}