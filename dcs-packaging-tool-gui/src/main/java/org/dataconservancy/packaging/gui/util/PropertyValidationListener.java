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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.tool.model.ValidationType;

import static org.dataconservancy.packaging.tool.model.ValidationType.*;

/**
 * Simple String property listener that validates the string field on change, and displays the correct information in the provided labels.
 */
public class PropertyValidationListener implements ChangeListener<String>, CssConstants {

    private Messages messages;
    //validationLabel contains the validation error message
    private Label validationLabel;
    //validationImageLabel provides the X or check image as a user guide
    private Label validationImageLabel;
    private PropertyBox propertyBox;
    private ValidationType validationType;
    private ImageView successImage;
    private ImageView failureImage;

    public PropertyValidationListener(PropertyBox propertyBox, ValidationType validationType) {
        this.propertyBox = propertyBox;
        this.validationType = validationType;
        this.validationImageLabel = new Label();
        this.successImage = createSuccessImageView();
        this.failureImage = createFailureImageView();
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        //If the field contains an invalid entry, we will have two children the propertyBox
        if(propertyBox.getChildren().size() == 2){
            validationLabel = (Label) propertyBox.getChildren().get(0);
        } else { //otherwise, we add the validation label, then see if the changed value is valid
            validationLabel = createValidationLabel();
            propertyBox.getChildren().add(0,validationLabel);
        }

        HBox propertyInputBox = (HBox) propertyBox.getChildren().get(1);
        validationImageLabel = (Label) propertyInputBox.getChildren().get(1);

        if (newValue != null && !newValue.isEmpty()) {
            Validator validator = ValidatorFactory.getValidator(validationType);
            boolean result = validator.isValid(newValue);

            if (result) {
                switch (validationType) {
                    case PHONE:
                    case URL:
                    case EMAIL:
                        propertyBox.getChildren().remove(validationLabel);
                        validationImageLabel.setGraphic(successImage);
                        break;
                }
            } else {
                validationImageLabel.setVisible(true);
                if (validationType != NONE) {
                    validationImageLabel.setGraphic(failureImage);
                }

                switch (validationType) {
                    case PHONE:
                        validationLabel.setText(messages.formatPhoneValidationFailure(newValue));
                        break;
                    case URL:
                        validationLabel.setText(messages.formatUrlValidationFailure(newValue));
                        break;
                    case EMAIL:
                        validationLabel.setText(messages.formatEmailValidationFailure(newValue));
                        break;
                    default:
                        propertyBox.getChildren().remove(validationLabel);
                }
            }
        } else {
            //This listener doesn't validate if a property is required or not so an empty value is considered valid
            //If user has deleted all contents of a validating field, reset the property box to its initial state
            validationImageLabel.setVisible(false);
            propertyBox.getChildren().remove(validationLabel);

        }
    }

    private Label createValidationLabel(){
        Label vLabel = new Label();
        vLabel.setMaxWidth(500);
        vLabel.setWrapText(true);
        vLabel.setTextFill(Color.RED);
        return vLabel;
    }

    private ImageView createSuccessImageView(){
        ImageView image = new ImageView();
        image.getStyleClass().add(GOOD_INPUT_IMAGE);
        return image;
    }

    private ImageView createFailureImageView(){
        ImageView image = new ImageView();
        image.getStyleClass().add(BAD_INPUT_IMAGE);
        return image;
    }
}
