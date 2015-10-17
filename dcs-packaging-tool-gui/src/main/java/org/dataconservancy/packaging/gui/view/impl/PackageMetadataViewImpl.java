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
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

import java.util.List;

/**
 * Implementation of the view that displays the controls for package metadata.
 */
public class PackageMetadataViewImpl extends BaseViewImpl<PackageMetadataPresenter> implements PackageMetadataView {

    //Controls for setting the package name and output directory
    private TextField packageNameField;
    private Label statusLabel;

    //The value of the combobox domain profiles
    private ComboBox<String> domainProfilesComboBox;
    private Button addDomainProfileButton;
    private VBox domainProfileFields;

    private Labels labels;

    //Contact information fields;
    private TextField contactEmailTextField;
    private TextField contactNameTextField;
    private TextField contactPhoneTextField;

    //Package detail fields
    private TextField keywordTextField;
    private List<RemovableLabel> keywordRemovableLabels;
    private TextField externalIdentifierTextField;
    private TextArea externalDescriptionTextArea;
    private TextField internalSenderIdentifierTextField;
    private List<RemovableLabel> internalSenderIdentifierRemovableLables;
    private TextArea internalSenderDescriptionTextArea;
    private TextField sourceOrganizationTextField;
    private TextField organizationAddressTextField;
    private TextField bagCountTextField;
    private TextField bagGroupIdentifierTextField;
    private TextField rightsTextField;
    private TextField rightsUriTextField;
    private DatePicker baggingDateDatePicker;
    private TextField bagSizeTextField;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ScrollPane contentScrollPane;

    public PackageMetadataViewImpl(Labels labels) {
        super(labels);
        this.labels = labels;

        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        VBox content = new VBox();

        //Set up the text for the controls in the footer.
        getContinueButton().setText(labels.get(LabelKey.FINISH_BUTTON));
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

        HBox topRow = new HBox(30);

        // Sets up the controls and label for the package name
        VBox packageNameEntryFields = new VBox(4);
        packageNameEntryFields.setAlignment(Pos.TOP_LEFT);

        Label packageNameLabel = new Label(labels.get(LabelKey.PACKAGE_NAME_LABEL) + "*");
        packageNameEntryFields.getChildren().add(packageNameLabel);

        packageNameField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        packageNameField.setPrefWidth(280);
        packageNameEntryFields.getChildren().add(packageNameField);

        topRow.getChildren().add(packageNameEntryFields);

        domainProfileFields = new VBox(4);

        Label domainProfileLabel = new Label(labels.get(LabelKey.SELECT_DOMAIN_PROFILE_LABEL) + "*");
        domainProfileFields.getChildren().add(domainProfileLabel);

        HBox domainProfileAndButton = new HBox(4);
        domainProfilesComboBox = new ComboBox<>();
        domainProfilesComboBox.setPrefWidth(250);

        addDomainProfileButton = new Button(labels.get(LabelKey.ADD_BUTTON));

        domainProfileAndButton.getChildren().add(domainProfilesComboBox);
        domainProfileAndButton.getChildren().add(addDomainProfileButton);

        domainProfileFields.getChildren().add(domainProfileAndButton);

        topRow.getChildren().add(domainProfileFields);

        content.getChildren().add(topRow);

        //Create the controls for supplying contact information.
        //Create a vbox that displays the label for email entry and the text entry box.
        VBox contactFields = new VBox(10);
        Label contactLabel = new Label(labels.get(LabelKey.CONTACT_LABEL));
        contactLabel.getStyleClass().add(SECTION_LABEL);
        contactFields.getChildren().add(contactLabel);

        HBox secondRowContactFields = new HBox(60);
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
        ;

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
        contactFields.getChildren().add(nameEntryFields);
        secondRowContactFields.getChildren().add(emailEntryFields);
        secondRowContactFields.getChildren().add(phoneEntryFields);
        contactFields.getChildren().add(secondRowContactFields);

        content.getChildren().add(contactFields);

        VBox externalIdentifierBox = new VBox(4);
        externalIdentifierBox.setAlignment(Pos.CENTER_LEFT);

        Label externalIdentifierLabel = new Label(labels.get(LabelKey.EXTERNAL_IDENTIFIER_LABEL_KEY) + "*");
        externalIdentifierBox.getChildren().add(externalIdentifierLabel);

        externalIdentifierTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        externalIdentifierTextField.setPrefWidth(300);
        externalIdentifierBox.getChildren().add(externalIdentifierTextField);
        content.getChildren().add(externalIdentifierBox);

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
    public ListView<String> getDomainProfilesListView() {
        return null;
    }

    @Override
    public Button getAddDomainProfileButton() {
        return null;
    }

    @Override
    public Label getStatusLabel() {
        return statusLabel;
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
    public List<RemovableLabel> getKeywordsRemovableLabels() {
        return keywordRemovableLabels;
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
    public List<RemovableLabel> getInternalIdentifierRemovableLabels() {
        return internalSenderIdentifierRemovableLables;
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
    public DatePicker getBaggingDateDatePicker() {
        return baggingDateDatePicker;
    }

    @Override
    public TextField getBagSizeTextField() {
        return bagSizeTextField;
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

    public void loadDomainProfileNames(List<String> profileNames) {
        domainProfilesComboBox.getItems().addAll(profileNames);
    }

    public void addDomainProfileRemovableLabel(String label) {
        final RemovableLabel removableLabel = new RemovableLabel(label);
        domainProfileFields.getChildren().add(removableLabel);
        removableLabel.getRemoveImage().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                domainProfileFields.getChildren().remove(removableLabel);
                event.consume();
            }
        });
    }

}