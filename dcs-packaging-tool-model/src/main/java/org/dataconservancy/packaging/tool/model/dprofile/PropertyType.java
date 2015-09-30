package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;
import java.util.List;

/**
 * The type of a property explains how to map a property to a domain object.
 *
 */
public interface PropertyType extends HasDescription {
    /**
     * @return Domain predicate used for the property.
     */
    URI getDomainPredicate();

    /**
     * @return Type of the value
     */
    PropertyValueType getPropertyValueType();

    /**
     * @return Hint about how to interpret value.
     */
    PropertyValueHint getPropertyValueHint();

    /**
     * @return If non-null and not empty, the value must be one contained in
     *         this list.
     */
    List<PropertyValue> getAllowedValues();

    /**
     * @return If the value is complex, return list of subproperty types.
     */
    List<PropertyType> getSubPropertyTypes();

    /**
     * @return Category of property.
     */
    PropertyCategory getPropertyCategory();

    /**
     * @return Whether or not user should be allowed to change the value.
     */
    boolean isReadOnly();
}
