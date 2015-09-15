package org.dataconservancy.packaging.gui.util;

import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;


/**
 * A class to create Control objects for the Package Tool GUI. The main purpose of this is to provide resizing ang tooltip
 * functionality for various input controls.
 */
public class ControlFactory {

    public static double textPrefWidth = 1600; //make this big enough so that text input controls will widen when widening
    // the window. this property is public so it can be used in other classes.

    /**
     * This method creates various types of Controls for the GUI, setting various properties etc. as approriate for the
     * type of control created.
     * @param type the ControlType of the control to be created
     * @param initialValue the initial text value for a text input control, ignored if not applicable
     * @return the Control of the Type specified
     */
    public static Control createControl(ControlType type, String initialValue) {
        if (type == null) {
          throw new IllegalArgumentException("ControlType must not be null");
        }
        final Control control;
        switch (type) {
            case COMBO_BOX:
                 control = new ComboBox<>();
                ((ComboBox)control).setEditable(false);
                control.setPrefWidth(textPrefWidth);
                break;

            case EDITABLE_COMBO_BOX:
                control = new ComboBox<>();
                ((ComboBox)control).setEditable(true);
                control.setPrefWidth(textPrefWidth);
                control.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        String text = ((ComboBox) control).getEditor().getText();
                        if (!text.isEmpty()) {
                            if (control.getTooltip() == null) {
                                control.setTooltip(new Tooltip(text));
                            } else {
                                control.getTooltip().setText(text);
                            }
                        }
                    }
                });
                break;

            case TEXT_AREA:
                control = initialValue == null ? new TextArea() : new TextArea(initialValue);
                control.setPrefWidth(textPrefWidth);
                break;

            case TEXT_FIELD:
                control = initialValue == null ? new TextField() : new TextField(initialValue);
                control.setPrefWidth(textPrefWidth);
                control.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        String text = ((TextField)control).getText();
                        if (!text.isEmpty()) {
                            if (control.getTooltip() == null) {
                                control.setTooltip(new Tooltip(text));
                            } else {
                                control.getTooltip().setText(text);
                            }
                        }
                    }
                });
                break;

            default:
                throw new IllegalArgumentException("Unable to create a Control of unknown type: " + type.toString());
        }
        return control;
    }



}
