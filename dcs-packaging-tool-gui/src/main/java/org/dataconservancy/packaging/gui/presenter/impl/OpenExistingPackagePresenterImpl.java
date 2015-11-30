package org.dataconservancy.packaging.gui.presenter.impl;

import java.io.File;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
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
    private OpenedPackage pkg;
    private File stagingDir;
    private LoadPackageBackgroundService loadPackageBackgroundService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public OpenExistingPackagePresenterImpl(OpenExistingPackageView view) {
        super(view);
        this.view = view;
        this.directoryChooser = new DirectoryChooser();
        this.fileChooser = new FileChooser();
        loadPackageBackgroundService = new LoadPackageBackgroundService();

        view.setPresenter(this);
        bind();

        // Staging directory is working directory by default.
        // TODO Configurable?
        stagingDir = new File(System.getProperty("user.dir"));
    }

    private void bind() {
        loadPackageBackgroundService.setOnSucceeded(t -> {
            view.getProgressPopup().hide();
            pkg = (OpenedPackage) t.getSource().getValue();
            loadPackageBackgroundService.reset();
            view.getContinueButton().setDisable(false);
        });

        loadPackageBackgroundService.setOnFailed(workerStateEvent -> {

            view.getProgressPopup().hide();
            if (workerStateEvent.getSource().getMessage() == null ||
                    workerStateEvent.getSource().getMessage().isEmpty()) {
                Throwable e = workerStateEvent.getSource().getException();
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR));
                log.error(e.getMessage());
            } else {
                view.getErrorLabel().setText(workerStateEvent.getSource().getMessage());
            }

            view.getErrorLabel().setTextFill(Color.RED);
            view.getErrorLabel().setVisible(true);
            loadPackageBackgroundService.reset();
        });

        loadPackageBackgroundService.setOnCancelled(workerStateEvent -> {
            if (Platform.isFxApplicationThread()) {
                view.getProgressPopup().hide();
            }
            view.getErrorLabel().setText(workerStateEvent.getSource().getMessage());
            view.getErrorLabel().setTextFill(Color.RED);
            view.getErrorLabel().setVisible(true);
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

        // User selects a package state file
        view.getChoosePackageStateFileButton().setOnAction(event -> {
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package State File (.zip)", controller.getPackageStateFileExtension()));

            File file = controller.showOpenFileDialog(fileChooser);

            if (file == null) {
                return;
            }

            clear();
            view.getChoosePackageStateFileTextField().setText(file.getName());

            loadPackageBackgroundService.setPackageFile(file, FILE_TYPE.STATE_FILE);
            loadPackageBackgroundService.start();
            view.getProgressPopup().show();
        });

        // User selects an serialized package
        view.getChoosePackageFileButton().setOnAction(event -> {
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package File", "*zip", "*ZIP", "*tar", "*TAR", "*gz", "*GZ", "*gzip", "*GZIP"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*"));

            File file = controller.showOpenFileDialog(fileChooser);

            if (file == null) {
                return;
            }

            clear();
            view.getChoosePackageFileTextField().setText(file.getName());

            loadPackageBackgroundService.setPackageFile(file, FILE_TYPE.PACKAGE);
            loadPackageBackgroundService.start();
            view.getProgressPopup().show();
        });

        // User selects an exploded package
        view.getChooseExplodedPackageDirectoryButton().setOnAction(event -> {
            File file = controller.showOpenDirectoryDialog(directoryChooser);

            if (file == null) {
                return;
            }

            clear();

            view.getChooseExplodedPackageDirectoryTextField().setText(file.getName());

            loadPackageBackgroundService.setPackageFile(file, FILE_TYPE.EXPLODED_PACKAGE);
            loadPackageBackgroundService.start();
            view.getProgressPopup().show();
        });
    }

    @Override
    public void onContinuePressed() {
        controller.setPackageState(pkg.getPackageState());
        controller.setPackageTree(pkg.getPackageTree());

        super.onContinuePressed();
    }

    @Override
    public void clear() {
        clearError();

        view.getChoosePackageStateFileTextField().setText("");
        view.getChooseExplodedPackageDirectoryTextField().setText("");
        view.getChoosePackageFileTextField().setText("");
        view.getChoosePackageStagingDirectoryTextField().setText(stagingDir.getPath());
        view.getContinueButton().setDisable(true);
    }

    @Override
    public Node display() {
        clear();

        return view.asNode();
    }

    private enum FILE_TYPE {
        STATE_FILE,
        EXPLODED_PACKAGE,
        PACKAGE
    }

    /**
     * A {@link javafx.concurrent.Service} which executes the {@link javafx.concurrent.Task} for validating the node properties in the tree.
     */
    private class LoadPackageBackgroundService extends Service<OpenedPackage> {

        File packageFile;
        FILE_TYPE fileType;

        public LoadPackageBackgroundService() {
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
