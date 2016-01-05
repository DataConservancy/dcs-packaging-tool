package org.dataconservancy.packaging.tool.model.dprofile;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * Represents a transform of a node Each transform has a source, a result,
 * possibly an action, and a description. The result of the transform must
 * result in a valid tree.
 * 
 * A node having meeting all the specified constraints on the source node may be
 * transformed to the result node. The source node must have the specified type.
 * If a constraint is specified for the source parent, it must be met. If
 * constraints are specified on the source node children, every child must meet
 * at least one of them.
 * 
 * The node and its children may have their types changed. Changing the type of
 * a node will change the relations between the node and its parent and children
 * to allowed structural relations of the new node type.
 * 
 * Actions can be performed on the structure of the tree. A parent node may be
 * inserted. Child nodes may be moved to their parent. If the resulting node is
 * a leaf, it may be removed.
 * 
 * Finally any children of the result node which match a child transform, are
 * themselves transformed. Child transforms are checked after the node type and
 * structure changes are made. A child may only undergo one transform. The tree
 * may not be valid at that time, but must be valid after the child transforms
 * are performed.
 */
public class NodeTransform extends AbstractDescribedObject {
    private NodeType source_type;
    private NodeConstraint source_parent_constraint;
    private List<NodeConstraint> source_child_constraints;
    private List<NodeTransform> result_child_transforms;
    private NodeType result_node_type;
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
     * If child constraints are null or empty, they are ignored.
     * 
     * @return Every child must meet at least one constraint.
     */
    public List<NodeConstraint> getSourceChildConstraints() {
        return source_child_constraints;
    }

    /**
     * Set constraints on children of the source node. If child constraints are
     * null or empty, they are ignored. Otherwise every child must meet at least
     * one constraint.
     * 
     * @param childConstraints
     *            The NodeConstraints of the source child.
     */
    public void setSourceChildConstraints(List<NodeConstraint> childConstraints) {
        this.source_child_constraints = childConstraints;
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
     * @param other The other node transform to compare.
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((insert_parent_node_type == null) ? 0 : insert_parent_node_type.hashCode());
        result = prime * result + (move_children_to_parent ? 1231 : 1237);
        result = prime * result + (remove_empty_result ? 1231 : 1237);
        result = prime * result + ((result_node_type == null) ? 0 : result_node_type.hashCode());
        result = prime * result
                + ((source_child_constraints == null) ? 0 : new HashSet<>(source_child_constraints).hashCode());
        result = prime * result + ((source_parent_constraint == null) ? 0 : source_parent_constraint.hashCode());
        result = prime * result + ((source_type == null) ? 0 : source_type.hashCode());
        result = prime * result + ((result_child_transforms == null) ? 0 : new HashSet<>(result_child_transforms).hashCode());
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
        if (result_node_type == null) {
            if (other.result_node_type != null)
                return false;
        } else if (!result_node_type.equals(other.result_node_type))
            return false;
        if (result_child_transforms == null) {
            if (other.result_child_transforms != null)
                return false;
        } else if (other.result_child_transforms == null
                || !CollectionUtils.isEqualCollection(result_child_transforms, other.result_child_transforms))
            return false;
        if (source_child_constraints == null) {
            if (other.source_child_constraints != null)
                return false;
        } else if (other.source_child_constraints == null
                || !CollectionUtils.isEqualCollection(source_child_constraints, other.source_child_constraints))
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
                + source_parent_constraint + ", source_child_constraints="
                + (source_child_constraints == null ? "null" : source_child_constraints) + ", result_node_type="
                + result_node_type.getIdentifier() + ", insert_parent_node_type="
                + insert_parent_node_type.getIdentifier() + ", move_children_to_parent=" + move_children_to_parent
                + ", remove_empty_result=" + remove_empty_result + "]";
    }

    /**
     * @return Transforms that are applied to children of the result node.
     */
    public List<NodeTransform> getResultChildTransforms() {
        return result_child_transforms;
    }

    /**
     * Transforms which match a child of the result node are applied to that
     * child.
     * 
     * @param result_child_transforms
     *            Set transforms on result node children.
     */
    public void setResultChildTransforms(List<NodeTransform> result_child_transforms) {
        this.result_child_transforms = result_child_transforms;
    }
}
