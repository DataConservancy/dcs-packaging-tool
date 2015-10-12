/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.dataconservancy.packaging.validation.PackageValidationException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PackageDescriptionValidatorTest {

    private static final String SPEC = "spec";

    private static final String TYPE_1 = "type1";

    private static final String TYPE_2 = "type2";

    private AtomicInteger idSource = new AtomicInteger();

    private final PackageDescriptionValidator validator =
            new PackageDescriptionValidator();

    private static File TMP_DIR;

    @ClassRule
    public static TemporaryFolder tmpfolder = new TemporaryFolder();
    

    @BeforeClass
    public static void setup() throws IOException {
        TMP_DIR = tmpfolder.newFolder("test");
    }
    
    /* Verify validation of package spec id */
    @Test
    public void missingSpecTest() throws PackageValidationException {
        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(new HashSet<PackageArtifact>());

        desc.getPackageArtifacts().add(newArtifact(TYPE_1));

        /* Should fail with no spec */
        try {
            validator.validate(desc);
            fail("Lack of spec id passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        desc.setPackageOntologyIdentifier(SPEC);

        /* Should work now */
        validator.validate(desc);
    }

    /* verify Every artifact must have an ID */
    @Test
    public void missingArtifactIdTest() throws PackageValidationException {
        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact = newArtifact(TYPE_1);
        desc.getPackageArtifacts().add(artifact);

        /* Should fail with no id */
        artifact.setId(null);
        try {
            validator.validate(desc);
            fail("null artifact id passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Should fail with empty id */
        artifact.setId("");
        try {
            validator.validate(desc);
            fail("empty artifact id passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Give it a value, and it should work now */
        artifact.setId("any_ID");
        validator.validate(desc);
    }

    /* verify All artifact IDs must be unique */
    @Test
    public void duplicateIdentifierTest() throws PackageValidationException {
        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact1 = newArtifact(TYPE_1);
        PackageArtifact artifact2 = newArtifact(TYPE_2);
        desc.getPackageArtifacts().add(artifact1);
        desc.getPackageArtifacts().add(artifact2);

        /* Duplicate id should fail */
        artifact2.setId(artifact1.getId());
        try {
            validator.validate(desc);
            fail("Duplicate ID passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Give it a unique id, and it should pass */
        artifact2.setId("New_ID");
        validator.validate(desc);
    }

    /* verify Artifact type must be defined */
    @Test
    public void artifactTypeDefinedTest() throws PackageValidationException {
        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact = newArtifact(null);
        desc.getPackageArtifacts().add(artifact);

        /* Absence of type should fail */
        try {
            validator.validate(desc);
            fail("Missing type passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Fixing the type should pass */
        artifact.setType(TYPE_1);
        validator.validate(desc);
    }

    /* verify artifact ref is a valid, resolvable, protocol-based URI */
/*    @Test
    public void artifactRefTest() throws PackageValidationException {
        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact = newArtifact(TYPE_1);
        desc.getPackageArtifacts().add(artifact);
*/
        /* Incomprehensible artifact ref should fail */

/*        try {
            artifact.setArtifactRef("urn:cannot_be_resolved");
            //next line should never be reached, as the construction of the artifactRef should fail
            validator.validate(desc);
            fail("non resolvable artifact ref passed validation");
        } catch (IllegalArgumentException e) {
            /* expected */
//        }

        /* Resolvable ref should pass */
/*        artifact.setArtifactRef("http://dataconservancy.org");
        validator.validate(desc);
    }
*/
    @Test
    public void nullArtifactRefTest() throws PackageValidationException {
        PackageDescription desc = newPackageDescription();
        PackageArtifact artifact = new PackageArtifact();

        desc.getPackageArtifacts().add(artifact);

        artifact.setId(Integer.toString(idSource.incrementAndGet()));
        artifact.setType(TYPE_1);
        try {
            validator.validate(desc);
            fail("null artifact ref passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

    }

    /* Verify properties are sane */
    @Test
    public void propertiesTest() throws PackageValidationException {
        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact = newArtifact(TYPE_1);
        desc.getPackageArtifacts().add(artifact);

        /* Null properties should fail */
        artifact.setSimplePropertyValues(null, null, null);
        
        try {
            validator.validate(desc);
            fail("null properties passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Properties with no name should fail */
        artifact.addSimplePropertyValue(null, "value");
        try {
            validator.validate(desc);
            fail("null property name passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Properties with a name, but null value, are fine */
        artifact.removeAllProperties();
        artifact.addSimplePropertyValue("Some_Key", null);
        validator.validate(desc);
    }

    /* Verify relationships are sane, */
    @Test
    public void relationshipsTest() throws PackageValidationException {
        final String REL_NAME = "Rel";

        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact = newArtifact(TYPE_1);

        desc.getPackageArtifacts().add(artifact);

        /* Null rels should fail */
        
        artifact.setRelationships((List<PackageRelationship>)null);        

        try {
            validator.validate(desc);
            fail("null relationships passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        artifact.setRelationships();

        /* Null relationship name should fail */
        artifact.getRelationships().add(new PackageRelationship(null, true, "value"));
        try {
            validator.validate(desc);
            fail("null relationship name passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Null relationship targets should fail */
        artifact.getRelationships().clear();
        artifact.getRelationships().add(new PackageRelationship(REL_NAME, true, (Set<String>)null));
        try {
            validator.validate(desc);
            fail("null relationship targets passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Empty relationship targets should fail */
        artifact.getRelationships().add(new PackageRelationship(REL_NAME, true, new HashSet<String>()));
        try {
            validator.validate(desc);
            fail("Empty relationship targets passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }

        /* Null relationship target id should fail */
        artifact.getRelationships().clear();
        artifact.getRelationships().add(new PackageRelationship(REL_NAME, true, new HashSet<String>()));
        artifact.getRelationshipByName(REL_NAME).getTargets().add(null);
        try {
            validator.validate(desc);
            fail("null relationship target id passed validation");
        } catch (PackageValidationException e) {
            /* expected */
        }
    }

    @Test
    public void relationshipTargetTest_NonHierarchicalRels() throws PackageValidationException {
        final String REL_NAME = "Rel";

        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact1 = newArtifact(TYPE_1);
        PackageArtifact artifact2 = newArtifact(TYPE_2);
        PackageArtifact artifact3 = newArtifact(TYPE_2);

        desc.getPackageArtifacts().add(artifact1);
        desc.getPackageArtifacts().add(artifact2);
        desc.getPackageArtifacts().add(artifact3);

        /* Correct relationship should pass */
        addRel(REL_NAME, artifact2, artifact1);
        addRel(REL_NAME, artifact3, artifact1);
        validator.validate(desc);

        /*
         * Adding an ID of the target of the relationship to something not in
         * the package description should fail
         */
        artifact1.getRelationshipByName(REL_NAME).getTargets().add("NOT_IN_DESC");
        validator.validate(desc);
    }

    @Test (expected = PackageValidationException.class)
    public void relationshipTargetTest_HierarchicalRels() throws PackageValidationException {

        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact1 = newArtifact(TYPE_1);
        PackageArtifact artifact2 = newArtifact(TYPE_2);
        PackageArtifact artifact3 = newArtifact(TYPE_2);

        desc.getPackageArtifacts().add(artifact1);
        desc.getPackageArtifacts().add(artifact2);
        desc.getPackageArtifacts().add(artifact3);

        /* Correct relationship should pass */
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, artifact2, artifact1);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, artifact3, artifact1);
        validator.validate(desc);

        /*
         * Adding an ID of the target of the relationship to something not in
         * the package description should fail
         */
        artifact1.getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF).getTargets().add("NOT_IN_DESC");
        validator.validate(desc);
    }

    private PackageDescription newPackageDescription() {
        PackageDescription desc = new PackageDescription();
        desc.setPackageOntologyIdentifier(SPEC);
        desc.setPackageArtifacts(new HashSet<PackageArtifact>());

        return desc;
    }

    /* Safely add a relationship from one artifact to another. */
    private void addRel(String rel, PackageArtifact to, PackageArtifact from) {
        PackageRelationship relationship = from.getRelationshipByName(rel);
        if (relationship == null) {
            relationship = new PackageRelationship(rel, true, new HashSet<String>());
            from.getRelationships().add(relationship);
        }

        relationship.getTargets().add(to.getId());
    }

    /* Helper to create new, identified artifacts. */
    public PackageArtifact newArtifact(final String type) {
        PackageArtifact artifact = new PackageArtifact();

        artifact.setId(Integer.toString(idSource.incrementAndGet()));
        artifact.setType(type);

        artifact.setArtifactRef(TMP_DIR.toURI().toString());

        return artifact;
    }


    @Test (expected = PackageValidationException.class)
    public void testSubPropertyNameInvalid() throws PackageValidationException {
        PackageDescription desc = newPackageDescription();

        PackageArtifact artifact1 = newArtifact(TYPE_1);
        PackageArtifact.PropertyValueGroup group = new PackageArtifact.PropertyValueGroup();
        group.addSubPropertyValue("", "bogus spocus");
        group.addSubPropertyValue("name", "cookie");
        group.addSubPropertyValue("phone", "coffee");

        artifact1.addPropertyValueGroup("group1", group);

        desc.getPackageArtifacts().add(artifact1);

        validator.validate(desc);
    }
}
