package org.dataconservancy.packaging.tool.impl;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.packaging.tool.api.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.ipm.Node;

public class DomainProfileServiceImpl implements DomainProfileService {
    private final DomainProfile profile;
    private final DomainProfileObjectStore objstore;

    public DomainProfileServiceImpl(DomainProfile profile, DomainProfileObjectStore objstore) {
        this.profile = profile;
        this.objstore = objstore;
    }

    @Override
    public void addProperty(Node node, PropertyValue value) {
        objstore.addProperty(node.getDomainObject(), value);
    }

    @Override
    public void removeProperty(Node node, PropertyValue value) {
        objstore.removeProperty(node.getDomainObject(), value);
    }

    @Override
    public void removeProperty(Node node, PropertyType type) {
        objstore.removeProperty(node.getDomainObject(), type);
    }

    @Override
    public List<PropertyValue> getProperties(Node node, NodeType type) {
        return objstore.getProperties(node.getDomainObject(), type);
    }

    @Override
    public boolean validateProperties(Node node, NodeType type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void transformNode(Node node, NodeTransform trans) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<NodeTransform> getNodeTransforms(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean validateTree(Node root) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean meets_parent_constraint(Node node, Node parent, NodeConstraint parent_constraint) {
        if (parent_constraint == null) {
            return true;
        }

        // Check type

        if (!parent_constraint.getNodeType().getIdentifier().equals(parent.getNodeType().getIdentifier())) {
            return false;
        }

        // Check that existing objects have one of the required structural
        // predicates

        if (node.getDomainObject() != null && parent.getDomainObject() != null) {
            StructuralRelation rel = parent_constraint.getStructuralRelation();
            
            if (objstore.hasRelationship(node.getDomainObject(), rel.getHasParentPredicate(), parent.getDomainObject())
                    && objstore.hasRelationship(parent.getDomainObject(), rel.getHasChildPredicate(),
                            node.getDomainObject())) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    private boolean meets_file_requirements(Node node, NodeType type) {
        type.getFileAssociationRequirement();

        type.getDirectoryAssociationRequirement();

        // TODO

        return true;
    }

    private boolean is_valid_type(Node node, NodeType type) {
        if (!meets_file_requirements(node, type)) {
            return false;
        }

        // Parent must meet one constraint

        for (NodeConstraint c : type.getParentConstraints()) {
            if (meets_parent_constraint(node, node.getParent(), c)) {
                return true;
            }
        }

        return false;
    }

    private List<NodeType> get_valid_types_ordered_by_preference(Node node) {
        List<NodeType> result = new ArrayList<>();

        for (NodeType nt : profile.getNodeTypes()) {

            // Order by preference

            nt.getPreferredCountOfChildrenWithFiles();

            if (is_valid_type(node, nt)) {
                result.add(nt);
            }
        }

        return result;
    }

    @Override
    public boolean assignNodeTypes(Node node) {
        boolean result = assign_node_types(node);

        if (result) {
            update_domain_objects(node);
        }

        return result;
    }

    private void update_domain_objects(Node node) {
        objstore.updateObject(node);

        if (node.isLeaf()) {
            return;
        }

        for (Node child : node.getChildren()) {
            update_domain_objects(child);
        }
    }

    private boolean assign_node_types(Node node) {
        next: for (NodeType nt : get_valid_types_ordered_by_preference(node)) {
            node.setNodeType(nt);

            if (node.isLeaf()) {
                continue;
            }

            for (Node child : node.getChildren()) {
                if (!assign_node_types(child)) {
                    continue next;
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void propagateInheritedProperties(Node node) {
        // nodetypemap.get(node.getNodeType()).getInheritableProperties();

        // TODO Auto-generated method stub
    }
}
