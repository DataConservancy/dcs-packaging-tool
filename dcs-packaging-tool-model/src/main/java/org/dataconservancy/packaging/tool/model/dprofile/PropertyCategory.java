package org.dataconservancy.packaging.tool.model.dprofile;

import java.util.List;

/**
 * Represents a group of related properties.
 */
public interface PropertyCategory extends HasDescription {
    List<PropertyType> getPropertyTypes();
}
