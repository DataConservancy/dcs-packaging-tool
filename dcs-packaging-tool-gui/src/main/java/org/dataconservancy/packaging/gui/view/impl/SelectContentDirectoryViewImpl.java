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
import org.dataconservancy.packaging.gui.presenter.impl.SelectContentDirectoryPresenter;
import org.dataconservancy.packaging.gui.view.SelectContentDirectoryView;

public class SelectContentDirectoryViewImpl extends BaseViewImpl<SelectContentDirectoryPresenter> implements SelectContentDirectoryView {

    private Button chooseContentDirectoryButton;
    private TextField currentContentDirectory;

    private Label errorMessage;

    public SelectContentDirectoryViewImpl(Labels labels) {
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
