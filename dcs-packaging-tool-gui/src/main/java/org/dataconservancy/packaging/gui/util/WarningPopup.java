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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.Labels;

/**
 * A popup that has a warning message for the user with confirm and cancel actions.
 */
public class WarningPopup extends PackageToolPopup {

    private Label message;
    private Button cancelButton;
    private Button confirmButton;

    public WarningPopup(Labels labels) {
        super();

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        message = new Label();
        content.getChildren().add(message);

        HBox buttonsHBox = new HBox(5);
        buttonsHBox.setAlignment(Pos.CENTER);

        confirmButton = new Button();
        confirmButton.setText(labels.get(Labels.LabelKey.CONTINUE_BUTTON));
        confirmButton.getStyleClass().add(CLICKABLE);
        buttonsHBox.getChildren().add(confirmButton);

        cancelButton = new Button();
        cancelButton.setText(labels.get(Labels.LabelKey.CANCEL_BUTTON));
        cancelButton.getStyleClass().add(CLICKABLE);
        buttonsHBox.getChildren().add(cancelButton);

        content.getChildren().add(buttonsHBox);

        super.setContent(content);
    }

    @Override
    public void setContent(Node content) {
        //no-op the content for this popup is set in the constructor
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setCancelEventHandler(EventHandler<ActionEvent> handler) {
        cancelButton.setOnAction(handler);
    }

    public void setConfirmEventHandler(EventHandler<ActionEvent> handler) {
        confirmButton.setOnAction(handler);
    }

}
