package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;

/**
 * Represents a pair of symmetric of domain predicates used to connect domain
 * objects.
 */
public class StructuralRelation {
    private URI has_parent;
    private URI has_child;

    public StructuralRelation(URI has_parent, URI has_child) {
        this.has_parent = has_parent;
        this.has_child = has_child;
    }

    /**
     * @return Predicate used to indicate child has a parent or null for no such
     *         predicate.
     */
    public URI getHasParentPredicate() {
        return has_parent;
    }

    /**
     * @param has_parent The has parent predicate to set.
     */
    public void setHasParentPredicate(URI has_parent) {
        this.has_parent = has_parent;
    }

    /**
     * @return Predicate used to indicate parent has a child or null for no such
     *         predicate.
     */
    public URI getHasChildPredicate() {
        return has_child;
    }

    /**
     * @param has_child The has child predicate to set.
     */
    public void setHasChildPredicate(URI has_child) {
        this.has_child = has_child;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((has_child == null) ? 0 : has_child.hashCode());
        result = prime * result + ((has_parent == null) ? 0 : has_parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof StructuralRelation))
            return false;
        StructuralRelation other = (StructuralRelation) obj;
        if (has_child == null) {
            if (other.has_child != null)
                return false;
        } else if (!has_child.equals(other.has_child))
            return false;
        if (has_parent == null) {
            if (other.has_parent != null)
                return false;
        } else if (!has_parent.equals(other.has_parent))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StructuralRelation [has_parent=" + has_parent + ", has_child=" + has_child + "]";
    }
}

