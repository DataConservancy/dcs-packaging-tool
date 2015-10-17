package org.dataconservancy.packaging.gui.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.dataconservancy.packaging.gui.CssConstants;

/**
 * Created by pmeyer on 10/17/15.
 */
public class RemovableLabel extends HBox implements CssConstants {

    private ImageView removeImage;
    private Label label;

    public RemovableLabel(String labelName) {

        setSpacing(5);

        removeImage = new ImageView();
        removeImage.getStyleClass().add(BAD_INPUT_IMAGE);
        removeImage.getStyleClass().add(CLICKABLE);
        /**
         * The presenter should implement this handler appropriately:
         *
         removeImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                Do your work...
                event.consume();
            }
         });
         */

        label = new Label(labelName);

        getChildren().add(removeImage);
        getChildren().add(label);
    }

    public ImageView getRemoveImage() {
        return removeImage;
    }

}
