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
import org.dataconservancy.packaging.tool.model.PackageDescription;
import sun.security.x509.AVA;

/**
 * Root container for application that manages changes between presenters.
 */
public class Controller {
    private BorderPane container;
    private Factory factory;
    private Page currentPage;
    private PackageDescription packageDescription;
    private File packageDescriptionFile;
    private File contentRoot;
    private File rootArtifactDir;
    private File outputDirectory;
    private String packageGenerationParams;
    private String packageFilenameIllegalCharacters;
    private String availableProjects;
    private String buildNumber;
    private String buildRevision;
    private String buildTimeStamp;
    private PackageToolPopup crossPageProgressIndicatorPopUp;
    private Stack<Page> previousPages;
    
    /* For handling file dialog mutex locks as a MacOS bug workaround DC-1624 */
    private final ConcurrentHashMap<Object, Semaphore> locks = new ConcurrentHashMap<>();
    
    public Controller() {
        this.container = new BorderPane();
        container.getStyleClass().add(CssConstants.ROOT_CLASS);
        previousPages = new Stack<>();
    }

    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }

    public void startApp() {
        packageGenerationParams = factory.getConfiguration().getPackageGenerationParameters();
        packageFilenameIllegalCharacters = factory.getConfiguration().getPackageFilenameIllegalCharacters();
        availableProjects = factory.getConfiguration().getAvailableProjects();
        showHome(true);
    }
    
    /**
     * Switch to home.
     * @param clear Set to true if the fields on the home page should be cleared, false if not.
     */
    public void showHome(boolean clear) {
        container.setTop((VBox)factory.getHeaderView());
        currentPage = Page.HOMEPAGE;
        packageDescription = null;
        packageDescriptionFile = null;
        contentRoot = null;
        rootArtifactDir = null;

        if (clear) {
            clearPresenters();
        }
        showPage();
    }

    /**
     * Method to clear stale information from the presenters.
     */
    private void clearPresenters() {
        factory.getHomepagePresenter().clear();
        factory.getCreateNewPackagePresenter().clear();
        factory.getContentDirectoryPresenter().clear();
        factory.getPackageDescriptionPresenter().clear();
        factory.getPackageGenerationPresenter().clear();
    }

    /**
     * Switch to homepage
     */
    private void showHomepage() {
        show(factory.getHomepagePresenter());
    }

    /**
     * Switch to creating package description.
     */
    public void showCreatePackageDescription() {
        show(factory.getCreateNewPackagePresenter());
    }

    /**
     * Switch to the screen for selecting a content directory.
     */
    public void showSelectContentDirectory() {
        show(factory.getContentDirectoryPresenter());
    }

    /**
     * @return container node of controller
     */
    public Parent asParent() {
        return container;
    }

    /**
     * Shows the presenter and optionally clears the information
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
    
    /** Pops up a save file dialog.
     * 
     * @param chooser  the FileChooser
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
        Page nextPage = currentPage;
        int currentPosition = currentPage.getPosition();
        int nextPosition = Integer.MAX_VALUE;
        for (Page pages : Page.values()) {
            if (pages.getPosition() > currentPosition && pages.getPosition() < nextPosition && pages.isValidPage(this)) {
                nextPosition = pages.getPosition();
            }
        }
        
        if (nextPosition < Integer.MAX_VALUE) {
            Page pageForPosition = Page.getPageByPosition(nextPosition);
            if (pageForPosition != null) {
                nextPage = pageForPosition;
            }
        }

        previousPages.push(currentPage);
        currentPage = nextPage;
        showPage();
    }
    
    //Returns the application to the previous page, or redisplays the current page if it's the first page. 
    public void goToPreviousPage() {
        if (previousPages != null && !previousPages.isEmpty()) {
            currentPage = previousPages.pop();
            showPage();
        }
    }

    public void goToPage(Page page) {
        currentPage = page;
        showPage();
    }
    
    /**
     * Shows the current page, a tells the presenter if it should clear it's information. 
     */
    private void showPage() {
        factory.getHeaderView().highlightNextPage(currentPage.getPosition());
        switch (currentPage) {
            case HOMEPAGE:
                showHomepage();
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
            case SELECT_CONTENT_DIRECTORY:
                showSelectContentDirectory();
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
    
    public void setPackageDescriptionFile(File packageDescriptionFile){
        this.packageDescriptionFile = packageDescriptionFile;
    }

    public File getPackageDescriptionFile(){
        return packageDescriptionFile;
    }

    public File getContentRoot() {
        return contentRoot;
    }

    public void setContentRoot(File contentRoot) {
        this.contentRoot = contentRoot;
    }

    public File getRootArtifactDir() { return rootArtifactDir; }

    public void setRootArtifactDir(File rootArtifactDir) { this.rootArtifactDir = rootArtifactDir; }

    public File getOutputDirectory() { return outputDirectory; }

    public void setOutputDirectory(File outputDirectory) { this.outputDirectory = outputDirectory; }
    
    public String getPackageGenerationParamsFilePath() {
        return packageGenerationParams;
    }

    public String getPackageFilenameIllegalCharacters() { return packageFilenameIllegalCharacters; }

    public void setPackageFilenameIllegalCharacters(String illegalCharacters) { this.packageFilenameIllegalCharacters = illegalCharacters;}

    public String getAvailableProjects() { return availableProjects; }

    public void setAvailableProjects(String availableProjects) { this.availableProjects = availableProjects; }

    public void setBuildNumber(String buildNumberString) {
        this.buildNumber = buildNumberString;
    }
    
    public String getBuildNumber() {
        return buildNumber;
    }
    
    public void setBuildRevision(String buildRevision) {
        this.buildRevision = buildRevision;
    }
    
    public String getBuildRevision() {
        return buildRevision;
    }
    
    public void setBuildTimeStamp(String timeStamp) {
        this.buildTimeStamp = timeStamp;
    }
    
    public String getBuildTimeStamp() {
        return buildTimeStamp;
    }

    public PackageToolPopup getCrossPageProgressIndicatorPopUp() {
        return crossPageProgressIndicatorPopUp;
    }

    public void setCrossPageProgressIndicatorPopUp(PackageToolPopup crossPageProgressIndicatorPopUp) {
        this.crossPageProgressIndicatorPopUp = crossPageProgressIndicatorPopUp;
    }
}
