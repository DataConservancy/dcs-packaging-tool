package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
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
    private Model model;

    @Before
    public void setup() {
        model = ModelFactory.createDefaultModel();
        
        URIGenerator urigen = new SimpleURIGenerator();
        
        store = new DomainProfileObjectStoreImpl(model, urigen);
        service = new DomainProfileServiceImpl(store, urigen);
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

    private void update_object_and_get_transforms(Node node) {
        node.walk(store::updateObject);
        node.walk(service::getNodeTransforms);
    }

    /**
     * Test that checking for node transforms does not throw exceptions
     */
    @Test
    public void testGetNodeTransformsNoException() {
        ipmfact.createSimpleTree().walk(this::update_object_and_get_transforms);
        ipmfact.createSimpleTree2().walk(this::update_object_and_get_transforms);
        ipmfact.createSingleDirectoryTree().walk(this::update_object_and_get_transforms);
        ipmfact.createTwoDirectoryTree().walk(this::update_object_and_get_transforms);
        ipmfact.createCompleteTree(3, 2).walk(this::update_object_and_get_transforms);
    }

    @Test
    public void testGetNodeTransforms() {
        Node root = ipmfact.createSimpleTree();
        Node barn = root.getChildren().get(0);
        Node cow = barn.getChildren().get(0);
        Node media = cow.getChildren().get(0);

        root.walk(store::updateObject);

        // No transform because cow has media child
        assertEquals(0, service.getNodeTransforms(cow).size());

        // Remove child, now has transform available
        cow.setChildren(null);
        model.removeAll();
        root.walk(store::updateObject);

        List<NodeTransform> result = service.getNodeTransforms(cow);
        assertEquals(1, result.size());
        assertEquals(profile.getCowToStockpileTransform(), result.get(0));

        // No transform for barn because has cow child
        assertEquals(0, service.getNodeTransforms(barn).size());

        // Remove child, now has transform available
        barn.setChildren(null);
        model.removeAll();
        root.walk(store::updateObject);

        result = service.getNodeTransforms(barn);
        assertEquals(1, result.size());
        assertEquals(profile.getBarnNoChildToFarmTransform(), result.get(0));

        // Add media child to barn, now has different transform available
        barn.addChild(media);
        model.removeAll();
        root.walk(store::updateObject);

        result = service.getNodeTransforms(barn);
        assertEquals(1, result.size());
        assertEquals(profile.getBarnMediaChildToFarmTransform(), result.get(0));
    }

    @Test
    public void testBarnMediaChildToFarmNodeTransform() {
        Node root = ipmfact.createSimpleTree2();
        Node barn = root.getChildren().stream().filter(n -> n.getNodeType() == profile.getBarnNodeType()).findFirst()
                .get();
        Node barn_file = barn.getChildren().get(0);

        root.walk(store::updateObject);
        
        List<NodeTransform> trs = service.getNodeTransforms(barn);
        assertEquals(1, trs.size());
        assertEquals(profile.getBarnMediaChildToFarmTransform(), trs.get(0));
        
        // Transform barn -> farm
        
        service.transformNode(barn, profile.getBarnMediaChildToFarmTransform());

        assertEquals(profile.getFarmNodeType().getIdentifier(), barn.getNodeType().getIdentifier()); 
        assertEquals(root.getIdentifier(), barn.getParent().getIdentifier());
        assertEquals(1, barn.getChildren().size());
        assertEquals(barn_file.getIdentifier(), barn.getChildren().get(0).getIdentifier());

        assertTrue(service.validateTree(root));
    }
    
    @Test
    public void testCowToStockpileNodeTransform() {
        Node root = ipmfact.createSimpleTree();
        Node barn = root.getChildren().get(0);
        Node cow = barn.getChildren().get(0);

        // Remove cow child so cow can be transformed.
        cow.setChildren(null);        
        root.walk(store::updateObject);
        
        // Transform cow -> stockpile
        
        service.transformNode(cow, profile.getCowToStockpileTransform());

        assertEquals(profile.getStockpileNodeType().getIdentifier(), cow.getNodeType().getIdentifier());        
        assertEquals(barn.getIdentifier(), cow.getParent().getIdentifier());

        assertTrue(service.validateTree(root));
    }
    
    @Test
    public void testTroughToCowNodeTransform() {
        Node root = ipmfact.createTwoDirectoryTree2();
        Node trough = root.getChildren().get(0);
        
        root.walk(store::updateObject);
        
        service.transformNode(trough, profile.getTroughToCowTransform());
        
        assertEquals(profile.getCowNodeType().getIdentifier(), trough.getNodeType().getIdentifier());        
        
        // Check that barn parent is inserted.
        
        Node parent = trough.getParent();
        
        assertEquals(profile.getBarnNodeType().getIdentifier(), parent.getNodeType().getIdentifier());        
        
        assertTrue(root.getChildren().contains(parent));
        assertEquals(root, parent.getParent());
        assertTrue(parent.getChildren().contains(trough));
        
        assertTrue(service.validateTree(root)); 
    }
    
    @Test
    public void testMoveMediaFromCowToFarmTransform() {
        Node root = ipmfact.createSimpleTree();
        Node barn = root.getChildren().get(0);
        Node cow = barn.getChildren().get(0);
        Node media = cow.getChildren().get(0);
        
        root.walk(store::updateObject);

        // Should move media to barn and remove cow
        
        service.transformNode(media, profile.getMoveMediaFromCowToBarnTransform());
        
        assertEquals(profile.getMediaNodeType().getIdentifier(), media.getNodeType().getIdentifier());
        assertEquals(barn.getIdentifier(), media.getParent().getIdentifier()); 
        assertTrue(barn.getChildren().contains(media));
        assertFalse(barn.getChildren().contains(cow));
    
        assertTrue(service.validateTree(root));         
    }
    
}
