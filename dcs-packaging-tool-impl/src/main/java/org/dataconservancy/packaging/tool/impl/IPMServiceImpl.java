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
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public boolean checkFileInfoIsAccessible(Node node) {
        // TODO Auto-generated method stub
        return false;
    }

}
