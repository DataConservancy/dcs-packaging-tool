package org.dataconservancy.packaging.tool.profile;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStoreImpl;
import org.dataconservancy.packaging.tool.impl.DomainProfileServiceImpl;
import org.dataconservancy.packaging.tool.impl.SimpleURIGenerator;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.impl.support.IpmTreeFactory;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.Test;

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

    // Update objects and then validate the tree
    private void checkValidTree(Node node) {
        node.walk(store::updateObject);
        assertTrue(service.validateTree(node));
    }
}
