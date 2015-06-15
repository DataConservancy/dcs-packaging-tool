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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.impl.generator.OrePackageModelBuilder;
import org.dataconservancy.packaging.tool.impl.generator.mocks.FunctionalAssemblerMock;
import org.dataconservancy.packaging.tool.model.DcsPackageDescriptionSpec.ArtifactType;
import org.dataconservancy.packaging.tool.model.DcsPackageDescriptionSpec.Relationship;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.support.RelationshipConstraintChecker;
import org.dataconservancy.packaging.tool.support.RelationshipTypeChecker;
import org.dataconservancy.packaging.tool.support.ResourceMapExtractor;
import org.dataconservancy.packaging.validation.OrphanResourceChecker;
import org.dataconservancy.packaging.validation.RDFXMLSyntaxChecker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


public class ProducedPackageValidationTest {

    private PackageValidationPipeline packageValidationPipeline;
    
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    private File baseDir;
    
    @Before
    public void setup() {
        //TODO: Run package builder to create package and set packageLocation to path
        
        baseDir = tmpfolder.newFolder("base");
        packageValidationPipeline = new PackageValidationPipeline(baseDir);
    }

    @Test
    public void testPackageIsValid() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();

        PackageAssembler assembler = new FunctionalAssemblerMock(baseDir);

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact subcollection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);
        PackageArtifact metadataFile = newArtifact(ArtifactType.MetadataFile);

        File content = tmpfolder.newFile("batman.jpg");

        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());
        
        content = tmpfolder.newFile("boring man");

        IOUtils.write("test", new FileOutputStream(content));
        metadataFile.setArtifactRef(content.toURI().toString());

        addRel(Relationship.IS_MEMBER_OF.toString(),
               project,
               collection);
        addRel(Relationship.IS_MEMBER_OF.toString(),
               project,
               subcollection);
        addRel(Relationship.IS_MEMBER_OF.toString(),
               collection,
               subcollection);
        addRel(Relationship.IS_MEMBER_OF.toString(),
               collection,
               dataItem);
        // This appears not to be in the ontology yet.
        //addRel("isDocumentationFor",
        //       collection,
        //       dataItem);
        addRel(Relationship.IS_METADATA_FOR.toString(),
               subcollection,
               metadataFile);
        addRel(Relationship.IS_MEMBER_OF.toString(),
               dataItem,
               dataFile);

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project, collection, subcollection, dataItem, dataFile, metadataFile));
        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);

        try {
            packageValidationPipeline.validatePackage(builder.getPackageRemURI());
        } catch (Exception e) {
            throw e;
        }

    }

    static class PackageValidationPipeline {

        private final OrphanResourceChecker orphanChecker;

        private final RDFXMLSyntaxChecker syntaxChecker;

        private final RelationshipConstraintChecker constraintChecker;

        private final RelationshipTypeChecker relationshipTypeChecker;

        private final ResourceMapExtractor resourceMapExtractor;
                
        private final File baseDir;

        PackageValidationPipeline(File baseDir) {
            orphanChecker = new OrphanResourceChecker();
            syntaxChecker = new RDFXMLSyntaxChecker();
            constraintChecker = new RelationshipConstraintChecker();
            relationshipTypeChecker = new RelationshipTypeChecker();
            resourceMapExtractor = new ResourceMapExtractor();
            this.baseDir = baseDir;
        }

        List<String> validatePackage(URI packageLocation) throws Exception {
            List<String> errorMessages = new ArrayList<String>();

            if (packageLocation == null || baseDir == null) {
                //TODO: Handle setup being bad.
            } else {

                Model model = ModelFactory.createDefaultModel();
                Resource resmap =
                        model.createResource(packageLocation.toString());
                syntaxChecker.validateResourceMap(resmap, baseDir, model);
                orphanChecker.checkForOrphanResources(packageLocation, baseDir);

                Map<String, AttributeSet> attributeMap = null;
                attributeMap =
                        resourceMapExtractor.execute(baseDir, packageLocation);

                if (attributeMap != null) {
                    constraintChecker.validate("", attributeMap);

                    relationshipTypeChecker.validate("", attributeMap);
                }
            }
            return errorMessages;
        }
    }

    private PackageArtifact newArtifact(final ArtifactType type) {
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId("_" + Math.random());
        artifact.setArtifactRef("/" + Math.random());
        artifact.setType(type.toString());

        return artifact;
    }

    private Set<PackageArtifact> asSet(PackageArtifact... artifacts) {
        Set<PackageArtifact> asSet = new HashSet<PackageArtifact>();
        Collections.addAll(asSet, artifacts);

        return asSet;
    }

    private void addRel(String relName,
                        PackageArtifact to,
                        PackageArtifact from) {
        if (from.getRelationshipByName(relName) == null) {
            from.getRelationships().add(new PackageRelationship(relName, true, new HashSet<String>()));
        }

        from.getRelationshipByName(relName).getTargets().add(to.getId());
    }
}