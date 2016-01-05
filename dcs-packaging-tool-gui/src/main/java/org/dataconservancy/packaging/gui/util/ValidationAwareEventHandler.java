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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * This class is a validation aware event handler for our multiple field UI element.
 * This ui element and this class should be removed asap.
 */
public class ValidationAwareEventHandler implements EventHandler<ActionEvent> {

    private TextField control;
    private VBox parentContainer;
    private BooleanProperty isValid;

    public ValidationAwareEventHandler(TextField control, VBox parentContainer) {
        this.control = control;
        this.parentContainer = parentContainer;
        isValid = new SimpleBooleanProperty(true);
    }

    @Override
    public void handle(ActionEvent event) {
        if (isValid.getValue() && control.getText() != null && !control.getText().isEmpty()) {
            String text = control.getText();
            RemovableLabel removableLabel = new RemovableLabel(text, parentContainer);
            parentContainer.getChildren().add(removableLabel);
            control.clear();
        }
    }

    public void setValidProperty(BooleanProperty isValid) {
        this.isValid = isValid;
    }
}
