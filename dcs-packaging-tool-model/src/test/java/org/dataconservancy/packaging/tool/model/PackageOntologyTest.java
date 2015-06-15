/*
 * Copyright 2014 Johns Hopkins University
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import static org.junit.Assert.*;

public class PackageOntologyTest {
    private static final String PROJECT = "Project";
    private static final String COLLECTION = "Collection";
    private static final String DATAITEM = "DataItem";
    private static final String METADATAFILE = "MetadataFile";
    private static final String DATAFILE = "DataFile";
    private static final String ontologyId = "dcs.package.ontology:1";

    @Test
    public void testAddingAndRetrieveArtifact() {
        PackageOntology dcsBoOntology = new PackageOntology();

        dcsBoOntology.setId(ontologyId);

        dcsBoOntology.addArtifactType(PROJECT);
        dcsBoOntology.addArtifactProperty(PROJECT, "name", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, "description", "String", 1, 1, false, true);
        dcsBoOntology.addArtifactProperty(PROJECT, "projectAdmin", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, "startDate", "DateTime", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, "endDate", "DateTime", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, "storageAllocated", "long", 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, "storageUsed", "long", 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, "fundingEntity", "String", 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, "awardNumber", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactRelationship(PROJECT, "isProjectOf",  COLLECTION, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(PROJECT, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);

        dcsBoOntology.addArtifactType(COLLECTION);
        dcsBoOntology.addArtifactProperty(COLLECTION, "name", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "description", "String", 1, 1, false, true);
        dcsBoOntology.addArtifactProperty(COLLECTION, "creatorName", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "createdDate", "DateTime", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "alternateIds", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "citableLocator", "String", 0, 1, false, false);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "belongsToProject", PROJECT, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "isMemberOf", COLLECTION, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMember", DATAITEM, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMember", COLLECTION, true, 0, Integer.MAX_VALUE);

        dcsBoOntology.addArtifactType(DATAITEM);
        dcsBoOntology.addArtifactProperty(DATAITEM, "name", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, "description", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, "depositorId", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactRelationship(DATAITEM, "isMemberOf", COLLECTION, true, 1, 1);
        dcsBoOntology.addArtifactRelationship(DATAITEM, "hasMember", DATAFILE, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(DATAITEM, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);

        assertEquals(3, dcsBoOntology.getArtifactTypes().size());
        assertTrue(dcsBoOntology.getArtifactTypes().contains(PROJECT));
        assertTrue(dcsBoOntology.getArtifactTypes().contains(COLLECTION));
        assertTrue(dcsBoOntology.getArtifactTypes().contains(DATAITEM));

        Set<PackageOntology.Property> projectProperties = dcsBoOntology.getProperties(PROJECT);
        assertEquals(9, projectProperties.size());
        for (PackageOntology.Property property : projectProperties) {
            if (property.getName().equals("name")) {
                assertEquals("String", property.getValueType());
                assertEquals(1, property.getMinOccurrence());
                assertEquals(1, property.getMaxOccurrence());
            } else if (property.getName().equals("description")) {
                assertEquals("String", property.getValueType());
                assertEquals(1, property.getMinOccurrence());
                assertEquals(1, property.getMaxOccurrence());
            } else if (property.getName().equals("projectAdmin")) {
                assertEquals("String", property.getValueType());
                assertEquals(1, property.getMinOccurrence());
                assertEquals(Integer.MAX_VALUE, property.getMaxOccurrence());
            } else if (property.getName().equals("startDate")) {
                assertEquals("DateTime", property.getValueType());
                assertEquals(1, property.getMinOccurrence());
                assertEquals(1, property.getMaxOccurrence());
            } else if (property.getName().equals("endDate")) {
                assertEquals("DateTime", property.getValueType());
                assertEquals(1, property.getMinOccurrence());
                assertEquals(1, property.getMaxOccurrence());
            } else if (property.getName().equals("storageAllocated")) {
                assertEquals("long", property.getValueType());
                assertEquals(0, property.getMinOccurrence());
                assertEquals(1, property.getMaxOccurrence());
            } else if (property.getName().equals("storageUsed")) {
                assertEquals("long", property.getValueType());
                assertEquals(0, property.getMinOccurrence());
                assertEquals(1, property.getMaxOccurrence());
            } else if (property.getName().equals("fundingEntity")) {
                assertEquals("String", property.getValueType());
                assertEquals(0, property.getMinOccurrence());
                assertEquals(Integer.MAX_VALUE, property.getMaxOccurrence());
            } else if (property.getName().equals("awardNumber")) {
                assertEquals("String", property.getValueType());
                assertEquals(1, property.getMinOccurrence());
                assertEquals(Integer.MAX_VALUE, property.getMaxOccurrence());
            } else {
                fail("Found a property that was not expected to be part of the Project's allowable set of properties");
            }

            Set<PackageOntology.Relationship> projectRelationships = dcsBoOntology.getRelationships(PROJECT);
            assertEquals(2, projectRelationships.size());
            for (PackageOntology.Relationship relationship : projectRelationships) {
                if (relationship.getName().equals("isProjectOf")) {
                    assertEquals(COLLECTION, relationship.getRelatedArtifactType());
                    assertEquals(0, relationship.getMinOccurrence());
                    assertEquals(Integer.MAX_VALUE, relationship.getMaxOccurrence());
                } else if (relationship.getName().equals("hasMetadata")) {
                    assertEquals(METADATAFILE, relationship.getRelatedArtifactType());
                    assertEquals(0, relationship.getMinOccurrence());
                    assertEquals(Integer.MAX_VALUE, relationship.getMaxOccurrence());
                } else {
                    fail("Found relationship that was not expected to part of the Project's allowable set of relationships");
                }
            }
        }
    }

    @Test
    public void testAddingPropertyToNonExistingArtifactType() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(PackageOntology.PROPERTY_TO_NONEXISTING_TYPE_ERR_MSG);

        PackageOntology dcsBoOntology = new PackageOntology();
        dcsBoOntology.setId(ontologyId);

        dcsBoOntology.addArtifactProperty(COLLECTION, "name", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "description", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "creatorName", "String", 1, Integer.MAX_VALUE, false, false);

    }

    @Test
    public void testAddingRelationshipToNonExistingArtifactType() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(PackageOntology.RELATIONSHIP_TO_NONEXISTING_TYPE_ERR_MSG);

        PackageOntology dcsBoOntology = new PackageOntology();
        dcsBoOntology.setId(ontologyId);

        dcsBoOntology.addArtifactRelationship(COLLECTION, "belongsToProject", PROJECT, true,  0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "isMemberOf", COLLECTION, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);

    }

    @Test
    public void testGetPropertiesForNonExistingArtifactType() {
        PackageOntology dcsBoOntology = new PackageOntology();
        dcsBoOntology.setId(ontologyId);

        dcsBoOntology.addArtifactType(COLLECTION);
        dcsBoOntology.addArtifactProperty(COLLECTION, "name", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "description", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "creatorName", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "createdDate", "DateTime", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "alternateIds", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "citableLocator", "String", 0, 1, false, false);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "belongsToProject", PROJECT, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "isMemberOf", COLLECTION, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMember", DATAITEM, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMember", COLLECTION, true, 0, Integer.MAX_VALUE);

        assertNull(dcsBoOntology.getProperties(PROJECT));

    }

    @Test
    public void testGetRelationshipsForNonExistingArtifactType() {
        PackageOntology dcsBoOntology = new PackageOntology();
        dcsBoOntology.setId(ontologyId);

        dcsBoOntology.addArtifactType(COLLECTION);
        dcsBoOntology.addArtifactProperty(COLLECTION, "name", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "description", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "creatorName", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "createdDate", "DateTime", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "alternateIds", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "citableLocator", "String", 0, 1, false, false);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "belongsToProject", PROJECT, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "isMemberOf", COLLECTION, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMember", DATAITEM, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, "hasMember", COLLECTION, true, 0, Integer.MAX_VALUE);

        assertNull(dcsBoOntology.getRelationships(PROJECT));

    }

    @Test
    public void testGetPropertyType() {
        PackageOntology dcsBoOntology = new PackageOntology();
        dcsBoOntology.setId(ontologyId);

        dcsBoOntology.addArtifactType(COLLECTION);
        dcsBoOntology.addArtifactProperty(COLLECTION, "name", "String", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "description", "String", 1, 1, false, true);
        dcsBoOntology.addArtifactProperty(COLLECTION, "creatorName", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "createdDate", "DateTime", 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "alternateIds", "String", 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "citableLocator", "String", 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, "creator", "CreatorType", 0, Integer.MAX_VALUE, false, true);

        PackageOntology.PropertyType creatorType = dcsBoOntology. new PropertyType();
        creatorType.setName("CreatorType");
        creatorType.addField(dcsBoOntology.new Property("name", "String", 1, 1, false, false));
        creatorType.addField(dcsBoOntology.new Property("phone", "phone", 0, Integer.MAX_VALUE, false, false));
        creatorType.addField(dcsBoOntology.new Property("email", "email", 0, Integer.MAX_VALUE, false, false));
        creatorType.addField(dcsBoOntology.new Property("webpage", "url", 0, Integer.MAX_VALUE, false, false));
        dcsBoOntology.getCustomPropertyTypes().add(creatorType);

        //Test simple properties
        assertEquals("String", dcsBoOntology.getPropertyType(COLLECTION, "", "name"));
        assertEquals("DateTime", dcsBoOntology.getPropertyType(COLLECTION, "", "createdDate"));


        //Test complex properties
        assertEquals("CreatorType", dcsBoOntology.getPropertyType(COLLECTION, "", "creator"));
        assertEquals("String", dcsBoOntology.getPropertyType(COLLECTION, "creator", "name"));
        assertEquals("phone", dcsBoOntology.getPropertyType(COLLECTION, "creator", "phone"));

        //Test not found properties
        assertNull(dcsBoOntology.getPropertyType(COLLECTION, "", "foo"));
        assertNull(dcsBoOntology.getPropertyType("FOO", "", "name"));
        assertNull(dcsBoOntology.getPropertyType(COLLECTION, "CreatorType", "address"));
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();
}
