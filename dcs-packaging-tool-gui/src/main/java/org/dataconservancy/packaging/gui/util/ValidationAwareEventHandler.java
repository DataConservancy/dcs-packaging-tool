package org.dataconservancy.packaging.gui.util;

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
        if (isValid.getValue()) {
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
