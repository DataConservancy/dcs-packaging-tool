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

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.CreateNewPackagePresenter;
import org.dataconservancy.packaging.gui.util.ControlFactory;
import org.dataconservancy.packaging.gui.util.ControlType;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.view.CreateNewPackageView;

/**
 * Implementation for the view that controls how the user will create a package either by setting a content directory, or,
 * by loading a package description file. 
 */
public class CreateNewPackageViewImpl extends BaseViewImpl<CreateNewPackagePresenter> implements
        CreateNewPackageView {
    
    private Button chooseBaseDirectoryButton;
    private TextField currentBaseDirectory;
    
    private ProgressDialogPopup progressIndicatorPopUp;
    private VBox content;

    public CreateNewPackageViewImpl() {
        super();

        getContinueButton().setText(TextFactory.getText(LabelKey.CONTINUE_BUTTON));
        getCancelLink().setText(TextFactory.getText(LabelKey.BACK_LINK));

        content = new VBox();
        content.setSpacing(32);
        content.getStyleClass().add(CREATE_NEW_PACKAGE_CLASS);

        setCenter(content);
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().add(errorTextArea);

        //Create a vbox that will display the options for generating a package, either selecting a base directory or
        //choosing an existing package description. 
        VBox packageSelectionFields = new VBox(12);
        packageSelectionFields.setAlignment(Pos.TOP_LEFT);
        
        //Create the controls for choosing a base directory to generate a pacakge from.
        VBox baseDirectorySelectionFields = new VBox(4);
        baseDirectorySelectionFields.setAlignment(Pos.TOP_LEFT);

        HBox baseDirectoryLabelAndTooltip = new HBox(4);
        Label chooseBaseDirectoryLabel = new Label(TextFactory.getText(LabelKey.BASE_DIRECTORY_LABEL));
        ImageView tooltipImage = new ImageView();
        tooltipImage.getStyleClass().add(TOOLTIP_IMAGE);
        Tooltip tooltip = new Tooltip(TextFactory.getText(LabelKey.BASE_DIRECTORY_TOOLTIP));
        tooltip.setPrefWidth(350);
        tooltip.setWrapText(true);
        Tooltip.install(tooltipImage, tooltip);
        baseDirectoryLabelAndTooltip.getChildren().add(chooseBaseDirectoryLabel);
        baseDirectoryLabelAndTooltip.getChildren().add(tooltipImage);

        baseDirectorySelectionFields.getChildren().add(baseDirectoryLabelAndTooltip);
        
        HBox baseDirectorySelector = new HBox(6);
        baseDirectorySelector.getStyleClass().add(DIRECTORY_BOX);
        baseDirectorySelector.setPrefWidth(ControlFactory.textPrefWidth);
        
        chooseBaseDirectoryButton = new Button(TextFactory.getText(LabelKey.BROWSE_BUTTON));
        chooseBaseDirectoryButton.setMinWidth(80);
        baseDirectorySelector.getChildren().add(chooseBaseDirectoryButton);
        
        //currentBaseDirectory = new TextField();
        currentBaseDirectory = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, "", "");
        currentBaseDirectory.setEditable(false);
        currentBaseDirectory.getStyleClass().add(INVISBILE_TEXT_FIELD);
        baseDirectorySelector.getChildren().add(currentBaseDirectory);
        baseDirectorySelectionFields.getChildren().add(baseDirectorySelector);
       
        packageSelectionFields.getChildren().add(baseDirectorySelectionFields);   

        content.getChildren().add(packageSelectionFields);
    }

    @Override
    public Button getChooseContentDirectoryButton() {
        return chooseBaseDirectoryButton;
    }

    @Override
    public TextField getChooseContentDirectoryTextField() {
        return currentBaseDirectory;
    }

    @Override
    public String getHelpText() {
        return TextFactory.getText(Help.HelpKey.CREATE_NEW_PACKAGE_HELP);
    }

    @Override
    public void showProgressIndicatorPopUp() {

        if (progressIndicatorPopUp == null) {
            progressIndicatorPopUp = new ProgressDialogPopup(true);
        }

        progressIndicatorPopUp.setTitleText(TextFactory.getText(LabelKey.PROGRESS_INDICATOR));
        progressIndicatorPopUp.setMoveable(true);
        progressIndicatorPopUp.setMessage(TextFactory.getText(LabelKey.BUILDING_PACKAGE_TREE));

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
