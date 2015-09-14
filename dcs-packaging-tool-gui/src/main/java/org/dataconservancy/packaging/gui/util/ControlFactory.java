package org.dataconservancy.packaging.gui.util;

import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;


/**
 * Created by jrm on 9/11/15.
 */
public class ControlFactory {

    public static double textPrefWidth = 1600; //make this big enough so that the control will widen when widening
    // the window. this variable is public so it can be used with other Controls besides those set up here


    public static TextInputControl createControl(ControlType type, String initialValue) {
        if (type == null) {
          throw new IllegalArgumentException("ControlType must not be null");
        }
        final TextInputControl control;
        switch (type) {
            case TEXT_AREA:
                control = initialValue == null ? new TextArea() : new TextArea(initialValue);
                break;
            case TEXT_FIELD:
                control = initialValue == null ? new TextField() : new TextField(initialValue);
                control.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!control.getText().isEmpty()) {
                            if (control.getTooltip() == null) {
                                control.setTooltip(new Tooltip(control.getText()));
                            } else {
                                control.getTooltip().setText(control.getText());
                            }
                        }
                    }
                });

                break;
            default:
                throw new IllegalArgumentException("Unable to create a Control of unknown type: " + type.toString());
        }
        control.setPrefWidth(textPrefWidth);
        return control;
    }



}
