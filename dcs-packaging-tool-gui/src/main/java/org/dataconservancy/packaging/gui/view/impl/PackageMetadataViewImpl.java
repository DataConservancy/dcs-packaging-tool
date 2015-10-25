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

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.packaging.gui.Help.HelpKey;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.util.ControlFactory;
import org.dataconservancy.packaging.gui.util.ControlType;
import org.dataconservancy.packaging.gui.util.EmailValidator;
import org.dataconservancy.packaging.gui.util.PhoneNumberValidator;
import org.dataconservancy.packaging.gui.util.RemovableLabel;
import org.dataconservancy.packaging.gui.view.PackageMetadataView;
import org.dataconservancy.packaging.tool.impl.support.UrlPropertyValidator;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the view that displays the controls for package metadata.
 */
public class PackageMetadataViewImpl extends BaseViewImpl<PackageMetadataPresenter> implements PackageMetadataView {

    private static final Logger LOG = LoggerFactory.getLogger(PackageMetadataViewImpl.class);

    //Controls for setting the package name and output directory
    private TextField packageNameField;

    //The value of the combobox domain profiles
    private ComboBox<String> domainProfilesComboBox;
    private Button addDomainProfileButton;
    private VBox domainProfileRemovableLabelVBox;

    private Label statusLabel;
    private ScrollPane contentScrollPane;
    private VBox content;

    private Labels labels;
    private List<Node> allFields;

    public PackageMetadataViewImpl(Labels labels) {
        super(labels);
        this.labels = labels;

        allFields = new ArrayList<>();

        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        content = new VBox();

        //Set up the text for the controls in the footer.
        getContinueButton().setText(labels.get(LabelKey.SAVE_AND_CONTINUE_BUTTON));
        getCancelLink().setText(labels.get(LabelKey.BACK_LINK));

        content.getStyleClass().add(PACKAGE_GENERATION_VIEW_CLASS);
        contentScrollPane.setContent(content);
        setCenter(contentScrollPane);

        //Create a label to show any status messages at the top of the screen.
        HBox status = new HBox();
        statusLabel = new Label();
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        //statusLabel.setMaxWidth(600);
        status.getChildren().add(statusLabel);
        status.setAlignment(Pos.TOP_CENTER);

        content.getChildren().add(status);

        Label requiredLabel = new Label(labels.get(LabelKey.REQUIRED_FIELDS_LABEL));
        content.getChildren().add(requiredLabel);

    }

    /*
     * Set image on the provided label, using a String imageKey
     * @param label
     * @param imageKey
     */
    private void setLabelImage(Label label, String imageKey) {
        if (imageKey != null) {
            ImageView image = new ImageView();
            image.getStyleClass().add(imageKey);
            label.setGraphic(image);
        } else {
            label.setGraphic(null);
        }
    }

    @Override
    public TextField getPackageNameField() {
        return packageNameField;
    }

    @Override
    public ComboBox<String> getDomainProfilesComboBox() {
        return domainProfilesComboBox;
    }

    @Override
    public Button getAddDomainProfileButton() {
        return addDomainProfileButton;
    }

    @Override
    public void scrollToTop() {
        contentScrollPane.setVvalue(0);
    }

    @Override
    public void setupHelp() {
        Label helpText = new Label(help.get(HelpKey.PACKAGE_METADATA_HELP));
        helpText.setMaxWidth(300);
        helpText.setWrapText(true);
        helpText.setTextAlignment(TextAlignment.CENTER);
        setHelpPopupContent(helpText);
    }

    @Override
    public void loadDomainProfileNames(List<String> profileNames) {
        domainProfilesComboBox.getItems().addAll(profileNames);
    }

    @Override
    public void addDomainProfileRemovableLabel(String domainProfileName) {
        RemovableLabel removableLabel = new RemovableLabel(domainProfileName, domainProfileRemovableLabelVBox);
        domainProfileRemovableLabelVBox.getChildren().add(removableLabel);
        allFields.add(removableLabel);
    }

    @Override
    public void setupStaticFields() {

        HBox topRow = new HBox(40);

        VBox packageNameEntryFields = new VBox(4);
        packageNameEntryFields.setAlignment(Pos.TOP_LEFT);

        Label packageNameLabel = new Label(labels.get(LabelKey.PACKAGE_NAME_LABEL) + "*");
        packageNameEntryFields.getChildren().add(packageNameLabel);

        packageNameField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null, null);
        packageNameEntryFields.getChildren().add(packageNameField);
        packageNameField.setPrefWidth(310);

        topRow.getChildren().add(packageNameEntryFields);

        VBox domainProfileVBox = new VBox(4);

        Label domainProfileLabel = new Label(labels.get(LabelKey.SELECT_DOMAIN_PROFILE_LABEL) + "*");
        domainProfileVBox.getChildren().add(domainProfileLabel);

        HBox domainProfileAndButton = new HBox(4);
        domainProfilesComboBox = new ComboBox<>();
        domainProfilesComboBox.setPrefWidth(260);

        addDomainProfileButton = new Button(labels.get(LabelKey.ADD_BUTTON));
        addDomainProfileButton.setPrefHeight(28);
        addDomainProfileButton.getStyleClass().add(CLICKABLE);

        domainProfileRemovableLabelVBox = new VBox(4);
        domainProfileRemovableLabelVBox.getStyleClass().add(VBOX_BORDER);
        domainProfileRemovableLabelVBox.setId("Domain-Profile");
        allFields.add(domainProfileRemovableLabelVBox);

        domainProfileAndButton.getChildren().add(domainProfilesComboBox);
        domainProfileAndButton.getChildren().add(addDomainProfileButton);

        domainProfileVBox.getChildren().add(domainProfileAndButton);
        domainProfileVBox.getChildren().add(domainProfileRemovableLabelVBox);

        topRow.getChildren().add(domainProfileVBox);

        content.getChildren().add(topRow);
    }

    @Override
    public void setupRecommendedFields(List<PackageMetadata> recommendedPackageMetadataList) {
        for (PackageMetadata packageMetadata : recommendedPackageMetadataList) {
            VBox fieldContainer = createFieldsView(packageMetadata);
            content.getChildren().add(fieldContainer);
        }
    }

    @Override
    public void setupOptionalFields(List<PackageMetadata> optionalPackageMetadataList) {
        for (PackageMetadata packageMetadata : optionalPackageMetadataList) {
            VBox fieldContainer = createFieldsView(packageMetadata);
            content.getChildren().add(fieldContainer);
        }
    }

    @Override
    public void showStatus(String status) {
        statusLabel.setText(status);
        statusLabel.setVisible(true);
    }

    @Override
    public void clearAllFields() {
        packageNameField.clear();
        domainProfileRemovableLabelVBox.getChildren().clear();
        for (Node node : allFields) {
            if (node instanceof TextField) {
                ((TextField) node).clear();
            }
            if (node instanceof DatePicker) {
                ((DatePicker) node).setValue(null);
            }
            if (node instanceof VBox) {
                ((VBox) node).getChildren().clear();
            }
        }
    }

    @Override
    public List<Node> getAllFields() {
        return this.allFields;
    }

    private VBox createFieldsView(PackageMetadata packageMetadata) {
        VBox fieldContainer = new VBox(4);
        Label fieldLabel = new Label(packageMetadata.getName());
        fieldContainer.getChildren().add(fieldLabel);

        if (packageMetadata.isRepeatable()) {
            VBox parentContainer = new VBox();
            parentContainer.getStyleClass().add(VBOX_BORDER);
            parentContainer.setId(packageMetadata.getName());

            TextField textField = (TextField) ControlFactory.createControl("Type value and press enter to add", packageMetadata.getHelpText(), parentContainer, ControlType.TEXT_FIELD_W_REMOVABLE_LABEL);
            allFields.add(textField);

            if (packageMetadata.getValidationType().equals(PackageMetadata.ValidationType.URL)) {
                // TODO: this may have to be done via a button
                HBox horizontalBox = createHBoxForType(textField, PackageMetadata.ValidationType.URL);
                fieldContainer.getChildren().add(horizontalBox);
            }

            fieldContainer.getChildren().add(textField);
            fieldContainer.getChildren().add(parentContainer);

        } else {

            if (packageMetadata.getValidationType().equals(PackageMetadata.ValidationType.DATE)) {
                DatePicker datePicker = (DatePicker) ControlFactory.createControl(ControlType.DATE_PICKER, null, packageMetadata.getHelpText());
                allFields.add(datePicker);
                fieldContainer.getChildren().add(datePicker);
            } else {
                TextField textField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null, packageMetadata.getHelpText());
                textField.setId(packageMetadata.getName());
                allFields.add(textField);

                if (packageMetadata.getValidationType().equals(PackageMetadata.ValidationType.PHONE)) {
                    HBox horizontalBox = createHBoxForType(textField, PackageMetadata.ValidationType.PHONE);
                    fieldContainer.getChildren().add(horizontalBox);
                } else if (packageMetadata.getValidationType().equals(PackageMetadata.ValidationType.EMAIL)) {
                    HBox horizontalBox = createHBoxForType(textField, PackageMetadata.ValidationType.EMAIL);
                    fieldContainer.getChildren().add(horizontalBox);
                } else {
                    fieldContainer.getChildren().add(textField);
                }
            }
        }
        return fieldContainer;
    }

    private HBox createHBoxForType(TextField textField, PackageMetadata.ValidationType validationType) {
        HBox horizontalBox = new HBox(4);
        Label inputVerificationLabel = new Label();
        if (validationType.equals(PackageMetadata.ValidationType.URL)) {
            textField.textProperty().addListener(getNewChangeListenerForUrl(inputVerificationLabel));
        } else if (validationType.equals(PackageMetadata.ValidationType.PHONE)) {
            textField.textProperty().addListener(getNewChangeListenerForPhoneNumber(inputVerificationLabel));
        } else if (validationType.equals(PackageMetadata.ValidationType.EMAIL)) {
            textField.textProperty().addListener(getNewChangeListenerForEmail(inputVerificationLabel));
        }
        horizontalBox.getChildren().add(textField);
        horizontalBox.getChildren().add(inputVerificationLabel);
        return horizontalBox;
    }

    private ChangeListener<String> getNewChangeListenerForPhoneNumber(final Label errorMessageLabel) {
        //Add a listener for text entry in the phone number box
        return (observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                setLabelImage(errorMessageLabel, null);
            } else if (PhoneNumberValidator.isValid(newValue)) {
                setLabelImage(errorMessageLabel, GOOD_INPUT_IMAGE);
            } else {
                setLabelImage(errorMessageLabel, BAD_INPUT_IMAGE);
            }
        };
    }

    private ChangeListener<String> getNewChangeListenerForEmail(final Label errorMessageLabel) {
        return (observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                setLabelImage(errorMessageLabel, null);
            } else if (EmailValidator.isValidEmail(newValue)) {
                setLabelImage(errorMessageLabel, GOOD_INPUT_IMAGE);
            } else {
                setLabelImage(errorMessageLabel, BAD_INPUT_IMAGE);
            }
        };
    }

    private ChangeListener<String> getNewChangeListenerForUrl(final Label errorMessageLabel) {
        return (observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                setLabelImage(errorMessageLabel, null);
            } else if (UriUtility.isHttpUrl(newValue)) {
                setLabelImage(errorMessageLabel, GOOD_INPUT_IMAGE);
            } else {
                setLabelImage(errorMessageLabel, BAD_INPUT_IMAGE);
            }
        };
    }

}