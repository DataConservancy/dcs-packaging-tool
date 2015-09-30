package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;

/**
 * Represents a pair of symmetric of domain predicates used to connect domain
 * objects.
 */
public interface StructuralRelation {
    /**
     * @return Predicate used to indicate child has a parent or null.
     */
    public URI getHasParentPredicate();

    /**
     * @return Predicate used to indicate parent has a child or null.
     */
    public URI getHasChildPredicate();
}
