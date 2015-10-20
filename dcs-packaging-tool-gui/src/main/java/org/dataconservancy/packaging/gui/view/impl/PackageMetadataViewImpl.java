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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.dataconservancy.packaging.gui.Help.HelpKey;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.util.ControlFactory;
import org.dataconservancy.packaging.gui.util.ControlType;
import org.dataconservancy.packaging.gui.util.PhoneNumberValidator;
import org.dataconservancy.packaging.gui.util.RemovableLabel;
import org.dataconservancy.packaging.gui.view.PackageMetadataView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the view that displays the controls for package metadata.
 */
public class PackageMetadataViewImpl extends BaseViewImpl<PackageMetadataPresenter> implements PackageMetadataView {

    //Controls for setting the package name and output directory
    private TextField packageNameField;

    //The value of the combobox domain profiles
    private ComboBox<String> domainProfilesComboBox;
    private Button addDomainProfileButton;
    private VBox domainProfileRemovableLabelVBox;
    private List<String> domainProfileLabelsList = new ArrayList<>();

    private Labels labels;

    //Contact information fields;
    private TextField contactEmailTextField;
    private TextField contactNameTextField;
    private TextField contactPhoneTextField;

    //Package detail fields
    private TextField keywordTextField;
    private List<String> keywordsList = new ArrayList<>();
    private VBox keywordsRemovalLabelVBox;
    private TextField externalIdentifierTextField;
    private TextArea externalDescriptionTextArea;
    private TextField internalSenderIdentifierTextField;
    private List<String> internalSenderIdentifiersList = new ArrayList<>();
    private VBox internalSenderIdsVBox;
    private TextArea internalSenderDescriptionTextArea;
    private TextField sourceOrganizationTextField;
    private TextField organizationAddressTextField;
    private TextField bagCountTextField;
    private TextField bagGroupIdentifierTextField;
    private TextField rightsTextField;
    private TextField rightsUriTextField;
    private VBox rightsUriVBox;
    private List<String> rightsUriList = new ArrayList<>();
    private DatePicker baggingDateDatePicker;
    private TextField bagSizeTextField;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ScrollPane contentScrollPane;
    private VBox bottomContent;

    public PackageMetadataViewImpl(Labels labels) {
        super(labels);
        this.labels = labels;

        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        VBox content = new VBox();
        VBox topContent = new VBox();
        bottomContent = new VBox();
        bottomContent.setVisible(false);
        content.getChildren().add(topContent);
        content.getChildren().add(bottomContent);

        //Set up the text for the controls in the footer.
        getContinueButton().setText(labels.get(LabelKey.SAVE_AND_CONTINUE_BUTTON));
        getCancelLink().setText(labels.get(LabelKey.BACK_LINK));

        topContent.getStyleClass().add(PACKAGE_GENERATION_VIEW_CLASS);
        bottomContent.getStyleClass().add(PACKAGE_GENERATION_VIEW_CLASS);
        contentScrollPane.setContent(content);
        setCenter(contentScrollPane);

        Label requiredLabel = new Label(labels.get(LabelKey.REQUIRED_FIELDS_LABEL));
        topContent.getChildren().add(requiredLabel);

        HBox topRow = new HBox(40);

        // Sets up the controls and label for the package name
        VBox packageNameEntryFields = new VBox(4);
        packageNameEntryFields.setAlignment(Pos.TOP_LEFT);

        Label packageNameLabel = new Label(labels.get(LabelKey.PACKAGE_NAME_LABEL) + "*");
        packageNameEntryFields.getChildren().add(packageNameLabel);

        packageNameField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
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

        domainProfileAndButton.getChildren().add(domainProfilesComboBox);
        domainProfileAndButton.getChildren().add(addDomainProfileButton);

        domainProfileVBox.getChildren().add(domainProfileAndButton);
        domainProfileVBox.getChildren().add(domainProfileRemovableLabelVBox);

        topRow.getChildren().add(domainProfileVBox);

        topContent.getChildren().add(topRow);

        HBox secondRow = new HBox(40);

        VBox secondRowLeftColumn = new VBox(10);
        Label contactLabel = new Label(labels.get(LabelKey.PACKAGE_METADATA));
        contactLabel.getStyleClass().add(SECTION_LABEL);
        secondRowLeftColumn.getChildren().add(contactLabel);

        VBox nameEntryFields = new VBox(4);
        nameEntryFields.setAlignment(Pos.TOP_LEFT);

        Label nameLabel = new Label(labels.get(LabelKey.NAME_LABEL) + "*");
        nameEntryFields.getChildren().add(nameLabel);

        contactNameTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        nameEntryFields.getChildren().add(contactNameTextField);

        VBox emailEntryFields = new VBox(4);
        emailEntryFields.setAlignment(Pos.TOP_LEFT);

        Label emailLabel = new Label(labels.get(LabelKey.EMAIL_LABEL) + "*");
        emailEntryFields.getChildren().add(emailLabel);

        contactEmailTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        emailEntryFields.getChildren().add(contactEmailTextField);

        VBox phoneEntryFields = new VBox(4);
        phoneEntryFields.setAlignment(Pos.TOP_LEFT);

        Label phoneLabel = new Label(labels.get(LabelKey.PHONE_LABEL) + "*");
        phoneEntryFields.getChildren().add(phoneLabel);

        contactPhoneTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        Label inputVerificationLabel = new Label();
        contactPhoneTextField.textProperty().addListener(getNewChangeListenerForPhoneNumber(inputVerificationLabel));
        phoneEntryFields.getChildren().add(contactPhoneTextField);

        HBox inputVerificationBox = new HBox(3);
        inputVerificationBox.getChildren().add(contactPhoneTextField);
        inputVerificationBox.getChildren().add(inputVerificationLabel);
        phoneEntryFields.getChildren().add(inputVerificationBox);

        //position the contact fields
        secondRowLeftColumn.getChildren().add(nameEntryFields);
        secondRowLeftColumn.getChildren().add(emailEntryFields);
        secondRowLeftColumn.getChildren().add(phoneEntryFields);

        secondRow.getChildren().add(secondRowLeftColumn);

        VBox secondRowRightColumn = new VBox(10);

        VBox keywordsVBox = new VBox(4);
        VBox.setMargin(keywordsVBox, new Insets(29, 0, 0, 0));
        Label keywordLabel = new Label(labels.get(LabelKey.KEYWORD_LABEL));
        keywordTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        keywordTextField.setPromptText("Insert keyword and press enter to add");
        keywordsRemovalLabelVBox = new VBox(4);
        keywordsRemovalLabelVBox.getStyleClass().add(VBOX_BORDER);
        keywordsVBox.getChildren().add(keywordLabel);
        keywordsVBox.getChildren().add(keywordTextField);
        keywordsVBox.getChildren().add(keywordsRemovalLabelVBox);

        secondRowRightColumn.getChildren().add(keywordsVBox);

        secondRow.getChildren().add(secondRowRightColumn);

        bottomContent.getChildren().add(secondRow);

        VBox bottomVBox = new VBox(4);
        bottomVBox.setAlignment(Pos.CENTER_LEFT);

        Label externalIdentifierLabel = new Label(labels.get(LabelKey.EXTERNAL_IDENTIFIER_LABEL_KEY));
        bottomVBox.getChildren().add(externalIdentifierLabel);
        externalIdentifierTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        bottomVBox.getChildren().add(externalIdentifierTextField);

        Label externalDescriptionLabel = new Label(labels.get(LabelKey.EXTERNAL_DESCRIPTION_LABEL));
        bottomVBox.getChildren().add(externalDescriptionLabel);
        externalDescriptionTextArea = (TextArea) ControlFactory.createControl(ControlType.TEXT_AREA, null);
        bottomVBox.getChildren().add(externalDescriptionTextArea);

        Label internalSenderIdentifierLabel = new Label(labels.get(LabelKey.INTERNAL_SENDER_IDENTIFIER_LABEL));
        bottomVBox.getChildren().add(internalSenderIdentifierLabel);
        internalSenderIdentifierTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        internalSenderIdentifierTextField.setPromptText("Insert ID and press enter to add");
        bottomVBox.getChildren().add(internalSenderIdentifierTextField);
        internalSenderIdsVBox = new VBox(4);
        internalSenderIdsVBox.getStyleClass().add(VBOX_BORDER);
        bottomVBox.getChildren().add(internalSenderIdsVBox);

        Label internalDescriptionLabel = new Label(labels.get(LabelKey.INTERNAL_DESCRIPTION_LABEL));
        bottomVBox.getChildren().add(internalDescriptionLabel);
        internalSenderDescriptionTextArea = (TextArea) ControlFactory.createControl(ControlType.TEXT_AREA, null);
        bottomVBox.getChildren().add(internalSenderDescriptionTextArea);

        Label sourceOrganizationLabel = new Label(labels.get(LabelKey.SOURCE_ORGANIZATION_LABEL));
        bottomVBox.getChildren().add(sourceOrganizationLabel);
        sourceOrganizationTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        bottomVBox.getChildren().add(sourceOrganizationTextField);

        Label organizationAddressLabel = new Label(labels.get(LabelKey.ORGANIZATION_ADDRESS_LABEL));
        bottomVBox.getChildren().add(organizationAddressLabel);
        organizationAddressTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        bottomVBox.getChildren().add(organizationAddressTextField);

        Label bagCountLabel = new Label(labels.get(LabelKey.BAG_COUNT_LABEL) + "*");
        bottomVBox.getChildren().add(bagCountLabel);
        bagCountTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        bottomVBox.getChildren().add(bagCountTextField);

        Label bagGroupIdentifierLabel = new Label(labels.get(LabelKey.BAG_GROUP_IDENTIFIER_LABEL) + "*");
        bottomVBox.getChildren().add(bagGroupIdentifierLabel);
        bagGroupIdentifierTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        bottomVBox.getChildren().add(bagGroupIdentifierTextField);

        Label rightsLabel = new Label(labels.get(LabelKey.RIGHTS_LABEL));
        bottomVBox.getChildren().add(rightsLabel);
        rightsTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        bottomVBox.getChildren().add(rightsTextField);

        Label rightsUriLabel = new Label(labels.get(LabelKey.RIGHTS_URI_LABEL));
        bottomVBox.getChildren().add(rightsUriLabel);
        rightsUriTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        rightsUriTextField.setPromptText("Insert URI and press enter to add");
        bottomVBox.getChildren().add(rightsUriTextField);
        rightsUriVBox = new VBox(4);
        rightsUriVBox.getStyleClass().add(VBOX_BORDER);
        bottomVBox.getChildren().add(rightsUriVBox);

        Label bagSizeLabel = new Label(labels.get(LabelKey.BAG_SIZE_LABEL) + "*");
        bottomVBox.getChildren().add(bagSizeLabel);
        bagSizeTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        bottomVBox.getChildren().add(bagSizeTextField);

        Label baggingDateLabel = new Label(labels.get(LabelKey.BAGGING_DATE_LABEL) + "*");
        bottomVBox.getChildren().add(baggingDateLabel);
        baggingDateDatePicker = new DatePicker();
        baggingDateDatePicker.setEditable(false);
        baggingDateDatePicker.setPromptText("Select Date ->");
        bottomVBox.getChildren().add(baggingDateDatePicker);

        bottomContent.getChildren().add(bottomVBox);

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
    public TextField getContactEmailTextField() {
        return contactEmailTextField;
    }

    @Override
    public TextField getContactNameTextField() {
        return contactNameTextField;
    }

    @Override
    public TextField getContactPhoneTextField() {
        return contactPhoneTextField;
    }

    @Override
    public TextField getKeywordTextField() {
        return keywordTextField;
    }

    @Override
    public List<String> getKeywordsList() {
        return keywordsList;
    }

    @Override
    public TextField getExternalIdentifierTextField() {
        return externalIdentifierTextField;
    }

    @Override
    public TextArea getExternalDescriptionTextArea() {
        return externalDescriptionTextArea;
    }

    @Override
    public TextField getInternalSenderIdentifierTextField() {
        return internalSenderIdentifierTextField;
    }

    @Override
    public List<String> getInternalIdentifiersList() {
        return internalSenderIdentifiersList;
    }

    @Override
    public TextArea getInternalSenderDescriptionTextArea() {
        return internalSenderDescriptionTextArea;
    }

    @Override
    public TextField getSourceOrganizationTextField() {
        return sourceOrganizationTextField;
    }

    @Override
    public TextField getOrganizationAddressTextField() {
        return organizationAddressTextField;
    }

    @Override
    public TextField getBagCountTextField() {
        return bagCountTextField;
    }

    @Override
    public TextField getBagGroupIdentifierTextField() {
        return bagGroupIdentifierTextField;
    }

    @Override
    public TextField getRightsTextField() {
        return rightsTextField;
    }

    @Override
    public TextField getRightsUriTextField() {
        return rightsUriTextField;
    }

    @Override
    public List<String> getRightsUriList() {
        return rightsUriList;
    }

    @Override
    public DatePicker getBaggingDateDatePicker() {
        return baggingDateDatePicker;
    }

    @Override
    public TextField getBagSizeTextField() {
        return bagSizeTextField;
    }

    @Override
    public List<String> getDomainProfileLabelsList() {
        return this.domainProfileLabelsList;
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
        RemovableLabel removableLabel = new RemovableLabel(domainProfileName, domainProfileLabelsList, domainProfileRemovableLabelVBox);
        domainProfileLabelsList.add(domainProfileName);
        domainProfileRemovableLabelVBox.getChildren().add(removableLabel);
    }

    @Override
    public void addKeywordRemovableLabel(String keyword) {
        RemovableLabel removableLabel = new RemovableLabel(keyword, keywordsList, keywordsRemovalLabelVBox);
        keywordsList.add(keyword);
        keywordsRemovalLabelVBox.getChildren().add(removableLabel);
    }

    @Override
    public void addInternalSenderIdentifierRemovableLabel(String internalSenderIdentifier) {
        RemovableLabel removableLabel = new RemovableLabel(internalSenderIdentifier, internalSenderIdentifiersList, internalSenderIdsVBox);
        internalSenderIdentifiersList.add(internalSenderIdentifier);
        internalSenderIdsVBox.getChildren().add(removableLabel);
    }

    @Override
    public void addRightsUriRemovableLabel(String rightsUri) {
        RemovableLabel removableLabel = new RemovableLabel(rightsUri, rightsUriList, rightsUriVBox);
        rightsUriList.add(rightsUri);
        rightsUriVBox.getChildren().add(removableLabel);
    }

    @Override
    public void showBottomContent(boolean show) {
        bottomContent.setVisible(show);
    }

    @Override
    public void clearAllFields() {
        packageNameField.clear();
        domainProfileRemovableLabelVBox.getChildren().clear();
        domainProfileLabelsList.clear();
        contactEmailTextField.clear();
        contactNameTextField.clear();
        contactPhoneTextField.clear();
        keywordTextField.clear();
        keywordsList.clear();
        keywordsRemovalLabelVBox.getChildren().clear();
        externalIdentifierTextField.clear();
        externalDescriptionTextArea.clear();
        internalSenderIdentifierTextField.clear();
        internalSenderIdentifiersList.clear();
        internalSenderIdsVBox.getChildren().clear();
        internalSenderDescriptionTextArea.clear();
        sourceOrganizationTextField.clear();
        organizationAddressTextField.clear();
        bagCountTextField.clear();
        bagGroupIdentifierTextField.clear();
        rightsTextField.clear();
        rightsUriTextField.clear();
        rightsUriVBox.getChildren().clear();
        rightsUriList.clear();
        bagSizeTextField.clear();
    }

}