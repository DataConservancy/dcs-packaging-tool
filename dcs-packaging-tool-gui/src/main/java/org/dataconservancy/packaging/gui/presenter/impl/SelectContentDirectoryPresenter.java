package org.dataconservancy.packaging.gui.presenter.impl;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import org.dataconservancy.packaging.gui.Errors;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.view.SelectContentDirectoryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SelectContentDirectoryPresenter extends BasePresenterImpl
        implements Presenter {

    private SelectContentDirectoryView view;
    private File content_dir;
    private File root_artifact_dir;
    private DirectoryChooser directoryChooser;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public SelectContentDirectoryPresenter(SelectContentDirectoryView view) {
        super(view);
        this.view = view;
        this.content_dir = null;
        directoryChooser = new DirectoryChooser();

        view.setPresenter(this);
        bind();
    }

    private void bind() {

        //Handles the continue button in the footer being pressed. Validates that all required fields are present, sets the state on the controller,
        //and instructs the controller to move to the next page.
        view.getContinueButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (content_dir != null && content_dir.exists() &&
                        content_dir.canRead()) {
                        controller.setContentRoot(content_dir);
                        controller.setRootArtifactDir(root_artifact_dir);
                        controller.goToNextPage();
                    } else if (content_dir != null && (!content_dir.exists() || !content_dir.canRead()) ||
                        root_artifact_dir != null &&
                            (!root_artifact_dir.exists() || !root_artifact_dir.canRead())) {
                        view.getErrorMessage().setText(errors.get(Errors.ErrorKey.INACCESSIBLE_CONTENT_DIR));
                        view.getErrorMessage().setVisible(true);
                    } else {
                        view.getErrorMessage().setText(errors.get(Errors.ErrorKey.CONTENT_DIRECTORY_ERROR));
                        view.getErrorMessage().setVisible(true);
                    }
                } catch (Exception e) {
                    view.getErrorMessage().setText(messages.formatErrorCreatingNewPackage(e.getMessage()));
                    view.getErrorMessage().setVisible(true);
                    log.error(e.getMessage());
                }
            }
        });

        //Handles the user pressing the button to choose the content directory of the package
        view.getChooseContentDirectoryButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (directoryChooser.getInitialDirectory() != null &&
                    !directoryChooser.getInitialDirectory().exists()) {
                    directoryChooser.setInitialDirectory(null);
                }

                File dir = controller.showOpenDirectoryDialog(directoryChooser);

                if (dir != null) {
                    root_artifact_dir = dir;
                    content_dir = root_artifact_dir.getParentFile();
                    view.getSelectedContentDirectoryTextField().setText(root_artifact_dir.getPath());

                    //If the error message happens to be visible erase it.
                    view.getErrorMessage().setVisible(false);
                    directoryChooser.setInitialDirectory(dir);
                }
            }
        });
    }

    @Override
    public Node display(boolean clear) {
        //Clear out any values that already exist, this happens when canceling and returning to this screen.
        if (clear) {
            view.getSelectedContentDirectoryTextField().setText("");
            view.getErrorMessage().setText("");

            content_dir = null;
            root_artifact_dir = null;
        }

        //Setup help content and then rebind the base class to this view.
        view.setupHelp();
        setView(view);
        super.bindBaseElements();

        return view.asNode();
    }
}
