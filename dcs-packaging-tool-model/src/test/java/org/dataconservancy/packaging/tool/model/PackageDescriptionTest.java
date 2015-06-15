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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class PackageDescriptionTest {
    
    @Test
    public void testEquality() {
        PackageRelationship relationshipOne = new PackageRelationship("relationshipsOne", false, "foo", "bar", "baz");
        PackageRelationship relationshipTwo = new PackageRelationship("relationshipsTwo", false, "foo");

        PackageArtifact artifactOne = new PackageArtifact();
        PackageArtifact artifactTwo = new PackageArtifact();
        PackageArtifact artifactThree = new PackageArtifact();
        
        artifactOne.setId("id:a");
        artifactOne.setType(DcsPackageArtifactType.Collection.name());
        artifactOne.setArtifactRef("this.ref");
        artifactOne.addSimplePropertyValue("PropertyOne", "valueOne");
        artifactOne.addSimplePropertyValue("PropertyTwo", "valueTwo");
        artifactOne.setRelationships(relationshipOne);
        
        artifactTwo.setId("id:a");
        artifactTwo.setType(DcsPackageArtifactType.Collection.name());
        artifactTwo.setArtifactRef("this.ref");
        artifactOne.addSimplePropertyValue("PropertyOne", "valueOne");
        artifactOne.addSimplePropertyValue("PropertyTwo", "valueTwo");
        artifactTwo.setRelationships(relationshipOne);
        
        artifactThree.setId("id:b");
        artifactThree.setType(DcsPackageArtifactType.Collection.name());
        artifactThree.setArtifactRef("this.ref");
        artifactThree.addSimplePropertyValue("PropertyThree", "valueThree");
        artifactThree.setRelationships(relationshipTwo);
        
        Set<PackageArtifact> artifactSetOne = new HashSet<PackageArtifact>();
        artifactSetOne.add(artifactOne);
        artifactSetOne.add(artifactTwo);
        
        Set<PackageArtifact> artifactSetTwo = new HashSet<PackageArtifact>();
        artifactSetTwo.add(artifactThree);
        
        String specIdentifierOne = "Spec:one";
        String specIdentifierTwo = "Spec:two";
        PackageDescription descriptionOne = new PackageDescription();
        PackageDescription descriptionTwo = new PackageDescription();
        PackageDescription descriptionThree = new PackageDescription();
        
        descriptionOne.setPackageOntologyIdentifier(specIdentifierOne);
        descriptionTwo.setPackageOntologyIdentifier(specIdentifierOne);
        descriptionThree.setPackageOntologyIdentifier(specIdentifierTwo);
        
        descriptionOne.setPackageArtifacts(artifactSetOne);
        descriptionTwo.setPackageArtifacts(artifactSetOne);
        descriptionThree.setPackageArtifacts(artifactSetTwo);
        
        assertTrue(descriptionOne.equals(descriptionOne));
        assertTrue(descriptionOne.equals(descriptionTwo));
        assertTrue(descriptionTwo.equals(descriptionOne));
        
        assertFalse(descriptionOne.equals(descriptionThree));
        assertFalse(descriptionThree.equals(descriptionOne));
    }
}