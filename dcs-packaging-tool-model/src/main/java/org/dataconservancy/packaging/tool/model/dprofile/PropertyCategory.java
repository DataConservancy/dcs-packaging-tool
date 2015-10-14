package org.dataconservancy.packaging.tool.model.dprofile;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((types == null) ? 0 : types.hashCode());
        return result;
    }

    /**
     * @param other
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
        } else if (!types.equals(other.types))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PropertyCategory [types=" + types + "]";
    }
}
