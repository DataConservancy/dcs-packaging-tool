package org.dataconservancy.packaging.tool.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dataconservancy.packaging.tool.api.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.ipm.ComparisonNode;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

public class DomainProfileServiceImpl implements DomainProfileService {
    private final DomainProfile profile;
    private final DomainProfileObjectStore objstore;
    private final Map<URI, NodeType> nodetypemap;

    public DomainProfileServiceImpl(DomainProfile profile, DomainProfileObjectStore objstore) {
        this.profile = profile;
        this.objstore = objstore;
        this.nodetypemap = new HashMap<>();

        for (NodeType nt : profile.getNodeTypes()) {
            nodetypemap.put(nt.getIdentifier(), nt);
        }
    }

    @Override
    public DomainProfile getDomainProfile() {
        return profile;
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
    public List<PropertyValue> getProperties(Node node) {
        return objstore.getProperties(node.getDomainObject());
    }

    @Override
    public List<PropertyValue> getProperties(Node node, PropertyType type) {
        return objstore.getProperties(node.getDomainObject(), type);
    }

    @Override
    public boolean validateProperties(Node node) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<PropertyConstraint> getPropertyConstraints(Node node) {
        return nodetypemap.get(node.getNodeType()).getPropertyConstraints();
    }

    @Override
    public String formatPropertyValue(PropertyValue value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertyValue parsePropertyValue(PropertyType type, String value) {
        // TODO Auto-generated method stub
        return null;
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
    public boolean validateTree(Node root, boolean check_properties) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean meets_parent_constraint(Node node, Node parent, NodeConstraint parent_constraint) {
        if (parent_constraint == null) {
            return true;
        }

        // Check type
        
        if (!parent_constraint.getNodeTypes().contains(nodetypemap.get(parent.getNodeType()))) {
            return false;
        }

        // Check that existing objects have one of the required structural
        // predicates

        if (node.getDomainObject() != null && parent.getDomainObject() != null) {
            boolean found_pred = false;

            for (StructuralRelation rel : parent_constraint.getStructuralRelations()) {
                if (objstore.hasRelationship(node.getDomainObject(), rel.getHasParentPredicate(),
                        parent.getDomainObject())
                        && objstore.hasRelationship(parent.getDomainObject(), rel.getHasChildPredicate(),
                                node.getDomainObject())) {
                    found_pred = true;
                    break;
                }
            }

            if (!found_pred) {
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
        NodeType nt = nodetypemap.get(node.getNodeType());

        if (node.getDomainObject() == null) {
            objstore.updateObject(node.getDomainObject(), nt);
        } else {
            node.setDomainObject(objstore.createObject(nt));
        }

        for (Node child : node.getChildren()) {
            update_domain_objects(child);
        }
    }

    // TODO If there are hundreds of thousands of nodes with many types, this
    // recursive approach may be problematic.

    private boolean assign_node_types(Node node) {
        next: for (NodeType nt : get_valid_types_ordered_by_preference(node)) {
            node.setNodeType(nt.getIdentifier());

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
    public Node createTreeFromFileSystem(Path path) {
        return create_tree(null, path);
    }

    private Node create_tree(Node parent, Path path) {
        Node node = null;

        if (parent != null) {
            parent.addChild(node);
        }

        node.setIdentifier(URI.create("urn:uuid:" + UUID.randomUUID()));

        // TODO Gather file info here
        node.setFileInfo(null);

        // TODO Ignore hidden files
        
        
        if (Files.isRegularFile(path)) {

        } else if (Files.isDirectory(path)) {
            try {
                Files.list(path).forEach(child_path -> create_tree(node, child_path));
            } catch (IOException e) {
                // TODO
                throw new RuntimeException(e);
            }
        } else {
            // TODO
        }

        return node;
    }

    @Override
    public void ignoreNode(Node node, boolean status) {
        if (node.isIgnored() == status) {
            return;
        }

        if (node.isIgnored()) {
            node.setIgnored(false);

            // Unignore ancestors
            for (Node n = node; n != null && n.isIgnored(); n = n.getParent()) {
                n.setIgnored(false);
            }

            // Unignore descendants
            for (Node child : node.getChildren()) {
                ignoreNode(child, false);
            }
        } else {
            node.setIgnored(true);

            // Ignore descendants
            for (Node child : node.getChildren()) {
                ignoreNode(child, true);
            }
        }
    }

    @Override
    public void propagateInheritedProperties(Node node) {
        // nodetypemap.get(node.getNodeType()).getInheritableProperties();

        // TODO Auto-generated method stub
    }

    @Override
    public boolean checkFileInfoIsAccessible(Node node) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateFileInfo(Node node, FileInfo info) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ComparisonNode compareTree(Node existingTree, Node comparisonTree) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void mergeTree(Node existingTree, ComparisonNode comparisonTree) {
        // TODO Auto-generated method stub
        
    }
}
