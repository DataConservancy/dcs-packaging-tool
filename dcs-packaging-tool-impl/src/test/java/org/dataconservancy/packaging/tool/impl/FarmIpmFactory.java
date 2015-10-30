package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import org.dataconservancy.packaging.tool.impl.support.IpmTreeFactory;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo.Algorithm;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Factory for creating trees of IPM nodes for testing.
 */
public class FarmIpmFactory {
    private final FarmDomainProfile profile;
    private final IpmTreeFactory treeFactory;

    public FarmIpmFactory() {
        this.profile = new FarmDomainProfile();
        this.treeFactory = new IpmTreeFactory();
    }

    /**
     * Return a tree with types assigned of a single directory.
     * 
     * <pre>
     *  /bestfarm                (Farm)
     * </pre>
     * 
     * @return root of tree.
     */
    public Node createSingleDirectoryTree() {
        Node root = treeFactory.createSingleDirectoryTree(profile.getFarmNodeType());

        return root;
    }

    /**
     * Return a tree with types assigned for a single file. The tree is invalid.
     * 
     * <pre>
     *  /moo.wav                (Media)
     * </pre>
     * 
     * @return root of tree.
     */
    public Node createInvalidSingleFileTree() {
        Node root = treeFactory.createSingleFileTree(profile.getMediaNodeType());

        return root;
    }

    /**
     * Return a tree with types assigned of a single directory with a single
     * subdirectory.
     * 
     * <pre>
     *  /farm                (Farm)
     *  /far/barn/           (Barn)
     * </pre>
     * 
     * @return root of tree.
     */
    public Node createTwoDirectoryTree() {
        Node root = treeFactory.createTwoDirectoryTree(profile.getFarmNodeType(), profile.getBarnNodeType());

        return root;
    }
    
    /**
     * Return a tree with types assigned of a single directory with a single
     * subdirectory.
     * 
     * <pre>
     *  /farm                (Farm)
     *  /far/trough/         (Trough)
     * </pre>
     * 
     * @return root of tree.
     */
    public Node createTwoDirectoryTree2() {
        Node root = treeFactory.createTwoDirectoryTree(profile.getFarmNodeType(), profile.getTroughNodeType());

        return root;
    }

    /**
     * Return a tree with types assigned.
     * 
     * <pre>
     *  /farm  (Farm)
     *  /farm/barn1/ (Barn)
     *  /farm/barn1/cow1 (Cow)
     *  /farm/barn1/cow1/lastgoodbye.mp4 (Media)
     * </pre>
     * 
     * @return root of tree.
     */
    public Node createSimpleTree() {
        IpmTreeFactory.NodeTypeSetter nodeTypeSetter = new IpmTreeFactory.NodeTypeSetter() {
            @Override
            public void setNodeType(Node node, int depth) {
                switch (depth) {
                    case 0:
                        node.setNodeType(profile.getFarmNodeType());
                        break;
                    case 1:
                        node.setNodeType(profile.getBarnNodeType());
                        break;
                    case 2:
                        node.setNodeType(profile.getCowNodeType());
                        break;
                    case 3:
                        node.setNodeType(profile.getMediaNodeType());
                        break;
                }

            }
        };
        treeFactory.setNodeTypeSetter(nodeTypeSetter);

        Node root = treeFactory.createTree(4, 1, false);

        treeFactory.setNodeTypeSetter(null);
        return root;
    }

    /**
     * Return a tree with types assigned.
     * 
     * <pre>
     *  /farm                (Farm)
     *  /farm/sounds.mp3     (Media)
     *  /farm/barn/           (Barn)
     *  /farm/barn/photo.jpg (Media)
     * </pre>
     * 
     * @return root of tree.
     */
    public Node createSimpleTree2() {
        //TODO Create a method in tree factory that can do this
        Node root = new Node(URI.create("test:farm"));

        root.setNodeType(profile.getFarmNodeType());
        root.setFileInfo(create_directory_info("/farm", "farm"));

        Node barn = new Node(URI.create("test:barn"));
        barn.setNodeType(profile.getBarnNodeType());
        barn.setFileInfo(create_directory_info("/farm/barn", "barn1"));

        Node sounds = new Node(URI.create("test:farm_sounds"));
        sounds.setNodeType(profile.getMediaNodeType());
        sounds.setFileInfo(create_file_info("/farm/sounds.mp3", "sounds.mp3"));

        Node photo = new Node(URI.create("test:barn_photo"));
        photo.setNodeType(profile.getMediaNodeType());
        photo.setFileInfo(create_file_info("/farm/barn/photo.jpg", "photo.jpg"));

        root.addChild(barn);
        root.addChild(sounds);
        barn.addChild(photo);

        return root;
    }

    /**
     * Return a tree of the given size with the given depth and branching.
     * Every inner node is a Farm. Every leaf is a Media.
     * 
     * @return root of tree.
     */
    public Node createCompleteTree(int depth, int branching) {
        IpmTreeFactory.NodeTypeSetter nodeTypeSetter = (node, depth1) -> {
            if (node.isLeaf()) {
                node.setNodeType(profile.getMediaNodeType());
            } else {
                node.setNodeType(profile.getFarmNodeType());
            }

        };

        treeFactory.setNodeTypeSetter(nodeTypeSetter);
        Node root = treeFactory.createTree(depth, branching, false);

        treeFactory.setNodeTypeSetter(null);
        return root;
    }

    private FileInfo create_directory_info(String path, String name) {
        FileInfo result = new FileInfo(Paths.get(path).toUri(), name);

        result.setIsDirectory(true);
        result.setCreationTime(FileTime.fromMillis(400000));
        result.setLastModifiedTime(FileTime.fromMillis(600000));

        return result;
    }

    private FileInfo create_file_info(String path, String name) {
        FileInfo result = new FileInfo(Paths.get(path).toUri(), name);

        result.setIsFile(true);
        result.setSize(120032);

        result.setCreationTime(FileTime.fromMillis(10000000));
        result.setLastModifiedTime(FileTime.fromMillis(2000000));

        result.addFormat("application/octet-stream");
        result.addChecksum(Algorithm.MD5, "12345");
        result.addChecksum(Algorithm.SHA1, "54321");

        return result;
    }

    public FarmDomainProfile getProfile() {
        return profile;
    }
}
