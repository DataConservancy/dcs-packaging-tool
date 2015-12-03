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
package org.dataconservancy.packaging.tool.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PackageStateTest {

    @Test
    public void PackageStateVerifier() throws URISyntaxException {
        EqualsVerifier
                .forClass(PackageState.class).allFieldsShouldBeUsed()
                .withPrefabValues(Node.class, new Node(new URI("uri:foo")), new Node(new URI("uri:bar")))
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.REFERENCE_EQUALITY)
                .verify();
    }

    @Test
    public void testCloneWithModels() throws Exception {
        Model tree = ModelFactory.createDefaultModel();
        Model objects = ModelFactory.createDefaultModel();
        tree.add(createResource("subject"), createProperty("pred"), createResource("obj"));
        objects.add(createResource("anothersubj"), createProperty("anotherpred"), createResource("anotherobj"));

        PackageState one = new PackageState();
        one.setPackageTree(tree);
        one.setDomainObjectRDF(objects);

        PackageState clone = (PackageState) one.clone();

        assertFalse(one == clone);

        assertFalse(one.getPackageTree() == clone.getPackageTree());
        assertTrue(one.getPackageTree().isIsomorphicWith(clone.getPackageTree()));

        assertFalse(one.getDomainObjectRDF() == clone.getDomainObjectRDF());
        assertTrue(one.getDomainObjectRDF().isIsomorphicWith(clone.getDomainObjectRDF()));

        tree.add(createResource("foo"), createProperty("bar"), createResource("baz"));
        objects.add(createResource("foo"), createProperty("bar"), createResource("baz"));
        assertFalse(one.getPackageTree().isIsomorphicWith(clone.getPackageTree()));
        assertFalse(one.getDomainObjectRDF().isIsomorphicWith(clone.getDomainObjectRDF()));
    }
}
