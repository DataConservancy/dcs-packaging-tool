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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.tool.model.PropertyValidationResult;
import org.dataconservancy.packaging.tool.model.ValidationType;

import static org.dataconservancy.packaging.tool.model.ValidationType.*;

/**
 * Simple String property listener that validates the string field on change, and displays the correct information in the provided labels.
 */
public class PropertyValidationListener implements ChangeListener<String>, CssConstants {

    private Messages messages;
    //validationLabel contains the validation error message
    private Label validationLabel;
    private PropertyBox propertyBox;
    private ValidationType validationType;

    public PropertyValidationListener(PropertyBox propertyBox, ValidationType validationType) {
        this.propertyBox = propertyBox;
        this.validationType = validationType;
        validationLabel = new Label();
        validationLabel.setMaxWidth(350);
        validationLabel.setWrapText(true);
        validationLabel.setTextFill(Color.RED);

    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
       Label validationImageLabel = propertyBox.getValidationImageLabel();

        if (newValue != null && !newValue.isEmpty()) {
            Validator validator = ValidatorFactory.getValidator(validationType);
            PropertyValidationResult result = new PropertyValidationResult(validator.isValid(newValue), validationType);

            if (result.isValid()) {
                switch (result.getValidationType()) {
                    case PHONE:
                    case URL:
                    case EMAIL:
                        ImageView image = new ImageView();
                        image.getStyleClass().add(GOOD_INPUT_IMAGE);
                        validationImageLabel.setGraphic(image);
                        validationImageLabel.setVisible(true);
                        propertyBox.getChildren().remove(validationLabel);
                        break;
                }
            } else {
                validationImageLabel.setVisible(true);
                if (result.getValidationType() != NONE) {
                    ImageView image = new ImageView();
                    image.getStyleClass().add(BAD_INPUT_IMAGE);
                    validationImageLabel.setGraphic(image);
                }

                switch (result.getValidationType()) {
                    case PHONE:
                        validationLabel.setText(messages.formatPhoneValidationFailure(newValue));
                        break;
                    case URL:
                        validationLabel.setText(messages.formatUrlValidationFailure(newValue));
                        break;
                    case EMAIL:
                        validationLabel.setText(messages.formatEmailValidationFailure(newValue));
                    default:
                        validationLabel.setText("");
                }
            }
        } else {
            //This listener doesn't validate if a property is required or not so an empty value is considered valid
            validationImageLabel.setVisible(false);
            propertyBox.getChildren().remove(validationLabel);
        }
    }
}
