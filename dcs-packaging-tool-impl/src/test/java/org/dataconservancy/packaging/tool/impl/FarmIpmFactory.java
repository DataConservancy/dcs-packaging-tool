package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo.Algorithm;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Factory for creating trees of IPM nodes for testing.
 */
public class FarmIpmFactory {
    private final FarmDomainProfile profile;

    public FarmIpmFactory() {
        this.profile = new FarmDomainProfile();
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
        Node root = new Node(URI.create("test:bestfarm"));

        root.setNodeType(profile.getFarmNodeType());
        root.setFileInfo(create_directory_info("/bestfarm", "best farm"));

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
        Node root = new Node(URI.create("test:moo"));

        root.setNodeType(profile.getMediaNodeType());
        root.setFileInfo(create_file_info("/moo.wav", "Moo!"));

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
        Node root = new Node(URI.create("test:farm"));

        root.setNodeType(profile.getFarmNodeType());
        root.setFileInfo(create_directory_info("/farm", "farm"));

        Node barn = new Node(URI.create("test:barn"));
        barn.setNodeType(profile.getBarnNodeType());
        barn.setFileInfo(create_directory_info("/farm/barn", "barn"));

        root.addChild(barn);

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
        Node root = new Node(URI.create("test:farm"));

        root.setNodeType(profile.getFarmNodeType());
        root.setFileInfo(create_directory_info("/farm", "farm"));

        Node barn = new Node(URI.create("test:barn1"));
        barn.setNodeType(profile.getBarnNodeType());
        barn.setFileInfo(create_directory_info("/farm/barn1", "barn1"));

        Node cow = new Node(URI.create("test:cow1"));
        cow.setNodeType(profile.getCowNodeType());
        cow.setFileInfo(create_directory_info("/farm/barn1/cow1", "cow1"));

        Node video = new Node(URI.create("test:cow1_video"));
        video.setNodeType(profile.getMediaNodeType());
        video.setFileInfo(create_file_info("/farm/barn1/cow1/lastgoodbye.mp4", "lastgoodbye.mp4"));

        root.addChild(barn);
        barn.addChild(cow);
        cow.addChild(video);

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
        return create_large_tree(0, depth, branching, 0);
    }

    private Node create_large_tree(int depth, int max_depth, int branching, int node_id) {
        Node node = new Node(URI.create("test:" + depth + "," + node_id));

        if (++depth < max_depth) {
            node.setFileInfo(create_directory_info("/" + depth + "/" + node_id + "/", "dir"));
            node.setNodeType(profile.getFarmNodeType());
            
            for (int branch = 0; branch < branching; branch++) {
                node.addChild(create_large_tree(depth, max_depth, branching, branch));
            }
        } else {
            node.setFileInfo(create_file_info("/" + depth + "/" + node_id, "file"));
            node.setNodeType(profile.getMediaNodeType());
        }


        return node;
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
