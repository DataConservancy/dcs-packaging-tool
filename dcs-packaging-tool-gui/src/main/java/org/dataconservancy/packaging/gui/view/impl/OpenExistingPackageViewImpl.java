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
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.presenter.impl.OpenExistingPackagePresenterImpl;
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;

public class OpenExistingPackageViewImpl extends BaseViewImpl<OpenExistingPackagePresenterImpl> implements OpenExistingPackageView {

    private Button chooseInProgressPackageFileButton;
    private TextField chooseInProgressFileTextField;

    private Button choosePackageDirectoryButton;
    private TextField choosePackageDirectoryTextField;

    private Button choosePackageFileButton;
    private TextField choosePackageFileTextField;

    private Label errorMessage;

    public OpenExistingPackageViewImpl(Labels labels) {
        super(labels);

        getContinueButton().setText(labels.get(Labels.LabelKey.CONTINUE_BUTTON));
        getCancelLink().setText(labels.get(Labels.LabelKey.BACK_LINK));

        VBox content = new VBox();
        content.setSpacing(32);
        content.getStyleClass().add(CREATE_NEW_PACKAGE_CLASS);

        setCenter(content);
        content.setAlignment(Pos.TOP_CENTER);
        errorMessage = new Label();
        errorMessage.setTextFill(Color.RED);
        errorMessage.setWrapText(true);
        errorMessage.setMaxWidth(600);
        content.getChildren().add(errorMessage);

        VBox infoVBox = new VBox(4);
        Label selectOneOptionLabel = new Label(labels.get(Labels.LabelKey.SELECT_ONE_OPTION_LABEL));
        selectOneOptionLabel.getStyleClass().add(FORM_FIELDS_DIVISION_CLASS);
        infoVBox.getChildren().add(selectOneOptionLabel);
        infoVBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        content.getChildren().add(infoVBox);

        //Create the controls for choosing a package file
        VBox chooseFileDirSelectionFields = new VBox(4);
        chooseFileDirSelectionFields.setAlignment(Pos.TOP_LEFT);

        Label chooseFileLabel = new Label(labels.get(Labels.LabelKey.SELECT_IN_PROGRESS_PACKAGE_FILE_LABEL));
        chooseFileDirSelectionFields.getChildren().add(chooseFileLabel);

        HBox fileChooserSelector = new HBox(6);
        fileChooserSelector.getStyleClass().add(DIRECTORY_BOX);
        fileChooserSelector.setMaxWidth(420);

        chooseInProgressPackageFileButton = new Button(labels.get(Labels.LabelKey.BROWSE_BUTTON));
        chooseInProgressPackageFileButton.setMinWidth(60);
        fileChooserSelector.getChildren().add(chooseInProgressPackageFileButton);

        chooseInProgressFileTextField = new TextField();
        chooseInProgressFileTextField.setEditable(false);
        chooseInProgressFileTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        chooseInProgressFileTextField.setPrefWidth(320);
        fileChooserSelector.getChildren().add(chooseInProgressFileTextField);
        chooseFileDirSelectionFields.getChildren().add(fileChooserSelector);

        Label choosePackageFileLabel = new Label(labels.get(Labels.LabelKey.SELECT_PACKAGE_FILE_LABEL));
        chooseFileDirSelectionFields.getChildren().add(choosePackageFileLabel);

        HBox packageFileChooserSelector = new HBox(6);
        packageFileChooserSelector.getStyleClass().add(DIRECTORY_BOX);
        packageFileChooserSelector.setMaxWidth(420);

        choosePackageFileButton = new Button(labels.get(Labels.LabelKey.BROWSE_BUTTON));
        choosePackageFileButton.setMinWidth(60);
        packageFileChooserSelector.getChildren().add(choosePackageFileButton);

        choosePackageFileTextField = new TextField();
        choosePackageFileTextField.setEditable(false);
        choosePackageFileTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        choosePackageFileTextField.setPrefWidth(320);
        packageFileChooserSelector.getChildren().add(choosePackageFileTextField);
        chooseFileDirSelectionFields.getChildren().add(packageFileChooserSelector);

        Label choosePackageDirectoryLabel = new Label(labels.get(Labels.LabelKey.PACKAGE_DIRECTORY_LABEL));
        chooseFileDirSelectionFields.getChildren().add(choosePackageDirectoryLabel);

        HBox baseDirectorySelector = new HBox(6);
        baseDirectorySelector.getStyleClass().add(DIRECTORY_BOX);
        baseDirectorySelector.setMaxWidth(420);

        choosePackageDirectoryButton = new Button(labels.get(Labels.LabelKey.BROWSE_BUTTON));
        choosePackageDirectoryButton.setMinWidth(60);
        baseDirectorySelector.getChildren().add(choosePackageDirectoryButton);

        choosePackageDirectoryTextField = new TextField();
        choosePackageDirectoryTextField.setEditable(false);
        choosePackageDirectoryTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        choosePackageDirectoryTextField.setPrefWidth(320);
        baseDirectorySelector.getChildren().add(choosePackageDirectoryTextField);
        chooseFileDirSelectionFields.getChildren().add(baseDirectorySelector);

        content.getChildren().add(chooseFileDirSelectionFields);


    }

    @Override
    public Button getChooseInProgressPackageFileButton() {
        return chooseInProgressPackageFileButton;
    }

    @Override
    public TextField getChooseInProgressFileTextField() {
        return chooseInProgressFileTextField;
    }

    @Override
    public Button getChoosePackageDirectoryButton() {
        return choosePackageDirectoryButton;
    }

    @Override
    public TextField getPackageDirectoryTextField() {
        return choosePackageDirectoryTextField;
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
    public Label getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setupHelp() {
        Label helpText = new Label(help.get(Help.HelpKey.OPEN_EXISTING_PACKAGE));
        helpText.setMaxWidth(300);
        helpText.setWrapText(true);
        helpText.setTextAlignment(TextAlignment.CENTER);
        setHelpPopupContent(helpText);
    }
}
