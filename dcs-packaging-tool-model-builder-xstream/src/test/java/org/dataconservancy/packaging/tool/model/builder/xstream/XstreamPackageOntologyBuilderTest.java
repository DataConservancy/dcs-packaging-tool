/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.tool.model.builder.xstream;

import org.dataconservancy.packaging.tool.model.PackageOntology;
import org.dataconservancy.packaging.tool.model.PackageOntologyBuilder;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by hanh on 2/17/14.
 */
public class XstreamPackageOntologyBuilderTest {

    private static final String PROJECT = "Project";
    private static final String COLLECTION = "Collection";
    private static final String DATAITEM = "DataItem";
    private static final String METADATAFILE = "MetadataFile";
    private static final String DATAFILE = "DataFile";
    private static final String CONTACT_INFO_TYPE = "ContactInfo";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String PHONE = "phone";
    private static final String PAGES = "pages";
    private static final String STRING_TYPE = "String";

    PackageOntologyBuilder builder;
    PackageOntology bogusOntology;


    @Before
    public void setUp() {
        bogusOntology = new PackageOntology();
        bogusOntology.setId("package.ontology:dcs.bo.1");

        bogusOntology.addArtifactType(PROJECT);
        bogusOntology.addArtifactProperty(PROJECT, "name", "String", 1, 1, false, false);
        bogusOntology.addArtifactProperty(PROJECT, "description", "String", 1, 1, false, true);
        bogusOntology.addArtifactProperty(PROJECT, "projectAdmin", "String", 1, Integer.MAX_VALUE, false, false);
        bogusOntology.addArtifactProperty(PROJECT, "startDate", "DateTime", 1, 1, false, false);
        bogusOntology.addArtifactProperty(PROJECT, "endDate", "DateTime", 1, 1, false, false);
        bogusOntology.addArtifactProperty(PROJECT, "storageAllocated", "long", 0, 1, false, false);
        bogusOntology.addArtifactProperty(PROJECT, "storageUsed", "long", 0, 1, false, false);
        bogusOntology.addArtifactProperty(PROJECT, "fundingEntity", "String", 0, Integer.MAX_VALUE, false, false);
        bogusOntology.addArtifactProperty(PROJECT, "awardNumber", "String", 1, Integer.MAX_VALUE, false, false);
        bogusOntology.addArtifactRelationship(PROJECT, "isProjectOf", COLLECTION, true, 0, Integer.MAX_VALUE);
        bogusOntology.addArtifactRelationship(PROJECT, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);

        bogusOntology.addArtifactType(COLLECTION);
        bogusOntology.addArtifactProperty(COLLECTION, "name", "String", 1, 1, false, false);
        bogusOntology.addArtifactProperty(COLLECTION, "description", "String", 1, 1, false, false);
        bogusOntology.addArtifactProperty(COLLECTION, "creatorName", "String", 1, Integer.MAX_VALUE, false, false);
        bogusOntology.addArtifactProperty(COLLECTION, "createdDate", "DateTime", 1, 1, false, false);
        bogusOntology.addArtifactProperty(COLLECTION, "alternateIds", "String", 1, Integer.MAX_VALUE, false, false);
        bogusOntology.addArtifactProperty(COLLECTION, "citableLocator", "String", 0, 1, true, false);
        bogusOntology.addArtifactRelationship(COLLECTION, "belongsToProject", PROJECT, true,  0, 1);
        bogusOntology.addArtifactRelationship(COLLECTION, "isMemberOf", COLLECTION, true, 0, 1);
        bogusOntology.addArtifactRelationship(COLLECTION, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);
        bogusOntology.addArtifactRelationship(COLLECTION, "hasMember", DATAITEM, true, 0, Integer.MAX_VALUE);
        bogusOntology.addArtifactRelationship(COLLECTION, "hasMember", COLLECTION, true, 0, Integer.MAX_VALUE);

        bogusOntology.addArtifactType(DATAITEM);
        bogusOntology.addArtifactProperty(DATAITEM, "name", "String", 1, 1, false, false);
        bogusOntology.addArtifactProperty(DATAITEM, "description", "String", 1, 1, false, false);
        bogusOntology.addArtifactProperty(DATAITEM, "depositorId", "String", 1, 1, false, false);
        bogusOntology.addArtifactRelationship(DATAITEM, "isMemberOf", COLLECTION, true, 1, 1);
        bogusOntology.addArtifactRelationship(DATAITEM, "hasMember", DATAFILE, true, 0, Integer.MAX_VALUE);
        bogusOntology.addArtifactRelationship(DATAITEM, "hasMetadata", METADATAFILE, false, 0, Integer.MAX_VALUE);

        PackageOntology.PropertyType contactInfoPropertyType = bogusOntology.new PropertyType();
        contactInfoPropertyType.setName(CONTACT_INFO_TYPE);
        contactInfoPropertyType.addField(bogusOntology.new Property(NAME, STRING_TYPE,1, 1, false, false));
        contactInfoPropertyType.addField(bogusOntology.new Property(EMAIL, STRING_TYPE,1, Integer.MAX_VALUE, true, false));
        contactInfoPropertyType.addField(bogusOntology.new Property(PHONE, STRING_TYPE,1, Integer.MAX_VALUE, false, false));
        contactInfoPropertyType.addField(bogusOntology.new Property(PAGES, STRING_TYPE,1, Integer.MAX_VALUE, false, true));

        bogusOntology.getCustomPropertyTypes().add(contactInfoPropertyType);
        builder = XstreamPackageOntologyBuilderFactory.newInstance();

    }


    @Test
    public void testRoundTrip() {
        builder = XstreamPackageOntologyBuilderFactory.newInstance();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        builder.buildOntology(bogusOntology, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        PackageOntology ontology = builder.buildOntology(bais);
        assertEquals(ontology.getArtifactTypes(), bogusOntology.getArtifactTypes());
        for (String type : ontology.getArtifactTypes()) {
            assertEquals(ontology.getProperties(type), bogusOntology.getProperties(type));
            assertEquals(ontology.getId(), bogusOntology.getId());
            assertEquals(ontology.getRelationships(type), bogusOntology.getRelationships(type));
        }
        assertEquals(bogusOntology, ontology);
        Set<PackageOntology.Relationship> rels = ontology.getRelationships(COLLECTION);
        for (PackageOntology.Relationship rel : rels) {
            if (rel.getName().equals("isMemberOf") || rel.getName().equals("hasMember")) {
                assertTrue(rel.isHierarchical());
            }
        }
    }

    @Test
    public void createDCSBOOntologyDoc() {
        PackageOntology dcsBoOntology = DcsBoPackageOntology.getInstance();
        builder = XstreamPackageOntologyBuilderFactory.newInstance();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        builder.buildOntology(dcsBoOntology, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        PackageOntology ontology = builder.buildOntology(bais);

        assertEquals(ontology, dcsBoOntology);
    }


}
