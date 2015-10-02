package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * Represents a constraint based on the cardinality of some item.
 */
public interface CardinalityConstraint {
    /**
     * 
     * @return Minimum number of required occurrences.
     */
    int getMinimum();
    
    /**
     * @return Maximum number of required occurences or -1 for unbounded.
     */
    int getMaximum();
}
