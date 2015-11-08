package org.dataconservancy.packaging.gui.util;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.dataconservancy.packaging.gui.App;

import java.time.LocalDate;


/**
 * A class to create Control objects for the Package Tool GUI. The main purpose of this is to provide resizing and tooltip
 * functionality for various input controls.
 */
public class ControlFactory {

    public static double textPrefWidth = 1600; //make this big enough so that text input controls will widen when widening
    // the window. this property is public so it can be used in other classes.

    private static final double startingTextHeight = 105.0;

    /**
     * This method creates various types of Controls for the GUI, setting various properties etc. as appropriate for the
     * type of control created.
     * @param type the ControlType of the control to be created
     * @param initialValue the initial text value for a text input control, ignored if not applicable
     * @param helpText the help text to display with the control
     * @return the Control of the Type specified
     */
    public static Control createControl(ControlType type, Object initialValue, final String helpText) {
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
                control = initialValue != null && initialValue instanceof String ? new TextArea((String)initialValue) : new TextArea();
                control.setPrefWidth(textPrefWidth);
                ((TextArea)control).setPrefRowCount(5);
                ((TextArea)control).setWrapText(true);
                //The following code handles the growing of the text area to fit the text. It starts as 5 rows of text and is locked to never go below that size.
                //This code only handles changes when the box is already visible for handling when the box is first visible see above.
                ((TextArea)control).textProperty().addListener((observableValue, s, newValue) -> {

                    //Account for the padding inside of the text area
                    final int textAreaPaddingSize = 20;

                    // This code can only be executed after the window is shown, because it needs to be laid out to get sized, and for the stylesheet to be set:

                    //Hide the vertical scroll bar, the scroll bar sometimes appears briefly when resizing, this prevents that.
                    ScrollBar scrollBarv = (ScrollBar)control.lookup(".scroll-bar:vertical");
                    if (scrollBarv != null ) {
                        scrollBarv.setDisable(true);
                    }

                    if (newValue.length() > 0) {
                        // Perform a lookup for an element with a css class of "text"
                        // This will give the Node that actually renders the text inside the
                        // TextArea
                        final Node text = control.lookup(".text");

                        //Text will be null if the view has text already when the pop up is being shown
                        //TODO: In java 8 this can be avoided by calling applyCSS
                        if (text != null) {
                            //If the text area is now bigger then starting size increase the size to fit the text plus the space for padding.
                            if (text.getBoundsInLocal().getHeight() + textAreaPaddingSize > startingTextHeight) {

                                control.setPrefHeight(text.getBoundsInLocal().getHeight() + textAreaPaddingSize);
                            } else { //Otherwise set to the minimum size, this needs to be checked everytime in case the user selects all the text and deletes it
                                control.setPrefHeight(startingTextHeight);
                            }
                        } else {
                            //In the case where the text is set before the view is laid out we measure the text and then set the size to it.
                            double textHeight = computeTextHeight(newValue, 170.0) + textAreaPaddingSize;
                            if (textHeight + textAreaPaddingSize > startingTextHeight) {
                                 control.setPrefHeight(textHeight);
                            } else {
                                control.setPrefHeight(startingTextHeight);
                            }
                        }
                    } else {
                        control.setPrefHeight(startingTextHeight);
                    }
                });
                break;

            case TEXT_FIELD:
                control = initialValue != null && initialValue instanceof String ? new TextField((String)initialValue) : new TextField();
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
                if (initialValue != null && initialValue instanceof LocalDate) {
                    ((DatePicker)control).setValue((LocalDate) initialValue);
                } else {
                    ((DatePicker) control).setPromptText("Select Date ->");
                }

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

    private static double computeTextHeight(String text, double wrappingWidth) {
        Text helper = new Text();
        helper.setText(text);
        helper.setFont(Font.loadFont(App.class.getResource("/fonts/OpenSans-Regular.ttf").toExternalForm(), 14));
        helper.setWrappingWidth((int)wrappingWidth);
        return helper.getLayoutBounds().getHeight();
    }
}
