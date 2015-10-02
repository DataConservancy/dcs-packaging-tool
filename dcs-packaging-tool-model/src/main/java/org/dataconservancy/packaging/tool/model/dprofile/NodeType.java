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
public interface NodeType extends HasDescription {
    /**
     * @return Unique identifier of this node type.
     */
    URI getIdentifier();

    /**
     * @return The types of the corresponding domain object
     */
    List<URI> getDomainTypes();

    /**
     * The parent node must meet at least one of these constraints.
     * 
     * @return Constraints on parent node.
     */
    List<NodeConstraint> getParentConstraints();

    /**
     * @return Constraints on node properties.
     */
    List<PropertyConstraint> getPropertyConstraints();

    /**
     * @return Types of properties which may be inherited by descendants.
     */
    List<PropertyType> getInheritableProperties();

    /**
     * @return List of default property values the domain object must have on
     *         creation.
     */
    List<PropertyValue> getDefaultPropertyValues();

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
    Map<PropertyType, URI> getSuppliedProperties();

    /**
     * @return Requirement about association with a regular file.
     */
    Requirement getFileAssociationRequirement();

    /**
     * @return Requirement about association with a directory.
     */
    Requirement getDirectoryAssociationRequirement();

    /**
     * A node is more likely to be assigned to a node type if the preferences of
     * the node type are met.
     * 
     * @return Preferred number of children with file association or null for no
     *         preference.
     */
    CardinalityConstraint getPreferredCountOfChildrenWithFiles();
}
