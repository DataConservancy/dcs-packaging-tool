package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * Represents a group of related properties.
 */
public class PropertyCategory extends AbstractDescribedObject {


    /**
     * Note: This method converts list to HashSet so that it is order independent.
     * @return The hashcode of the PropertyCategory.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
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

        return true;
    }

    @Override
    public String toString() {
        return "PropertyCategory [label=" + getLabel() + "]";
    }
}
