package org.dataconservancy.packaging.gui.util;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

public class DatePropertyBox extends HBox implements PropertyBox {

    DatePicker propertyInput;
    /**
     * Creates a validating property box for a property, with an initial String value
     * @param initialValue the intial value of the property
     * @param isEditable  indicates whether the text input control should be user-editable
     * @param helpText the help text for the field if any exists
     */
    public DatePropertyBox(Object initialValue, boolean isEditable, String helpText) {
        propertyInput = (DatePicker) ControlFactory.createControl(ControlType.DATE_PICKER, initialValue, helpText);
        propertyInput.setDisable(!isEditable);
        getChildren().add(propertyInput);
    }

    @Override
    public Object getValue() {
        return propertyInput.getValue();
    }

    @Override
    public Control getPropertyInput() {
        return propertyInput;
    }

    @Override
    public String getValueAsString() {
        return propertyInput.getValue().toString();
    }

    @Override
    public PropertyValueType getPropertyBoxValueType() {
        return PropertyValueType.DATE_TIME;
    }

    @Override
    public Node getView() {
        return this;
    }
}
