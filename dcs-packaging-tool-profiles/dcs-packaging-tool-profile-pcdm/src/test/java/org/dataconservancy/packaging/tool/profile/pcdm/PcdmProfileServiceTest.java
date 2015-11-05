/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.profile.pcdm;

import java.net.URI;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStoreImpl;
import org.dataconservancy.packaging.tool.impl.DomainProfileServiceImpl;
import org.dataconservancy.packaging.tool.impl.SimpleURIGenerator;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.impl.support.IpmTreeFactory;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.profile.pcdm.PCDMProfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class PcdmProfileServiceTest {

    private DomainProfileServiceImpl service;

    private DomainProfileObjectStoreImpl store;

    private PCDMProfile profile;

    private Model model;

    private IpmTreeFactory treeFactory;

    @Before
    public void setup() {
        model = ModelFactory.createDefaultModel();

        URIGenerator urigen = new SimpleURIGenerator();

        store = new DomainProfileObjectStoreImpl(model, urigen);
        service = new DomainProfileServiceImpl(store, urigen);
        profile = new PCDMProfile();
        treeFactory = new IpmTreeFactory();
    }

    @Test
    public void testSingleDirectoryAssignment() {
        Node root = treeFactory.createSingleDirectoryTree(null);

        assertTrue(service.assignNodeTypes(profile, root));
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.administrativeSet.getIdentifier(), root
                .getNodeType().getIdentifier());

        checkValidTree(root);
    }

    @Test
    public void singleNodeTransformToCollectionTest() {
        Node root = treeFactory.createSingleDirectoryTree(null);
        assertTrue(service.assignNodeTypes(profile, root));

        service.transformNode(root, profile.administrativeSet_to_collection);
        assertEquals(profile.collection.getIdentifier(), root.getNodeType()
                .getIdentifier());

        checkValidTree(root);

        /* Back to administrativeSet */
        service.transformNode(root, profile.collection_to_administrativeSet);
        assertEquals(profile.administrativeSet.getIdentifier(), root
                .getNodeType().getIdentifier());
        checkValidTree(root);
    }

    @Test
    public void singleNodeTransformToObjectTest() {
        Node root = treeFactory.createSingleDirectoryTree(null);
        assertTrue(service.assignNodeTypes(profile, root));

        /* Sequence of transforms to get to Object */
        service.transformNode(root, profile.administrativeSet_to_collection);
        service.transformNode(root, profile.collection_to_object);
        assertEquals(profile.object.getIdentifier(), root.getNodeType()
                .getIdentifier());

        checkValidTree(root);
        /* Back to administrativeSet */
        service.transformNode(root, profile.object_to_collection);
        service.transformNode(root, profile.collection_to_administrativeSet);
        assertEquals(profile.administrativeSet.getIdentifier(), root
                .getNodeType().getIdentifier());
        checkValidTree(root);
    }

    @Test
    public void singleFileTest() {
        Node root = treeFactory.createSingleFileTree(null);
        assertTrue(service.assignNodeTypes(profile, root));
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.file.getIdentifier(), root.getNodeType()
                .getIdentifier());

        checkValidTree(root);
    }

    @Test
    public void twoDirectoryTest() {
        Node root = treeFactory.createTwoDirectoryTree(null, null);
        assertTrue(service.assignNodeTypes(profile, root));

        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.administrativeSet.getIdentifier(), root
                .getNodeType().getIdentifier());
        assertEquals(profile.collection.getIdentifier(), root.getChildren()
                .get(0).getNodeType().getIdentifier());

        checkValidTree(root);
    }

    @Test
    public void twoDirectoryTransformTopNodeTest() {
        Node root = treeFactory.createTwoDirectoryTree(null, null);
        assertTrue(service.assignNodeTypes(profile, root));

        service.transformNode(root, profile.administrativeSet_to_collection);
        checkValidTree(root);
    }

    @Test
    public void twoDirectoryTransformBottomNodeTest() {
        Node root = treeFactory.createTwoDirectoryTree(null, null);
        assertTrue(service.assignNodeTypes(profile, root));
        Node child = root.getChildren().get(0);

        service.transformNode(child, profile.collection_to_object);
        checkValidTree(root);
    }

    @Test
    public void twoDirectoryTransformBothNodeTest() {
        Node root = treeFactory.createTwoDirectoryTree(null, null);
        assertTrue(service.assignNodeTypes(profile, root));
        Node child = root.getChildren().get(0);

        service.transformNode(root, profile.administrativeSet_to_collection);
        checkValidTree(root);

        service.transformNode(child, profile.collection_to_object);
        checkValidTree(root);
    }

    @Test
    public void directoryAndFileTest() {
        Node root = treeFactory.createSingleDirectoryFileTree(null, null);
        assertTrue(service.assignNodeTypes(profile, root));
        Node child = root.getChildren().get(0);

        assertEquals(profile.object.getIdentifier(), root.getNodeType()
                .getIdentifier());
        assertEquals(profile.file.getIdentifier(), child.getNodeType()
                .getIdentifier());
        checkValidTree(root);
    }

    @Test
    public void relatedObjectTest() {

        String RELATED_RESOURCE_URI = "http://example.org/related_object";

        Node root =
                treeFactory
                        .createSingleDirectoryTree(profile.administrativeSet);

        assertTrue(service.assignNodeTypes(profile, root));
        checkValidTree(root);

        /* Make sure all is valid after adding property */
        Property rel = new Property(profile.hasRelatedObject);
        rel.setUriValue(URI.create(RELATED_RESOURCE_URI));

        service.addProperty(root, rel);
        checkValidTree(root);

        assertTrue(model
                .listStatements(model.createResource(root.getDomainObject()
                                        .toString()),
                                model.createProperty(profile.hasRelatedObject
                                        .getDomainPredicate().toString()),
                                model.createResource(RELATED_RESOURCE_URI))
                .hasNext());

        /* Make sure property survives transforms */
        service.transformNode(root, profile.administrativeSet_to_collection);
        checkValidTree(root);

        assertTrue(model
                .listStatements(model.createResource(root.getDomainObject()
                                        .toString()),
                                model.createProperty(profile.hasRelatedObject
                                        .getDomainPredicate().toString()),
                                model.createResource(RELATED_RESOURCE_URI))
                .hasNext());

        service.transformNode(root, profile.collection_to_object);
        checkValidTree(root);

        assertTrue(model
                .listStatements(model.createResource(root.getDomainObject()
                                        .toString()),
                                model.createProperty(profile.hasRelatedObject
                                        .getDomainPredicate().toString()),
                                model.createResource(RELATED_RESOURCE_URI))
                .hasNext());

    }

    //Update objects and then validate the tree
    private void checkValidTree(Node node) {
        node.walk(store::updateObject);
        assertTrue(service.validateTree(node));
    }
}
