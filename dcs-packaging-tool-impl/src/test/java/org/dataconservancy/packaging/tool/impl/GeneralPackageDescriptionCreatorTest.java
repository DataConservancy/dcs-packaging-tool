/*
 * Copyright 2014 Johns Hopkins University
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
 * 
 */

package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.packaging.tool.api.PackageDescriptionCreator;
import org.dataconservancy.packaging.tool.api.PackageDescriptionCreatorException;
import org.dataconservancy.packaging.tool.model.DcsPackageDescriptionSpec;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionRulesBuilder;
import org.dataconservancy.packaging.tool.model.builder.xstream.JaxbPackageDescriptionRulesBuilder;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GeneralPackageDescriptionCreatorTest {

    private static PackageDescriptionCreator creator;

    /* Directories that should be projects */
    private static final List<String> ROOT_COLLECTION_PATHS = Arrays.asList("");

    /* Directories that should be empty collections */
    private static final List<String> EMPTY_COLLECTION_PATHS = Arrays
            .asList("empty_collection");

    /* Directories that should be collections */
    private static final List<String> COLLECTION_PATHS = Arrays
            .asList("collection1",
                    "collection2",
                    "empty_collection",
                    "collection2/subcollection2.0",
                    "collection2/subcollection2.1");

    /* Directories that are collections, but also subcollections */
    private static final List<String> SUBCOLLECTION_PATHS = Arrays
            .asList("collection2/subcollection2.0",
                    "collection2/subcollection2.1");

    /* Directories that should be DataItems */
    private static final List<String> DATA_ITEM_PATHS = Arrays
            .asList("collection1/dataItem1.0",
                    "collection1/dataItem1.1",
                    "collection2/subcollection2.0/dataItem2.0.0",
                    "collection2/subcollection2.1/dataItem2.1.0");

    /* Files that should be DataFiles */
    private static final List<String> DATA_FILE_PATHS = Arrays
            .asList("collection1/dataItem1.0/dataFile1.0.0",
                    "collection1/dataItem1.0/dataFile1.0.1",
                    "collection1/dataItem1.0/dataFile1.0.2",
                    "collection1/dataItem1.1/dataFile1.1.0",
                    "collection1/dataItem1.1/dataFile1.1.1",
                    "collection2/subcollection2.0/dataItem2.0.0/dataFile2.0.0",
                    "collection2/subcollection2.0/dataItem2.0.0/dataFile2.0.1",
                    "collection2/subcollection2.1/dataItem2.1.0/dataFile2.1.0");

    /* Files that should be MetadataFiles */
    private static final List<String> METADATA_FILE_PATHS = Arrays
            .asList("metadata_for_project.txt",
                    "collection1/metadataFile1.0",
                    "collection2/metadataFile2.0");

    private static final List<String> DOT_PATHS = Arrays
            .asList(".dotfile", ".dotdirectory", ".dotdirectory/excluded.txt");

    private static PackageDescription desc;

    private static File rootArtifactDir;

    private static String packageOntologyIdentifier = "ontologyIdentifier";

    @ClassRule
    public static TemporaryFolder tmpfolder = new TemporaryFolder();
    
    @BeforeClass
    public static void setUp() throws Exception {
        InputStream zipInputStream =
                GeneralPackageDescriptionCreatorTest.class
                        .getClassLoader()
                        .getResourceAsStream("GeneralPackageDescriptionCreator.zip");
        File temp =
                tmpfolder.newFolder("GeneralPackageDescriptionCreatorTest");

        File zipFile =
                tmpfolder.newFile("GeneralPackageDescriptionCreatorTest.zip");
        
        OutputStream zipOutputStream = new FileOutputStream(zipFile);

        IOUtils.copy(zipInputStream, zipOutputStream);
        zipOutputStream.close();
        zipInputStream.close();

        ZipFile zip = new ZipFile(zipFile);
        zip.extractAll(temp.getPath());

        rootArtifactDir = new File(temp, "content");
        if (!rootArtifactDir.isDirectory()) {
            throw new RuntimeException();
        }

        /*
         * OK, now that we have the content directory, load the rules and create
         * a creator
         */
        InputStream rulesStream =
                GeneralPackageDescriptionCreatorTest.class.getClassLoader()
                        .getResourceAsStream("rules/default-rules.xml");

        PackageDescriptionRulesBuilder builder =
                new JaxbPackageDescriptionRulesBuilder();

        creator =
                new GeneralPackageDescriptionCreator(builder.buildPackageDescriptionRules(rulesStream));

        desc = creator.createPackageDescription(packageOntologyIdentifier, rootArtifactDir);
    }

    /* Verify that PackageDescription is well-formed */
    @Test
    public void validationTest() throws Exception {
        PackageDescriptionValidator validator =
                new PackageDescriptionValidator();
        validator.validate(desc);
    }

    /* verify that we can create the same description twice in a row. */
    @Test
    public void repeatSamePackageDescriptionCreationTest()
            throws PackageDescriptionCreatorException {
        PackageDescription firstDesc =
                creator.createPackageDescription(packageOntologyIdentifier, rootArtifactDir);
        assertNotNull(firstDesc);
        assertEquals(firstDesc, creator.createPackageDescription(packageOntologyIdentifier, rootArtifactDir));
    }

    /*
     * Verify that the root directory results in a package artifact (as per the
     * rules)
     */
    @Test
    public void rootProjectTest() {
        for (File file : pathsToFiles(ROOT_COLLECTION_PATHS)) {
            PackageArtifact artifact = artifactFor(file);
            assertEquals(DcsPackageDescriptionSpec.ArtifactType.Collection.toString(),
                         artifact.getType());

            /* Make sure that artifact's isByteStream flag was set to false. */
            assertFalse(artifact.isByteStream());

        }
    }

    /* Verify that empty directories are created as empty collections */
    @Test
    public void emptyCollectionTest() {
        for (File file : pathsToFiles(EMPTY_COLLECTION_PATHS)) {
            PackageArtifact artifact = artifactFor(file);
            assertEquals(DcsPackageDescriptionSpec.ArtifactType.Collection.toString(),
                         artifact.getType());
            /* Make sure that artifact's isByteStream flag was set to false. */
            assertFalse(artifact.isByteStream());
        }
    }

    /* Verify that directories that contain directories are collections */
    @Test
    public void collectionTest() {
        for (File file : pathsToFiles(COLLECTION_PATHS)) {
            PackageArtifact artifact = artifactFor(file);
            assertEquals(DcsPackageDescriptionSpec.ArtifactType.Collection.toString(),
                         artifact.getType());

            /* Make sure that artifact's isByteStream flag was set to false. */
            assertFalse(artifact.isByteStream());

        }
    }

    /*
     * Verify that collections under the project directory are members of the
     * project
     */
    @Test
    public void collectionProjectTest() {

        List<File> subcollections = pathsToFiles(SUBCOLLECTION_PATHS);
        for (File file : pathsToFiles(COLLECTION_PATHS)) {

            /* We don't care about subcollections */
            if (!subcollections.contains(file)) {

                PackageArtifact artifact = artifactFor(file);

                /*
                 * Make sure it has an isMemberOf relationship. (Name of
                 * relationship is determined by ontology.)
                 */
                assertTrue(artifact
                        .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF) != null);

                /* ... and make sure it points to the project */
                assertTrue(artifact
                        .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF)
                        .getTargets().contains(artifactFor(rootArtifactDir).getId()));
            }
        }
    }

    /* verify that sub-collections are members of the parent collection */
    @Test
    public void subCollectionTest() {
        for (File file : pathsToFiles(SUBCOLLECTION_PATHS)) {
            PackageArtifact artifact = artifactFor(file);

            /* Make sure it has an agg by project relationship */
            assertTrue(artifact
                    .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF) != null);

            /* Make sure it's to the parent */
            assertEquals(file.getParentFile().toURI().toString(), artifact
                    .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF)
                    .getTargets().iterator().next());
        }
    }

    /*
     * Verify that files within a project or collection directory are metadata
     * files for that project or collection
     */
    @Test
    public void metadataFilesTest() {
        for (File file : pathsToFiles(METADATA_FILE_PATHS)) {
            PackageArtifact artifact = artifactFor(file);

            assertEquals(DcsPackageDescriptionSpec.ArtifactType.MetadataFile.toString(),
                         artifact.getType());

            /* Make sure that artifact's isByteStream flag was set to true. */
            assertTrue(artifact.isByteStream());

            assertTrue(artifact
                    .getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR) != null);

            /* Make sure it's to the parent */
            assertEquals(file.getParentFile().toURI().toString(),
                         artifact.getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR)
                                 .getTargets().iterator().next());
        }
    }

    /*
     * Verify that folders containing only files are DataItems, and members of
     * the parent collection
     */
    @Test
    public void dataItemTest() {
        for (File file : pathsToFiles(DATA_ITEM_PATHS)) {
            PackageArtifact artifact = artifactFor(file);

            assertEquals(DcsPackageDescriptionSpec.ArtifactType.DataItem.toString(),
                         artifact.getType());

            /* Make sure that artifact's isByteStream flag was set to false. */
            assertFalse(artifact.isByteStream());

            assertTrue(artifact
                    .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF) != null);

            /* Make sure it's a member of the parent */
            assertEquals(file.getParentFile().toURI().toString(), artifact
                    .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF)
                    .getTargets().iterator().next());
        }
    }

    /*
     * Verify that files within dataItem folders are DataFiles within that
     * DataItem
     */
    @Test
    public void dataFileTest() {
        for (File file : pathsToFiles(DATA_FILE_PATHS)) {
            PackageArtifact artifact = artifactFor(file);

            assertEquals(DcsPackageDescriptionSpec.ArtifactType.DataFile.toString(),
                         artifact.getType());
            /* Make sure that artifact's isByteStream flag was set to true. */
            assertTrue(artifact.isByteStream());

            assertTrue(artifact
                    .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF) != null);

            /* Make sure it's a member of the parent */
            assertEquals(file.getParentFile().toURI().toString(), artifact
                    .getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF)
                    .getTargets().iterator().next());
        }
    }

    /*
     * Verify that excluded files (dotfiles, and stuff in dot directories) are
     * excluded
     */
    @Test
    public void exclusionTest() {
        for (File file : pathsToFiles(DOT_PATHS)) {
            PackageArtifact artifact = artifactFor(file);
            assertNotNull(artifact);
            assertTrue(artifact.isIgnored());
        }
    }

    /* Verify that symlink cycles can be detected */
    @Test
    public void simLinkCycleTest() throws Exception {
        File tempDir = tmpfolder.newFolder("moo");

        File subdir = new File(tempDir, "cow");
        subdir.mkdir();

        Path link = Paths.get(subdir.getPath(), "link");
        link.toFile().deleteOnExit();

        try {
            Files.createSymbolicLink(link, subdir.toPath());
        } catch (UnsupportedOperationException e) {
            /* Nothing we can do if the system doesn't support symlinks */
            FileUtils.deleteDirectory(tempDir);
            return;
        }

        try {
            creator.createPackageDescription(packageOntologyIdentifier,tempDir);
            Assert.fail("Expected symbolic link cycle to cause an exception");
        } catch (PackageDescriptionCreatorException e) {
            /* Expected */
            // clean up just in case other tests create the same dirs/files
            FileUtils.deleteDirectory(tempDir);
        }

    }

    /* Verify that symlink cycles can be detected */
    //TODO: These setReadable false tests don't work on windows for now the test only runs if that operation succeeded.
    @Test
    public void nonreadableFileTest() throws Exception {
        File tempDir = tmpfolder.newFolder("moo");

        File subdir = new File(tempDir, "cow");
        subdir.mkdir();
                
        if (subdir.setReadable(false)) {

            try {
                creator.createPackageDescription(packageOntologyIdentifier,tempDir);
                Assert.fail("Expected a non-readable directory to cause an exception");
            } catch (PackageDescriptionCreatorException e) {
                /* Expected */
                // clean up just in case other tests create the same dirs/files. Can't use
                // FileUtils due to readable false
                subdir.delete();
                tempDir.delete();
            }
        }

    }

    /* Verify that DataItem+DataFile files are sane (DC-1717) */
    @Test
    public void dataItemPlusDataFileTest() throws URISyntaxException {
        Map<String, PackageArtifact> parentDataItems =
                new HashMap<String, PackageArtifact>();
        List<PackageArtifact> childDataFiles = new ArrayList<PackageArtifact>();

        for (PackageArtifact artifact : desc.getPackageArtifacts()) {
            if (artifact.getArtifactRef().getRefString().contains("impliedData")) {
                if (artifact.getType().equals("DataItem")) {
                    parentDataItems.put(artifact.getId(), artifact);
                    URI idUri = new URI(artifact.getId());
                    assertNotNull(idUri.getFragment());
                    assertNotNull(artifact.getArtifactRef().getFragment());
                    assertFalse(artifact.isByteStream());
                } else {
                    childDataFiles.add(artifact);
                    assertTrue(artifact.isByteStream());
                    artifact.setByteStream(false);
                }
            }
        }

        assertEquals(childDataFiles.size(), 3);
        for (PackageArtifact child : childDataFiles) {
            Set<String> targets =
                    child.getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF)
                            .getTargets();
            assertEquals(1, targets.size());
            
            assertTrue(parentDataItems.keySet().contains(targets.iterator()
                    .next()));
            URI idUri = new URI(child.getId());
            assertNull(idUri.getFragment());
            assertNull(child.getArtifactRef().getFragment());

        }
    }

    private static List<File> pathsToFiles(List<String> paths) {
        List<File> files = new ArrayList<File>(paths.size());

        for (String path : paths) {
            files.add(new File(rootArtifactDir, path
                    .replace('/', File.separatorChar)));
        }

        return files;
    }

    private static PackageArtifact artifactFor(File file) {
        for (PackageArtifact artifact : desc.getPackageArtifacts()) {
            if (artifact.getArtifactRef().getResolvedAbsoluteRefString(rootArtifactDir.getParentFile()).equals(file.getPath())) {
                return artifact;
            }
        }

        return null;
    }

}
