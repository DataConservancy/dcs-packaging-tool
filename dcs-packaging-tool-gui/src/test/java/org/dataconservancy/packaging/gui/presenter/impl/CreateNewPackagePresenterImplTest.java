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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Errors;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.OntologyLabels;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.presenter.PackageDescriptionPresenter;
import org.dataconservancy.packaging.gui.view.CreateNewPackageView;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.PackageDescriptionView;
import org.dataconservancy.packaging.gui.view.impl.CreateNewPackageViewImpl;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl;
import org.dataconservancy.packaging.tool.api.PackageDescriptionCreatorException;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;

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
    private PackageDescription description = null;
    private PackageDescriptionBuilder builder;

    @Rule
    public ExecuteContinueRule continueRule = new ExecuteContinueRule();

    @Autowired
    private Messages msgs;

    private boolean initialized = false;
    
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    private PackageDescriptionPresenterImpl packageDescriptionPresenter;

    @Before
    public void setup() throws InterruptedException {
        if (!initialized) {

            showDirectoryDialog = false;
            showNextPage = false;
            showFileDialog = false;
            chosenFile = null;
            chosenDirectory = null;

            HeaderView header = new HeaderViewImpl(labels);

            // Setup next page
            PackageDescriptionView packageDescriptionView = new PackageDescriptionViewImpl(labels, errors, messages, propertyLabels, internalProperties, "classpath:/defaultRelationships");
            packageDescriptionView.setHeaderView(header);
            packageDescriptionPresenter = new PackageDescriptionPresenterImpl(packageDescriptionView);
            factory.setPackageDescriptionPresenter(packageDescriptionPresenter);

            Controller controller = new Controller() {

                @Override
                public File showOpenDirectoryDialog(DirectoryChooser chooser) {
                    showDirectoryDialog = true;
                    return chosenDirectory;
                }

                @Override
                public void setPackageDescription(PackageDescription desc) {
                    description = desc;
                }

                @Override
                public PackageDescription getPackageDescription() {
                    return description;
                }

                @Override
                public File showOpenFileDialog(FileChooser chooser) {
                    showFileDialog = true;
                    return chosenFile;
                }

                @Override
                public PackageDescriptionPresenter showPackageDescriptionViewer() {
                    showNextPage = true;
                    return packageDescriptionPresenter;
                }
            };
            controller.setFactory(factory);
            factory.setController(controller);

            view = new CreateNewPackageViewImpl(labels);
            view.setHelp(help);

            view.setHeaderView(header);
            presenter = new CreateNewPackagePresenterImpl(view);

            presenter.setController(controller);
            presenter.setMessages(msgs);

            PackageDescription description = new PackageDescription();

            builder = mock(PackageDescriptionBuilder.class);
            when(builder.deserialize(any(InputStream.class))).thenReturn(description);

            presenter.setPackageDescriptionBuilder(builder);

            // Setup controller to handle going to the next page.
            controller.setCreateNewPackage(true);
            controller.getCreateNewPackagePagesStack().clear();
            controller.getCreateNewPackagePagesStack().push(Page.DEFINE_RELATIONSHIPS);
            packageDescriptionView.setHeaderView(header);

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
     * Tests that hitting continue without setting a content directory or an existing package description displays an error message.
     */
    @Test
    public void testContinueWithoutDirectoryGeneratesError() {
        assertFalse(showDirectoryDialog);
        view.getChooseContentDirectoryButton().fire();
        assertTrue(showDirectoryDialog);
        
        assertEquals(0, view.getErrorMessage().getText().length());
        assertFalse(showNextPage);
        view.getContinueButton().fire();
        assertFalse(showNextPage);

        assertTrue(view.getErrorMessage().getText().length() > 0);
    }
    
    /**
     * Test that continue works correctly when all required fields are set.
     */
    @Test
    public void testContinue() throws InterruptedException {
        waitForInitialization();
        assertTrue(showNextPage);

        assertEquals(0, view.getErrorMessage().getText().length());
        assertNotNull(description);
    }

    /*
     * This method will wait for the thread that creates the tree to finish so that everything is initialized before executing the tests.
     */
    private void waitForInitialization() throws InterruptedException {
        final int sleepCount = 25;
        int sleep = 0;

        while (sleep < sleepCount && description == null) {
            Thread.sleep(2000);
            sleep++;
        }
    }

    /**
     * Tests that given an exception, displayExceptionMessage() method will pass the exception's messages to the user via
     * view's error message label.
     */
    @Test
    public void testDisplayPDCreationErrorMessages() {
        String exceptionMsg = "test message";
        String exceptionDetails = "test details";
        PackageDescriptionCreatorException exception = new PackageDescriptionCreatorException(exceptionMsg, exceptionDetails);
        presenter.displayExceptionMessage(exception);
        assertFalse(showNextPage);
        assertTrue(view.getErrorMessage().getText().contains(exceptionMsg));
        assertTrue(view.getErrorMessage().getText().contains(exceptionDetails));
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

                    Platform.runLater(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               executeContinue();
                           } catch (Throwable e) {
                               rethrownException = e;
                           }
                           countDownLatch.countDown();
                       }});

                    countDownLatch.await();
                    if (rethrownException != null) {
                        throw rethrownException;
                    }
                }

                statement.evaluate();
            }

            protected void executeContinue() throws InterruptedException, IOException {
                setup();
                assertNull(description);
                assertFalse(showDirectoryDialog);

                try {
                    chosenDirectory = tmpfolder.newFolder("MOO");

                    view.getChooseContentDirectoryButton().fire();
                    assertTrue(showDirectoryDialog);

                    assertEquals(0, view.getErrorMessage().getText().length());
                    assertFalse(showNextPage);
                    view.getContinueButton().fire();
                } catch (Exception e) {

                }
            }
        }
    }
}
