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

package org.dataconservancy.packaging.tool.impl.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.dcs.model.AttributeSetName;
import org.dataconservancy.dcs.model.Metadata;
import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.impl.generator.mocks.FunctionalAssemblerMock;
import org.dataconservancy.packaging.tool.model.DcsPackageDescriptionSpec.ArtifactType;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageOntology;
import org.dataconservancy.packaging.tool.model.PackageOntology.Property;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.dataconservancy.packaging.tool.support.ResourceMapExtractor;
import org.dspace.foresite.Aggregation;
import org.dspace.foresite.OREParser;
import org.dspace.foresite.OREParserFactory;
import org.dspace.foresite.ResourceMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 */
public class OrePackageModelBuilderTest {
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    List<String> simplePropertiesToTest = Arrays
            .asList(DcsBoPackageOntology.ID,
                    DcsBoPackageOntology.ALTERNATE_ID,
                    DcsBoPackageOntology.DISCIPLINE,
                    DcsBoPackageOntology.PUBLISHER,
                    DcsBoPackageOntology.PUBLICATION_DATE,
                    DcsBoPackageOntology.CREATE_DATE,
                    DcsBoPackageOntology.MODIFIED_DATE,
                    DcsBoPackageOntology.CITABLE_LOCATOR,
                    DcsBoPackageOntology.TITLE,
                    DcsBoPackageOntology.DESCRIPTION,
                    DcsBoPackageOntology.FORMAT);

    List<String> propertyGroupsToTest = Arrays
            .asList(DcsBoPackageOntology.CONTACT_INFO,
                    DcsBoPackageOntology.CREATOR);

    /*
     * Can't figure out how to get the ontology service to spit out these
     * results, so we just use a list here
     */
    List<String> propertyGroupPropertiesToTest = Arrays
            .asList(DcsBoPackageOntology.PERSON_NAME,
                    DcsBoPackageOntology.PHONE,
                    DcsBoPackageOntology.EMAIL,
                    DcsBoPackageOntology.PAGE);

    /*
     * Verify that the project ReM is created, has an aggregation, and is given
     * to the assembler in the correct location.
     */
    @Test
    public void packageRemTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler = mock(PackageAssembler.class);

        final URI REM_URI = URI.create("file:///tmp/test.xml");

        /*
         * This will (should) be called when the builder reserves the package
         * ReM. Check the path and contents for correctness when this happens.
         */
        when(assembler.reserveResource(anyString(),
                                       eq(PackageResourceType.METADATA)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.startsWith("/ORE-REM"));
                    assertTrue(path.endsWith(".xml"));

                    return REM_URI;
                });

        doAnswer(invocation -> {
            ResourceMap rem =
                    parse((InputStream) invocation.getArguments()[1]);
            Aggregation agg = rem.getAggregation();
            assertNotNull(agg);
            return null;
        }).when(assembler).putResource(eq(REM_URI), any(InputStream.class));

        builder.buildModel(mock(PackageDescription.class), assembler);

        verify(assembler).reserveResource(anyString(),
                                          eq(PackageResourceType.METADATA));
        verify(assembler).putResource(eq(REM_URI), any(InputStream.class));

        assertEquals(REM_URI, builder.getPackageRemURI());
    }

    @Test
    public void projectRemTest() {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler = mock(PackageAssembler.class);

        when(assembler.reserveResource(anyString(),
                                       eq(PackageResourceType.METADATA)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.startsWith("/ORE-REM"));
                    assertTrue(path.endsWith(".xml"));

                    return uriFor(path);
                });

        doAnswer(invocation -> {
            ResourceMap rem =
                    parse((InputStream) invocation.getArguments()[1]);
            Aggregation agg = rem.getAggregation();
            assertNotNull(agg);

            //TODO: What should go in here?  Commenting out
            /*
             * if (agg.getTypes()
             * .contains(ResourceMapConstants.DC_PACKAGE_TYPE)) { }
             */
            return null;
        }).when(assembler).putResource(any(URI.class), any(InputStream.class));

        PackageDescription desc = new PackageDescription();

        PackageArtifact project = newArtifact(ArtifactType.Project);

        desc.setPackageArtifacts(asSet(project));
        desc.setRootArtifactRef(project.getArtifactRef());
        
        builder.buildModel(desc, assembler);

        verify(assembler, times(2))
                .reserveResource(anyString(), eq(PackageResourceType.METADATA));
        verify(assembler, times(2)).putResource(any(URI.class),
                                                any(InputStream.class));
    }

    @Test
    public void collectionRemTest() {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler = mock(PackageAssembler.class);

        when(assembler.reserveResource(anyString(),
                                       eq(PackageResourceType.METADATA)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.startsWith("/ORE-REM"));
                    assertTrue(path.endsWith(".xml"));

                    return uriFor(path);
                });

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, project, collection);

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project, collection));
        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);

        verify(assembler, times(3))
                .reserveResource(anyString(), eq(PackageResourceType.METADATA));
        verify(assembler, times(3)).putResource(any(URI.class),
                                                any(InputStream.class));
    }

    @Test
    public void dataItemRemTest() {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler = mock(PackageAssembler.class);

        when(assembler.reserveResource(anyString(),
                                       eq(PackageResourceType.METADATA)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.startsWith("/ORE-REM"));
                    assertTrue(path.endsWith(".xml"));

                    return uriFor(path);
                });

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, project, collection);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project, collection, dataItem));
        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);

        verify(assembler, times(4))
                .reserveResource(anyString(), eq(PackageResourceType.METADATA));
        verify(assembler, times(4)).putResource(any(URI.class),
                                                any(InputStream.class));
    }

    @Test
    public void dataFileTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler = mock(PackageAssembler.class);

        when(assembler.reserveResource(anyString(),
                                       eq(PackageResourceType.METADATA)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.startsWith("/ORE-REM"));
                    assertTrue(path.endsWith(".xml"));

                    return uriFor(path);
                });

        when(assembler.createResource(anyString(),
                                      eq(PackageResourceType.DATA),
                                      any(InputStream.class)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.endsWith("tst"));

                    return uriFor(path);
                });

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);
        PackageArtifact dataFileExplicitPath =
                newArtifact(ArtifactType.DataFile);

        /* Add a file with no explicit path specified */
        File content = tmpfolder.newFile("dataFileTest.tst");
        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());

        /* Add another file, this time with an explicit path */
        content = tmpfolder.newFile("dataFileTest2.tst");

        IOUtils.write("test", new FileOutputStream(content));
        dataFileExplicitPath.setArtifactRef(content.toURI().toString());
        //dataFileExplicitPath.addSimplePropertyValue(Property.FILE_PATH.toString(),
        //        EXPLICIT_FILE_PATH);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, project, collection);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, dataItem, dataFile);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF,
               dataItem,
               dataFileExplicitPath);

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project,
                                       collection,
                                       dataItem,
                                       dataFile,
                                       dataFileExplicitPath));

        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);

        verify(assembler, times(2))
                .createResource(anyString(),
                                eq(PackageResourceType.DATA),
                                any(InputStream.class));
        //verify(assembler).createResource(eq(EXPLICIT_FILE_PATH),
        //                                 eq(PackageResourceType.DATA),
        //                                 any(InputStream.class));
        verify(assembler, times(4))
                .reserveResource(anyString(), eq(PackageResourceType.METADATA));
        verify(assembler, times(4)).putResource(any(URI.class),
                                                any(InputStream.class));
    }

    @Test
    public void metadataFileTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler = mock(PackageAssembler.class);

        when(assembler.reserveResource(anyString(),
                                       eq(PackageResourceType.METADATA)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.startsWith("/ORE-REM"));
                    assertTrue(path.endsWith(".xml"));

                    return uriFor(path);
                });

        when(assembler.createResource(anyString(),
                                      eq(PackageResourceType.DATA),
                                      any(InputStream.class)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.endsWith("tst"));

                    return uriFor(path);
                });

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact metadataFile = newArtifact(ArtifactType.MetadataFile);

        /* Add a file with no explicit path specified */
        File content = tmpfolder.newFile("dataFileTest32.tst");

        IOUtils.write("test", new FileOutputStream(content));
        metadataFile.setArtifactRef(content.toURI().toString());

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, project, collection);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);
        addRel(DcsBoPackageOntology.IS_METADATA_FOR, collection, metadataFile);

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project,
                                       collection,
                                       dataItem,
                                       metadataFile));

        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);

        verify(assembler, times(1))
                .createResource(anyString(),
                                eq(PackageResourceType.DATA),
                                any(InputStream.class));
        verify(assembler, times(4))
                .reserveResource(anyString(), eq(PackageResourceType.METADATA));
        verify(assembler, times(4)).putResource(any(URI.class),
                                                any(InputStream.class));
        
        // TODO Must actually test that an isMetadataFor relationship is produced.
    }

    /* DataFiles without relationships to anything else should throw an error */
    @Test
    public void orphanDataFileTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler =
                new FunctionalAssemblerMock(tmpfolder.newFolder("build"));

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);

        File content = tmpfolder.newFile("dataFileTest123.tst");
        
        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, project, collection);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);
        /* We intentionally do not add the DATA_FILE_IS_PART_OF_DATA_ITEM rel */

        PackageDescription desc = new PackageDescription();
        desc.setRootArtifactRef(project.getArtifactRef());
        desc.setPackageArtifacts(asSet(project, collection, dataItem, dataFile));

        try {
            builder.buildModel(desc, assembler);
            Assert.fail("An exception should have been thrown due to missing data file relationship");
        } catch (Exception e) {
        }
    }

    @Test(expected = Exception.class)
    public void badPropertyTest() {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler =
                new FunctionalAssemblerMock(tmpfolder.newFolder("clover"));

        PackageArtifact project = newArtifact(ArtifactType.Project);
        project.addSimplePropertyValue("This is not a valid kety", "whatever");
        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project));
        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);
    }

    @Test
    public void propertyExtractionTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        File baseDir = tmpfolder.newFolder("destiny");
        PackageAssembler assembler = new FunctionalAssemblerMock(baseDir);

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);
        PackageArtifact metadataFile = newArtifact(ArtifactType.MetadataFile);

        addRandomPropertiesTo(project);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, project, collection);
        addRandomPropertiesTo(collection);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);
        addRandomPropertiesTo(dataItem);

        addRel(DcsBoPackageOntology.IS_METADATA_FOR, collection, metadataFile);
        addRandomPropertiesTo(metadataFile);
        File metaContent = tmpfolder.newFile("dataFileTest12.tst");
        
        IOUtils.write("test", new FileOutputStream(metaContent));
        metadataFile.setArtifactRef(metaContent.toURI().toString());

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, dataItem, dataFile);
        addRandomPropertiesTo(dataFile);
        File content = tmpfolder.newFile("cow");

        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project,
                                       collection,
                                       dataItem,
                                       dataFile,
                                       metadataFile));
        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);

        ResourceMapExtractor extractor = new ResourceMapExtractor();

        Map<String, AttributeSet> attrs =
                extractor.execute(baseDir, builder.getPackageRemURI());

        Set<String> extractedValues = new HashSet<>();

        for (AttributeSet attSet : attrs.values()) {
            for (Attribute att : attSet.getAttributes()) {
                extractedValues.add(att.getValue());
            }
        }

        for (PackageArtifact artifact : asSet(project,
                                              collection,
                                              dataItem,
                                              dataFile,
                                              metadataFile)) {

            for (String key : artifact.getPropertyNames()) {
                if (artifact.hasSimpleProperty(key)) {
                    for (String value : artifact.getSimplePropertyValues(key)) {
                        assertTrue("Missing value for property " + key,
                                   extractedValues.contains(value));
                    }
                } else if (artifact.hasPropertyValueGroup(key)) {
                    artifact.getPropertyValueGroups(key);
                    for (PropertyValueGroup group : artifact
                            .getPropertyValueGroups(key)) {
                        for (String subKey : group.getSubPropertyNames()) {
                            for (String value : group
                                    .getSubPropertyValues(subKey)) {
                                assertTrue(String.format("Missing value for property %s->%s",
                                                         key,
                                                         subKey),
                                           extractedValues.contains(value));
                            }
                            assertTrue(extractedValues.containsAll(group
                                    .getSubPropertyValues(subKey)));
                        }
                    }
                } else {
                    Assert.fail("No value for property " + key);
                }
            }

        }
    }
    
    /* Verify that external relationships work */
    @Test
    public void externalRelationshipExtractionTest() throws Exception{
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        File baseDir = tmpfolder.newFolder("grass");
        PackageAssembler assembler = new FunctionalAssemblerMock(baseDir);

        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, dataItem, dataFile);
        File content = tmpfolder.newFile("pastures");

        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());
        
        final String EXTERNAL_REL = "http://arbitrary/rel";
        final String EXTERNAL_REL_VALUE = "http://external/target";
        
        addExternalRel(collection, EXTERNAL_REL, EXTERNAL_REL_VALUE);
        
        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(
                                       collection, dataItem, dataFile));
        desc.setRootArtifactRef(collection.getArtifactRef());
        builder.buildModel(desc, assembler);

        ResourceMapExtractor extractor = new ResourceMapExtractor();

        Map<String, AttributeSet> attrs =
                extractor.execute(baseDir, builder.getPackageRemURI());

        Set<String> extractedValues = new HashSet<>();

        for (AttributeSet attSet : attrs.values()) {
            for (Attribute att : attSet.getAttributes()) {
                extractedValues.add(att.getValue());
            }
        }
        
        assertTrue(extractedValues.contains(EXTERNAL_REL_VALUE));
    }
    
    /* verify that a mixture of internal and external relationships using ths same predicate works */
    @Test
    public void mixedRelationshipExtractionTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        File baseDir = tmpfolder.newFolder("flowers");
        PackageAssembler assembler = new FunctionalAssemblerMock(baseDir);

        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, dataItem, dataFile);
        File content = tmpfolder.newFile("barn.tst");

        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());
        
        final String REL = "http://arbitrary/rel";
        final String EXTERNAL_REL_VALUE = "http://external/target";

        addExternalRel(collection, REL, EXTERNAL_REL_VALUE);
        addRel(REL, dataFile, collection);
        
        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(
                                       collection, dataItem, dataFile));
        desc.setRootArtifactRef(collection.getArtifactRef());
        builder.buildModel(desc, assembler);

        ResourceMapExtractor extractor = new ResourceMapExtractor();

        Map<String, AttributeSet> attrs =
                extractor.execute(baseDir, builder.getPackageRemURI());

        Set<String> extractedValues = new HashSet<>();

        for (AttributeSet attSet : attrs.values()) {
            for (Attribute att : attSet.getAttributes()) {
                if (att.getName().contains(REL)) {
                    extractedValues.add(att.getValue());
                }
            }
        }
        
        assertEquals(2, extractedValues.size());
        assertTrue(extractedValues.contains(EXTERNAL_REL_VALUE));
        // The URI of the dataItem isn't apparent, so we assume that it's the other entry
        // in the extracted values set
        
    }

    /* Tests that ignored artifacts are still inspected by the builder, but add resource is not called.
     * This ensures that files are not added if they are ignored.
     */
    @Test
    public void ignoredArtifactStillCheckedTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        PackageAssembler assembler = mock(PackageAssembler.class);

        when(assembler.reserveResource(anyString(),
                                       eq(PackageResourceType.METADATA)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.startsWith("/ORE-REM"));
                    assertTrue(path.endsWith(".xml"));

                    return uriFor(path);
                });

        when(assembler.createResource(anyString(),
                                      eq(PackageResourceType.DATA),
                                      any(InputStream.class)))
                .thenAnswer(invocation -> {

                    String path = (String) invocation.getArguments()[0];

                    assertTrue(path.endsWith("tst"));

                    return uriFor(path);
                });

        PackageArtifact project = newArtifact(ArtifactType.Project);
        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        dataItem.setIgnored(true);
        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);
        dataFile.setIgnored(true);

        /* Add a file */
        File content = tmpfolder.newFile("save me");
        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());

        //dataFileExplicitPath.addSimplePropertyValue(Property.FILE_PATH.toString(),
        //        EXPLICIT_FILE_PATH);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, project, collection);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, dataItem, dataFile);

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(project,
                                       collection,
                                       dataItem,
                                       dataFile));

        desc.setRootArtifactRef(project.getArtifactRef());
        builder.buildModel(desc, assembler);

        verify(assembler, times(0))
                .createResource(anyString(),
                                eq(PackageResourceType.DATA),
                                any(InputStream.class));
        //verify(assembler).createResource(eq(EXPLICIT_FILE_PATH),
        //                                 eq(PackageResourceType.DATA),
        //                                 any(InputStream.class));
        verify(assembler, times(3))
                .reserveResource(anyString(), eq(PackageResourceType.METADATA));
        verify(assembler, times(3)).putResource(any(URI.class),
                                                any(InputStream.class));
    }

    /*
     * Tests that if resources are ignored they are not put in the REM.
     */
    @Test
    public void testIgnoredArtifactAreLeftOutOfREM() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        File baseDir = tmpfolder.newFolder("moocow");
        PackageAssembler assembler = new FunctionalAssemblerMock(baseDir);

        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PackageArtifact ignoredDataItem = newArtifact(ArtifactType.DataItem);
        ignoredDataItem.setIgnored(true);
        PackageArtifact ignoredDataFile = newArtifact(ArtifactType.DataFile);
        ignoredDataFile.setIgnored(true);

        PackageArtifact unignoredDataItem = newArtifact(ArtifactType.DataItem);
        PackageArtifact unignoredDataFile = newArtifact(ArtifactType.DataFile);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, ignoredDataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, ignoredDataItem, ignoredDataFile);

        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, unignoredDataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, unignoredDataItem, unignoredDataFile);

        File content = tmpfolder.newFile("batman");

        IOUtils.write("test", new FileOutputStream(content));
        unignoredDataFile.setArtifactRef(content.toURI().toString());

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(collection, ignoredDataItem, ignoredDataFile, unignoredDataFile, unignoredDataItem));
        desc.setRootArtifactRef(collection.getArtifactRef());
        builder.buildModel(desc, assembler);

        ResourceMapExtractor extractor = new ResourceMapExtractor();

        Map<String, AttributeSet> attrs =
                extractor.execute(baseDir, builder.getPackageRemURI());

        int collectionCount = 0;
        int dataItemCount = 0;
        int dataFileCount = 0;

        for (AttributeSet attSet : attrs.values()) {
            if (attSet.getName().contains(AttributeSetName.ORE_REM_COLLECTION)) {
                collectionCount++;
            }

            if (attSet.getName().contains(AttributeSetName.ORE_REM_DATAITEM)) {
                dataItemCount++;
            }

            if (attSet.getName().contains(AttributeSetName.ORE_REM_FILE)) {
                dataFileCount++;
            }
        }

        assertEquals(1, collectionCount);
        assertEquals(1, dataItemCount);
        assertEquals(1, dataFileCount);
    }

    /* verify that a mixture of internal and external relationships using ths same predicate works */
    @Test
    public void multipleCreatorsTest() throws Exception {
        OrePackageModelBuilder builder = new OrePackageModelBuilder();
        File baseDir = tmpfolder.newFolder("moocow");
        PackageAssembler assembler = new FunctionalAssemblerMock(baseDir);

        PackageArtifact collection = newArtifact(ArtifactType.Collection);
        PropertyValueGroup creator1 = new PropertyValueGroup();
        creator1.addSubPropertyValue(DcsBoPackageOntology.PERSON_NAME, "creator 1");
        creator1.addSubPropertyValue(DcsBoPackageOntology.EMAIL, "mailTo:creator1@email.com");
        creator1.addSubPropertyValue(DcsBoPackageOntology.PHONE, "tel:5405854152");
        PropertyValueGroup creator2 = new PropertyValueGroup();
        creator1.addSubPropertyValue(DcsBoPackageOntology.PERSON_NAME, "creator 2");
        creator1.addSubPropertyValue(DcsBoPackageOntology.EMAIL, "mailTo:creator2@email.com");
        creator1.addSubPropertyValue(DcsBoPackageOntology.PHONE, "tel:5408888888");
        PropertyValueGroup creator3 = new PropertyValueGroup();
        creator1.addSubPropertyValue(DcsBoPackageOntology.PERSON_NAME, "creator 3");
        creator1.addSubPropertyValue(DcsBoPackageOntology.EMAIL, "mailTo:creator3@email.com");
        creator1.addSubPropertyValue(DcsBoPackageOntology.PHONE, "tel:5405555555");
        collection.addPropertyValueGroup(DcsBoPackageOntology.CREATOR, creator1);
        collection.addPropertyValueGroup(DcsBoPackageOntology.CREATOR, creator2);
        collection.addPropertyValueGroup(DcsBoPackageOntology.CREATOR, creator3);

        PackageArtifact dataItem = newArtifact(ArtifactType.DataItem);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, collection, dataItem);

        PackageArtifact dataFile = newArtifact(ArtifactType.DataFile);
        addRel(DcsBoPackageOntology.IS_MEMBER_OF, dataItem, dataFile);
        File content = tmpfolder.newFile("catman");

        IOUtils.write("test", new FileOutputStream(content));
        dataFile.setArtifactRef(content.toURI().toString());

        PackageDescription desc = new PackageDescription();
        desc.setPackageArtifacts(asSet(collection,dataItem,dataFile));
        desc.setRootArtifactRef(collection.getArtifactRef());
        builder.buildModel(desc, assembler);

        ResourceMapExtractor extractor = new ResourceMapExtractor();

        Map<String, AttributeSet> attrs =
                extractor.execute(baseDir, builder.getPackageRemURI());

        //The implementation of resource map extractor used in this tests put a creator's name, phone and email address
        //in separate attributes making it impossible to group them in the correct grouping under a specific creator.
        //However, to test that more than one creator's information was expressed in the ReM, this test will verify
        // the count of each attributes: 3 of creator's names, emails, phone numbers
        int countCreatorName = 0;
        int countCreatorEmail = 0;
        int countCreatorPhone = 0;
        for (AttributeSet attSet : attrs.values()) {
            for (Attribute att : attSet.getAttributes()) {
                if (att.getName().contains(Metadata.CREATOR_NAME)) {
                    countCreatorName++;
                } else if (att.getName().contains(Metadata.CREATOR_EMAIL)) {
                    countCreatorEmail++;
                } else if (att.getName().contains(Metadata.CREATOR_PHONE)) {
                    countCreatorPhone++;
                }
            }
        }
        assertEquals(3, countCreatorEmail);
        assertEquals(3, countCreatorPhone);
        assertEquals(3, countCreatorName);
    }

    private static ResourceMap parse(InputStream input) throws Throwable {

        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        return parser.parse(input);
    }

    /* Just to deal with the annoying interface */
    private PackageArtifact newArtifact(final ArtifactType type) {
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId("_" + Math.random());
        artifact.setArtifactRef("/" + Math.random());
        artifact.setType(type.toString());

        return artifact;
    }

    private URI uriFor(String path) {
        return URI.create("file:///" + path);
    }

    private Set<PackageArtifact> asSet(PackageArtifact... artifacts) {
        Set<PackageArtifact> asSet = new HashSet<>();
        Collections.addAll(asSet, artifacts);

        return asSet;
    }

    private void addRel(String rel, PackageArtifact to, PackageArtifact from) {
        PackageRelationship relationship = from.getRelationshipByName(rel);
        if ( relationship == null) {
            relationship = new PackageRelationship(rel, true, new HashSet<>());
            from.getRelationships().add(relationship);
        }

        relationship.getTargets().add(to.getId());
    }
    
    private void addExternalRel(PackageArtifact subject, String rel, String target) {
        PackageRelationship relationship = subject.getRelationshipByName(rel);
        if (relationship == null) {
            relationship = new PackageRelationship(rel, true, new HashSet<>());
            subject.getRelationships().add(relationship);
        }
        
        relationship.getTargets().add(target);
    }

    private void addRandomPropertiesTo(PackageArtifact artifact) {
        PackageOntology ontology = DcsBoPackageOntology.getInstance();

        List<String> validProperties = new ArrayList<>();

        for (Property p : ontology.getProperties(artifact.getType())) {
            validProperties.add(p.getName());
        }

        for (String property : simplePropertiesToTest) {
            if (validProperties.contains(property)) {
                artifact.addSimplePropertyValue(property,
                                                String.format("Value-%f",
                                                              Math.random()));
            }
        }

        for (String property : propertyGroupsToTest) {
            PropertyValueGroup group = new PropertyValueGroup();
            if (validProperties.contains(property)) {
                for (String subProperty : propertyGroupPropertiesToTest) {
                    group.addSubPropertyValue(subProperty,
                                              String.format("Value:%f",
                                                            Math.random()));
                }
            }
            artifact.addPropertyValueGroup(property, group);
        }
    }
}
