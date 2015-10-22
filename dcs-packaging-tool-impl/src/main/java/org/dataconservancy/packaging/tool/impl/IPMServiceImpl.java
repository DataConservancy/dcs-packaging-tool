package org.dataconservancy.packaging.tool.impl;

import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IPMServiceImpl implements IPMService {
    private Set<Path> visitedFiles = new HashSet<>();

    @Override
    public Node createTreeFromFileSystem(Path path) throws IOException {
        visitedFiles.clear();
        Node root;
        root = createTree(null, path);
        return root;
    }

    /*
     * Creates a Node in the tree for the given path, and will recurse the file structure to add all child files and folders.
     */
    private Node createTree(Node parent, Path path) throws IOException {
        //Tests to ensure any symbolic links do not create cycles in the tree.
        try {
            if (visitedFiles.contains(path.toRealPath())) {
                if (Files.isSymbolicLink(path)) {
                    throw new IOException("Symbolic link cycle detected." +
                                              "Fix offending symbolic link at " +
                                              path.toFile().toString() +
                                              ", which points to " +
                                              path.toRealPath());
                } else {
                    throw new IOException("Symbolic link cycle detected." +
                                              "There is a symbolic link under " +
                                              path.getRoot().toString() +
                                              " which points to " + path +
                                              ".  Find the link and remove it.");
                }
            } else {
                visitedFiles.add(path);
            }
        } catch (IOException e) {
            throw new IOException(
                "Error determining canonical path of " + path.toFile(), e);
        }

        Node node = new Node(URI.create("urn:uuid:" + UUID.randomUUID()));

        FileInfo info = new FileInfo(path);
        node.setFileInfo(info);

        //If it's not the root set the parent child information.
        if (parent != null) {
            parent.addChild(node);

            //If the parent of this new node was previously ignored, ignore this node as well.
            if (parent.isIgnored()) {
                node.setIgnored(true);
            }

            node.setParent(parent);
        }

        //If the file is hidden or starts with a "." set it to ignored.
        //The "." semantics are carried over from the old rules based approach.
        if (Files.isHidden(path) ||
            path.getFileName().toString().startsWith(".")) {
            node.setIgnored(true);
        }

        //If the path represents a directory loop through all children and add them to the tree.
        if (Files.isDirectory(path)) {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            for (Path childPath : stream) {
                createTree(node, childPath);
            }

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
    public boolean mergeTree(Node existingTree,
                          Map<Node, NodeComparison> comparisonResult) {

        //Handle the case where root node has been deleted.
        if (comparisonResult.get(existingTree) != null && comparisonResult.get(existingTree).getStatus() == NodeComparison.Status.DELETED) {
            //In the case where we're building a new tree we need to find the new root.
            Node newRoot = null;
            for (Node node : comparisonResult.keySet()) {
                NodeComparison comparison = comparisonResult.get(node);
                if (comparison.getStatus() == NodeComparison.Status.ADDED && comparison.getNode() == null && !node.equals(existingTree)) {
                    newRoot = node;
                }
            }

            if (newRoot == null) {
                return false;
            }

            applyTreeChanges(comparisonResult);

            //Assign the new root to the old root
            existingTree.setIdentifier(newRoot.getIdentifier());
            existingTree.setFileInfo(newRoot.getFileInfo());
            existingTree.setDomainObject(newRoot.getDomainObject());
            existingTree.getChildren().clear();

            existingTree.setChildren(newRoot.getChildren());
            existingTree.setNodeType(newRoot.getNodeType());
            existingTree.setSubNodeTypes(newRoot.getSubNodeTypes());

        } else {
            applyTreeChanges(comparisonResult);
        }
        return true;
    }

    private void applyTreeChanges(Map<Node, NodeComparison> comparisonMap) {
        for (Node key : comparisonMap.keySet()) {
            NodeComparison comparison = comparisonMap.get(key);

            switch (comparison.getStatus()) {
                case ADDED:
                    if (comparison.getNode() != null) {
                        comparison.getNode().addChild(key);
                    }
                    break;
                case DELETED:
                    if (comparison.getNode() != null) {
                        comparison.getNode().removeChild(key);
                    }
                    break;
                case UPDATED:
                    if (comparison.getNode() != null) {
                        comparison.getNode().setFileInfo(key.getFileInfo());
                    }
                    break;
            }
        }
    }

    @Override
    public Map<Node, NodeComparison> compareTree(Node existingTree,
                                                 Node comparisonTree) {

        Map<Node, NodeComparison> nodeMap = new HashMap<>();

        //Generate maps of existing locations in the trees
        Map<URI, Node> existingLocationMap = new HashMap<>();
        Map<URI, Node> comparisonLocationMap = new HashMap<>();

        addNodeLocation(existingTree, existingLocationMap);
        addNodeLocation(comparisonTree, comparisonLocationMap);

        for (URI location : existingLocationMap.keySet()) {
            if (comparisonLocationMap.containsKey(location)) {
                Node existingNode = existingLocationMap.get(location);
                Node comparisonNode = comparisonLocationMap.get(location);

                //If the node is root or the node's parent location is the same then we consider if the same
                if ((existingNode.getParent() == null && comparisonNode.getParent() == null) || existingNode.getParent().getFileInfo().getLocation().equals(comparisonNode.getParent().getFileInfo().getLocation())) {
                    if (existingNode.getFileInfo().isFile()
                            && !existingNode.getFileInfo().getChecksum(FileInfo.Algorithm.MD5).equalsIgnoreCase(comparisonNode.getFileInfo().getChecksum(FileInfo.Algorithm.MD5))
                            && !existingNode.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1).equalsIgnoreCase(comparisonNode.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1))) {

                        //The checksums are different so we consider this an update
                        nodeMap.put(comparisonNode, new NodeComparison(NodeComparison.Status.UPDATED, existingNode));
                        comparisonLocationMap.remove(location);
                    } else {
                        //The file location is completely unchanged and not updated
                        comparisonLocationMap.remove(location);
                    }
                } else {
                    //The node has moved so we consider it a delete and add.
                    nodeMap.put(existingNode, new NodeComparison(NodeComparison.Status.DELETED, existingNode.getParent()));

                    //Determine what the parent of the new node will be it will either be already in the tree, or a new node being added.
                    Node parent;

                    //If we've added the comparison node as the parent it will be in the map
                    if (nodeMap.get(comparisonNode.getParent()) != null) {
                        parent = comparisonNode.getParent();
                    } else {
                        parent = existingNode.getParent();
                    }
                    nodeMap.put(comparisonNode, new NodeComparison(NodeComparison.Status.ADDED, parent));
                    comparisonLocationMap.remove(location);
                }
            } else {
                nodeMap.put(existingLocationMap.get(location), new NodeComparison(NodeComparison.Status.DELETED, existingLocationMap.get(location).getParent()));
            }
        }

        //Anything remaining in the comparison location map should be added
        if (!comparisonLocationMap.isEmpty()) {
            for (URI location : comparisonLocationMap.keySet()) {
                //Determine what the parent of the new node will be it will either be already in the tree, or a new node being added.
                Node parent = null;

                Node newNode = comparisonLocationMap.get(location);
                if (newNode.getParent() != null) {
                    if (existingLocationMap.get(newNode.getParent().getFileInfo().getLocation()) != null) {
                        parent = existingLocationMap.get(newNode.getParent().getFileInfo().getLocation());
                    }

                    //If the parent isn't existing then we must have added it
                    if (parent == null) {
                        parent = comparisonLocationMap.get(newNode.getParent().getFileInfo().getLocation());
                    }
                }
                nodeMap.put(newNode, new NodeComparison(NodeComparison.Status.ADDED, parent));
            }
        }

        return nodeMap;
    }

    private void addNodeLocation(Node node, Map<URI, Node> nodeLocationMap) {
        nodeLocationMap.put(node.getFileInfo().getLocation(), node);

        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                addNodeLocation(child, nodeLocationMap);
            }
        }
    }

    @Override
    public boolean checkFileInfoIsAccessible(Node node) {
        boolean accessible = false;

        if (node != null && node.getFileInfo() != null) {
            FileInfo info = node.getFileInfo();
            if (info.getLocation() != null) {
                accessible = Paths.get(info.getLocation()).toFile().exists();
            }
        }
        return accessible;
    }

}
