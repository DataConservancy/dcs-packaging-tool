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
package org.dataconservancy.packaging.gui.view.impl;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.impl.OpenExistingPackagePresenterImpl;
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;

public class OpenExistingPackageViewImpl extends BaseViewImpl<OpenExistingPackagePresenterImpl> implements OpenExistingPackageView {

    private Button choosePackageStateFileButton;
    private TextField choosePackageStateFileTextField;

    private Button chooseExplodedPackageDirectoryButton;
    private TextField chooseExplodedPackageDirectoryTextField;

    private Button choosePackageFileButton;
    private TextField choosePackageFileTextField;
    private Button choosePackageStagingDirectoryButton;
    private TextField choosePackageStagingDirectoryTextField;

    public OpenExistingPackageViewImpl(Help help) {
        super();

        getContinueButton().setText(TextFactory.getText(Labels.LabelKey.CONTINUE_BUTTON));
        getCancelLink().setText(TextFactory.getText(Labels.LabelKey.BACK_LINK));

        VBox content = new VBox();
        content.setSpacing(32);
        content.getStyleClass().add(CREATE_NEW_PACKAGE_CLASS);

        setCenter(content);
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().add(errorLabel);

        VBox stagingVBox = new VBox(4);
        stagingVBox.setAlignment(Pos.TOP_LEFT);
        content.getChildren().add(stagingVBox);
        
        HBox stagingHBox = new HBox(6);
        stagingHBox.getStyleClass().add(DIRECTORY_BOX);
        stagingHBox.setMaxWidth(420);
        
        Label stagingLabel = new Label(TextFactory.getText(Labels.LabelKey.SELECT_STAGING_DIRECTORY));
        stagingLabel.getStyleClass().add(FORM_FIELDS_DIVISION_CLASS);
        stagingVBox.getChildren().add(stagingLabel);
        
        choosePackageStagingDirectoryButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
        choosePackageStagingDirectoryButton.setMinWidth(60);
        stagingHBox.getChildren().add(choosePackageStagingDirectoryButton);

        choosePackageStagingDirectoryTextField = new TextField();
        choosePackageStagingDirectoryTextField.setEditable(false);
        choosePackageStagingDirectoryTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        choosePackageStagingDirectoryTextField.setPrefWidth(320);
        
        stagingHBox.getChildren().add(choosePackageStagingDirectoryTextField);
        stagingVBox.getChildren().add(stagingHBox);
        
        VBox infoVBox = new VBox(4);
        Label selectOneOptionLabel = new Label(TextFactory.getText(Labels.LabelKey.SELECT_ONE_OPTION_LABEL));
        selectOneOptionLabel.getStyleClass().add(FORM_FIELDS_DIVISION_CLASS);
        infoVBox.getChildren().add(selectOneOptionLabel);
        infoVBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        content.getChildren().add(infoVBox);

        //Create the controls for choosing a package file
        VBox chooseFileDirSelectionFields = new VBox(4);
        chooseFileDirSelectionFields.setAlignment(Pos.TOP_LEFT);

        Label chooseFileLabel = new Label(TextFactory.getText(Labels.LabelKey.SELECT_IN_PROGRESS_PACKAGE_FILE_LABEL));
        chooseFileDirSelectionFields.getChildren().add(chooseFileLabel);

        HBox fileChooserSelector = new HBox(6);
        fileChooserSelector.getStyleClass().add(DIRECTORY_BOX);
        fileChooserSelector.setMaxWidth(420);

        choosePackageStateFileButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
        choosePackageStateFileButton.setMinWidth(60);
        fileChooserSelector.getChildren().add(choosePackageStateFileButton);

        choosePackageStateFileTextField = new TextField();
        choosePackageStateFileTextField.setEditable(false);
        choosePackageStateFileTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        choosePackageStateFileTextField.setPrefWidth(320);
        fileChooserSelector.getChildren().add(choosePackageStateFileTextField);
        chooseFileDirSelectionFields.getChildren().add(fileChooserSelector);

        Label choosePackageFileLabel = new Label(TextFactory.getText(Labels.LabelKey.SELECT_PACKAGE_FILE_LABEL));
        chooseFileDirSelectionFields.getChildren().add(choosePackageFileLabel);
        
        HBox packageFileChooserSelector = new HBox(6);
        packageFileChooserSelector.getStyleClass().add(DIRECTORY_BOX);
        packageFileChooserSelector.setMaxWidth(420);

        choosePackageFileButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
        choosePackageFileButton.setMinWidth(60);
        packageFileChooserSelector.getChildren().add(choosePackageFileButton);

        choosePackageFileTextField = new TextField();
        choosePackageFileTextField.setEditable(false);
        choosePackageFileTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        choosePackageFileTextField.setPrefWidth(320);
        packageFileChooserSelector.getChildren().add(choosePackageFileTextField);
        chooseFileDirSelectionFields.getChildren().add(packageFileChooserSelector);                

        Label choosePackageDirectoryLabel = new Label(TextFactory.getText(Labels.LabelKey.PACKAGE_DIRECTORY_LABEL));
        chooseFileDirSelectionFields.getChildren().add(choosePackageDirectoryLabel);

        HBox baseDirectorySelector = new HBox(6);
        baseDirectorySelector.getStyleClass().add(DIRECTORY_BOX);
        baseDirectorySelector.setMaxWidth(420);

        chooseExplodedPackageDirectoryButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
        chooseExplodedPackageDirectoryButton.setMinWidth(60);
        baseDirectorySelector.getChildren().add(chooseExplodedPackageDirectoryButton);

        chooseExplodedPackageDirectoryTextField = new TextField();
        chooseExplodedPackageDirectoryTextField.setEditable(false);
        chooseExplodedPackageDirectoryTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        chooseExplodedPackageDirectoryTextField.setPrefWidth(320);
        baseDirectorySelector.getChildren().add(chooseExplodedPackageDirectoryTextField);
        chooseFileDirSelectionFields.getChildren().add(baseDirectorySelector);

        content.getChildren().add(chooseFileDirSelectionFields);

        setHelpPopupContent(help.get(Help.HelpKey.OPEN_EXISTING_PACKAGE));
    }

    @Override
    public Button getChoosePackageStateFileButton() {
        return choosePackageStateFileButton;
    }

    @Override
    public TextField getChoosePackageStateFileTextField() {
        return choosePackageStateFileTextField;
    }

    @Override
    public Button getChooseExplodedPackageDirectoryButton() {
        return chooseExplodedPackageDirectoryButton;
    }

    @Override
    public TextField getChooseExplodedPackageDirectoryTextField() {
        return chooseExplodedPackageDirectoryTextField;
    }

    @Override
    public Button getChoosePackageFileButton() {
        return choosePackageFileButton;
    }

    @Override
    public TextField getChoosePackageFileTextField() {
        return choosePackageFileTextField;
    }

    @Override
    public Button getChoosePackageStagingDirectoryButton() {
        return choosePackageStagingDirectoryButton;
    }

    @Override
    public TextField getChoosePackageStagingDirectoryTextField() {
        return choosePackageStagingDirectoryTextField;
    }
}
