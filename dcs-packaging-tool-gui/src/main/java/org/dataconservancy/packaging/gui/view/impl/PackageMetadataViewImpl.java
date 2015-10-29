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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
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
import org.dataconservancy.packaging.gui.util.WarningPopup;
import org.dataconservancy.packaging.gui.view.PackageMetadataView;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
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
    private VBox requiredVBox;
    private VBox bottomContent;

    private Labels labels;
    private List<Node> allFields;
    private WarningPopup warningPopup;
    private boolean formAlreadyDrawn = false;
    private FileChooser packageMetadataFileChooser;

    public PackageMetadataViewImpl(Labels labels) {
        super(labels);
        this.labels = labels;

        allFields = new ArrayList<>();

        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        content = new VBox();

        warningPopup = new WarningPopup(labels);

        packageMetadataFileChooser = new FileChooser();

        //Set up the text for the controls in the footer.
        getContinueButton().setText(labels.get(LabelKey.SAVE_AND_CONTINUE_BUTTON));
        getCancelLink().setText(labels.get(LabelKey.BACK_LINK));
        getSaveButton().setText(labels.get(LabelKey.SAVE_BUTTON));
        getSaveButton().setVisible(true);

        content.getStyleClass().add(PACKAGE_GENERATION_VIEW_CLASS);
        contentScrollPane.setContent(content);
        setCenter(contentScrollPane);

        //Create a label to show any status messages at the top of the screen.
        HBox status = new HBox();
        statusLabel = new Label();
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setTextFill(Color.web("#C00000"));
        status.getChildren().add(statusLabel);
        status.setAlignment(Pos.TOP_CENTER);

        content.getChildren().add(status);

        requiredVBox = new VBox(5);

        VBox requiredLabelVBox = createSectionLabel(labels.get(LabelKey.REQUIRED_FIELDS_LABEL));
        requiredVBox.getChildren().add(requiredLabelVBox);

        // setup static fields
        HBox topRow = new HBox(40);

        VBox packageNameEntryFields = new VBox(4);
        packageNameEntryFields.setAlignment(Pos.TOP_LEFT);

        Label packageNameLabel = new Label(labels.get(LabelKey.PACKAGE_NAME_LABEL));
        packageNameEntryFields.getChildren().add(packageNameLabel);

        packageNameField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null, null);
        packageNameEntryFields.getChildren().add(packageNameField);
        packageNameField.setPrefWidth(310);

        topRow.getChildren().add(packageNameEntryFields);

        VBox domainProfileVBox = new VBox(4);

        Label domainProfileLabel = new Label(labels.get(LabelKey.SELECT_DOMAIN_PROFILE_LABEL));
        domainProfileVBox.getChildren().add(domainProfileLabel);

        HBox domainProfileAndButton = new HBox(4);
        domainProfilesComboBox = new ComboBox<>();
        domainProfilesComboBox.setPrefWidth(267);

        addDomainProfileButton = new Button(labels.get(LabelKey.ADD_BUTTON));
        addDomainProfileButton.setPrefHeight(28);
        addDomainProfileButton.getStyleClass().add(CLICKABLE);

        domainProfileRemovableLabelVBox = new VBox(4);
        domainProfileRemovableLabelVBox.getStyleClass().add(VBOX_BORDER);
        domainProfileRemovableLabelVBox.setId(GeneralParameterNames.DOMAIN_PROFILE);

        domainProfileAndButton.getChildren().add(domainProfilesComboBox);
        domainProfileAndButton.getChildren().add(addDomainProfileButton);

        domainProfileVBox.getChildren().add(domainProfileAndButton);
        domainProfileVBox.getChildren().add(domainProfileRemovableLabelVBox);

        topRow.getChildren().add(domainProfileVBox);

        requiredVBox.getChildren().add(topRow);

        content.getChildren().add(requiredVBox);

        bottomContent = new VBox(5);

        content.getChildren().add(bottomContent);

    }

    /**
     * Set image on the provided label, using a String imageKey
     *
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
    }

    @Override
    public VBox getDomainProfileRemovableLabelVBox() {
        return this.domainProfileRemovableLabelVBox;
    }

    @Override
    public void setupRequiredFields(List<PackageMetadata> requiredPackageMetadataList) {
        if (requiredPackageMetadataList != null && !requiredPackageMetadataList.isEmpty()) {

            for (PackageMetadata packageMetadata : requiredPackageMetadataList) {
                VBox fieldContainer = createFieldsView(packageMetadata);
                requiredVBox.getChildren().add(fieldContainer);
            }

            formAlreadyDrawn = true;
        }
    }

    @Override
    public void setupRecommendedFields(List<PackageMetadata> recommendedPackageMetadataList) {
        if (recommendedPackageMetadataList != null && !recommendedPackageMetadataList.isEmpty()) {

            VBox recommendedLabelVBox = createSectionLabel(labels.get(LabelKey.RECOMMENDED_FIELDS_LABEL));
            bottomContent.getChildren().add(recommendedLabelVBox);

            for (PackageMetadata packageMetadata : recommendedPackageMetadataList) {
                VBox fieldContainer = createFieldsView(packageMetadata);
                bottomContent.getChildren().add(fieldContainer);
            }

            formAlreadyDrawn = true;
        }
    }

    @Override
    public void setupOptionalFields(List<PackageMetadata> optionalPackageMetadataList) {
        if (optionalPackageMetadataList != null && !optionalPackageMetadataList.isEmpty()) {

            VBox optionalLabelVBox = createSectionLabel(labels.get(LabelKey.OPTIONAL_FIELDS_LABEL));
            bottomContent.getChildren().add(optionalLabelVBox);

            for (PackageMetadata packageMetadata : optionalPackageMetadataList) {
                VBox fieldContainer = createFieldsView(packageMetadata);
                bottomContent.getChildren().add(fieldContainer);
            }

            formAlreadyDrawn = true;
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
        domainProfilesComboBox.getItems().clear();
        domainProfilesComboBox.setDisable(false);
        addDomainProfileButton.setDisable(false);
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

    @Override
    public void showWarningPopup() {

        if (warningPopup == null) {
            warningPopup = new WarningPopup(labels);
        }

        warningPopup.setTitleText(labels.get(Labels.LabelKey.WARNING_POPUP_TITLE));
        warningPopup.setMoveable(true);
        warningPopup.setMessage(labels.get(Labels.LabelKey.ALL_FIELDS_CLEAR_WARNING_MESSAGE));

        if (getScene() != null && getScene().getWindow() != null) {
            double x = getScene().getWindow().getX() + getScene().getWidth() / 2.0 - 150;
            double y = getScene().getWindow().getY() + getScene().getHeight() / 2.0 - 150;
            warningPopup.setOwner(getScene().getWindow());
            warningPopup.show(x, y);
            warningPopup.hide();

            //Get the content width and height to property center the popup.
            x = getScene().getWindow().getX() + getScene().getWidth() / 2.0 - warningPopup.getWidth() / 2.0;
            y = getScene().getWindow().getY() + getScene().getHeight() / 2.0 - warningPopup.getHeight() / 2.0;
            warningPopup.setOwner(getScene().getWindow());
            warningPopup.show(x, y);
        }


    }

    @Override
    public WarningPopup getWarningPopup() {
        return warningPopup;
    }

    @Override
    public void addDomainProfileLabel(String domainProfile) {
        Label removableLabel = new Label(domainProfile);
        domainProfileRemovableLabelVBox.getChildren().add(removableLabel);
    }

    @Override
    public boolean isFormAlreadyDrawn() {
        return formAlreadyDrawn;
    }

    @Override
    public FileChooser getPackageMetadataFileChooser() {
        return packageMetadataFileChooser;
    }

    /**
     * Helper method that creates the field based on the given package metadata.
     *
     * @param packageMetadata
     * @return container vbox with the field
     */
    private VBox createFieldsView(PackageMetadata packageMetadata) {
        VBox fieldContainer = new VBox(4);

        HBox fieldLabelHbox = new HBox(4);
        Label fieldLabel = new Label(packageMetadata.getName());
        fieldLabel.setPadding(new Insets(3, 0, 0, 0));
        fieldLabelHbox.getChildren().add(fieldLabel);

        if (packageMetadata.getHelpText() != null && !packageMetadata.getHelpText().isEmpty()) {
            ImageView tooltipImage = new ImageView();
            tooltipImage.getStyleClass().add(TOOLTIP_IMAGE);
            Tooltip tooltip = new Tooltip(packageMetadata.getHelpText());
            tooltip.setPrefWidth(350);
            tooltip.setWrapText(true);
            Tooltip.install(tooltipImage, tooltip);
            fieldLabelHbox.getChildren().add(tooltipImage);
        }

        fieldContainer.getChildren().add(fieldLabelHbox);

        if (packageMetadata.isRepeatable()) {
            VBox parentContainer = new VBox();
            parentContainer.getStyleClass().add(VBOX_BORDER);
            parentContainer.setId(packageMetadata.getName());

            TextField textField = (TextField) ControlFactory.createControl(labels.get(LabelKey.TYPE_VALUE_AND_ENTER_PROMPT), packageMetadata.getHelpText(), parentContainer, ControlType.TEXT_FIELD_W_REMOVABLE_LABEL);
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
                textField.setEditable(packageMetadata.isEditable());
                textField.setDisable(!packageMetadata.isEditable());
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

    /**
     * Helper method to createFields helper method to create the HBox for fields that need validation.
     *
     * @param textField
     * @param validationType
     * @return container hbox with the validation
     */
    private HBox createHBoxForType(TextField textField, PackageMetadata.ValidationType validationType) {
        HBox horizontalBox = new HBox();
        Label inputVerificationLabel = new Label();
        inputVerificationLabel.setPadding(new Insets(3, 0, 0, 3));
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

    /**
     * Helper method that creates labels with tooltips for a given text.
     *
     * @param text
     * @return container vbox
     */
    private VBox createSectionLabel(String text) {
        VBox labelVBox = new VBox();
        Label label = new Label(text);
        label.getStyleClass().add(FORM_FIELDS_DIVISION_CLASS);
        labelVBox.getChildren().add(label);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        labelVBox.getChildren().add(separator);
        return labelVBox;
    }

    /**
     * Listener to validate phone number.
     *
     * @param errorMessageLabel
     * @return ChangeListener
     */
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

    /**
     * Listener to validate email.
     *
     * @param errorMessageLabel
     * @return ChangeListener
     */
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

    /**
     * Listener to validate URL.
     *
     * @param errorMessageLabel
     * @return ChangeListener
     */
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