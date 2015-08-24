/*
 * Copyright 2014 Johns Hopkins University
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

import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import org.dataconservancy.packaging.gui.Help.HelpKey;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.presenter.CreateNewPackagePresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.view.CreateNewPackageView;
import org.dataconservancy.packaging.tool.api.support.RulePropertiesManager;

/**
 * Implementation for the view that controls how the user will create a package either by setting a content directory, or,
 * by loading a package description file. 
 */
public class CreateNewPackageViewImpl extends BaseViewImpl<CreateNewPackagePresenter> implements
        CreateNewPackageView {
    
    private Button chooseBaseDirectoryButton;
    private TextField currentBaseDirectory;
    
    private Button choosePackageDescriptionButton;
    private TextField currentPackageDescription;


    private ProgressDialogPopup progressIndicatorPopUp;
    private Label errorMessage;
    private VBox content;

    private Labels labels;
    
    private Map<String, TextField> propertyFields = new HashMap<>();
    
    public CreateNewPackageViewImpl(Labels labels) {
        super(labels); 
        this.labels = labels;
        
        getContinueButton().setText(labels.get(LabelKey.CONTINUE_BUTTON));

        content = new VBox();
        content.setSpacing(32);
        content.getStyleClass().add(CREATE_NEW_PACKAGE_CLASS);

        setCenter(content);
        content.setAlignment(Pos.TOP_CENTER);
        errorMessage = new Label();
        errorMessage.setTextFill(Color.RED);
        errorMessage.setWrapText(true);
        errorMessage.setMaxWidth(600);
        content.getChildren().add(errorMessage);

        //Create a vbox that will display the options for generating a package, either selecting a base directory or
        //choosing an existing package description. 
        VBox packageSelectionFields = new VBox(12);
        packageSelectionFields.setAlignment(Pos.TOP_LEFT);
        
        //Create the controls for choosing a base directory to generate a pacakge from.
        VBox baseDirectorySelectionFields = new VBox(4);
        baseDirectorySelectionFields.setAlignment(Pos.TOP_LEFT);
        
        Label chooseBaseDirectoryLabel = new Label(labels.get(LabelKey.BASE_DIRECTORY_LABEL));
        baseDirectorySelectionFields.getChildren().add(chooseBaseDirectoryLabel);
        
        HBox baseDirectorySelector = new HBox(6);
        baseDirectorySelector.getStyleClass().add(DIRECTORY_BOX);
        baseDirectorySelector.setMaxWidth(420);
        
        chooseBaseDirectoryButton = new Button(labels.get(LabelKey.BROWSE_BUTTON));
        chooseBaseDirectoryButton.setMinWidth(60);
        baseDirectorySelector.getChildren().add(chooseBaseDirectoryButton);
        
        currentBaseDirectory = new TextField();
        currentBaseDirectory.setEditable(false);
        currentBaseDirectory.getStyleClass().add(INVISBILE_TEXT_FIELD);
        currentBaseDirectory.setPrefWidth(320);
        baseDirectorySelector.getChildren().add(currentBaseDirectory);
        baseDirectorySelectionFields.getChildren().add(baseDirectorySelector);
       
        packageSelectionFields.getChildren().add(baseDirectorySelectionFields);   
        
        Label orLabel = new Label(labels.get(LabelKey.OR_LABEL));
        packageSelectionFields.getChildren().add(orLabel);
        
        //Create controls for selecting an existing package description.
        VBox packageDescriptionSelectionFields = new VBox(4);
        packageDescriptionSelectionFields.setAlignment(Pos.TOP_LEFT);
        
        Label choosePackageDescriptionLabel = new Label(labels.get(LabelKey.OPEN_PACKAGE_DESCRIPTION_LABEL_KEY));
        packageDescriptionSelectionFields.getChildren().add(choosePackageDescriptionLabel);
        
        HBox packageDescriptionSelector = new HBox(6);
        packageDescriptionSelector.getStyleClass().add(DIRECTORY_BOX);
        packageDescriptionSelector.setMaxWidth(420);
        
        choosePackageDescriptionButton = new Button(labels.get(LabelKey.BROWSE_BUTTON));
        choosePackageDescriptionButton.setMinWidth(60);
        packageDescriptionSelector.getChildren().add(choosePackageDescriptionButton);
        
        currentPackageDescription = new TextField();
        currentPackageDescription.setMinWidth(100);
        currentPackageDescription.setPrefWidth(320);
        currentPackageDescription.getStyleClass().add(INVISBILE_TEXT_FIELD);
        currentPackageDescription.setEditable(false);
        packageDescriptionSelector.getChildren().add(currentPackageDescription);
        packageDescriptionSelectionFields.getChildren().add(packageDescriptionSelector);
       
        packageSelectionFields.getChildren().add(packageDescriptionSelectionFields);
        
        content.getChildren().add(packageSelectionFields);        
    }

    @Override
    public Button getChooseContentDirectoryButton() {
        return chooseBaseDirectoryButton;
    }

    @Override
    public Label getErrorMessage() {
        return errorMessage;
    }

    @Override
    public TextField getSelectedBaseDirectoryTextField() {
        return currentBaseDirectory;
    }

    @Override
    public Button getChoosePackageDescriptionButton() {
        return choosePackageDescriptionButton;
    }

    @Override
    public TextField getSelectedPackageDescriptionTextField() {
        return currentPackageDescription;
    }

    @Override
    public void setupHelp() {
        Label helpText = new Label(help.get(HelpKey.CREATE_NEW_PACKAGE_HELP));
        helpText.setMaxWidth(300);
        helpText.setWrapText(true);
        helpText.setTextAlignment(TextAlignment.CENTER);
        setHelpPopupContent(helpText);         
    }

    @Override
    public void promptForUndefinedProperties(RulePropertiesManager mgr) {
        /* Add property definition boxes, where appropriate */
        if (!mgr.getAllProperties().isEmpty()) {
            final VBox propertiesBox = new VBox(12);

            propertiesBox.getChildren().add(new Label(labels.get(LabelKey.PROPERTY_INPUT_LABEL)));
            propertiesBox.setAlignment(Pos.TOP_LEFT);
            propertiesBox.getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);

            /*
             * Populate a map of property description (human name) and value (if
             * any)
             */
            for (Map.Entry<String, String> propDesc : mgr.getAllProperties()
                    .entrySet()) {
                propertiesBox
                        .getChildren()
                        .add(createPropertyBox(propDesc.getKey(), propDesc.getValue(),
                                System.getProperty(propDesc.getKey())));
            }

            content.getChildren().add(propertiesBox);
        }
    }
    
    private HBox createPropertyBox(String propertyKey, String propertyName, String propertyValue) {
        HBox propertyBox = new HBox(30);
        propertyBox.setAlignment(Pos.TOP_LEFT);
        Label propertyNameLabel = new Label(propertyName);
        propertyNameLabel.setPrefWidth(100);
        propertyNameLabel.setWrapText(true);
        propertyBox.getChildren().add(propertyNameLabel);
        
        final VBox propertyValuesBox = new VBox(6);
        TextField propertyValueField;
  
        if (propertyValue != null) {
                propertyValueField = new TextField(propertyValue); 
        } else {
            propertyValueField = new TextField();
        }
        propertyValueField.setPrefWidth(250);
        propertyValueField.setEditable(true);
        propertyFields.put(propertyKey, propertyValueField);
        propertyValuesBox.getChildren().add(propertyValueField);
        
        propertyBox.getChildren().add(propertyValuesBox);
        
        return propertyBox;
    }

    @Override
    public Map<String, String> getPropertyValues() {
        
        Map<String, String> propertyValues = new HashMap<>();
        for (Map.Entry<String, TextField> field : propertyFields.entrySet()) {
            String value = field.getValue().getText();
            if (value != null && !value.equals("")) {
                propertyValues.put(field.getKey(), value);
            }
        }
        
        return propertyValues;
    }

    @Override
    public void showProgressIndicatorPopUp() {

        if (progressIndicatorPopUp == null) {
            progressIndicatorPopUp = new ProgressDialogPopup(labels);
        }


        progressIndicatorPopUp.setTitleText(labels.get(LabelKey.PROGRESS_INDICATOR));
        progressIndicatorPopUp.setMoveable(true);
        progressIndicatorPopUp.setMessage(labels.get(LabelKey.BUILDING_PACKAGE_DESCRIPTION));

        //Quickly display the popup so we can measure the content
        //Check for scene == null in the case of unit tests, or massive disaster..
        if (getScene() != null && getScene().getWindow() != null) {
            double x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - 150;
            double y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - 150;
            progressIndicatorPopUp.setOwner(getScene().getWindow());
            progressIndicatorPopUp.show(x, y);
            progressIndicatorPopUp.hide();

            //Get the content width and height to property center the popup.
            x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - progressIndicatorPopUp.getWidth()/2.0;
            y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - progressIndicatorPopUp.getHeight()/2.0;
            progressIndicatorPopUp.setOwner(getScene().getWindow());
            progressIndicatorPopUp.show(x, y);
        }
    }

    @Override
    public PackageToolPopup getProgressIndicatorPopUp() {
        return progressIndicatorPopUp;
    }

}
