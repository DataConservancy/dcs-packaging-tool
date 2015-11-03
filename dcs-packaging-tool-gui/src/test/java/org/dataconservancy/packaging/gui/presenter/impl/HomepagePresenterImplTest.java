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
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;
import org.dataconservancy.packaging.gui.view.impl.CreateNewPackageViewImpl;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.HomepageViewImpl;
import org.dataconservancy.packaging.gui.view.impl.OpenExistingPackageViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageMetadataViewImpl;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for the Homepage presenter implementation. Tests that each button navigates to the appropriate page.
 */
public class HomepagePresenterImplTest extends BaseGuiTest {

    private HomepagePresenterImpl presenter;
    private HomepageViewImpl view;

    private boolean showNextPage;
    private Controller controller;
    private HeaderView header;


    @Before
    public void setup() throws InterruptedException, IOException {
        showNextPage = false;

        controller = new Controller() {

            @Override
            public void showCreatePackageDescription() {
                showNextPage = true;
            }

            @Override
            public void showOpenExistingPackage() {
                showNextPage = true;
            }
        };
        controller.setFactory(factory);
        controller.setPackageState(new PackageState());

        factory.setController(controller);

        view = new HomepageViewImpl(labels);
        view.setHelp(help);
        header = new HeaderViewImpl(labels);
        view.setHeaderView(header);
        presenter = new HomepagePresenterImpl(view);
        presenter.setErrors(errors);

        presenter.setController(controller);


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
    public void testContinueToCreateNewPackage() {
        assertFalse(showNextPage);

        // Setup controller to handle going to the next page.
        controller.getCreateNewPackagePagesStack().clear();
        controller.getCreateNewPackagePagesStack().push(Page.CREATE_NEW_PACKAGE);
        CreateNewPackageViewImpl createNewPackageView = new CreateNewPackageViewImpl(labels);
        createNewPackageView.setHeaderView(header);
        factory.setCreateNewPackagePresenter(new CreateNewPackagePresenterImpl(createNewPackageView));

        view.getCreateNewPackageButton().fire();
        assertTrue(showNextPage);

        showNextPage = false;
    }

    /**
     * Tests that hitting continue without a domain profile displays an error message.
     */
    @Test
    public void testContinueToOpenExistingPackage() {
        assertFalse(showNextPage);

        // Setup controller to handle going to the next page.
        controller.getOpenExistingPackagePagesStack().clear();
        controller.getOpenExistingPackagePagesStack().push(Page.OPEN_EXISTING_PACKAGE);
        OpenExistingPackageView openExistingPackageView = new OpenExistingPackageViewImpl(labels);
        openExistingPackageView.setHeaderView(header);
        factory.setOpenExistingPackagePresenter(new OpenExistingPackagePresenterImpl(openExistingPackageView));

        view.getOpenExistingPackageButton().fire();
        assertTrue(showNextPage);

        showNextPage = false;
    }

}
