package org.dataconservancy.packaging.tool.api;

import java.util.List;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Service to perform operations on a tree of domain objects described by a
 * domain profile.
 */
public interface DomainProfileService {
    /**
     * Add a property to a node.
     * 
     * @param node
     *            The node that the property will be added to.
     * @param prop
     *            The value of the property that should be added to the node.
     */
    void addProperty(Node node, Property prop);

    /**
     * Remove a particular property from a node.
     * 
     * @param node
     *            The node to remove the properties from.
     * @param prop
     *            The value of the property to remove.
     */
    void removeProperty(Node node, Property prop);

    /**
     * Remove all properties of a given type from a node.
     * 
     * @param node
     *            The node to remove properties from.
     * @param type
     *            The type of property that should be removed from the node.
     */
    void removeProperty(Node node, PropertyType type);

    /**
     * @param node
     *            The node whose properties should be returned.
     * @param type
     *            The type of node to get properties for or null for all node
     *            types.
     * @return All properties of a node.
     */
    List<Property> getProperties(Node node, NodeType type);

    /**
     * Gets all properties on the node of the given property type.
     * 
     * @param node
     *            The node whose properties should be returned.
     * @param type
     *            The type of node to get the properties for.
     * @return All properties of the given type on the node.
     */
    List<Property> getProperties(Node node, PropertyType type);

    /**
     * Check that all the properties on a node satisfy constraints for that node
     * type.
     * 
     * @param node
     *            The node whose properties are going to be validated.
     * @param type
     *            The type of node to get properties for or null for all node
     *            types.
     * @return valid or invalid
     */
    boolean validateProperties(Node node, NodeType type);

    /**
     * Remove domain object associated with node. The node itself is not
     * modified. Removing the domain object of a node which is not ignored
     * will make the tree invalid.
     * 
     * @param node
     *            The node whose domain object should be removed.
     */
    void removeDomainObject(Node node);

    /**
     * Transform a node. The tree must be valid before a transform and will be
     * valid after.
     * 
     * @param node
     *            The node that is going to be transformed.
     * @param trans
     *            The node transform to perform on the passed in node.
     */
    void transformNode(Node node, NodeTransform trans);

    /**
     * @param node
     *            The node to get the available transforms for.
     * @return All node transforms able to be performed on the node.
     */
    List<NodeTransform> getNodeTransforms(Node node);

    /**
     * Check if a tree satisfies all its node constraints. Such a tree must also
     * have domain objects with the correct relations. A tree is invalid if the
     * root is an ignored node. Otherwise ignored nodes are not considered when
     * checking validity.
     * 
     * @param root
     *            The root node of the tree to validate.
     * @return valid or invalid
     */
    boolean validateTree(Node root);

    /**
     * Attempt to assign node types to a tree such that it is valid with respect
     * to node types. Only the node and its descendants will have types
     * assigned. Ignored nodes do not have types assigned and are treated as if
     * they do not exist.
     * 
     * The parent of the node must either not exist or be part of a valid tree.
     * 
     * On success, domain objects will be created if they do not exist or
     * updated with the new type if they do. On failure, node types may be
     * assigned, but no domain objects will be modified.
     * 
     * @param profile
     *            The profile used to assign types to the nodes.
     * @param node
     *            The root node of the tree to assign types to.
     * @return success or failure
     */
    boolean assignNodeTypes(DomainProfile profile, Node node);
}
