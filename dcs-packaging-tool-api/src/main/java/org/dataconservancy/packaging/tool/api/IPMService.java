package org.dataconservancy.packaging.tool.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.dataconservancy.packaging.tool.api.support.NodeComparisonStatus;
import org.dataconservancy.packaging.tool.model.ipm.Node;

public interface IPMService {
    /**
     * Create a tree from the file system. The tree has file data associated
     * with it, but does not have types assigned to nodes.
     * 
     * @param path The path on the file system to create a node tree from.
     * @return root of tree
     */
    Node createTreeFromFileSystem(Path path) throws IOException;

    /**
     * Change the ignored status of a node. This may cause the types of other
     * nodes to change.
     * 
     * If a node is marked as ignored, all descendants are also marked as
     * ignored.
     * 
     * If a node is marked as not ignored, all descendants and ancestors are
     * also marked as not ignored.
     * 
     * @param node The node to change the ignored status for.
     * @param status The new status of the node.
     */
    void ignoreNode(Node node, boolean status);
    
    /**
     * Compares the three provided under the existing tree root node with the tree under the comparison tree root node.
     * @param existingTree The root node of the existing tree to compare.
     * @param comparisonTree The root node of the new tree to compare against the existing tree.
     * @return A map of nodes and their status after comparison.
     */
    Map<Node, NodeComparisonStatus> compareTree(Node existingTree, Node comparisonTree);

    /**
     * Merges the provided comparison tree into the existing tree.
     * @param existingTree The existing tree that will receive the results of the merge.
     * @param comparisonResult A map of the comparison result to apply to the existing tree.
     */
    void mergeTree(Node existingTree, Map<Node, NodeComparisonStatus> comparisonResult);
    
    /**
     * Checks that the file backing the node is accessible on the file system.
     * Note that this method makes no assertion that the file or folder pointed to was the original one, just that something exists
     * at the file location stored in the nodes file info.
     * @param node The node to check if it's file info file location is still accessible.
     * @return true if the file is accessible false otherwise.
     */
    boolean checkFileInfoIsAccessible(Node node);
}
