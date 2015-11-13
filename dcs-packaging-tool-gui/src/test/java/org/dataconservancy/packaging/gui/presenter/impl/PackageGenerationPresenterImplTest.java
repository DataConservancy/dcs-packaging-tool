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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.PackageGenerationView;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageGenerationViewImpl;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.impl.PackageImpl;
import org.dataconservancy.packaging.tool.model.*;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
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
    private Controller controller;
    private PackageState packageState;
    private PackageGenerationParameters params;

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    @Before
    public void setup() throws IOException, URISyntaxException {

        outputChooserShown = false;

        controller = mock(Controller.class);
        when(controller.showOpenFileDialog(any())).thenReturn(testFile);
        when(controller.showOpenDirectoryDialog(any())).thenReturn(outputDirectory);

        params = new PackageGenerationParameters();

        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.getLocation()).thenReturn(tmpfolder.newFile().toURI());
        Node packageTree = mock(Node.class);
        when(packageTree.getFileInfo()).thenReturn(fileInfo);
        when(controller.getPackageTree()).thenReturn(packageTree);

        packageState = mock(PackageState.class);
        when(packageState.getPackageName()).thenReturn("packageName");

        when(controller.getPackageState()).thenReturn(packageState);

        controller.setFactory(factory);
        factory.setController(controller);

        // For this test, we want a new Presenter and view for each test so that the status message is checked properly
        view = new PackageGenerationViewImpl();
        view.setHelp(help);

        HeaderView headerView = new HeaderViewImpl();
        view.setHeaderView(headerView);

        presenter = new PackageGenerationPresenterImpl(view);
        presenter.setPackageGenerationService(packageGenerationService);
        presenter.setPackageGenerationParametersBuilder(packageGenerationParamsBuilder);
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
     * Test that input values from GUI form are gathered  for package generation process.
     */
    @Test
    public void testFormInputCollectedCorrectly() {

        presenter.display();
        view.getArchiveToggleGroup().getToggles().get(1).setSelected(true);
        String archiveChoice = view.getArchiveToggleGroup().getToggles().get(1).getUserData().toString();
        view.getCompressionToggleGroup().getToggles().get(0).setSelected(true);
        String compressionChoice = view.getCompressionToggleGroup().getToggles().get(0).getUserData().toString();
        view.getSerializationToggleGroup().getToggles().get(1).setSelected(true);
        String serializationChoice = view.getSerializationToggleGroup().getToggles().get(1).getUserData().toString();
        view.getMd5CheckBox().setSelected(true);

        view.getContinueButton().fire();
        params = presenter.getGenerationParams();
        assertEquals(1, params.getParam(GeneralParameterNames.ARCHIVING_FORMAT).size());
        assertTrue(params.getParam(GeneralParameterNames.ARCHIVING_FORMAT).contains(archiveChoice));
        assertEquals(1, params.getParam(GeneralParameterNames.COMPRESSION_FORMAT).size());
        assertTrue(params.getParam(GeneralParameterNames.COMPRESSION_FORMAT).contains(compressionChoice));
        assertEquals(1, params.getParam(GeneralParameterNames.REM_SERIALIZATION_FORMAT).size());
        assertTrue(params.getParam(GeneralParameterNames.REM_SERIALIZATION_FORMAT).contains(serializationChoice));
        System.out.println("test");
    }

    /**
     * Tests that status label is updated appropriately when package provided or configured generation parameters files
     * could not be parsed
     *
     * @throws IOException
     */
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

        assertTrue(view.getErrorLabel().isVisible());
        assertEquals(TextFactory.getText(ErrorKey.PARAM_LOADING_ERROR), view.getErrorLabel().getText());
    }

    /**
     * Tests that errors thrown by the package generation service are correctly handled.
     *
     * @throws IOException
     */

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

        presenter.setPackageGenerationService((desc, params) -> {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_UNEXPECTED_PACKAGING_FORMAT);
        });

        outputDirectory = new File("./");
        view.getSelectOutputDirectoryButton().fire();
        assertFalse(view.getErrorLabel().isVisible());

        view.getContinueButton().fire();

        assertTrue(view.getErrorLabel().isVisible());
        assertEquals(TextFactory.getText(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR)
                + " " + PackagingToolReturnInfo.PKG_UNEXPECTED_PACKAGING_FORMAT.stringMessage(),
                view.getErrorLabel().getText());
    }

    /**
     * Tests that a successful package generation results in the correct message.
     *
     * @throws IOException
     */
    @Test
    public void testSuccessfulPackageGeneration() throws IOException, InterruptedException {
        File packageLocation = tmpfolder.newFolder();
        //Check if what will become the package file already exists and if so delete it, this prevents the overwrite popup from appearing.
        File createdFile = new File(packageLocation, controller.getPackageState().getPackageName()+".tar.gz");
        if (createdFile.exists()) {
            createdFile.delete();
        }

        testFile = tmpfolder.newFile("test");
        PrintWriter fileOut = new PrintWriter(testFile);
        fileOut.println("foo");
        fileOut.close();

        final Package pkg = new PackageImpl(testFile, controller.getPackageState().getPackageName(), null);

        final PackageGenerationParameters pkgParams = new PackageGenerationParameters();
        pkgParams.addParam(GeneralParameterNames.PACKAGE_NAME, controller.getPackageState().getPackageName());
        pkgParams.addParam(GeneralParameterNames.ARCHIVING_FORMAT, "tar");
        pkgParams.addParam(GeneralParameterNames.COMPRESSION_FORMAT, "gz");
        pkgParams.addParam(GeneralParameterNames.REM_SERIALIZATION_FORMAT, "xml");
        pkgParams.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocation.getAbsolutePath());

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

        presenter.setPackageGenerationService((state, params) -> pkg);
        presenter.display();

        outputDirectory = packageLocation;

        view.getErrorLabel().setVisible(false);
        view.getSelectOutputDirectoryButton().fire();

        view.getContinueButton().fire();

        assertTrue(createdFile.exists());
        assertFalse(view.getErrorLabel().isVisible());

        //Clean up the newly created file
        if (!createdFile.delete()) {
            createdFile.deleteOnExit();
        }
    }



}
