package org.dataconservancy.packaging.gui.presenter.impl;

import java.io.File;
import java.io.IOException;

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

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public OpenExistingPackagePresenterImpl(OpenExistingPackageView view) {
        super(view);
        this.view = view;
        this.directoryChooser = new DirectoryChooser();
        this.fileChooser = new FileChooser();

        view.setPresenter(this);
        bind();

        // Staging directory is working directory by default.
        // TODO Configurable?
        stagingDir = new File(System.getProperty("user.dir"));
    }

    private void bind() {
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

            try {
                pkg = controller.getFactory().getOpenPackageService().openPackageState(file);
                view.getContinueButton().setDisable(false);
            } catch (IOException e) {
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR));
                log.error(e.getMessage());
            }
        });

        // User selects an serialized package
        view.getChoosePackageFileButton().setOnAction(event -> {
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package File (.zip)", "*zip"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package File (.gz)", "*gz"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package File (.gzip)", "*gzip"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package File (.tar)", "*tar"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*"));

            File file = controller.showOpenFileDialog(fileChooser);

            if (file == null) {
                return;
            }

            clear();
            view.getChoosePackageFileTextField().setText(file.getName());

            try {
                pkg = controller.getFactory().getOpenPackageService().openPackage(stagingDir, file);

                view.getContinueButton().setDisable(false);
            } catch (IOException e) {
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR));
                log.error(e.getMessage());
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

            try {
                pkg = controller.getFactory().getOpenPackageService().openExplodedPackage(file);

                view.getContinueButton().setDisable(false);
            } catch (IOException e) {
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR));
                log.error(e.getMessage());
            }
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
}
