package org.dataconservancy.packaging.gui.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.CssConstants;

import java.util.List;

/**
 * Created by pmeyer on 10/17/15.
 */
public class RemovableLabel extends HBox implements CssConstants {

    public RemovableLabel(final String labelName, final VBox parentElement) {

        setSpacing(4);

        ImageView removeImage = new ImageView();
        removeImage.getStyleClass().add(TRASH_IMAGE);
        removeImage.getStyleClass().add(CLICKABLE);
        removeImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                parentElement.getChildren().remove(RemovableLabel.this);
                event.consume();
            }
        });

        Label label = new Label(labelName);
        label.setPadding(new Insets(4, 0, 0, 0));

        getChildren().add(removeImage);
        getChildren().add(label);
    }

}
