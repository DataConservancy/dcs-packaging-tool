package org.dataconservancy.packaging.tool.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class ApplicationVersionTest {
    @Test
    public void ApplicationVersionVerifier() {
        EqualsVerifier
                .forClass(ApplicationVersion.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
}
