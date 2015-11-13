package org.dataconservancy.packaging.gui.presenter.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class OpenExistingPackagePresenterImpl extends BasePresenterImpl implements Presenter {
    private OpenExistingPackageView view;
    private DirectoryChooser directoryChooser;
    private FileChooser fileChooser;
    private PackageState loadedState;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public OpenExistingPackagePresenterImpl(OpenExistingPackageView view) {
        super(view);
        this.view = view;
        this.directoryChooser = new DirectoryChooser();
        this.fileChooser = new FileChooser();

        view.setPresenter(this);
        bind();
    }   

    private PackageState load_package_state(File file) throws IOException {
        PackageState state = new PackageState();
        
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            clearError();
            controller.getPackageStateSerializer().deserialize(state, is);
            view.getContinueButton().setDisable(false);
        }
        
        return state;
    }
    
    // TODO
    
    private PackageState load_package_state_from_package(File file) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    private PackageState load_package_state_from_exploded_package(File file) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    private void bind() {
        // User selects a package state file
        view.getChooseInProgressPackageFileButton().setOnAction(event -> {
            File file = controller.showOpenFileDialog(fileChooser);
           
            if (file == null) {
                return;
            }
            
            clear();
            
            view.getChooseInProgressPackageFileTextField().setText(file.getName());

            try {
                loadedState = load_package_state(file);
                view.getContinueButton().setDisable(false);
            } catch (IOException e) {
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR));
                log.error(e.getMessage());
            }
        });

        // User selects an serialized package
        view.getChoosePackageFileButton().setOnAction(event -> {
            File file = controller.showOpenFileDialog(fileChooser);
            
            if (file == null) {
                return;
            }
            
            clear();
            
            view.getChoosePackageFileTextField().setText(file.getName());

            try {
                loadedState = load_package_state_from_package(file);
                view.getContinueButton().setDisable(false);
            } catch (IOException e) {
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR));
                log.error(e.getMessage());
            }
        });

        // User selects an exploded package
        view.getChoosePackageDirectoryButton().setOnAction(event -> {
            File file = controller.showOpenDirectoryDialog(directoryChooser);
            
            if (file == null) {
                return;
            }
            
            clear();
            
            view.getChoosePackageDirectoryTextField().setText(file.getName());

            try {
                loadedState = load_package_state_from_exploded_package(file);
                view.getContinueButton().setDisable(false);
            } catch (IOException e) {
                showError(TextFactory.format(ErrorKey.PACKAGE_STATE_LOAD_ERROR));
                log.error(e.getMessage());
            }
        });
    }

    @Override
    public void onContinuePressed() {
        controller.setPackageState(loadedState);
        super.onContinuePressed();
    }

    @Override
    public void clear() {
        clearError();

        view.getChooseInProgressPackageFileTextField().setText("");
        view.getChoosePackageDirectoryTextField().setText("");
        view.getChoosePackageFileTextField().setText(""); 
        
        loadedState = null;
        view.getContinueButton().setDisable(true);
    }

    @Override
    public Node display() {
        clear();
        
        return view.asNode();
    }
}
