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

import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.HomepageViewImpl;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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


    @Before
    public void setup() throws InterruptedException, IOException {
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

        view = new HomepageViewImpl(help);
        HeaderView header = new HeaderViewImpl();
        view.setHeaderView(header);
        presenter = new HomepagePresenterImpl(view);

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
     * Tests that hitting create a new package goes to the next page
     */
    @Test
    public void testContinueToCreateNewPackage() {
        assertFalse(showNextPage);

        view.getCreateNewPackageButton().fire();
        assertTrue(showNextPage);

        showNextPage = false;
    }

    /**
     * Tests that hitting open existing package goes to the next screen.
     */
    @Test
    public void testContinueToOpenExistingPackage() {
        assertFalse(showNextPage);

        view.getOpenExistingPackageButton().fire();
        assertTrue(showNextPage);

        showNextPage = false;
    }

}
