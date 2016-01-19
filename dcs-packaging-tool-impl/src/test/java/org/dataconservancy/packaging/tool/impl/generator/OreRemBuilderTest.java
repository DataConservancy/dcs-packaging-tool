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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.dataconservancy.packaging.tool.impl.SimpleURIGenerator;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.impl.generator.mocks.FunctionalAssemblerMock;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_ORE;

public class OreRemBuilderTest {

    private URIGenerator uriGen = new SimpleURIGenerator();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void clearTempFolder() {
        folder.delete();
    }

    @Test
    public void basicTreeTest() throws Exception {
        Node root = new Node(uriGen.generateNodeURI());
        Node child1 = new Node(uriGen.generateNodeURI());
        Node child2 = new Node(uriGen.generateNodeURI());
        Node leaf = new Node(uriGen.generateNodeURI());

        root.addChild(child1);
        root.addChild(child2);
        child1.addChild(leaf);

        OreReMBuilder serializer = new OreReMBuilder();

        PackageModelBuilderState state = new PackageModelBuilderState();
        state.assembler = new FunctionalAssemblerMock(folder.getRoot());
        state.manifest = ModelFactory.createDefaultModel();
        state.tree = root;
        state.params = new PackageGenerationParameters();

        serializer.init(state);
        root.walk(node -> {

            state.domainObjectSerializationLocations.put(node.getIdentifier(),
                                                         URI.create(node
                                                                 .getIdentifier()
                                                                 .toString()
                                                                 + "#ser"));
        });

        root.walk(node -> {
            serializer.visitNode(node, state);
        });

        serializer.finish(state);

        String rem = getRemAsString();

        /* Make sure the rem uses the null relative URI */
        assertTrue(rem.contains("<>"));

        Model deserialized = ModelFactory.createDefaultModel();
        deserialized.read(IOUtils.toInputStream(rem), "", "TURTLE");

        List<URI> aggregatedResources =
                deserialized
                        .listObjectsOfProperty(deserialized.getProperty(NS_ORE
                                + "aggregates"))
                        .mapWith(o -> URI.create(o.asResource().getURI()))
                        .toList();

        aggregatedResources
                .forEach(uri -> assertTrue(state.domainObjectSerializationLocations
                        .values().contains(uri)));
        state.domainObjectSerializationLocations.values()
                .forEach(uri -> assertTrue(aggregatedResources.contains(uri)));
    }

    @Test
    public void ignoreTest() throws Exception {
        Node root = new Node(uriGen.generateNodeURI());
        Node child1 = new Node(uriGen.generateNodeURI());
        Node child2 = new Node(uriGen.generateNodeURI());
        Node leaf = new Node(uriGen.generateNodeURI());

        root.addChild(child1);
        root.addChild(child2);
        child1.addChild(leaf);

        child2.setIgnored(true);
        leaf.setIgnored(true);

        OreReMBuilder serializer = new OreReMBuilder();

        PackageModelBuilderState state = new PackageModelBuilderState();
        state.assembler = new FunctionalAssemblerMock(folder.getRoot());
        state.manifest = ModelFactory.createDefaultModel();
        state.tree = root;
        state.params = new PackageGenerationParameters();

        serializer.init(state);
        root.walk(node -> {
            if (!node.isIgnored()) {
                state.domainObjectSerializationLocations.put(node
                        .getIdentifier(), URI.create(node.getIdentifier()
                        .toString() + "#ser"));
            }
        });

        root.walk(node -> {
            if(!node.isIgnored()) {
                serializer.visitNode(node, state);
            }
        });

        serializer.finish(state);

        String rem = getRemAsString();

        Model deserialized = ModelFactory.createDefaultModel();
        deserialized.read(IOUtils.toInputStream(rem), "", "TURTLE");

        List<URI> aggregatedResources =
                deserialized
                        .listObjectsOfProperty(deserialized.getProperty(NS_ORE
                                + "aggregates"))
                        .mapWith(o -> URI.create(o.asResource().getURI()))
                        .toList();

        aggregatedResources
                .forEach(uri -> assertTrue(state.domainObjectSerializationLocations
                        .values().contains(uri)));
        state.domainObjectSerializationLocations.values()
                .forEach(uri -> assertTrue(aggregatedResources.contains(uri)));
    }

    private String getRemAsString() throws IOException {
        List<Path> paths =
                Files.walk(folder.getRoot().toPath())
                        .filter(p -> p.toString().endsWith(".ttl"))
                        .collect(Collectors.toList());
        assertEquals(1, paths.size());

        String content;
        try (InputStream in = new FileInputStream(paths.get(0).toFile())) {
            content = IOUtils.toString(in);
        }

        return content;
    }
}
