package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
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
    private FarmIpmTree ipmtree;

    @Before
    public void setup() {
        model = ModelFactory.createDefaultModel();
        store = new DomainProfileObjectStoreImpl(model);
        ipmtree = new FarmIpmTree();
        profile = ipmtree.getProfile();
    }

    /**
     * Update object tests walks the tree updating objects and checking the
     * model for correct triples.
     */
    @Test
    public void testUpdateObject() {
        test_update_object(ipmtree.getRoot());
    }

    @Test
    public void testAddStringProperty() {
        URI test_object = URI.create("test:moo");

        PropertyType type = profile.getTitlePropertyType();
        PropertyValue val = new PropertyValue(type);
        val.setStringValue("Jim the cow");

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
    }

    @Test
    public void testAddLongProperty() {
        URI test_object = URI.create("test:jimfoot");

        PropertyType type = profile.getSizePropertyType();
        PropertyValue val = new PropertyValue(type);
        val.setLongValue(32);

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
    }

    @Test
    public void testAddDateTimeProperty() {
        URI test_object = URI.create("test:jimlunch");

        PropertyType type = profile.getCreatedPropertyType();
        PropertyValue val = new PropertyValue(type);
        val.setDateTimeValue(new DateTime(100000000));

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
    }

    @Test
    public void testAddComplexProperty() {
        URI test_object = URI.create("test:farmcontactinfo");

        PropertyType type = profile.getPersonPropertyType();
        PropertyValue val = new PropertyValue(type);

        List<PropertyValue> subvals = new ArrayList<>();
        PropertyValue name_val = new PropertyValue(profile.getNamePropertyType());
        name_val.setStringValue("Jim Moocow Farmer");
        PropertyValue mbox_val = new PropertyValue(profile.getMboxPropertyType());
        mbox_val.setStringValue("moo@moo.moo");
        subvals.add(name_val);
        subvals.add(mbox_val);

        val.setComplexValue(subvals);

        store.addProperty(test_object, val);

        assertTrue(has_property(test_object, val));
    }

    @Test
    public void testRemoveSimplePropertyByValue() {
        URI test_object = URI.create("test:jimfoot");

        PropertyType type = profile.getSizePropertyType();
        PropertyValue val = new PropertyValue(type);
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
        PropertyValue val1 = new PropertyValue(type);
        val1.setLongValue(32);
        PropertyValue val2 = new PropertyValue(type);
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
        // System.err.println("Node: " + node.getIdentifier());

        store.updateObject(node);

        // System.err.println("Domain object: " + node.getDomainObject());

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
            for (PropertyValue val : type.getDefaultPropertyValues()) {
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

    private boolean has_property(URI id, PropertyValue val) {
        return store.getProperties(id, val.getPropertyType()).contains(val);
    }

    private Resource as_resource(URI uri) {
        return model.createResource(uri.toString());
    }

    private Property as_property(URI uri) {
        return model.createProperty(uri.toString());
    }

    private Statement as_statement(URI subject, URI predicate, URI object) {
        return model.createStatement(as_resource(subject), as_property(predicate), as_resource(object));
    }

    private boolean has_statement(URI subject, URI predicate, URI object) {
        return model.contains(as_statement(subject, predicate, object));
    }

    private boolean has_statement(URI subject, Property prop, URI object) {
        return model.contains(as_statement(subject, prop, object));
    }

    private Statement as_statement(URI subject, Property prop, URI object) {
        return model.createStatement(as_resource(subject), prop, as_resource(object));
    }
}
