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

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.view.CreateNewPackageView;
import org.dataconservancy.packaging.gui.view.EditPackageContentsView;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.impl.CreateNewPackageViewImpl;
import org.dataconservancy.packaging.gui.view.impl.EditPackageContentsViewImpl;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for the create new package presenter implementation. Tests that elements are retrieved correctly, properties are set correctly,
 * and navigation is handled correctly. 
 */
public class CreateNewPackagePresenterImplTest extends BaseGuiTest {

    private CreateNewPackagePresenterImpl presenter;
    private CreateNewPackageView view;

    private boolean showDirectoryDialog;
    private boolean showFileDialog;
    private File chosenFile;
    
    private boolean showNextPage;
    private File chosenDirectory;
    private Node root = null;
    private PackageState packageState;

    @Rule
    public ExecuteContinueRule continueRule = new ExecuteContinueRule();

    private boolean initialized = false;
    
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    private EditPackageContentsPresenterImpl packageDescriptionPresenter;

    @Before
    public void setup() throws InterruptedException, IOException {
        if (!initialized) {

            showDirectoryDialog = false;
            showNextPage = false;
            showFileDialog = false;
            chosenFile = null;
            chosenDirectory = null;

            HeaderView header = new HeaderViewImpl();

            // Setup next page
            EditPackageContentsView editPackageContentsView = new EditPackageContentsViewImpl(internalProperties, "classpath:/userProperties.json", help);
            editPackageContentsView.setHeaderView(header);
            packageDescriptionPresenter = new EditPackageContentsPresenterImpl(editPackageContentsView);
            factory.setEditPackageContentsPresenter(packageDescriptionPresenter);
            packageState = new PackageState();

            Controller controller = new Controller() {

                @Override
                public File showOpenDirectoryDialog(DirectoryChooser chooser) {
                    showDirectoryDialog = true;
                    return chosenDirectory;
                }

                @Override
                public File showOpenFileDialog(FileChooser chooser) {
                    showFileDialog = true;
                    return chosenFile;
                }

                @Override
                public void showEditPackageContents() {
                    showNextPage = true;
                }

                @Override
                public PackageState getPackageState() {
                    return packageState;
                }

                @Override
                public DomainProfileService getDomainProfileService() {
                    return mock(DomainProfileService.class);
                }
            };
            controller.setFactory(factory);
            factory.setController(controller);

            view = new CreateNewPackageViewImpl(help);

            view.setHeaderView(header);
            presenter = new CreateNewPackagePresenterImpl(view);

            presenter.setController(controller);

            IPMService ipmService = mock(IPMService.class);
            when(ipmService.createTreeFromFileSystem(any(Path.class))).thenReturn(root);
            presenter.setIpmService(ipmService);

            // Setup controller to handle going to the next page.
            controller.setCreateNewPackage(true);
            controller.getCreateNewPackagePagesStack().clear();
            controller.getCreateNewPackagePagesStack().push(Page.EDIT_PACKAGE_CONTENTS);
            editPackageContentsView.setHeaderView(header);

            initialized = true;
        }
    }

    /**
     * Ensure the presenter displays a node.
     */
    @Test
    public void testDisplay() {
        assertNotNull(presenter.display());
    }

    /**
     * Ensure that event handlers to the right thing.
     */
    @Test
    public void testChooseDirectoryDialog() {
        
        assertFalse(showDirectoryDialog);
        chosenDirectory = new File("test1");
        view.getChooseContentDirectoryButton().fire();
        assertTrue(showDirectoryDialog);
    }

    /**
     * Tests that given an exception, displayExceptionMessage() method will pass the exception's messages to the user via
     * view's error message label.
     */
    @Test
    public void testDisplayPDCreationErrorMessages() {
        String exceptionMsg = "test message";
        IOException exception = new IOException(exceptionMsg);
        presenter.displayExceptionMessage(exception);
        assertFalse(showNextPage);
        assertTrue(view.getErrorLabel().getText().contains(exceptionMsg));
    }

    private class ExecuteContinueRule implements TestRule {

        @Override
        public Statement apply(Statement statement, Description description) {

            return new ContinueStatement(statement, description.getMethodName());
        }

        private class ContinueStatement extends Statement {

            private final Statement statement;
            private final String methodName;
            private Throwable rethrownException = null;

            public ContinueStatement(Statement aStatement, String methodName) {
                statement = aStatement;
                this.methodName = methodName;
            }


            @Override
            public void evaluate() throws Throwable {
                if (methodName.equalsIgnoreCase("testContinue")) {
                    final CountDownLatch countDownLatch = new CountDownLatch(1);

                    Platform.runLater(() -> {
                        try {
                            executeContinue();
                        } catch (Throwable e) {
                            rethrownException = e;
                        }
                        countDownLatch.countDown();
                    });

                    countDownLatch.await();
                    if (rethrownException != null) {
                        throw rethrownException;
                    }
                }

                statement.evaluate();
            }

            protected void executeContinue() throws InterruptedException, IOException {
                setup();
                assertNull(packageState.getPackageTree());
                assertFalse(showDirectoryDialog);

                try {
                    chosenDirectory = tmpfolder.newFolder("MOO");

                    view.getChooseContentDirectoryButton().fire();
                    assertTrue(showDirectoryDialog);

                    assertEquals(0, view.getErrorLabel().getText().length());
                    assertFalse(showNextPage);
                    view.getContinueButton().fire();
                } catch (Exception e) {

                }
            }
        }
    }
}
