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
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.Labels;

/**
 * A popup that includes a progress dialog. You don't use setContent with this popup only setTitle and setMessage.
 */
public class ProgressDialogPopup extends PackageToolPopup {

    Label message;
    Button cancelButton;
    public ProgressDialogPopup(Labels labels) {
        super();

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        message = new Label();
        content.getChildren().add(message);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        content.getChildren().add(progressBar);

        cancelButton = new Button();
        cancelButton.setText(labels.get(Labels.LabelKey.CANCEL_BUTTON));
        content.getChildren().add(cancelButton);

        super.setContent(content);
    }

    @Override
    public void setContent(Node content) {
        //NO-op the content for this popup is set in the constructor
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setCancelEventHandler(EventHandler<ActionEvent> handler) {
        cancelButton.setOnAction(handler);
    }

}
