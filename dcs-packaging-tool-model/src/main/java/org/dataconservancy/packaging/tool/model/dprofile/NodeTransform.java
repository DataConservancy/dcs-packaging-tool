package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * Represents a transform of a node Each transform has a source, a result,
 * possibly an action, and a description. The result of the transform must
 * result in a valid tree.
 * 
 * A node having meeting all the specified constraints on the source node may be
 * transformed to the result node. The source node must have the specified type.
 * If a constraint is specified for the source parent, it must be met. If a
 * constraint is specified on the source node children, it must be met by all
 * children.
 * 
 * The node and its children may have their types changed. Changing the type of
 * a node will change the relations between the node and its parent and children
 * to allowed structural relations of the new node type.
 * 
 * In addition actions can be performed on the structure of the tree. A parent
 * node may be inserted. Child nodes may be moved to their parent. If the
 * resulting node is a leaf, it may be removed.
 */
public class NodeTransform extends AbstractDescribedObject {
    private NodeType source_type;
    private NodeConstraint source_parent_constraint;
    private NodeConstraint source_child_constraint;
    private NodeType result_node_type;
    private NodeType result_child_node_type;
    private NodeType insert_parent_node_type;
    private boolean move_children_to_parent;
    private boolean remove_empty_result;

    /**
     * @return Required node type of the transform source.
     */
    public NodeType getSourceNodeType() {
        return source_type;
    }

    /**
     * Sets the NodeType of the transform source.
     * 
     * @param sourceNodeType
     *            The NodeType of the transform source.
     */
    public void setSourceNodeType(NodeType sourceNodeType) {
        this.source_type = sourceNodeType;
    }

    /**
     * @return Required constraint on source parent. May be null to indicate no
     *         constraint.
     */
    public NodeConstraint getSourceParentConstraint() {
        return source_parent_constraint;
    }

    /**
     * Sets the NodeConstraint of the source parent.
     * 
     * @param sourceParentConstraint
     *            The NodeConstraint on the source parent.
     */
    public void setSourceParentConstraint(NodeConstraint sourceParentConstraint) {
        this.source_parent_constraint = sourceParentConstraint;
    }

    /**
     * @return Constraint that must be obeyed by every child.
     */
    public NodeConstraint getSourceChildConstraint() {
        return source_child_constraint;
    }

    /**
     * Sets the NodeConstraint of the source child.
     * 
     * @param childConstraint
     *            The NodeConstraint of the source child.
     */
    public void setSourceChildConstraint(NodeConstraint childConstraint) {
        this.source_child_constraint = childConstraint;
    }

    /**
     * @return Node type of resulting node. May be null to indicate no change.
     */
    public NodeType getResultNodeType() {
        return result_node_type;
    }

    /**
     * Sets the NodeType of the result of transform .
     * 
     * @param resultNodeType
     *            The NodeType of the result of the transform.
     */
    public void setResultNodeType(NodeType resultNodeType) {
        this.result_node_type = resultNodeType;
    }

    /**
     * @return Whether or not source node children of source node are moved to
     *         parent.
     */
    public boolean moveChildrenToParent() {
        return move_children_to_parent;
    }

    /**
     * @param status
     *            Boolean flag on whether or not children of source node should
     *            be moved to parent.
     */
    public void setMoveChildrenToParent(boolean status) {
        this.move_children_to_parent = status;
    }

    /**
     * @return Whether or not result nodes with no children are removed.
     */
    public boolean removeEmptyResult() {
        return remove_empty_result;
    }

    /**
     * @param status
     *            A boolean flag that states whether or not result nodes with no
     *            children are removed.
     */
    public void setRemoveEmptyResult(boolean status) {
        this.remove_empty_result = status;
    }

    /**
     * @param other
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof NodeTransform);
    }

    /**
     * @return The type of parent node to insert or null for no insertion.
     */
    public NodeType getInsertParentNodeType() {
        return insert_parent_node_type;
    }

    /**
     * @param type
     *            The type of parent node to insert or null for no insertion.
     */
    public void setInsertParentNodeType(NodeType type) {
        this.insert_parent_node_type = type;
    }

    /**
     * @return Type to transform children to or null for no transform.
     */
    public NodeType getResultChildNodeType() {
        return result_child_node_type;
    }

    /**
     * @param result_child_node_type
     *            Type to transform children to or null for no transform.
     */
    public void setResultChildNodeType(NodeType result_child_node_type) {
        this.result_child_node_type = result_child_node_type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((insert_parent_node_type == null) ? 0 : insert_parent_node_type.hashCode());
        result = prime * result + (move_children_to_parent ? 1231 : 1237);
        result = prime * result + (remove_empty_result ? 1231 : 1237);
        result = prime * result + ((result_child_node_type == null) ? 0 : result_child_node_type.hashCode());
        result = prime * result + ((result_node_type == null) ? 0 : result_node_type.hashCode());
        result = prime * result + ((source_child_constraint == null) ? 0 : source_child_constraint.hashCode());
        result = prime * result + ((source_parent_constraint == null) ? 0 : source_parent_constraint.hashCode());
        result = prime * result + ((source_type == null) ? 0 : source_type.hashCode());
        return result;
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

        if (insert_parent_node_type == null) {
            if (other.insert_parent_node_type != null)
                return false;
        } else if (!insert_parent_node_type.equals(other.insert_parent_node_type))
            return false;
        if (move_children_to_parent != other.move_children_to_parent)
            return false;
        if (remove_empty_result != other.remove_empty_result)
            return false;
        if (result_child_node_type == null) {
            if (other.result_child_node_type != null)
                return false;
        } else if (!result_child_node_type.equals(other.result_child_node_type))
            return false;
        if (result_node_type == null) {
            if (other.result_node_type != null)
                return false;
        } else if (!result_node_type.equals(other.result_node_type))
            return false;
        if (source_child_constraint == null) {
            if (other.source_child_constraint != null)
                return false;
        } else if (!source_child_constraint.equals(other.source_child_constraint))
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
        return "NodeTransform [source_type=" + source_type.getIdentifier() + ", source_parent_constraint="
                + source_parent_constraint + ", source_child_constraint=" + source_child_constraint
                + ", result_node_type=" + result_node_type.getIdentifier() + ", result_child_node_type=" + result_child_node_type.getIdentifier()
                + ", insert_parent_node_type=" + insert_parent_node_type.getIdentifier() + ", move_children_to_parent="
                + move_children_to_parent + ", remove_empty_result=" + remove_empty_result + "]";
    }
}
