package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * The type of a node explains how to map a node to a domain object.
 * 
 * Note that the type of a node depends solely on its parent constraints and
 * file association requirements.
 */
public class NodeType extends AbstractDescribedObject {
    private URI id;
    private DomainProfile profile;
    private List<URI> domain_types;
    private List<NodeConstraint> parent_constraints;
    private List<PropertyConstraint> property_constraints;
    private List<PropertyType> inheritable_properties;
    private List<PropertyValue> default_property_values;
    private Map<PropertyType, URI> supplied_properties;
    private Requirement file_req;
    private Requirement dir_req;
    private CardinalityConstraint child_file_constraint;
    
    /**
     * @return Unique identifier of this node type.
     */
    public URI getIdentifier() {
        return id;
    }

    /**
     * @return Domain profile which defines this node type.
     */
    public DomainProfile getDomainProfile() {
        return profile;
    }
    
    /**
     * @return The types of the corresponding domain object
     */
    public List<URI> getDomainTypes() {
        return domain_types;
    }

    /**
     * The parent node must meet at least one of these constraints.
     * 
     * @return Constraints on parent node.
     */
    public List<NodeConstraint> getParentConstraints() {
        return parent_constraints;
    }

    /**
     * @return Constraints on node properties.
     */
    public List<PropertyConstraint> getPropertyConstraints() {
        return property_constraints;
    }

    /**
     * @return Types of properties which may be inherited by descendants.
     */
    public List<PropertyType> getInheritableProperties() {
        return inheritable_properties;
    }

    /**
     * @return List of default property values the domain object must have on
     *         creation.
     */
    public List<PropertyValue> getDefaultPropertyValues() {
        return default_property_values;
    }

    /**
     * Some property values may be supplied by the system automatically. Such
     * types of property are assumed to have an identifier known to the profile.
     * This allows the profile to map system supplied properties to domain
     * properties. An example is the size of a file associated with a node.
     * 
     * TODO Just list such properties with enum?
     * 
     * @return Mapping of property types to system identifiers.
     */
    public Map<PropertyType, URI> getSuppliedProperties() {
        return supplied_properties;
    }

    /**
     * @return Requirement about association with a regular file or null for no requirement.
     */
    public Requirement getFileAssociationRequirement() {
        return file_req;
    }

    /**
     * @return Requirement about association with a directory or null for no requirement.
     */
    public Requirement getDirectoryAssociationRequirement() {
        return dir_req;
    }

    /**
     * A node is more likely to be assigned to a node type if the preferences of
     * the node type are met.
     * 
     * @return Preferred number of children with file association or null for no
     *         preference.
     */
    public CardinalityConstraint getPreferredCountOfChildrenWithFiles() {
        return child_file_constraint;
    }

    /**
     * @param id The id to set.
     */
    public void setIdentifier(URI id) {
        this.id = id;
    }

    /**
     * @param domain_types The domain types to set.
     */
    public void setDomainTypes(List<URI> domain_types) {
        this.domain_types = domain_types;
    }

    /**
     * @param parent_constraints The parent constraints to set.
     */
    public void setParentConstraints(List<NodeConstraint> parent_constraints) {
        this.parent_constraints = parent_constraints;
    }

    /**
     * @param property_constraints The property constraints to set.
     */
    public void setPropertyConstraints(List<PropertyConstraint> property_constraints) {
        this.property_constraints = property_constraints;
    }

    /**
     * @param inheritable_properties The inheritable properties to set.
     */
    public void setInheritableProperties(List<PropertyType> inheritable_properties) {
        this.inheritable_properties = inheritable_properties;
    }

    /**
     * @param default_property_values The default_property_values to set.
     */
    public void setDefaultPropertyValues(List<PropertyValue> default_property_values) {
        this.default_property_values = default_property_values;
    }

    /**
     * @param supplied_properties The supplied_properties to set.
     */
    public void setSuppliedProperties(Map<PropertyType, URI> supplied_properties) {
        this.supplied_properties = supplied_properties;
    }

    /**
     * @param file_req the file_req to set
     */
    public void setFileAssocationRequirement(Requirement file_req) {
        this.file_req = file_req;
    }

    /**
     * @param dir_req the dir_req to set
     */
    public void setDirectoryAssocationRequirement(Requirement dir_req) {
        this.dir_req = dir_req;
    }

    /**
     * @param child_file_constraint the child_file_constraint to set
     */
    public void setChildFileConstraint(CardinalityConstraint child_file_constraint) {
        this.child_file_constraint = child_file_constraint;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((child_file_constraint == null) ? 0 : child_file_constraint.hashCode());
        result = prime * result + ((default_property_values == null) ? 0 : default_property_values.hashCode());
        result = prime * result + ((dir_req == null) ? 0 : dir_req.hashCode());
        result = prime * result + ((domain_types == null) ? 0 : domain_types.hashCode());
        result = prime * result + ((file_req == null) ? 0 : file_req.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((inheritable_properties == null) ? 0 : inheritable_properties.hashCode());
        result = prime * result + ((parent_constraints == null) ? 0 : parent_constraints.hashCode());
        result = prime * result + ((profile == null) ? 0 : profile.hashCode());
        result = prime * result + ((property_constraints == null) ? 0 : property_constraints.hashCode());
        result = prime * result + ((supplied_properties == null) ? 0 : supplied_properties.hashCode());
        return result;
    }

    /**
     * @param other
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof NodeType);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof NodeType))
            return false;
        NodeType other = (NodeType) obj;
        
        if (!other.canEqual(this))
            return false;
        
        if (child_file_constraint == null) {
            if (other.child_file_constraint != null)
                return false;
        } else if (!child_file_constraint.equals(other.child_file_constraint))
            return false;
        if (default_property_values == null) {
            if (other.default_property_values != null)
                return false;
        } else if (!default_property_values.equals(other.default_property_values))
            return false;
        if (dir_req != other.dir_req)
            return false;
        if (domain_types == null) {
            if (other.domain_types != null)
                return false;
        } else if (!domain_types.equals(other.domain_types))
            return false;
        if (file_req != other.file_req)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (inheritable_properties == null) {
            if (other.inheritable_properties != null)
                return false;
        } else if (!inheritable_properties.equals(other.inheritable_properties))
            return false;
        if (parent_constraints == null) {
            if (other.parent_constraints != null)
                return false;
        } else if (!parent_constraints.equals(other.parent_constraints))
            return false;
        if (profile == null) {
            if (other.profile != null)
                return false;
        } else if (!profile.equals(other.profile))
            return false;
        if (property_constraints == null) {
            if (other.property_constraints != null)
                return false;
        } else if (!property_constraints.equals(other.property_constraints))
            return false;
        if (supplied_properties == null) {
            if (other.supplied_properties != null)
                return false;
        } else if (!supplied_properties.equals(other.supplied_properties))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NodeType [id=" + id + ", profile=" + profile + ", domain_types=" + domain_types
                + ", parent_constraints=" + parent_constraints + ", property_constraints=" + property_constraints
                + ", inheritable_properties=" + inheritable_properties + ", default_property_values="
                + default_property_values + ", supplied_properties=" + supplied_properties + ", file_req=" + file_req
                + ", dir_req=" + dir_req + ", child_file_constraint=" + child_file_constraint + "]";
    }
}
