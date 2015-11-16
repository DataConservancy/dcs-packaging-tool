/*
 * Copyright 2014 Johns Hopkins University
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
import java.io.OutputStream;

import java.net.URI;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.dataconservancy.packaging.tool.impl.generator.mocks.FunctionalAssemblerMock;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class PackageStateBuilderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void clearTempFolder() {
        folder.delete();
    }

    @Test
    public void domainObjectMappingTest() throws Exception {
        String oldURI = "http://example.org/old";
        String newURI = "http://example.org/new";

        PackageModelBuilderState state = new PackageModelBuilderState();

        PackageStateSerializer serializer = mock(PackageStateSerializer.class);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ((PackageState) args[0]).getDomainObjectRDF()
                        .write((OutputStream) args[1], "TURTLE");
                return null;
            }
        }).when(serializer).serialize(any(PackageState.class),
                                      any(OutputStream.class));

        PackageStateBuilder builder = new PackageStateBuilder();
        builder.setPackageStateSerializer(serializer);

        Model model = ModelFactory.createDefaultModel();

        model.add(model.createResource(oldURI),
                  model.createProperty("http://example.org/property"),
                  "whatever");

        state.pkgState = new PackageState();
        state.pkgState.setDomainObjectRDF(model);
        state.assembler = new FunctionalAssemblerMock(folder.getRoot());

        state.renamedResources = new HashMap<>();
        state.renamedResources.put(oldURI, newURI);

        state.tree = new Node(URI.create("http://example.org/blah"));

        builder.finish(state);

        /*
         * Inherently, this verifies that a package state is written to the
         * assembler at all
         */
        String serialized = getPkgStateAsString();

        /* We decided that packageState was a jena model, so deserialize it */
        Model serializedModel = ModelFactory.createDefaultModel();
        serializedModel.read(IOUtils.toInputStream(serialized), null, "TURTLE");

        /* Verify that the model was mutated according to the map */
        assertTrue(serializedModel.listSubjects()
                .mapWith(res -> res.toString()).toSet().contains(newURI));
        assertFalse(serializedModel.listSubjects()
                .mapWith(res -> res.toString()).toSet().contains(oldURI));
    }

    private String getPkgStateAsString() throws IOException {
        List<Path> paths =
                Files.walk(folder.getRoot().toPath())
                        .filter(p -> p.toString().endsWith(".bin"))
                        .collect(Collectors.toList());
        assertEquals(1, paths.size());

        String content = null;
        try (InputStream in = new FileInputStream(paths.get(0).toFile())) {
            content = IOUtils.toString(in);
        }

        return content;
    }
}
