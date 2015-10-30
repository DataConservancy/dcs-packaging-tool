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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class PackageDescriptionRDFTransformTest {
    
    private PackageDescription description;
    private final static String ARTIFACT_ONE_ID = "id:a";
    private final static String ARTIFACT_TWO_ID = "id:b";
    private final static String ARTIFACT_THREE_ID = "id:c";
    private final static String ARTIFACT_FOUR_ID = "id:d";

    private final static String RELATIONSHIP_ONE_TYPE = "relationshipsOne";
    private final static String RELATIONSHIP_TWO_TYPE = "relationshipTwo";
    
    @Before 
    public void setup() {
        PackageRelationship relationshipSetOne = new PackageRelationship(RELATIONSHIP_ONE_TYPE, false, "foo", "bar", "baz");
        PackageRelationship relationshipSetTwo = new PackageRelationship(RELATIONSHIP_TWO_TYPE, false, "foo");

        PropertyValueGroup groupOne = new PropertyValueGroup();
        groupOne.addSubPropertyValue("SubOne", "SubValueOne");
        groupOne.addSubPropertyValue("SubTwo", "SubValueTwo");
        
        PropertyValueGroup groupTwo = new PropertyValueGroup();
        Set<String> subValues = new HashSet<String>();
        subValues.add("SetValueOne");
        subValues.add("SetValueTwo");
        
        groupTwo.setSubPropertyValues("SubSet", subValues);
        groupTwo.addSubPropertyValue("SubThree", "SubValueThree");
        
        PackageArtifact artifactOne = new PackageArtifact();
        PackageArtifact artifactTwo = new PackageArtifact();
        PackageArtifact artifactThree = new PackageArtifact();
        PackageArtifact artifactFour = new PackageArtifact();

        artifactOne.setId(ARTIFACT_ONE_ID);
        artifactOne.setType(DcsPackageArtifactType.Collection.name());
        artifactOne.setArtifactRef("this.ref");
        artifactOne.addSimplePropertyValue("PropertyOne", "valueOne");
        artifactOne.addSimplePropertyValue("PropertyTwo", "valueTwo");
        artifactOne.addPropertyValueGroup("PropertyGroup", groupOne);
        artifactOne.addPropertyValueGroup("PropertyGroup", groupTwo);
        
        artifactOne.setRelationships(relationshipSetOne);
        artifactOne.setByteStream(false);
        artifactOne.setIgnored(false);
        
        artifactTwo.setId(ARTIFACT_TWO_ID);
        artifactTwo.setType(DcsPackageArtifactType.DataItem.name());
        artifactTwo.setArtifactRef("this.ref");
        artifactTwo.addSimplePropertyValue("PropertyOne", "valueOne");
        artifactTwo.addSimplePropertyValue("PropertyTwo", "valueTwo");
        artifactTwo.setRelationships(relationshipSetOne);
        artifactTwo.setByteStream(false);
        artifactTwo.setIgnored(true);
        
        artifactThree.setId(ARTIFACT_THREE_ID);
        artifactThree.setType(DcsPackageArtifactType.DataItem.name());
        artifactThree.setArtifactRef("this.ref");
        artifactThree.addSimplePropertyValue("PropertyThree", "valueThree");
        artifactThree.setRelationships(relationshipSetTwo);
        artifactThree.setByteStream(false);

        artifactFour.setId(ARTIFACT_FOUR_ID);
        artifactFour.setType(DcsPackageArtifactType.DataFile.name());
        artifactFour.setArtifactRef("this.ref");
        artifactFour.addSimplePropertyValue("PropertyThree", "valueThree");
        artifactFour.addSimplePropertyValue("PropertyThree", "valueFour");
        artifactFour.setRelationships(relationshipSetTwo);
        artifactFour.setByteStream(true);
        
        Set<PackageArtifact> artifactSetOne = new HashSet<PackageArtifact>();
        artifactSetOne.add(artifactOne);
        artifactSetOne.add(artifactTwo);
        artifactSetOne.add(artifactThree);
        artifactSetOne.add(artifactFour);

        String specIdentifierOne = "Spec:one";
        description = new PackageDescription();
        
        description.setPackageOntologyIdentifier(specIdentifierOne);
        description.setRootArtifactRef("root");
        description.setPackageArtifacts(artifactSetOne);
    }
    
    @Test
    public void testTransformToRdf() throws RDFTransformException {
        Model descriptionModel = PackageDescriptionRDFTransform.transformToRDF(description);
        List<Resource> descriptionResource = descriptionModel.listResourcesWithProperty(RDF.type, PackageResourceMapConstants.PACKAGE_DESCRIPTION_TYPE).toList();
        assertEquals(1, descriptionResource.size());
        
        List<Resource> artifactResources = descriptionModel.listResourcesWithProperty(RDF.type, PackageResourceMapConstants.ARTIFACT_TYPE).toList();        
        assertEquals(4, artifactResources.size());
        
        boolean testedArtifactOne = false;
        boolean testedArtifactTwo = false;
        boolean testedArtifactThree = false;
        boolean testedArtifactFour = false;
        for (Resource artifactResource : artifactResources) {
            if (artifactResource.hasProperty(PackageResourceMapConstants.HAS_ID, ARTIFACT_ONE_ID)) {
                assertTrue(artifactResource.hasProperty(PackageResourceMapConstants.HAS_TYPE, DcsPackageArtifactType.Collection.name()));
                assertEquals(4, artifactResource.listProperties(PackageResourceMapConstants.HAS_PROPERTY).toList().size());
                
                int valueGroupPropertyFound = 0;
                int valueGroupFound = 0;
                for (RDFNode propertyNode: descriptionModel.listObjectsOfProperty(artifactResource, PackageResourceMapConstants.HAS_PROPERTY).toList()) {
                    Resource propertyResource = propertyNode.asResource();
                    if (propertyResource.listProperties(PackageResourceMapConstants.HAS_PROPERTY).toList().size() > 0) {
                        valueGroupPropertyFound += propertyResource.listProperties(PackageResourceMapConstants.HAS_PROPERTY).toList().size();
                        valueGroupFound++;
                        
                        if (propertyResource.hasProperty(PackageResourceMapConstants.HAS_NAME, "PropertyGroup")) {
                            assertEquals(2, descriptionModel.listObjectsOfProperty(propertyResource, PackageResourceMapConstants.HAS_PROPERTY).toList().size());
                        }
                        
                        if (propertyResource.hasProperty(PackageResourceMapConstants.HAS_NAME, "PropertyGroupTwo")) {
                            boolean subPropertyOneFound = false;
                            boolean subPropertyTwoFound = false;
                            for (RDFNode subPropertyNode : descriptionModel.listObjectsOfProperty(propertyResource, PackageResourceMapConstants.HAS_PROPERTY).toList()) {
                                Resource subPropertyResource = subPropertyNode.asResource();
                                if (subPropertyResource.hasProperty(PackageResourceMapConstants.HAS_NAME, "SubSet")) {
                                    subPropertyOneFound = true;
                                    assertEquals(2, subPropertyResource.listProperties(PackageResourceMapConstants.HAS_VALUE).toList().size());
                                } else if (subPropertyResource.hasProperty(PackageResourceMapConstants.HAS_NAME, "SubThree")) {
                                    subPropertyTwoFound = true;
                                    assertEquals(1, subPropertyResource.listProperties(PackageResourceMapConstants.HAS_VALUE).toList().size());
                                }
                            }
                            
                            assertTrue(subPropertyOneFound);
                            assertTrue(subPropertyTwoFound);
                        }
                    }
                    
                    
                }
                
                assertEquals(2, valueGroupFound);
                assertEquals(4, valueGroupPropertyFound);
                
                assertEquals(1, artifactResource.listProperties(PackageResourceMapConstants.HAS_RELATIONSHIP).toList().size());
                assertEquals(1,artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).toList().size());
                assertFalse(artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).toList().get(0).getBoolean());
                
                assertEquals(1,artifactResource.listProperties(PackageResourceMapConstants.IS_IGNORED).toList().size());
                assertFalse(artifactResource.listProperties(PackageResourceMapConstants.IS_IGNORED).toList().get(0).getBoolean());
                
                testedArtifactOne = true;
            } else if (artifactResource.hasProperty(PackageResourceMapConstants.HAS_ID, ARTIFACT_TWO_ID)) {
                assertTrue(artifactResource.hasProperty(PackageResourceMapConstants.HAS_TYPE, DcsPackageArtifactType.DataItem.name()));
                assertEquals(2, artifactResource.listProperties(PackageResourceMapConstants.HAS_PROPERTY).toList().size());
                assertEquals(1, artifactResource.listProperties(PackageResourceMapConstants.HAS_RELATIONSHIP).toList().size());
                assertEquals(1,artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).toList().size());
                assertFalse(artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).toList().get(0).getBoolean());
                assertEquals(1,artifactResource.listProperties(PackageResourceMapConstants.IS_IGNORED).toList().size());
                assertTrue(artifactResource.listProperties(PackageResourceMapConstants.IS_IGNORED).toList().get(0).getBoolean());
                testedArtifactTwo = true;
            } else if (artifactResource.hasProperty(PackageResourceMapConstants.HAS_ID, ARTIFACT_THREE_ID)) {
                assertTrue(artifactResource.hasProperty(PackageResourceMapConstants.HAS_TYPE, DcsPackageArtifactType.DataItem.name()));
                assertEquals(1, artifactResource.listProperties(PackageResourceMapConstants.HAS_PROPERTY).toList().size());
                assertEquals(1, artifactResource.listProperties(PackageResourceMapConstants.HAS_RELATIONSHIP).toList().size());
                assertEquals(1,artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).toList().size());
                assertFalse(artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).toList().get(0).getBoolean());
                testedArtifactThree = true;
            } else if (artifactResource.hasProperty(PackageResourceMapConstants.HAS_ID, ARTIFACT_FOUR_ID)) {
                assertTrue(artifactResource.hasProperty(PackageResourceMapConstants.HAS_TYPE, DcsPackageArtifactType.DataFile.name()));
                assertEquals(1, artifactResource.listProperties(PackageResourceMapConstants.HAS_PROPERTY).toList().size());
                assertEquals(1, artifactResource.listProperties(PackageResourceMapConstants.HAS_RELATIONSHIP).toList().size());
                assertEquals(1, artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).toList().size());
                assertTrue(artifactResource.listProperties(PackageResourceMapConstants.IS_BYTE_STREAM).next().getBoolean());

                testedArtifactFour = true;
            }

        }
        
        assertTrue(testedArtifactOne);
        assertTrue(testedArtifactTwo);
        assertTrue(testedArtifactFour);
        assertTrue(testedArtifactThree);
        
        List<Resource> relationshipResources = descriptionModel.listResourcesWithProperty(RDF.type, PackageResourceMapConstants.RELATIONSHIP_TYPE).toList();
        assertEquals(4, relationshipResources.size());
        
        boolean relationshipOneTested = false;
        boolean relationshipTwoTested = false;
        for (Resource relationshipResource : relationshipResources) {
            if (relationshipResource.hasProperty(PackageResourceMapConstants.HAS_TYPE, RELATIONSHIP_ONE_TYPE)) {
                assertEquals(3, relationshipResource.listProperties(PackageResourceMapConstants.HAS_TARGET).toList().size());
                relationshipOneTested = true;
            } else if (relationshipResource.hasProperty(PackageResourceMapConstants.HAS_TYPE, RELATIONSHIP_TWO_TYPE)) {
                assertEquals(1, relationshipResource.listProperties(PackageResourceMapConstants.HAS_TARGET).toList().size());
                relationshipTwoTested = true;
            }
        }
        
        assertTrue(relationshipOneTested);
        assertTrue(relationshipTwoTested);
    }
    
    @Test
    public void testTransformToPackageDescription() throws RDFTransformException {
        Model model = PackageDescriptionRDFTransform.transformToRDF(description);
        PackageDescription test = PackageDescriptionRDFTransform.transformToPackageDescription(model);
        
        assertEquals(description, test);
    }
}