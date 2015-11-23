package org.dataconservancy.packaging.tool.model.ipm;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class NodeTest {

    private NodeType typeA;
    private NodeType typeB;

    @Before
    public void setup() throws URISyntaxException {
        typeA = new NodeType();
        typeA.setIdentifier(new URI("id:A"));
        typeB = new NodeType();
        typeB.setIdentifier(new URI("id:B"));
    }

    @Test
    public void equalsTest() throws URISyntaxException {
        EqualsVerifier
            .forClass(Node.class)
            .withPrefabValues(Node.class, new Node(new URI("uri:foo")), new Node(new URI("uri:bar")))
            .withPrefabValues(NodeType.class, typeA, typeB)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }
}
