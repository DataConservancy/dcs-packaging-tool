package org.dataconservancy.packaging.tool.model.dprofile;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.List;

/**
 * Represents a group of related properties.
 */
public class PropertyCategory extends AbstractDescribedObject {
    private List<PropertyType> types;
    
    /**
     * @param types The types to set.
     */
    public void setPropertyTypes(List<PropertyType> types) {
        this.types = types;
    }

    /**
     * @return Property types in category.
     */
    public List<PropertyType> getPropertyTypes() {
        return types;
    }

    /**
     * Note: This method converts list to HashSet so that it is order independent.
     * @return The hashcode of the PropertyCategory.
     */
    @Override
    public int hashCode() {
        HashSet<PropertyType> typeSet = null;
        if (types != null) {
            typeSet = new HashSet<>();
            typeSet.addAll(types);
        }
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((typeSet == null) ? 0 : typeSet.hashCode());
        return result;
    }

    /**
     * @param other The object to compare
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof PropertyCategory);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PropertyCategory))
            return false;
        PropertyCategory other = (PropertyCategory) obj;
        
        if (!other.canEqual(this))
            return false;
        
        if (types == null) {
            if (other.types != null)
                return false;
        } else if (other.types == null || !CollectionUtils.isEqualCollection(types, other.types))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PropertyCategory [types=" + types + "]";
    }
}
