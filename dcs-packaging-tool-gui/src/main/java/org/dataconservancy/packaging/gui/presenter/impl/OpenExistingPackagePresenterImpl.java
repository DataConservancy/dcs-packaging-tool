package org.dataconservancy.packaging.gui.presenter.impl;

import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;
import org.dataconservancy.packaging.tool.model.BagItParameterNames;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OpenExistingPackagePresenterImpl extends BasePresenterImpl
        implements Presenter {

    private OpenExistingPackageView view;
    private File contentDir;
    private File rootArtifactDir;
    private DirectoryChooser directoryChooser;
    private FileChooser fileChooser;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public OpenExistingPackagePresenterImpl(OpenExistingPackageView view) {
        super(view);
        this.view = view;
        this.contentDir = null;
        directoryChooser = new DirectoryChooser();
        fileChooser = new FileChooser();

        view.setPresenter(this);
        bind();
    }

    private void bind() {

        // Handles the user pressing the continue button in the footer.
        view.getContinueButton().setOnAction(event -> {
            // TODO: Needs to be able to read the existing package metadata whether via a dir or file and set it in the
            // PackageState and move the user to the package metadata page. i.e.:
            getController().getPackageState().setPackageName("FakeLoadedPackageName");
            getController().getPackageState().addPackageMetadata(GeneralParameterNames.DOMAIN_PROFILE, "FakeProfile");
            getController().goToNextPage(Page.PACKAGE_METADATA);
        });

        //Handles the user pressing the button to choose the content directory of the package
        view.getChoosePackageDirectoryButton().setOnAction(event -> {
           // TODO: Handle choosing a directory and validating the dir.
        });

        view.getChooseInProgressPackageFileButton().setOnAction(event -> {
            // TODO: Handle choosing a package file and validating it. Also need to ask for a dir to decompress if the file is compressed.

            /* Here's some existing code that may be reused.
            File descriptionFile = controller.showOpenFileDialog(fileChooser);

            if (descriptionFile != null) {
                try {
                    FileInputStream fis = new FileInputStream(descriptionFile);
                    PackageDescription description = packageDescriptionBuilder.deserialize(fis);
                    //If the selected package description file is valid set it on the controller and remove the content directory if it was set.
                    if (description != null) {
                        //contentDir = null;
                        controller.setPackageDescription(description);
                        controller.setPackageDescriptionFile(descriptionFile);
                        controller.setRootArtifactDir(null);
                        controller.setContentRoot(null);
                        contentDir = null;
                        rootArtifactDir = null;

                        view.getErrorMessage().setVisible(false);
                        view.getSelectedPackageDescriptionTextField().setText(descriptionFile.getPath());
                        view.getSelectedBaseDirectoryTextField().setText("");
                        fileChooser.setInitialDirectory(descriptionFile.getParentFile());
                    }
                } catch (FileNotFoundException | PackageToolException e) {
                    view.getErrorMessage().setText(messages.formatPackageDescriptionBuilderFailure(descriptionFile.getName()));
                    view.getErrorMessage().setVisible(true);
                    log.error(e.getMessage());
                }
            }
            */
        });
    }

    @Override
    public void clear() {
        // Default method body
        view.getPackageDirectoryTextField().setText("");
        view.getErrorMessage().setText("");

        contentDir = null;
        rootArtifactDir = null;
    }

    @Override
    public Node display() {
        //Setup help content and then rebind the base class to this view.
        view.setupHelp();
        setView(view);
        super.bindBaseElements();

        return view.asNode();
    }
}
