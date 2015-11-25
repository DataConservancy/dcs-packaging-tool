package org.dataconservancy.packaging.tool.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.model.dprofile.CardinalityConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.FileAssociation;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

public class DomainProfileServiceImpl implements DomainProfileService {
    private final DomainProfileObjectStore objstore;
    private final URIGenerator urigen;

    public DomainProfileServiceImpl(DomainProfileObjectStore objstore, URIGenerator urigen) {
        this.objstore = objstore;
        this.urigen = urigen;
    }

    @Override
    public void addProperty(Node node, Property value) {
        if (node.getDomainObject() == null) {
            throw new IllegalArgumentException("Node does not have domain object.");
        }

        objstore.addProperty(node.getDomainObject(), value);
    }

    @Override
    public void removeProperty(Node node, Property value) {
        if (node.getDomainObject() == null) {
            throw new IllegalArgumentException("Node does not have domain object.");
        }

        objstore.removeProperty(node.getDomainObject(), value);
    }

    @Override
    public void removeProperty(Node node, PropertyType type) {
        if (node.getDomainObject() == null) {
            throw new IllegalArgumentException("Node does not have domain object.");
        }

        objstore.removeProperty(node.getDomainObject(), type);
    }

    @Override
    public List<Property> getProperties(Node node, NodeType type) {
        if (node.getDomainObject() == null) {
            throw new IllegalArgumentException("Node does not have domain object.");
        }

        return objstore.getProperties(node.getDomainObject(), type);
    }

    @Override
    public List<Property> getProperties(Node node, PropertyType type) {
        if (node.getDomainObject() == null) {
            throw new IllegalArgumentException("Node does not have domain object.");
        }

        return objstore.getProperties(node.getDomainObject(), type);
    }

    @Override
    public List<PropertyConstraint> validateProperties(Node node, NodeType type) {
        List<PropertyConstraint> constraintViolations = new ArrayList<>();

        if (!node.isIgnored()) {
            if (node.getDomainObject() == null) {
                throw new IllegalArgumentException("Node does not have domain object.");
            }

            for (PropertyConstraint pc : type.getPropertyConstraints()) {
                PropertyType prop_type = pc.getPropertyType();

                List<Property> vals = objstore.getProperties(node.getDomainObject(), prop_type);

                if (vals.size() < pc.getMinimum() ||
                    (pc.getMaximum() != -1 && vals.size() > pc.getMaximum())) {
                    constraintViolations.add(pc);
                }

                if (!validate_complex_property_cardinality(vals)) {
                    constraintViolations.add(pc);
                }
            }
        }
        return constraintViolations;
    }

    // Check cardinality of complex properties in list.
    private boolean validate_complex_property_cardinality(List<Property> vals) {
        if (vals == null) {
            return true;
        }

        for (Property val : vals) {
            if (val.getPropertyType().getPropertyValueType() == PropertyValueType.COMPLEX) {
                if (!validate_complex_property_cardinality(val)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validate_complex_property_cardinality(Property prop) {
        // Check cardinality of sub-property for each constraint

        for (PropertyConstraint pc : prop.getPropertyType().getComplexPropertyConstraints()) {
            int count = 0;

            if (prop.getComplexValue() != null) {
                for (Property subprop : prop.getComplexValue()) {
                    if (subprop.getPropertyType().equals(pc.getPropertyType())) {
                        count++;
                    }
                }
            }

            if (count < pc.getMinimum() || (pc.getMaximum() != -1 && count > pc.getMaximum())) {
                return false;
            }
        }

        // Recurse to check sub-properties that are themselves complex

        return validate_complex_property_cardinality(prop.getComplexValue());
    }

    @Override
    public void transformNode(Node node, NodeTransform tr) {
        if (node.getNodeType() == null) {
            throw new IllegalArgumentException("No node type: " + node);
        }

        if (node.getDomainObject() == null) {
            throw new IllegalArgumentException("No domain object: " + node);
        }

        if (!can_transform(node, tr)) {
            throw new IllegalArgumentException("Transform not available.");
        }

        if (tr.getInsertParentNodeType() != null) {
            Node new_parent = new Node(urigen.generateNodeURI());

            objstore.moveObject(new_parent, tr.getInsertParentNodeType(), node.getParent());
            objstore.moveObject(node, tr.getResultNodeType(), new_parent);
        }

        if (tr.moveChildrenToParent()) {
            Node parent = node.getParent();

            if (parent == null) {
                throw new IllegalStateException("No parent for: " + node.getIdentifier());
            }

            // Move children to parent, avoiding concurrent modification of
            // children list also performs any child transformations on the
            // moved children
            if (node.hasChildren()) {
                for (Node child : new ArrayList<>(node.getChildren())) {
                    objstore.moveObject(child, null, parent);
                    if (tr.getResultChildTransforms() != null) {
                        transformChildren(child, tr.getResultChildTransforms());
                    }
                }
            }
        }

        if (tr.removeEmptyResult() && node.isLeaf() && !node.isRoot()) {
            if (!node.isRoot()) {
                node.getParent().removeChild(node);
            }

            objstore.deleteObject(node);
        } else if (tr.getResultNodeType() != null
                && !tr.getResultNodeType().getIdentifier().equals(node.getNodeType().getIdentifier())) {
            // Do result node transform if not removed and not already done

            node.setNodeType(tr.getResultNodeType());
            objstore.updateObject(node);
        }

        if (tr.getResultChildTransforms() != null && node.hasChildren()) {
            // For each child, do only first transform which matches
            for (Node child : node.getChildren()) {
                transformChildren(child, tr.getResultChildTransforms());
            }
        }
    }

    // Runs the child transforms on the passed in child node. Applies the first
    // applicable transform in the list.
    private void transformChildren(Node child, List<NodeTransform> childTransforms) {

        for (NodeTransform child_tr : childTransforms) {
            if (can_transform(child, child_tr)) {
                transformNode(child, child_tr);
                break;
            }
        }
    }

    @Override
    public List<NodeTransform> getNodeTransforms(Node node) {
        if (node.getNodeType() == null) {
            throw new IllegalArgumentException("No node type: " + node);
        }

        if (node.getDomainObject() == null) {
            throw new IllegalArgumentException("No domain object: " + node);
        }

        ArrayList<NodeTransform> result = new ArrayList<>();
        
        if (node.isIgnored()) {
            return result;
        }

        result.addAll(node.getNodeType().getDomainProfile().getNodeTransforms().stream().filter(tr -> can_transform(node, tr)).collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<NodeTransform> getNodeTransforms(List<Node> nodes) {
        List<NodeTransform> possibleTransforms = new ArrayList<>();

        if (nodes != null && !nodes.isEmpty()) {
            possibleTransforms = getNodeTransforms(nodes.get(0));

            for (int i = 1; i < nodes.size(); i++) {
                List<NodeTransform> nodeTransforms = getNodeTransforms(nodes.get(i));
                Iterator<NodeTransform> it = possibleTransforms.iterator();
                while (it.hasNext()) {
                    if (!nodeTransforms.contains(it.next())) {
                        it.remove();
                    }
                }
            }
        }

        return possibleTransforms;

    }

    private boolean can_transform(Node node, NodeTransform tr) {
        if (node == null || node.getNodeType() == null || (tr.getSourceNodeType() != null
                && !node.getNodeType().getIdentifier().equals(tr.getSourceNodeType().getIdentifier()))) {
            return false;
        }

        Node parent = node.getParent();
        List<NodeConstraint> child_constraints = tr.getSourceChildConstraints();

        // Check against node without ignored children

        List<Node> kids = null;

        if (node.hasChildren()) {
            kids = new ArrayList<>(node.getChildren());
            kids.removeIf(Node::isIgnored);
        }

        boolean is_leaf = kids == null || kids.isEmpty();

        if (child_constraints != null && !tr.getSourceChildConstraints().isEmpty()) {
            if (is_leaf) {
                // Leaf node must have a matches none child constraint

                boolean matches_none = false;

                for (NodeConstraint nc : child_constraints) {
                    if (nc.matchesNone()) {
                        matches_none = true;
                    }
                }

                if (!matches_none) {
                    return false;
                }
            } else {
                // Each child must meet at least one child constraint

                for (Node child : kids) {
                    boolean meets_constraint = false;

                    for (NodeConstraint nc : child_constraints) {
                        if (meets_type_constraint(child, nc) && meets_parent_relation_constraint(child, node, nc)) {
                            meets_constraint = true;
                            break;
                        }
                    }

                    if (!meets_constraint) {
                        return false;
                    }
                }
            }
        }

        NodeConstraint parent_constraint = tr.getSourceParentConstraint();

        if (parent_constraint != null) {
            if (!meets_type_constraint(parent, parent_constraint)
                    || !meets_parent_relation_constraint(node, parent, parent_constraint)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean validateTree(Node node) {
        if (node.isIgnored()) {
            return false;
        }

        if (!is_valid(node)) {
            return false;
        }

        if (node.isLeaf()) {
            return true;
        }

        for (Node child : node.getChildren()) {
            if (!child.isIgnored() && !validateTree(child)) {
                return false;
            }
        }

        return true;
    }

    private boolean meets_type_constraint(Node parent, NodeConstraint parent_constraint) {
        if (parent_constraint.matchesNone()) {
            return parent == null;
        }

        if (parent_constraint.matchesAny()) {
            return parent != null;
        }

        if (parent == null) {
            return false;
        }

        if (parent_constraint.getNodeType() == null) {
            return true;
        }

        return parent_constraint.getNodeType().getIdentifier().equals(parent.getNodeType().getIdentifier());
    }

    // Check that existing domain objects have the required relations
    // Must first pass meets_type_constraint
    private boolean meets_parent_relation_constraint(Node node, Node parent, NodeConstraint parent_constraint) {
        if (parent == null) {
            // Handled by meets_type_constraint.
            return true;
        }

        if (node.getDomainObject() == null || parent.getDomainObject() == null) {
            return false;
        }

        StructuralRelation rel = parent_constraint.getStructuralRelation();

        if (rel == null) {
            return true;
        }

        if (rel.getHasParentPredicate() != null) {
            if (!objstore.hasRelationship(node.getDomainObject(), rel.getHasParentPredicate(),
                    parent.getDomainObject())) {
                return false;
            }
        }

        if (rel.getHasChildPredicate() != null) {
            if (!objstore.hasRelationship(parent.getDomainObject(), rel.getHasChildPredicate(),
                    node.getDomainObject())) {
                return false;
            }
        }

        return true;
    }

    private boolean meets_file_requirements(Node node, NodeType type) {
        FileInfo info = node.getFileInfo();
        FileAssociation assoc = type.getFileAssociation();

        if (info == null || assoc == null) {
            return true;
        }

        if (info.isFile() && assoc == FileAssociation.REGULAR_FILE) {
            return true;
        }

        if (info.isDirectory() && assoc == FileAssociation.DIRECTORY) {
            return true;
        }

        return false;
    }

    // Check if node can be the given type.
    private boolean may_be_type(Node node, NodeType type) {
        if (!meets_file_requirements(node, type)) {
            return false;
        }

        List<NodeConstraint> constraints = type.getParentConstraints();

        if (constraints == null || constraints.isEmpty()) {
            return true;
        }

        // Parent must meet one constraint. Only the type matters.

        Node parent = node.getParent();

        for (NodeConstraint c : constraints) {
            if (meets_type_constraint(parent, c)) {
                return true;
            }
        }

        return false;
    }

    // Check if node is valid given constraints of its type
    private boolean is_valid(Node node) {
        NodeType type = node.getNodeType();

        if (type == null) {
            return false;
        }

        if (node.getDomainObject() == null) {
            return false;
        }

        if (!meets_file_requirements(node, type)) {
            return false;
        }

        List<NodeConstraint> constraints = type.getParentConstraints();

        if (constraints == null || constraints.isEmpty()) {
            return true;
        }

        // Parent must meet one constraint

        Node parent = node.getParent();

        for (NodeConstraint c : constraints) {
            if (meets_type_constraint(parent, c) && meets_parent_relation_constraint(node, parent, c)) {
                return true;
            }
        }

        return false;
    }

    private int count_children_with_files(Node node) {
        if (node.isLeaf()) {
            return 0;
        }

        int result = 0;

        for (Node child : node.getChildren()) {
            FileInfo info = child.getFileInfo();

            if (info != null && info.isFile()) {
                result++;
            }
        }

        return result;
    }

    private boolean is_preferred_type(Node node, NodeType type) {
        CardinalityConstraint cc = type.getPreferredCountOfChildrenWithFiles();

        if (cc != null) {
            int count = count_children_with_files(node);

            return count >= cc.getMinimum() && (count <= cc.getMaximum() || cc.getMaximum() == -1);
        }

        if (type.getPreferredParentType() != null && node.getParent() != null) {
            return type.getPreferredParentType().equals(node.getParent().getNodeType());
        }

        return false;
    }

    // Return valid types for node with preferred types in front.
    private List<NodeType> get_possible_types(DomainProfile profile, Node node) {
        List<NodeType> result = new ArrayList<>();

        for (NodeType type : profile.getNodeTypes()) {
            if (may_be_type(node, type)) {
                if (is_preferred_type(node, type)) {
                    result.add(0, type);
                } else {
                    result.add(type);
                }
            }
        }

        return result;
    }

    @Override
    public boolean assignNodeTypes(DomainProfile profile, Node node) {
        if (node.isIgnored()) {
            throw new IllegalArgumentException("Cannot assign types to ignored node.: " + node.getIdentifier());
        }
        
        boolean success = assign_node_types(profile, node);

        if (success) {
            // Do not create domain objects for ignored nodes.
            node.walk(n -> { 
                if (!n.isIgnored()) {
                    objstore.updateObject(n);
                }
            });
        }

        return success;
    }

    private boolean assign_node_types(DomainProfile profile, Node node) {
        //Add support for the process being stopped by the GUI
        if (Thread.currentThread().isInterrupted()) {
            return true;
        }

        List<NodeType> valid_types = get_possible_types(profile, node);

        if (valid_types.isEmpty()) {
            return false;
        }

        if (node.isLeaf()) {
            node.setNodeType(valid_types.get(0));
            return true;
        }

        next: for (NodeType nt : valid_types) {
            node.setNodeType(nt);

            for (Node child : node.getChildren()) {
                //Add support for the process being stopped by the GUI
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                if (!child.isIgnored() && !assign_node_types(profile, child)) {
                    continue next;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void removeDomainObject(Node node) {
        if (node.getDomainObject() != null) {
            objstore.deleteObject(node);
        }
    }
}
