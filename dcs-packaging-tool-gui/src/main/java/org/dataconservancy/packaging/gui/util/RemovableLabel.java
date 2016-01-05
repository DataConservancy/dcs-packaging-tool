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


import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.CssConstants;

/**
 * Custom widget that has a label with a trash icon within a parent vbox that is passed in so that these values can
 * be added to or removed from that vbox.
 */
public class RemovableLabel extends HBox implements CssConstants {

    private Label label;

    public RemovableLabel(final String labelName, final VBox parentElement) {

        setSpacing(4);

        ImageView removeImage = new ImageView();
        removeImage.getStyleClass().add(TRASH_IMAGE);
        removeImage.getStyleClass().add(CLICKABLE);
        removeImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            parentElement.getChildren().remove(RemovableLabel.this);
            event.consume();
        });

        label = new Label(labelName);

        getChildren().add(removeImage);
        getChildren().add(label);
    }

    public Label getLabel() {
        return label;
    }
}
