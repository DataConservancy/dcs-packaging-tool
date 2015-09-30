package org.dataconservancy.packaging.tool.api;

import java.nio.file.Path;
import java.util.List;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
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
     * @param node
     * @param value
     */
    void addProperty(Node node, PropertyValue value);

    /**
     * Remove a particular property from a node.
     * 
     * @param node
     * @param value
     */
    void removeProperty(Node node, PropertyValue value);

    /**
     * Remove all properties of a given type from a node.
     * 
     * @param node
     * @param type
     */
    void removeProperty(Node node, PropertyType type);

    /**
     * @param node
     * @return All properties of a node.
     */
    List<PropertyValue> getProperties(Node node);

    /**
     * @param node
     * @param type
     * @return All properties of a node of a certain type.
     */
    List<PropertyValue> getProperties(Node node, PropertyType type);

    /**
     * Check that all the properties on a node satisfy constraints for that node
     * type.
     * 
     * @param node
     * @return valid or invalid
     */
    boolean validateProperties(Node node);

    /**
     * @return Constraints on node properties.
     */
    List<PropertyConstraint> getPropertyConstraints(Node node);

    /**
     * Format property value as a string according to its type and hint.
     * 
     * @param value
     * @return Formatted property value
     */
    String formatPropertyValue(PropertyValue value);

    /**
     * Attempt to parse a string into a property value according to its type and
     * hint.
     * 
     * @param type
     * @param value
     * @return value on success and null on failure
     */
    PropertyValue parsePropertyValue(PropertyType type, String value);

    /**
     * Transform a node. The tree must be valid before a transform and will be
     * valid after.
     * 
     * @param node
     * @param trans
     */
    void transformNode(Node node, NodeTransform trans);

    /**
     * @param node
     * @return All node transforms able to be performed on the node.
     */
    List<NodeTransform> getNodeTransforms(Node node);

    /**
     * Check if a tree satisfies all its node and optionally property
     * constraints.
     * 
     * @param root
     * @param check_properties
     * @return valid or invalid
     */
    boolean validateTree(Node root, boolean check_properties);

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
     * @param node
     * @return success or failure
     */
    boolean assignNodeTypes(Node node);

    /**
     * Create a tree from the file system. The tree has file data associated
     * with it, but does not have types assigned to nodes.
     * 
     * @param path
     * @return root of tree
     */
    Node createTreeFromFileSystem(Path path);

    /**
     * Change the ignored status of a node. This may cause the types of other
     * nodes to change.
     * 
     * If a node is marked as ignored, all descendants are also marked as
     * ignored.
     * 
     * If a node is marked as not ignored, all descendants and ancestors are
     * also marked as not ignored.
     * 
     * @param node
     * @param status
     */
    void ignoreNode(Node node, boolean status);
    
    
    /**
     * TODO: Needed? Is this done automatically?
     * 
     * Copy inheritable properties from this node to descendants.
     * 
     * @param node
     */
    void propagateInheritedProperties(Node node);
}
