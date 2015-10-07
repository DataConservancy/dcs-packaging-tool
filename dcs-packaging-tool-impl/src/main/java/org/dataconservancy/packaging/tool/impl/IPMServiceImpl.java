package org.dataconservancy.packaging.tool.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparisonStatus;
import org.dataconservancy.packaging.tool.model.ipm.Node;

public class IPMServiceImpl implements IPMService {
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
    public void mergeTree(Node existingTree, Map<Node, NodeComparisonStatus> comparisonResult) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<Node, NodeComparisonStatus> compareTree(Node existingTree, Node comparisonTree) {
        // TODO Auto-generated method stub
        return null;
    }
    

    @Override
    public boolean checkFileInfoIsAccessible(Node node) {
        // TODO Auto-generated method stub
        return false;
    }

}
