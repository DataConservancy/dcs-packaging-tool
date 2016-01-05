package org.dataconservancy.packaging.gui.util;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

public class NumericPropertyBox extends HBox implements PropertyBox {
    TextInputControl propertyInput;
    /**
     * Creates a validating property box for a property, with an initial String value
     * @param initialValue the intial value of the property
     * @param isEditable  indicates whether the text input control should be user-editable
     * @param helpText the help text for the field if any exists
     */
    public NumericPropertyBox(Object initialValue, boolean isEditable, String helpText) {
        propertyInput = (TextInputControl) ControlFactory.createControl(ControlType.NUMERIC, initialValue, helpText);
        propertyInput.setDisable(!isEditable);
        getChildren().add(propertyInput);
    }

    @Override
    public Object getValue() {
        Long value = null;
        if (propertyInput.getText() != null && !propertyInput.getText().isEmpty()) {
            value = Long.valueOf(propertyInput.getText());
        }
        return value;
    }

    @Override
    public Control getPropertyInput() {
        return propertyInput;
    }

    @Override
    public String getValueAsString() {
        return propertyInput.getText();
    }

    @Override
    public PropertyValueType getPropertyBoxValueType() {
        return PropertyValueType.LONG;
    }

    @Override
    public Node getView() {
        return this;
    }

    @Override
    public BooleanProperty isValid() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public void clearValue() {
        propertyInput.clear();
    }
}
