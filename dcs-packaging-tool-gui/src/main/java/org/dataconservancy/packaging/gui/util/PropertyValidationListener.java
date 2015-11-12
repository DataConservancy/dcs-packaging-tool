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
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;

/**
 * Simple String property listener that validates the string field on change, and displays the correct information in the provided labels.
 */
public class PropertyValidationListener implements ChangeListener<String>, CssConstants {

    //validationImageLabel provides the X or check image as a user guide
    private Label validationImageLabel;
    private TextPropertyBox textPropertyBox;
    private PropertyValueHint validationType;
    private ImageView successImage;
    private ImageView failureImage;

    /**
     * A validation listener for a property box.
     * @param textPropertyBox the property box whose text input control is to be listened to
     * @param validationType the type of validation this input control's value requires
     */
    public PropertyValidationListener(TextPropertyBox textPropertyBox, PropertyValueHint validationType) {
        this.textPropertyBox = textPropertyBox;
        this.validationType = validationType;
        this.validationImageLabel = new Label();
        this.successImage = createSuccessImageView();
        this.failureImage = createFailureImageView();
    }

    public void updateValidationType(PropertyValueHint validationType) {
        this.validationType = validationType;
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        //If the field contains an invalid entry, we will have two children the propertyBox
        Label validationLabel;
        if(textPropertyBox.getChildren().size() == 2){
            validationLabel = (Label) textPropertyBox.getChildren().get(0);
        } else { //otherwise, we add the validation label, then see if the changed value is valid
            validationLabel = createValidationLabel();
            textPropertyBox.getChildren().add(0, validationLabel);
        }

        HBox propertyInputBox = (HBox) textPropertyBox.getChildren().get(1);
        validationImageLabel = (Label) propertyInputBox.getChildren().get(1);
        Validator validator = null;
        if (validationType != null) {
            validator = ValidatorFactory.getValidator(validationType);
        }

        if (validator != null && newValue != null && !newValue.isEmpty()) {
            boolean result = validator.isValid(newValue);

            if (result) {
                switch (validationType) {
                    case PHONE_NUMBER:
                    case URL:
                    case EMAIL:
                        textPropertyBox.getChildren().remove(validationLabel);
                        validationImageLabel.setGraphic(successImage);
                        break;
                }
            } else {
                validationImageLabel.setGraphic(failureImage);
                validationImageLabel.setVisible(true);

                switch (validationType) {
                    case PHONE_NUMBER:
                        validationLabel.setText(TextFactory.format(Messages.MessageKey.PHONE_VALIDATION_FAILURE, newValue));
                        break;
                    case URL:
                        validationLabel.setText(TextFactory.format(Messages.MessageKey.URL_VALIDATION_FAILURE, newValue));
                        break;
                    case EMAIL:
                        validationLabel.setText(TextFactory.format(Messages.MessageKey.EMAIL_VALIDATION_FAILURE, newValue));
                        break;
                    default:
                        textPropertyBox.getChildren().remove(validationLabel);
                }
            }
        } else {
            //This listener doesn't validate if a property is required or not so an empty value is considered valid
            //If user has deleted all contents of a validating field, reset the property box to its initial state
            validationImageLabel.setVisible(false);
            textPropertyBox.getChildren().remove(validationLabel);

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
