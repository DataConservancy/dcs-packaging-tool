package org.dataconservancy.packaging.tool.model.dprofile;

import org.apache.commons.collections.CollectionUtils;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

/**
 * The type of a property explains how to map a property to a domain object.
 *
 */
public class PropertyType extends AbstractDescribedObject {
    private URI domain_pred;
    private PropertyValueType value_type;
    private PropertyValueHint value_hint;
    private List<Property> allowed_values;
    private List<PropertyConstraint> complex_constraints;
    private List<URI> complex_domain_types;
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
    public List<Property> getAllowedPropertyValues() {
        return allowed_values;
    }

    /**
     * Complex properties contain other properties.
     * 
     * @return If the value is complex, return list of property constraints.
     */
    public List<PropertyConstraint> getComplexPropertyConstraints() {
        return complex_constraints;
    }
    
    /**
     * @return Unordered list of domain types of this complex property.
     */
    public List<URI> getComplexDomainTypes() {
        return complex_domain_types;
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
    public void setAllowedPropertyValues(List<Property> allowed_values) {
        this.allowed_values = allowed_values;
    }

    /**
     * @param complex_constraints The types contained by a complex property.
     */
    public void setComplexPropertyConstraints(List<PropertyConstraint> complex_constraints) {
        this.complex_constraints = complex_constraints;
    }
    
    /**
     * @param domain_types The domain types of a complex property.
     */
    public void setComplexDomainTypes(List<URI> domain_types) {
        this.complex_domain_types = domain_types;
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

    /**
     * Note this method converts lists to HashSets so they are order independent.
     * @return The hashcode of the PropertyType
     */
    @Override
    public int hashCode() {
            final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((allowed_values == null) ? 0 : new HashSet<>(allowed_values).hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((domain_pred == null) ? 0 : domain_pred.hashCode());
        result = prime * result + (readonly ? 1231 : 1237);
        result = prime * result + ((complex_constraints == null) ? 0 : new HashSet<>(complex_constraints).hashCode());
        result = prime * result + ((complex_domain_types == null) ? 0 : new HashSet<>(complex_domain_types).hashCode());
        result = prime * result + ((value_hint == null) ? 0 : value_hint.hashCode());
        result = prime * result + ((value_type == null) ? 0 : value_type.hashCode());
        return result;
    }

    /**
     * @param other The object to compare
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
        if (complex_constraints == null) {
            if (other.complex_constraints != null)
                return false;
        } else if (other.complex_constraints == null || !CollectionUtils.isEqualCollection(complex_constraints, other.complex_constraints))
            return false;
        if (complex_domain_types == null) {
            if (other.complex_domain_types != null)
                return false;
        } else if (other.complex_domain_types == null || !CollectionUtils.isEqualCollection(complex_domain_types, other.complex_domain_types))
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
                + ", allowed_values=" + allowed_values + ", subtypes=" + complex_constraints + ", category=" + category
                + ", readonly=" + readonly + "]";
    }
}
