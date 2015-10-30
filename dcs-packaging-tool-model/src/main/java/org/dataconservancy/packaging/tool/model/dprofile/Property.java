package org.dataconservancy.packaging.tool.model.dprofile;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;

/**
 * A property has a value and a type.
 * Complex properties hold a set of sub-properties.
 */
public class Property {
    private Object value;
    private PropertyType type;

    public Property(PropertyType type) {
        this.type = type;
    }

    /**
     * @return The type of the property.
     */
    public PropertyType getPropertyType() {
        return type;
    }

    /**
     * @return Whether or not a value is held.
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * @return If type is string, return string value.
     */
    public String getStringValue() {
        check_value_type(PropertyValueType.STRING);
        return String.class.cast(value);
    }

    /**
     * @return If type is long, return long value.
     */
    public long getLongValue() {
        check_value_type(PropertyValueType.LONG);
        return Long.class.cast(value);
    }

    public DateTime getDateTimeValue() {
        check_value_type(PropertyValueType.DATE_TIME);
        return DateTime.class.cast(value);
    }

    /**
     * @return If type is complex, return contained values.
     */
    @SuppressWarnings("unchecked")
    public List<Property> getComplexValue() {
        check_value_type(PropertyValueType.COMPLEX);
        return List.class.cast(value);
    }

    private void check_value_type(PropertyValueType value_type) {
        if (type.getPropertyValueType() != value_type) {
            throw new IllegalStateException("Expected type of value to be " + value_type);
        }
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setStringValue(String value) {
        check_value_type(PropertyValueType.STRING);
        this.value = value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setLongValue(long value) {
        check_value_type(PropertyValueType.LONG);
        this.value = value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setDateTimeValue(DateTime value) {
        check_value_type(PropertyValueType.DATE_TIME);
        this.value = value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setComplexValue(List<Property> value) {
        check_value_type(PropertyValueType.COMPLEX);
        this.value = value;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setPropertyType(PropertyType type) {
        this.type = type;
    }

    /**
     * @return Whether or not the property has a simple value.
     */
    public boolean isSimpleValue() {
        return type != null && type.getPropertyValueType() != PropertyValueType.COMPLEX;
    }

    /**
     * @return Whether or not the property has a complex value.
     */
    public boolean isComplexValue() {
        return type != null && type.getPropertyValueType() == PropertyValueType.COMPLEX;
    }

    @Override
    public int hashCode() {
        // Special handling to ignore order of complex values.

        Object value_hash = value;

        if (isComplexValue() && hasValue()) {
            value_hash = new HashSet<>(getComplexValue());
        }

        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value_hash == null) ? 0 : value_hash.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Property))
            return false;
        Property other = (Property) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (isSimpleValue() && !value.equals(other.value)) {
            return false;
        } else if (isComplexValue() && (other.value == null
                || !CollectionUtils.isEqualCollection(getComplexValue(), other.getComplexValue()))) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "PropertyValue [value=" + value + ", type=" + type + "]";
    }
}
