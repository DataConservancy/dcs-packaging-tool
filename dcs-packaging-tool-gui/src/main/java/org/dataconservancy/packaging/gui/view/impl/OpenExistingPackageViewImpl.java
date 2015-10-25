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

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private Button chooseContentDirectoryButton;
    private TextField currentContentDirectory;

    private Button chooseFileButton;
    private TextField chooseFileTextField;

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

        //Create the controls for choosing the content root directory
        VBox contentDirectorySelectionFields = new VBox(4);
        contentDirectorySelectionFields.setAlignment(Pos.TOP_LEFT);

        Label chooseContentDirectoryLabel = new Label(labels.get(Labels.LabelKey.CONTENT_DIRECTORY_LABEL));
        contentDirectorySelectionFields.getChildren().add(chooseContentDirectoryLabel);

        HBox baseDirectorySelector = new HBox(6);
        baseDirectorySelector.getStyleClass().add(DIRECTORY_BOX);
        baseDirectorySelector.setMaxWidth(420);

        chooseContentDirectoryButton = new Button(labels.get(Labels.LabelKey.BROWSE_BUTTON));
        chooseContentDirectoryButton.setMinWidth(60);
        baseDirectorySelector.getChildren().add(chooseContentDirectoryButton);

        currentContentDirectory = new TextField();
        currentContentDirectory.setEditable(false);
        currentContentDirectory.getStyleClass().add(INVISBILE_TEXT_FIELD);
        currentContentDirectory.setPrefWidth(320);
        baseDirectorySelector.getChildren().add(currentContentDirectory);
        contentDirectorySelectionFields.getChildren().add(baseDirectorySelector);

        content.getChildren().add(contentDirectorySelectionFields);

        VBox orLabelVBox = new VBox(4);
        Label orLabel = new Label(labels.get(Labels.LabelKey.OR_LABEL));
        orLabel.setAlignment(Pos.TOP_LEFT);
        orLabelVBox.getChildren().add(orLabel);

        content.getChildren().add(orLabelVBox);

        //Create the controls for choosing a package file
        VBox chooseFileSelectionFields = new VBox(4);
        chooseFileSelectionFields.setAlignment(Pos.TOP_LEFT);

        Label chooseFileLabel = new Label(labels.get(Labels.LabelKey.SELECT_PACKAGE_FILE_LABEL));
        chooseFileSelectionFields.getChildren().add(chooseFileLabel);

        HBox fileChooserSelector = new HBox(6);
        fileChooserSelector.getStyleClass().add(DIRECTORY_BOX);
        fileChooserSelector.setMaxWidth(420);

        chooseFileButton = new Button(labels.get(Labels.LabelKey.BROWSE_BUTTON));
        chooseFileButton.setMinWidth(60);
        fileChooserSelector.getChildren().add(chooseFileButton);

        chooseFileTextField = new TextField();
        chooseFileTextField.setEditable(false);
        chooseFileTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        chooseFileTextField.setPrefWidth(320);
        fileChooserSelector.getChildren().add(chooseFileTextField);
        chooseFileSelectionFields.getChildren().add(fileChooserSelector);

        content.getChildren().add(chooseFileSelectionFields);
    }

    @Override
    public Button getChooseContentDirectoryButton() {
        return chooseContentDirectoryButton;
    }

    @Override
    public TextField getSelectedContentDirectoryTextField() {
        return currentContentDirectory;
    }

    @Override
    public Button getChooseFileButton() {
        return chooseFileButton;
    }

    @Override
    public TextField getChooseFileTextField() {
        return chooseFileTextField;
    }

    @Override
    public Label getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setupHelp() {
        Label helpText = new Label(help.get(Help.HelpKey.CREATE_NEW_PACKAGE_HELP));
        helpText.setMaxWidth(300);
        helpText.setWrapText(true);
        helpText.setTextAlignment(TextAlignment.CENTER);
        setHelpPopupContent(helpText);
    }
}
