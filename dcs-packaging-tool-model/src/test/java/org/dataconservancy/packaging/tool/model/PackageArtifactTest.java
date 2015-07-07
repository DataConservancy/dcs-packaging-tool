/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;


public class PackageArtifactTest {
    
    @Test
    public void testEquality() {

        String propertyOneName = "PropertyOne";
        String propertyOneValue = "ValueOne";

        String propertyTwoName = "PropertyTwo";
        String propertyTwoValue = "ValueTwo";

        PackageRelationship relationshipSetOne = new PackageRelationship("relationshipsOne", false, "foo", "bar", "baz");
        PackageRelationship relationshipSetTwo = new PackageRelationship("relationshipsTwo", false, "foo");

        Map<String, Set<String>> propertySetTwo = new HashMap<String, Set<String>>();
        propertySetTwo.put("PropertyThree", new HashSet<String>(Arrays.asList(new String[] {"valueThree"})));

        String propertyThreeName = "PropertyThree";
        String propertyThreeValue = "valueThree";

        PackageArtifact artifactOne = new PackageArtifact();
        PackageArtifact artifactTwo = new PackageArtifact();
        PackageArtifact artifactThree = new PackageArtifact();
        
        artifactOne.setId("id:a");
        artifactOne.setType(DcsPackageArtifactType.Collection.name());
        artifactOne.setArtifactRef("file:/this.ref");
        artifactOne.addSimplePropertyValue(propertyOneName, propertyOneValue);
        artifactOne.addSimplePropertyValue(propertyTwoName, propertyTwoValue);
        artifactOne.setRelationships(relationshipSetOne);
        artifactOne.setIgnored(true);

        artifactTwo.setId("id:a");
        artifactTwo.setType(DcsPackageArtifactType.Collection.name());
        artifactTwo.setArtifactRef("file:/this.ref");
        artifactTwo.addSimplePropertyValue(propertyOneName, propertyOneValue);
        artifactTwo.addSimplePropertyValue(propertyTwoName, propertyTwoValue);
        artifactTwo.setRelationships(relationshipSetOne);
        artifactTwo.setIgnored(true);

        artifactThree.setId("id:b");
        artifactThree.setType(DcsPackageArtifactType.Collection.name());
        artifactThree.setArtifactRef("file:/this.ref");
        artifactThree.addSimplePropertyValue(propertyThreeName, propertyThreeValue);
        artifactThree.setRelationships(relationshipSetTwo);
        artifactThree.setIgnored(false);
        
        assertTrue(artifactOne.equals(artifactOne));
        assertTrue(artifactOne.equals(artifactTwo));
        assertTrue(artifactTwo.equals(artifactOne));
        
        assertFalse(artifactOne.equals(artifactThree));
        assertFalse(artifactThree.equals(artifactOne));
    }


    @Test
    public void testAddSimpleProperty() {
        PackageArtifact artifact = new PackageArtifact();
        assertEquals(0, artifact.getPropertyNames().size());

        // Test that adding a new property works
        artifact.addSimplePropertyValue("prop1", "val1");
        assertEquals(1, artifact.getPropertyNames().size());
        assertEquals(1, artifact.getSimplePropertyValues("prop1").size());

        // Test that adding a second property works.
        artifact.addSimplePropertyValue("prop2", "val2");
        assertEquals(2, artifact.getPropertyNames().size());
        assertEquals(1, artifact.getSimplePropertyValues("prop2").size());

        // Test that adding a new value to an existing property works
        artifact.addSimplePropertyValue("prop1", "val3");
        assertEquals(2, artifact.getPropertyNames().size());
        assertEquals(2, artifact.getSimplePropertyValues("prop1").size());
    }

    /**
     * Test setProperty() method.
     */
    @Test
    public void testSetProperty () {
        Set<String> values = new HashSet<String>(Arrays.asList("dogs", "cats", "spiders", "cows", "foxes"));
        PackageArtifact artifact = new PackageArtifact();
        assertEquals(0, artifact.getPropertyNames().size());

        artifact.setSimplePropertyValues("animals", values);
        assertEquals(1, artifact.getPropertyNames().size());
        assertEquals(5, artifact.getSimplePropertyValues("animals").size());
        assertTrue(artifact.getSimplePropertyValues("animals").contains("dogs"));
        assertTrue(artifact.getSimplePropertyValues("animals").contains("cats"));
        assertTrue(artifact.getSimplePropertyValues("animals").contains("cows"));
        assertTrue(artifact.getSimplePropertyValues("animals").contains("spiders"));
        assertTrue(artifact.getSimplePropertyValues("animals").contains("foxes"));

    }

    /**
     * Test getSimpleProperty() method.
     */
    @Test
    public void testGetSimpleProperty() {
        PackageArtifact artifact = new PackageArtifact();

        String prop1Name = "prop1";
        String prop2Name = "prop2";
        String prop2Value = "prop2Value";
        String prop1Value = "prop1Value";
        artifact.addSimplePropertyValue(prop1Name, prop1Value);
        artifact.addSimplePropertyValue(prop2Name, prop2Value);

        assertEquals(1, artifact.getSimplePropertyValues(prop1Name).size());
        assertTrue(artifact.getSimplePropertyValues(prop1Name).contains(prop1Value));
        assertEquals(1, artifact.getSimplePropertyValues(prop2Name).size());
        assertTrue(artifact.getSimplePropertyValues(prop2Name).contains(prop2Value));

    }
    /**
     * Test method addPropertyValueGroup() on interface
     */
    @Test
    public void testAddPropertyGroup() {
        PackageArtifact artifact = new PackageArtifact();
        assertEquals(0, artifact.getPropertyNames().size());

        String propertyGroupName = "contactInfo";


        PackageArtifact.PropertyValueGroup contactInfo1 = new PackageArtifact.PropertyValueGroup();
        contactInfo1.addSubPropertyValue("name", "Willard Brown");
        contactInfo1.addSubPropertyValue("phone", "18004682663");
        contactInfo1.addSubPropertyValue("phone", "18004683663");
        contactInfo1.addSubPropertyValue("email", "finisher@bonesexpress.com");
        contactInfo1.addSubPropertyValue("page", "http://bonesexpress.com");

        // Test that adding a new property works
        artifact.addPropertyValueGroup(propertyGroupName, contactInfo1);
        assertEquals(1, artifact.getPropertyNames().size());
        assertEquals(1, artifact.getPropertyValueGroups(propertyGroupName).size());

        PackageArtifact.PropertyValueGroup contactInfo2 = new PackageArtifact.PropertyValueGroup();
        contactInfo2.addSubPropertyValue("name", "Alfee White");
        contactInfo2.addSubPropertyValue("phone", "18004733669");
        contactInfo2.addSubPropertyValue("email", "cruncher@bonesexpress.com");
        contactInfo2.addSubPropertyValue("page", "http://bonesexpress.com");


        // Test that adding a second property works.
        artifact.addPropertyValueGroup(propertyGroupName, contactInfo2);
        assertEquals(1, artifact.getPropertyNames().size());
        assertEquals(2, artifact.getPropertyValueGroups(propertyGroupName).size());

        assertTrue(artifact.getPropertyValueGroups(propertyGroupName).contains(contactInfo1));
        assertTrue(artifact.getPropertyValueGroups(propertyGroupName).contains(contactInfo2));

    }

    /**
     * Test method getPropertyGroup
     */
    @Test
    public void testGetPropertyGroup() {
        PackageArtifact artifact = new PackageArtifact();
        assertEquals(0, artifact.getPropertyNames().size());

        String propertyGroupName = "contactInfo";

        PackageArtifact.PropertyValueGroup contactInfo1 = new PackageArtifact.PropertyValueGroup();
        contactInfo1.addSubPropertyValue("name", "Willard Brown");
        contactInfo1.addSubPropertyValue("phone", "18004682663");
        contactInfo1.addSubPropertyValue("phone", "18004683663");
        contactInfo1.addSubPropertyValue("email", "finisher@bonesexpress.com");
        contactInfo1.addSubPropertyValue("page", "http://bonesexpress.com");

        artifact.addPropertyValueGroup(propertyGroupName, contactInfo1);

        PackageArtifact.PropertyValueGroup contactInfo2 = new PackageArtifact.PropertyValueGroup();
        contactInfo2.addSubPropertyValue("name", "Alfee White");
        contactInfo2.addSubPropertyValue("phone", "18004733669");
        contactInfo2.addSubPropertyValue("email", "cruncher@bonesexpress.com");
        contactInfo2.addSubPropertyValue("page", "http://bonesexpress.com");


        artifact.addPropertyValueGroup(propertyGroupName, contactInfo2);

        assertEquals(2, artifact.getPropertyValueGroups(propertyGroupName).size());
        assertTrue(artifact.getPropertyValueGroups(propertyGroupName).contains(contactInfo1));
        assertTrue(artifact.getPropertyValueGroups(propertyGroupName).contains(contactInfo2));
    }

    @Test
    public void testGetPropertyGroupValues() {
        PackageArtifact artifact = new PackageArtifact();
        assertEquals(0, artifact.getPropertyNames().size());

        String propertyGroupName = "contactInfo";

        PackageArtifact.PropertyValueGroup contactInfo1 = new PackageArtifact.PropertyValueGroup();
        contactInfo1.addSubPropertyValue("name", "Willard Brown");
        contactInfo1.addSubPropertyValue("phone", "18004682663");
        contactInfo1.addSubPropertyValue("phone", "18004683663");
        contactInfo1.addSubPropertyValue("email", "finisher@bonesexpress.com");
        contactInfo1.addSubPropertyValue("page", "http://bonesexpress.com");

        artifact.addPropertyValueGroup(propertyGroupName, contactInfo1);

        Set<PackageArtifact.PropertyValueGroup> valueGroups = artifact.getPropertyValueGroups(propertyGroupName);
        assertEquals(1, valueGroups.size());
        for (PackageArtifact.PropertyValueGroup group : valueGroups) {
            assertEquals(1, group.getSubPropertyValues("name").size());
            assertTrue(group.getSubPropertyValues("name").contains("Willard Brown"));

            assertEquals(2, group.getSubPropertyValues("phone").size());
            assertTrue(group.getSubPropertyValues("phone").contains("18004682663"));
            assertTrue(group.getSubPropertyValues("phone").contains("18004683663"));

            assertEquals(1, group.getSubPropertyValues("email").size());
            assertTrue(group.getSubPropertyValues("email").contains("finisher@bonesexpress.com"));

            assertEquals(1, group.getSubPropertyValues("page").size());
            assertTrue(group.getSubPropertyValues("page").contains("http://bonesexpress.com"));

        }
    }

    @Test
    public void testGettingSubPropertyNames() {
        PackageArtifact artifact = new PackageArtifact();
        String propertyGroupName = "contactInfo";

        PackageArtifact.PropertyValueGroup contactInfo1 = new PackageArtifact.PropertyValueGroup();
        contactInfo1.addSubPropertyValue("name", "Willard Brown");
        contactInfo1.addSubPropertyValue("phone", "18004682663");
        contactInfo1.addSubPropertyValue("phone", "18004683663");
        contactInfo1.addSubPropertyValue("email", "finisher@bonesexpress.com");
        contactInfo1.addSubPropertyValue("page", "http://bonesexpress.com");

        artifact.addPropertyValueGroup(propertyGroupName, contactInfo1);

        Set<PackageArtifact.PropertyValueGroup> valueGroups = artifact.getPropertyValueGroups(propertyGroupName);
        assertEquals(1, valueGroups.size());
        for (PackageArtifact.PropertyValueGroup group : valueGroups) {
            assertEquals(4, group.getSubPropertyNames().size());
            assertTrue(group.getSubPropertyNames().contains("name"));
            assertTrue(group.getSubPropertyNames().contains("phone"));
            assertTrue(group.getSubPropertyNames().contains("email"));
            assertTrue(group.getSubPropertyNames().contains("page"));
        }
    }

    /**
     * Test that attempt to get Simple property value with getPropertyValueGroups() returns null
     */
    @Test
    public void testGettingSimplePropertyValueWithIncompatibleMethod() {
        PackageArtifact artifact = new PackageArtifact();
        assertEquals(0, artifact.getPropertyNames().size());

        // Test that adding a new property works
        artifact.addSimplePropertyValue("prop1", "val1");
        assertEquals(1, artifact.getPropertyNames().size());
        assertEquals(1, artifact.getSimplePropertyValues("prop1").size());

        assertNull(artifact.getPropertyValueGroups("prop1"));
    }

    /**
     * Test that null subproperty name cannot be added to the property value group.
     */
    @Test (expected = IllegalArgumentException.class)
    public void testAddSubPropertyWithNullName() {
        PackageArtifact.PropertyValueGroup valueGroup = new PackageArtifact.PropertyValueGroup();
        valueGroup.addSubPropertyValue(null, "testString");
    }

    /**
     * Test that null subproperty name and value cannot be added to the property value group.
     */
    @Test (expected = IllegalArgumentException.class)
    public void testSetNullForSubPropertyName() {
        PackageArtifact.PropertyValueGroup valueGroup = new PackageArtifact.PropertyValueGroup();
        valueGroup.setSubPropertyValues(null, null);
    }


}