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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.dataconservancy.packaging.gui.view.impl.PackageArtifactWindowBuilder;

import java.util.List;

/**
 * Class used to capture changes to simple properties, provides ability to determine if any fields are empty.
 * This code is used by the {@link PackageArtifactWindowBuilder} to determine
 * when fields are empty in order to determine if add more fields buttons should be enabled or not.
 */
public class SimplePropertyChangeListener implements ChangeListener<String> {
    private Button propertyAddButton;
    private List<Node> propertyValueBoxes;

    public SimplePropertyChangeListener(Button propertyAddButton, List<Node> propertyValueBoxes) {
        this.propertyAddButton = propertyAddButton;
        this.propertyValueBoxes = propertyValueBoxes;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        propertyAddButton.setDisable(anyPropertiesEmpty());
    }

    /**
     * Determines if any of the property values are empty
     *
     * @return true if at least one property value is empty, false if none (or if there are no values at all)
     */
    private boolean anyPropertiesEmpty() {
        for (Node n : propertyValueBoxes) {
            if (n instanceof TextField) {
                TextField text = (TextField) n;
                if (text.getText().isEmpty()) return true;
            } else if (n instanceof ComboBox) {
                ComboBox<String> box = (ComboBox<String>) n;
                if (box.getValue().isEmpty()) return true;
            }
        }

        return false;
    }
}
