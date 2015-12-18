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

import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.impl.OpenExistingPackagePresenterImpl;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.view.OpenExistingPackageView;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OpenExistingPackageViewImpl extends BaseViewImpl<OpenExistingPackagePresenterImpl> implements OpenExistingPackageView {
    private Button chooseExplodedPackageDirectoryButton;
    private TextField chooseExplodedPackageDirectoryTextField;
    private Button choosePackageFileButton;
    private TextField choosePackageFileTextField;
    private Button choosePackageStagingDirectoryButton;
    private TextField choosePackageStagingDirectoryTextField;
    private ProgressDialogPopup progressDialogPopup;

    public OpenExistingPackageViewImpl() {
        super();

        getContinueButton().setText(TextFactory.getText(Labels.LabelKey.CONTINUE_BUTTON));
        getCancelLink().setText(TextFactory.getText(Labels.LabelKey.BACK_LINK));

        VBox content = new VBox();
        content.setSpacing(32);
        content.getStyleClass().add(CREATE_NEW_PACKAGE_CLASS);

        setCenter(content);
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().add(errorTextArea);
             
        VBox infoVBox = new VBox(4);
        Label selectOneOptionLabel = new Label(TextFactory.getText(Labels.LabelKey.SELECT_ONE_OPTION_LABEL));
        selectOneOptionLabel.getStyleClass().add(FORM_FIELDS_DIVISION_CLASS);
        infoVBox.getChildren().addAll(selectOneOptionLabel, new Separator(Orientation.HORIZONTAL));
        
        VBox optionVBox = new VBox(80);
        optionVBox.setAlignment(Pos.TOP_LEFT);

        
        // Choose a package or state file and extraction directory
        {
            VBox openPackageVBox = new VBox(12);
            VBox packageVBox = new VBox(4);

            HBox package_hbox = new HBox(6);
            package_hbox.getStyleClass().add(DIRECTORY_BOX);
            package_hbox.setMaxWidth(420);

            VBox extractVBox = new VBox(4);
            HBox extract_hbox = new HBox(6);
            extract_hbox.getStyleClass().add(DIRECTORY_BOX);
            extract_hbox.setMaxWidth(420);

            HBox packageLabelAndTooltip = new HBox(4);
            Label package_label = new Label(TextFactory.getText(Labels.LabelKey.SELECT_PACKAGE_FILE_LABEL));
            packageLabelAndTooltip.getChildren().add(package_label);

            ImageView packageTooltipImage = new ImageView();
            packageTooltipImage.getStyleClass().add(TOOLTIP_IMAGE);
            Tooltip packageTooltip = new Tooltip(TextFactory.getText(Labels.LabelKey.PACKAGE_SELECTION_TOOLTIP));
            packageTooltip.setPrefWidth(350);
            packageTooltip.setWrapText(true);
            Tooltip.install(packageTooltipImage, packageTooltip);
            packageLabelAndTooltip.getChildren().add(packageTooltipImage);

            packageVBox.getChildren().addAll(packageLabelAndTooltip, package_hbox);

            HBox extractLabelAndTooltip = new HBox(4);
            Label extract_label = new Label(TextFactory.getText(Labels.LabelKey.SELECT_STAGING_DIRECTORY));

            ImageView extractTooltipImage = new ImageView();
            extractTooltipImage.getStyleClass().add(TOOLTIP_IMAGE);
            Tooltip extractDirectoryTooltip = new Tooltip(TextFactory.getText(Labels.LabelKey.EXTRACT_DIRECTORY_TOOLTIP));
            extractDirectoryTooltip.setPrefWidth(350);
            extractDirectoryTooltip.setWrapText(true);
            Tooltip.install(extractTooltipImage, extractDirectoryTooltip);

            extractLabelAndTooltip.getChildren().addAll(extract_label, extractTooltipImage);
            extractVBox.getChildren().addAll(extractLabelAndTooltip, extract_hbox);

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
            choosePackageStagingDirectoryButton.disableProperty().bind(choosePackageFileTextField.textProperty().isEmpty());
            choosePackageStagingDirectoryTextField.disableProperty().bind(choosePackageFileTextField.textProperty().isEmpty());

            package_hbox.getChildren().addAll(choosePackageFileButton, choosePackageFileTextField);
            extract_hbox.getChildren().addAll(choosePackageStagingDirectoryButton, choosePackageStagingDirectoryTextField);
            
            openPackageVBox.getChildren().addAll(packageVBox, extractVBox);
            optionVBox.getChildren().add(openPackageVBox);
            
            // Indent from the left
            //VBox.setMargin(package_hbox, new Insets(0, 0, 0, 20));
            //VBox.setMargin(extract_hbox, new Insets(0, 0, 0, 20));
            //VBox.setMargin(extract_label, new Insets(0, 0, 0, 20));
        }
        
        // Choose a exploded package
        {
            VBox explodedPackageVBox = new VBox(4);

            HBox explodedLabelAndTooltip = new HBox(4);
            Label label = new Label(TextFactory.getText(Labels.LabelKey.PACKAGE_DIRECTORY_LABEL));

            ImageView explodedTooltipImage = new ImageView();
            explodedTooltipImage.getStyleClass().add(TOOLTIP_IMAGE);
            Tooltip explodedTooltip = new Tooltip(TextFactory.getText(Labels.LabelKey.EXPLODED_PACKAGE_TOOLTIP));
            explodedTooltip.setPrefWidth(350);
            explodedTooltip.setWrapText(true);
            Tooltip.install(explodedTooltipImage, explodedTooltip);

            explodedLabelAndTooltip.getChildren().addAll(label, explodedTooltipImage);
            explodedPackageVBox.getChildren().add(explodedLabelAndTooltip);

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
            explodedPackageVBox.getChildren().add(hbox);
            optionVBox.getChildren().add(explodedPackageVBox);
        }
        
        content.getChildren().addAll(infoVBox, optionVBox);
    }

    @Override
    public ProgressDialogPopup getProgressPopup() {
        if (progressDialogPopup == null) {
            progressDialogPopup = new ProgressDialogPopup(false);
            progressDialogPopup.setTitleText(TextFactory.getText(Labels.LabelKey.LOADING_PACKAGE_LABEL));

            if (getScene() != null && getScene().getWindow() != null) {
                double x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - 150;
                double y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - 150;
                progressDialogPopup.setOwner(getScene().getWindow());
                progressDialogPopup.show(x, y);
                progressDialogPopup.hide();

                //Get the content width and height to property center the popup.
                x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - progressDialogPopup.getWidth()/2.0;
                y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - progressDialogPopup.getHeight()/2.0;
                progressDialogPopup.setOwner(getScene().getWindow());
                progressDialogPopup.show(x, y);
            }
        }

        return progressDialogPopup;
    }

    @Override
    public String getHelpText() {
        return TextFactory.getText(Help.HelpKey.OPEN_EXISTING_PACKAGE);
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
