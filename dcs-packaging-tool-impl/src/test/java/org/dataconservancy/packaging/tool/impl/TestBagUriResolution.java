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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ser.AbstractSerializationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestBagUriResolution {

    private IpmRdfTransformService transform;

    private Node packageTree;

    private Path baseDir = Paths.get("/foo/bar");



    @Before
    public void setUp() throws Exception {
        DomainProfileStore dps = new DomainProfileStoreJenaImpl(ModelFactory.createDefaultModel(), ModelFactory.createDefaultModel());
        transform = new IpmRdfTransformService();
        transform.setDomainProfileStore(dps);
        packageTree = transform.transformToNode(AbstractSerializationTest.TestObjects.packageTreeRDFWithBagUris);
        assertNotNull(packageTree);
    }

    @After
    public void tearDown() throws Exception {
        Model m = transform.transformToRDF(packageTree);
        m.write(System.out);
    }

    @Test
    public void testScratch() throws Exception {

        Map<URI, Path> resolvedPaths = new HashMap<>();

        // Resolve FileInfo bag URIs to a Path, and then set the new location (a file URI) on the FileInfo
        packageTree.stream()
                .filter(n -> n.getFileInfo() != null && UriUtility.isBagUri(n.getFileInfo().getLocation()))
                .forEach(n -> {
                            URI bagUri = n.getFileInfo().getLocation();
                            Path resolvedPath = UriUtility.resolveBagUri(baseDir, bagUri);
                            n.getFileInfo().setLocation(resolvedPath.toUri());
                            resolvedPaths.put(bagUri, resolvedPath);
                        }
                );

        assertTrue(resolvedPaths.containsKey(new URI("bag://my-bag/1/0")));
        assertEquals(Paths.get(baseDir.toString(), "/my-bag/1/0"), resolvedPaths.get(new URI("bag://my-bag/1/0")));
        assertTrue(resolvedPaths.containsKey(new URI("bag://my-bag/2/0")));
        assertEquals(Paths.get(baseDir.toString(), "/my-bag/2/0"), resolvedPaths.get(new URI("bag://my-bag/2/0")));
        assertTrue(resolvedPaths.containsKey(new URI("bag://my-bag/2/1")));
        assertEquals(Paths.get(baseDir.toString(), "/my-bag/2/1"), resolvedPaths.get(new URI("bag://my-bag/2/1")));
    }
}
