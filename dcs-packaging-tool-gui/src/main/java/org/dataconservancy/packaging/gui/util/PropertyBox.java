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
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.tool.model.PropertyValidationResult;
import org.dataconservancy.packaging.tool.model.ValidationType;

import java.text.DecimalFormat;

/**
 * A text widget with a label and text input control, with possibility of adding validationto the text property
 */
public class PropertyBox extends HBox {
    private Label propertyNameLabel;
    private Label validationImageLabel;
    private TextInputControl textInput;
    private boolean isSizeProperty;
    private boolean isMultiLine;
    private boolean isEditable;

    // array of labels used to format file size into B, kB, MB, GB, TB, PB, EB, ZB or YB value
    private static final String[] sizeLabels = {" Bytes", " kB", " MB", " GB", " TB", " PB", " EB", " ZB", " YB"};

    public PropertyBox(String initialText, boolean isSizeProperty, boolean isMultiLine, boolean isEditable) {
        this.isSizeProperty = isSizeProperty;
        this.isMultiLine = isMultiLine;
        this.isEditable = isEditable;
        textInput = (isMultiLine ? new TextArea(initialText) : new TextField(initialText));
        getChildren().add(propertyNameLabel);
        getChildren().add(textInput);
    }

    public PropertyBox(String initialText, boolean isSizeProperty, boolean isMultiLine, ValidationType validationType) {
        this.isSizeProperty = isSizeProperty;
        this.isMultiLine = isMultiLine;
        this.isEditable = true;
        textInput = (isMultiLine ? new TextArea(initialText) : new TextField(initialText));
        textInput.textProperty().addListener(new PropertyValidationListener(this, validationType));
        getChildren().add(propertyNameLabel);
        getChildren().add(textInput);
    }

    public PropertyBox(TextField textField, ValidationType validationType){
        textInput = textField;
        textInput.textProperty().addListener(new PropertyValidationListener(this, validationType));
        getChildren().add(propertyNameLabel);
        getChildren().add(textInput);
    }


    private String formatSizePropertyValue(String propertyName, String originalValue) {
        final DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double doubleValue = Double.parseDouble(originalValue);
        int i = 0;
        int test = 1;
        while (doubleValue >= test * 1000 && i < sizeLabels.length - 1) {
            test *= 1000;
            i++;
        }
        String sizeLabel = (doubleValue == 1) ? " Byte" : sizeLabels[i];
        return twoDecimalForm.format(doubleValue / test) + sizeLabel;
    }

    public Label getValidationImageLabel() {
        return this.validationImageLabel;
    }

    public String getValue() {
        return textInput.getText();
    }
}

