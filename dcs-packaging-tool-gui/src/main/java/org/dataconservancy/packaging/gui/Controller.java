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

import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.dcs.util.Util;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStoreImpl;
import org.dataconservancy.packaging.tool.impl.DomainProfileServiceImpl;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.impl.support.FilenameValidator;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Root container for application that manages changes between presenters.
 */
public class Controller {
    private BorderPane container;
    private Factory factory;

    /**
     * Package-scope metadata
     */

    private PackageState packageState;
    private File packageStateFile;
    private FileChooser packageStateFileChooser;
    private PackageStateSerializer packageStateSerializer;
    private String packageStateFileExtension="*.dcp";
    private StringProperty defaultStateFileName;

    private DomainProfileStore domainProfileStore;
    private IpmRdfTransformService ipmRdfTransformService;
    private Node packageTree;
    private DomainProfileService domainProfileService;
    private URIGenerator uriGenerator;


    /**
     * Application-scope metadata
     */
    private String defaultPackageGenerationParametersFilePath;
    private ApplicationVersion toolVersion;

    /**
     * Flow-control fields
     */
    private PackageToolPopup crossPageProgressIndicatorPopUp;
    private Page currentPage;
    private Stack<Page> previousPages;
    private Stack<Page> pageStack;

    /* For handling file dialog mutex locks as a MacOS bug workaround DC-1624 */
    private final ConcurrentHashMap<Object, Semaphore> locks = new ConcurrentHashMap<>();

    /**
     * Handle for application host services - used in launching a website from the application using default browser
     * when a hyperlink is clicked
     */
    private HostServices applicationHostServices;


    public Controller() {
        this.container = new BorderPane();
        container.getStyleClass().add(CssConstants.ROOT_CLASS);
        previousPages = new Stack<>();
        pageStack = new Stack<>();
        toolVersion = new ApplicationVersion();
        defaultStateFileName = new SimpleStringProperty(packageStateFileExtension);

        ipmRdfTransformService = new IpmRdfTransformService();
        packageStateFileChooser = new FileChooser();
        packageStateFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                TextFactory.getText(Labels.LabelKey.PACKAGE_STATE_FILE_DESCRIPTION_LABEL), packageStateFileExtension));
    }

    public Factory getFactory() {
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    public void setDomainProfileStore(DomainProfileStore domainProfileStore) {
        this.domainProfileStore = domainProfileStore;
    }

    public DomainProfile getPrimaryDomainProfile() {
        DomainProfile primaryProfile = null;
        for (DomainProfile profile : domainProfileStore.getPrimaryDomainProfiles()) {
            if (profile.getIdentifier().equals(packageState.getDomainProfileIdList().get(0))) {
                primaryProfile = profile;
                break;
            }
        }

        return primaryProfile;
    }

    public void startApp() {
        defaultPackageGenerationParametersFilePath = Configuration.resolveConfigurationFile(Configuration.ConfigFile.PKG_GEN_PARAMS);
        showHome(true);
    }

    /**
     * Switch to home.
     *
     * @param clear Set to true if the fields on the home page should be cleared, false if not.
     */
    public void showHome(boolean clear) {
        initializePageStack();

        container.setTop((VBox) factory.getHeaderView());
        currentPage = Page.HOMEPAGE;

        packageState = new PackageState(this.toolVersion);
        initializeDomainStoreAndServices();

        packageStateFile = null;
        packageTree = null;

        if (clear) {
            clearPresenters();
        }

        factory.getHeaderView().highlightNextPage(currentPage);
        show(factory.getHomepagePresenter());
    }

    /**
     * Initializes the page stack with the shared screens.
     */
    private void initializePageStack() {
        pageStack.clear();
        previousPages.clear();
        pageStack.add(Page.GENERATE_PACKAGE);
        pageStack.add(Page.EDIT_PACKAGE_CONTENTS);
    }

    /**
     * Method to clear stale information from the presenters.
     */
    private void clearPresenters() {
        factory.getHomepagePresenter().clear();
        factory.getPackageMetadataPresenter().clear();
        factory.getCreateNewPackagePresenter().clear();
        factory.getOpenExistingPackagePresenter().clear();
        factory.getEditPackageContentsPresenter().clear();
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

    /**
     * Pops up a dialog that waits for the user to choose a file.
     *
     * @param chooser the FileChooser
     * @return file chosen or null on cancel
     */
    //TODO: We should check if these can be removed now in Java 8
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
        currentPage = pageStack.pop();

        showPage();
    }

    //Returns the application to the previous page
    public void goToPreviousPage() {
        if (previousPages != null && !previousPages.isEmpty()) {
            pageStack.push(currentPage);
            currentPage = previousPages.pop();
            showPage();
        }
    }

    /**
     * Shows the current page, a tells the presenter if it should clear its information.
     */
    private void showPage() {
        factory.getHeaderView().highlightNextPage(currentPage);
        switch (currentPage) {
            case HOMEPAGE:
                showHomepage();
                break;
            case PACKAGE_METADATA:
                show(factory.getPackageMetadataPresenter());
                break;
            case CREATE_NEW_PACKAGE:
                show(factory.getCreateNewPackagePresenter());
                break;
            case EDIT_PACKAGE_CONTENTS:
                show(factory.getEditPackageContentsPresenter());
                break;
            case GENERATE_PACKAGE:
                show(factory.getPackageGenerationPresenter());
                break;
            case OPEN_EXISTING_PACKAGE:
                show(factory.getOpenExistingPackagePresenter());
                break;
            default:
                //There is no next page do nothing
                break;
        }
    }

    public void savePackageStateFile() throws IOException, RDFTransformException {
        if (packageState != null) {
            //Convert the package node tree to rdf to set on the state.
            if (packageTree != null) {
                packageState.setPackageTree(ipmRdfTransformService.transformToRDF(packageTree));
            }
            if(packageStateFile == null){
                if (Util.isEmptyOrNull(packageStateFileChooser.getInitialFileName())) {
                    FilenameValidator validator = new FilenameValidator();
                    String defaultFileName = packageStateFileExtension;
                    if (!defaultStateFileName.getValueSafe().isEmpty() &&
                        validator.isValid(defaultStateFileName.getValue())) {
                        defaultFileName = defaultStateFileName.getValue();
                        if (!defaultStateFileName.getValue().endsWith(".dcp")) {
                            defaultFileName += ".dcp";
                        }
                    }
                    packageStateFileChooser.setInitialFileName(defaultFileName);
                }
                packageStateFile = showSaveFileDialog(packageStateFileChooser);
            }
            if (packageStateFile != null) {
                try (FileOutputStream fs = new FileOutputStream(packageStateFile)) {
                    packageStateSerializer.serialize(getPackageState(), fs);
                }

                setPackageStateFileChooserInitialChoice(packageStateFile);
            }
        }
    }

    /**
     * Set the file which the package state file chooser will be set to when next shown.
     *
     * @param file The file that will be shown in the file picker.
     */
    public void setPackageStateFileChooserInitialChoice(File file) {
        packageStateFileChooser.setInitialDirectory(file.getParentFile());
        packageStateFileChooser.setInitialFileName(file.getName());
    }

    public PackageState getPackageState() {
        return packageState;
    }

    public void setPackageState(PackageState packageState) {
        this.packageState = packageState;
        initializeDomainStoreAndServices();
        initializeEditPackagePageStack();
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
        if (createNewPackage) {
            initializeCreateNewPackagePageStack();
        } else {
            pageStack.add(Page.OPEN_EXISTING_PACKAGE);
        }
    }

    // Only used for tests so each test's continue button can be tested.
    public Stack<Page> getPageStack() {
        return pageStack;
    }

    public void setPackageStateSerializer(PackageStateSerializer packageStateSerializer){
        this.packageStateSerializer = packageStateSerializer;
    }

    public PackageStateSerializer getPackageStateSerializer() {
        return packageStateSerializer;
    }

    public Node getPackageTree() {
        return packageTree;
    }

    public void setPackageTree(Node packageTree) {
        this.packageTree = packageTree;
    }

    public StringProperty getDefaultStateFileName() {
        return defaultStateFileName;
    }

    /**
     * Initializes the DomainProfileObjectStore and the DomainProfileService.
     * Note: DomainProfileService is not set until this method is called.
     */
    private void initializeDomainStoreAndServices() {
        DomainProfileObjectStore store;

        if (packageState.getDomainObjectRDF() == null) {
            Model objectModel = ModelFactory.createDefaultModel();
            store = new DomainProfileObjectStoreImpl(objectModel, uriGenerator);
            packageState.setDomainObjectRDF(objectModel);
        } else {
            store = new DomainProfileObjectStoreImpl(packageState.getDomainObjectRDF(), uriGenerator);
        }

        domainProfileService = new DomainProfileServiceImpl(store, uriGenerator);
    }

    /*
     * If we're creating a new package we add the metadata screen and the new package content screen to the stack.
     */
    private void initializeCreateNewPackagePageStack() {
        pageStack.add(Page.CREATE_NEW_PACKAGE);
        pageStack.add(Page.PACKAGE_METADATA);
    }

    /*
     * If we're opening a package or state we add the metadata screen. If the tree hasn't been created in the state we add that screen as well.
     */
    private void initializeEditPackagePageStack() {
        //First we reset any previously loaded edit pages.
        //This can occur if the user selects a state file and then returns to select another one
        if (!pageStack.empty()) {
            if (pageStack.peek() == Page.PACKAGE_METADATA) {
                pageStack.pop();
            }

            if (pageStack.peek() == Page.CREATE_NEW_PACKAGE) {
                pageStack.pop();
            }
        }

        //Then add the new pages based on how complete the state file is.
        if (packageState.getPackageTree() == null) {
            pageStack.add(Page.CREATE_NEW_PACKAGE);
        }
        pageStack.add(Page.PACKAGE_METADATA);
    }

    public String getPackageStateFileExtension() {
        return packageStateFileExtension;
    }

    public DomainProfileService getDomainProfileService() {
        return domainProfileService;
    }

    public void setUriGenerator(URIGenerator uriGenerator) {
        this.uriGenerator = uriGenerator;
    }

    public HostServices getApplicationHostServices() {
        return applicationHostServices;
    }

    public void setApplicationHostServices(HostServices applicationHostServices) {
        this.applicationHostServices = applicationHostServices;
    }

}
