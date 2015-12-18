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

import static junit.framework.TestCase.assertTrue;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.ARCHIVING_FORMAT;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.COMPRESSION_FORMAT;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.PACKAGE_LOCATION;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.REM_SERIALIZATION_FORMAT;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.SERIALIZATION_FORMAT.XML;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.SERIALIZATION_FORMAT.values;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.SERIALIZATION_FORMAT.TURTLE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import javafx.scene.control.Toggle;
import javafx.stage.DirectoryChooser;

import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.PackageGenerationView;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.PackageGenerationViewImpl;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.impl.PackageImpl;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageGenerationParametersBuilder;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;
import org.dataconservancy.packaging.tool.model.ParametersBuildException;
import org.dataconservancy.packaging.tool.model.PropertiesConfigurationParametersBuilder;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
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

    private File testFile = null;
    private File outputDirectory = null;
    private Controller controller;
    private PackageGenerationParameters params;

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    @Before
    public void setup() throws IOException, URISyntaxException {

        controller = mock(Controller.class);
        when(controller.showOpenFileDialog(any())).thenReturn(testFile);
        when(controller.showOpenDirectoryDialog(any())).thenReturn(outputDirectory);

        params = new PackageGenerationParameters();

        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.getLocation()).thenReturn(tmpfolder.newFile().toURI());
        Node packageTree = mock(Node.class);
        when(packageTree.getFileInfo()).thenReturn(fileInfo);
        when(controller.getPackageTree()).thenReturn(packageTree);

        PackageState packageState = mock(PackageState.class);
        when(packageState.getPackageName()).thenReturn("packageName");

        when(controller.getPackageState()).thenReturn(packageState);

        controller.setFactory(factory);
        factory.setController(controller);

        // For this test, we want a new Presenter and view for each test so that the status message is checked properly
        view = new PackageGenerationViewImpl();

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
        // Mock the PackageGenerationService to do nothing.
        PackageGenerationService mockGenerationSvc = mock(PackageGenerationService.class);

        // Update the Controller mock to return an non-null File
        when(controller.showOpenDirectoryDialog(any())).thenReturn(outputDirectory);

        presenter.setTestBackgroundService();
        presenter.setPackageGenerationService(mockGenerationSvc);
        
        
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
        assertEquals(1, params.getParam(REM_SERIALIZATION_FORMAT).size());
        assertEquals(params.getParam(REM_SERIALIZATION_FORMAT).get(0), serializationChoice);
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

        assertTrue(view.getErrorTextArea().isVisible());
        assertEquals(TextFactory.getText(ErrorKey.PARAM_LOADING_ERROR), view.getErrorTextArea().getText());
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
        try {
            view.getSelectOutputDirectoryButton().fire();
        } catch (PackageToolException e) {
            assertFalse(view.getErrorTextArea().isVisible());

            view.getContinueButton().fire();

            assertTrue(view.getErrorTextArea().isVisible());
            assertEquals(TextFactory.getText(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR)
                            + " " + PackagingToolReturnInfo.PKG_UNEXPECTED_PACKAGING_FORMAT.stringMessage(),
                    view.getErrorTextArea().getText());
        }
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
        pkgParams.addParam(REM_SERIALIZATION_FORMAT, "XML");
        pkgParams.addParam(PACKAGE_LOCATION, packageLocation.getAbsolutePath());

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

        view.getErrorTextArea().setVisible(false);
        view.getSelectOutputDirectoryButton().fire();

        view.getContinueButton().fire();

        assertTrue(createdFile.exists());
        assertFalse(view.getErrorTextArea().isVisible());

        //Clean up the newly created file
        if (!createdFile.delete()) {
            createdFile.deleteOnExit();
        }
    }

    /**
     * <p>
     * Tests the side affects of selecting toggles and firing buttons.  Essentially this test is about testing the
     * event handlers attached to various UI elements.
     * </p>
     * <p>
     * PackageGenerationPresenterImpl is full of event handlers that manipulate the state of its components, notably
     * PackageGenerationParameters.  There was a bug where the user selected an output directory for an exploded
     * archive, resulting in erronous information being written to the PackageGenerationParameters by an event handler.
     * This test ensures this specific bug won't resurface, while also somewhat lazily testing the side effects of other
     * event handlers on the PackageGenerationParameters.
     * </p>
     *
     * @throws IOException
     */
    @Test
    public void testExplodedPackageGeneration() throws IOException, InterruptedException, ParametersBuildException {

        // simulates the output directory selected by a user when serializing the package.
        outputDirectory = tmpfolder.newFolder();

        // Mock the PackageGenerationParametersBuilder to return 'pkgParams', so we can test the contents of the
        // parameters after we invoke the test methods on the presenter.
        PackageGenerationParametersBuilder mockParamsBuilder = mock(PackageGenerationParametersBuilder.class);
        final PackageGenerationParameters pkgParams = new PackageGenerationParameters();
        pkgParams.addParam(GeneralParameterNames.PACKAGE_NAME, controller.getPackageState().getPackageName());
        when(mockParamsBuilder.buildParameters(any(InputStream.class))).thenAnswer(invocationOnMock -> pkgParams);

        // Mock the PackageGenerationService to do nothing.
        PackageGenerationService mockGenerationSvc = mock(PackageGenerationService.class);

        // Update the Controller mock to return an non-null File
        when(controller.showOpenDirectoryDialog(any())).thenReturn(outputDirectory);

        presenter.setTestBackgroundService();
        presenter.setPackageGenerationService(mockGenerationSvc);
        presenter.setPackageGenerationParametersBuilder(mockParamsBuilder);

        presenter.display();

        // Compression: None
        view.getCompressionToggleGroup().selectToggle(presenter.getNoCompressionToggle());

        // Archive: Exploded
        view.getArchiveToggleGroup().selectToggle(presenter.getExplodedArchiveToggle());

        // Serialization: XML
        view.getSerializationToggleGroup().selectToggle(presenter.getSerializationToggle(XML));

        // Select the output directory ('outputDirectory' above)
        view.getSelectOutputDirectoryButton().fire();
        view.getContinueButton().fire();

        assertFalse(view.getErrorTextArea().isVisible());

        verify(controller).showOpenDirectoryDialog(any(DirectoryChooser.class));
        verify(mockGenerationSvc).generatePackage(any(PackageState.class), any(PackageGenerationParameters.class));
        verify(mockParamsBuilder).buildParameters(any(InputStream.class));

        assertNotNull(pkgParams.getParam(REM_SERIALIZATION_FORMAT, 0));
        assertEquals(XML.name(), pkgParams.getParam(REM_SERIALIZATION_FORMAT, 0));

        assertNotNull(pkgParams.getParam(ARCHIVING_FORMAT, 0));
        assertEquals("exploded", pkgParams.getParam(ARCHIVING_FORMAT, 0));

        // Setting the compression format to "none" results in the param being removed altogether.
        assertNull(pkgParams.getParam(COMPRESSION_FORMAT, 0));

        // This was the specific bug: the PACKAGE_LOCATION was getting written twice, so it had two values.  The
        // first value was incorrect, but the second value was correct.  the Assembler would look at the first value
        // only, and output the package to the incorrect location.  So we make sure that there is only one package
        // location specified, and that it's the expected location.
        assertNotNull(pkgParams.getParam(PACKAGE_LOCATION, 0));
        assertEquals(1, pkgParams.getParam(PACKAGE_LOCATION).size());
        assertEquals(outputDirectory.getAbsolutePath(), pkgParams.getParam(PACKAGE_LOCATION, 0));
    }

    /**
     * Ensures that the Toggle representing 'no compression' is available.
     *
     * @throws Exception
     */
    @Test
    public void testGetNoCompressionToggle() throws Exception {
        Toggle noCompressionToggle = presenter.getNoCompressionToggle();
        assertNotNull(noCompressionToggle);
        assertEquals("", noCompressionToggle.getUserData().toString());
    }

    /**
     * Ensures that the Toggle representing an 'exploded' archive is available.
     *
     * @throws Exception
     */
    @Test
    public void testGetExplodedArchiveToggle() throws Exception {
        Toggle t = presenter.getExplodedArchiveToggle();
        assertNotNull(t);
        assertEquals("exploded", t.getUserData());
    }

    /**
     * Ensures that every SERIALIZATION_FORMAT is available as a Toggle.
     *
     * @throws Exception
     */
    @Test
    public void testGetSerializationToggle() throws Exception {
        int found = 0;
        for (GeneralParameterNames.SERIALIZATION_FORMAT format : values()) {
            Toggle t = presenter.getSerializationToggle(format);
            assertNotNull(t);
            assertEquals(format.name(), t.getUserData().toString());
            found++;
        }

        assertEquals(values().length, found);
    }

    /**
     * Ensures that the default serialization format is Turtle.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultSerializationToggle() throws Exception {
        // Mock the PackageGenerationService to do nothing.
        PackageGenerationService mockGenerationSvc = mock(PackageGenerationService.class);

        presenter.setTestBackgroundService();
        presenter.setPackageGenerationService(mockGenerationSvc);

        // We need the concrete builder so that it will read in the defaults from the classpath resource.
        PropertiesConfigurationParametersBuilder paramsBuilder = new PropertiesConfigurationParametersBuilder();
        presenter.setPackageGenerationParametersBuilder(paramsBuilder);

        presenter.display();

        // Ensure that the generation params include the correct value.
        assertNotNull(presenter.getGenerationParams().getParam(GeneralParameterNames.REM_SERIALIZATION_FORMAT, 0));
        assertEquals(GeneralParameterNames.SERIALIZATION_FORMAT.TURTLE.name(),
                presenter.getGenerationParams().getParam(GeneralParameterNames.REM_SERIALIZATION_FORMAT, 0));
        assertEquals(1,
                presenter.getGenerationParams().getParam(GeneralParameterNames.REM_SERIALIZATION_FORMAT).size());

        // Ensure that the proper default value has been set from the generation parameters.
        assertEquals(presenter.getSerializationToggle(TURTLE).getUserData(),
                view.getSerializationToggleGroup().getSelectedToggle().getUserData());
    }
}
