package org.dataconservancy.packaging.gui.presenter.impl;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Factory;
import org.dataconservancy.packaging.gui.presenter.impl.OpenExistingPackagePresenterImpl.FILE_TYPE;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.impl.HeaderViewImpl;
import org.dataconservancy.packaging.gui.view.impl.OpenExistingPackageViewImpl;
import org.dataconservancy.packaging.tool.api.OpenPackageService;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test the presenter for opening packages.
 */
public class OpenExistingPackagePresenterImplTest extends BaseGuiTest {
    private static final String PACKAGE_STATE_EXT=".dcp";
    
    private OpenExistingPackagePresenterImpl presenter;
    private OpenExistingPackageViewImpl view;
    private OpenPackageService open_package_service;
    private Controller controller;
    private OpenedPackage pkg = new OpenedPackage();
    private File opened_file;
    private File opened_dir;
    
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        open_package_service = mock(OpenPackageService.class);

        factory = new Factory() {
            @Override
            public OpenPackageService getOpenPackageService() {
                return open_package_service;
            }
        };

        factory.setConfiguration(new Configuration());

        opened_file = tmpfolder.newFile();
        opened_dir = tmpfolder.newFolder();
        
        controller = mock(Controller.class);
        when(controller.showOpenFileDialog(any())).thenReturn(opened_file);
        when(controller.showOpenDirectoryDialog(any())).thenReturn(opened_dir);
        when(controller.getFactory()).thenReturn(factory);
        when(controller.getPackageStateFileExtension()).thenReturn("*" + PACKAGE_STATE_EXT);
        view = new OpenExistingPackageViewImpl();

        HeaderView headerView = new HeaderViewImpl();
        factory.setHeaderView(headerView);
        view.setHeaderView(headerView);

        presenter = new OpenExistingPackagePresenterImpl(view);
        presenter.setTestBackgroundService();
        presenter.setController(controller);
        presenter.setInternalProperties(internalProperties);

        Node tree = new Node(URI.create("test:moo"));
        PackageState state = new PackageState();

        pkg.setBaseDirectory(tmpfolder.newFolder());
        pkg.setPackageState(state);
        pkg.setPackageTree(tree);

        when(open_package_service.openPackageState(any())).thenReturn(pkg);
        when(open_package_service.openExplodedPackage(any())).thenReturn(pkg);
        when(open_package_service.openPackage(any(), any())).thenReturn(pkg);
    }

    /**
     * Ensure the presenter displays a node.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testDisplay() throws InterruptedException {
        assertNotNull(presenter.display());
    }

    /**
     * Test that selected file and type are set correctly.
     * 
     * @throws IOException
     */
    @Test
    public void testOpenPackageState() throws IOException {
        opened_file = tmpfolder.newFile("test" + PACKAGE_STATE_EXT);
        when(controller.showOpenFileDialog(any())).thenReturn(opened_file);
        
        view.getChoosePackageFileButton().fire();

        assertEquals(opened_file, presenter.getSelectedFile());
        assertEquals(FILE_TYPE.STATE_FILE, presenter.getSelectedFileType());
        assertEquals(false, view.getContinueButton().isDisabled());
    }

    /**
     * Test that selected file and type are set correctly.
     * 
     * @throws IOException
     */
    @Test
    public void testOpenPackage() throws IOException {
        opened_file = tmpfolder.newFile("test.tar.gz");
        when(controller.showOpenFileDialog(any())).thenReturn(opened_file);
        
        view.getChoosePackageFileButton().fire();

        assertEquals(opened_file, presenter.getSelectedFile());
        assertEquals(FILE_TYPE.PACKAGE, presenter.getSelectedFileType());
        assertEquals(false, view.getContinueButton().isDisabled());
    }

    /**
     * Test that selected file and type are set correctly.
     * 
     * @throws IOException
     */
    @Test
    public void testOpenExplodedPackage() throws IOException {
        view.getChooseExplodedPackageDirectoryButton().fire();

        assertEquals(opened_dir, presenter.getSelectedFile());
        assertEquals(FILE_TYPE.EXPLODED_PACKAGE, presenter.getSelectedFileType());
        assertEquals(false, view.getContinueButton().isDisabled());
    }

    /**
     * Test that package state and tree are set when user opens a package state
     * and continues. Verify that state is not saved.
     * 
     * @throws Exception
     */
    @Test
    public void testContinueOnPackageState() throws Exception {
        pkg.setBaseDirectory(null);
        opened_file = tmpfolder.newFile("test" + PACKAGE_STATE_EXT);
        
        view.getChoosePackageFileButton().fire();
        view.getContinueButton().fire();

        verify(controller).setPackageState(any());
        verify(controller).setPackageTree(any());
        verify(controller).goToNextPage();
        verify(controller, times(0)).savePackageStateFile();
    }
    
    /**
     * Test that package state and tree are set when user opens a package state
     * and continues. Verify that state is saved.
     * 
     * @throws Exception
     */
    @Test
    public void testContinueOnPackage() throws Exception {
        opened_file = tmpfolder.newFile("test.tar.gz");
        
        view.getChoosePackageFileButton().fire();
        view.getContinueButton().fire();

        verify(controller).setPackageState(any());
        verify(controller).setPackageTree(any());
        verify(controller).goToNextPage();
        verify(controller).savePackageStateFile();
    }
    
    /**
     * Verify that state is not saved.
     * 
     * @throws Exception
     */
    @Test
    public void testGoBack() throws Exception {
        view.getCancelLink().fire();

        verify(controller).goToPreviousPage();
        verify(controller, times(0)).savePackageStateFile();
    }
}
