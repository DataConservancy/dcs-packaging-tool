package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * A PropertyConstraint represents restrictions on the cardinality and type of a
 * property in a node.
 */
public interface PropertyConstraint {
    /**
     * @return Required property type
     */
    PropertyType getPropertyType();

    /**
     * @return Required minimum cardinality.
     */
    int getMinCardinality();

    /**
     * @return Required maximum cardinality
     */
    int getMaxCardinality();
}
