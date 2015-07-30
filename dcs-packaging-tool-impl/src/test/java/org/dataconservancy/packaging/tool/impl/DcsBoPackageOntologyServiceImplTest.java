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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageNode;
import org.dataconservancy.packaging.tool.model.PackageOntologyException;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.PackageTree;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

public class DcsBoPackageOntologyServiceImplTest {
    private static PackageRelationship mdF2TestRelationship;

    PackageArtifact datafile1;
    PackageArtifact datafile2a;
    PackageArtifact datafile2b;
    PackageArtifact metadataFile1;
    PackageArtifact metadataFile2;
    PackageArtifact metadataFile3;
    PackageArtifact dataItem1;
    PackageArtifact dataItem2;
    PackageArtifact dataItem3;
    PackageArtifact dataItem4;
    PackageArtifact collection1;
    PackageArtifact collection2;
    PackageArtifact collection3;
    PackageArtifact collection4;
    PackageArtifact collection5;
    PackageArtifact project;

    PackageDescription pd;

    File contentRoot = new File("/tmp");

    PackageTree tree;
    DcsBoPackageOntologyServiceImpl underTest;

    String filePathString = "some/file/path";
    /**
     * Set up Service with hierarchical relationship names.
     * Set up a package description in the following structure
     *
     * project
     * |-collection1
     *      |-dataitem1
     *          |-datafile1
     *          |-metadatafile1
     *      |-dataitem2
     *          |-datafile2a
     *          |-datafile2b
     *      |collection3
     *          |dataitem4
     *          |collection4
     * |-collection2
     *      |-dataItem3
     * |-collection5
     *      |-metadatafile2
     *      |-metadatafile3
     *
     */
    @Before
    public void setUp() {
        underTest = new DcsBoPackageOntologyServiceImpl();

        datafile1 = new PackageArtifact();
        datafile1.setId("id:df1");
        datafile1.setArtifactRef(filePathString);
        datafile1.setType(DcsBoPackageOntology.DATAFILE);
        datafile1.setByteStream(true);
        datafile1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di1"));

        metadataFile1 = new PackageArtifact();
        metadataFile1.setId("id:mdf1");
        metadataFile1.setArtifactRef(filePathString);
        metadataFile1.setType(DcsBoPackageOntology.METADATAFILE);
        metadataFile1.setByteStream(true);
        metadataFile1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, "id:di1"));

        metadataFile2 = new PackageArtifact();
        metadataFile2.setId("id:mdf2");
        metadataFile2.setArtifactRef(filePathString);
        metadataFile2.setType(DcsBoPackageOntology.METADATAFILE);
        metadataFile2.setByteStream(true);
        metadataFile2.setSimplePropertyValues(DcsBoPackageOntology.CREATE_DATE, "2014-04-12");
        metadataFile2.setSimplePropertyValues(DcsBoPackageOntology.MODIFIED_DATE, "2014-04-12");
        metadataFile2.setSimplePropertyValues(DcsBoPackageOntology.FILE_NAME, "File to be transformed.");
        metadataFile2.setSimplePropertyValues(DcsBoPackageOntology.SIZE, "123456");
        metadataFile2.setSimplePropertyValues(DcsBoPackageOntology.FORMAT, "application/octet-stream");
        PackageRelationship mdF2HierarchicalRel = new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, "id:col5");
        mdF2TestRelationship = new PackageRelationship("http://purl.org/dc/terms/description", true, "id:col5");
        metadataFile2.setRelationships(mdF2HierarchicalRel, mdF2TestRelationship);


        metadataFile3 = new PackageArtifact();
        metadataFile3.setId("id:mdf3");
        metadataFile3.setArtifactRef(filePathString);
        metadataFile3.setType(DcsBoPackageOntology.METADATAFILE);
        metadataFile3.setByteStream(true);
        metadataFile3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, "id:col5"));

        dataItem1 = new PackageArtifact();
        dataItem1.setId("id:di1");
        dataItem1.setArtifactRef(filePathString);
        dataItem1.setType(DcsBoPackageOntology.DATAITEM);
        dataItem1.setByteStream(false);
        dataItem1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));

        datafile2a = new PackageArtifact();
        datafile2a.setId("id:df2a");
        datafile2a.setArtifactRef(filePathString);
        datafile2a.setType(DcsBoPackageOntology.DATAFILE);
        datafile2a.setByteStream(true);
        datafile2a.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di2"));

        datafile2b = new PackageArtifact();
        datafile2b.setId("id:df2b");
        datafile2b.setArtifactRef(filePathString);
        datafile2b.setType(DcsBoPackageOntology.DATAFILE);
        datafile2b.setByteStream(true);
        datafile2b.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di2"));

        dataItem2 = new PackageArtifact();
        dataItem2.setId("id:di2");
        dataItem2.setArtifactRef(filePathString);
        dataItem2.setType(DcsBoPackageOntology.DATAITEM);
        dataItem2.setByteStream(false);
        dataItem2.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));

        collection1 = new PackageArtifact();
        collection1.setId("id:col1");
        collection1.setArtifactRef(filePathString);
        collection1.setType(DcsBoPackageOntology.COLLECTION);
        collection1.setByteStream(false);
        collection1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"));


        dataItem3 = new PackageArtifact();
        dataItem3.setId("id:di3");
        dataItem3.setArtifactRef(filePathString);
        dataItem3.setType(DcsBoPackageOntology.DATAITEM);
        dataItem3.setByteStream(false);
        dataItem3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col2"));

        collection2 = new PackageArtifact();
        collection2.setId("id:col2");
        collection2.setArtifactRef(filePathString);
        collection2.setType(DcsBoPackageOntology.COLLECTION);
        collection2.setByteStream(false);
        collection2.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"));

        collection3 = new PackageArtifact();
        collection3.setId("id:col3");
        collection3.setArtifactRef(filePathString);
        collection3.setType(DcsBoPackageOntology.COLLECTION);
        collection3.setByteStream(false);
        collection3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));

        dataItem4 = new PackageArtifact();
        dataItem4.setId("id:di4");
        dataItem4.setArtifactRef(filePathString);
        dataItem4.setType(DcsBoPackageOntology.DATAITEM);
        dataItem4.setByteStream(false);
        dataItem4.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col3"));


        collection4 = new PackageArtifact();
        collection4.setId("id:col4");
        collection4.setArtifactRef(filePathString);
        collection4.setType(DcsBoPackageOntology.COLLECTION);
        collection4.setByteStream(false);
        collection4.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col3"));

        collection5 = new PackageArtifact();
        collection5.setId("id:col5");
        collection5.setArtifactRef(filePathString);
        collection5.setType(DcsBoPackageOntology.COLLECTION);
        collection5.setByteStream(false);
        collection5.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"), new PackageRelationship(DcsBoPackageOntology.HAS_METADATA, true, metadataFile2.getId(), metadataFile3.getId()));


        project = new PackageArtifact();
        project.setId("id:project");
        project.setArtifactRef(filePathString);
        project.setType(DcsBoPackageOntology.PROJECT);
        project.setByteStream(false);

        pd = new PackageDescription();
        Set<PackageArtifact> packageArtifacts = new HashSet<PackageArtifact>();
        packageArtifacts.add(datafile1);
        packageArtifacts.add(metadataFile1);
        packageArtifacts.add(metadataFile2);
        packageArtifacts.add(metadataFile3);
        packageArtifacts.add(datafile2a);
        packageArtifacts.add(datafile2b);
        packageArtifacts.add(dataItem1);
        packageArtifacts.add(dataItem2);
        packageArtifacts.add(dataItem3);
        packageArtifacts.add(collection1);
        packageArtifacts.add(collection2);
        packageArtifacts.add(collection3);
        packageArtifacts.add(collection4);
        packageArtifacts.add(collection5);
        packageArtifacts.add(dataItem4);
        packageArtifacts.add(project);

        pd.setPackageArtifacts(packageArtifacts);
    }

    /**
     * Test that resulted PackageNode has the same hierarchical structure to the provided PackageDescription.
     * @throws PackageOntologyException 
     */
    @Test
    public void testBuildPackageTree() throws PackageOntologyException{
        tree = underTest.buildPackageTree(pd, contentRoot);
        //test that the Root node of the tree contains the project.
        assertEquals(project, tree.getRoot().getValue());
        PackageNode col1Node = null;
        PackageNode col2Node = null;
        PackageNode col5Node = null;
        assertEquals(3, tree.getRoot().getChildrenNodes().size());
        //fetch project node and assert it is the only child of the containing root node
        PackageNode resultProject = tree.getRoot();
        assertNotNull(resultProject);
        assertEquals(DcsBoPackageOntology.PROJECT, resultProject.getValue().getType());

        //assert that project has 3, and only 3, nodes, which are collection1, collection2  and collection5
        assertEquals(3, resultProject.getChildrenNodes().size());
        for (PackageNode node : resultProject.getChildrenNodes()) {
            assertTrue(node.getValue().equals(collection1) || node.getValue().equals(collection2) || node.getValue().equals(collection5));
            if (node.getValue().equals(collection1)) {
                col1Node = node;
            } else if (node.getValue().equals(collection2)) {
                col2Node = node;
            } else if (node.getValue().equals(collection5)) {
                col5Node = node;
            }
        }

        /***********************************************
         * Test for collection2
         ***********************************************/

        //assert that collection2 has one child that is dataitem3
        assertEquals(1, col2Node.getChildrenNodes().size());
        assertEquals(dataItem3, col2Node.getChildrenNodes().iterator().next().getValue());

        //assert that collection2 parent is the node containing project
        assertEquals(project, col2Node.getParentNode().getValue());


        /***********************************************
         * Test for collection3
         * ***********************************************/
        PackageNode di1Node = null;
        PackageNode di2Node = null;
        PackageNode col3Node = null;
        assertEquals(3, col1Node.getChildrenNodes().size());

        //assert that collection1 parent is the node containing project
        assertEquals(project, col1Node.getParentNode().getValue());
        for (PackageNode node : col1Node.getChildrenNodes()) {
            assertTrue(node.getValue().equals(dataItem2) || node.getValue().equals(dataItem1)
                    || node.getValue().equals(collection3));
            if (node.getValue().equals(dataItem1)) {
                di1Node = node;
            } else if (node.getValue().equals(dataItem2)) {
                di2Node = node;
            } else if (node.getValue().equals(collection3)) {
                col3Node = node;
            }
        }

        assertEquals(2, col3Node.getChildrenNodes().size());
        for (PackageNode node : col3Node.getChildrenNodes()) {
            assertTrue(node.getValue().equals(dataItem4) || node.getValue().equals(collection4));
        }

        assertEquals(2, di1Node.getChildrenNodes().size());
        for (PackageNode node : di1Node.getChildrenNodes()) {
            assertTrue(node.getValue().equals(datafile1) || node.getValue().equals(metadataFile1));
        }
        //assert that dataitem1 parent is the node containing collection1
        assertEquals(collection1, di1Node.getParentNode().getValue());
        assertEquals(2, di2Node.getChildrenNodes().size());
        //assert that dataitem2 parent is the node containing collection1
        assertEquals(collection1, di2Node.getParentNode().getValue());
        for (PackageNode node : di2Node.getChildrenNodes()) {
            assertTrue(node.getValue().equals(datafile2a) || node.getValue().equals(datafile2b));
        }


        /***********************************************
         * Test for collection5
         ***********************************************/
        //assert that collection5 has two children that is metadatafile3 and metadatafile2
        assertEquals(2, col5Node.getChildrenNodes().size());
        for (PackageNode node : col5Node.getChildrenNodes()) {
            assertTrue(node.getValue().equals(metadataFile3) || node.getValue().equals(metadataFile2));
        }

        //assert that collection5 parent is the node containing project
        assertEquals(project, col5Node.getParentNode().getValue());

        assertEquals(16, tree.getNodesMap().size());

    }

    @Test
    public void testSetRelationshipsToParentByString() throws PackageOntologyException {
        underTest = new DcsBoPackageOntologyServiceImpl();
        testBuildPackageTree();
    }

    /**
     * Test valid types is returned.
     * <p/>
     * Case: changing types in the scenario that DataFile does not have to be switched to MetadataFile or vice versa
     * Dependent on the exact structure of the tree built in the setUp method.
     */
    @Test
    public void testGetValidTypeWithoutChangingFileType() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        Set<String> validTypes = underTest.getValidTypes(tree, collection4.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("Collection"));
        assertTrue(validTypes.contains("DataItem"));

        validTypes = underTest.getValidTypes(tree, collection1.getId());
        assertEquals(1, validTypes.size());
        assertTrue(validTypes.contains("Collection"));

        validTypes = underTest.getValidTypes(tree, collection3.getId());
        assertEquals(1, validTypes.size());
        assertTrue(validTypes.contains("Collection"));

        validTypes = underTest.getValidTypes(tree, metadataFile1.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("DataFile"));
        assertTrue(validTypes.contains("MetadataFile"));

        validTypes = underTest.getValidTypes(tree, dataItem4.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("Collection"));
        assertTrue(validTypes.contains("DataItem"));

        validTypes = underTest.getValidTypes(tree, project.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("Collection"));
        assertTrue(validTypes.contains("Project"));

    }

    /**
     * Test valid types is returned.
     * <p/>
     * Case: changing types in the scenario that DataFile has the potential to be switched to MetadataFile
     * Dependent on the exact structure of the tree built in the setUp method.
     */
    @Test
    public void testGetValidTypeChangingFileTypeFromDataFileToMetadataFile() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        Set<String> validTypes = underTest.getValidTypes(tree, dataItem2.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("Collection"));
        assertTrue(validTypes.contains("DataItem"));
    }

    /**
     * Test valid types is returned
     * <p/>
     * Case: A MdF of a collection can either be a MetadataFile or a DI+DF combo.
     * @throws PackageOntologyException
     */
    @Test
    public void testsGetValidTypesForMdF() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        Set<String> validTypes = underTest.getValidTypes(tree, metadataFile2.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("MetadataFile"));
        assertTrue(validTypes.contains(DcsBoPackageOntologyServiceImpl.didfComboType));
    }
    /**
     * Test valid types is returned
     * <p/>
     * Case: A MdF of a DataItem canNOT a DI+DF combo.
     * @throws PackageOntologyException
     */
    @Test
    public void testsGetValidTypesForDIMdF() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        Set<String> validTypes = underTest.getValidTypes(tree, metadataFile1.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("MetadataFile"));
        assertTrue(validTypes.contains("DataFile"));
    }

    /**
     * Test valid types is returned.
     * <p/>
     * Case: changing types in the scenario that MetadataFile has the potential to be switched to DataFile
     * Dependent on the exact structure of the tree built in the setUp method.
     */
    @Test
    public void testGetValidTypeChangingFileTypeFromMetadataFileToDataFile() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        Set<String> validTypes = underTest.getValidTypes(tree, dataItem1.getId());
        assertEquals(2, validTypes.size());
        assertTrue(validTypes.contains("Collection"));
        assertTrue(validTypes.contains("DataItem"));
    }


    /**
     * Test changing type for Collections
     */
    @Test
    public void testChangeType() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        //Change dataItem2 to a collection
        underTest.changeType(pd, tree, dataItem2, contentRoot, DcsBoPackageOntology.COLLECTION);

        assertEquals(DcsBoPackageOntology.COLLECTION, tree.getNodesMap().get(dataItem2.getId()).getValue().getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, tree.getNodesMap().get(datafile2a.getId()).getValue().getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, tree.getNodesMap().get(datafile2b.getId()).getValue().getType());
        assertEquals(tree.getNodesMap().get(dataItem2.getId()).getValue(),
                tree.getNodesMap().get(datafile2a.getId()).getParentNode().getValue());

        assertNotNull(tree.getNodesMap().get(datafile2a.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR));
        Set<String> relatedArtifactForDf2a = tree.getNodesMap().get(datafile2a.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR).getTargets();
        assertNotNull(relatedArtifactForDf2a);
        assertTrue(relatedArtifactForDf2a.contains(dataItem2.getId()));

        assertNotNull(tree.getNodesMap().get(datafile2b.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR));
        Set<String> relatedArtifactForDf2b = tree.getNodesMap().get(datafile2b.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR).getTargets();
        assertNotNull(relatedArtifactForDf2b);
        assertTrue(relatedArtifactForDf2b.contains(dataItem2.getId()));


        //Change dataItem1 to a collection
        underTest.changeType(pd, tree, dataItem1, contentRoot, DcsBoPackageOntology.COLLECTION);

        assertEquals(DcsBoPackageOntology.COLLECTION, tree.getNodesMap().get(dataItem1.getId()).getValue().getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, tree.getNodesMap().get(datafile1.getId()).getValue().getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, tree.getNodesMap().get(metadataFile1.getId()).getValue().getType());
        assertEquals(tree.getNodesMap().get(dataItem1.getId()).getValue(),
                tree.getNodesMap().get(datafile1.getId()).getParentNode().getValue());

        assertNotNull(tree.getNodesMap().get(datafile1.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR));

        Set<String> relatedArtifactForDf1 = tree.getNodesMap().get(datafile1.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR).getTargets();
        assertNotNull(relatedArtifactForDf1);
        assertTrue(relatedArtifactForDf1.contains(dataItem1.getId()));

        assertNotNull(tree.getNodesMap().get(datafile2a.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR));

        Set<String> relatedArtifactForMdf1 = tree.getNodesMap().get(metadataFile1.getId()).getValue().getRelationshipByName(DcsBoPackageOntology.IS_METADATA_FOR).getTargets();
        assertNotNull(relatedArtifactForMdf1);
        assertTrue(relatedArtifactForMdf1.contains(dataItem1.getId()));

    }

    /**
     * Test changing a Datafile to a MetadataFile
     *
     * @throws PackageOntologyException
     */
    @Test
    public void testChangeTypeForDataFile() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        underTest.changeType(pd, tree, datafile2a, contentRoot, DcsBoPackageOntology.METADATAFILE);
        assertEquals(DcsBoPackageOntology.METADATAFILE, tree.getNodesMap().get(datafile2a.getId()).getValue().getType());
    }

    /**
     * Test changing a MetadataFile to a DataFile
     *
     * @throws PackageOntologyException
     */
    @Test
    public void testChangeTypeForMetadataFile() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        underTest.changeType(pd, tree, metadataFile1, contentRoot, DcsBoPackageOntology.DATAFILE);
        assertEquals(DcsBoPackageOntology.DATAFILE, tree.getNodesMap().get(metadataFile1.getId()).getValue().getType());
    }

    /**
     * Test changing a Collection to a DataItem
     *
     * @throws PackageOntologyException
     */
    @Test
    public void testChangeTypeForCollection() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        underTest.changeType(pd, tree, collection4, contentRoot, DcsBoPackageOntology.DATAITEM);
        assertEquals(DcsBoPackageOntology.DATAITEM, tree.getNodesMap().get(collection4.getId()).getValue().getType());
    }

    /**
     * Test changing a Collection to a DataItem
     *
     * @throws PackageOntologyException
     */
    @Test
    public void testChangeTypeForDataItemAndBack() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        underTest.changeType(pd, tree, dataItem2, contentRoot, DcsBoPackageOntology.COLLECTION);
        assertEquals(DcsBoPackageOntology.COLLECTION, tree.getNodesMap().get(dataItem2.getId()).getValue().getType());
        for (PackageNode node : tree.getNodesMap().get(dataItem2.getId()).getChildrenNodes()) {
            assertEquals(DcsBoPackageOntology.METADATAFILE, node.getValue().getType());
        }

        underTest.changeType(pd, tree, dataItem2, contentRoot, DcsBoPackageOntology.DATAITEM);
        assertEquals(DcsBoPackageOntology.DATAITEM, tree.getNodesMap().get(dataItem2.getId()).getValue().getType());
        for (PackageNode node : tree.getNodesMap().get(dataItem2.getId()).getChildrenNodes()) {
            assertEquals(DcsBoPackageOntology.DATAFILE, node.getValue().getType());
        }
    }

    /**
     * Test changing a Collection's MdF to a Combo of DI + DF
     * @throws PackageOntologyException
     */
    @Test
    public void testChangeTypeForCollectionsMdF() throws PackageOntologyException, URISyntaxException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        int originalNodesMapSize = tree.getNodesMap().size();
        String originalFileUri = metadataFile2.getArtifactRef().getRefString();
        underTest.changeType(pd, tree, metadataFile2, contentRoot, DcsBoPackageOntologyServiceImpl.didfComboType);
        verifyFilesTransformationToDIDF(originalNodesMapSize, originalFileUri);
    }

    @Test
    public void testPointMdFToGrandparent() throws PackageOntologyException, URISyntaxException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        int originalNodesMapSize = tree.getNodesMap().size();
        String originalFileUri = metadataFile2.getArtifactRef().getRefString();
        underTest.changeType(pd, tree, metadataFile2, contentRoot, DcsBoPackageOntologyServiceImpl.didfComboType);
        verifyFilesTransformationToDIDF(originalNodesMapSize, originalFileUri);

        PackageNode grandparentNode = tree.getNodesMap().get(collection5.getId());
        assertEquals(2, grandparentNode.getChildrenNodes().size());
        PackageNode diNode = null;
        //loop through the children nodes of collection find to find the expected dataitem node
        for (PackageNode childNode : grandparentNode.getChildrenNodes()) {
            if (childNode.getValue().getType().equals(DcsBoPackageOntology.DATAITEM)) {
                diNode = childNode;
                break;
            }
        }
        assertNotNull(diNode);
        assertEquals(1, diNode.getChildrenNodes().size());
        PackageNode fileNode = diNode.getChildrenNodes().get(0);
        underTest.changeType(pd, tree, fileNode.getValue(), contentRoot, DcsBoPackageOntology.METADATAFILE);
        underTest.collapseParentArtifact(pd, tree, fileNode.getValue().getId());

        assertEquals(2, grandparentNode.getChildrenNodes().size());
        boolean foundDIArtifact = false;
        assertFalse(grandparentNode.getChildrenNodes().contains(diNode));
        for (PackageNode childNode : grandparentNode.getChildrenNodes()) {
            if (childNode.getValue().getType().equals(DcsBoPackageOntology.DATAITEM)) {
                foundDIArtifact = true;
            }
        }
        assertFalse(foundDIArtifact);
        assertEquals(originalNodesMapSize, tree.getNodesMap().size());
    }

    /**
     * Test attempting to change a DataItem's MdF into DI+DF combo
     * @throws PackageOntologyException
     */
    @Test (expected = PackageOntologyException.class)
    public void testIllegalTypeChangeIntoDIDF() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        underTest.changeType(pd, tree, metadataFile1, contentRoot, DcsBoPackageOntologyServiceImpl.didfComboType);
    }

    @Test(expected = PackageOntologyException.class)
    public void testIllegalTypeChange() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        underTest.changeType(pd, tree, collection1, contentRoot, DcsBoPackageOntology.DATAITEM);
    }

    @Test(expected = PackageOntologyException.class)
    public void testIllegalTypeChangeForCollection() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        underTest.changeType(pd, tree, collection5, contentRoot, DcsBoPackageOntology.DATAITEM);
    }

    @Test
    public void testGetProperties() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        Map<String, String> collectionProperties = underTest.getProperties(collection1);

        assertTrue(collectionProperties.containsKey(DcsBoPackageOntology.ALTERNATE_ID));
        assertEquals(DcsBoPackageOntology.STRING_TYPE, collectionProperties.get(DcsBoPackageOntology.ALTERNATE_ID));

        assertTrue(collectionProperties.containsKey(DcsBoPackageOntology.PUBLICATION_DATE));
        assertEquals(DcsBoPackageOntology.DATETIME_TYPE, collectionProperties.get(DcsBoPackageOntology.PUBLICATION_DATE));

        assertTrue(collectionProperties.containsKey(DcsBoPackageOntology.CREATOR));
        assertEquals(DcsBoPackageOntology.CREATOR_TYPE, collectionProperties.get(DcsBoPackageOntology.CREATOR));

        assertFalse(collectionProperties.containsKey(DcsBoPackageOntology.STORAGE_USED));

        Map<String, String> projectProperties = underTest.getProperties(project);
        assertTrue(projectProperties.containsKey(DcsBoPackageOntology.STORAGE_USED));
        assertEquals(DcsBoPackageOntology.LONG_TYPE, projectProperties.get(DcsBoPackageOntology.STORAGE_USED));
    }

    @Test
    public void testGetMaxOccurances() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        assertEquals(1, underTest.getPropertyMaxOccurrences(collection1, DcsBoPackageOntology.TITLE, ""));
        assertEquals(Integer.MAX_VALUE, underTest.getPropertyMaxOccurrences(datafile1, DcsBoPackageOntology.FORMAT, ""));

        assertEquals(Integer.MAX_VALUE, underTest.getPropertyMaxOccurrences(dataItem1, DcsBoPackageOntology.CREATOR, ""));
        assertEquals(Integer.MAX_VALUE, underTest.getPropertyMaxOccurrences(project, DcsBoPackageOntology.NUMBER, ""));

        //Test getting sub properties of a group
        assertEquals(1, underTest.getPropertyMaxOccurrences(collection1, DcsBoPackageOntology.PERSON_NAME, DcsBoPackageOntology.CONTACT_INFO_TYPE));
        assertEquals(Integer.MAX_VALUE, underTest.getPropertyMaxOccurrences(collection1, DcsBoPackageOntology.PAGE, DcsBoPackageOntology.CREATOR_TYPE));

    }

    @Test
    public void testGetMaxOccurancesBadArtifact() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        //Test a null artifact
        assertEquals(-1, underTest.getPropertyMaxOccurrences(null, DcsBoPackageOntology.CONTACT_INFO, ""));

        //Test an artifact with no type
        PackageArtifact foo = new PackageArtifact();
        assertEquals(-1, underTest.getPropertyMaxOccurrences(foo, DcsBoPackageOntology.ALTERNATE_ID, ""));

        //Test an artifact with a made up type.
        foo.setType("foo");
        assertEquals(-1, underTest.getPropertyMaxOccurrences(foo, DcsBoPackageOntology.CREATOR, ""));

        //Test a null artifact for a sub property still returns
        assertEquals(1, underTest.getPropertyMaxOccurrences(foo, DcsBoPackageOntology.PERSON_NAME, DcsBoPackageOntology.CONTACT_INFO_TYPE));
    }

    @Test
    public void testGetMaxOccurancesBadProperty() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        //Test a property that doesn't exist
        assertEquals(-1, underTest.getPropertyMaxOccurrences(collection1, "foo", ""));

        //Test a property that exists but not on this artifact type.
        assertEquals(-1, underTest.getPropertyMaxOccurrences(collection1, DcsBoPackageOntology.SIZE, ""));

        //Test bad property in a group.
        assertEquals(-1, underTest.getPropertyMaxOccurrences(collection1, DcsBoPackageOntology.NAME, "foo"));
        assertEquals(-1, underTest.getPropertyMaxOccurrences(collection1, "foo", DcsBoPackageOntology.CONTACT_INFO_TYPE));
    }

    @Test
    public void testGetMinOccurances() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        assertEquals(0, underTest.getPropertyMinOccurrences(collection1, DcsBoPackageOntology.TITLE, ""));
        assertEquals(1, underTest.getPropertyMinOccurrences(datafile1, DcsBoPackageOntology.CREATE_DATE, ""));

        assertEquals(1, underTest.getPropertyMinOccurrences(dataItem1, DcsBoPackageOntology.CREATE_DATE, ""));
        assertEquals(1, underTest.getPropertyMinOccurrences(project, DcsBoPackageOntology.NAME, ""));

        //Test getting sub properties of a group
        assertEquals(1, underTest.getPropertyMinOccurrences(collection1, DcsBoPackageOntology.PERSON_NAME, DcsBoPackageOntology.CONTACT_INFO_TYPE));
        assertEquals(0, underTest.getPropertyMinOccurrences(collection1, DcsBoPackageOntology.PAGE, DcsBoPackageOntology.CREATOR_TYPE));

    }

    @Test
    public void testGetMinOccurancesBadArtifact() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        //Test a null artifact
        assertEquals(-1, underTest.getPropertyMinOccurrences(null, DcsBoPackageOntology.CONTACT_INFO, ""));

        //Test an artifact with no type
        PackageArtifact foo = new PackageArtifact();
        assertEquals(-1, underTest.getPropertyMinOccurrences(foo, DcsBoPackageOntology.ALTERNATE_ID, ""));

        //Test an artifact with a made up type.
        foo.setType("foo");
        assertEquals(-1, underTest.getPropertyMinOccurrences(foo, DcsBoPackageOntology.CREATOR, ""));

        //Test a null artifact for a sub property still returns
        assertEquals(1, underTest.getPropertyMinOccurrences(foo, DcsBoPackageOntology.PERSON_NAME, DcsBoPackageOntology.CONTACT_INFO_TYPE));
    }

    @Test
    public void testGetMinOccurancesBadProperty() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        //Test a property that doesn't exist
        assertEquals(-1, underTest.getPropertyMinOccurrences(collection1, "foo", ""));

        //Test a property that exists but not on this artifact type.
        assertEquals(-1, underTest.getPropertyMinOccurrences(collection1, DcsBoPackageOntology.SIZE, ""));

        //Test bad property in a group.
        assertEquals(-1, underTest.getPropertyMinOccurrences(collection1, DcsBoPackageOntology.NAME, "foo"));
        assertEquals(-1, underTest.getPropertyMinOccurrences(collection1, "foo", DcsBoPackageOntology.CONTACT_INFO_TYPE));
    }

    @Test
    public void testGetPropertyGroupNames() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        //Test that contact info returns the correct number.
        assertEquals(3, underTest.getGroupPropertyNames(DcsBoPackageOntology.CONTACT_INFO_TYPE).size());
        assertEquals(4, underTest.getGroupPropertyNames(DcsBoPackageOntology.CREATOR_TYPE).size());

        //Test that string type returns empty set and not null.
        assertNotNull(underTest.getGroupPropertyNames(DcsBoPackageOntology.ALTERNATE_ID));
        assertEquals(0, underTest.getGroupPropertyNames(DcsBoPackageOntology.ALTERNATE_ID).size());

        //Test that a non-existent field returns empty set and not null.
        assertNotNull(underTest.getGroupPropertyNames("foo"));
        assertEquals(0, underTest.getGroupPropertyNames("foo").size());
    }

    @Test
    public void testIsComplexProperty() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);

        //Test that contact info returns true
        assertTrue(underTest.isPropertyComplex(DcsBoPackageOntology.CONTACT_INFO_TYPE));
        assertTrue(underTest.isPropertyComplex(DcsBoPackageOntology.CREATOR_TYPE));

        //Test that string type returns false.
        assertFalse(underTest.isPropertyComplex(DcsBoPackageOntology.ALTERNATE_ID));

        //Test that a non-existent field returns false.
        assertFalse(underTest.isPropertyComplex("foo"));
    }

    @Test
    public void testGetComplexPropertySubPropertyType() {
        assertEquals(DcsBoPackageOntology.STRING_TYPE, underTest.getComplexPropertySubPropertyType(DcsBoPackageOntology.CONTACT_INFO_TYPE, DcsBoPackageOntology.PERSON_NAME));

        assertTrue(underTest.getComplexPropertySubPropertyType("foo", DcsBoPackageOntology.NAME).isEmpty());
    }

    @Test
    public void testIsRelationshipHierarchical() {
        assertTrue(underTest.isRelationshipHierarchical(collection1, DcsBoPackageOntology.HAS_MEMBER));
        assertTrue(underTest.isRelationshipHierarchical(dataItem1, DcsBoPackageOntology.IS_MEMBER_OF));

        assertFalse(underTest.isRelationshipHierarchical(metadataFile1, DcsBoPackageOntology.HAS_MEMBER));
        assertFalse(underTest.isRelationshipHierarchical(collection1, "foo"));
    }

    @Test
    public void testIsSystemSuppliedProperty() {
        assertTrue(underTest.isSystemSuppliedProperty(metadataFile1, DcsBoPackageOntology.SIZE));
        assertFalse(underTest.isSystemSuppliedProperty(collection1, "foo"));
        assertFalse(underTest.isSystemSuppliedProperty(collection1, DcsBoPackageOntology.CONTACT_INFO));
    }

    @Test
    public void testIsInheritableProperty() {
        assertTrue(underTest.isInheritableProperty(collection1, DcsBoPackageOntology.CREATOR));
        assertTrue(underTest.isInheritableProperty(project, DcsBoPackageOntology.PUBLISHER));
        assertFalse(underTest.isInheritableProperty(collection1, "dachshund"));
        assertFalse(underTest.isInheritableProperty(null, DcsBoPackageOntology.CREATOR));
    }

    @Test
    public void testGetKnownRelationshipNames() {
        Set<String> knownRelationshipNames = underTest.getKnownRelationshipNames();
        assertEquals(4, knownRelationshipNames.size());
        assertTrue(knownRelationshipNames.contains(DcsBoPackageOntology.HAS_MEMBER));
        assertTrue(knownRelationshipNames.contains(DcsBoPackageOntology.HAS_METADATA));
        assertTrue(knownRelationshipNames.contains(DcsBoPackageOntology.IS_MEMBER_OF));
        assertTrue(knownRelationshipNames.contains(DcsBoPackageOntology.IS_METADATA_FOR));
    }

    @Test
    public void testGetTypesContainingProperty() {
        Set<String> result = underTest.getArtifactTypesContainProperty(DcsBoPackageOntology.PUBLISHER);
        assertEquals(2, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(result.contains(DcsBoPackageOntology.PROJECT));

        result = underTest.getArtifactTypesContainProperty(DcsBoPackageOntology.CONTACT_INFO);
        assertEquals(2, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(result.contains(DcsBoPackageOntology.DATAITEM));

        result = underTest.getArtifactTypesContainProperty(DcsBoPackageOntology.DESCRIPTION);
        assertEquals(5, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(result.contains(DcsBoPackageOntology.PROJECT));
        assertTrue(result.contains(DcsBoPackageOntology.DATAFILE));
        assertTrue(result.contains(DcsBoPackageOntology.METADATAFILE));
        assertTrue(result.contains(DcsBoPackageOntology.DATAITEM));

        result = underTest.getArtifactTypesContainProperty("Bogus");
        assertEquals(0, result.size());
    }

    @Test
    public void testGetValidChildrenType() {
        Set<String> result = underTest.getValidChildrenTypes(DcsBoPackageOntology.COLLECTION);
        assertEquals(3, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(result.contains(DcsBoPackageOntology.DATAITEM));
        assertTrue(result.contains(DcsBoPackageOntology.METADATAFILE));

        result = underTest.getValidChildrenTypes(DcsBoPackageOntology.PROJECT);
        assertEquals(1, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.COLLECTION));


    }

    @Test
    public void testGetValidDescendants() {

        Set<String> result = underTest.getValidDescendantTypes(DcsBoPackageOntology.COLLECTION);
        assertEquals(4, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(result.contains(DcsBoPackageOntology.DATAITEM));
        assertTrue(result.contains(DcsBoPackageOntology.DATAFILE));
        assertTrue(result.contains(DcsBoPackageOntology.METADATAFILE));

        result = underTest.getValidDescendantTypes(DcsBoPackageOntology.PROJECT);
        assertEquals(4, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(result.contains(DcsBoPackageOntology.DATAITEM));
        assertTrue(result.contains(DcsBoPackageOntology.DATAFILE));
        assertTrue(result.contains(DcsBoPackageOntology.METADATAFILE));

        result = underTest.getValidDescendantTypes(DcsBoPackageOntology.DATAITEM);
        assertEquals(2, result.size());
        assertTrue(result.contains(DcsBoPackageOntology.DATAFILE));
        assertTrue(result.contains(DcsBoPackageOntology.METADATAFILE));

    }

    @Test
    public void testIsDateProperty() {
        assertTrue(underTest.isDateProperty(null, DcsBoPackageOntology.CREATE_DATE));
        assertTrue(underTest.isDateProperty(null, DcsBoPackageOntology.MODIFIED_DATE));
        assertTrue(underTest.isDateProperty(null, DcsBoPackageOntology.PUBLICATION_DATE));
        assertFalse(underTest.isDateProperty(null, DcsBoPackageOntology.NAME));
        assertFalse(underTest.isDateProperty(null, "date"));
        assertFalse(underTest.isDateProperty(null, null));
    }

    @Test
    public void testIsSizeProperty() {
        assertTrue(underTest.isSizeProperty(null, DcsBoPackageOntology.SIZE));
        assertFalse(underTest.isDateProperty(null, DcsBoPackageOntology.NAME));
        assertFalse(underTest.isDateProperty(null, "SIZE"));
        assertFalse(underTest.isDateProperty(null, null));
    }

    @Test
    public void testIsDisciplineProperty() {
        assertTrue(underTest.isDisciplineProperty(null, DcsBoPackageOntology.DISCIPLINE));
        assertFalse(underTest.isDisciplineProperty(null, DcsBoPackageOntology.NAME));
        assertFalse(underTest.isDisciplineProperty(null, "DISCIPLINE"));
        assertFalse(underTest.isDisciplineProperty(null, null));
    }

    /**
     * Tests to make sure Collection's metadata file can be transformed in to DI_DF combo, while DataItem's dataFile and
     * metadata file cannot
     *
     * @throws PackageOntologyException
     */
    @Test
    public void testCanTransformFile() throws PackageOntologyException {
        tree = underTest.buildPackageTree(pd, contentRoot);
        assertTrue(underTest.canBeDataItemFile(tree, metadataFile2.getId()));
        assertFalse(underTest.canBeDataItemFile(tree, datafile1.getId()));
        assertFalse(underTest.canBeDataItemFile(tree, metadataFile1.getId()));
        assertFalse(underTest.canBeDataItemFile(tree, collection2.getId()));
    }

    /**
     * Test that after a metadatafile is transformed into DI+DF the desirable tree structure is resulted.
     *
     * @throws PackageOntologyException
     */
    @Test
    public void testTransformFile() throws PackageOntologyException, URISyntaxException {
        //build tree
        tree = underTest.buildPackageTree(pd, contentRoot);
        int originalNodesMapSize = tree.getNodesMap().size();
        String originalFileUri = metadataFile2.getArtifactRef().getRefString();

        //transform metadataFile2 into DI+DF combo
        underTest.makeDataItemFileCombo(pd, tree, metadataFile2, contentRoot);
        verifyFilesTransformationToDIDF(originalNodesMapSize, originalFileUri);
    }

    /**
     * Test Changing synthesized DI to Collection
     * @throws PackageOntologyException
     * @throws URISyntaxException
     */
    @Test
    public void testGetValidTypesForSynthesizedDI() throws PackageOntologyException, URISyntaxException {
        //build tree
        tree = underTest.buildPackageTree(pd, contentRoot);
        int originalNodesMapSize = tree.getNodesMap().size();
        String originalFileUri = metadataFile2.getArtifactRef().getRefString();

        //transform metadataFile2 into DI+DF combo
        underTest.makeDataItemFileCombo(pd, tree, metadataFile2, contentRoot);
        verifyFilesTransformationToDIDF(originalNodesMapSize, originalFileUri);
        PackageNode diNode = null;
        //Get the containing collection: collection5
        PackageNode node = tree.getNodesMap().get(collection5.getId());
        //make sure it has 2 children
        assertEquals(2, node.getChildrenNodes().size());
        //loop through the children nodes of collection find to find the expected dataitem node
        for (PackageNode childNode : node.getChildrenNodes()) {
            if (childNode.getValue().getType().equals(DcsBoPackageOntology.DATAITEM)) {
                diNode = childNode;
                break;
            }
        }

        Set<String> validTypesForSynthesizedDi = underTest.getValidTypes(tree, diNode.getValue().getId());
        assertEquals(2, validTypesForSynthesizedDi.size());
        assertTrue(validTypesForSynthesizedDi.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(validTypesForSynthesizedDi.contains(DcsBoPackageOntology.DATAITEM));
    }

    private void verifyFilesTransformationToDIDF(int originalNodesMapSize, String originalFileUri) throws URISyntaxException {
        //To hold the expected childnode of the containing collection: DI node
        PackageNode diNode = null;

        //Get the containing collection: collection5
        PackageNode node = tree.getNodesMap().get(collection5.getId());
        //make sure it has 2 children
        assertEquals(2, node.getChildrenNodes().size());
        //loop through the children nodes of collection find to find the expected dataitem node
        for (PackageNode childNode : node.getChildrenNodes()) {
            if (childNode.getValue().getType().equals(DcsBoPackageOntology.DATAITEM)) {
                diNode = childNode;
                break;
            }
        }
        assertNotNull(diNode);
        //verify that the DataItem's ArtifactRef being set properly
        assertTrue(diNode.getValue().getArtifactRef().getRefString().contains(originalFileUri));
        assertNotNull(diNode.getValue().getArtifactRef().getFragment());

        //verify that properties are transferred properly to the resulting Data Item
        assertTrue(diNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.CREATE_DATE).
                containsAll(metadataFile2.getSimplePropertyValues(DcsBoPackageOntology.CREATE_DATE)));
        assertTrue(diNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.MODIFIED_DATE).
                containsAll(metadataFile2.getSimplePropertyValues(DcsBoPackageOntology.MODIFIED_DATE)));
        assertEquals(1, diNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.NAME).size());

        assertTrue(diNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.NAME).iterator().next().
                contains(metadataFile2.getSimplePropertyValues(DcsBoPackageOntology.FILE_NAME).iterator().next()));
        assertFalse(diNode.getValue().getPropertyNames().contains(DcsBoPackageOntology.SIZE));
        assertFalse(diNode.getValue().getPropertyNames().contains(DcsBoPackageOntology.FORMAT));
        //verify that relationships are transferred properly
        assertTrue(diNode.getValue().getRelationships().contains(mdF2TestRelationship));
        assertTrue(diNode.getValue().getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF).getTargets().contains(collection5.getId()));

        //examine children (1) of the resulting DataItem node
        assertEquals(1, diNode.getChildrenNodes().size());
        //retrieve the child of the DI node
        PackageNode fileNode = diNode.getChildrenNodes().iterator().next();
        //verify that the DI node's child is a data file node
        assertEquals(DcsBoPackageOntology.DATAFILE, fileNode.getValue().getType());
        //verify the id of the DataFile node is set properly
        assertTrue(fileNode.getValue().getArtifactRef().getRefString().equals(originalFileUri));
 //       assertTrue(fileNode.getValue().getId().contains(originalFileUri));
//        assertNotNull((new URI(fileNode.getValue().getId())).getFragment());

        assertTrue(fileNode.getValue().getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF).getTargets().contains(diNode.getValue().getId()));
        //assert that there are no other relationship in the resulting file artifact
        assertEquals(1, fileNode.getValue().getRelationships().size());
        assertEquals(fileNode.getParentNode(), diNode);
        assertTrue(fileNode.getChildrenNodes().isEmpty());

        //verify that the nodes map has gain one node.
        assertEquals(originalNodesMapSize + 1, tree.getNodesMap().size());

        //verify that pd contains the new di artifact
        assertTrue(pd.getPackageArtifacts().contains(diNode.getValue()));
    }

    /**
     * Test that a DI resulted from a MdF->DI+DF transformation can be converted back to MdF
     * @throws PackageOntologyException
     */
    @Test
    public void testCanPointToGrandparent() throws PackageOntologyException, URISyntaxException {
        //****************************************
        // Setting up the test
        //****************************************
        tree = underTest.buildPackageTree(pd, contentRoot);

        String originalFileUri = metadataFile2.getArtifactRef().getRefString();

        //transform metadataFile2 into DI+DF combo
        underTest.makeDataItemFileCombo(pd, tree, metadataFile2, contentRoot);

        PackageNode containingCollection = tree.getNodesMap().get(collection5.getId());
        PackageNode diNode = null;

        for (PackageNode childNode : containingCollection.getChildrenNodes()) {
            if (childNode.getValue().getType().equals(DcsBoPackageOntology.DATAITEM)) {
                diNode = childNode;
                break;
            }
        }
        assertNotNull(diNode);
        assertTrue(diNode.getValue().getId().contains(originalFileUri));
        assertNotNull(new URI(diNode.getValue().getId()).getFragment());
        assertEquals(1, diNode.getChildrenNodes().size());
        PackageNode fileNode = diNode.getChildrenNodes().get(0);

        underTest.changeType(pd, tree, fileNode.getValue(), contentRoot, DcsBoPackageOntology.METADATAFILE);
        assertTrue(underTest.canCollapseParentArtifact(tree, fileNode.getValue().getId()));
    }

    /**
     * Test that a DI which was not resulted from a MdF->DI+DF transformation canNOT be converted to MdF
     * @throws PackageOntologyException
     */
    @Test
    public void testCannotPointToGrandparent () throws PackageOntologyException {
        //****************************************
        // Setting up the test
        //****************************************
        tree = underTest.buildPackageTree(pd, contentRoot);
        assertFalse(underTest.canCollapseParentArtifact(tree, metadataFile1.getId()));
        assertFalse(underTest.canCollapseParentArtifact(tree, dataItem2.getId()));
        assertFalse(underTest.canCollapseParentArtifact(tree, collection2.getId()));
        assertFalse(underTest.canCollapseParentArtifact(tree, metadataFile3.getId()));
    }

    /**
     * Test that combo of DI+F can be collapsed back to MdF under a collection
     * @throws PackageOntologyException
     * @throws URISyntaxException
     */
    @Test
    public void testCollapseDI_DFtoMdF() throws PackageOntologyException, URISyntaxException {

        //****************************************
        // Setting up the test
        //****************************************
        tree = underTest.buildPackageTree(pd, contentRoot);

        int originalNodesMapSize = tree.getNodesMap().size();
        String originalFileUri = metadataFile2.getArtifactRef().getRefString();
        //transform metadataFile2 into DI+DF combo
        underTest.makeDataItemFileCombo(pd, tree, metadataFile2, contentRoot);

        //****************************************
        // Checking preconditions
        //****************************************
        //To hold the expected childnode of the containing collection: DI node
        PackageNode diNode = null;

        //Get the containing collection: collection5
        PackageNode containingCollection = tree.getNodesMap().get(collection5.getId());
        //make sure it has 2 children
        assertEquals(2, containingCollection.getChildrenNodes().size());
        //loop through the children nodes of collection find to find the expected dataitem node
        for (PackageNode childNode : containingCollection.getChildrenNodes()) {
            if (childNode.getValue().getType().equals(DcsBoPackageOntology.DATAITEM)) {
                diNode = childNode;
                break;
            }
        }
        assertNotNull(diNode);
        //verify that the DataItem's id being set properly
        assertTrue(diNode.getValue().getArtifactRef().getRefString().contains(originalFileUri));
        assertNotNull((new URI(diNode.getValue().getId())).getFragment());
        //examine children (1) of the resulting DataItem node
        assertEquals(1, diNode.getChildrenNodes().size());
        //retrieve the child of the DI node
        PackageNode fileNode = diNode.getChildrenNodes().iterator().next();
        //verify that the DI node's child is a data file node
        assertEquals(DcsBoPackageOntology.DATAFILE, fileNode.getValue().getType());
        //verify the id of the DataFile node is set properly
        assertTrue(fileNode.getValue().getArtifactRef().getRefString().equals(originalFileUri));
  //      assertTrue(fileNode.getValue().getId().contains(originalFileUri));
//        assertNotNull(new URI(fileNode.getValue().getId()).getFragment());

        //****************************************
        // Modify property for the DI
        //****************************************
        String diNodeCreateDate = DateTime.now().toString(DateTimeFormat.forPattern("MM/dd/yyyy"));
        diNode.getValue().setSimplePropertyValues(DcsBoPackageOntology.CREATE_DATE, diNodeCreateDate);
        //make sure the DI node's create date is different than that of the fileNode
        assertTrue(!diNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.CREATE_DATE).
                equals(fileNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.CREATE_DATE)));

        diNode.getValue().setSimplePropertyValues(DcsBoPackageOntology.CITABLE_LOCATOR, "citableLocator");
        //add relationships to DI
        PackageRelationship relationship1 = new PackageRelationship("Relationship1", false, "target11", "target12");
        PackageRelationship relationship2 = new PackageRelationship("Relationship2", false, "target21", "target22");
        diNode.getValue().setRelationships(relationship1, relationship2);

        //****************************************
        //the test
        //****************************************
        underTest.changeType(pd, tree, fileNode.getValue(), contentRoot, DcsBoPackageOntology.METADATAFILE);
        String removedArtifactId = underTest.collapseParentArtifact(pd, tree, fileNode.getValue().getId());
        //****************************************
        //verifying result
        //****************************************
        //assert that the removed artifact's id is returned
        assertEquals(diNode.getValue().getId(), removedArtifactId);
        //assert that file is now a child of the containinig collection
        assertTrue(containingCollection.getChildrenNodes().contains(fileNode));

        //assert that file's parent is the collection
        assertEquals(containingCollection, fileNode.getParentNode());

        //assert that file's relationships to collection was inserted properly
        assertTrue(fileNode.getValue().getRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF).getTargets().contains(containingCollection.getValue().getId()));
        //assert that file not only have 1 create date
        Set<String> createDatePropertyValues = fileNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.CREATE_DATE);
        assertEquals(1, createDatePropertyValues.size());

        //assert that when DI and DF property's value conflict, the DI's value got retained.
        assertEquals(diNodeCreateDate, createDatePropertyValues.iterator().next());

        //assert that non-applicable property from DI got dropped
        assertNull(fileNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.CITABLE_LOCATOR));
        //assert that all relationships from the DI is transferred to the mdf
        assertTrue(fileNode.getValue().getRelationships().contains(relationship1));
        assertTrue(fileNode.getValue().getRelationships().contains(relationship2));

        //assert that the DI node is no longer in the map
        assertEquals(originalNodesMapSize, tree.getNodesMap().size());
        assertFalse(tree.getNodesMap().containsValue(diNode));
        //assert that the DI artifact is not in the the pd
        assertFalse(pd.getPackageArtifacts().contains(diNode.getValue()));
    }

    @Test
    public void testGetFormattedProperty() {
        //Test that property with no format returns original value
        assertEquals("value", underTest.getFormattedProperty(collection1, "", DcsBoPackageOntology.NAME, "value"));

        //Test email is formatted correctly.
        assertEquals("mailto:email", underTest.getFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.EMAIL, "email"));

        //Test phone numbers
        assertEquals("tel:+1-888-651-5908", underTest.getFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "18886515908"));
        assertEquals("tel:+44-20-8765-4321", underTest.getFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "+44 20 8765 4321"));
        assertEquals("tel:+1-888-651-5908;ext=1624306", underTest.getFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "8886515908ext1624306"));

    }

    @Test
    public void testGetUnFormattedProperty() {
        //Test that property with no format returns original value
        assertEquals("value", underTest.getUnFormattedProperty(collection1, "", DcsBoPackageOntology.NAME, "value"));

        //Test email is unformatted correctly correctly.
        assertEquals("email", underTest.getUnFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.EMAIL, "mailto:email"));

        //Test phone numbers are unformatted correctly.
        assertEquals("+1 888-651-5908", underTest.getUnFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "tel:+1-888-651-5908"));
        assertEquals("+44 20 8765 4321", underTest.getUnFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "tel:+44-20-8765-4321"));
        assertEquals("+1 888-651-5908 ext. 1624306", underTest.getUnFormattedProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "tel:+1-888-651-5908;ext=1624306"));
    }

    @Test
    public void testPropertyValidation() {
        //Test that no validator available returns true
        assertTrue(underTest.validateProperty(collection1, "", DcsBoPackageOntology.NAME, "value").isValid());

        //Test phone number validation
        assertTrue(underTest.validateProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "18886515908").isValid());
        assertFalse(underTest.validateProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PHONE, "1234").isValid());

        //Test url validation
        assertTrue(underTest.validateProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PAGE, "http://dataconservancy.org").isValid());
        assertFalse(underTest.validateProperty(collection1, DcsBoPackageOntology.CREATOR, DcsBoPackageOntology.PAGE, "www.dataconservancy.org").isValid());
    }

    //Test that ignoring a collection ignores all it's children as well.
    @Test
    public void testIgnoreCollection() throws PackageOntologyException {

        collection1.setIgnored(true);
        underTest.buildPackageTree(pd, contentRoot);

        //All children of the collection should be ignored.
        assertTrue(dataItem1.isIgnored());
        assertTrue(collection3.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadataFile1.isIgnored());
        
        // Types should be the same
        assertEquals(DcsBoPackageOntology.DATAITEM, dataItem1.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection3.getType());
        assertEquals(DcsBoPackageOntology.DATAFILE, datafile1.getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, metadataFile1.getType());
    }

    //Tests that ignoring a data item ignores all it's children but not it's parent.
    @Test
    public void testIgnoreDataItem() throws PackageOntologyException {
        dataItem1.setIgnored(true);
        underTest.buildPackageTree(pd, contentRoot);

        //Parents shouldn't be ignored
        assertFalse(collection1.isIgnored());

        //All children should be ignored
        assertTrue(dataItem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadataFile1.isIgnored());

        //Siblings shouldn't be ignored
        assertFalse(collection3.isIgnored());

        // Types should be the same
        assertEquals(DcsBoPackageOntology.DATAITEM, dataItem1.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection1.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection3.getType());
        assertEquals(DcsBoPackageOntology.DATAFILE, datafile1.getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, metadataFile1.getType());
    }

    //Tests that ignoring then unignoring a collection leaves all children ignored
    @Test
    public void testUnIgnoreCollection() throws PackageOntologyException {
        collection1.setIgnored(true);
        underTest.buildPackageTree(pd, contentRoot);

        assertTrue(dataItem1.isIgnored());
        assertTrue(collection3.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadataFile1.isIgnored());

        collection1.setIgnored(false);
        underTest.buildPackageTree(pd, contentRoot);

        //All children should still be ignored.
        assertTrue(dataItem1.isIgnored());
        assertTrue(collection3.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadataFile1.isIgnored());
        
        // Types should be the same
        assertEquals(DcsBoPackageOntology.DATAITEM, dataItem1.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection1.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection3.getType());
        assertEquals(DcsBoPackageOntology.DATAFILE, datafile1.getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, metadataFile1.getType());
    }
    
    //Tests that unignoring a data item
    @Test
    public void testUnIgnoreDataItem() throws PackageOntologyException {
        collection1.setIgnored(true);
        underTest.buildPackageTree(pd, contentRoot);

        assertTrue(dataItem1.isIgnored());
        assertTrue(collection3.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadataFile1.isIgnored());

        dataItem1.setIgnored(false);
        underTest.buildPackageTree(pd, contentRoot);

        //This is kind of a strange test, since the collection is still ignored, all of it's children will be ignored
        assertTrue(dataItem1.isIgnored());
        assertTrue(collection3.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadataFile1.isIgnored());
        
        // Types should be the same
        assertEquals(DcsBoPackageOntology.DATAITEM, dataItem1.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection1.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection3.getType());
        assertEquals(DcsBoPackageOntology.DATAFILE, datafile1.getType());
        assertEquals(DcsBoPackageOntology.METADATAFILE, metadataFile1.getType());
    }

    //Test that after a collection has all it's data items and sub collections ignored it can become a data item.
    @Test
    public void testIgnoreAdjustsAvailableTypes() throws PackageOntologyException {
        PackageTree tree = underTest.buildPackageTree(pd, contentRoot);
        Set<String> types = underTest.getValidTypes(tree, collection3.getId());
        assertEquals(1, types.size());

        assertTrue(types.contains(DcsBoPackageOntology.COLLECTION));

        dataItem4.setIgnored(true);
        collection4.setIgnored(true);

        tree = underTest.buildPackageTree(pd, contentRoot);

        types = underTest.getValidTypes(tree, collection3.getId());
        assertEquals(2, types.size());

        types.contains(DcsBoPackageOntology.COLLECTION);
        types.contains(DcsBoPackageOntology.DATAITEM);
    }
    
    
    // Test that after a subcollection has its children ignored, it can be changed to a DataItem.
    // Then when a child collection is unignored, the DataItem becomes a Collection again.
    @Test
    public void testIgnoreAndUnignoreTypeTransform() throws PackageOntologyException {
        PackageTree tree = underTest.buildPackageTree(pd, contentRoot);

        assertEquals(1, underTest.getValidTypes(tree, collection3.getId()).size());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection3.getType());
        
        // Ignore collection3 children and rebuild tree
        
        dataItem4.setIgnored(true);
        collection4.setIgnored(true);        
        tree = underTest.buildPackageTree(pd, contentRoot);
        
        assertEquals(DcsBoPackageOntology.COLLECTION, collection3.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection4.getType());
        Set<String> types = underTest.getValidTypes(tree, collection3.getId());
        assertEquals(types, new HashSet<>(Arrays.asList(DcsBoPackageOntology.COLLECTION, DcsBoPackageOntology.DATAITEM)));        
        
        // Change collection3 type to DataItem
        
        underTest.changeType(pd, tree, collection3, contentRoot, DcsBoPackageOntology.DATAITEM);
        
        assertEquals(DcsBoPackageOntology.DATAITEM, collection3.getType());       
        assertEquals(DcsBoPackageOntology.COLLECTION, collection4.getType());
        assertTrue(collection4.isIgnored());
       
        // Unignore collection3 child collection and rebuild tree
        
        collection4.setIgnored(false);        
        tree = underTest.buildPackageTree(pd, contentRoot);
        
        assertFalse(collection4.isIgnored());
        assertTrue(dataItem4.isIgnored());
        assertEquals(1, underTest.getValidTypes(tree, collection3.getId()).size());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection3.getType());
        assertEquals(DcsBoPackageOntology.COLLECTION, collection4.getType());
    }

    /**
     * Test that an ignored mdf (of a collection) can be unignored and turned in to DIDF, then turned back into mdf
     * and get ignored without throwing exception.
     *
     * @throws PackageOntologyException
     * @throws URISyntaxException
     */
    @Test
    public void testTransformUnignoredMdFIntoDiDf() throws PackageOntologyException, URISyntaxException {
        //build a tree with ignored collectin's mdf
        metadataFile2.setIgnored(true);
        tree = underTest.buildPackageTree(pd, contentRoot);
        assertTrue(tree.getNodesMap().get(metadataFile2.getId()).getValue().isIgnored());

        //get info for later verification
        int originalNodesMapSize = tree.getNodesMap().size();
        String originalFileUri = metadataFile2.getArtifactRef().getRefString();
        //unignore the mdf
        metadataFile2.setIgnored(false);
        underTest.buildPackageTree(pd, contentRoot);

        //turn unignored mdf into DIDF
        underTest.changeType(pd, tree, metadataFile2, contentRoot, DcsBoPackageOntologyServiceImpl.didfComboType);
        //verify successful transformation
        verifyFilesTransformationToDIDF(originalNodesMapSize, originalFileUri);

        //collapse the didf backto mdf of collection
        underTest.changeType(pd, tree, metadataFile2, contentRoot, DcsBoPackageOntology.METADATAFILE);
        underTest.collapseParentArtifact(pd, tree, metadataFile2.getId());

        assertFalse(metadataFile2.isIgnored());

        //ignore mdf
        metadataFile2.setIgnored(true);
        underTest.buildPackageTree(pd, contentRoot);

    }

}
