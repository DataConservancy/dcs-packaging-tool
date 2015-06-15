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
package org.dataconservancy.packaging.gui.util;


import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PropertyValidationResult;

import static org.dataconservancy.packaging.tool.model.PropertyValidationResult.VALIDATION_HINT.*;

/**
 * Simple String property listener that validates the string field on change, and displays the correct information in the provided labels.
 */
public class PropertyValidationListener implements ChangeListener<String>, CssConstants {

    private PackageArtifact propertyArtifact;
    private String parentPropertyName;
    private String propertyName;
    private VBox parent;
    private Label validationImageLabel;
    private PackageOntologyService ontologyService;
    private Messages messages;
    private BooleanProperty validProperty;
    private HBox propertyBox;
    private Label validationLabel;

    public PropertyValidationListener(PackageArtifact artifact, String complexPropertyName, String propertyName, VBox parent, HBox propertyBox,
                                      Label validationImageLabel, PackageOntologyService ontologyService, Messages messages, BooleanProperty validProperty) {
        this.propertyArtifact = artifact;
        this.parentPropertyName = complexPropertyName;
        this.propertyName = propertyName;
        this.validationImageLabel = validationImageLabel;
        this.ontologyService = ontologyService;
        this.messages = messages;
        this.validProperty = validProperty;
        this.propertyBox = propertyBox;
        this.parent = parent;

        validationLabel = new Label();
        validationLabel.setMaxWidth(350);
        validationLabel.setWrapText(true);
        validationLabel.setTextFill(Color.RED);

    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        if (newValue != null && !newValue.isEmpty()) {
            PropertyValidationResult result = ontologyService.validateProperty(propertyArtifact, parentPropertyName, propertyName, newValue);

            validProperty.setValue(result.isValid());

            if (result.isValid()) {
                switch (result.getHint()) {
                    case PHONE:
                    case URL:
                        ImageView image = new ImageView();
                        image.getStyleClass().add(GOOD_INPUT_IMAGE);
                        validationImageLabel.setGraphic(image);
                        validationImageLabel.setVisible(true);

                        parent.getChildren().remove(validationLabel);
                        break;
                }
            } else {
                if (!parent.getChildren().contains(validationLabel)) {
                    parent.getChildren().add(parent.getChildren().indexOf(propertyBox), validationLabel);
                }
                validationImageLabel.setVisible(true);
                if (result.getHint() != NONE) {
                    ImageView image = new ImageView();
                    image.getStyleClass().add(BAD_INPUT_IMAGE);
                    validationImageLabel.setGraphic(image);
                }

                switch (result.getHint()) {
                    case PHONE:
                        validationLabel.setText(messages.formatPhoneValidationFailure(newValue));
                        break;
                    case URL:
                        validationLabel.setText(messages.formatUrlValidationFailure(newValue));
                        break;
                    default:
                        validationLabel.setText("");
                }
            }
        } else {
            //This listener doesn't validate if a property is required or not so an empty value is considered valid
            validProperty.setValue(true);
            validationImageLabel.setVisible(false);
            parent.getChildren().remove(validationLabel);
        }
    }
}
