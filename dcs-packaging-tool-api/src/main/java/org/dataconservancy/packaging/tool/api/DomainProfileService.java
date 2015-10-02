package org.dataconservancy.packaging.tool.api;

import java.nio.file.Path;
import java.util.List;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
import org.dataconservancy.packaging.tool.model.ipm.ComparisonNode;
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
     * @return All properties of a node.
     */
    List<PropertyValue> getProperties(Node node);

    /**
     * @param node The node to get the properties for.
     * @param type The type of the property that should be retrieved for the node.
     * @return All properties of a node of a certain type.
     */
    List<PropertyValue> getProperties(Node node, PropertyType type);

    /**
     * Check that all the properties on a node satisfy constraints for that node
     * type.
     * 
     * @param node The node whose properties are going to be valided.
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
     * @param value The value of the property to format.
     * @return Formatted property value
     */
    String formatPropertyValue(PropertyValue value);

    /**
     * Attempt to parse a string into a property value according to its type and
     * hint.
     * 
     * @param type The type of the property that's going to be parsed.
     * @param value The value of the property to be parsed.
     * @return value on success and null on failure
     */
    PropertyValue parsePropertyValue(PropertyType type, String value);

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
     * @param check_properties True if properties should be validated, false if only the tree structure and types should be validated
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
     * @param node The root node of the tree to assign types to.
     * @return success or failure
     */
    boolean assignNodeTypes(Node node);

    /**
     * Create a tree from the file system. The tree has file data associated
     * with it, but does not have types assigned to nodes.
     * 
     * @param path The path on the file system to create a node tree from.
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
     * @param node The node to change the ignored status for.
     * @param status The new status of the node.
     */
    void ignoreNode(Node node, boolean status);

    /**
     * Checks that the file backing the node is accessible on the file system.
     * Note that this method makes no assertion that the file or folder pointed to was the original one, just that something exists
     * at the file location stored in the nodes file info.
     * @param node The node to check if it's file info file location is still accessible.
     * @return true if the file is accessible false otherwise.
     */
    boolean checkFileInfoIsAccessible(Node node);

    /**
     * TODO: This may not be needed as one could just update the file info object on the node directly.
     *
     * Updates the FileInfo object on the node, when the file or folder backing the node has changed.
     * @param node The node to update the file information for.
     * @param info The new file info to set on the node.
     */
    void updateFileInfo(Node node, FileInfo info);

    /**
     * TODO: Needed? Is this done automatically?
     * 
     * Copy inheritable properties from this node to descendants.
     * 
     * @param node The node to propagate properties to from it's parent.
     */
    void propagateInheritedProperties(Node node);

    /**
     * Compares the three provided under the existing tree root node with the tree under the comparison tree root node.
     * @param existingTree The root node of the existing tree to compare.
     * @param comparisonTree The root node of the new tree to compare against the existing tree.
     * @return The root node of the of the tree showing the results of the comparison.
     */
    ComparisonNode compareTree(Node existingTree, Node comparisonTree);

    /**
     * Merges the provided comparison tree into the existing tree.
     * @param existingTree The existing tree that will recieve the results of the merge.
     * @param comparisonTree The comparison tree to merge into the existing tree.
     */
    void mergeTree(Node existingTree, ComparisonNode comparisonTree);
}
