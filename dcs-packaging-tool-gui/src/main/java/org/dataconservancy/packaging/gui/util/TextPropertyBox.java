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

package org.dataconservancy.packaging.gui.util;

import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;

import java.time.LocalDate;

/**
 * A text widget with a label and text input control, with possibility of adding validation to the text property
 * The VBox is necessary because validation adds an additional Label element above the HBox temporarily, informing the user
 * of a problem with the entry while it is invalid. This Label is removed after successful validation. Any adjustments
 * to the size of the VBox will necessitate adjusting the PropertyValidationListener class, which relies on the size
 * of the VBox in order to control presentation of this Label element.
 */
public class TextPropertyBox extends VBox {
    private Control propertyInput;
    private HBox propertyInputBox = new HBox(4);
    private Label validationImageLabel = new Label();

    /**
     * Creates a non-validating property box for a property, with an initial String value
     * @param initialValue the intial value of the property
     * @param isEditable  indicates whether the text input control should be user-editable
     * @param helpText the help text for the field if any exists
     * @param validationType indicates the ValidationType of validator that should be supplied to
                            the text control input
     */
    public TextPropertyBox(Object initialValue, boolean isEditable, PropertyValueHint validationType, String helpText) {
        createPropertyValueWidget(isEditable, initialValue, helpText, validationType);

        propertyInputBox.getChildren().add(propertyInput);
        propertyInputBox.getChildren().add(validationImageLabel);
        getChildren().add(propertyInputBox);
    }

    /**
     * Creates a validating PropertyBox with a supplied TextField
     * @param textField the supplied TextField
     * @param validationType  indicates the ValidationType of validator that should be supplied to
     *                       the text control input
     */
    //TODO: This should really go away, but is here to support repeatable box in the PackageMetadata view.
    public TextPropertyBox(TextField textField, PropertyValueHint validationType){
        propertyInput = textField;
        if(validationType != null) {
            ((TextInputControl)propertyInput).textProperty().addListener(new PropertyValidationListener(this, validationType));
        }
        propertyInputBox.getChildren().add(propertyInput);
        propertyInputBox.getChildren().add(validationImageLabel);
        getChildren().add(propertyInputBox);
    }

    private void createPropertyValueWidget(boolean editable, Object initialValue, String helpText, PropertyValueHint valueHint){
        if (valueHint == null) {
            propertyInput = ControlFactory.createControl(ControlType.TEXT_FIELD, initialValue, helpText);
            ((TextInputControl) propertyInput).setEditable(editable);
        } else if (valueHint.equals(PropertyValueHint.DATE_TIME)) {
            propertyInput = ControlFactory.createControl(ControlType.DATE_PICKER, initialValue, helpText);
            propertyInput.setDisable(!editable);
        } else if (valueHint.equals(PropertyValueHint.MULTI_LINE_TEXT)) {
            propertyInput = ControlFactory.createControl(ControlType.TEXT_AREA, initialValue, helpText);
            ((TextInputControl) propertyInput).setEditable(editable);
        } else {
            propertyInput = ControlFactory.createControl(ControlType.TEXT_FIELD, initialValue, helpText);
            ((TextInputControl) propertyInput).textProperty().addListener(new PropertyValidationListener(this, valueHint));
            ((TextInputControl) propertyInput).setEditable(editable);
        }

    }
    /**
     * @return the value of the TextInputControl in the PropertyBox
     */
    public String getValueAsString() {
        return ((TextInputControl) propertyInput).getText();
    }

    /**
     * @return the vale of the DatePicker in the PropertyBox
     */
    public LocalDate getValueAsDate() {
        return ((DatePicker) propertyInput).getValue();
    }

    /**
     * @return the value of the property box as an object, it's up to the client to determine the type.
     */
    public Object getValue() {
        if (propertyInput instanceof TextInputControl) {
            return getValueAsString();
        } else {
            return getValueAsDate();
        }
    }

    /**
     * Gets the text input control used to enter values, this allows externally setting listeners on the control.
     * @return The text input control for the PropertyBox.
     */
    public Control getPropertyInput() {
        return propertyInput;
    }
}

