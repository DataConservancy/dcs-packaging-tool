package org.dataconservancy.packaging.tool.model.dprofile;

import org.apache.commons.collections.CollectionUtils;

import java.net.URI;
import java.util.List;

/**
 * The type of a property explains how to map a property to a domain object.
 *
 */
public class PropertyType extends AbstractDescribedObject {
    private URI domain_pred;
    private PropertyValueType value_type;
    private PropertyValueHint value_hint;
    private List<PropertyValue> allowed_values;
    private List<PropertyType> subtypes;
    private PropertyCategory category;
    private boolean readonly;

    /**
     * @return Domain predicate used for the property.
     */
    public URI getDomainPredicate() {
        return domain_pred;
    }

    /**
     * @return Type of the value
     */
    public PropertyValueType getPropertyValueType() {
        return value_type;
    }

    /**
     * @return Hint about how to interpret value.
     */
    public PropertyValueHint getPropertyValueHint() {
        return value_hint;
    }

    /**
     * @return If non-null and not empty, the value must be one contained in
     *         this list.
     */
    public List<PropertyValue> getAllowedPropertyValues() {
        return allowed_values;
    }

    /**
     * Complex properties contain other properties.
     * 
     * @return If the value is complex, return list of property sub-types.
     */
    public List<PropertyType> getPropertySubTypes() {
        return subtypes;
    }

    /**
     * @return Category of property.
     */
    public PropertyCategory getPropertyCategory() {
        return category;
    }

    /**
     * @return Whether or not user should be allowed to change the value.
     */
    public boolean isReadOnly() {
        return readonly;
    }

    /**
     * @param domain_pred The domain predicate to set.
     */
    public void setDomainPredicate(URI domain_pred) {
        this.domain_pred = domain_pred;
    }

    /**
     * @param value_type The value type to set.
     */
    public void setPropertyValueType(PropertyValueType value_type) {
        this.value_type = value_type;
    }

    /**
     * @param value_hint The value hint to set.
     */
    public void setPropertyValueHint(PropertyValueHint value_hint) {
        this.value_hint = value_hint;
    }

    /**
     * @param allowed_values The allowed values to set.
     */
    public void setAllowedPropertyValues(List<PropertyValue> allowed_values) {
        this.allowed_values = allowed_values;
    }

    /**
     * @param subtypes The sub-types to set.
     */
    public void setPropertySubTypes(List<PropertyType> subtypes) {
        this.subtypes = subtypes;
    }

    /**
     * @param category The category to set.
     */
    public void setCategory(PropertyCategory category) {
        this.category = category;
    }

    /**
     * @param readonly The read-only status to set.
     */
    public void setReadOnly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((allowed_values == null) ? 0 : allowed_values.hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((domain_pred == null) ? 0 : domain_pred.hashCode());
        result = prime * result + (readonly ? 1231 : 1237);
        result = prime * result + ((subtypes == null) ? 0 : subtypes.hashCode());
        result = prime * result + ((value_hint == null) ? 0 : value_hint.hashCode());
        result = prime * result + ((value_type == null) ? 0 : value_type.hashCode());
        return result;
    }

    /**
     * @param other
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof PropertyType);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PropertyType))
            return false;
        PropertyType other = (PropertyType) obj;
        
        if (!other.canEqual(this))
            return false;

        if (allowed_values == null) {
            if (other.allowed_values != null)
                return false;
        } else if (other.allowed_values == null || !CollectionUtils.isEqualCollection(allowed_values, other.allowed_values))
            return false;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        if (domain_pred == null) {
            if (other.domain_pred != null)
                return false;
        } else if (!domain_pred.equals(other.domain_pred))
            return false;
        if (readonly != other.readonly)
            return false;
        if (subtypes == null) {
            if (other.subtypes != null)
                return false;
        } else if (other.subtypes == null || !CollectionUtils.isEqualCollection(subtypes, other.subtypes))
            return false;
        if (value_hint != other.value_hint)
            return false;
        if (value_type != other.value_type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PropertyType [domain_pred=" + domain_pred + ", value_type=" + value_type + ", value_hint=" + value_hint
                + ", allowed_values=" + allowed_values + ", subtypes=" + subtypes + ", category=" + category
                + ", readonly=" + readonly + "]";
    }
}
