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
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.CreateNewPackagePresenter;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.view.CreateNewPackageView;
import org.dataconservancy.packaging.tool.api.IPMService;
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

    private DirectoryChooser directoryChooser;
    private File selectedDir;
    private IPMService ipmService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public CreateNewPackagePresenterImpl(CreateNewPackageView view) {
        super(view);
        this.view = view;
        directoryChooser = new DirectoryChooser();

        view.setPresenter(this);
        bind();
    }

    private void bind() {
        //Handles the user pressing the button to choose a base directory to create a package from.
        view.getChooseContentDirectoryButton()
                .setOnAction(event -> {
                    if (directoryChooser.getInitialDirectory() != null &&
                        !directoryChooser.getInitialDirectory().exists()) {
                        directoryChooser.setInitialDirectory(null);
                    }

                    selectedDir = controller.showOpenDirectoryDialog(directoryChooser);

                    if (selectedDir != null) {
                        view.getChooseContentDirectoryTextField().setText(selectedDir.getPath());
                        //view.getSelectedPackageDescriptionTextField().setText("");
                        //If the error message happens to be visible erase it.
                        view.getErrorLabel().setVisible(false);
                        directoryChooser.setInitialDirectory(selectedDir);
                    }
                });

    }

    @Override
    public void onContinuePressed() {
        try {
            if (selectedDir != null && selectedDir.exists() &&
                selectedDir.canRead()) {

                final PackageIpmBuilderService ipmBuilderService = new PackageIpmBuilderService(selectedDir);

                view.showProgressIndicatorPopUp();

                ((ProgressDialogPopup)view.getProgressIndicatorPopUp()).setCancelEventHandler(event -> ipmBuilderService.cancel());

                controller.setCrossPageProgressIndicatorPopUp(view.getProgressIndicatorPopUp());

                ipmBuilderService.setOnCancelled(event -> {
                    ipmBuilderService.reset();
                    controller.getCrossPageProgressIndicatorPopUp().hide();
                });

                ipmBuilderService.setOnFailed(workerStateEvent -> {
                    displayExceptionMessage(workerStateEvent.getSource().getException());
                    view.getErrorLabel().setVisible(true);
                    ipmBuilderService.reset();
                    controller.getCrossPageProgressIndicatorPopUp().hide();
                });

                ipmBuilderService.setOnSucceeded(workerStateEvent -> {
                    Node rootNode = (Node) workerStateEvent.getSource().getValue();
                    ipmBuilderService.reset();

                    if (rootNode != null) {
                        controller.setPackageTree(rootNode);
                        super.onContinuePressed();
                    }
                });

                ipmBuilderService.start();

            } else if (selectedDir != null &&
                    (!selectedDir.exists() ||
                         !selectedDir.canRead())) {
                view.getErrorLabel().setText(TextFactory.getText(ErrorKey.INACCESSIBLE_CONTENT_DIR));
                view.getErrorLabel().setVisible(true);
            } else {
                view.getErrorLabel().setText(TextFactory.getText(ErrorKey.BASE_DIRECTORY_NOT_SELECTED));
                view.getErrorLabel().setVisible(true);
            }
        } catch (Exception e) {
            view.getErrorLabel().setText(TextFactory.format(Messages.MessageKey.ERROR_CREATING_NEW_PACKAGE, e.getMessage()));
            view.getErrorLabel().setVisible(true);
            log.error(e.getMessage());
        }

    }

    @Override
    public void clear() {
        view.getChooseContentDirectoryTextField().setText("");
        view.getErrorLabel().setText("");
    }

    public javafx.scene.Node display() {
        return view.asNode();
    }

    protected void displayExceptionMessage(Throwable throwable) {
        view.getErrorLabel().setText(throwable.getMessage());
        view.getErrorLabel().setVisible(true);
    }

    @Override
    public void setIpmService(IPMService ipmService) {
        this.ipmService = ipmService;
    }

    /**
     * A {@link javafx.concurrent.Service} which executes the {@link javafx.concurrent.Task} of obtaining a
     * {@link org.dataconservancy.packaging.tool.model.ipm.Node} given a package ontology identifier and
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

                    //To support the cancelling of tree creation we check if the thread is interrupted
                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }

                    if (!controller.getDomainProfileService().assignNodeTypes(controller.getPrimaryDomainProfile(), root)) {
                        throw new IllegalStateException("Unable to assign Profile types to this file tree, please select a different profile or directory.");
                    }

                    return root;
                }
            };
        }
    }
}
