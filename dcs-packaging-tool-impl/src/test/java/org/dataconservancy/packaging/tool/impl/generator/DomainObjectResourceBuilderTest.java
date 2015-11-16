/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.packaging.tool.impl.generator;

import java.io.InputStream;

import java.net.URI;
import java.net.URL;

import java.nio.file.Paths;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.dataconservancy.packaging.tool.impl.generator.mocks.FunctionalAssemblerMock;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class DomainObjectResourceBuilderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void clearTempFolder() {
        folder.delete();
    }

    /*
     * Verifies that init() re-maps URIs according to the URIs given by the
     * assembler
     */
    @Test
    public void singleNodeInitializationTest() throws Exception {

        PackageModelBuilderState state = bootstrap1();

        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        DomainObjectResourceBuilder serializer =
                new DomainObjectResourceBuilder();

        /* Domain object URIs are not file URIs */
        state.domainObjects
                .listSubjects()
                .filterKeep(s -> !s.isAnon())
                .forEachRemaining(s -> assertFalse(s.getURI()
                        .startsWith("file")));

        /* Should use PackageAssembler to reserve resources and rename URIs */
        serializer.init(state);

        /* Now they should be file URIs */
        state.domainObjects
                .listSubjects()
                .filterKeep(s -> !s.isAnon())
                .forEachRemaining(s -> assertTrue(s.getURI().startsWith("file")));

        /*
         * Now, for sanity sake, verify that the number of non-anonymous
         * subjects is not zero
         */
        AtomicInteger count = new AtomicInteger(0);
        state.domainObjects.listSubjects().filterKeep(s -> !s.isAnon())
                .forEachRemaining(s -> count.incrementAndGet());

        assertTrue(count.get() > 0);
    }

    /*
     * Verifies that the serialized domain object has been written, and has the
     * same number of triples as the original domain object, and uses the null
     * relative URI.
     */
    @Test
    public void serializedFileTest() throws Exception {
        PackageModelBuilderState state = bootstrap1();

        int COUNT = state.domainObjects.listStatements().toSet().size();

        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        DomainObjectResourceBuilder serializer =
                new DomainObjectResourceBuilder();

        /* Init and walk the tree */
        serializer.init(state);
        state.tree.walk(node -> serializer.visitNode(node, state));

        try (InputStream in = state.tree.getIdentifier().toURL().openStream()) {
            String serialized = IOUtils.toString(in);

            /*
             * Quick 'n dirty way to verify that we use the null URI in
             * serialization
             */
            assertTrue(serialized.contains("<>"));
            assertTrue(serialized.contains("<#"));
            assertFalse(serialized.contains("<file:"));

            /* Now load and compare counts */
            Model fromFile = ModelFactory.createDefaultModel();
            fromFile.read(IOUtils.toInputStream(serialized), "", "TURTLE");
            assertEquals(COUNT, fromFile.listStatements().toSet().size());
        }
    }

    /*
     * Verifies that finalization process passes in the normal case, but fails
     * if there are extra, unserialized triples.
     */
    @Test
    public void finalizationTest() throws Exception {
        PackageModelBuilderState state = bootstrap1();

        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        DomainObjectResourceBuilder serializer =
                new DomainObjectResourceBuilder();

        /* This should work OK */
        serializer.init(state);
        state.tree.walk(node -> serializer.visitNode(node, state));
        serializer.finish(state);

        /* This should cause an exception, left over triples */
        state.domainObjects.add(state.domainObjects
                .createResource("http://example.org/x"), state.domainObjects
                .createProperty("http://example.org/y"), "z");
        try {
            serializer.finish(state);
            Assert.fail("Did not catch left-behind triples");
        } catch (Exception e) {
            /* good! */
        }
    }

    /*
     * Verify that the default case, where the domain object is associated with
     * a File IPM node and does not explicitly link to content, that the domain
     * object URI resolves to the content, that there is content present, that
     * the domain object serialization is at a separate URI, and that the
     * subject of the triples in the domain object serialization is the
     * resolvable content URI and *not* the domain object serialization URI
     */
    @Test
    public void defaultBinaryContentTest() throws Exception {
        PackageModelBuilderState state = bootstrap2();

        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        DomainObjectResourceBuilder serializer =
                new DomainObjectResourceBuilder();

        /* Init and walk the tree */
        serializer.init(state);
        state.tree.walk(node -> serializer.visitNode(node, state));
        serializer.finish(state);

        Node fileNode = state.tree.getChildren().get(0);

        assertNotEquals(fileNode.getIdentifier(), fileNode.getDomainObject());
        assertEquals(fileNode.getDomainObject(), fileNode.getFileInfo()
                .getLocation());
        assertNotEquals(fileNode.getDomainObject(),
                        getClass().getResource("/TestDomainObjects/2/file.txt")
                                .toURI());

        /* Make sure that the domain object URI resolves to the content */
        try (InputStream in = fileNode.getDomainObject().toURL().openStream()) {
            String content = IOUtils.toString(in);

            String knownContent =
                    IOUtils.toString(getClass()
                            .getResourceAsStream("/TestDomainObjects/2/file.txt"));

            assertEquals(knownContent, content);
        }

        /*
         * Make sure the domain object serialization deserializes, and has the
         * domain object URI as a subject
         */
        try (InputStream in = fileNode.getIdentifier().toURL().openStream()) {

            Model deserialized = ModelFactory.createDefaultModel();

            String str = IOUtils.toString(in);

            /* Make sure it reads fine */
            deserialized.read(IOUtils.toInputStream(str), null, "TURTLE");

            assertTrue(str.contains(fileNode.getDomainObject().toString()));
        }

    }

    /*
     * Verifies that if a domain object (in an IPM node corresponding to a File)
     * explicitly links to binary content via some property, then the link
     * resolves to the binary content and the domain object URI is the same as
     * the domain object serialization URI.
     */

    @Test
    public void linkedBinaryContentTest() throws Exception {
        PackageModelBuilderState state = bootstrap2();

        Property fileRelationProperty =
                state.domainObjects.createProperty("http://example.org/A#src");

        Node fileNode = state.tree.getChildren().get(0);

        /*
         * Now add a triple to the File domain object explicitly linking to the
         * content
         */
        state.domainObjects.add(state.domainObjects.getResource(fileNode
                                        .getDomainObject().toString()),
                                fileRelationProperty,
                                state.domainObjects
                                        .createResource(fileNode.getFileInfo()
                                                .getLocation().toString()));

        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        DomainObjectResourceBuilder serializer =
                new DomainObjectResourceBuilder();

        /* Init and walk the tree */
        serializer.init(state);
        state.tree.walk(node -> serializer.visitNode(node, state));
        serializer.finish(state);

        assertEquals(fileNode.getIdentifier(), fileNode.getDomainObject());

        try (InputStream in = fileNode.getIdentifier().toURL().openStream()) {

            Model deserialized = ModelFactory.createDefaultModel();

            String str = IOUtils.toString(in);

            /* Make sure it reads fine */
            deserialized.read(IOUtils.toInputStream(str), null, "TURTLE");
            Set<RDFNode> fileLinks =
                    deserialized.listObjectsOfProperty(fileRelationProperty)
                            .toSet();

            assertEquals(1, fileLinks.size());
            String linkedFileURI =
                    fileLinks.iterator().next().asResource().toString();

            assertNotEquals(getClass()
                                    .getResource("/TestDomainObjects/2/file.txt")
                                    .toURI().toString(),
                            linkedFileURI);

            /* Make sure that the content link URI resolves to the content */
            try (InputStream fc = new URL(linkedFileURI).openStream()) {
                String content = IOUtils.toString(fc);

                String knownContent =
                        IOUtils.toString(getClass()
                                .getResourceAsStream("/TestDomainObjects/2/file.txt"));

                assertEquals(knownContent, content);
            }

        }
    }

    /* Tests a two-node tree */
    @Test
    public void treeTest() throws Exception {
        PackageModelBuilderState state = bootstrap2();

        int COUNT = state.domainObjects.listStatements().toSet().size();

        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        DomainObjectResourceBuilder serializer =
                new DomainObjectResourceBuilder();

        /* Init and walk the tree */
        serializer.init(state);
        state.tree.walk(node -> serializer.visitNode(node, state));
        serializer.finish(state);

        Model deserialized = ModelFactory.createDefaultModel();

        try (InputStream in = state.tree.getIdentifier().toURL().openStream()) {
            deserialized.read(in,
                              state.tree.getIdentifier().toString(),
                              "TURTLE");
        }
        try (InputStream in =
                state.tree.getChildren().get(0).getIdentifier().toURL()
                        .openStream()) {
            deserialized.read(in, state.tree.getChildren().get(0)
                    .getIdentifier().toString(), "TURTLE");
        }

        assertEquals(COUNT, deserialized.listStatements().toSet().size());

    }

    @Test
    public void ignoreTest() throws Exception {
        PackageModelBuilderState state = bootstrap2();

        int COUNT = state.domainObjects.listStatements().toSet().size();

        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        DomainObjectResourceBuilder serializer =
                new DomainObjectResourceBuilder();

        Node fileNode = state.tree.getChildren().get(0);
        fileNode.setIgnored(true);

        /* Init and walk the tree */
        serializer.init(state);
        state.tree.walk(node -> serializer.visitNode(node, state));
        serializer.finish(state);

        Model deserialized = ModelFactory.createDefaultModel();

        try (InputStream in = state.tree.getIdentifier().toURL().openStream()) {
            String content = IOUtils.toString(in);
            deserialized.read(IOUtils.toInputStream(content), state.tree
                    .getIdentifier().toString(), "TURTLE");

            assertTrue(deserialized.listStatements().toSet().size() < COUNT);

            /* Make sure no references to the file (which is ignored) */
            assertFalse(content.contains(".txt"));
        }

    }

    /* Bootstrap a single complex domain object */
    private PackageModelBuilderState bootstrap1() throws Exception {
        PackageState pkgState = new PackageState();

        /* Create the IPM tree */
        Node treeNode = new Node(URI.create("http://example.org/1"));
        treeNode.setDomainObject(URI
                .create("http://example.org/TestDomainObject"));

        /* Set the FileInfo as the directory TestDomainObjects */
        treeNode.setFileInfo(new FileInfo(Paths.get(getClass()
                .getResource("/TestDomainObjects/1.ttl").toURI()).getParent()));

        pkgState.setDomainObjectRDF(ModelFactory.createDefaultModel());

        try (InputStream in =
                this.getClass().getResourceAsStream("/TestDomainObjects/1.ttl")) {
            pkgState.getDomainObjectRDF().read(in, null, "TTL");
        }

        PackageModelBuilderState state = new PackageModelBuilderState();
        state.domainObjects =
                ModelFactory.createModelForGraph(pkgState.getDomainObjectRDF()
                        .getGraph());
        state.tree = treeNode;
        state.pkgState = pkgState;
        state.renamedResources = new HashMap<>();

        return state;
    }

    /* Bootstrap a tree of two domain objects */
    private PackageModelBuilderState bootstrap2() throws Exception {
        PackageState pkgState = new PackageState();

        /* Create the IPM tree */
        Node treeNode = new Node(URI.create("http://example.org/2"));
        treeNode.setDomainObject(URI
                .create("http://example.org/TestDomainObject/Directory1"));
        treeNode.setFileInfo(new FileInfo(Paths.get(getClass()
                .getResource("/TestDomainObjects/2/2.ttl").toURI()).getParent()));

        Node child = new Node(URI.create("http://example.org/2/file"));
        treeNode.setChildren(Arrays.asList(child));
        child.setParent(treeNode);
        child.setDomainObject(URI
                .create("http://example.org/TestDomainObject/File1"));
        child.setFileInfo(new FileInfo(Paths.get(getClass()
                .getResource("/TestDomainObjects/2/file.txt").toURI())));

        pkgState.setDomainObjectRDF(ModelFactory.createDefaultModel());

        try (InputStream in =
                this.getClass()
                        .getResourceAsStream("/TestDomainObjects/2/2.ttl")) {
            pkgState.getDomainObjectRDF().read(in, null, "TTL");
        }

        try (InputStream in =
                this.getClass()
                        .getResourceAsStream("/TestDomainObjects/2/file.txt.ttl")) {
            pkgState.getDomainObjectRDF().read(in, null, "TTL");
        }

        PackageModelBuilderState state = new PackageModelBuilderState();
        state.domainObjects =
                ModelFactory.createModelForGraph(pkgState.getDomainObjectRDF()
                        .getGraph());
        state.tree = treeNode;
        state.pkgState = pkgState;
        state.renamedResources = new HashMap<>();

        return state;
    }
}
