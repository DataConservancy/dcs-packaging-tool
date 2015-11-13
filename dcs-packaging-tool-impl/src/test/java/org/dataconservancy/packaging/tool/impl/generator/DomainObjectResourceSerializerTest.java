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

import java.nio.file.Paths;

import org.apache.jena.rdf.model.ModelFactory;

import org.junit.Test;

import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

public class DomainObjectResourceSerializerTest {

    @Test
    public void singleRootNodeSerializationTest() throws Exception {
        PackageState pkgState = new PackageState();

        /* Create the IPM tree */
        Node treeNode = new Node(URI.create("http://example.org/1"));
        treeNode.setDomainObject(URI
                .create("http://example.org/TestDomainObject"));

        /* Set the FileInfo as the directory TestDomainObjects */
        treeNode.setFileInfo(new FileInfo(Paths.get(getClass()
                .getResource("/TestDomainObjects/1.ttl").toURI()).getParent()));
        System.out.println(treeNode.getFileInfo());

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

        /* FINALLY, we can start */
        DomainObjectResourceSerializer serializer =
                new DomainObjectResourceSerializer();
        serializer.init(state);

    }
}
