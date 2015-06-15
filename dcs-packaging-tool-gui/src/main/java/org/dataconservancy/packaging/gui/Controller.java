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
    private File outputDirectory;
    private String packageGenerationParams;
    private String packageFilenameIllegalCharacters;
    private String buildNumber;
    private String buildRevision;
    private String buildTimeStamp;
    private PackageToolPopup crossPageProgressIndicatorPopUp;
    
    /* For handling file dialog mutex locks as a MacOS bug workaround DC-1624 */
    private final ConcurrentHashMap<Object, Semaphore> locks = new ConcurrentHashMap<>();
    
    public Controller() {
        this.container = new BorderPane();
        container.getStyleClass().add(CssConstants.ROOT_CLASS);
    }

    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }

    public void startApp() {
        packageGenerationParams = factory.getConfiguration().getPackageGenerationParamsFile();
        packageFilenameIllegalCharacters = factory.getConfiguration().getPackageFilenameIllegalCharacters();
        showHome(true);
    }
    
    /**
     * Switch to home.
     * @param clear Set to true if the fields on the home page should be cleared, false if not.
     */
    public void showHome(boolean clear) {
        container.setTop((VBox)factory.getHeaderView());
        currentPage = Page.CREATE_NEW_PACKAGE;
        packageDescription = null;
        packageDescriptionFile = null;

        showPage(clear);
    }

    /**
     * Switch to creating package description.
     * @param clear A flag to clear out any previous presenter information
     */
    public void showCreatePackageDescription(boolean clear) {
        show(factory.getCreateNewPackagePresenter(), clear);
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
     * @param clear A flag that tells whether the presenter state should be cleared
     */
    private void show(Presenter presenter, boolean clear) {
        container.setCenter(presenter.display(clear));
    }
    
    public void showGeneratePackage(boolean clear) {
        show(factory.getPackageGenerationPresenter(), clear);
    }

    public PackageDescriptionPresenter showPackageDescriptionViewer(boolean clear) {
        PackageDescriptionPresenter presenter = factory.getPackageDescriptionPresenter();
        show(presenter, clear);
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
    
    /** 
     * A simple enumeration that is used to control flow in the application. There is an entry for each page in the application.
     * Each page contains it's order in the application as well as a title. 
     */
    public enum Page {
        
        //Postions must be in numerical order relating to their position in the application
        CREATE_NEW_PACKAGE(1, Labels.LabelKey.CREATE_PACKAGE_PAGE),
        DEFINE_RELATIONSHIPS(2, Labels.LabelKey.DEFINE_RELATIONSHIPS_PAGE),
        GENERATE_PACKAGE(3, Labels.LabelKey.GENERATE_PACKAGE_PAGE);
        
        private int position;
        private Labels.LabelKey labelKey;
        
        private Page(int position, Labels.LabelKey label) {
            this.position = position;
            this.labelKey = label;
        }
        
        /**
         * Returns the position of the page in the application.
         * @return  the position of the page
         */
        public int getPosition() {
            return position;
        }
        
        /**
         * Returns the label key to get the title of the page.
         * @return  the label key to get the title of the page
         */
        public Labels.LabelKey getLabelKey() {
            return labelKey;
        }
    }
    
    //Advances the application to the next page. Or redisplays the current page if it's the last page.
    public void goToNextPage() {
        Page nextPage = currentPage;
        int currentPosition = currentPage.getPosition();
        int nextPosition = Integer.MAX_VALUE;
        for (Page pages : Page.values()) {
            if (pages.position > currentPosition && pages.position < nextPosition) {
                nextPosition = pages.position;
            }
        }
        
        if (nextPosition < Integer.MAX_VALUE) {
            nextPage = Page.values()[nextPosition-1];
        }
        
        currentPage = nextPage;
        showPage(false);
    }
    
    //Returns the application to the previous page, or redisplays the current page if it's the first page. 
    public void goToPreviousPage() {
        Page nextPage = currentPage;
        int currentPosition = currentPage.getPosition();
        int nextPosition = Integer.MIN_VALUE;
        for (Page pages : Page.values()) {
            if (pages.position < currentPosition && pages.position > nextPosition) {
                nextPosition = pages.position;
            }
        }
        
        if (nextPosition < Integer.MAX_VALUE) {
            nextPage = Page.values()[nextPosition-1];
        }
        
        currentPage = nextPage;
        showPage(false);
    }
    
    /**
     * Shows the current page, a tells the presenter if it should clear it's information. 
     * @param clear A boolean flag passed to the presenter letting it now if information should be cleared when displaying.
     */
    private void showPage(boolean clear) {
        factory.getHeaderView().highlightNextPage(currentPage.getPosition());
        switch (currentPage) {
            case CREATE_NEW_PACKAGE:
                showCreatePackageDescription(clear);
                break;
            case DEFINE_RELATIONSHIPS:
                showPackageDescriptionViewer(clear);
                break;
            case GENERATE_PACKAGE:
                showGeneratePackage(clear);
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

    public File getOutputDirectory() { return outputDirectory; }

    public void setOutputDirectory(File outputDirectory) { this.outputDirectory = outputDirectory; }
    
    public String getPackageGenerationParamsFilePath() {
        return packageGenerationParams;
    }

    public String getPackageFilenameIllegalCharacters() { return packageFilenameIllegalCharacters; }

    public void setPackageFilenameIllegalCharacters(String illegalCharacters) { this.packageFilenameIllegalCharacters = illegalCharacters;}
    
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
