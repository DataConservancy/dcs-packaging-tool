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

import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.presenter.impl.EditPackageContentsPresenterImpl;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.profile.DcsBOProfile;
import org.dataconservancy.packaging.tool.profile.util.DcBoIpmFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that the package description view imple initializes all fields correctly. 
 */
@SuppressWarnings("unchecked")
public class EditPackageContentsViewImplTest extends BaseGuiTest {
    private EditPackageContentsViewImpl view;

    private EditPackageContentsPresenterImpl presenter;


    @Autowired
    @Qualifier("profileService")
    private DomainProfileService profileService;

    @Autowired
    @Qualifier("ipmService")
    private IPMService ipmService;

    @Autowired
    @Qualifier("domainProfileStore")
    private DomainProfileStore domainProfileStore;

    private Node project;
    private Node collection;
    private Node dataItem;
    private Node dataFile;

    @Before
    public void setup() {
        DcBoIpmFactory boFactory = new DcBoIpmFactory();
        project = boFactory.createSmallLinearTree();
        collection = project.getChildren().get(0);
        dataItem = collection.getChildren().get(0);
        dataFile = dataItem.getChildren().get(0);

        view = new EditPackageContentsViewImpl(internalProperties, "classpath:/defaultRelationships", help);
        HeaderView headerView = new HeaderViewImpl();
        view.setIpmService(ipmService);
        view.setHeaderView(headerView);

        PackageState state = new PackageState();
        state.setDomainProfileIdList(Collections.singletonList(new DcsBOProfile().getIdentifier()));
        Controller controller = new Controller() {
            @Override
            public File showSaveFileDialog(FileChooser chooser) {
                return null;
            }

            @Override
            public DomainProfileService getDomainProfileService() {
                return profileService;
            }

            @Override
            public Node getPackageTree() {
                return project;
            }

            @Override
            public PackageState getPackageState() {
                return state;
            }
        };

        controller.setDomainProfileStore(domainProfileStore);
        controller.setPackageState(state);
        controller.initializeDomainStoreAndServices(null);

        controller.getDomainProfileService().assignNodeTypes(controller.getPrimaryDomainProfile(), project);

        presenter = new EditPackageContentsPresenterImpl(view);
        presenter.setController(controller);
        presenter.setInternalProperties(internalProperties);
        presenter.setIpmService(ipmService);
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
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(collection)), true);

        assertTrue(collection.isIgnored());
        assertTrue(dataItem.isIgnored());
        assertTrue(dataFile.isIgnored());
    }

    @Test
    public void testUnignoreCollection() {
        presenter.rebuildTreeView();

        //First ignore the collection
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(collection)), true);

        assertTrue(collection.isIgnored());
        assertTrue(dataFile.isIgnored());
        assertTrue(dataItem.isIgnored());

        //Now unignore the collection and check that all children are also unignored.
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(collection)), false);

        assertFalse(dataItem.isIgnored());
        assertFalse(dataFile.isIgnored());
    }

    @Test
    public void testIgnoreDataItem() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataItem)), true);

        //Parent should not be ignored
        assertFalse(collection.isIgnored());

        //Item and it's children should be ignored
        assertTrue(dataFile.isIgnored());
        assertTrue(dataItem.isIgnored());
    }

    @Test
    public void testUnIgnoreDataItem() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataItem)), true);

        //Parent should not be ignored
        assertFalse(collection.isIgnored());

        //Item and it's children should be ignored
        assertTrue(dataItem.isIgnored());
        assertTrue(dataFile.isIgnored());

        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataItem)), false);

        //Parent should not be ignored
        assertFalse(collection.isIgnored());

        //Item and it's children should be ignored
        assertFalse(dataFile.isIgnored());
        assertFalse(dataItem.isIgnored());
    }

    @Test
    public void testUnignoreDataItemUnignoresParent() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(collection)), true);

        assertTrue(collection.isIgnored());
        assertTrue(dataItem.isIgnored());
        assertTrue(dataFile.isIgnored());

        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataItem)), false);

        //Parent should not be ignored
        assertFalse(collection.isIgnored());

        //Item and it's children should not be ignored
        assertFalse(dataItem.isIgnored());
        assertFalse(dataFile.isIgnored());
    }

    @Test
    public void testIgnoreDataFile() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataFile)), true);

        //Parent should not be ignored
        assertFalse(collection.isIgnored());
        assertFalse(dataItem.isIgnored());

        //Item should be ignored
        assertTrue(dataFile.isIgnored());
    }

    @Test
    public void testUnIgnoreDataFile() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataFile)), true);

        //Parent should not be ignored
        assertFalse(collection.isIgnored());
        assertFalse(dataItem.isIgnored());

        //Item should be ignored
        assertTrue(dataFile.isIgnored());

        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataFile)), false);

        //Parent should not be ignored
        assertFalse(collection.isIgnored());
        assertFalse(dataItem.isIgnored());

        //Item should not be ignored
        assertFalse(dataFile.isIgnored());
    }

    @Test
    public void testUnIgnoreDataFileUnIgnoresParents() {
        presenter.rebuildTreeView();
        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(collection)), true);

        assertTrue(collection.isIgnored());
        assertTrue(dataItem.isIgnored());
        assertTrue(dataFile.isIgnored());

        view.toggleItemIgnore(FXCollections.observableArrayList(presenter.findItem(dataFile)), false);

        //Parents should not be ignored
        assertFalse(collection.isIgnored());
        assertFalse(dataItem.isIgnored());

        //Item should not be ignored
        assertFalse(dataFile.isIgnored());
    }
}