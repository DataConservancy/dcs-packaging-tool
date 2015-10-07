package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;
import java.util.List;

/**
 * A profile contains information about how to organize objects in some domain
 * into a tree and perform operations on it.
 * 
 * Each node in the tree corresponds to a domain object. The type of a node
 * explains how to map a node to a domain object. Nodes have properties and each
 * property has a value. The type of a property explains how to map a property
 * to a domain object. The types also encode constraints on how domain objects
 * can be organized into a tree.
 * 
 * The node transforms explain under what circumstances and how the user can
 * modify the tree.
 * 
 * TODO: List assumptions about domain objects here?
 */
public class DomainProfile extends AbstractDescribedObject {
    private URI id;
    private URI domain_id;
    private List<NodeType> node_types;
    private List<PropertyType> prop_types;
    private List<PropertyCategory> prop_categories;
    private List<NodeTransform> node_transforms;

    /**
     * @return Identifier of profile
     */
    public URI getIdentifier() {
        return id;
    }

    /**
     * @return Identifier of profile domain
     */
    public URI getDomainIdentifier() {
        return domain_id;
    }

    /**
     * @return All node types.
     */
    public List<NodeType> getNodeTypes() {
        return node_types;
    }

    /**
     * @return All property types.
     */
    public List<PropertyType> getPropertyTypes() {
        return prop_types;
    }

    /**
     * @return All property categories.
     */
    public List<PropertyCategory> getPropertyCategories() {
        return prop_categories;
    }

    /**
     * @return All available node transforms.
     */
    public List<NodeTransform> getNodeTransforms() {
        return node_transforms;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setIdentifier(URI id) {
        this.id = id;
    }

    /**
     * @param domain_id
     *            The domain id to set.
     */
    public void setDomainIdentifier(URI domain_id) {
        this.domain_id = domain_id;
    }

    /**
     * @param node_types
     *            The node types to set.
     */
    public void setNodeTypes(List<NodeType> node_types) {
        this.node_types = node_types;
    }

    /**
     * @param prop_types
     *            The property types to set.
     */
    public void setPropertyTypes(List<PropertyType> prop_types) {
        this.prop_types = prop_types;
    }

    /**
     * @param prop_categories
     *            The property categories to set
     */
    public void setPropertyCategories(List<PropertyCategory> prop_categories) {
        this.prop_categories = prop_categories;
    }

    /**
     * @param node_transforms
     *            The node transforms to set.
     */
    public void setNodeTransforms(List<NodeTransform> node_transforms) {
        this.node_transforms = node_transforms;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((domain_id == null) ? 0 : domain_id.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((node_transforms == null) ? 0 : node_transforms.hashCode());
        result = prime * result + ((node_types == null) ? 0 : node_types.hashCode());
        result = prime * result + ((prop_categories == null) ? 0 : prop_categories.hashCode());
        result = prime * result + ((prop_types == null) ? 0 : prop_types.hashCode());
        return result;
    }
    
    /**
     * @param other 
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof DomainProfile);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof DomainProfile))
            return false;
        DomainProfile other = (DomainProfile) obj;
        
        if (!other.canEqual(this))
            return false;
        
        if (domain_id == null) {
            if (other.domain_id != null)
                return false;
        } else if (!domain_id.equals(other.domain_id))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (node_transforms == null) {
            if (other.node_transforms != null)
                return false;
        } else if (!node_transforms.equals(other.node_transforms))
            return false;
        if (node_types == null) {
            if (other.node_types != null)
                return false;
        } else if (!node_types.equals(other.node_types))
            return false;
        if (prop_categories == null) {
            if (other.prop_categories != null)
                return false;
        } else if (!prop_categories.equals(other.prop_categories))
            return false;
        if (prop_types == null) {
            if (other.prop_types != null)
                return false;
        } else if (!prop_types.equals(other.prop_types))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DomainProfile [id=" + id + ", domain_id=" + domain_id + ", node_types=" + node_types + ", prop_types="
                + prop_types + ", prop_categories=" + prop_categories + ", node_transforms=" + node_transforms + "]";
    }
}
