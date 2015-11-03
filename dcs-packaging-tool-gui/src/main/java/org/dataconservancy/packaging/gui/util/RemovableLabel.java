package org.dataconservancy.packaging.gui.util;

import javafx.event.EventHandler;
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
        removeImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                parentElement.getChildren().remove(RemovableLabel.this);
                event.consume();
            }
        });

        label = new Label(labelName);

        getChildren().add(removeImage);
        getChildren().add(label);
    }

    public Label getLabel() {
        return label;
    }
}
