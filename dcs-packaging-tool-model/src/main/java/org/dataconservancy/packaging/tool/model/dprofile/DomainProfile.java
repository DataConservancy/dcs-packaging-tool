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
public interface DomainProfile extends HasDescription {
    /**
     * @return Identifier of profile
     */
    URI getIdentifier();

    /**
     * @return Identifier of profile domain
     */
    URI getDomainIdentifier();

    /**
     * @return All node types.
     */
    List<NodeType> getNodeTypes();

    /**
     * @return All property types.
     */
    List<PropertyType> getPropertyTypes();

    /**
     * @return All property categories.
     */
    List<PropertyCategory> getPropertyCategories();
    
    /**
     * @return All available node transforms.
     */
    List<NodeTransform> getNodeTransforms();
}
