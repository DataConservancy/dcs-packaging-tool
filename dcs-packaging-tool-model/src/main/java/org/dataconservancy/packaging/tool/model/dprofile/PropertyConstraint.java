package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * A PropertyConstraint represents restrictions on the cardinality and type of a
 * property in a node.
 */
public class PropertyConstraint extends CardinalityConstraint {
    private PropertyType type;

    /**
     * @return Required property type
     */
    public PropertyType getPropertyType() {
        return type;
    }

    public void setPropertyType(PropertyType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * @param other 
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof PropertyConstraint);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PropertyConstraint))
            return false;
        PropertyConstraint other = (PropertyConstraint) obj;
        
        if (!other.canEqual(this))
            return false;

        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PropertyConstraint [type=" + type + "] {" + super.toString() + "}";
    }
}
