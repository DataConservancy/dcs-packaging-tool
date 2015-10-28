package org.dataconservancy.packaging.tool.profile;


import org.dataconservancy.packaging.tool.impl.support.IpmTreeFactory;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.util.Random;

/**
 * Utility class used for generating trees with the DcBO Profile.
 */
public class DcBoIpmFactory {

    private final DcsBOProfile profile;
    private final IpmTreeFactory treeFactory;

    public DcBoIpmFactory() {
        profile = new DcsBOProfile();
        treeFactory = new IpmTreeFactory();
    }

    /**
     * Creates a tree that is a single collection node.
     * @return The root of the tree.
     */
    public Node createSingleCollectionTree() {
        return treeFactory.createSingleDirectoryTree(profile.getCollectionNodeType());
    }

    /**
     * Creates a single node tree that is a project.
     * @return The root of the tree.
     */
    public Node createSingleProjectTree() {
        return treeFactory.createSingleDirectoryTree(profile.getProjectNodeType());
    }

    /**
     * Creates a single node tree that is a data item.
     * @return The root of the tree.
     */
    public Node createSingleDataItemTree() {
        return treeFactory.createSingleDirectoryTree(profile.getDataItemNodeType());
    }

    /**
     * Creates a single node tree that is a file.
     * @return The root of the tree.
     */
    public Node createSingleFileTree() {
        return treeFactory.createSingleFileTree(profile.getFileNodeType());
    }

    /**
     * Creates a tree that is two nodes, both collections.
     * @return The root of the tree.
     */
    public Node createSubCollectionTree() {
        return treeFactory.createTwoDirectoryTree(profile.getCollectionNodeType(), profile.getCollectionNodeType());
    }

    /**
     * Creates a tree that is two nodes, a Data Item as root and a File as it's child.
     * @return The root of the tree.
     */
    public Node createDataItemSingleFileTree() {
        return treeFactory.createSingleDirectoryFileTree(profile.getCollectionNodeType(), profile.getFileNodeType());
    }

    /**
     * Creates a linear tree that assigns all the profile types.
     * The tree has the following structure
     * project
     * --collection
     * ----data item
     * ------file
     *
     * @return The root of the tree.
     */
    public Node createSmallLinearTree() {
        IpmTreeFactory.NodeTypeSetter nodeTypeSetter = (node, depth) -> {
            switch (depth) {
                case 0:
                    node.setNodeType(profile.getProjectNodeType());
                    break;
                case 1:
                    node.setNodeType(profile.getCollectionNodeType());
                    break;
                case 2:
                    node.setNodeType(profile.getDataItemNodeType());
                    break;
                case 3:
                    node.setNodeType(profile.getFileNodeType());
                    break;
            }

        };
        treeFactory.setNodeTypeSetter(nodeTypeSetter);

        Node root = treeFactory.createTree(4, 1, false);

        treeFactory.setNodeTypeSetter(null);
        return root;
    }

    /**
     * Creates a small tree of depth 4, with two branches assigning random valid types.
     * @return The root of the tree.
     */
    public Node createSmallTree() {
        treeFactory.setNodeTypeSetter(dcBoSetter);

        Node root = treeFactory.createTree(4, 2, false);

        treeFactory.setNodeTypeSetter(null);
        return root;
    }

    /**
     * Creates a large tree of depth 15 with 4 branches assigning random valid types.
     * @return The root of the tree.
     */
    public Node createLargeTree() {
        treeFactory.setNodeTypeSetter(dcBoSetter);
        Node root = treeFactory.createTree(15, 4, false);

        treeFactory.setNodeTypeSetter(null);

        return null;
    }

    private IpmTreeFactory.NodeTypeSetter dcBoSetter = new IpmTreeFactory.NodeTypeSetter() {
        @Override
        public void setNodeType(Node node, int depth) {
            if (node.getFileInfo().isFile()) {
                node.setNodeType(profile.getFileNodeType());
            } else {
                if (!node.isLeaf()) {
                    boolean containsDirectory = false;
                    for (Node child : node.getChildren()) {
                        if (!child.getNodeType().equals(profile.getFileNodeType())) {
                            containsDirectory = true;
                            break;
                        }
                    }

                    if (containsDirectory) {
                        node.setNodeType(profile.getCollectionNodeType());
                    } else {
                        node.setNodeType(profile.getDataItemNodeType());
                    }
                } else {
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        node.setNodeType(profile.getCollectionNodeType());
                    } else {
                        node.setNodeType(profile.getDataItemNodeType());
                    }
                }
            }
        }
    };
}
