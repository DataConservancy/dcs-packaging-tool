package org.dataconservancy.packaging.gui.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;


/**
 * A class to create Control objects for the Package Tool GUI. The main purpose of this is to provide resizing and tooltip
 * functionality for various input controls.
 */
public class ControlFactory {

    public static double textPrefWidth = 1600; //make this big enough so that text input controls will widen when widening
    // the window. this property is public so it can be used in other classes.

    /**
     * This method creates various types of Controls for the GUI, setting various properties etc. as appropriate for the
     * type of control created.
     * @param type the ControlType of the control to be created
     * @param initialValue the initial text value for a text input control, ignored if not applicable
     * @return the Control of the Type specified
     */
    public static Control createControl(ControlType type, String initialValue, final String helpText) {
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
                control.setOnMouseEntered(event -> {
                    if (helpText != null && !helpText.isEmpty()) {
                        if (control.getTooltip() == null) {
                            Tooltip tooltip = new Tooltip(helpText);
                            tooltip.setPrefWidth(350);
                            tooltip.setWrapText(true);
                            control.setTooltip(tooltip);
                        } else {
                            control.getTooltip().setText(helpText);
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
                control.setOnMouseEntered(event -> {
                    if (helpText != null && !helpText.isEmpty()) {
                        if (control.getTooltip() == null) {
                            Tooltip tooltip = new Tooltip(helpText);
                            tooltip.setPrefWidth(350);
                            tooltip.setWrapText(true);
                            control.setTooltip(tooltip);
                        } else {
                            control.getTooltip().setText(helpText);
                        }
                    }
                });
                break;

            case DATE_PICKER:
                control = new DatePicker();
                control.setPrefWidth(1600);
                ((DatePicker) control).setEditable(false);
                ((DatePicker) control).setPromptText("Select Date ->");
                control.setPrefWidth(textPrefWidth);
                control.setOnMouseEntered(event -> {
                    if (helpText != null && !helpText.isEmpty()) {
                        if (control.getTooltip() == null) {
                            Tooltip tooltip = new Tooltip(helpText);
                            tooltip.setPrefWidth(350);
                            tooltip.setWrapText(true);
                            control.setTooltip(tooltip);
                        } else {
                            control.getTooltip().setText(helpText);
                        }
                    }
                });
                break;

            default:
                throw new IllegalArgumentException("Unable to create a Control of unknown type: " + type.toString());
        }
        return control;
    }

    public static Control createControl(String promptText, String helpText, VBox parentContainer, ControlType controlType) {
        if (controlType == null) {
            throw new IllegalArgumentException("ControlType must not be null");
        }
        final Control control;
        switch (controlType) {
            case TEXT_FIELD_W_REMOVABLE_LABEL:
                control = new TextField();
                control.setPrefWidth(textPrefWidth);
                ((TextField) control).setPromptText(promptText);
                control.setOnMouseEntered(event -> {
                    if (helpText != null && !helpText.isEmpty()) {
                        if (control.getTooltip() == null) {
                            Tooltip tooltip = new Tooltip(helpText);
                            tooltip.setPrefWidth(350);
                            tooltip.setWrapText(true);
                            control.setTooltip(tooltip);
                        } else {
                            control.getTooltip().setText(helpText);
                        }
                    }
                });
                ((TextField) control).setOnAction(event -> {
                    String text = ((TextField) control).getText();
                    RemovableLabel removableLabel = new RemovableLabel(text, parentContainer);
                    parentContainer.getChildren().add(removableLabel);
                    ((TextField) control).clear();
                });
                break;

            default:
                throw new IllegalArgumentException("Unable to create a Control of unknown type: " + controlType.toString());
        }

        return control;
    }



}
