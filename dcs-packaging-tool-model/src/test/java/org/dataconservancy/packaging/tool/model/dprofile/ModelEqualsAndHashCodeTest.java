package org.dataconservancy.packaging.tool.model.dprofile;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ModelEqualsAndHashCodeTest {
    @Test
    public void AbstractDescribedObjectTest() {
        EqualsVerifier
                .forClass(AbstractDescribedObject.class)
                .allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void CardinalityConstraintTest() {
        EqualsVerifier
                .forClass(CardinalityConstraint.class)
                .allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void DomainProfileTest() {
        EqualsVerifier
                .forClass(DomainProfile.class)
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void NodeConstraintTest() {
        EqualsVerifier
                .forClass(NodeConstraint.class)
                .allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void NodeTransformTest() {
        EqualsVerifier
                .forClass(NodeTransform.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void NodeTypeTest() {
        EqualsVerifier
                .forClass(NodeType.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void PropertyCategoryTest() {
        EqualsVerifier
                .forClass(PropertyCategory.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void PropertyConstraintTest() {
        EqualsVerifier
                .forClass(PropertyConstraint.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void PropertyTypeTest() {
        EqualsVerifier
                .forClass(PropertyType.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void PropertyValueTest() {
        EqualsVerifier
                .forClass(Property.class)
                .allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void StructuralRelationTest() {
        EqualsVerifier
                .forClass(StructuralRelation.class)
                .allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
}
