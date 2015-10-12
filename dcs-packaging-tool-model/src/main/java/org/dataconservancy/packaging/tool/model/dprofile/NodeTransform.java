package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * Represents a transform of a node.Each transform has a source, a result,
 * possibly an action, and a description.
 * 
 * A node having the characteristics of the source node may be transformed to
 * the result. The modification may be the type of the node or the predicates
 * connecting the node to its parent. The action may be inserting a parent node
 * or moving a node to its grandparent.
 */
public class NodeTransform extends AbstractDescribedObject {
    private NodeType source_type;
    private NodeConstraint source_parent_constraint;
    private NodeConstraint source_grandparent_constraint;
    private NodeConstraint source_child_constraint;
    private NodeType result_node_type;
    private NodeConstraint result_parent_constraint;
    private boolean insert_parent;
    private boolean move_result_grandparent;
    private boolean remove_empty_parent;
    
    /**
     * @return Required node type of the transform source.
     */
    public NodeType getSourceNodeType() {
        return source_type;
    }

    /**
     * @return Required constraint on source parent. May be null to indicate no
     *         constraint.
     */
    public NodeConstraint getSourceParentConstraint() {
        return source_parent_constraint;
    }

    /**
     * @return Required constraint on source grandparent. May be null to
     *         indicate no constraint.
     */
    public NodeConstraint getSourceGrandParentConstraint() {
        return source_grandparent_constraint;
    }

    /**
     * @return Constraint that must be obeyed by every child.
     */
    public NodeConstraint getSourceChildConstraint() {
        return source_child_constraint;
    }

    /**
     * @return Node type of resulting node. May be null to indicate no change.
     */
    public NodeType getResultNodeType() {
        return result_node_type;
    }

    /**
     * If parent inserted, applies to inserted parent. If source moved to
     * grandparent, applies to grandparent.
     * 
     * @return Constraints that parent will be transformed to meet. May be null
     *         to indicate no change.
     */
    public NodeConstraint getResultParentConstraint() {
        return result_parent_constraint;
    }

    /**
     * @return Whether or not result node has new parent inserted.
     */
    public boolean insertParent() {
        return insert_parent;
    }

    /**
     * Source node may be optionally moved to its grandparent. This may only
     * occur if the source node has a grandparent.
     * 
     * @return Whether or not source node is moved.
     */
    public boolean moveResultToGrandParent() {
        return move_result_grandparent;
    }

    /**
     * If the source node is moved and the original parent of the source node
     * has no children, remove it.
     * 
     * @return Whether or not empty parent is removed.
     */
    public boolean removeEmptyParent() {
        return remove_empty_parent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (insert_parent ? 1231 : 1237);
        result = prime * result + (move_result_grandparent ? 1231 : 1237);
        result = prime * result + (remove_empty_parent ? 1231 : 1237);
        result = prime * result + ((result_node_type == null) ? 0 : result_node_type.hashCode());
        result = prime * result + ((result_parent_constraint == null) ? 0 : result_parent_constraint.hashCode());
        result = prime * result + ((source_child_constraint == null) ? 0 : source_child_constraint.hashCode());
        result = prime * result
                + ((source_grandparent_constraint == null) ? 0 : source_grandparent_constraint.hashCode());
        result = prime * result + ((source_parent_constraint == null) ? 0 : source_parent_constraint.hashCode());
        result = prime * result + ((source_type == null) ? 0 : source_type.hashCode());
        return result;
    }

    /**
     * @param other 
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof NodeTransform);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof NodeTransform))
            return false;
        NodeTransform other = (NodeTransform) obj;
        
        if (!other.canEqual(this))
            return false;
        
        if (insert_parent != other.insert_parent)
            return false;
        if (move_result_grandparent != other.move_result_grandparent)
            return false;
        if (remove_empty_parent != other.remove_empty_parent)
            return false;
        if (result_node_type == null) {
            if (other.result_node_type != null)
                return false;
        } else if (!result_node_type.equals(other.result_node_type))
            return false;
        if (result_parent_constraint == null) {
            if (other.result_parent_constraint != null)
                return false;
        } else if (!result_parent_constraint.equals(other.result_parent_constraint))
            return false;
        if (source_child_constraint == null) {
            if (other.source_child_constraint != null)
                return false;
        } else if (!source_child_constraint.equals(other.source_child_constraint))
            return false;
        if (source_grandparent_constraint == null) {
            if (other.source_grandparent_constraint != null)
                return false;
        } else if (!source_grandparent_constraint.equals(other.source_grandparent_constraint))
            return false;
        if (source_parent_constraint == null) {
            if (other.source_parent_constraint != null)
                return false;
        } else if (!source_parent_constraint.equals(other.source_parent_constraint))
            return false;
        if (source_type == null) {
            if (other.source_type != null)
                return false;
        } else if (!source_type.equals(other.source_type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NodeTransform [source_type=" + source_type + ", source_parent_constraint=" + source_parent_constraint
                + ", source_grandparent_constraint=" + source_grandparent_constraint + ", source_child_constraint="
                + source_child_constraint + ", result_node_type=" + result_node_type + ", result_parent_constraint="
                + result_parent_constraint + ", insert_parent=" + insert_parent + ", move_result_grandparent="
                + move_result_grandparent + ", remove_empty_parent=" + remove_empty_parent + "]";
    }
}