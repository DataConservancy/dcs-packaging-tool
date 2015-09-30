package org.dataconservancy.packaging.tool.model.dprofile;

import java.util.List;

/**
 * The value of a property.
 */
public interface PropertyValue {
    /**
     * @return The type of the property.
     */
    PropertyType getPropertyType();

    /**
     * @return If type is string, return string value.
     */
    String asString();

    /**
     * @return If type is long, return long value.
     */
    long asLong();

    // TODO: DateTime asDateTime();

    /**
     * @return If type is complex, return contained values.
     */
    List<PropertyValue> asComplexValue();
}
