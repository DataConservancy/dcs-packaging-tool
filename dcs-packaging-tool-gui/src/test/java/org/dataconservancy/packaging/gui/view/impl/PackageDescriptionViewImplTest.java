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
 */
package org.dataconservancy.packaging.gui.view.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


import javafx.stage.FileChooser;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.presenter.impl.PackageDescriptionPresenterImpl;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.impl.PackageDescriptionValidator;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests that the package description view imple initializes all fields correctly. 
 */
public class PackageDescriptionViewImplTest extends BaseGuiTest {
    private PackageDescriptionViewImpl view;

    private PackageDescriptionPresenterImpl presenter;
    private PackageDescription description;

    private PackageArtifact datafile1;
    private PackageArtifact datafile2a;
    private PackageArtifact datafile2b;
    private PackageArtifact metadatafile1;
    private PackageArtifact dataitem1;
    private PackageArtifact dataitem2;
    private PackageArtifact dataitem3;
    private PackageArtifact dataitem4;
    private PackageArtifact collection1;
    private PackageArtifact collection2;
    private PackageArtifact collection3;
    private PackageArtifact collection4;
    private PackageArtifact project;

    @Autowired
    @Qualifier("packageOntologyService")
    private PackageOntologyService packageOntologyService;

    /**
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
     *
     */
    private Set<PackageArtifact> setupPackageArtifacts() {
        Set<PackageArtifact> artifacts = new HashSet<PackageArtifact>();

        datafile1 = new PackageArtifact();
        datafile1.setId("id:df1");
        datafile1.setArtifactRef("file:/some/file/path");
        datafile1.setType(DcsBoPackageOntology.DATAFILE);
        datafile1.setByteStream(true);
        datafile1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di1"));
        artifacts.add(datafile1);

        metadatafile1 = new PackageArtifact();
        metadatafile1.setId("id:mdf1");
        metadatafile1.setArtifactRef("file:/some/file/path");
        metadatafile1.setType(DcsBoPackageOntology.METADATAFILE);
        metadatafile1.setByteStream(true);
        metadatafile1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, "id:di1"));
        artifacts.add(metadatafile1);

        dataitem1 = new PackageArtifact();
        dataitem1.setId("id:di1");
        dataitem1.setArtifactRef("file:/some/file/path");
        dataitem1.setType(DcsBoPackageOntology.DATAITEM);
        dataitem1.setByteStream(false);
        dataitem1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));
        artifacts.add(dataitem1);

        datafile2a = new PackageArtifact();
        datafile2a.setId("id:df2a");
        datafile2a.setArtifactRef("file:/some/file/path");
        datafile2a.setType(DcsBoPackageOntology.DATAFILE);
        datafile2a.setByteStream(true);
        datafile2a.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di2"));
        artifacts.add(datafile2a);

        datafile2b = new PackageArtifact();
        datafile2b.setId("id:df2b");
        datafile2b.setArtifactRef("file:/some/file/path");
        datafile2b.setType(DcsBoPackageOntology.DATAFILE);
        datafile2b.setByteStream(true);
        datafile2b.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di2"));
        artifacts.add(datafile2b);

        dataitem2 = new PackageArtifact();
        dataitem2.setId("id:di2");
        dataitem2.setArtifactRef("file:/some/file/path");
        dataitem2.setType(DcsBoPackageOntology.DATAITEM);
        dataitem2.setByteStream(false);
        dataitem2.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));
        artifacts.add(dataitem2);

        collection1 = new PackageArtifact();
        collection1.setId("id:col1");
        collection1.setArtifactRef("file:/some/file/path");
        collection1.setType(DcsBoPackageOntology.COLLECTION);
        collection1.setByteStream(false);
        collection1 .setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"));
        artifacts.add(collection1);

        dataitem3 = new PackageArtifact();
        dataitem3.setId("id:di3");
        dataitem3.setArtifactRef("file:/some/file/path");
        dataitem3.setType(DcsBoPackageOntology.DATAITEM);
        dataitem3.setByteStream(false);
        dataitem3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col2"));
        artifacts.add(dataitem3);

        collection2 = new PackageArtifact();
        collection2.setId("id:col2");
        collection2.setArtifactRef("file:/some/file/path");
        collection2.setType(DcsBoPackageOntology.COLLECTION);
        collection2.setByteStream(false);
        collection2 .setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"));
        artifacts.add(collection2);

        collection3 = new PackageArtifact();
        collection3.setId("id:col3");
        collection3.setArtifactRef("file:/some/file/path");
        collection3.setType(DcsBoPackageOntology.COLLECTION);
        collection3.setByteStream(false);
        collection3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));
        artifacts.add(collection3);

        dataitem4 = new PackageArtifact();
        dataitem4.setId("id:di4");
        dataitem4.setArtifactRef("file:/some/file/path");
        dataitem4.setType(DcsBoPackageOntology.DATAITEM);
        dataitem4.setByteStream(false);
        dataitem4.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col3"));
        artifacts.add(dataitem4);

        collection4 = new PackageArtifact();
        collection4.setId("id:col4");
        collection4.setArtifactRef("file:/some/file/path");
        collection4.setType(DcsBoPackageOntology.COLLECTION);
        collection4.setByteStream(false);
        collection4.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col3"));
        artifacts.add(collection4);

        project = new PackageArtifact();
        project.setId("id:project");
        project.setArtifactRef("file:/some/file/path");
        project.setType(DcsBoPackageOntology.PROJECT);
        project.setByteStream(false);
        artifacts.add(project);

        return artifacts;
    }
    @Before
    public void setup() {
        view = new PackageDescriptionViewImpl(labels, errors, messages, propertyLabels, internalProperties, "classpath:/defaultRelationships");
        HeaderView headerView = new HeaderViewImpl(labels);
        view.setPackageOntologyService(packageOntologyService);

        view.setHeaderView(headerView);
        view.setHelp(help);

        PackageDescriptionBuilder builder = mock(PackageDescriptionBuilder.class);
        PackageDescriptionValidator validator = mock(PackageDescriptionValidator.class);
        Controller controller = new Controller() {

            @Override
            public PackageDescription getPackageDescription() {
                return description;
            }

            @Override
            public File showSaveFileDialog(FileChooser chooser) {
                return null;
            }
        };

        presenter = new PackageDescriptionPresenterImpl(view);
        presenter.setLabels(labels);
        presenter.setPackageDescriptionBuilder(builder);
        presenter.setPackageDescriptionValidator(validator);
        presenter.setController(controller);
        presenter.setPackageOntologyService(packageOntologyService);
        presenter.setInternalProperties(internalProperties);

        description = new PackageDescription();
        description.setPackageOntologyIdentifier("test");
        description.setPackageArtifacts(setupPackageArtifacts());

    }
    
    /**
     * Make sure controls can be retrieved.
     */
    @Test
    public void testComponentsNotNull() {
        assertNotNull(view.getArtifactTreeView());
        assertNotNull(view.getContinueButton());
        assertNotNull(view.getPackageNameLabel());
        assertNotNull(view.getInheritMetadataCheckBoxMap());
    }


    @Test
    public void testIgnoreCollection() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(collection1), true);

        assertTrue(collection1.isIgnored());
        assertTrue(dataitem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadatafile1.isIgnored());
        assertTrue(collection3.isIgnored());

        //Sibling collection should not be ignored
        assertFalse(collection2.isIgnored());
    }

    @Test
    public void testUnignoreCollection() {
        presenter.rebuildTreeView();

        //First ignore the collection
        view.toggleItemIgnore(presenter.findItem(collection1), true);

        assertTrue(collection1.isIgnored());
        assertTrue(dataitem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadatafile1.isIgnored());
        assertTrue(collection3.isIgnored());

        //Sibling collection should not be ignored
        assertFalse(collection2.isIgnored());

        //Now unignore the collection and check that all children are also unignored.
        view.toggleItemIgnore(presenter.findItem(collection1), false);

        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());
        assertFalse(datafile1.isIgnored());
        assertFalse(metadatafile1.isIgnored());
        assertFalse(collection3.isIgnored());

        //Sibling collection should not be ignored
        assertFalse(collection2.isIgnored());
    }

    @Test
    public void testIgnoreDataItem() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(dataitem1), true);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());

        //Item and it's children should be ignored
        assertTrue(dataitem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadatafile1.isIgnored());

        //Sibling should not be ignored
        assertFalse(collection3.isIgnored());
    }

    @Test
    public void testUnIgnoreDataItem() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(dataitem1), true);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());

        //Item and it's children should be ignored
        assertTrue(dataitem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadatafile1.isIgnored());

        //Sibling should not be ignored
        assertFalse(collection3.isIgnored());

        view.toggleItemIgnore(presenter.findItem(dataitem1), false);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());

        //Item and it's children should be ignored
        assertFalse(dataitem1.isIgnored());
        assertFalse(datafile1.isIgnored());
        assertFalse(metadatafile1.isIgnored());

        //Sibling should not be ignored
        assertFalse(collection3.isIgnored());
    }

    @Test
    public void testUnignoreDataItemUnignoresParent() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(collection1), true);

        assertTrue(collection1.isIgnored());
        assertTrue(dataitem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadatafile1.isIgnored());
        assertTrue(collection3.isIgnored());

        view.toggleItemIgnore(presenter.findItem(dataitem1), false);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());

        //Item and it's children should not be ignored
        assertFalse(dataitem1.isIgnored());
        assertFalse(datafile1.isIgnored());
        assertFalse(metadatafile1.isIgnored());

        //Sibling should still be ignored
        assertTrue(collection3.isIgnored());
    }

    @Test
    public void testIgnoreDataFile() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(datafile1), true);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should be ignored
        assertTrue(datafile1.isIgnored());

        //Sibling should not be ignored
        assertFalse(metadatafile1.isIgnored());
    }

    @Test
    public void testUnIgnoreDataFile() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(datafile1), true);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should be ignored
        assertTrue(datafile1.isIgnored());

        //Sibling should not be ignored
        assertFalse(metadatafile1.isIgnored());

        view.toggleItemIgnore(presenter.findItem(datafile1), false);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should not be ignored
        assertFalse(datafile1.isIgnored());

        //Sibling should still not be ignored
        assertFalse(metadatafile1.isIgnored());
    }

    @Test
    public void testUnIgnoreDataFileUnIgnoresParents() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(collection1), true);

        assertTrue(collection1.isIgnored());
        assertTrue(dataitem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadatafile1.isIgnored());
        assertTrue(collection3.isIgnored());

        view.toggleItemIgnore(presenter.findItem(datafile1), false);

        //Parents should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should not be ignored
        assertFalse(datafile1.isIgnored());

        //Siblings should be ignored
        assertTrue(metadatafile1.isIgnored());

        //Cousins?? should still be ignored
        assertTrue(collection3.isIgnored());
    }

    @Test
    public void testIgnoreMetadataFile() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(metadatafile1), true);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should be ignored
        assertTrue(metadatafile1.isIgnored());

        //Sibling should not be ignored
        assertFalse(datafile1.isIgnored());
    }

    @Test
    public void testUnIgnoreMetadataFile() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(metadatafile1), true);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should be ignored
        assertTrue(metadatafile1.isIgnored());

        //Sibling should not be ignored
        assertFalse(datafile1.isIgnored());

        view.toggleItemIgnore(presenter.findItem(metadatafile1), false);

        //Parent should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should not be ignored
        assertFalse(metadatafile1.isIgnored());

        //Sibling should still not be ignored
        assertFalse(datafile1.isIgnored());
    }

    @Test
    public void testUnIgnoreMetadataFileUnIgnoresParents() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(presenter.findItem(collection1), true);

        assertTrue(collection1.isIgnored());
        assertTrue(dataitem1.isIgnored());
        assertTrue(datafile1.isIgnored());
        assertTrue(metadatafile1.isIgnored());
        assertTrue(collection3.isIgnored());

        view.toggleItemIgnore(presenter.findItem(metadatafile1), false);

        //Parents should not be ignored
        assertFalse(collection1.isIgnored());
        assertFalse(dataitem1.isIgnored());

        //Item should not be ignored
        assertFalse(metadatafile1.isIgnored());

        //Siblings should be ignored
        assertTrue(datafile1.isIgnored());

        //Cousins?? should still be ignored
        assertTrue(collection3.isIgnored());
    }

    @Ignore
    @Test
    public void testAddFile() {
        // TODO: test that adding files works
    }

    @Ignore
    @Test
    public void testAddFolder() {
        // TODO: test that adding files works
    }

    @Ignore
    @Test
    public void testRefresh() {
        // TODO: test that refresh works
    }

    @Ignore
    @Test
    public void testRemapFile() {
        // TODO: test that remap file works
    }

    @Ignore
    @Test
    public void testRemapFolder() {
        // TODO: test that remap folder works
    }
}