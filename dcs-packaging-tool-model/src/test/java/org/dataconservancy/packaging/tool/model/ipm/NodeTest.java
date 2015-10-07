package org.dataconservancy.packaging.tool.model.ipm;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class NodeTest {

    @Test
    public void equalsTest() {
        EqualsVerifier
            .forClass(NodeTest.class)
            .allFieldsShouldBeUsed()
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }
}
