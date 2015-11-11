package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DomainProfileObjectStoreImpl by using a tree of nodes in FarmIpmTree
 * which uses the FarmDomainProfile.
 */
public class DomainProfileObjectStoreImplTest {
    private Model model;
    private DomainProfileObjectStoreImpl store;
    private FarmDomainProfile profile;
    private FarmIpmFactory ipmfactory;

    @Before
    public void setup() {
        URIGenerator urigen = new SimpleURIGenerator();
        
        model = ModelFactory.createDefaultModel();
        store = new DomainProfileObjectStoreImpl(model, urigen);
        ipmfactory = new FarmIpmFactory();
        profile = ipmfactory.getProfile();
    }

    /**
     * Update object tests walks various trees updating objects and checking the
     * model for correct triples.
     */
    @Test
    public void testUpdateObject() {
        test_update_object(ipmfactory.createSingleDirectoryTree());
        test_update_object(ipmfactory.createTwoDirectoryTree());
        test_update_object(ipmfactory.createSimpleTree());
        test_update_object(ipmfactory.createSimpleTree2());
        test_update_object(ipmfactory.createSimpleTree3());
    }
    
    /**
     * Check updating existing objects with new types.
     */
    @Test
    public void testUpdateObjectAfterChangingTypes() {
        Node root = ipmfactory.createSimpleTree();
        
        test_update_object(root);
        
        Node barn = root.getChildren().get(0);
        Node cow = barn.getChildren().get(0);
        Node media = cow.getChildren().get(0);
        
        // Barn can have a stockpile
        cow.setNodeType(profile.getStockpileNodeType());

        // Stockpile can have feed.
        media.setNodeType(profile.getFeedNodeType());

        StructuralRelation occ = profile.getOccupantRelation();
        StructuralRelation part = profile.getPartRelation();
        
        assertTrue(store.hasRelationship(cow.getDomainObject(), occ.getHasParentPredicate(), barn.getDomainObject()));
        assertFalse(store.hasRelationship(cow.getDomainObject(), part.getHasParentPredicate(), barn.getDomainObject()));

        test_update_object(root);
        
        // Check that old relations no longer exist
        
        assertFalse(store.hasRelationship(cow.getDomainObject(), occ.getHasParentPredicate(), barn.getDomainObject()));
        assertTrue(store.hasRelationship(cow.getDomainObject(), part.getHasParentPredicate(), barn.getDomainObject()));
        
        // Has new domain types
        profile.getStockpileNodeType().getDomainTypes().forEach(u -> assertTrue(store.hasRelationship(cow.getDomainObject(), URI.create(RDF.type.getURI()), u)));
        
        // Does not have old domain types
        profile.getCowNodeType().getDomainTypes().forEach(u -> assertFalse(store.hasRelationship(cow.getDomainObject(), URI.create(RDF.type.getURI()), u)));
    }
    
    // Check that deleting all the objects of a tree removes all statements in the model. 
    @Test
    public void testDeleteObject() {
        Node root = ipmfactory.createSimpleTree();        
        root.walk(store::updateObject);
        
        assertTrue(model.size() > 0);
        
        root.walk(store::deleteObject);
        
        assertEquals(0, model.size());
    }
    
    @Test
    public void testMoveObject() {
        Node root = ipmfactory.createSimpleTree();
        root.walk(store::updateObject);
        
        Node barn = root.getChildren().get(0);
        Node cow = barn.getChildren().get(0);
        Node media = cow.getChildren().get(0);

        // Move media from cow to barn
        
        store.moveObject(media, null, barn);
        
        assertEquals(media.getNodeType().getIdentifier(), profile.getMediaNodeType().getIdentifier());
        assertEquals(barn.getIdentifier(), media.getParent().getIdentifier());
        assertTrue(barn.getChildren().contains(media));
        assertFalse(cow.getChildren().contains(media));
        
        // Move cow to root and turn it into a farm
        
        store.moveObject(cow, profile.getFarmNodeType(), root);
        
        assertEquals(cow.getNodeType().getIdentifier(), profile.getFarmNodeType().getIdentifier());
        assertEquals(root.getIdentifier(), cow.getParent().getIdentifier());
        assertTrue(root.getChildren().contains(cow));
        assertFalse(barn.getChildren().contains(cow));
    }
    
    @Test
    public void testAddStringProperty() {
        URI test_object = URI.create("test:moo");

        PropertyType type = profile.getTitlePropertyType();
        Property val = new Property(type);
        val.setStringValue("Jim the cow");

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
    }

    /**
     * Test that adding the same simple property twice only results in one
     * instance of the property being added.
     */
    @Test
    public void testAddSameSimpleProperty() {
        URI test_object = URI.create("test:moo");

        PropertyType type = profile.getTitlePropertyType();
        Property val = new Property(type);
        val.setStringValue("Jim the cow");

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
        assertEquals(1, store.getProperties(test_object, type).size());

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
        assertEquals(1, store.getProperties(test_object, type).size());
    }

    @Test
    public void testAddLongProperty() {
        URI test_object = URI.create("test:jimfoot");

        PropertyType type = profile.getSizePropertyType();
        Property val = new Property(type);
        val.setLongValue(32);

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
    }

    @Test
    public void testAddDateTimeProperty() {
        URI test_object = URI.create("test:jimlunch");

        PropertyType type = profile.getCreatedPropertyType();
        Property val = new Property(type);
        val.setDateTimeValue(new DateTime(100000000));

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
    }

    @Test
    public void testAddAndRemoveComplexProperty() {
        URI test_object = URI.create("test:farm");

        PropertyType type = profile.getFarmerPropertyType();
        Property prop1 = new Property(type);

        {
            List<Property> subprops = new ArrayList<>();
            Property name_val = new Property(profile.getNamePropertyType());
            name_val.setStringValue("Jim Moocow Farmer");
            Property mbox_val = new Property(profile.getMboxPropertyType());
            mbox_val.setStringValue("moo@moo.moo");
            subprops.add(name_val);
            subprops.add(mbox_val);

            prop1.setComplexValue(subprops);
        }

        store.addProperty(test_object, prop1);
        assertTrue(has_property(test_object, prop1));

        // There may be multiple complex objects with same value
        
        store.addProperty(test_object, prop1);
        assertTrue(has_property(test_object, prop1));
        assertEquals(2, store.getProperties(test_object, type).size());
        

        Property prop2 = new Property(type);
        
        {
            List<Property> subvals = new ArrayList<>();
            Property name_val = new Property(profile.getNamePropertyType());
            name_val.setStringValue("Jeff OinkOink Farmer");
            Property mbox_val = new Property(profile.getMboxPropertyType());
            mbox_val.setStringValue("oink@oink.oink");
            subvals.add(name_val);
            subvals.add(mbox_val);

            prop2.setComplexValue(subvals);
        }
        
        // May be multiple complex objects with same type

        store.addProperty(test_object, prop2);

        assertTrue(has_property(test_object, prop2));
        assertEquals(3, store.getProperties(test_object, type).size());
        
        // Complex properties may be removed independently even if same value
        
        store.removeProperty(test_object, prop1);        
        assertTrue(has_property(test_object, prop1));
        assertTrue(has_property(test_object, prop2));
        assertEquals(2, store.getProperties(test_object, type).size());
        
        store.removeProperty(test_object, prop1);     
        assertTrue(has_property(test_object, prop2));
        assertEquals(1, store.getProperties(test_object, type).size());
        
        // Delete final property
        
        store.removeProperty(test_object, prop2);     
        assertEquals(0, store.getProperties(test_object, type).size());
        
        // Should be no triples left
        
        assertEquals(0, model.size());
        
        // Test Removing by type
        
        store.addProperty(test_object, prop1);
        store.addProperty(test_object, prop2);
        
        assertTrue(has_property(test_object, prop1));
        assertTrue(has_property(test_object, prop2));
        
        store.removeProperty(test_object, profile.getFarmerPropertyType());
        
        assertFalse(has_property(test_object, prop1));
        assertFalse(has_property(test_object, prop2));
        
        assertEquals(0, model.size());
    }

    @Test
    public void testRemoveSimplePropertyByValue() {
        URI test_object = URI.create("test:jimfoot");

        PropertyType type = profile.getSizePropertyType();
        Property val = new Property(type);
        val.setLongValue(32);

        store.addProperty(test_object, val);
        assertTrue(has_property(test_object, val));

        store.removeProperty(test_object, val);

        assertFalse(has_property(test_object, val));
    }

    @Test
    public void testRemoveSimplePropertyByType() {
        URI test_object = URI.create("test:jimfeet");

        PropertyType type = profile.getSizePropertyType();
        Property val1 = new Property(type);
        val1.setLongValue(32);
        Property val2 = new Property(type);
        val2.setLongValue(42);

        store.addProperty(test_object, val1);
        store.addProperty(test_object, val2);

        assertTrue(has_property(test_object, val1));
        assertTrue(has_property(test_object, val2));

        store.removeProperty(test_object, profile.getSizePropertyType());

        assertFalse(has_property(test_object, val1));
        assertFalse(has_property(test_object, val2));
    }

    private void test_update_object(Node node) {
        store.updateObject(node);

        check_properties(node.getDomainObject(), node.getNodeType());

        if (node.getSubNodeTypes() != null) {
            for (NodeType type : node.getSubNodeTypes()) {
                check_properties(node.getDomainObject(), type);
            }
        }

        check_parent_relations(node);

        if (node.isLeaf()) {
            return;
        }

        for (Node child : node.getChildren()) {
            test_update_object(child);
        }
    }

    private void check_properties(URI id, NodeType type) {
        // print_model();

        // Check RDF types

        if (type.getDomainTypes() != null) {
            for (URI dt : type.getDomainTypes()) {
                assertTrue(id + " has type " + dt, has_statement(id, RDF.type, dt));
            }
        }

        // Check default properties

        if (type.getDefaultPropertyValues() != null) {
            for (Property val : type.getDefaultPropertyValues()) {
                assertTrue(id + " has property " + val, has_property(id, val));
            }
        }

        // Check supplied properties exist

        if (type.getSuppliedProperties() != null) {
            type.getSuppliedProperties().forEach((pt, sp) -> assertTrue(id + " has supplied property " + sp,
                    !store.getProperties(id, pt).isEmpty()));
        }
    }

    // Check that relationships between node and parent domain objects meet
    // constraints of node.

    private void check_parent_relations(Node node) {
        Node parent_node = node.getParent();

        if (parent_node == null) {
            return;
        }

        NodeType parent_type = parent_node.getNodeType();

        URI id = node.getDomainObject();
        URI parent_id = parent_node.getDomainObject();

        if (node.getNodeType().getParentConstraints() != null && !node.getNodeType().getParentConstraints().isEmpty()) {
            for (NodeConstraint nc : node.getNodeType().getParentConstraints()) {
                if (nc.getNodeType() != null && nc.getNodeType().getIdentifier().equals(parent_type.getIdentifier())) {
                    StructuralRelation rel = nc.getStructuralRelation();

                    assertTrue(has_statement(id, rel.getHasParentPredicate(), parent_id));
                    assertTrue(has_statement(parent_id, rel.getHasChildPredicate(), id));

                    return;
                }
            }

            assertTrue("No constraint found for parent of node " + id, false);
        }
    }

    private boolean has_property(URI id, Property val) {
        return store.getProperties(id, val.getPropertyType()).contains(val);
    }

    private Resource as_resource(URI uri) {
        return model.createResource(uri.toString());
    }

    private org.apache.jena.rdf.model.Property as_property(URI uri) {
        return model.createProperty(uri.toString());
    }

    private Statement as_statement(URI subject, URI predicate, URI object) {
        return model.createStatement(as_resource(subject), as_property(predicate), as_resource(object));
    }

    private boolean has_statement(URI subject, URI predicate, URI object) {
        return model.contains(as_statement(subject, predicate, object));
    }

    private boolean has_statement(URI subject, org.apache.jena.rdf.model.Property prop, URI object) {
        return model.contains(as_statement(subject, prop, object));
    }

    private Statement as_statement(URI subject, org.apache.jena.rdf.model.Property prop, URI object) {
        return model.createStatement(as_resource(subject), prop, as_resource(object));
    }
}
