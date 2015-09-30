package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * Represents a transform of a node.
 * 
 * Each transform has a source, a result, possibly an action, and a description.
 * A node having the characteristics of the source node may be transformed to
 * the result. The modification may be the type of the node or the predicates
 * connecting the node to its parent. The action may be inserting a parent node
 * or moving a node to its grandparent.
 */
public interface NodeTransform extends HasDescription {
    /**
     * @return Required node type of the transform source.
     */
    NodeType getSourceNodeType();

    /**
     * @return Required constraint on source parent. May be null to indicate no
     *         constraint.
     */
    NodeConstraint getSourceParentConstraint();

    /**
     * @return Required constraint on source grandparent. May be null to
     *         indicate no constraint.
     */
    NodeConstraint getSourceGrandParentConstraint();

    /**
     * @return Constraint that must be obeyed by every child.
     */
    NodeConstraint getSourceChildConstraint();

    /**
     * @return Node type of resulting node. May be null to indicate no change.
     */
    NodeType getResultNodeType();

    /**
     * If parent inserted, applies to inserted parent. If source moved to
     * grandparent, applies to grandparent.
     * 
     * @return Constraints that parent will be transformed to meet. May be null
     *         to indicate no change.
     */
    NodeConstraint getResultParentConstraint();

    /**
     * @return Whether or not result node has new parent inserted.
     */
    boolean insertParent();

    /**
     * Source node may be optionally moved to its grandparent. This may only
     * occur if the source node has a grandparent.
     * 
     * @return Whether or not source node is moved.
     */
    boolean moveResultToGrandParent();

    /**
     * If the source node is moved and the original parent of the source node
     * has no children, remove it.
     * 
     * @return Whether or not empty parent is removed.
     */
    boolean removeEmptyParent();
}
