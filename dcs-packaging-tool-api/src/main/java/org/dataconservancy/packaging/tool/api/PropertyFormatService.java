package org.dataconservancy.packaging.tool.api;

import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;

/**
 * Service to display and parse property values.
 */
public interface PropertyFormatService {
    /**
     * Format property value as a string according to its type and hint.
     * 
     * @param prop
     *            The value of the property to format.
     * @return Formatted property value
     */
    String formatPropertyValue(Property prop);

    /**
     * Attempt to parse a string into a property value according to its type and
     * hint.
     * 
     * @param type
     *            The type of the property that's going to be parsed.
     * @param value
     *            The value of the property to be parsed.
     * @return value on success and null on failure
     */
    Property parsePropertyValue(PropertyType type, String value);
}
