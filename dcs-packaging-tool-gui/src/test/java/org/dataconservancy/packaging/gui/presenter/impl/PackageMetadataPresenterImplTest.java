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
import org.dataconservancy.packaging.gui.services.PackageMetadataService;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageMetadataViewImpl;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.impl.DomainProfileStoreJenaImpl;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.profile.DcsBOProfile;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void setup() throws IOException {
        showNextPage = false;

        Controller controller = new Controller() {

            @Override
            public void goToNextPage() {
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

        view = new PackageMetadataViewImpl(help);

        HeaderView header = new HeaderViewImpl();
        view.setHeaderView(header);
        presenter = new PackageMetadataPresenterImpl(view);

        presenter.setController(controller);

        service = new PackageMetadataService(configuration);
        presenter.setPackageMetadataService(service);
        presenter.setDomainProfileStore(domainProfileStore);
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
        //view.addDomainProfileLabel("Some Domain");
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

        assertEquals(0, view.getErrorLabel().getText().length());
        assertFalse(showNextPage);
        view.getContinueButton().fire();
        assertFalse(showNextPage);
        assertTrue(view.getErrorLabel().getText().length() > 0);

    }

}
