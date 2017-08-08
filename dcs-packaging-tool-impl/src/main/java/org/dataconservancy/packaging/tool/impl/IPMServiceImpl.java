package org.dataconservancy.packaging.tool.impl;

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


import org.apache.commons.io.DirectoryWalker;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.impl.support.FilenameValidatorService;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IPMServiceImpl implements IPMService {
    private Map<URI, File> fileUris = new HashMap<>();
    private Map<File, Node> fileToNode = new HashMap<>();
    private Set<Node> visitedFiles = new HashSet<>();
    private final URIGenerator uriGenerator;
    private FilenameValidatorService validatorService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public IPMServiceImpl(URIGenerator uriGenerator) {
        this.uriGenerator = uriGenerator;
        this.validatorService = new FilenameValidatorService();
    }

    @Override
    public Node createTreeFromFileSystem(Path path) throws IOException {

        visitedFiles.clear();
        fileUris.clear();
        fileToNode.clear();

        Walker walker = new Walker();

        Thread walkerThread = new Thread(() -> {
            try {
                walker.doWalk(path.toFile(), visitedFiles);
            } catch (IOException e) {
                walker.cancel();
            }
        });

        walkerThread.start();

        while (walkerThread.isAlive()) {
            if (Thread.currentThread().isInterrupted()) {
                walker.cancel();
            }
            try {
                walkerThread.join(1000 * 5);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        visitedFiles.parallelStream().forEach(visitedNode -> {
            final File file = fileUris.get(visitedNode.getIdentifier());
            final FileInfo fi;
            try {
                fi = new FileInfo(file.toPath().toRealPath());
                visitedNode.setFileInfo(fi);
            } catch (IOException e) {
                log.warn("Unable to resolve file path '{}': {}", file.toPath(), e.getMessage(), e);
            }
        });

        return walker.getRoot();
    }

    @Override
    public void ignoreNode(Node node, boolean status) {
        if (node.isIgnored() == status) {
            return;
        }

        if (node.isIgnored()) {
            node.setIgnored(false);

            // Unignore ancestors
            for (Node n = node.getParent(); n != null && n.isIgnored(); n = n.getParent()) {
                n.setIgnored(false);
            }

            // Unignore descendants
            if (node.getChildren() != null) {
                for (Node child : node.getChildren()) {
                    ignoreNode(child, false);
                }
            }
        } else {
            node.setIgnored(true);

            // Ignore descendants
            if (node.getChildren() != null) {
                for (Node child : node.getChildren()) {
                    ignoreNode(child, true);
                }
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

                //This is a specialized case that occurs when we create a node in the tree with no backing file entity
                if (existingNode.getParent() != null && existingNode.getParent().getFileInfo() == null) {
                    checkFileUpdate(location, existingNode, comparisonNode, nodeMap, comparisonLocationMap);
                } else if (existingNode.getParent() != null && comparisonNode.getParent() == null) {
                    //In the event we refreshed on a sub node, the sub node is the root of the comparison tree so it's options are limited we can simply check for update.
                    //Note that this is only true because in the GUI currently you can't refresh a node whose file content is missing.
                    checkFileUpdate(location, existingNode, comparisonNode, nodeMap, comparisonLocationMap);
                } else if ((existingNode.getParent() == null && comparisonNode.getParent() == null) || existingNode.getParent().getFileInfo().getLocation().equals(comparisonNode.getParent().getFileInfo().getLocation())) {
                    //If both nodes are root or the node's parent location is the same then we consider it the same
                    checkFileUpdate(location, existingNode, comparisonNode, nodeMap, comparisonLocationMap);
                } else {
                    //The node has moved so we consider it a delete and add.
                    markNodesRemoved(existingNode, existingNode.getParent(), nodeMap);

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
                markNodesRemoved(existingLocationMap.get(location), existingLocationMap.get(location).getParent(), nodeMap);
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

    private void checkFileUpdate(URI location, Node existingNode, Node comparisonNode, Map<Node, NodeComparison> nodeMap, Map<URI, Node> comparisonLocationMap) {
        if (existingNode.getFileInfo().isFile() && comparisonNode.getFileInfo().isFile()
                && !existingNode.getFileInfo().getChecksum(FileInfo.Algorithm.MD5).equalsIgnoreCase(comparisonNode.getFileInfo().getChecksum(FileInfo.Algorithm.MD5))
                && !existingNode.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1).equalsIgnoreCase(comparisonNode.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1))) {

            //The checksums are different so we consider this an update
            nodeMap.put(comparisonNode, new NodeComparison(NodeComparison.Status.UPDATED, existingNode));
            comparisonLocationMap.remove(location);
        } else {
            //The file location is completely unchanged and not updated
            comparisonLocationMap.remove(location);
        }
    }
    private void markNodesRemoved(Node node, Node parent, Map<Node, NodeComparison> nodeMap) {
        nodeMap.put(node, new NodeComparison(NodeComparison.Status.DELETED, parent));
        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                markNodesRemoved(child, node, nodeMap);
            }
        }
    }

    private void addNodeLocation(Node node, Map<URI, Node> nodeLocationMap) {

        //If the node has no file information leave it out of the map
        if (node.getFileInfo() != null) {
            nodeLocationMap.put(node.getFileInfo().getLocation(), node);
        }

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

    @Override
    public void remapNode(Node node, Path newPath) throws IOException {

        Path oldPath = null;
        if (node.getFileInfo().getLocation() != null) {
            oldPath = Paths.get(node.getFileInfo().getLocation());
        }
        node.setFileInfo(new FileInfo(newPath));

        //If we have an old path try to remap children
        if (oldPath != null && node.getChildren() != null) {
            //If this path can't be relativized we won't automatically remap
            final Path finalOldPath = oldPath;
            node.getChildren().stream().filter(child -> child.getFileInfo() !=
                null).forEach(child -> {
                try {
                    Path oldRelativePath = finalOldPath.relativize(Paths.get(child.getFileInfo().getLocation()));
                    Path newChildPath = newPath.resolve(oldRelativePath);
                    if (newChildPath.toFile().exists()) {
                        remapNode(child, newChildPath);
                    }
                } catch (IllegalArgumentException | IOException e) {
                    //If this path can't be relativized we won't automatically remap
                }
            });
        }
    }

    @Override
    public Map<Node, NodeComparison> refreshTreeContent(Node node) throws IOException {
        Node newTree = buildComparisonTree(node);
        return compareTree(node, newTree);
    }

    /**
     * Builds a tree from the current file system to compare with the existing file system.
     * @param node The node from the existing tree that will be the root of the comparison
     * @return The root of the new tree to compare
     * @throws IOException If there is a problem reading from the file system.
     */
    private Node buildComparisonTree(Node node) throws IOException {
        Node newTree = createTreeFromFileSystem(Paths.get(node.getFileInfo().getLocation()));
        buildContentRoots(node, newTree);

        return newTree;
    }

    /**
     * Loops through the existing tree to find any content locations different from their parent, it then builds a tree from the file system under that location.
     * @param node The node to check for different content locations
     * @param newTree The new tree to add the tree from the file system to
     * @throws IOException If there is a problem reading from the file system.
     */
    private void buildContentRoots(Node node, Node newTree) throws IOException {
        if (node.getChildren() != null && node.getFileInfo() != null) {
            for (Node child : node.getChildren()) {
                if (child.getFileInfo() != null && Paths.get(child.getFileInfo().getLocation()).toFile().exists()) {
                    if (!Paths.get(child.getFileInfo().getLocation()).startsWith(Paths.get(node.getFileInfo().getLocation()))) {
                        Node newTreeParent = getNewTreeNodeForExistingNode(node, newTree);
                        if (newTreeParent != null) {
                            newTreeParent.addChild(buildComparisonTree(child));
                        } else {
                            newTree.addChild(buildComparisonTree(child));
                        }
                    } else if (child.getChildren() != null) {
                        buildContentRoots(child, newTree);
                    }
                } else if (child.getChildren() != null) {
                    buildContentRoots(child, newTree);
                }
            }
        }
    }

    /**
     * Finds nodes in the new comparison tree that correspond to nodes in the existing tree.
     * This is used to ensure new content locations are placed in the correct spot in the tree.
     * @param node The node to find in the new tree.
     * @param newTree The new tree to search for the node.
     * @return The node from the new tree or false if none exists
     */
    private Node getNewTreeNodeForExistingNode(Node node, Node newTree) {
        Node foundNode = null;
        if (node.getFileInfo() != null && newTree.getFileInfo() != null
            && node.getFileInfo().getLocation().equals(newTree.getFileInfo().getLocation())) {
            foundNode =  newTree;
        } else if (newTree.getChildren() != null){
            for (Node newTreeChild : newTree.getChildren()) {
                foundNode = getNewTreeNodeForExistingNode(node, newTreeChild);
                if (foundNode != null) {
                    break;
                }
            }
        }

        return foundNode;
    }

    /**
     * This method will check filenames in the given path for validity against the Cata Conservancy BagIt profile
     * specification, version 1.0
     * @param path The path to check filenames for
     * @throws IOException
     */
    private void validateFileNames(Path path) throws IOException {
        List<String> invalidNamesList = validatorService.findInvalidFilenames(path);
        if (invalidNamesList != null && !invalidNamesList.isEmpty()) {
            String invalidNames = "";
            for (int i = 0; i < invalidNamesList.size(); i++) {
                invalidNames += invalidNamesList.get(i);

                if (i + 1 < invalidNamesList.size()) {
                    invalidNames += "\n";
                }
            }
            throw new IOException("Error creating package tree. The following names were invalid:\n\n" + invalidNames);
        }
    }

    private class Walker extends DirectoryWalker<Node> {

        private boolean verifyExists = true;

        private boolean ignoreHidden = true;

        private boolean ignoreDot = true;

        private Node root = null;

        private Path rootPath = null;

        private volatile boolean cancelled = false;

        private Walker() {

        }

        private Walker(boolean verifyExists, boolean ignoreHidden, boolean ignoreDot) {
            this.verifyExists = verifyExists;
            this.ignoreHidden = ignoreHidden;
            this.ignoreDot  = ignoreDot;
        }

        private void doWalk(File baseDir, Collection<Node> results) throws IOException {
            super.walk(baseDir, results);
        }

        @Override
        protected boolean handleDirectory(File directory, int depth, Collection<Node> results) throws IOException {
            return handle(directory, depth, results);
        }

        @Override
        protected void handleFile(File file, int depth, Collection<Node> results) throws IOException {
            handle(file, depth, results);
        }

        private boolean handle(File file, int depth, Collection<Node> results) throws IOException {
            final Path fileAsPath = file.toPath();

            if (Files.isSymbolicLink(fileAsPath) && fileAsPath.toRealPath().startsWith(rootPath)) {
                // ignore: the symbolic link is targeting a file underneath the root
                return false;
            }

            final URI nodeUri = uriGenerator.generateNodeURI();
            final Node node = new Node(nodeUri);

            fileUris.put(nodeUri, file);
            fileToNode.put(file, node);
            results.add(node);

            if (root == null) {
                root = node;
                rootPath = fileAsPath.toRealPath();
            } else {
                Node parent = fileToNode.get(file.getParentFile());
                node.setParent(parent);
                parent.addChild(node);
            }

            if (Files.isHidden(fileAsPath) && ignoreHidden) {
                node.setIgnored(true);
            }

            if (fileAsPath.getFileName().toString().startsWith(".") && ignoreDot) {
                node.setIgnored(true);
            }

            if (node.getParent() != null && node.getParent().isIgnored()) {
                node.setIgnored(node.getParent().isIgnored());
            }

            if (!verifyExists || file.exists()) {
                results.add(node);
                return true;
            }

            return false;
        }

        private void cancel() {
            cancelled = true;
        }

        @Override
        protected void handleCancelled(File startDirectory, Collection<Node> results, CancelException cancel)
                throws IOException {
            visitedFiles.clear();
            fileUris.clear();
            fileToNode.clear();
        }

        @Override
        protected boolean handleIsCancelled(File file, int depth, Collection<Node> results) throws IOException {
            return cancelled;
        }

        private Node getRoot() {
            return root;
        }
    }
}
