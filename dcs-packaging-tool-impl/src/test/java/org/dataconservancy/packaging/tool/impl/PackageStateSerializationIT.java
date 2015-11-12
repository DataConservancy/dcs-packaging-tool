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

import org.apache.commons.io.FileUtils;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.Serialize;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;
import org.dataconservancy.packaging.tool.ser.SerializationAnnotationUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.applicationVersion;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.domainObjectsRDF;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.domainProfileUris;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageMetadata;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageName;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageTreeRDF;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.userProperties;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the PackageStateSerializer using <em>production</em> configuration settings (i.e. it does not use test
 * Spring configuration files).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:org/dataconservancy/packaging/tool/ser/config/applicationContext.xml", "classpath:/applicationContext.xml"})
public class PackageStateSerializationIT {

    /**
     * The PackageStateSerializer instance under test.
     */
    @Autowired
    private PackageStateSerializer underTest;

    /**
     * A PackageState object used for testing.  Populated by {@link #preparePackageState()}.
     */
    private PackageState state;

    /**
     * A reference to a File used to serialize or deserialize state.  Used as a field so it can be cleaned up
     * automatically on {@link #tearDown()}.
     */
    private File stateFile;

    /**
     * A Map of fields that are annotated with {@link Serialize} on PackageState. They are keyed by {@link StreamId}.
     */
    private Map<StreamId, PropertyDescriptor> annotatedPackageStateFields =
            SerializationAnnotationUtil.getStreamDescriptors(PackageState.class);

    @Before
    public void setUp() throws Exception {
        state = preparePackageState();

        // Verify assumptions that the SerializationAnnotationUtil found some annotated fields on PackageState.
        // We aren't strict about exactly what was found, just that some are expected to be there.
        assertTrue(annotatedPackageStateFields.size() > 1);

        stateFile = File.createTempFile("PackageStateFile-PackageStateSerializationIT-", ".zip");
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteQuietly(stateFile);
    }

    /**
     * Instantiates an instance of PackageState, and prepares its state by setting test objects on its fields.
     *
     * @return a new instance of package state for testing
     */
    PackageState preparePackageState() {

        // TODO is there a way we can do this with annotation?
        PackageState state = new PackageState();

        state.setCreationToolVersion(applicationVersion);
        state.setDomainProfileIdList(domainProfileUris);
        state.setUserSpecifiedProperties(userProperties);
        state.setDomainObjectRDF(domainObjectsRDF);
        state.setPackageMetadataList(packageMetadata);
        state.setPackageName(packageName);
        state.setPackageTree(packageTreeRDF);

        return state;
    }

    /**
     * Serializes the entire PackageState object to a zip file, then re-reads the zip file and initializes a fresh
     * PackageState object.
     *
     * @throws Exception
     */
    @Test
    public void testSerializationRoundTrip() throws Exception {
        // Serialize the PackageState to a zip file.
        FileOutputStream out = new FileOutputStream(stateFile);
        underTest.serialize(state, out);
        out.close();
        assertTrue(stateFile.exists() && stateFile.length() > 1);

        // Deserialize the PackageState from the zip.
        PackageState deserializedState = new PackageState();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(stateFile));
        underTest.deserialize(deserializedState, in);
        in.close();

        // Verify the non-nullity of each @Serialize field in PackageState
        annotatedPackageStateFields.forEach((streamId, descriptor) -> {
            try {
                assertNotNull(descriptor.getReadMethod().invoke(deserializedState));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }
}
