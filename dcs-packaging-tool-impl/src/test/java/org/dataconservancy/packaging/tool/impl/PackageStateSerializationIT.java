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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ser.Serialize;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;
import org.dataconservancy.packaging.tool.ser.SerializationAnnotationUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.packaging.tool.impl.TestPackageState.V1.Objects;
import static org.dataconservancy.packaging.tool.impl.TestPackageState.V1.Resources;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the PackageStateSerializer using <em>production</em> configuration settings (i.e. it does not use test
 * Spring configuration files).  This test class <em>does</em> use test objects from the {@code dcs-packaging-tool-ser}
 * module to populate instances of {@code PackageState}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:org/dataconservancy/packaging/tool/ser/config/applicationContext.xml", "classpath*:org/dataconservancy/config/applicationContext.xml"})
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

        state.setCreationToolVersion(Objects.appVersion);
        state.setDomainProfileIdList(Objects.profiles);
        state.setUserSpecifiedProperties(Objects.userProps);
        state.setDomainObjectRDF(Objects.objects);
        state.setPackageMetadataList(Objects.metadata);
        state.setPackageName(Objects.name);
        state.setPackageTree(Objects.tree);

        return state;
    }

    /**
     * Serializes the entire PackageState object to a zip file, then re-reads the zip file and initializes a fresh
     * PackageState object.  The two state objects are compared for equality.
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
                assertNotNull(
                        "Expected non-null value for field " + descriptor.getName() + " on deserialized PackageState",
                        descriptor.getReadMethod().invoke(deserializedState));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });

        SerializeEqualsTester.serializeEquals(state, deserializedState);
    }

    @Test
    public void testDeserializeCorruptCrc32Stream() throws Exception {

        Resource corruptStream = new ClassPathResource("/stateWithCorruptStream.zip");
        PackageState deserializedState = new PackageState();
        // this should emit a message at WARN level about a corrupt stream (if SLF4J has an impl hooked up)
        underTest.deserialize(deserializedState, new BufferedInputStream(corruptStream.getInputStream()));
        SerializeEqualsTester.serializeEquals(state, deserializedState);

        // TODO this test method should really be somewhere else; or the PackageStateSerializer should expose a
        // setFailOnChecksumMismatch(boolean) method.
        if (underTest instanceof AnnotationDrivenPackageStateSerializer) {
            ((AnnotationDrivenPackageStateSerializer)underTest).setFailOnChecksumMismatch(true);
            try {
                underTest.deserialize(deserializedState, new BufferedInputStream(corruptStream.getInputStream()));
                fail("Expected a StreamChecksumMismatch to be thrown.");
            } catch (StreamChecksumMismatch e) {
                // expected
            }
        } else {
            fail("Cannot test the failOnChecksumMismatch flag of AnnotationDrivenPackageStateSerializer.  This " +
                    "portion of the test should probably be in a unit test, or the flag should be moved to the" +
                    "PackageStateSerializer interface.");
        }

    }

    @Test
    public void testDeserializeUnknownStream() throws Exception {
        // Has an additional stream named 'unknown_stream' which cannot be made into an instance of StreamId.
        // Despite this, serialization should proceed without a problem.
        Resource unknownStream = new ClassPathResource("/stateWithUnknownStream.zip");
        PackageState deserializedState = new PackageState();
        underTest.deserialize(deserializedState, new BufferedInputStream(unknownStream.getInputStream()));

        // Assert that the expected state and the deserialized state are equal
        SerializeEqualsTester.serializeEquals(state, deserializedState);
    }

    /**
     * Attempt to deserialize version 1 serializations with the production PackageStateSerializer.
     *
     * @throws Exception
     */
    @Test
    public void testV1SerializationCompatibility() throws Exception {
        PackageState deserializedState = new PackageState();

        underTest.deserialize(deserializedState, StreamId.DOMAIN_OBJECTS, Resources.DOMAIN_OBJECTS.getInputStream());
        assertTrue(Objects.objects.isIsomorphicWith(deserializedState.getDomainObjectRDF()));
        assertTrue(deserializedState.getDomainObjectRDF().isIsomorphicWith(Objects.objects));

        underTest.deserialize(deserializedState, StreamId.APPLICATION_VERSION, Resources.APPLICATION_VERSION.getInputStream());
        assertEquals(Objects.appVersion, deserializedState.getCreationToolVersion());

        underTest.deserialize(deserializedState, StreamId.DOMAIN_PROFILE_LIST, Resources.DOMAIN_PROFILES.getInputStream());
        assertEquals(Objects.profiles, deserializedState.getDomainProfileIdList());

        underTest.deserialize(deserializedState, StreamId.PACKAGE_METADATA, Resources.PACKAGE_METADATA.getInputStream());
        assertEquals(Objects.metadata, deserializedState.getPackageMetadataList());

        underTest.deserialize(deserializedState, StreamId.PACKAGE_NAME, Resources.PACKAGE_NAME.getInputStream());
        assertEquals(Objects.name, deserializedState.getPackageName());

        underTest.deserialize(deserializedState, StreamId.PACKAGE_TREE, Resources.PACKAGE_TREE.getInputStream());
        assertTrue(Objects.tree.isIsomorphicWith(deserializedState.getPackageTree()));
        assertTrue(deserializedState.getPackageTree().isIsomorphicWith(Objects.tree));

        underTest.deserialize(deserializedState, StreamId.USER_SPECIFIED_PROPERTIES, Resources.USER_PROPS.getInputStream());
        assertEquals(Objects.userProps, deserializedState.getUserSpecifiedProperties());

        deserializedState = new PackageState();
        underTest.deserialize(deserializedState, Resources.FULL_STATE.getInputStream());
        SerializeEqualsTester.serializeEquals(state, deserializedState);
    }

    @Test
    @Ignore("Used to generate version 1 serializations for integration tests.")
    public void testScratchProduceV1Serializations() throws Exception {
        File baseDir = new File("/Users/esm/dc-workspace/dc-gh-fork/dcs-packaging-tool/dcs-packaging-tool-impl/src/test/resources/org/dataconservancy/packaging/tool/impl");

        // Using our production configuration, single streams will still be zipped. (e.g. the 'archive' flag is 'true')
        underTest.serialize(state, StreamId.APPLICATION_VERSION, new FileOutputStream(new File(baseDir, "appver-v1.ser")));
        underTest.serialize(state, StreamId.DOMAIN_OBJECTS, new FileOutputStream(new File(baseDir, "objects-v1.ser")));
        underTest.serialize(state, StreamId.DOMAIN_PROFILE_LIST, new FileOutputStream(new File(baseDir, "profiles-v1.ser")));
        underTest.serialize(state, StreamId.PACKAGE_METADATA, new FileOutputStream(new File(baseDir, "metadata-v1.ser")));
        underTest.serialize(state, StreamId.PACKAGE_NAME, new FileOutputStream(new File(baseDir, "name-v1.ser")));
        underTest.serialize(state, StreamId.PACKAGE_TREE, new FileOutputStream(new File(baseDir, "tree-v1.ser")));
        underTest.serialize(state, StreamId.USER_SPECIFIED_PROPERTIES, new FileOutputStream(new File(baseDir, "userprops-v1.ser")));

        underTest.serialize(state, new FileOutputStream(new File(baseDir, "fullstate-v1.ser")));
    }

    /**
     * Insures that unicode characters can be round-tripped through package state serialization using the platform
     * default encoding.
     *
     * @throws Exception
     */
    @Test
    public void testPlatformEncodingRoundTrip() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        String unicodeString = "S\u00EDSe\u00F1or!";

        // Package name with a unicode string
        state.setPackageName(unicodeString);

        // Package metadata with a unicode string
        LinkedHashMap<String, List<String>> packageMetadata = new LinkedHashMap<>();
        packageMetadata.put("foo", Collections.singletonList(unicodeString));
        state.setPackageMetadataList(packageMetadata);

        // A String user-defined property value with a unicode string
        Map<URI, List<Property>> userProps = new HashMap<>();
        PropertyType type = new PropertyType();
        type.setPropertyValueType(PropertyValueType.STRING);
        Property property = new Property(type);
        property.setStringValue(unicodeString);
        userProps.put(URI.create("http://a/uri"), Collections.singletonList(property));
        state.setUserSpecifiedProperties(userProps);


        // A IPM node with a unicode string
        Model ipm = ModelFactory.createDefaultModel();
        Statement s = ipm.createStatement(ipm.createResource("foo:s"), ipm.createProperty("foo:p"), ipm.createResource(unicodeString));
        ipm.add(s);
        state.setPackageTree(ipm);

        // A domain object with a unicode string
        Model objects = ModelFactory.createDefaultModel();
        s = objects.createStatement(objects.createResource("bar:s"), objects.createProperty("bar:p"), objects.createResource(unicodeString));
        objects.add(s);
        state.setDomainObjectRDF(objects);

        // Serialize the state.
        underTest.serialize(state, sink);

        // Deserialize it to a new PackageState instance
        PackageState deserializedPs = new PackageState();
        underTest.deserialize(deserializedPs, new ByteArrayInputStream(sink.toByteArray()));

        // Make sure our characters are there.
        assertEquals(unicodeString, deserializedPs.getPackageName());
        assertEquals(unicodeString, deserializedPs.getPackageMetadataList().get("foo").get(0));
        assertEquals(unicodeString, deserializedPs.getUserSpecifiedProperties().get(URI.create("http://a/uri")).get(0).getStringValue());

        Model objectsPrime = deserializedPs.getDomainObjectRDF();
        assertFalse(objects == objectsPrime);
        assertTrue(objectsPrime.listObjectsOfProperty(objectsPrime.createProperty("bar:p")).next().toString().endsWith(unicodeString));

        Model ipmPrime = deserializedPs.getPackageTree();
        assertFalse(ipm == ipmPrime);
        assertTrue(ipmPrime.listObjectsOfProperty(ipmPrime.createProperty("foo:p")).next().toString().endsWith(unicodeString));
    }

    /**
     * As configured in production, the {@link AnnotationDrivenPackageStateSerializer} should be encoding characters
     * using UTF-8, no matter what the platform default is.
     *
     * @throws Exception
     */
    @Test
    public void testUtf8Encoding() throws Exception {
        PackageState deserializedState = null;
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        String unicodeString = "S\u00EDSe\u00F1or!";
        byte[] unicodeBytes = unicodeString.getBytes(Charset.forName("UTF-8"));

        // We will not configure this to archive, so that the serialized stream will
        // not be placed in a zip entry; this makes searching through the sink for a
        // byte sequence more robust.

        // TODO this test method should really be somewhere else; or the PackageStateSerializer should expose a setArchive(boolean) method.
        if (!(underTest instanceof AnnotationDrivenPackageStateSerializer)) {
            fail("Expected an instance of AnnotationDrivenPackageStateSerializer");
        }
        ((AnnotationDrivenPackageStateSerializer)underTest).setArchive(false);

        // Package name with a unicode string
        state.setPackageName(unicodeString);

        underTest.serialize(state, StreamId.PACKAGE_NAME, sink);
        assertTrue(contains(unicodeBytes, sink));
        deserializedState = new PackageState();
        underTest.deserialize(deserializedState, StreamId.PACKAGE_NAME, new ByteArrayInputStream(sink.toByteArray()));
        assertEquals(unicodeString, deserializedState.getPackageName());

        sink.reset();
        deserializedState = null;

        // Package metadata with a unicode string
        LinkedHashMap<String, List<String>> packageMetadata = new LinkedHashMap<>();
        packageMetadata.put("foo", Collections.singletonList(unicodeString));
        state.setPackageMetadataList(packageMetadata);

        underTest.serialize(state, StreamId.PACKAGE_METADATA, sink);
        assertTrue(contains(unicodeBytes, sink));
        deserializedState = new PackageState();
        underTest.deserialize(deserializedState, StreamId.PACKAGE_METADATA, new ByteArrayInputStream(sink.toByteArray()));
        assertEquals(unicodeString, deserializedState.getPackageMetadataList().get("foo").get(0));

        sink.reset();
        deserializedState = null;

        // A String user-defined property value with a unicode string
        Map<URI, List<Property>> userProps = new HashMap<>();
        PropertyType type = new PropertyType();
        type.setPropertyValueType(PropertyValueType.STRING);
        Property property = new Property(type);
        property.setStringValue(unicodeString);
        userProps.put(URI.create("http://a/uri"), Collections.singletonList(property));
        state.setUserSpecifiedProperties(userProps);

        underTest.serialize(state, StreamId.USER_SPECIFIED_PROPERTIES, sink);
        assertTrue(contains(unicodeBytes, sink));
        deserializedState = new PackageState();
        underTest.deserialize(deserializedState, StreamId.USER_SPECIFIED_PROPERTIES, new ByteArrayInputStream(sink.toByteArray()));
        assertEquals(unicodeString, deserializedState.getUserSpecifiedProperties().get(URI.create("http://a/uri")).get(0).getStringValue());

        sink.reset();
        deserializedState = null;

        // A IPM node with a unicode string
        Model ipm = ModelFactory.createDefaultModel();
        Statement s = ipm.createStatement(ipm.createResource("foo:s"), ipm.createProperty("foo:p"), ipm.createResource(unicodeString));
        ipm.add(s);
        state.setPackageTree(ipm);

        underTest.serialize(state, StreamId.PACKAGE_TREE, sink);
        assertTrue(contains(unicodeBytes, sink));
        deserializedState = new PackageState();
        underTest.deserialize(deserializedState, StreamId.PACKAGE_TREE, new ByteArrayInputStream(sink.toByteArray()));
        assertTrue(deserializedState.getPackageTree().listObjectsOfProperty(ResourceFactory.createProperty("foo:p")).next().toString().endsWith(unicodeString));

        sink.reset();
        deserializedState = null;

        // A domain object with a unicode string
        Model objects = ModelFactory.createDefaultModel();
        s = objects.createStatement(objects.createResource("bar:s"), objects.createProperty("bar:p"), objects.createResource(unicodeString));
        objects.add(s);
        state.setDomainObjectRDF(objects);

        underTest.serialize(state, StreamId.DOMAIN_OBJECTS, sink);
        assertTrue(contains(unicodeBytes, sink));
        deserializedState = new PackageState();
        underTest.deserialize(deserializedState, StreamId.DOMAIN_OBJECTS, new ByteArrayInputStream(sink.toByteArray()));
        assertTrue(deserializedState.getDomainObjectRDF().listObjectsOfProperty(ResourceFactory.createProperty("bar:p")).next().toString().endsWith(unicodeString));

        sink.reset();
        deserializedState = null;
    }

    private boolean contains(byte[] candidates, ByteArrayOutputStream sink) {
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
                } else {
                    continue OUTER;
                }
            }
        }

        return false;
    }
}
