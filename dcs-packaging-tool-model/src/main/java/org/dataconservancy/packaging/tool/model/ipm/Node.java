package org.dataconservancy.packaging.tool.model.ipm;

import java.net.URI;
import java.util.List;

/**
 * A node in the PTG tree which the user views and manipulates. The tree is a
 * DAG. The tree must contain at least one node, the root node.
 * 
 * Each node is associated with a domain object, file data, and a type. The type
 * of the node constrains its position in the tree and explains how it is mapped
 * to a type of domain object.
 * 
 * 
 */
public interface Node {
    /**
     * @return Unique identifier of the node in the tree.
     */
    URI getIdentifier();

    /**
     * Set unique identifier of the node in the tree.
     * 
     * @param id
     */
    void setIdentifier(URI id);

    /**
     * @return Parent node or null if node is root.
     */
    Node getParent();

    /**
     * Set the parent node.
     * 
     * @param parent
     */
    void setParent(Node parent);

    /**
     * @return Children of node as a list that should not be modified.
     */
    List<Node> getChildren();

    /**
     * Add a child node. The parent of the child node will be set to this node.
     * 
     * @param node
     */
    void addChild(Node node);

    /**
     * Remove a child node.
     * 
     * @param node
     */
    void removeChild(Node node);

    /**
     * @return Identifier of domain object associated with node.
     */
    URI getDomainObject();

    /**
     * Set identifier of domain object associated with node.
     * 
     * @param id
     */
    void setDomainObject(URI id);

    /**
     * @return Information about the file associated with the node.
     */

    FileInfo getFileInfo();

    /**
     * Set information about file associated with node.
     * 
     * @param info
     */
    void setFileInfo(FileInfo info);

    /**
     * @return Type of the node.
     */
    URI getNodeType();

    /**
     * Set the type of the node.
     * 
     * @param type
     */
    void setNodeType(URI type);

    /**
     * @return Ignored status.
     */
    boolean isIgnored();

    /**
     * Set ignored status.
     * 
     * @param status
     */
    void setIgnored(boolean status);

    /**
     * @return Whether or not the node is a leaf.
     */
    boolean isLeaf();

    /**
     * @return Whether or not node is the root of the tree.
     */
    boolean isRoot();
}
