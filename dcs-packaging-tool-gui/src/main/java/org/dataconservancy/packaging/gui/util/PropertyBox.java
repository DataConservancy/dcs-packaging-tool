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
import javafx.scene.Node;
import javafx.scene.control.Control;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

public interface PropertyBox {

    /**
     * Gets the value contained in the property box
     * @return Gets the value of the property box.
     */
    Object getValue();

    /**
     * Gets the input control used to enter values for this property.
     * @return The scene control used for entering the property value.
     */
    Control getPropertyInput();

    /**
     * Gets the value of the property box as a string.
     * @return The string property value if it exists, undefined otherwise.
     */
    String getValueAsString();

    /**
     * Returns the value hint for the PropertyBox to help determine which get method should be used.
     * @return The PropertyValueHint for the property type this box represents.
     */
    PropertyValueType getPropertyBoxValueType();

    /**
     * Gets the view representing the property box.
     * @return The JavaFx node that makes up the view for the property box.
     */
    Node getView();

    /**
     * A boolean property that reflects whether or not the value in the property box is valid.
     * @return True if the property is valid, or has no corresponding validator, false otherwise.
     */
    BooleanProperty isValid();

    /**
     * Clears any values from this property box.
     */
    void clearValue();
}
