package org.dataconservancy.packaging.tool.model.dprofile;

import org.apache.commons.collections.CollectionUtils;

import java.net.URI;
import java.util.HashSet;
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
    private List<Property> default_property_values;
    private Map<PropertyType, SuppliedProperty> supplied_properties;
    private FileAssociation file_assoc;
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
     * If there are no constraint the node must be the root node.
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
    public List<Property> getDefaultPropertyValues() {
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
    public Map<PropertyType, SuppliedProperty> getSuppliedProperties() {
        return supplied_properties;
    }

    /**
     * @return Required association with a file
     */
    public FileAssociation getFileAssociation() {
        return file_assoc;
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
    public void setDefaultPropertyValues(List<Property> default_property_values) {
        this.default_property_values = default_property_values;
    }

    /**
     * @param supplied_properties The supplied properties to set.
     */
    public void setSuppliedProperties(Map<PropertyType, SuppliedProperty> supplied_properties) {
        this.supplied_properties = supplied_properties;
    }

    /**
     * @param file_assoc The file association to set.
     */
    public void setFileAssociation(FileAssociation file_assoc) {
        this.file_assoc = file_assoc;
    }

    /**
     * @param child_file_constraint the child_file_constraint to set
     */
    public void setChildFileConstraint(CardinalityConstraint child_file_constraint) {
        this.child_file_constraint = child_file_constraint;
    }

    /**
     * Sets the DomainProfile that this NodeType belongs to.
     * @param profile The DomainProfile this NodeType corresponds to.
     */
    public void setDomainProfile(DomainProfile profile) {
        this.profile = profile;
    }

    /**
     * Calculates the HashCode of the NodeType.
     * Note: Lists are converted to HashSets in this method to make them order independent.
     * @return The hashcode of the NodeType.
     */
    @Override
    public int hashCode() {
        HashSet<URI> domainTypeSet = null;
        if (domain_types != null) {
            domainTypeSet = new HashSet<>();
            domainTypeSet.addAll(domain_types);
        }

        HashSet<NodeConstraint> parentConstraintSet = null;
        if (parent_constraints != null) {
            parentConstraintSet = new HashSet<>();
            parentConstraintSet.addAll(parent_constraints);
        }

        HashSet<PropertyConstraint> propertyConstraintSet = null;
        if (property_constraints != null) {
            propertyConstraintSet = new HashSet<>();
            propertyConstraintSet.addAll(property_constraints);
        }

        HashSet<PropertyType> inheritablePropertySet = null;
        if (inheritable_properties != null) {
            inheritablePropertySet = new HashSet<>();
            inheritablePropertySet.addAll(inheritable_properties);
        }

        HashSet<Property> defaultPropertyValueSet = null;
        if (default_property_values != null) {
            defaultPropertyValueSet = new HashSet<>();
            defaultPropertyValueSet.addAll(default_property_values);
        }

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((child_file_constraint == null) ? 0 : child_file_constraint.hashCode());
        result = prime * result + ((defaultPropertyValueSet == null) ? 0 : defaultPropertyValueSet.hashCode());
        result = prime * result + ((domainTypeSet == null) ? 0 : domainTypeSet.hashCode());
        result = prime * result + ((file_assoc == null) ? 0 : file_assoc.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((inheritablePropertySet == null) ? 0 : inheritablePropertySet.hashCode());
        result = prime * result + ((parentConstraintSet == null) ? 0 : parentConstraintSet.hashCode());
        result = prime * result + ((profile == null || profile.getIdentifier() == null) ? 0 : profile.getIdentifier().hashCode());
        result = prime * result + ((propertyConstraintSet == null) ? 0 : propertyConstraintSet.hashCode());
        result = prime * result + ((supplied_properties == null) ? 0 : supplied_properties.hashCode());
        return result;
    }

    /**
     * @param other The object to compare
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
        if (domain_types == null) {
            if (other.domain_types != null)
                return false;
        } else if (other.domain_types == null || !CollectionUtils.isEqualCollection(domain_types, other.domain_types))
            return false;
        if (file_assoc != other.file_assoc)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (inheritable_properties == null) {
            if (other.inheritable_properties != null)
                return false;
        } else if (other.inheritable_properties == null ||!CollectionUtils.isEqualCollection(inheritable_properties, other.inheritable_properties))
            return false;
        if (parent_constraints == null) {
            if (other.parent_constraints != null)
                return false;
        } else if (other.parent_constraints == null || !CollectionUtils.isEqualCollection(parent_constraints, other.parent_constraints))
            return false;
        if (profile == null) {
            if (other.profile != null)
                return false;
        } else if (profile.getIdentifier() == null) {
            if (other.profile.getIdentifier() != null)
                return  false;
        } else if (other.profile == null || other.profile.getIdentifier() == null || !profile.getIdentifier().equals(other.profile.getIdentifier()))
            return false;
        if (property_constraints == null) {
            if (other.property_constraints != null)
                return false;
        } else if (other.property_constraints == null || !CollectionUtils.isEqualCollection(property_constraints, other.property_constraints))
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
                + default_property_values + ", supplied_properties=" + supplied_properties + ", file_assoc=" + file_assoc
                + ", child_file_constraint=" + child_file_constraint + "]";
    }
}
