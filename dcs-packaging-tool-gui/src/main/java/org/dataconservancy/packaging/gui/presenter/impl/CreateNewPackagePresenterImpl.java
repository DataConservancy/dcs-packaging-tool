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

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.CreateNewPackagePresenter;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.view.CreateNewPackageView;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.PackageDescriptionCreatorException;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The implementation for the presenter that will handle the creation of a new package either from a content directory,
 * or an existing package description file.
 */
public class CreateNewPackagePresenterImpl extends BasePresenterImpl
        implements CreateNewPackagePresenter {

    private CreateNewPackageView view;

    private File content_dir;
    private File root_artifact_dir; //has content_dir as parent

    private DirectoryChooser directoryChooser;
    private FileChooser fileChooser;

    private IPMService ipmService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public CreateNewPackagePresenterImpl(CreateNewPackageView view) {
        super(view);
        this.view = view;
        this.content_dir = null;
        directoryChooser = new DirectoryChooser();
        fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package Description (*.json)", "*.json"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));

        view.setPresenter(this);
        bind();
    }

    private void bind() {
       
        //Handles the continue button in the footer being pressed. Validates that all required fields are present, sets the state on the controller,
        //and instructs the controller to move to the next page.
        view.getContinueButton().setOnAction(arg0 -> {
            try {
                if (root_artifact_dir != null && root_artifact_dir.exists() &&
                    root_artifact_dir.canRead()) {

                    final PackageIpmBuilderService ipmBuilderService = new PackageIpmBuilderService(root_artifact_dir);

                    view.showProgressIndicatorPopUp();

                    ((ProgressDialogPopup)view.getProgressIndicatorPopUp()).setCancelEventHandler(event -> ipmBuilderService.cancel());

                    controller.setCrossPageProgressIndicatorPopUp(view.getProgressIndicatorPopUp());
                    controller.setContentRoot(content_dir);
                    controller.setRootArtifactDir(root_artifact_dir);

                    ipmBuilderService.setOnCancelled(event -> {
                        ipmBuilderService.reset();
                        controller.getCrossPageProgressIndicatorPopUp().hide();
                    });

                    ipmBuilderService.setOnFailed(workerStateEvent -> {
                        displayExceptionMessage(workerStateEvent.getSource().getException());
                        view.getErrorMessage().setVisible(true);
                        ipmBuilderService.reset();
                        controller.getCrossPageProgressIndicatorPopUp().hide();
                    });

                    ipmBuilderService.setOnSucceeded(workerStateEvent -> {
                        Node rootNode = (Node) workerStateEvent.getSource().getValue();
                        controller.setPackageTree(rootNode);
                        ipmBuilderService.reset();
                        controller.goToNextPage();
                    });

                    ipmBuilderService.start();

                } else if (root_artifact_dir != null &&
                        (!root_artifact_dir.exists() ||
                             !root_artifact_dir.canRead())) {
                    view.getErrorMessage().setText(TextFactory.getText(ErrorKey.INACCESSIBLE_CONTENT_DIR));
                    view.getErrorMessage().setVisible(true);
                } else if (controller.getPackageDescription() != null) {
                    controller.goToNextPage();
                } else {
                    view.getErrorMessage().setText(TextFactory.getText(ErrorKey.BASE_DIRECTORY_NOT_SELECTED));
                    view.getErrorMessage().setVisible(true);
                }
            } catch (Exception e) {
                view.getErrorMessage().setText(TextFactory.format(Messages.MessageKey.ERROR_CREATING_NEW_PACKAGE, e.getMessage()));
                view.getErrorMessage().setVisible(true);
                log.error(e.getMessage());
            }
        });

        //Handles the user pressing the button to choose a base directory to create a package from.
        view.getChooseContentDirectoryButton()
                .setOnAction(event -> {
                    if (directoryChooser.getInitialDirectory() != null &&
                        !directoryChooser.getInitialDirectory().exists()) {
                        directoryChooser.setInitialDirectory(null);
                    }

                    File dir = controller.showOpenDirectoryDialog(directoryChooser);

                    if (dir != null) {
                        root_artifact_dir = dir;
                        content_dir = root_artifact_dir.getParentFile();
                        view.getChooseContentDirectoryTextField().setText(root_artifact_dir.getPath());
                        //view.getSelectedPackageDescriptionTextField().setText("");
                        //If the error message happens to be visible erase it.
                        view.getErrorMessage().setVisible(false);
                        directoryChooser.setInitialDirectory(dir);
                    }
                });

    }

    @Override
    public void clear() {
        view.getChooseContentDirectoryTextField().setText("");
        view.getErrorMessage().setText("");

        content_dir = null;
        root_artifact_dir = null;
    }

    public javafx.scene.Node display() {
        //Setup help content and then rebind the base class to this view.
        view.setupHelp();
        setView(view);
        super.bindBaseElements();
        
        return view.asNode();
    }

    protected void displayExceptionMessage(Throwable throwable) {
        String errorMessage = throwable.getMessage();
        if (throwable instanceof PackageDescriptionCreatorException
                && ((PackageDescriptionCreatorException) throwable).hasDetail()) {
            errorMessage = errorMessage + "\n" + ((PackageDescriptionCreatorException) throwable).getDetail();
        }
        view.getErrorMessage().setText(errorMessage);
        view.getErrorMessage().setVisible(true);
    }

    @Override
    public void setIpmService(IPMService ipmService) {
        this.ipmService = ipmService;
    }

    /**
     * A {@link javafx.concurrent.Service} which executes the {@link javafx.concurrent.Task} of obtaining a
     * {@link org.dataconservancy.packaging.tool.model.PackageDescription} given a package ontology identifier and
     * a content directory
     */
    private class PackageIpmBuilderService extends Service<Node> {

        private File root_artifact_dir;

        public PackageIpmBuilderService(File root_artifact_dir) {
            this.root_artifact_dir = root_artifact_dir;
        }

        @Override
        protected Task<Node> createTask() {
            return new Task<Node>() {
                @Override
                protected Node call() throws Exception {
                    Node root = ipmService.createTreeFromFileSystem(root_artifact_dir.toPath());
                    controller.initializeDomainStoreAndServices(null);
                    controller.getDomainProfileService().assignNodeTypes(controller.getPrimaryDomainProfile(), root);

                    return root;
                }
            };
        }
    }
}
