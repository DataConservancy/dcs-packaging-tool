package org.dataconservancy.packaging.tool.model.ipm;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class NodeTest {

    @Test
    public void equalsTest() throws URISyntaxException {
        EqualsVerifier
            .forClass(Node.class)
            .withPrefabValues(Node.class, new Node(new URI("uri:foo")), new Node(new URI("uri:bar")))
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }
}
