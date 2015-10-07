package org.dataconservancy.packaging.tool.api;

import java.util.List;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Service to perform operations on a tree of domain objects using a domain
 * profile.
 */
public interface DomainProfileService {
    /**
     * @return Domain profile used by service.
     */
    DomainProfile getDomainProfile();

    /**
     * Add a property to a node.
     * 
     * @param node The node that the property will be added to.
     * @param value The value of the property that should be added to the node.
     */
    void addProperty(Node node, PropertyValue value);

    /**
     * Remove a particular property from a node.
     * 
     * @param node The node to remove the properties from.
     * @param value The value of the property to remove.
     */
    void removeProperty(Node node, PropertyValue value);

    /**
     * Remove all properties of a given type from a node.
     * 
     * @param node The node to remove properties from.
     * @param type The type of property that should be removed from the node.
     */
    void removeProperty(Node node, PropertyType type);

    /**
     * @param node The node whose properties should be returned.
     * @param type The type of node to get properties for or null for all node types.
     * @return All properties of a node.
     */
    List<PropertyValue> getProperties(Node node, NodeType type);

    /**
     * Check that all the properties on a node satisfy constraints for that node
     * type.
     * 
     * @param node The node whose properties are going to be validated.
     * @param type The type of node to get properties for or null for all node types.
     * @return valid or invalid
     */
    boolean validateProperties(Node node, NodeType type);

    /**
     * Transform a node. The tree must be valid before a transform and will be
     * valid after.
     * 
     * @param node The node that is going to be transformed.
     * @param trans The node transform to perform on the passed in node.
     */
    void transformNode(Node node, NodeTransform trans);

    /**
     * @param node The node to get the available transforms for.
     * @return All node transforms able to be performed on the node.
     */
    List<NodeTransform> getNodeTransforms(Node node);

    /**
     * Check if a tree satisfies all its node and optionally property
     * constraints.
     * 
     * @param root The root node of the tree to validate.
     * @return valid or invalid
     */
    boolean validateTree(Node root);

    /**
     * Attempt to assign node types to a tree such that it is valid with respect
     * to node types. Only the node and its descendants will have types
     * assigned.
     * 
     * The parent of the node must either not exist or be part of a valid tree.
     * 
     * On success, domain objects will be created if they do not exist or
     * updated with the new type if they do.
     * 
     * @param node The root node of the tree to assign types to.
     * @return success or failure
     */
    boolean assignNodeTypes(Node node);

    /**
     * Copy inheritable properties from this node to descendants.
     * 
     * @param node The node to propagate properties to from it's parent.
     */
    void propagateInheritedProperties(Node node);
}
