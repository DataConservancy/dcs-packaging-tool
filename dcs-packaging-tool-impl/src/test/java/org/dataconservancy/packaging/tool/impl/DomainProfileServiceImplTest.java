package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the DomainProfileServiceImpl against the FarmDomainProfile.
 * 
 * Methods that create objects and add and remove properties from them are not
 * tested here because they are actually implemented by the
 * DomainProfileObjectStoreImpl.
 */
public class DomainProfileServiceImplTest {
    private DomainProfileServiceImpl service;
    private DomainProfileObjectStoreImpl store;
    private FarmIpmFactory ipmfact;
    private FarmDomainProfile profile;

    @Before
    public void setup() {
        Model model = ModelFactory.createDefaultModel();

        store = new DomainProfileObjectStoreImpl(model);
        service = new DomainProfileServiceImpl(store);
        ipmfact = new FarmIpmFactory();
        profile = ipmfact.getProfile();
    }

    /**
     * A single node must get assigned to Farm because it is the only type which
     * may not have a parent.
     */
    @Test
    public void testAssignSingleDirectory() {
        Node root = ipmfact.createSingleDirectoryTree();

        root.walk(Node::clearNodeTypes);

        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.getFarmNodeType().getIdentifier(), root.getNodeType().getIdentifier());

        check_valid_tree(root);
    }

    /**
     * A single file has no valid assignment.
     */
    @Test
    public void testAssignSingleFile() {
        Node root = ipmfact.createInvalidSingleFileTree();

        root.walk(Node::clearNodeTypes);

        boolean success = service.assignNodeTypes(profile, root);

        assertFalse(success);
        assertNull(root.getDomainObject());
    }

    /**
     * A tree consisting of one directory containing another directory.
     */
    @Test
    public void testAssignTwoDirectory() {
        Node root = ipmfact.createTwoDirectoryTree();

        root.walk(Node::clearNodeTypes);

        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);
        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.getFarmNodeType().getIdentifier(), root.getNodeType().getIdentifier());

        Node child = root.getChildren().get(0);
        assertNotNull(child.getNodeType());
        assertNotNull(child.getDomainObject());

        check_valid_tree(root);
    }

    /**
     * A tree consisting of three directories and a file.
     */
    @Test
    public void testAssignSimple() {
        Node root = ipmfact.createSimpleTree();

        root.walk(Node::clearNodeTypes);

        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);

        root.walk(n -> {
            assertNotNull(n.getDomainObject());
            assertNotNull(n.getNodeType());
        });

        check_valid_tree(root);
    }

    /**
     * A tree consisting of two directories and two files.
     */
    @Test
    public void testAssignSimple2() {
        Node root = ipmfact.createSimpleTree2();

        root.walk(Node::clearNodeTypes);

        boolean success = service.assignNodeTypes(profile, root);

        assertTrue(success);

        root.walk(n -> {
            assertNotNull(n.getDomainObject());
            assertNotNull(n.getNodeType());
        });

        check_valid_tree(root);
    }

    /**
     * Test a large tree.
     */
    @Test
    public void testAssignLargeTree() {
        Node root = ipmfact.createCompleteTree(8, 4);

        root.walk(Node::clearNodeTypes);

        boolean success = service.assignNodeTypes(profile, root);

        // System.err.println(store);

        assertTrue(success);

        // Only valid assignment is meda for leaf
        // Inner nodes can be farm, barn, or cow.

        root.walk(n -> {
            assertNotNull(n.getDomainObject());
            assertNotNull(n.getNodeType());

            NodeType type = n.getNodeType();

            if (n.isLeaf()) {
                assertEquals(profile.getMediaNodeType().getIdentifier(), type.getIdentifier());
            } else {
                assertTrue(type.getIdentifier().equals(profile.getFarmNodeType().getIdentifier())
                        || type.getIdentifier().equals(profile.getCowNodeType().getIdentifier())
                        || type.getIdentifier().equals(profile.getBarnNodeType().getIdentifier()));
            }
        });

        check_valid_tree(root);
    }

    /**
     * Test validating properties on a Cow in the Farm domain profile.
     */
    @Test
    public void testValidateSimpleProperties() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getCowNodeType());
        node.setDomainObject(URI.create("domain:object"));

        // Missing species, title, and weight
        assertFalse(service.validateProperties(node, profile.getCowNodeType()));

        Property species = new Property(profile.getSpeciesPropertyType());
        species.setStringValue("robocow");
        service.addProperty(node, species);

        Property title = new Property(profile.getTitlePropertyType());
        title.setStringValue("Good cow");
        service.addProperty(node, title);

        // Missing weight
        assertFalse(service.validateProperties(node, profile.getCowNodeType()));

        Property weight = new Property(profile.getWeightPropertyType());
        weight.setLongValue(100);
        service.addProperty(node, weight);

        assertTrue(service.validateProperties(node, profile.getCowNodeType()));
    }

    /**
     * Test validating properties on a Farm in the Farm domain profile which has
     * a complex person property.
     */
    @Test
    public void testValidateComplexProperty() {
        Node node = new Node(URI.create("test:node"));
        node.setNodeType(profile.getFarmNodeType());
        node.setDomainObject(URI.create("domain:object"));

        // Missing title and person
        assertFalse(service.validateProperties(node, profile.getFarmNodeType()));

        Property title = new Property(profile.getTitlePropertyType());
        title.setStringValue("Jim's farm.");

        service.addProperty(node, title);

        // Missing person
        assertFalse(service.validateProperties(node, profile.getFarmNodeType()));

        Property person1 = new Property(profile.getFarmerPropertyType());

        Property name1 = new Property(profile.getNamePropertyType());
        name1.setStringValue("Farmer Jim");

        person1.setComplexValue(Arrays.asList(name1));

        service.addProperty(node, person1);

        // Missing mbox on person1
        assertFalse(service.validateProperties(node, profile.getFarmNodeType()));

        Property mbox1 = new Property(profile.getMboxPropertyType());
        mbox1.setStringValue("mooooo@moo");

        person1.setComplexValue(Arrays.asList(name1, mbox1));

        // Must remove existing incorrect person property
        service.removeProperty(node, profile.getFarmerPropertyType());
        service.addProperty(node, person1);

        
        System.err.println(store);
        
        assertTrue(service.validateProperties(node, profile.getFarmNodeType()));
    }

    @Test
    public void testValidateValidTree() {
        check_valid_tree(ipmfact.createSingleDirectoryTree());
        check_valid_tree(ipmfact.createTwoDirectoryTree());
        check_valid_tree(ipmfact.createSimpleTree());
        check_valid_tree(ipmfact.createSimpleTree2());
        check_valid_tree(ipmfact.createCompleteTree(3, 3));
    }

    @Test
    public void testValidateInvalidTree() {
        assertFalse(service.validateTree(ipmfact.createInvalidSingleFileTree()));
    }

    // Tree must have node types to be valid.
    @Test
    public void testValidateTreeWithoutType() {
        Node root = ipmfact.createSimpleTree();

        check_valid_tree(root);

        root.setNodeType(null);

        assertFalse(service.validateTree(root));
    }
    
    // Tree must have domain objects to be valid
    @Test
    public void testValidateTreeWithoutDomainObject() {
        Node root = ipmfact.createSimpleTree();

        check_valid_tree(root);

        root.setDomainObject(null);

        assertFalse(service.validateTree(root));
    }

    // Update objects and then validate the tree
    private void check_valid_tree(Node node) {
        node.walk(store::updateObject);

        assertTrue(service.validateTree(node));
    }
}
