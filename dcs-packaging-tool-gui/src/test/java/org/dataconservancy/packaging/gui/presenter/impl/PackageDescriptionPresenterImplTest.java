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
package org.dataconservancy.packaging.gui.presenter.impl;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.presenter.PackageDescriptionPresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.PackageDescriptionView;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl.ArtifactPropertyContainer;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl.ArtifactRelationshipContainer;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.impl.PackageDescriptionValidator;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Tests the pacakge description presenter implementation. Which tests that elements are displayed correctly,
 * the tree is generated correctly, and inheritance works correctly. 
 * Note currently the popup code is not being tested. 
 */
public class PackageDescriptionPresenterImplTest extends BaseGuiTest {

    private PackageDescriptionPresenterImpl presenter;
    private PackageDescriptionViewImpl view;

    private boolean goToNextPage;
    private PackageDescription description;

    private PackageArtifact datafile1;
    private PackageArtifact datafile2a;
    private PackageArtifact datafile2b;
    private PackageArtifact metadatafile1;
    private PackageArtifact metadatafile2;
    private PackageArtifact metadatafile3;
    private PackageArtifact dataitem1;
    private PackageArtifact dataitem2;
    private PackageArtifact dataitem3;
    private PackageArtifact dataitem4;
    private PackageArtifact collection1;
    private PackageArtifact collection2;
    private PackageArtifact collection3;
    private PackageArtifact collection4;
    private PackageArtifact collection5;
    private PackageArtifact project;

    @Autowired
    @Qualifier("packageOntologyService")
    private PackageOntologyService packageOntologyService;

    @Autowired
    @Qualifier("disciplineService")
    private DisciplineLoadingService disciplineLoadingService;

    private Controller controller;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
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
     * |-collection5
     *      |-metadatafile2
     *      |-metadatafile3
     *
     */
    private Set<PackageArtifact> setupPackageArtifacts() {
        Set<PackageArtifact> artifacts = new HashSet<PackageArtifact>();
        
        datafile1 = new PackageArtifact();
        datafile1.setId("id:df1");
        datafile1.setArtifactRef("some/file/path with spaces");
        datafile1.setType(DcsBoPackageOntology.DATAFILE);
        datafile1.setByteStream(true);
        datafile1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di1"));
        artifacts.add(datafile1);

        metadatafile1 = new PackageArtifact();
        metadatafile1.setId("id:mdf1");
        metadatafile1.setArtifactRef("some/file/path");
        metadatafile1.setType(DcsBoPackageOntology.METADATAFILE);
        metadatafile1.setByteStream(true);
        metadatafile1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, "id:di1"));
        artifacts.add(metadatafile1);

        metadatafile2 = new PackageArtifact();
        metadatafile2.setId("id:mdf2");
        metadatafile2.setArtifactRef("some/file/path");
        metadatafile2.setType(DcsBoPackageOntology.METADATAFILE);
        metadatafile2.setByteStream(true);
        metadatafile2.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, "id:col5"));
        artifacts.add(metadatafile2);

        metadatafile3 = new PackageArtifact();
        metadatafile3.setId("id:mdf3");
        metadatafile3.setArtifactRef("some/file/path");
        metadatafile3.setType(DcsBoPackageOntology.METADATAFILE);
        metadatafile3.setByteStream(true);
        metadatafile3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, "id:col5"));
        artifacts.add(metadatafile3);

        dataitem1 = new PackageArtifact();
        dataitem1.setId("id:di1");
        dataitem1.setArtifactRef("some/file/path");
        dataitem1.setType(DcsBoPackageOntology.DATAITEM);
        dataitem1.setByteStream(false);
        dataitem1.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));
        artifacts.add(dataitem1);

        datafile2a = new PackageArtifact();
        datafile2a.setId("id:df2a");
        datafile2a.setArtifactRef("some/file/path");
        datafile2a.setType(DcsBoPackageOntology.DATAFILE);
        datafile2a.setByteStream(true);
        datafile2a.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di2"));
        artifacts.add(datafile2a);

        datafile2b = new PackageArtifact();
        datafile2b.setId("id:df2b");
        datafile2b.setArtifactRef("some/file/path");
        datafile2b.setType(DcsBoPackageOntology.DATAFILE);
        datafile2b.setByteStream(true);
        datafile2b.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:di2"));
        artifacts.add(datafile2b);

        dataitem2 = new PackageArtifact();
        dataitem2.setId("id:di2");
        dataitem2.setArtifactRef("some/file/path");
        dataitem2.setType(DcsBoPackageOntology.DATAITEM);
        dataitem2.setByteStream(false);
        dataitem2.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));
        artifacts.add(dataitem2);

        collection1 = new PackageArtifact();
        collection1.setId("id:col1");
        collection1.setArtifactRef("some/file/path");
        collection1.setType(DcsBoPackageOntology.COLLECTION);
        collection1.setByteStream(false);
        collection1 .setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"));
        artifacts.add(collection1);

        dataitem3 = new PackageArtifact();
        dataitem3.setId("id:di3");
        dataitem3.setArtifactRef("some/file/path");
        dataitem3.setType(DcsBoPackageOntology.DATAITEM);
        dataitem3.setByteStream(false);
        dataitem3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col2"));
        artifacts.add(dataitem3);

        collection2 = new PackageArtifact();
        collection2.setId("id:col2");
        collection2.setArtifactRef("some/file/path");
        collection2.setType(DcsBoPackageOntology.COLLECTION);
        collection2.setByteStream(false);
        collection2 .setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"));
        artifacts.add(collection2);

        collection3 = new PackageArtifact();
        collection3.setId("id:col3");
        collection3.setArtifactRef("some/file/path");
        collection3.setType(DcsBoPackageOntology.COLLECTION);
        collection3.setByteStream(false);
        collection3.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col1"));
        artifacts.add(collection3);

        dataitem4 = new PackageArtifact();
        dataitem4.setId("id:di4");
        dataitem4.setArtifactRef("some/file/path");
        dataitem4.setType(DcsBoPackageOntology.DATAITEM);
        dataitem4.setByteStream(false);
        dataitem4.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col3"));
        artifacts.add(dataitem4);

        collection4 = new PackageArtifact();
        collection4.setId("id:col4");
        collection4.setArtifactRef("some/file/path");
        collection4.setType(DcsBoPackageOntology.COLLECTION);
        collection4.setByteStream(false);
        collection4.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:col3"));
        artifacts.add(collection4);
        
        collection5 = new PackageArtifact();
        collection5.setId("id:col5");
        collection5.setArtifactRef("some/file/path");
        collection5.setType(DcsBoPackageOntology.COLLECTION);
        collection5.setByteStream(false);
        collection5.setRelationships(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, "id:project"), new PackageRelationship(DcsBoPackageOntology.HAS_METADATA, true, metadatafile2.getId(), metadatafile3.getId()));
        artifacts.add(collection5);
        
        project = new PackageArtifact();
        project.setId("id:project");
        project.setArtifactRef("some/file/path");
        project.setType(DcsBoPackageOntology.PROJECT);
        project.setByteStream(false);
        artifacts.add(project);

        return artifacts;
    }
    
    
    @Before
    public void setup() throws IOException {
        goToNextPage = false;
        PackageDescriptionBuilder builder = mock(PackageDescriptionBuilder.class);
        PackageDescriptionValidator validator = mock(PackageDescriptionValidator.class);
        
        controller = new Controller() {
            @Override
            public void goToNextPage() {
                goToNextPage = true;
            }
            
            @Override
            public PackageDescription getPackageDescription() {
                return description;
            }
            
            @Override
            public File showSaveFileDialog(FileChooser chooser) {
                try {
                    return tmpfolder.newFile("test");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        
        // For this test, we want a new Presenter and view for each test so that the status message is checked properly
        view = new PackageDescriptionViewImpl(labels, errors, messages, propertyLabels, internalProperties, "classpath:/defaultRelationships", disciplineLoadingService);
        view.setPackageOntologyService(packageOntologyService);
        
        HeaderView headerView = new HeaderViewImpl(labels);
        view.setHeaderView(headerView);
        view.setHelp(help);
        
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
     * Ensure the presenter displays a node.
     * Can no longer call presenter display to set up the package artifact trees for the second page's operation, as
     * the method utilizes threads outside of the main application thread. Have to make call to set up the artifact
     * tree directly
     * @throws InterruptedException 
     */
    @Test
    public void testDisplay() throws InterruptedException {
        assertNotNull(presenter.display(false));
    }

    /**
     * Ensure that event handlers do the right thing.
     */
    @Test
    public void testEventHandlers() {

        assertFalse(goToNextPage);
        view.getContinueButton().fire();
        assertTrue(goToNextPage);
    }   
    
    /**
     * Tests successful tree generation
     * @throws IOException
     */
    @Test
    public void testSuccessfulTreeGeneration() throws IOException, InterruptedException {
        presenter.rebuildTreeView();
      
        assertEquals(3, view.getRoot().getChildren().size());

        view.getRoot().setExpanded(true);
    }


    /*
     * Tests that description field is inherited properly from collection subCollection.
     */
    @Test
    public void testInheritance_AmongstCollections() throws InterruptedException {
        collection1.addSimplePropertyValue(DcsBoPackageOntology.DESCRIPTION, "Best moos of all time.");
        
        presenter.rebuildTreeView();

        view.setPopupArtifact(collection1);
        view.getInheritMetadataCheckBoxMap().put(DcsBoPackageOntology.DESCRIPTION, new CheckBox());

        assertTrue(collection3.getPropertyNames().isEmpty());
        assertTrue(collection4.getPropertyNames().isEmpty());
        
        view.getInheritMetadataCheckBoxMap().get(DcsBoPackageOntology.DESCRIPTION).fire();
        
        presenter.applyMetadataInheritance();
        
        assertEquals(1, collection3.getPropertyNames().size());
        assertEquals(1, collection4.getPropertyNames().size());
        assertEquals(collection1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION),
                collection3.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(collection1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION),
                collection4.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
    }

    /*
     * Tests that publisher field is inherited properly from project to collection
     */
    @Test
    public void testInheritance_ProjectCollection() throws InterruptedException {
        project.addSimplePropertyValue(DcsBoPackageOntology.PUBLISHER, "Manure Inc.");
        
        presenter.rebuildTreeView();

        view.setPopupArtifact(project);
        view.getInheritMetadataCheckBoxMap().put(DcsBoPackageOntology.PUBLISHER, new CheckBox());

        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.PUBLISHER).equals(collection1.getSimplePropertyValues(DcsBoPackageOntology.PUBLISHER)));

        view.getInheritMetadataCheckBoxMap().get(DcsBoPackageOntology.PUBLISHER).fire();

        presenter.applyMetadataInheritance();
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.PUBLISHER), collection1.getSimplePropertyValues(DcsBoPackageOntology.PUBLISHER));

    }

    /*
     * Tests that description field is inherited properly from project to file
     */
    @Test
    public void testInheritance_ProjectFiles() throws InterruptedException {
        project.addSimplePropertyValue(DcsBoPackageOntology.DESCRIPTION, "A fine pasture with tasty clover.");
        
        presenter.rebuildTreeView();

        view.setPopupArtifact(project);
        view.getInheritMetadataCheckBoxMap().put(DcsBoPackageOntology.DESCRIPTION, new CheckBox());

        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(collection1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));
        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(collection3.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));
        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(datafile1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));
        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(datafile2a.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));
        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(dataitem1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));
        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(dataitem2.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));
        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(metadatafile1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));
        assertFalse(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION).equals(metadatafile2.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION)));

        view.getInheritMetadataCheckBoxMap().get(DcsBoPackageOntology.DESCRIPTION).fire();

        presenter.applyMetadataInheritance();
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), collection1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), collection3.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), dataitem1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), dataitem2.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), datafile2a.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), datafile1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), metadatafile1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));
        assertEquals(project.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION), metadatafile1.getSimplePropertyValues(DcsBoPackageOntology.DESCRIPTION));

    }

    /*
     * Tests that complex field is inherited properly from collection to collection and data item
     */
    @Test
    public void testInheritance_ComplexProperty() throws InterruptedException {
        PropertyValueGroup creatorGroup = new PropertyValueGroup();
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.EMAIL, "farmerbob@example.com");
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.NAME, "Bob Blahblah");
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.PHONE, "1234567890");
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.PAGE, "http://example.com/farm");

        collection1.addPropertyValueGroup(DcsBoPackageOntology.CREATOR, creatorGroup);
        
        presenter.rebuildTreeView();

        view.setPopupArtifact(collection1);
        view.getInheritMetadataCheckBoxMap().put(DcsBoPackageOntology.CREATOR, new CheckBox());

        assertFalse(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR).equals(collection3.getPropertyValueGroups(DcsBoPackageOntology.CREATOR)));
        assertFalse(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR).equals(collection4.getPropertyValueGroups(DcsBoPackageOntology.CREATOR)));
        assertFalse(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR).equals(dataitem1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR)));
        assertFalse(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR).equals(dataitem2.getPropertyValueGroups(DcsBoPackageOntology.CREATOR)));
        assertFalse(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR).equals(dataitem3.getPropertyValueGroups(DcsBoPackageOntology.CREATOR)));
        assertFalse(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR).equals(dataitem4.getPropertyValueGroups(DcsBoPackageOntology.CREATOR)));

        view.getInheritMetadataCheckBoxMap().get(DcsBoPackageOntology.CREATOR).fire();
        presenter.applyMetadataInheritance();
        
        assertEquals(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR), collection3.getPropertyValueGroups(DcsBoPackageOntology.CREATOR));
        assertEquals(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR), collection4.getPropertyValueGroups(DcsBoPackageOntology.CREATOR));
        assertEquals(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR), dataitem1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR));
        assertEquals(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR), dataitem2.getPropertyValueGroups(DcsBoPackageOntology.CREATOR));
        assertEquals(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR), dataitem4.getPropertyValueGroups(DcsBoPackageOntology.CREATOR));
        assertFalse(collection1.getPropertyValueGroups(DcsBoPackageOntology.CREATOR).equals(dataitem3.getPropertyValueGroups(DcsBoPackageOntology.CREATOR)));
    }

    /**
     * Tests that valid artifacts of the same type return empty, but invalid artifacts list their invalid properties
     */
    @Test
    public void testFindInvalidPropertiesOfSameType() {
        //Test a valid artifact
        List<String> invalidProperties = presenter.findInvalidProperties(collection1, collection1.getType());
        assertNotNull(invalidProperties);
        assertTrue(invalidProperties.isEmpty());

        //Test an invalid artifact
        PackageArtifact badCollection = new PackageArtifact();
        badCollection.setId("collection:1");
        badCollection.setArtifactRef("/project/collection/");
        badCollection.setType(DcsBoPackageOntology.COLLECTION);
        badCollection.addSimplePropertyValue("BadProperty", "Collection summary blah blah");
        invalidProperties = presenter.findInvalidProperties(badCollection, badCollection.getType());
        assertNotNull(invalidProperties);
        assertEquals(1, invalidProperties.size());

        assertTrue(invalidProperties.get(0).equalsIgnoreCase("BadProperty"));

        //Test that an empty artifact shows no invalid properties
        PackageArtifact emptyCollection = new PackageArtifact();
        emptyCollection.setId("collection:1");
        emptyCollection.setArtifactRef("/project/collection/");
        emptyCollection.setType(DcsBoPackageOntology.COLLECTION);
        invalidProperties = presenter.findInvalidProperties(emptyCollection, emptyCollection.getType());
        assertNotNull(invalidProperties);
        assertTrue(invalidProperties.isEmpty());

    }

    /**
     * Tests that when changing types only set properties are listed, and only properties that are in the new type.
     */
    @Test
    public void testFindInvalidPropertiesDifferentType() {
        //Test a collection to a data item with a shared property returns empty list
        PackageArtifact collection = new PackageArtifact();
        collection.setId("collection:1");
        collection.setArtifactRef("/project/collection/");
        collection.setType(DcsBoPackageOntology.COLLECTION);

        PropertyValueGroup creatorGroup = new PropertyValueGroup();
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.EMAIL, "foo@email.com");
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.NAME, "foo creator");
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.PHONE, "1234567890");
        creatorGroup.addSubPropertyValue(DcsBoPackageOntology.PAGE, "www.foo-creator.com");

        collection.addPropertyValueGroup(DcsBoPackageOntology.CREATOR, creatorGroup);

        List<String> invalidProperties = presenter.findInvalidProperties(collection, dataitem1.getType());
        assertNotNull(invalidProperties);
        assertTrue(invalidProperties.isEmpty());

        //Test that a collection to a data item with a non shared property returns the property.
        collection.addSimplePropertyValue(DcsBoPackageOntology.TITLE, "title");
        invalidProperties = presenter.findInvalidProperties(collection, dataitem1.getType());
        assertNotNull(invalidProperties);
        assertEquals(1, invalidProperties.size());
        assertTrue(invalidProperties.get(0).equalsIgnoreCase("title"));

        //Test that an empty artifact shows no invalid properties
        PackageArtifact emptyCollection = new PackageArtifact();
        collection.setId("collection:1");
        collection.setArtifactRef("/project/collection/");
        collection.setType(DcsBoPackageOntology.COLLECTION);
        invalidProperties = presenter.findInvalidProperties(emptyCollection, dataitem1.getType());
        assertNotNull(invalidProperties);
        assertTrue(invalidProperties.isEmpty());
    }


    /**
     * Tests that a mock popup is successfully generated.
     * @throws IOException
     */
    @Test
    public void testGeneratePopup() throws IOException {
        
        PackageDescriptionView view = new PackageDescriptionView() {

            private PackageToolPopup detailsPopup;
            
            @Override
            public void setPresenter(PackageDescriptionPresenter presenter) {
                
            }

            @Override
            public Node asNode() {
                return null;
            }

            @Override
            public TreeItem<PackageArtifact> getRoot() {
                return null;
            }

            @Override
            public TreeTableView<PackageArtifact> getArtifactTreeView() {
                return null;
            }

            @Override
            public PackageToolPopup getPackageArtifactPopup() {
                return detailsPopup;
            }

            @Override
            public void showArtifactDetails(PackageArtifact artifact,
                                            double xValue,
                                            double yValue) {
                detailsPopup = mock(PackageToolPopup.class);
                
            }

            @Override
            public Button getContinueButton() {
                return null;
            }

            @Override
            public Hyperlink getCancelLink() {
                return null;
            }

            @Override
            public Label getPackageNameLabel() {
                return null;
            }

            @Override
            public FileChooser getPackageDescriptionFileChooser() {
                return null;
            }

            @Override
            public CheckBox getFullPathCheckBox() {
                return null;
            }

            @Override
            public Hyperlink getCancelPopupHyperlink() {
                return null;
            }

            @Override
            public Button getApplyPopupButton() {
                return null;
            }

            @Override
            public Map<String, ArtifactPropertyContainer> getArtifactPropertyFields() {
                return new HashMap<String, ArtifactPropertyContainer>();
            }

            @Override
            public PackageArtifact getPopupArtifact() {
                return null;
            }

            @Override
            public PackageToolPopup getWarningPopup() {
                return null;
            }

            @Override
            public void showWarningPopup(String title, String errorMessage, boolean allowNegative, boolean allowFutureHide) {
                // Default method body

            }


            @Override
            public Button getWarningPopupPositiveButton() {
                return null;
            }

            @Override
            public Button getWarningPopupNegativeButton() {
                // Default method body
                return null;
            }

            @Override
            public CheckBox getHideFutureWarningPopupCheckbox() {
                // Default method body
                return null;
            }

            @Override
            public Button getSaveButton() {
                return null;
            }

            @Override
            public void showHelpPopup() {                
            }

            @Override
            public void showAboutPopup() {                
            }

            @Override
            public void setHelpPopupContent(Node content) {                
            }

            @Override
            public void setAboutPopupContent(Node content) {                
            }

            @Override
            public Hyperlink getHeaderViewAboutLink() {
                return null;
            }

            @Override
            public Hyperlink getHeaderViewHelpLink() {
                return null;
            }

            @Override
            public void setHeaderView(HeaderView headerView) {                
            }

            @Override
            public Set<ArtifactRelationshipContainer> getArtifactRelationshipFields() {
                return null;
            }

            @Override
            public Label getErrorMessageLabel() {
                return null;
            }

            @Override
            public CheckBox getShowIgnored() {
                return null;
            }

            @Override
            public Map<String, CheckBox> getInheritMetadataCheckBoxMap() {
                return null;
            }

            @Override
            public Button getReenableWarningsButton() {
                return null;
            }

            @Override
            public void setupHelp() {
            }

            @Override
            public void setHelp(Help help) {
            }
            
        };
        
        view.showArtifactDetails(new PackageArtifact(), 100, 100);
        assertNotNull(view.getPackageArtifactPopup());
    }

    @Test
    public void testGetInheritingTypes() {
        Set<String> inheritingTypes = presenter.getInheritingTypes(DcsBoPackageOntology.COLLECTION, DcsBoPackageOntology.CONTACT_INFO);
        assertEquals(2, inheritingTypes.size());
        assertTrue(inheritingTypes.contains(DcsBoPackageOntology.COLLECTION));
        assertTrue(inheritingTypes.contains(DcsBoPackageOntology.DATAITEM));

        inheritingTypes = presenter.getInheritingTypes(DcsBoPackageOntology.COLLECTION, DcsBoPackageOntology.PUBLISHER);
        assertEquals(1, inheritingTypes.size());
        assertTrue(inheritingTypes.contains(DcsBoPackageOntology.COLLECTION));

        inheritingTypes = presenter.getInheritingTypes(DcsBoPackageOntology.DATAITEM, DcsBoPackageOntology.DESCRIPTION);
        assertEquals(2, inheritingTypes.size());
        assertTrue(inheritingTypes.contains(DcsBoPackageOntology.DATAFILE));
        assertTrue(inheritingTypes.contains(DcsBoPackageOntology.METADATAFILE));
    }


    /**
     * Tests that package expansion is the same after it rebuilds
     */
    @Test
    public void testItemExpansionStaysAfterTreeRebuilt() {
        presenter.rebuildTreeView();
        assertTrue(view.getRoot().isExpanded());
        assertFalse(view.getRoot().getChildren().get(0).isExpanded());
        assertFalse(view.getRoot().getChildren().get(1).isExpanded());
        TreeItem before = view.getRoot();

        view.getRoot().setExpanded(true);
        view.getRoot().getChildren().get(0).setExpanded(true);
        presenter.rebuildTreeView();
        assertTrue(view.getRoot().isExpanded());
        assertTrue(view.getRoot().getChildren().get(0).isExpanded());
        assertFalse(view.getRoot().getChildren().get(1).isExpanded());
        assertNotSame(before, view.getRoot());
    }


    /**
     * Tests that after changing artifact types, the package expansion in the tree remains the same
     */
    @Test
    public void testItemExpansionStaysAfterArtifactTypeChanges() {
        presenter.rebuildTreeView();
        view.getRoot().setExpanded(true);
        view.getRoot().getChildren().get(0).setExpanded(true);
        TreeItem before = view.getRoot();
        presenter.changeType(datafile1, DcsBoPackageOntology.METADATAFILE);
        presenter.changeType(metadatafile1, DcsBoPackageOntology.DATAFILE);

        assertTrue(view.getRoot().isExpanded());
        assertTrue(view.getRoot().getChildren().get(0).isExpanded());
        assertFalse(view.getRoot().getChildren().get(1).isExpanded());
        assertNotSame(before, view.getRoot());
    }


    /**
     * Tests that when the Path checkbox is unchecked, that labels are unencoded
     */
    @Test
    public void testUnencodedNamesWhenBoxUnchecked() {
        presenter.rebuildTreeView();
        view.getFullPathCheckBox().setSelected(true);

        TreeItem<PackageArtifact> treeDi =
                findChild(findChild(findChild(view.getRoot(), collection1.getId()), dataitem1.getId()),datafile1.getId());

//        Label l = (Label)((HBox)treeDi.getGraphic()).getChildren().get(0);
//        assertTrue(l.getText()+" is encoded!", l.getText().contains("%20"));
    }

    /**
     * Test the toggling of hiding and showing ignored artifacts
     */
    @Test
    public void testBuildTreeHidingIgnoredArtifacts() {
        // build the tree view
        presenter.rebuildTreeView();
        //assert that collection 1 is not ignored yet
        assertFalse(collection1.isIgnored());
        //assert that collection is still on the tree view to start
        assertNotNull(findChild(view.getRoot(), collection1.getId()));
        //ignoring collection 1
        collection1.setIgnored(true);
        //assert that collection is still showing after being ignored
        assertNotNull(findChild(view.getRoot(), collection1.getId()));
        //switch the show ignored toggle off
        view.getShowIgnored().selectedProperty().setValue(false);
        // assert that the ignored artifact is no longer on the tree view
        assertNull(findChild(view.getRoot(),collection1.getId()));
    }

    @Test
    public void testBuildTreeShowIgnoredArtifacts() {
        // build the tree view
        presenter.rebuildTreeView();
        //assert that collection 1 is not ignored yet
        assertFalse(collection1.isIgnored());
        //assert that collection is still on the tree view to start
        assertNotNull(findChild(view.getRoot(), collection1.getId()));
        //ignoring collection 1
        collection1.setIgnored(true);
        //assert that collection is still showing after being ignored
        assertNotNull(findChild(view.getRoot(), collection1.getId()));
        //switch the show ignored toggle off
        view.getShowIgnored().selectedProperty().setValue(true);
        // assert that the ignored artifact is on the tree view
        assertNotNull(findChild(view.getRoot(),collection1.getId()));
    }

    /**
     * Helper function for getting a child with a given ID
     * @param parent the parent TreeItem
     * @param id the ID
     * @return  a child with a given ID
     */
    private TreeItem<PackageArtifact> findChild (TreeItem<PackageArtifact> parent, String id){
        for (TreeItem<PackageArtifact> ti : parent.getChildren()) {
            if (ti.getValue().getId().equals(id)) {
                parent.setExpanded(true);
                return ti;
            }
        }

        return null;
    }
}
