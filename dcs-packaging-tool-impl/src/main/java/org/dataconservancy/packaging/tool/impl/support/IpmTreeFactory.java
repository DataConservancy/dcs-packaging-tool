package org.dataconservancy.packaging.tool.impl.support;

import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Random;
import java.util.UUID;

public class IpmTreeFactory {

    //Callback used for setting types when building large trees
    private NodeTypeSetter typeSetter;
    private Random random = new Random();

    /**
     * Creates a single Node tree that is backed by a directory
     * @param type The type to assign to the node
     * @return The root node of the single node tree.
     */
    public Node createSingleDirectoryTree(NodeType type) {
        Node root = new Node(URI.create(UUID.randomUUID().toString()));
        root.setFileInfo(createDirectoryInfo("/" + randomString(5), randomString(8)));
        root.setNodeType(type);
        return root;
    }

    /**
     * Creates a single Node tree that is backed by a file
     * @param type The type to assign to the node
     * @return The root node of the single node tree.
     */
    public Node createSingleFileTree(NodeType type) {
        Node root = new Node(URI.create(UUID.randomUUID().toString()));
        root.setFileInfo(createFileInfo("/" + randomString(5), randomString(8)));
        root.setNodeType(type);
        return root;
    }

    /**
     * Creates a tree that has two nodes a root backed by a directory and a child backed by a file
     * @param directoryType The type to assign to the root/directory node.
     * @param fileType The type to assign to the child/file node.
     * @return The root node of the tree.
     */
    public Node createSingleDirectoryFileTree(NodeType directoryType, NodeType fileType) {
        Node root = new Node(URI.create(UUID.randomUUID().toString()));

        String rootPath = "/" + randomString(5);
        root.setFileInfo(createDirectoryInfo(rootPath, randomString(6)));
        root.setNodeType(directoryType);

        Node child = new Node(URI.create(UUID.randomUUID().toString()));
        child.setFileInfo(createFileInfo(rootPath + "/" + randomString(4), randomString(9)));
        child.setNodeType(fileType);
        root.addChild(child);

        return root;
    }

    /**
     * Creates a tree that has two nodes both backed by directories
     * @param parentDirectoryType The type to assign the root node.
     * @param childDirectoryType The type to assign the child node.
     * @return The root node of the tree.
     */
    public Node createTwoDirectoryTree(NodeType parentDirectoryType, NodeType childDirectoryType) {
        Node root = new Node(URI.create(UUID.randomUUID().toString()));

        String rootPath = "/" + randomString(5);
        root.setFileInfo(createDirectoryInfo(rootPath, randomString(6)));
        root.setNodeType(parentDirectoryType);

        Node child = new Node(URI.create(UUID.randomUUID().toString()));
        child.setFileInfo(createDirectoryInfo(rootPath + "/" + randomString(4), randomString(9)));
        child.setNodeType(childDirectoryType);
        root.addChild(child);

        return root;
    }

    /**
     * Creates a randomly generated tree using the given parameters.
     *
     * If types are going to be set the {@link NodeTypeSetter} must be set on the factory, otherwise no types will be assigned.
     * @param maxDepth The depth the tree should have
     * @param branching The number of branches each node should have
     * @param allowMidLevelFiles True if files can exist at any level in the tree, false if they should exist only at max depth.
     * @return The root node of the tree.
     */
    public Node createTree(int maxDepth, int branching, boolean allowMidLevelFiles) {
        return createTree(0, maxDepth, branching, 0, allowMidLevelFiles);
    }

    private Node createTree(int depth, int maxDepth, int branching, int nodeId, boolean allowMidLevelFiles) {
        Node node = new Node(URI.create("test:" + depth + "," + nodeId));

        if (++depth < maxDepth) {

            if (!allowMidLevelFiles || random.nextInt(1000) % 2 == 0) {
                node.setFileInfo(createDirectoryInfo(
                    "/" + depth + "/" + nodeId + "/", randomString(5)));

                for (int branch = 0; branch < branching; branch++) {
                    node.addChild(createTree(depth, maxDepth, branching, branch, allowMidLevelFiles));
                }
            } else {
                node.setFileInfo(createFileInfo("/" + depth + "/" + nodeId, randomString(6)));
            }
        } else {
            node.setFileInfo(createFileInfo("/" + depth + "/" + nodeId, randomString(6)));
        }



        if (typeSetter != null) {
            typeSetter.setNodeType(node, depth -1);
        }
        return node;
    }

    private FileInfo createDirectoryInfo(String path, String name) {
       FileInfo result = new FileInfo(Paths.get(path).toUri(), name);

       result.setIsDirectory(true);
       result.setCreationTime(FileTime.fromMillis(400000));
       result.setLastModifiedTime(FileTime.fromMillis(600000));

       return result;
   }

   private FileInfo createFileInfo(String path, String name) {
       FileInfo result = new FileInfo(Paths.get(path).toUri(), name);

       result.setIsFile(true);
       result.setSize(120032);

       result.setCreationTime(FileTime.fromMillis(10000000));
       result.setLastModifiedTime(FileTime.fromMillis(2000000));

       result.addFormat("application/octet-stream");
       result.addChecksum(FileInfo.Algorithm.MD5, "12345");
       result.addChecksum(FileInfo.Algorithm.SHA1, "54321");

       return result;
   }

    //Generates a random string to use as file and directory names
    private String randomString(final int length) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++) {
            char c = (char)(random.nextInt((int)(Character.MAX_VALUE)));
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Sets the NodeType setter that should be used to assign types while building the tree.
     * @param setter The node type setter to use while building the tree, if null no types will be assigned.
     */
    public void setNodeTypeSetter(NodeTypeSetter setter) {
        this.typeSetter = setter;
    }

    /**
     * Clients wishing to assign node types while building a tree should implement this interface.
     */
    public interface NodeTypeSetter {
        /**
         * Method will be called when the tree builder is ready to assign node type, the implementation of this method should set type on the node.
         * @param node The node to assign type to.
         * @param depth The current depth in the tree.
         */
        void setNodeType(Node node, int depth);
    }


}
