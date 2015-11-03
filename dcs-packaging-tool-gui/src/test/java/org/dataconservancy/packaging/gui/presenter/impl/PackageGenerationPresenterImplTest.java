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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.apache.commons.lang.StringUtils;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.PackageGenerationView;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageGenerationViewImpl;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.impl.PackageImpl;
import org.dataconservancy.packaging.tool.model.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for the package generation presenter implementations.
 * Tests that items are displayed correctly, packages generation success is handled correctly,
 * package generation errors are handled correctly.
 */
public class PackageGenerationPresenterImplTest extends BaseGuiTest {

    private PackageGenerationPresenterImpl presenter;
    private PackageGenerationView view;

    @Autowired
    private PackageGenerationService packageGenerationService;

    @Autowired
    private PackageGenerationParametersBuilder packageGenerationParamsBuilder;

    private boolean outputChooserShown;

    private File testFile = null;
    private File outputDirectory = null;
    private File fakePDFile = null;
    private Controller controller;

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    @Before
    public void setup() throws IOException {

        outputChooserShown = false;
        fakePDFile = tmpfolder.newFile("faker.json");

        controller = new Controller() {
            @Override
            public void showHome(boolean clear) {
            }

            @Override
            public File showOpenFileDialog(FileChooser chooser) {
                return testFile;
            }

            @Override
            public File showOpenDirectoryDialog(DirectoryChooser chooser) {
                outputChooserShown = true;
                return outputDirectory;
            }

            @Override
            public PackageDescription getPackageDescription() {
                return new PackageDescription();
            }

            @Override
            public File getPackageDescriptionFile() { return fakePDFile; }

            @Override
            public String getAvailableProjects() { return "MOO"; }
        };
        controller.setPackageState(new PackageState());

        PackageDescriptionBuilder builder = new PackageDescriptionBuilder() {

            @Override
            public void serialize(PackageDescription packageDescription, OutputStream outputStream) throws PackageToolException {

            }

            @Override
            public PackageDescription deserialize(InputStream inputStream) throws PackageToolException {
                return new PackageDescription();
            }
        };

        controller.setFactory(factory);
        factory.setController(controller);

        controller.setPackageFilenameIllegalCharacters(factory.getConfiguration().getPackageFilenameIllegalCharacters());

        // For this test, we want a new Presenter and view for each test so that the status message is checked properly
        view = new PackageGenerationViewImpl(labels);
        view.setHelp(help);

        HeaderView headerView = new HeaderViewImpl(labels);
        view.setHeaderView(headerView);

        presenter = new PackageGenerationPresenterImpl(view);
        presenter.setPackageGenerationService(packageGenerationService);
        presenter.setPackageGenerationParametersBuilder(packageGenerationParamsBuilder);
        presenter.setPackageDescriptionBuilder(builder);
        presenter.setMessages(messages);
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
     * Ensure that event handlers do the right thing.
     */
    @Test
    public void testEventHandlers() {

        assertFalse(outputChooserShown);
        view.getSelectOutputDirectoryButton().fire();
        assertTrue(outputChooserShown);
    }


    /**
     * Tests that the builders throwing an exception is handled correctly
     *
     * @throws IOException
     */
    @Ignore
    @Test
    public void testBuilderExceptions() throws IOException {
        presenter.setPackageGenerationParametersBuilder(new PackageGenerationParametersBuilder() {

            @Override
            public PackageGenerationParameters buildParameters(InputStream in)
                    throws ParametersBuildException {
                throw new ParametersBuildException("Bad params");
            }

            @Override
            public void buildParameters(PackageGenerationParameters params,
                                        OutputStream out)
                    throws ParametersBuildException {
            }

        });

        presenter.display();

        assertTrue(view.getStatusLabel().isVisible());
        assertEquals(errors.get(ErrorKey.PARAM_LOADING_ERROR), view.getStatusLabel().getText());
    }

    /**
     * Tests that errors thrown by the package generation service are correctly handled.
     *
     * @throws IOException
     */
    @Ignore
    @Test
    public void testPackageGenerationError() throws IOException, InterruptedException {
        presenter.display();
        testFile = tmpfolder.newFile("test");

        presenter.setTestBackgroundService();
        presenter.setPackageGenerationParametersBuilder(new PackageGenerationParametersBuilder() {

            @Override
            public PackageGenerationParameters buildParameters(InputStream in)
                    throws ParametersBuildException {
                return new PackageGenerationParameters();
            }

            @Override
            public void buildParameters(PackageGenerationParameters params,
                                        OutputStream out)
                    throws ParametersBuildException {
            }

        });

        presenter.setPackageGenerationService(new PackageGenerationService() {

            @Override
            public Package generatePackage(PackageDescription desc,
                                           PackageGenerationParameters params)
                    throws PackageToolException {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_UNEXPECTED_PACKAGING_FORMAT);
            }

        });

        outputDirectory = new File("./");
        //view.getPackageNameField().textProperty().setValue("tempfile");
        view.getSelectOutputDirectoryButton().fire();

        assertFalse(view.getStatusLabel().isVisible());

        view.getContinueButton().fire();

        assertTrue(view.getStatusLabel().isVisible());
        assertEquals(errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR) + " " + PackagingToolReturnInfo.PKG_UNEXPECTED_PACKAGING_FORMAT.stringMessage(), view.getStatusLabel().getText());
    }

    /**
     * Tests that a successful package generation results in the correct message.
     *
     * @throws IOException
     */
    @Ignore
    @Test
    public void testSuccessfulPackageGeneration() throws IOException, InterruptedException {
        //Check if what will become the package file already exists and if so delete it, this prevents the overwrite popup from appearing.
        File createdFile = new File("./thePackage.tar.gz");
        if (createdFile.exists()) {
            createdFile.delete();
        }

        testFile = tmpfolder.newFile("test");

        PrintWriter fileOut = new PrintWriter(testFile);

        fileOut.println("foo");
        fileOut.close();

        final Package pkg = new PackageImpl(testFile, "thePackage", null);

        final PackageGenerationParameters pkgParams = new PackageGenerationParameters();
        pkgParams.addParam(GeneralParameterNames.PACKAGE_NAME, "thePackage");
        pkgParams.addParam(GeneralParameterNames.ARCHIVING_FORMAT, "tar");
        pkgParams.addParam(GeneralParameterNames.COMPRESSION_FORMAT, "gz");
        pkgParams.addParam(BagItParameterNames.PKG_BAG_DIR, "./thePackage.tar.gz");

        presenter.setTestBackgroundService();

        presenter.setPackageGenerationParametersBuilder(new PackageGenerationParametersBuilder() {

            @Override
            public PackageGenerationParameters buildParameters(InputStream in)
                  throws ParametersBuildException {
                return pkgParams;
            }

            @Override
            public void buildParameters(PackageGenerationParameters params,
                                      OutputStream out)
                  throws ParametersBuildException {
            }

        });

        presenter.setPackageGenerationService(new PackageGenerationService() {

            @Override
            public Package generatePackage(PackageDescription desc,
                                         PackageGenerationParameters params)
                  throws PackageToolException {
                return pkg;
            }

        });
        presenter.display();

        outputDirectory = new File("./");

        //view.getPackageNameField().setText("thePackage");
        view.getStatusLabel().setVisible(false);
        view.getSelectOutputDirectoryButton().fire();

        view.getContinueButton().fire();

        assertTrue(createdFile.exists());
        assertFalse(view.getStatusLabel().isVisible());

        //Clean up the newly created file
        if (!createdFile.delete()) {
            createdFile.deleteOnExit();
        }
    }

    @Ignore
    @Test
    public void testEmptyOutputDirectoryProducesEmptyOutputFile() {
        outputDirectory = null;
        //view.getPackageNameField().setText("fake");

        assertTrue(view.getCurrentOutputDirectoryTextField().getText().isEmpty());
    }

    @Ignore
    @Test
    public void testEmptyPackageNameProducesEmptyOutputFile() {
        presenter.display();

        outputDirectory = new File("./");
        view.getSelectOutputDirectoryButton().fire();
        //view.getPackageNameField().setText("");

        assertTrue(view.getCurrentOutputDirectoryTextField().getText().isEmpty());
    }

    @Ignore
    @Test
    public void testOutputFileExistsIfPackageNameAndDirectoryValid() {
        presenter.display();

        //view.getPackageNameField().setText("fakefile");
        outputDirectory = new File("./");
        view.getSelectOutputDirectoryButton().fire();

        assertFalse(view.getCurrentOutputDirectoryTextField().getText().isEmpty());
        assertTrue(view.getCurrentOutputDirectoryTextField().getText().endsWith("fakefile.tar.gz"));
    }

    @Ignore
    @Test
    public void testIllegalCharactersAreNotAllowedInPackage() {
        presenter.display();

        //view.getPackageNameField().setText("fake*file");
        outputDirectory = new File("./");
        view.getSelectOutputDirectoryButton().fire();

        assertTrue(view.getCurrentOutputDirectoryTextField().getText().isEmpty());
        assertTrue(view.getStatusLabel().isVisible());
        assertTrue(view.getStatusLabel().getText().startsWith(errors.get(ErrorKey.PACKAGE_FILENAME_HAS_ILLEGAL_CHARACTERS)));
    }

    @Ignore
    @Test
    public void testLongFilenameShowsWarning() {
        presenter.display();

        //view.getPackageNameField().setText(StringUtils.rightPad("fakepackage", 300, "xyz"));
        outputDirectory = new File("/tmp");
        view.getSelectOutputDirectoryButton().fire();

        assertFalse(view.getCurrentOutputDirectoryTextField().getText().isEmpty());
        assertTrue(view.getStatusLabel().isVisible());

        assertEquals(messages.formatFilenameLengthWarning(view.getCurrentOutputDirectoryTextField().getText().length()), view.getStatusLabel().getText());
    }
}
