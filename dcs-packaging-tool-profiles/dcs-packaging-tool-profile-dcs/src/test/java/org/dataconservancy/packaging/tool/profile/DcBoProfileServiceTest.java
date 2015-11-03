package org.dataconservancy.packaging.tool.profile;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStoreImpl;
import org.dataconservancy.packaging.tool.impl.DomainProfileServiceImpl;
import org.dataconservancy.packaging.tool.impl.SimpleURIGenerator;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.impl.support.IpmTreeFactory;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DcBoProfileServiceTest {
    private DomainProfileServiceImpl service;
    private DomainProfileObjectStoreImpl store;
    private DcBoIpmFactory boIpmFactory;
    private DcsBOProfile profile;
    private IpmTreeFactory treeFactory;

    @Before
    public void setup() {
        Model model = ModelFactory.createDefaultModel();

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
        assertEquals(profile.getCollectionNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        assertEquals(profile.getMetadataNodeType().getIdentifier(), root.getChildren().get(0).getNodeType().getIdentifier());
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
        service.transformNode(subCollection, profile.getCollectionToDataItemTransform());

        assertEquals(profile.getDataItemNodeType().getIdentifier(), subCollection.getNodeType().getIdentifier());
        assertEquals(root.getIdentifier(), subCollection.getParent().getIdentifier());

        assertTrue(service.validateTree(root));
    }

    @Test
    public void testSubCollectionToDataItemWithChildrenTransform() {
        treeFactory.setNodeTypeSetter((node, depth) -> {
            switch (depth) {
                case 0:
                    node.setNodeType(profile.getCollectionNodeType());
                    break;
                case 1:
                    node.setNodeType(profile.getCollectionNodeType());
                    break;
                case 2:
                    node.setNodeType(profile.getMetadataNodeType());
                    break;
            }

        });
        Node root = treeFactory.createTree(3, 1, false);

        root.walk(store::updateObject);

        Node subCollection = root.getChildren().get(0);
        URI metadataFileId = subCollection.getChildren().get(0).getIdentifier();
        service.transformNode(subCollection, profile.getCollectionToDataItemTransform());

        assertEquals(profile.getCollectionNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        assertEquals(1, root.getChildren().size());

        Node dataItem = root.getChildren().get(0);

        assertEquals(profile.getDataItemNodeType().getIdentifier(), dataItem.getNodeType().getIdentifier());
        assertEquals(1, dataItem.getChildren().size());

        assertEquals(profile.getMetadataNodeType().getIdentifier(), dataItem.getChildren().get(0).getNodeType().getIdentifier());
        assertEquals(metadataFileId, dataItem.getChildren().get(0).getIdentifier());

        treeFactory.setNodeTypeSetter(null);
    }

    @Test
    public void testDataItemToSubCollectionTransformWithFile() {
        Node root = boIpmFactory.createSmallLinearTree();
        root.walk(store::updateObject);

        Node dataItem = root.getChildren().get(0).getChildren().get(0);
        service.transformNode(dataItem, profile.getDataItemToCollectionTransform());

        assertEquals(profile.getCollectionNodeType().getIdentifier(), dataItem.getNodeType().getIdentifier());
        assertEquals(root.getChildren().get(0).getIdentifier(), dataItem.getParent().getIdentifier());

        assertEquals(profile.getMetadataNodeType().getIdentifier(), dataItem.getChildren().get(0).getNodeType().getIdentifier());
        assertTrue(service.validateTree(root));
    }

    @Test
    public void testCollectionMetadataFileToDataItemAndDataFile() {
        Node root = treeFactory.createSingleDirectoryFileTree(profile.getCollectionNodeType(), profile.getMetadataNodeType());
        root.walk(store::updateObject);

        Node metadata = root.getChildren().get(0);
        service.transformNode(metadata, profile.getCollectionMetadataFileToDataFileTransform());

        assertEquals(profile.getCollectionNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        Node dataItem = root.getChildren().get(0);

        assertEquals(profile.getDataItemNodeType().getIdentifier(), dataItem.getNodeType().getIdentifier());
        assertNotNull(dataItem.getChildren());

        assertEquals(profile.getFileNodeType().getIdentifier(), dataItem.getChildren().get(0).getNodeType().getIdentifier());
        assertEquals(metadata.getIdentifier(), dataItem.getChildren().get(0).getIdentifier());
    }

    @Test
    @Ignore
    public void testDataFileToCollectionMetadataFile() {
        treeFactory.setNodeTypeSetter((node, depth) -> {
            switch (depth) {
                case 0:
                    node.setNodeType(profile.getCollectionNodeType());
                    break;
                case 1:
                    node.setNodeType(profile.getDataItemNodeType());
                    break;
                case 2:
                    node.setNodeType(profile.getFileNodeType());
                    break;
            }

        });
        Node root = treeFactory.createTree(3, 1, false);

        root.walk(store::updateObject);

        Node dataItem = root.getChildren().get(0);
        URI dataFileId = dataItem.getChildren().get(0).getIdentifier();
        service.transformNode(dataItem, profile.getDataFileToCollectionMetadataFileTransform());

        assertEquals(profile.getCollectionNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        assertEquals(1, root.getChildren().size());

        Node metadataFile = root.getChildren().get(0);

        assertEquals(profile.getMetadataNodeType().getIdentifier(), metadataFile.getNodeType().getIdentifier());
        assertNull(dataItem.getChildren());

        assertEquals(dataFileId, metadataFile.getIdentifier());

        treeFactory.setNodeTypeSetter(null);
    }

    @Test
    public void testCollectionToProjectTransform() {
        treeFactory.setNodeTypeSetter((node, depth) -> {
            switch (depth) {
                case 0:
                    node.setNodeType(profile.getCollectionNodeType());
                    break;
                case 1:
                    node.setNodeType(profile.getDataItemNodeType());
                    break;
                case 2:
                    node.setNodeType(profile.getFileNodeType());
                    break;
            }

        });
        Node root = treeFactory.createTree(3, 1, false);
        root.walk(store::updateObject);

        service.transformNode(root, profile.getCollectionToProjectTransform());

        assertEquals(profile.getProjectNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        assertEquals(1, root.getChildren().size());

        Node collection = root.getChildren().get(0);

        assertEquals(profile.getCollectionNodeType().getIdentifier(), collection.getNodeType().getIdentifier());
        assertEquals(1, collection.getChildren().size());

        Node metadataFile = collection.getChildren().get(0);
        assertEquals(profile.getMetadataNodeType().getIdentifier(), metadataFile.getNodeType().getIdentifier());

        treeFactory.setNodeTypeSetter(null);
    }

    @Test
    public void testProjectToCollectionTransform() {
        Node root = treeFactory.createTwoDirectoryTree(profile.getProjectNodeType(), profile.getCollectionNodeType());
        root.walk(store::updateObject);

        service.transformNode(root, profile.getProjectToCollectionTransform());

        assertEquals(profile.getCollectionNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        Node collection = root.getChildren().get(0);

        assertEquals(profile.getCollectionNodeType().getIdentifier(), collection.getNodeType().getIdentifier());
    }

    @Test
    public void testMetadataToFileTransform() {
        Node root = treeFactory.createSingleDirectoryFileTree(profile.getDataItemNodeType(), profile.getMetadataNodeType());
        root.walk(store::updateObject);

        Node metadataFile = root.getChildren().get(0);

        service.transformNode(metadataFile, profile.getMetadataToFileTransform());

        assertEquals(profile.getDataItemNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        Node dataFile = root.getChildren().get(0);

        assertEquals(profile.getFileNodeType().getIdentifier(), dataFile.getNodeType().getIdentifier());
    }

    @Test
    public void testFileToMetadataTransform() {
        Node root = treeFactory.createSingleDirectoryFileTree(profile.getDataItemNodeType(), profile.getFileNodeType());
        root.walk(store::updateObject);

        Node file = root.getChildren().get(0);

        service.transformNode(file, profile.getFileToMetadataTransform());

        assertEquals(profile.getDataItemNodeType().getIdentifier(), root.getNodeType().getIdentifier());
        Node metadataFile = root.getChildren().get(0);

        assertEquals(profile.getMetadataNodeType().getIdentifier(), metadataFile.getNodeType().getIdentifier());
    }

    @Test
    public void testRootCollectionToDataItemTransform() {
        Node root = treeFactory.createSingleDirectoryFileTree(profile.getCollectionNodeType(), profile.getMetadataNodeType());
        root.walk(store::updateObject);

        service.transformNode(root, profile.getRootCollectionToDataItemTransform());

        assertEquals(profile.getDataItemNodeType().getIdentifier(), root.getNodeType().getIdentifier());

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

    @Test
    public void testAvailableTransforms() {
        //Should be able to transform to a data item or a project.
        Node collectionRoot = boIpmFactory.createSingleCollectionTree();
        collectionRoot.setDomainObject(URI.create("domain:object"));
        List<NodeTransform> transformList = service.getNodeTransforms(collectionRoot);

        assertNotNull(transformList);
        assertEquals(2, transformList.size());
        assertTrue(transformList.contains(profile.getRootCollectionToDataItemTransform()));
        assertTrue(transformList.contains(profile.getCollectionToProjectTransform()));

        //Should be able to transform to a collection or a data item.
        Node projectRoot = boIpmFactory.createSingleProjectTree();
        projectRoot.setDomainObject(URI.create(UUID.randomUUID().toString()));
        transformList = service.getNodeTransforms(projectRoot);

    }

    /**
     * Test validating properties on a Collection
     */
    @Test
    public void testValidateCollectionProperties() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getCollectionNodeType());
        node.setDomainObject(URI.create("domain:object"));

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

    /**
     * Test that the creator property is correctly validated.
     */
    @Test
    public void testComplexPropertyValidates() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getCollectionNodeType());
        node.setDomainObject(URI.create("domain:object"));

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

        Property creator = new Property(profile.getHasCreator());

        Property phone = new Property(profile.getPhone());
        phone.setStringValue("8886515908");

        creator.setComplexValue(Collections.singletonList(phone));
        service.addProperty(node, creator);

        assertFalse(service.validateProperties(node, profile.getCollectionNodeType()));

        Property name = new Property(profile.getName());
        name.setStringValue("name");

        creator.setComplexValue(Arrays.asList(name, phone));

        // Must remove existing incorrect person property
        service.removeProperty(node, profile.getHasCreator());
        service.addProperty(node, creator);

        assertTrue(service.validateProperties(node, profile.getCollectionNodeType()));
    }

    // Update objects and then validate the tree
    private void checkValidTree(Node node) {
        node.walk(store::updateObject);
        assertTrue(service.validateTree(node));
    }
}
