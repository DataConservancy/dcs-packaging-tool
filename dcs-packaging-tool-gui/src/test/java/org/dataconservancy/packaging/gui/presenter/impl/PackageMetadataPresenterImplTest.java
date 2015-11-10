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

package org.dataconservancy.packaging.gui.presenter.impl;

import javafx.scene.control.TextField;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.services.PackageMetadataService;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.impl.CreateNewPackageViewImpl;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageMetadataViewImpl;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.impl.DomainProfileStoreJenaImpl;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.profile.DcsBOProfile;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * Test for the package metadata presenter implementation. Tests that validation for required fields works
 * and navigation is handled correctly.
 */

public class PackageMetadataPresenterImplTest extends BaseGuiTest {

    private PackageMetadataPresenterImpl presenter;
    private PackageMetadataViewImpl view;

    private PackageMetadataService service;

    private boolean showNextPage;

    @Autowired
    private Configuration configuration;

    @Before
    public void setup() throws InterruptedException, IOException {
            showNextPage = false;

            Controller controller = new Controller() {

                @Override
                public void showCreatePackageDescription() {
                    showNextPage = true;
                }
            };
            controller.setFactory(factory);
            controller.setPackageState(new PackageState());

            factory.setController(controller);

            DomainProfileStore domainProfileStore = new DomainProfileStoreJenaImpl(){

              @Override
              public List<DomainProfile> getPrimaryDomainProfiles(){
                  List<DomainProfile> domainProfileList= new ArrayList<>();
                  domainProfileList.add(new DcsBOProfile());
                  return domainProfileList;
              }

            };

            view = new PackageMetadataViewImpl();
            view.setHelp(help);
            HeaderView header = new HeaderViewImpl();
            view.setHeaderView(header);
            presenter = new PackageMetadataPresenterImpl(view);

            presenter.setController(controller);

            service = new PackageMetadataService(configuration);
            presenter.setPackageMetadataService(service);
            presenter.setDomainProfileStore(domainProfileStore);
            // Setup controller to handle going to the next page.
            controller.setCreateNewPackage(true);
            controller.getCreateNewPackagePagesStack().clear();
            controller.getCreateNewPackagePagesStack().push(Page.CREATE_NEW_PACKAGE);
            CreateNewPackageViewImpl createNewPackageView = new CreateNewPackageViewImpl();
            createNewPackageView.setHeaderView(header);
            factory.setCreateNewPackagePresenter(new CreateNewPackagePresenterImpl(createNewPackageView));

    }


    /**
     * Ensure the presenter displays a node.
     */
    @Test
    public void testDisplay() {
        assertNotNull(presenter.display());
    }

    /**
     * Tests that hitting continue without a package name displays an error message.
     */
    @Test
    public void testContinueWithoutPackageName() {
        presenter.display();

        view.getPackageNameField().setText(null);
        view.addDomainProfileLabel("Some Domain");
        for (PackageMetadata pm : service.getRequiredPackageMetadata()) {
            view.getAllDynamicFields().stream().filter(node -> node.getId().equals(pm.getName())).filter(node -> node instanceof TextField).forEach(node -> ((TextField) node).setText("Some Text"));
        }

        assertEquals(0, view.getErrorLabel().getText().length());
        assertFalse(showNextPage);
        view.getContinueButton().fire();
        assertFalse(showNextPage);
        assertTrue(view.getErrorLabel().getText().length() > 0);
    }

    /**
     * Tests that hitting continue without a domain profile displays an error message.
     */
    @Test
    public void testContinueWithoutDomainProfile() {
        presenter.display();

        view.getPackageNameField().setText("Some name");
        for (PackageMetadata pm : service.getRequiredPackageMetadata()) {
            view.getAllDynamicFields().stream().filter(node -> node.getId().equals(pm.getName())).filter(node -> node instanceof TextField).forEach(node -> ((TextField) node).setText("Some Text"));
        }

        assertEquals(0, view.getErrorLabel().getText().length());
        assertFalse(showNextPage);
        view.getContinueButton().fire();
        assertFalse(showNextPage);
        assertTrue(view.getErrorLabel().getText().length() > 0);
    }

    /**
     * Tests that hitting continue without dynamic required fields displays an error message.
     */
    @Test
    public void testContinueWithoutDynamicRequiredFields() {
        presenter.display();

        view.getPackageNameField().setText("Some name");
        view.addDomainProfileLabel("Some Domain");

        assertEquals(0, view.getErrorLabel().getText().length());
        assertFalse(showNextPage);
        view.getContinueButton().fire();
        assertFalse(showNextPage);
        assertTrue(view.getErrorLabel().getText().length() > 0);

    }

}
