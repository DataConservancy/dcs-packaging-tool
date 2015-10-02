package org.dataconservancy.packaging.tool.model.dprofile;

import java.util.List;

/**
 * Represents a group of related properties.
 */
public interface PropertyCategory extends HasDescription {
    
    /**
     * @return Property types in category.
     */
    List<PropertyType> getPropertyTypes();
}
