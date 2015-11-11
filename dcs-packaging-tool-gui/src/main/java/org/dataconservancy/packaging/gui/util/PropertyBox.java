package org.dataconservancy.packaging.gui.util;

import javafx.scene.Node;
import javafx.scene.control.Control;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

public interface PropertyBox {

    /**
     * Gets the value contained in the property box
     * @return Gets the value of the property box.
     */
    Object getValue();

    /**
     * Gets the input control used to enter values for this property.
     * @return The scene control used for entering the property value.
     */
    Control getPropertyInput();

    /**
     * Gets the value of the property box as a string.
     * @return The string property value if it exists, undefined otherwise.
     */
    String getValueAsString();

    /**
     * Returns the value hint for the PropertyBox to help determine which get method should be used.
     * @return The PropertyValueHint for the property type this box represents.
     */
    PropertyValueType getPropertyBoxValueType();

    /**
     * Gets the view representing the property box.
     * @return The JavaFx node that makes up the view for the property box.
     */
    Node getView();
}
