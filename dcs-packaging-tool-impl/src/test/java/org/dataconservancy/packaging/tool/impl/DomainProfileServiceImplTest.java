package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.api.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the DomainProfileServiceImpl against the FarmDomainProfile. Methods that
 * add and remove properties are not tested here because they are actually
 * implemented by the DomainProfileObjectStoreImpl.
 */
public class DomainProfileServiceImplTest {
    private DomainProfileServiceImpl service;
    private FarmIpmTree tree;
    private FarmDomainProfile profile;

    @Before
    public void setup() {
        Model model = ModelFactory.createDefaultModel();
        DomainProfileObjectStore store = new DomainProfileObjectStoreImpl(model);

        service = new DomainProfileServiceImpl(store);
        tree = new FarmIpmTree();
        profile = tree.getProfile();
    }

    /**
     * A single node must get assigned to Farm because it is the only type which
     * may not have a parent.
     */
    @Test
    public void testAssignNodeTypesToSingleNode() {
        Node root = new Node(URI.create("test:node"));

        service.assignNodeTypes(profile, root);

        assertNotNull(root.getNodeType());
        assertNotNull(root.getDomainObject());
        assertEquals(profile.getFarmNodeType().getIdentifier(), root.getNodeType().getIdentifier());
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

        Property person1 = new Property(profile.getPersonPropertyType());

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
        service.removeProperty(node, profile.getPersonPropertyType());
        service.addProperty(node, person1);

        assertTrue(service.validateProperties(node, profile.getFarmNodeType()));
    }
}
