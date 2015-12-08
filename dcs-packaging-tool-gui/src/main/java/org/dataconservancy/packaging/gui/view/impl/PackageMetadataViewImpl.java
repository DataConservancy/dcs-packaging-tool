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

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Help.HelpKey;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.util.ControlFactory;
import org.dataconservancy.packaging.gui.util.ControlType;
import org.dataconservancy.packaging.gui.util.TextPropertyBox;
import org.dataconservancy.packaging.gui.view.PackageMetadataView;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the view that displays the controls for package metadata.
 */
public class PackageMetadataViewImpl extends BaseViewImpl<PackageMetadataPresenter> implements PackageMetadataView {

    //Controls for setting the package name and output directory
    private TextField packageNameField;

    //The value of the combobox domain profiles
    private ComboBox<String> domainProfilesComboBox;

    //private Label errorLabel;
    private ScrollPane contentScrollPane;
    private VBox content;
    private VBox requiredVBox;
    private VBox bottomContent;

    private List<Node> allDynamicFields;
    private boolean formAlreadyDrawn = false;
    private Set<String> failedValidation;

    public PackageMetadataViewImpl(Help help) {
        super();
        
        allDynamicFields = new ArrayList<>();
        failedValidation = new HashSet<>();

        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        content = new VBox();

        //Set up the text for the controls in the footer.
        getContinueButton().setText(TextFactory.getText(LabelKey.SAVE_AND_CONTINUE_BUTTON));
        getCancelLink().setText(TextFactory.getText(LabelKey.BACK_LINK));
        getSaveButton().setText(TextFactory.getText(LabelKey.SAVE_BUTTON));
        getSaveButton().setVisible(true);

        content.getStyleClass().add(PACKAGE_GENERATION_VIEW_CLASS);
        contentScrollPane.setContent(content);
        setCenter(contentScrollPane);

        //Create a label to show any status messages at the top of the screen.
        HBox status = new HBox();
        status.getChildren().add(errorLabel);
        status.setAlignment(Pos.TOP_CENTER);

        content.getChildren().add(status);

        requiredVBox = new VBox(5);

        VBox requiredLabelVBox = createSectionLabel(TextFactory.getText(LabelKey.REQUIRED_FIELDS_LABEL));
        requiredVBox.getChildren().add(requiredLabelVBox);

        // setup static fields
        HBox topRow = new HBox(40);

        //since we want this field to be validatable, we create a PackageMetadata
        //object to pass to createFieldsView
        PackageMetadata packageNameMetadata = new PackageMetadata();
        packageNameMetadata.setName(TextFactory.getText(LabelKey.PACKAGE_NAME_LABEL));
        packageNameMetadata.setLabel(packageNameMetadata.getName().replace("-"," "));
        packageNameMetadata.setEditable(true);
        packageNameMetadata.setHelpText(TextFactory.getText(LabelKey.PACKAGE_NAME_TOOLTIP));
        packageNameMetadata.setValidationType(PropertyValueHint.FILE_NAME);

        VBox packageNameEntryFields = createFieldsView(packageNameMetadata);
        packageNameEntryFields.setPrefWidth(310);
        packageNameEntryFields.setAlignment(Pos.TOP_LEFT);
        topRow.getChildren().add(packageNameEntryFields);

        VBox domainProfileVBox = new VBox(4);

        HBox domainProfileLabelAndTooltipBox = new HBox(4);
        Label domainProfileLabel = new Label(TextFactory.getText(LabelKey.SELECT_DOMAIN_PROFILE_LABEL));
        domainProfileLabelAndTooltipBox.getChildren().add(domainProfileLabel);

        ImageView domainProfileToolTipImage = new ImageView();
        domainProfileToolTipImage.getStyleClass().add(TOOLTIP_IMAGE);
        Tooltip domainProfileToolTip = new Tooltip(TextFactory.getText(LabelKey.DOMAIN_PROFILE_TOOLTIP));
        domainProfileToolTip.setPrefWidth(350);
        domainProfileToolTip.setWrapText(true);
        Tooltip.install(domainProfileToolTipImage, domainProfileToolTip);
        domainProfileLabelAndTooltipBox.getChildren().add(domainProfileToolTipImage);

        domainProfileVBox.getChildren().add(domainProfileLabelAndTooltipBox);
        domainProfilesComboBox = new ComboBox<>();
        domainProfilesComboBox.setPrefWidth(267);
        domainProfileVBox.getChildren().add(domainProfilesComboBox);

        topRow.getChildren().add(domainProfileVBox);

        requiredVBox.getChildren().add(topRow);

        content.getChildren().add(requiredVBox);

        bottomContent = new VBox(5);

        content.getChildren().add(bottomContent);
        
        setHelpPopupContent(help.get(HelpKey.PACKAGE_METADATA_HELP));
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
    public void scrollToTop() {
        contentScrollPane.setVvalue(0);
    }

    @Override
    public void loadDomainProfileNames(List<String> profileNames) {
        Collections.sort(profileNames);
        domainProfilesComboBox.getItems().setAll(profileNames);
        domainProfilesComboBox.setValue(profileNames.get(0));
    }


    @Override
    public void setupRequiredFields(List<PackageMetadata> requiredPackageMetadataList) {
        if (requiredPackageMetadataList != null && !requiredPackageMetadataList.isEmpty()) {

            requiredPackageMetadataList.stream().filter(PackageMetadata::isVisible).forEach(packageMetadata -> {
                VBox fieldContainer = createFieldsView(packageMetadata);
                requiredVBox.getChildren().add(fieldContainer);
            });

            formAlreadyDrawn = true;
        }
    }

    @Override
    public void setupRecommendedFields(List<PackageMetadata> recommendedPackageMetadataList) {
        if (recommendedPackageMetadataList != null && !recommendedPackageMetadataList.isEmpty()) {

            VBox recommendedLabelVBox = createSectionLabel(TextFactory.getText(LabelKey.RECOMMENDED_FIELDS_LABEL));
            bottomContent.getChildren().add(recommendedLabelVBox);

            recommendedPackageMetadataList.stream().filter(PackageMetadata::isVisible).forEach(packageMetadata -> {
                VBox fieldContainer = createFieldsView(packageMetadata);
                bottomContent.getChildren().add(fieldContainer);
            });

            formAlreadyDrawn = true;
        }
    }

    @Override
    public void setupOptionalFields(List<PackageMetadata> optionalPackageMetadataList) {
        if (optionalPackageMetadataList != null && !optionalPackageMetadataList.isEmpty()) {

            VBox optionalLabelVBox = createSectionLabel(TextFactory.getText(LabelKey.OPTIONAL_FIELDS_LABEL));
            bottomContent.getChildren().add(optionalLabelVBox);

            optionalPackageMetadataList.stream().filter(PackageMetadata::isVisible).forEach(packageMetadata -> {
                VBox fieldContainer = createFieldsView(packageMetadata);
                bottomContent.getChildren().add(fieldContainer);
            });

            formAlreadyDrawn = true;
        }
    }

    @Override
    public void clearAllFields() {
        packageNameField.clear();
        domainProfilesComboBox.getItems().clear();
        domainProfilesComboBox.setDisable(false);
        for (Node node : allDynamicFields) {
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
    public List<Node> getAllDynamicFields() {
        return this.allDynamicFields;
    }

    @Override
    public boolean isFormAlreadyDrawn() {
        return formAlreadyDrawn;
    }

    /**
     * Helper method that creates the field based on the given package metadata.
     *
     * @param packageMetadata the given package metadata
     * @return container vbox with the field
     */
    private VBox createFieldsView(PackageMetadata packageMetadata) {
        VBox fieldContainer = new VBox(4);

        HBox fieldLabelHbox = new HBox(4);
        fieldLabelHbox.setAlignment(Pos.CENTER_LEFT);
        Label fieldLabel = new Label(packageMetadata.getLabel());
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
            allDynamicFields.add(parentContainer);

            TextField textField = (TextField) ControlFactory.createControl(TextFactory.getText(LabelKey.TYPE_VALUE_AND_ENTER_PROMPT), packageMetadata.getHelpText(), parentContainer, ControlType.TEXT_FIELD_W_REMOVABLE_LABEL);

            if (packageMetadata.getValidationType() != null && packageMetadata.getValidationType().equals(PropertyValueHint.URL)) {
                // TODO: this may have to be done via a button
                TextPropertyBox propertyBox = new TextPropertyBox(textField, packageMetadata.getValidationType());
                fieldContainer.getChildren().add(propertyBox);
            }

            fieldContainer.getChildren().add(textField);
            fieldContainer.getChildren().add(parentContainer);

        } else {

            if (packageMetadata.getValidationType() != null && packageMetadata.getValidationType().equals(PropertyValueHint.DATE_TIME)) {
                DatePicker datePicker = (DatePicker) ControlFactory.createControl(ControlType.DATE_PICKER, LocalDate.now(), packageMetadata.getHelpText());
                datePicker.setId(packageMetadata.getName());
                allDynamicFields.add(datePicker);
                fieldContainer.getChildren().add(datePicker);
            } else {
                String initialValue = packageMetadata.getDefaultValue() != null ? packageMetadata.getDefaultValue() : "";
                TextPropertyBox propertyBox = new TextPropertyBox(initialValue, packageMetadata.isEditable(), packageMetadata.getValidationType(), packageMetadata.getHelpText());
                propertyBox.getPropertyInput().setId(packageMetadata.getName());
                allDynamicFields.add(propertyBox.getPropertyInput());

                //sorry for this hack, need to set this value here
                if(packageMetadata.getName().equals(TextFactory.getText(LabelKey.PACKAGE_NAME_LABEL))){
                    packageNameField = (TextField) propertyBox.getPropertyInput();
                }
                fieldContainer.getChildren().add(propertyBox);
            }
        }
        return fieldContainer;
    }

    /**
     * Helper method that creates labels with tooltips for a given text.
     *
     * @param text  the supplied text
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

    @Override
    public boolean hasFailedValidation(String fieldName) {
        return failedValidation.contains(fieldName);
    }

}