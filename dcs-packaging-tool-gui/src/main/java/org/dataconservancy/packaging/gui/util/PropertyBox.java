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

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;;
import org.dataconservancy.packaging.tool.model.ValidationType;

import java.text.DecimalFormat;

/**
 * A text widget with a label and text input control, with possibility of adding validation to the text property
 * The VBox is necessary because validation adds an additional Label element above the HBox temporarily, informing the user
 * of a problem with the entry while it is invalid. This Label is removed after successful validation. Any adjustments
 * to the size of the VBox will necessitate andjusting the PropertyValidationListener class, which relies on the size
 * of the VBox in order to control presentation of this Label element.
 */
public class PropertyBox extends VBox {
    private TextInputControl textInput;
    private HBox propertyInputBox = new HBox(4);
    private Label validationImageLabel = new Label();

    public PropertyBox(String initialText, boolean isMultiLine, boolean isEditable) {
        textInput = (isMultiLine ? new TextArea(initialText) : new TextField(initialText));
        textInput.setEditable(isEditable);
        propertyInputBox.getChildren().add(textInput);
        getChildren().add(propertyInputBox);
    }

    public PropertyBox(String initialText, boolean isMultiLine, ValidationType validationType) {;
        textInput = (isMultiLine ? new TextArea(initialText) : new TextField(initialText));
        textInput.setEditable(true);
        if(!validationType.equals(ValidationType.NONE)) {
            textInput.textProperty().addListener(new PropertyValidationListener(this, validationType));
        }
        propertyInputBox.getChildren().add(textInput);
        propertyInputBox.getChildren().add(validationImageLabel);
        getChildren().add(propertyInputBox);

    }

    public PropertyBox(TextField textField, ValidationType validationType){
        textInput = textField;
        if(!validationType.equals(ValidationType.NONE)) {
            textInput.textProperty().addListener(new PropertyValidationListener(this, validationType));
        }
        propertyInputBox.getChildren().add(textInput);
        propertyInputBox.getChildren().add(validationImageLabel);
        getChildren().add(propertyInputBox);
    }

    public Label getValidationImageLabel() {
        return this.validationImageLabel;
    }

    public String getValue() {
        return textInput.getText();
    }
}

