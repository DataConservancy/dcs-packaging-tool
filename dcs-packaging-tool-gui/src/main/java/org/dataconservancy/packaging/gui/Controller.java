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

package org.dataconservancy.packaging.gui;

import java.io.File;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.dataconservancy.packaging.gui.presenter.PackageDescriptionPresenter;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageState;

/**
 * Root container for application that manages changes between presenters.
 */
public class Controller {
    private BorderPane container;
    private Factory factory;

    /**
     * Package-scope metadata
     */
    //TODO: these fields can be removed once PackageState contains IPM nodes and other object currently in the domain profile branch.
    private PackageDescription packageDescription;
    private File packageDescriptionFile;
    private File contentRoot;
    private File rootArtifactDir;
    //END TODO
    private PackageState packageState;

    /**
     * Application-scope metadata
     */
    private String defaultPackageGenerationParametersFilePath;
    private String packageFilenameIllegalCharacters;
    private String availableProjects;
    private ApplicationVersion toolVersion;

    /**
     * Flow-control fields
     */
    private PackageToolPopup crossPageProgressIndicatorPopUp;
    private Page currentPage;
    private Stack<Page> previousPages;

    private Stack<Page> createNewPackagePagesStack;
    private Stack<Page> openExistingPackagePagesStack;
    private boolean createNewPackage;

    /* For handling file dialog mutex locks as a MacOS bug workaround DC-1624 */
    private final ConcurrentHashMap<Object, Semaphore> locks = new ConcurrentHashMap<>();


    public Controller() {
        this.container = new BorderPane();
        container.getStyleClass().add(CssConstants.ROOT_CLASS);
        previousPages = new Stack<>();
        createNewPackagePagesStack = new Stack<>();
        openExistingPackagePagesStack = new Stack<>();
        toolVersion = new ApplicationVersion();
        initiatePagesStacks();
    }

    public Factory getFactory() {
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    public void startApp() {
        defaultPackageGenerationParametersFilePath = factory.getConfiguration().getPackageGenerationParameters();
        packageFilenameIllegalCharacters = factory.getConfiguration().getPackageFilenameIllegalCharacters();
        availableProjects = factory.getConfiguration().getAvailableProjects();
        showHome(true);
    }

    /**
     * Switch to home.
     *
     * @param clear Set to true if the fields on the home page should be cleared, false if not.
     */
    public void showHome(boolean clear) {
        container.setTop((VBox) factory.getHeaderView());
        currentPage = Page.HOMEPAGE;
        packageDescription = null;
        packageDescriptionFile = null;
        contentRoot = null;
        rootArtifactDir = null;
        packageState = new PackageState(this.toolVersion);

        if (clear) {
            clearPresenters();
        }

        factory.getHeaderView().highlightNextPage(currentPage);
        show(factory.getHomepagePresenter());
    }

    /**
     * Initiates the page stacks so the application flows appropriately
     */
    private void initiatePagesStacks() {
        createNewPackagePagesStack.clear();
        createNewPackagePagesStack.add(Page.GENERATE_PACKAGE);
        createNewPackagePagesStack.add(Page.DEFINE_RELATIONSHIPS);
        createNewPackagePagesStack.add(Page.CREATE_NEW_PACKAGE);
        createNewPackagePagesStack.add(Page.PACKAGE_METADATA);

        openExistingPackagePagesStack.clear();
        openExistingPackagePagesStack.add(Page.GENERATE_PACKAGE);
        openExistingPackagePagesStack.add(Page.DEFINE_RELATIONSHIPS);
        openExistingPackagePagesStack.add(Page.EXISTING_PACKAGE_METADATA);
        openExistingPackagePagesStack.add(Page.OPEN_EXISTING_PACKAGE);
    }

    /**
     * Method to clear stale information from the presenters.
     */
    private void clearPresenters() {
        factory.getHomepagePresenter().clear();
        factory.getPackageMetadataPresenter().clear();
        factory.getCreateNewPackagePresenter().clear();
        factory.getOpenExistingPackagePresenter().clear();
        factory.getPackageDescriptionPresenter().clear();
        factory.getPackageGenerationPresenter().clear();
    }

    /**
     * Switch to homepage
     */
    private void showHomepage() {
        previousPages.clear();
        showHome(true);
    }

    /**
     * Switch to package metadata
     */
    private void showPackageMetadata(boolean existing) {
        show(factory.getPackageMetadataPresenter());
        if (existing) {
            factory.getPackageMetadataPresenter().setExistingValues();
        }
    }

    /**
     * Switch to creating package description.
     */
    public void showCreatePackageDescription() {
        show(factory.getCreateNewPackagePresenter());
    }

    /**
     * Switch to the screen for selecting a package directory.
     */
    public void showOpenExistingPackage() {
        show(factory.getOpenExistingPackagePresenter());
    }

    /**
     * @return container node of controller
     */
    public Parent asParent() {
        return container;
    }

    /**
     * Shows the presenter and optionally clears the information
     *
     * @param presenter The presenter to show
     */
    private void show(Presenter presenter) {
        container.setCenter(presenter.display());
    }

    public void showGeneratePackage() {
        show(factory.getPackageGenerationPresenter());
    }

    public PackageDescriptionPresenter showPackageDescriptionViewer() {
        PackageDescriptionPresenter presenter = factory.getPackageDescriptionPresenter();
        show(presenter);
        return presenter;
    }

    /**
     * Pops up a dialog that waits for the user to choose a file.
     *
     * @param chooser the FileChooser
     * @return file chosen or null on cancel
     */
    public File showOpenFileDialog(FileChooser chooser) {
        Semaphore lock = getLock(chooser);
        
        /* We manually assure only one dialog box is open at a time, due to a MacOS bug DC-1624 */
        if (lock.tryAcquire()) {
            try {
                return chooser.showOpenDialog(factory.getStage());
            } finally {
                locks.remove(lock);
                lock.release();
            }
        } else {
            System.out.println("");
            return null;
        }
    }

    /**
     * Pops up a save file dialog.
     *
     * @param chooser the FileChooser
     * @return File to save or null on cancel.
     */
    public File showSaveFileDialog(FileChooser chooser) {
        Semaphore lock = getLock(chooser);
        
        /* We manually assure only one dialog box is open at a time, due to a MacOS bug DC-1624 */
        if (lock.tryAcquire()) {
            try {
                return chooser.showSaveDialog(factory.getStage());
            } finally {
                locks.remove(lock);
                lock.release();
            }
        } else {
            return null;
        }
    }

    /**
     * Pops up a dialog that waits for the user to choose a directory
     *
     * @param chooser the DirectoryChooser
     * @return directory chosen or null on cancel
     */
    public File showOpenDirectoryDialog(DirectoryChooser chooser) {
        Semaphore lock = getLock(chooser);

        /* We manually assure only one dialog box is open at a time, due to a MacOS bug DC-1624 */
        if (lock.tryAcquire()) {
            try {
                return chooser.showDialog(factory.getStage());
            } finally {
                locks.remove(lock);
                lock.release();
            }
        } else {
            return null;
        }
    }

    private Semaphore getLock(Object exclusive) {
        locks.putIfAbsent(exclusive, new Semaphore(1));
        return locks.get(exclusive);
    }

    //Advances the application to the next page. Or redisplays the current page if it's the last page.
    public void goToNextPage() {
        previousPages.push(currentPage);
        if (createNewPackage) {
            currentPage = createNewPackagePagesStack.pop();
        }
        else {
            currentPage = openExistingPackagePagesStack.pop();
        }
        showPage();
    }

    //Returns the application to the previous page
    public void goToPreviousPage() {
        if (previousPages != null && !previousPages.isEmpty()) {
            if (createNewPackage) {
                createNewPackagePagesStack.push(currentPage);
            }
            else {
                openExistingPackagePagesStack.push(currentPage);
            }
            currentPage = previousPages.pop();
            showPage();
        }
    }

    /**
     * Shows the current page, a tells the presenter if it should clear it's information.
     */
    private void showPage() {
        factory.getHeaderView().highlightNextPage(currentPage);
        switch (currentPage) {
            case HOMEPAGE:
                showHomepage();
                break;
            case PACKAGE_METADATA:
                showPackageMetadata(false);
                break;
            case EXISTING_PACKAGE_METADATA:
                showPackageMetadata(true);
                break;
            case CREATE_NEW_PACKAGE:
                showCreatePackageDescription();
                break;
            case DEFINE_RELATIONSHIPS:
                showPackageDescriptionViewer();
                break;
            case GENERATE_PACKAGE:
                showGeneratePackage();
                break;
            case OPEN_EXISTING_PACKAGE:
                showOpenExistingPackage();
                break;
            default:
                //There is no next page do nothing
                break;
        }
    }

    public void setPackageDescription(PackageDescription description) {
        this.packageDescription = description;
    }

    public PackageDescription getPackageDescription() {
        return packageDescription;
    }

    public void setPackageDescriptionFile(File packageDescriptionFile) {
        this.packageDescriptionFile = packageDescriptionFile;
    }

    public File getPackageDescriptionFile() {
        return packageDescriptionFile;
    }

    public File getContentRoot() {
        return contentRoot;
    }

    public void setContentRoot(File contentRoot) {
        this.contentRoot = contentRoot;
    }

    public File getRootArtifactDir() {
        return rootArtifactDir;
    }

    public void setRootArtifactDir(File rootArtifactDir) {
        this.rootArtifactDir = rootArtifactDir;
    }

    public String getPackageFilenameIllegalCharacters() {
        return packageFilenameIllegalCharacters;
    }

    public void setPackageFilenameIllegalCharacters(String illegalCharacters) {
        this.packageFilenameIllegalCharacters = illegalCharacters;
    }

    public String getAvailableProjects() {
        return availableProjects;
    }

    public void setAvailableProjects(String availableProjects) {
        this.availableProjects = availableProjects;
    }

    public PackageState getPackageState() {
        return packageState;
    }

    public void setPackageState(PackageState packageState) {
        this.packageState = packageState;
    }

    public PackageToolPopup getCrossPageProgressIndicatorPopUp() {
        return crossPageProgressIndicatorPopUp;
    }

    public void setCrossPageProgressIndicatorPopUp(PackageToolPopup crossPageProgressIndicatorPopUp) {
        this.crossPageProgressIndicatorPopUp = crossPageProgressIndicatorPopUp;
    }

    public void setToolBuildNumber(String buildNumber) {
        toolVersion.setBuildNumber(buildNumber);
    }

    public void setToolBuildRevision(String buildRevision) {
        toolVersion.setBuildRevision(buildRevision);
    }

    public void setToolBuildTimestamp(String buildTimestamp) {
        toolVersion.setBuildTimeStamp(buildTimestamp);
    }

    public String getDefaultPackageGenerationParametersFilePath() {
        return defaultPackageGenerationParametersFilePath;
    }

    public void setCreateNewPackage(boolean createNewPackage) {
        this.createNewPackage = createNewPackage;
    }

    // Only used for tests so each test's continue button can be tested.
    public Stack<Page> getCreateNewPackagePagesStack() {
        return createNewPackagePagesStack;
    }
}
