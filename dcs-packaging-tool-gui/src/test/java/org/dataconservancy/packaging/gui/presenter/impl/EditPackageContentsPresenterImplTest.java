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

import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Factory;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.impl.EditPackageContentsViewImpl;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageGenerationViewImpl;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.profile.util.DcBoIpmFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the pacakge description presenter implementation. Which tests that elements are displayed correctly,
 * the tree is generated correctly, and inheritance works correctly. 
 * Note currently the popup code is not being tested. 
 */
public class EditPackageContentsPresenterImplTest extends BaseGuiTest {

    private EditPackageContentsPresenterImpl presenter;
    private EditPackageContentsViewImpl view;

    private boolean goToNextPage;

    @Autowired
    @Qualifier("profileService")
    private DomainProfileService profileService;

    @Autowired
    @Qualifier("ipmService")
    private IPMService ipmService;

    @Autowired
    @Qualifier("domainProfileObjectStore")
    private DomainProfileObjectStore domainProfileObjectStore;

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    private Node collection;
    private Node dataFile;

    @Before
    public void setup() throws IOException {
        DcBoIpmFactory boFactory = new DcBoIpmFactory();
        Node project = boFactory.createSmallLinearTree();

        collection = project.getChildren().get(0);

        Node dataItem = collection.getChildren().get(0);

        dataFile = dataItem.getChildren().get(0);

        project.walk(domainProfileObjectStore::updateObject);
        goToNextPage = false;

        Configuration configuration = new Configuration() {
            @Override
            public String getDisciplineMap(){ return "MOO";}

        };

        Factory factory = new Factory();
        factory.setConfiguration(configuration);

        PackageState state = new PackageState();

        Controller controller = new Controller() {

            @Override
            public File showSaveFileDialog(FileChooser chooser) {
                try {
                    return tmpfolder.newFile("test");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void showGeneratePackage() {
                goToNextPage = true;
            }

            @Override
            public DomainProfileService getDomainProfileService() {
                return profileService;
            }

            @Override
            public PackageState getPackageState() {
                return state;
            }
        };

        controller.setFactory(factory);
        controller.setPackageTree(project);

        // For this test, we want a new Presenter and view for each test so that the status message is checked properly
        view = new EditPackageContentsViewImpl(internalProperties, "classpath:/defaultRelationships", help);
        view.setIpmService(ipmService);

        HeaderView headerView = new HeaderViewImpl();
        factory.setHeaderView(headerView);
        view.setHeaderView(headerView);
        
        presenter = new EditPackageContentsPresenterImpl(view);
        presenter.setController(controller);
        presenter.setInternalProperties(internalProperties);
        view.setPresenter(presenter);
        // Setup controller to handle going to the next page.
        controller.setCreateNewPackage(true);
        controller.getCreateNewPackagePagesStack().clear();
        controller.getCreateNewPackagePagesStack().push(Page.GENERATE_PACKAGE);
        PackageGenerationViewImpl packageGenerationView = new PackageGenerationViewImpl(help);
        packageGenerationView.setHeaderView(headerView);
        factory.setPackageGenerationPresenter(new PackageGenerationPresenterImpl(packageGenerationView));
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
        assertNotNull(presenter.display());
    }
    
    /**
     * Tests successful tree generation
     * @throws IOException
     */
    @Test
    public void testSuccessfulTreeGeneration() throws IOException, InterruptedException {
        presenter.rebuildTreeView();
      
        assertEquals(1, view.getRoot().getChildren().size());

        view.getRoot().setExpanded(true);
    }

    /**
     * Tests that package expansion is the same after it rebuilds
     */
    @Test
    public void testItemExpansionStaysAfterTreeRebuilt() {
        presenter.rebuildTreeView();
        assertTrue(view.getRoot().isExpanded());
        assertFalse(view.getRoot().getChildren().get(0).isExpanded());
        TreeItem before = view.getRoot();

        view.getRoot().setExpanded(true);
        view.getRoot().getChildren().get(0).setExpanded(true);
        presenter.rebuildTreeView();
        assertTrue(view.getRoot().isExpanded());
        assertTrue(view.getRoot().getChildren().get(0).isExpanded());
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
        presenter.changeType(FXCollections.observableArrayList(dataFile), profileService.getNodeTransforms(dataFile).get(0));

        assertTrue(view.getRoot().isExpanded());
        assertTrue(view.getRoot().getChildren().get(0).isExpanded());
        assertNotSame(before, view.getRoot());
    }

    /**
     * Test the toggling of hiding and showing ignored artifacts
     */
    @Test
    public void testBuildTreeHidingIgnoredArtifacts() {
        // build the tree view
        presenter.rebuildTreeView();
        //assert that collection 1 is not ignored yet
        assertFalse(collection.isIgnored());
        //assert that collection is still on the tree view to start
        assertNotNull(findChild(view.getRoot(), collection.getIdentifier()));
        //ignoring collection 1
        collection.setIgnored(true);
        //assert that collection is still showing after being ignored
        assertNotNull(findChild(view.getRoot(), collection.getIdentifier()));
        //switch the show ignored toggle off
        view.getShowIgnored().selectedProperty().setValue(false);
        // assert that the ignored artifact is no longer on the tree view
        assertNull(findChild(view.getRoot(),collection.getIdentifier()));
    }

    @Test
    public void testBuildTreeShowIgnoredArtifacts() {
        // build the tree view
        presenter.rebuildTreeView();
        //assert that collection 1 is not ignored yet
        assertFalse(collection.isIgnored());
        //assert that collection is still on the tree view to start
        assertNotNull(findChild(view.getRoot(), collection.getIdentifier()));
        //ignoring collection 1
        collection.setIgnored(true);
        //assert that collection is still showing after being ignored
        assertNotNull(findChild(view.getRoot(), collection.getIdentifier()));
        //switch the show ignored toggle off
        view.getShowIgnored().selectedProperty().setValue(true);
        // assert that the ignored artifact is on the tree view
        assertNotNull(findChild(view.getRoot(),collection.getIdentifier()));
    }

    /**
     * Helper function for getting a child with a given ID
     * @param parent the parent TreeItem
     * @param id the ID
     * @return  a child with a given ID
     */
    private TreeItem<Node> findChild (TreeItem<Node> parent, URI id){
        for (TreeItem<Node> ti : parent.getChildren()) {
            if (ti.getValue().getIdentifier().equals(id)) {
                parent.setExpanded(true);
                return ti;
            }
        }

        return null;
    }
}
