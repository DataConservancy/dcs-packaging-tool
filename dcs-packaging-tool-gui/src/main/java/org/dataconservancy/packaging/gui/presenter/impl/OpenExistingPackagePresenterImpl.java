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


import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.apache.commons.lang.SystemUtils;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.OpenExistingPackagePresenter;
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class OpenExistingPackagePresenterImpl extends BasePresenterImpl implements OpenExistingPackagePresenter {
    private OpenExistingPackageView view;
    private DirectoryChooser directoryChooser;
    private FileChooser fileChooser;
    private File selectedFile;
    private FILE_TYPE selectedFileType;
    private File stagingDir;
    private LoadPackageService loadPackageBackgroundService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public OpenExistingPackagePresenterImpl(OpenExistingPackageView view) {
        super(view);
        this.view = view;
        this.directoryChooser = new DirectoryChooser();
        this.fileChooser = new FileChooser();

        view.setPresenter(this);
        bind();

        // Staging directory is working directory by default.
        stagingDir = new File(System.getProperty("user.dir"));

        //If we can't write to the current working directory switch to the java temp dir which we should have write access to
        if (!Files.isWritable(FileSystems.getDefault().getPath(stagingDir.getPath()))) {
            stagingDir = new File(System.getProperty("java.io.tmpdir"));
        }
    }

    private void bind() {
        if (loadPackageBackgroundService == null) {
            loadPackageBackgroundService = new BackgroundPackageService();
        }

        // Background load service called when continue button fired
        loadPackageBackgroundService.setOnSucceeded(t -> {
            if (Platform.isFxApplicationThread()) {
                view.getProgressPopup().hide();
            }
            
            OpenedPackage pkg = (OpenedPackage) t.getSource().getValue();
            
            loadPackageBackgroundService.reset();
            
            controller.setPackageState(pkg.getPackageState());
            controller.setPackageTree(pkg.getPackageTree());

            if (pkg.getBaseDirectory() == null) {
                // Package state was loaded, go to next page.
                
                controller.setPackageStateFileChooserInitialChoice(selectedFile);
                controller.goToNextPage();
            } else {
                // Package was loaded, save state and go to next page.
                super.onContinuePressed();
            }
        });

        loadPackageBackgroundService.setOnFailed(workerStateEvent -> {
            if (Platform.isFxApplicationThread()) {
                view.getProgressPopup().hide();
            }
            
            if (workerStateEvent.getSource().getMessage() == null ||
                    workerStateEvent.getSource().getMessage().isEmpty()) {
                Throwable e = workerStateEvent.getSource().getException();
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR, e.getMessage()));
                log.error(e.getMessage());
            } else {
                showError(workerStateEvent.getSource().getMessage());
            }

            loadPackageBackgroundService.reset();
        });

        loadPackageBackgroundService.setOnCancelled(workerStateEvent -> {
            if (Platform.isFxApplicationThread()) {
                view.getProgressPopup().hide();
            }
            
            showError(workerStateEvent.getSource().getMessage());

            loadPackageBackgroundService.reset();
        });

        // User changes staging directory
        view.getChoosePackageStagingDirectoryButton().setOnAction(event -> {
            File file = controller.showOpenDirectoryDialog(directoryChooser);

            if (file == null) {
                return;
            }

            stagingDir = file;
            view.getChoosePackageStagingDirectoryTextField().setText(stagingDir.getPath());
        });

        // User selects an serialized package or a package state
        view.getChoosePackageFileButton().setOnAction(event -> {
            //Disables file extension filtering on mac since it doesn't seem to reliably work
            if (!SystemUtils.IS_OS_MAC) {
                fileChooser.getExtensionFilters().clear();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package File", "*.zip", "*.ZIP", "*.tar", "*.TAR", "*.gz", "*.GZ", "*.gzip", "*.GZIP", controller.getPackageStateFileExtension()));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));
            }

            File file = controller.showOpenFileDialog(fileChooser);

            if (file == null) {
                return;
            }

            clear();
            view.getChoosePackageFileTextField().setText(file.getName());
            view.getContinueButton().setDisable(false);
            
            selectedFile = file;
            
            // Check if package or state file
            String state_ext = controller.getPackageStateFileExtension().substring(1);
            
            if (selectedFile.getName().endsWith(state_ext)) {
                selectedFileType = FILE_TYPE.STATE_FILE;    
            } else {
                selectedFileType = FILE_TYPE.PACKAGE;
            }
        });

        // User selects an exploded package
        view.getChooseExplodedPackageDirectoryButton().setOnAction(event -> {
            File file = controller.showOpenDirectoryDialog(directoryChooser);

            if (file == null) {
                return;
            }

            clear();
            view.getChooseExplodedPackageDirectoryTextField().setText(file.getName());
            view.getContinueButton().setDisable(false);
            
            selectedFile = file;
            selectedFileType = FILE_TYPE.EXPLODED_PACKAGE;
        });
    }

    // Needed for testing.
    protected File getSelectedFile() {
        return selectedFile;
    }
    
    // Needed for testing.
    protected FILE_TYPE getSelectedFileType() {
        return selectedFileType;
    }
    
    @Override
    public void onContinuePressed() {
        loadPackageBackgroundService.execute(selectedFile, selectedFileType);
        
         if (Platform.isFxApplicationThread()) {
            view.getProgressPopup().show();
        }             
    }
    
    @Override
    public void onBackPressed() {
        // Do not save state.
        getController().goToPreviousPage();
    }
 
    @Override
    public void clear() {
        clearError();

        view.getChooseExplodedPackageDirectoryTextField().setText("");
        view.getChoosePackageFileTextField().setText("");
        view.getChoosePackageStagingDirectoryTextField().setText(System.getProperty("user.home"));
        view.getContinueButton().setDisable(true);
    }

    @Override
    public Node display() {
        clear();
        view.getHeaderViewHelpLink().setOnAction(arg0 -> view.showHelpPopup());
        return view.asNode();
    }

    //Method should only be used for testing, will run all code on the same thread to simplify the test.
    protected void setTestBackgroundService() {
        this.loadPackageBackgroundService = new AsyncPackageService();
        bind();
    }

    protected enum FILE_TYPE {
        STATE_FILE,
        EXPLODED_PACKAGE,
        PACKAGE
    }

    /*
     * Simple interface that shadows JavaFX service this is used so we can create our own instance to use in testing.
     */
    protected interface LoadPackageService {
        void execute(File file, FILE_TYPE fileType);

        void setOnFailed(EventHandler<WorkerStateEvent> handler);

        void setOnCancelled(EventHandler<WorkerStateEvent> handler);

        void setOnSucceeded(EventHandler<WorkerStateEvent> handler);

        void reset();

        void cancel();
    }

    private class BackgroundPackageService implements LoadPackageService {

        BackgroundService service;

        public BackgroundPackageService() {
            service = new BackgroundService();
        }

        @Override
        public void execute(File file, FILE_TYPE fileType) {
            service.setPackageFile(file, fileType);
            service.start();
        }

        @Override
        public void setOnFailed(EventHandler<WorkerStateEvent> handler) {
            service.setOnFailed(handler);
        }

        @Override
        public void setOnCancelled(EventHandler<WorkerStateEvent> handler) {
            service.setOnCancelled(handler);
        }

        @Override
        public void setOnSucceeded(EventHandler<WorkerStateEvent> handler) {
            service.setOnSucceeded(handler);
        }

        @Override
        public void cancel() {
            service.cancel();
        }

        @Override
        public void reset() {
            service.reset();
        }

        /**
         * A {@link javafx.concurrent.Service} which executes the {@link javafx.concurrent.Task} for validating the node properties in the tree.
         */
        private class BackgroundService extends Service<OpenedPackage> {

            File packageFile;
            FILE_TYPE fileType;

            public BackgroundService() {
            }

            public void setPackageFile(File packageFile, FILE_TYPE fileType) {
                this.packageFile = packageFile;
                this.fileType = fileType;
            }

            @Override
            protected Task<OpenedPackage> createTask() {
                return new Task<OpenedPackage>() {
                    @Override
                    protected OpenedPackage call() throws Exception {
                        switch (fileType) {
                            case STATE_FILE:
                                return controller.getFactory().getOpenPackageService().openPackageState(packageFile);
                            case EXPLODED_PACKAGE:
                                return controller.getFactory().getOpenPackageService().openExplodedPackage(packageFile);
                            case PACKAGE:
                                return controller.getFactory().getOpenPackageService().openPackage(stagingDir, packageFile);
                        }

                        return null;
                    }
                };
            }
        }
    }

    /*
     * Generate Package Service used for testing, this implementation mimics JavaFX service but operates on the same thread, to make it easier for testing. Also prevents
     * tests from needing to run on the JavaFX application thread.
     */
    private class AsyncPackageService implements LoadPackageService {
        @SuppressWarnings("unused")
        private EventHandler<WorkerStateEvent> cancelledHandler;
        private EventHandler<WorkerStateEvent> successHandler;

        @Override
        public void execute(File packageFile, FILE_TYPE fileType) {
            AsyncWorker worker = new AsyncWorker();

            try {
                switch (fileType) {
                    case STATE_FILE:
                        worker.setValue(controller.getFactory().getOpenPackageService().openPackageState(packageFile));
                    case EXPLODED_PACKAGE:
                        worker.setValue(controller.getFactory().getOpenPackageService().openExplodedPackage(packageFile));
                    case PACKAGE:
                        worker.setValue(controller.getFactory().getOpenPackageService().openPackage(stagingDir, packageFile));
                }

            } catch (Exception e) {
                worker.setState(Worker.State.FAILED);
                worker.setMessage(e.getMessage());
            }
            worker.setState(Worker.State.SUCCEEDED);
            successHandler.handle(new WorkerStateEvent(worker, WorkerStateEvent.WORKER_STATE_SUCCEEDED));
        }

        @Override
        public void setOnFailed(EventHandler<WorkerStateEvent> handler) {
        }

        @Override
        public void setOnCancelled(EventHandler<WorkerStateEvent> handler) {
            cancelledHandler = handler;
        }

        @Override
        public void setOnSucceeded(EventHandler<WorkerStateEvent> handler) {
            successHandler = handler;
        }

        @Override
        public void reset() {
        }

        @Override
        public void cancel() {
        }

        private class AsyncWorker implements Worker<Object> {

            private Object value;
            private String message;
            private State state;

            protected void setMessage(String message) {
                this.message = message;
            }

            protected void setState(State state) {
                this.state = state;
            }

            @Override
            public State getState() {
                return state;
            }

            @Override
            public ReadOnlyObjectProperty<State> stateProperty() {
                return null;
            }

            @Override
            public Object getValue() {
                return value;
            }

            public void setValue(Object object) {
                this.value = object;
            }

            @Override
            public ReadOnlyObjectProperty<Object> valueProperty() {
                return null;
            }

            @Override
            public Throwable getException() {
                return null;
            }

            @Override
            public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
                return null;
            }

            @Override
            public double getWorkDone() {
                return 0;
            }

            @Override
            public ReadOnlyDoubleProperty workDoneProperty() {
                return null;
            }

            @Override
            public double getTotalWork() {
                return 0;
            }

            @Override
            public ReadOnlyDoubleProperty totalWorkProperty() {
                return null;
            }

            @Override
            public double getProgress() {
                return 0;
            }

            @Override
            public ReadOnlyDoubleProperty progressProperty() {
                return null;
            }

            @Override
            public boolean isRunning() {
                return false;
            }

            @Override
            public ReadOnlyBooleanProperty runningProperty() {
                return null;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public ReadOnlyStringProperty messageProperty() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public ReadOnlyStringProperty titleProperty() {
                return null;
            }

            @Override
            public boolean cancel() {
                return false;
            }
        }
    }

}
