package org.dataconservancy.packaging.tool.profile;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStoreImpl;
import org.dataconservancy.packaging.tool.impl.DomainProfileServiceImpl;
import org.dataconservancy.packaging.tool.impl.SimpleURIGenerator;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.impl.support.IpmTreeFactory;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DcBoProfileServiceTest {
    private DomainProfileServiceImpl service;
    private DomainProfileObjectStoreImpl store;
    private DcBoIpmFactory boIpmFactory;
    private DcsBOProfile profile;
    private Model model;
    private IpmTreeFactory treeFactory;

    @Before
    public void setup() {
        model = ModelFactory.createDefaultModel();

        URIGenerator urigen = new SimpleURIGenerator();

        store = new DomainProfileObjectStoreImpl(model, urigen);
        service = new DomainProfileServiceImpl(store, urigen);
        boIpmFactory = new DcBoIpmFactory();
        profile = new DcsBOProfile();
        treeFactory = new IpmTreeFactory();
    }

    @Test
    public void testSingleDirectoryAssignment() {
        Node root = treeFactory.createSingleDirectoryTree(null);
        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.getProjectNodeType().getIdentifier(), root.getNodeType().getIdentifier());

        checkValidTree(root);
    }

    @Test
    public void testSingleFileAssignment() {
        Node root = treeFactory.createSingleFileTree(null);
        boolean success = service.assignNodeTypes(profile, root);

        assertFalse(success);
    }

    @Test
    public void testTwoDirectoryAssignment() {
        Node root = treeFactory.createTwoDirectoryTree(null, null);
        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.getProjectNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        assertEquals(profile.getCollectionNodeType().getIdentifier(), root.getChildren().get(0).getNodeType().getIdentifier());

        checkValidTree(root);
    }

    @Test
    public void testSingleDirectoryFileAssigment() {
        Node root = treeFactory.createSingleDirectoryFileTree(null, null);
        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.getProjectNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        assertEquals(profile.getFileNodeType().getIdentifier(), root.getChildren().get(0).getNodeType().getIdentifier());

        checkValidTree(root);
    }

    @Test
    public void testSmallTreeAssignment() {
        Node root = treeFactory.createTree(4, 2, true);
        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());

        checkValidTree(root);
    }

    @Test
    public void testSubCollectionToDataItemNoChildrenTransform() {
        Node root = boIpmFactory.createSubCollectionTree();
        root.walk(store::updateObject);

        Node subCollection = root.getChildren().get(0);
        service.transformNode(subCollection, profile.getCollectionToDataItemNoChildrenTransform());

        assertEquals(profile.getDataItemNodeType().getIdentifier(), subCollection.getNodeType().getIdentifier());
        assertEquals(root.getIdentifier(), subCollection.getParent().getIdentifier());

        assertTrue(service.validateTree(root));
    }

    @Test
    @Ignore("Unignore once transforms are updated")
    public void testDataItemToSubCollectionTransformWithFile() {
        Node root = boIpmFactory.createSmallLinearTree();
        root.walk(store::updateObject);

        Node dataItem = root.getChildren().get(0).getChildren().get(0);
        service.transformNode(dataItem, profile.getDataItemToCollectionTransform());

        assertEquals(profile.getCollectionNodeType().getIdentifier(), dataItem.getNodeType().getIdentifier());
        assertEquals(root.getChildren().get(0).getIdentifier(), dataItem.getParent().getIdentifier());

        assertEquals(profile.getMetadataNodeType(), dataItem.getChildren().get(0).getNodeType().getIdentifier());
        assertTrue(service.validateTree(root));
    }

    /**
     * Test validating properties on a Project
     */
    @Test
    public void testValidateProjectProperties() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getProjectNodeType());
        node.setDomainObject(URI.create("domain:object"));

        // Missing species, title, and weight
        assertFalse(service.validateProperties(node, profile.getProjectNodeType()));

        Property title = new Property(profile.getHasTitle());
        title.setStringValue("title");
        service.addProperty(node, title);

        Property description = new Property(profile.getHasDescription());
        description.setStringValue("description");
        service.addProperty(node, description);

        Property startDate = new Property(profile.getHasStartDate());
        startDate.setDateTimeValue(new DateTime());
        service.addProperty(node, startDate);

        Property hasAlottedStorage = new Property(profile.getHasAlottedStorage());
        hasAlottedStorage.setLongValue(1000L);
        service.addProperty(node, hasAlottedStorage);

        Property hasUsedStorage = new Property(profile.getHasUsedStorage());
        hasUsedStorage.setLongValue(50L);
        service.addProperty(node, hasUsedStorage);

        Property hasPublisher = new Property(profile.getHasPublisher());
        hasPublisher.setStringValue("publisher");
        service.addProperty(node, hasPublisher);

        assertTrue(service.validateProperties(node, profile.getProjectNodeType()));
    }

    /**
     * Test validating properties on a Project
     */
    @Test
    public void testValidateCollectionProperties() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getCollectionNodeType());
        node.setDomainObject(URI.create("domain:object"));

        // Missing species, title, and weight
        assertFalse(service.validateProperties(node, profile.getCollectionNodeType()));

        Property title = new Property(profile.getHasTitle());
        title.setStringValue("title");
        service.addProperty(node, title);

        Property description = new Property(profile.getHasDescription());
        description.setStringValue("description");
        service.addProperty(node, description);

        Property createDate = new Property(profile.getHasCreateDate());
        createDate.setDateTimeValue(new DateTime());
        service.addProperty(node, createDate);

        assertTrue(service.validateProperties(node, profile.getCollectionNodeType()));
    }

    /**
     * Test validating properties on a DataItem
     */
    @Test
    public void testValidateDataItemProperties() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getDataItemNodeType());
        node.setDomainObject(URI.create("domain:object"));

        // Missing species, title, and weight
        assertFalse(service.validateProperties(node, profile.getDataItemNodeType()));

        Property title = new Property(profile.getHasTitle());
        title.setStringValue("title");
        service.addProperty(node, title);

        Property description = new Property(profile.getHasDescription());
        description.setStringValue("description");
        service.addProperty(node, description);

        Property createDate = new Property(profile.getHasCreateDate());
        createDate.setDateTimeValue(new DateTime());
        service.addProperty(node, createDate);

        Property modifiedDate = new Property(profile.getHasModifiedDate());
        modifiedDate.setDateTimeValue(new DateTime());
        service.addProperty(node, modifiedDate);

        assertTrue(service.validateProperties(node, profile.getDataItemNodeType()));
    }

    /**
     * Test validating properties on a File
     */
    @Test
    public void testValidateFileProperties() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getFileNodeType());
        node.setDomainObject(URI.create("domain:object"));

        // Missing species, title, and weight
        assertFalse(service.validateProperties(node, profile.getFileNodeType()));

        Property title = new Property(profile.getHasTitle());
        title.setStringValue("title");
        service.addProperty(node, title);

        Property description = new Property(profile.getHasDescription());
        description.setStringValue("description");
        service.addProperty(node, description);

        Property createDate = new Property(profile.getHasCreateDate());
        createDate.setDateTimeValue(new DateTime());
        service.addProperty(node, createDate);

        Property modifiedDate = new Property(profile.getHasModifiedDate());
        modifiedDate.setDateTimeValue(new DateTime());
        service.addProperty(node, modifiedDate);

        Property format = new Property(profile.getHasFormat());
        format.setStringValue("application/xml");
        service.addProperty(node, format);

        Property size = new Property(profile.getHasSize());
        size.setLongValue(1234L);
        service.addProperty(node, size);

        assertTrue(service.validateProperties(node, profile.getFileNodeType()));
    }

    /**
     * Test validating properties on a Metadata
     */
    @Test
    public void testValidateMetadataProperties() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getMetadataNodeType());
        node.setDomainObject(URI.create("domain:object"));

        // Missing species, title, and weight
        assertFalse(service.validateProperties(node, profile.getMetadataNodeType()));

        Property title = new Property(profile.getHasTitle());
        title.setStringValue("title");
        service.addProperty(node, title);

        Property description = new Property(profile.getHasDescription());
        description.setStringValue("description");
        service.addProperty(node, description);

        Property createDate = new Property(profile.getHasCreateDate());
        createDate.setDateTimeValue(new DateTime());
        service.addProperty(node, createDate);

        Property modifiedDate = new Property(profile.getHasModifiedDate());
        modifiedDate.setDateTimeValue(new DateTime());
        service.addProperty(node, modifiedDate);

        Property format = new Property(profile.getHasFormat());
        format.setStringValue("application/xml");
        service.addProperty(node, format);

        Property size = new Property(profile.getHasSize());
        size.setLongValue(1234L);
        service.addProperty(node, size);

        assertTrue(service.validateProperties(node, profile.getMetadataNodeType()));
    }

    // Update objects and then validate the tree
    private void checkValidTree(Node node) {
        node.walk(store::updateObject);
        assertTrue(service.validateTree(node));
    }
}
