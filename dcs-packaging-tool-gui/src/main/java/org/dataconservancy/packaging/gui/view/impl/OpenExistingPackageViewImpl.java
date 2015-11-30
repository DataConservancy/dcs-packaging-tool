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

import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.impl.OpenExistingPackagePresenterImpl;
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
             
        VBox infoVBox = new VBox(4);
        Label selectOneOptionLabel = new Label(TextFactory.getText(Labels.LabelKey.SELECT_ONE_OPTION_LABEL));
        selectOneOptionLabel.getStyleClass().add(FORM_FIELDS_DIVISION_CLASS);
        infoVBox.getChildren().addAll(selectOneOptionLabel, new Separator(Orientation.HORIZONTAL));
        
        VBox optionVBox = new VBox(4);
        optionVBox.setAlignment(Pos.TOP_LEFT);

        // Choose a package state
        {
            Label label = new Label(TextFactory.getText(Labels.LabelKey.SELECT_IN_PROGRESS_PACKAGE_FILE_LABEL));
            optionVBox.getChildren().add(label);
    
            HBox hbox = new HBox(6);
            hbox.getStyleClass().add(DIRECTORY_BOX);
            hbox.setMaxWidth(420);
    
            choosePackageStateFileButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
            choosePackageStateFileButton.setMinWidth(60);

            choosePackageStateFileTextField = new TextField();
            choosePackageStateFileTextField.setEditable(false);
            choosePackageStateFileTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
            choosePackageStateFileTextField.setPrefWidth(320);
            
            hbox.getChildren().addAll(choosePackageStateFileButton, choosePackageStateFileTextField);
            optionVBox.getChildren().add(hbox);
        }
        
        // Choose a package and extraction directory
        {
            HBox package_hbox = new HBox(6);
            package_hbox.getStyleClass().add(DIRECTORY_BOX);
            package_hbox.setMaxWidth(420);
            
            HBox extract_hbox = new HBox(6);
            extract_hbox.getStyleClass().add(DIRECTORY_BOX);
            extract_hbox.setMaxWidth(420);
            
            Label package_label = new Label(TextFactory.getText(Labels.LabelKey.SELECT_PACKAGE_FILE_LABEL));
            Label extract_label = new Label(TextFactory.getText(Labels.LabelKey.SELECT_STAGING_DIRECTORY));
            
            choosePackageStagingDirectoryButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
            choosePackageStagingDirectoryButton.setMinWidth(60);
            
            choosePackageStagingDirectoryTextField = new TextField();
            choosePackageStagingDirectoryTextField.setEditable(false);
            choosePackageStagingDirectoryTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
            choosePackageStagingDirectoryTextField.setPrefWidth(320);
            
            choosePackageFileButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
            choosePackageFileButton.setMinWidth(60);

            choosePackageFileTextField = new TextField();
            choosePackageFileTextField.setEditable(false);
            choosePackageFileTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
            choosePackageFileTextField.setPrefWidth(320);
            
            package_hbox.getChildren().addAll(choosePackageFileButton, choosePackageFileTextField);
            extract_hbox.getChildren().addAll(choosePackageStagingDirectoryButton, choosePackageStagingDirectoryTextField);
            
            optionVBox.getChildren().addAll(package_label, package_hbox, extract_label, extract_hbox);
            
            // Indent from the left
            VBox.setMargin(package_hbox, new Insets(0, 0, 0, 20));
            VBox.setMargin(extract_hbox, new Insets(0, 0, 0, 20));
            VBox.setMargin(extract_label, new Insets(0, 0, 0, 20));
        }
        
        // Choose a exploded package
        {
            Label label = new Label(TextFactory.getText(Labels.LabelKey.PACKAGE_DIRECTORY_LABEL));
            optionVBox.getChildren().add(label);

            HBox hbox = new HBox(6);
            hbox.getStyleClass().add(DIRECTORY_BOX);
            hbox.setMaxWidth(420);

            chooseExplodedPackageDirectoryButton = new Button(TextFactory.getText(Labels.LabelKey.BROWSE_BUTTON));
            chooseExplodedPackageDirectoryButton.setMinWidth(60);
            
            chooseExplodedPackageDirectoryTextField = new TextField();
            chooseExplodedPackageDirectoryTextField.setEditable(false);
            chooseExplodedPackageDirectoryTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
            chooseExplodedPackageDirectoryTextField.setPrefWidth(320);
            
            hbox.getChildren().addAll(chooseExplodedPackageDirectoryButton, chooseExplodedPackageDirectoryTextField);
            optionVBox.getChildren().add(hbox);
        }
        
        content.getChildren().addAll(infoVBox, optionVBox);

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
