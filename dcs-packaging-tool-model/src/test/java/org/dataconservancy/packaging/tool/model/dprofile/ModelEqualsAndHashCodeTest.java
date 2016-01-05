package org.dataconservancy.packaging.tool.model.dprofile;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import java.net.URI;
import java.net.URISyntaxException;

public class ModelEqualsAndHashCodeTest {

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
                .withPrefabValues(NodeType.class, typeA, typeB)
                .verify();
    }
    
    @Test
    public void NodeTransformTest() {
        EqualsVerifier
                .forClass(NodeTransform.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .withPrefabValues(NodeType.class, typeA, typeB)
                .verify();
    }
    
    @Test
    public void NodeTypeTest() {
        EqualsVerifier
                .forClass(NodeType.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .withPrefabValues(NodeType.class, typeA, typeB)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void PropertyConstraintTest() {
        EqualsVerifier
                .forClass(PropertyConstraint.class)
                .allFieldsShouldBeUsed().withRedefinedSuperclass()
                .withPrefabValues(NodeType.class, typeA, typeB)
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
