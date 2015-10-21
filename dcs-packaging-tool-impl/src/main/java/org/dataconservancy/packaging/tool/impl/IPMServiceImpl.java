package org.dataconservancy.packaging.tool.impl;

import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparisonStatus;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public void mergeTree(Node existingTree,
                          Map<Node, NodeComparisonStatus> comparisonResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<Node, NodeComparisonStatus> compareTree(Node existingTree,
                                                       Node comparisonTree) {

        Map<Node, NodeComparisonStatus> nodeMap = new HashMap<>();
        //Check if root nodes compare
        compareNode(existingTree, comparisonTree, nodeMap);

        //Check if any nodes from the comparison tree weren't reached, in which case they're new nodes.
        checkNodeReached(comparisonTree, nodeMap);
        return nodeMap;
    }

    private void compareNode(Node existingNode, Node comparisonNode, Map<Node, NodeComparisonStatus> nodeMap) {
        if (existingNode.getFileInfo().getLocation().equals(comparisonNode.getFileInfo().getLocation())) {
            //Navigate the new tree
            for (Node existingChild : existingNode.getChildren()) {
                FileInfo existingInfo = existingChild.getFileInfo();
                boolean existingChildFound = false;
                for (Node comparisonChild : comparisonNode.getChildren()) {
                    if (existingInfo.getLocation().equals(comparisonChild.getFileInfo().getLocation())) {
                        existingChildFound = true;

                        //If it's a file and the checksums have changed it's an update, otherwise it's an unchanged file or directory
                        if (existingInfo.isFile()
                            && !existingInfo.getChecksum(FileInfo.Algorithm.MD5).equalsIgnoreCase(comparisonChild.getFileInfo().getChecksum(FileInfo.Algorithm.MD5))
                            && !existingInfo.getChecksum(FileInfo.Algorithm.SHA1).equalsIgnoreCase(comparisonChild.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1))) {
                            nodeMap.put(existingChild, NodeComparisonStatus.UPDATED);
                            nodeMap.put(comparisonChild, NodeComparisonStatus.UPDATED);
                        } else {
                            nodeMap.put(existingChild, NodeComparisonStatus.UNCHANGED);
                            nodeMap.put(comparisonChild, NodeComparisonStatus.UNCHANGED);

                            if (existingChild.getChildren() != null && !existingChild.getChildren().isEmpty()) {
                                compareNode(existingChild, comparisonChild, nodeMap);
                            }

                            //TODO Can we put an else in here to handle children not found, instead of doing it later?
                        }
                    }
                }

                //If we were unable to find the corresponding file entity in the new tree mark the old one and it's children as removed.
                if (!existingChildFound) {
                    markNodesAsRemoved(existingChild, nodeMap);
                }
            }

            nodeMap.put(existingNode, NodeComparisonStatus.UNCHANGED);
            nodeMap.put(comparisonNode, NodeComparisonStatus.UNCHANGED);
        } else { //The roots are different so the entire trees are different
            //If the entire trees are different mark every node in the existing tree as deleted and every node in the new tree as added.
            markNodesAsAdded(comparisonNode, nodeMap);
            markNodesAsRemoved(existingNode, nodeMap);
        }
    }

    private void checkNodeReached(Node comparisonNode, Map<Node, NodeComparisonStatus> nodeMap) {
        if (nodeMap.keySet().contains(comparisonNode)) {
            if (comparisonNode.getChildren() != null && !comparisonNode.getChildren().isEmpty()) {
                for (Node comparisonChild : comparisonNode.getChildren()) {
                    checkNodeReached(comparisonChild, nodeMap);
                }
            }
        } else {
            markNodesAsAdded(comparisonNode, nodeMap);
        }
    }

    //Marks a node and all it's children as added.
    private void markNodesAsAdded(Node node, Map<Node, NodeComparisonStatus> nodeMap) {
        nodeMap.put(node, NodeComparisonStatus.ADDED);
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (Node child : node.getChildren()) {
                markNodesAsAdded(child, nodeMap);
            }
        }
    }

    //Marks a node and all it's children as removed.
    private void markNodesAsRemoved(Node node, Map<Node, NodeComparisonStatus> nodeMap) {
        nodeMap.put(node, NodeComparisonStatus.DELETED);
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (Node child : node.getChildren()) {
                markNodesAsRemoved(child, nodeMap);
            }
        }
    }

    @Override
    public boolean checkFileInfoIsAccessible(Node node) {
        // TODO Auto-generated method stub
        return false;
    }

}
