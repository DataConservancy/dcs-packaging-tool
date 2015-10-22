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
        root.setFileInfo(createFileInfo("/bestfarm", "best farm", -1, 10123, 100, false));

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
        root.setFileInfo(createFileInfo("/farm", "farm", -1, 10123, 100, false));

        Node barn = new Node(URI.create("test:barn"));
        barn.setNodeType(profile.getBarnNodeType());
        barn.setFileInfo(createFileInfo("/farm/barn", "barn", -1, 200, 400, false));

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
        root.setFileInfo(createFileInfo("/farm", "farm", -1, -1, 100, false));

        Node barn = new Node(URI.create("test:barn1"));
        barn.setNodeType(profile.getBarnNodeType());
        barn.setFileInfo(createFileInfo("/farm/barn1", "barn1", -1, 200, 400, false));

        Node cow = new Node(URI.create("test:cow1"));
        cow.setNodeType(profile.getCowNodeType());
        cow.setFileInfo(createFileInfo("/farm/barn1/cow1", "cow1", -1, 600, 600, false));

        Node video = new Node(URI.create("test:cow1_video"));
        video.setNodeType(profile.getMediaNodeType());
        video.setFileInfo(
                createFileInfo("/farm/barn1/cow1/lastgoodbye.mp4", "lastgoodbye.mp4", 10000000, 800, 1000, true));

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
        root.setFileInfo(createFileInfo("/farm", "farm", -1, 0, 100, false));

        Node barn = new Node(URI.create("test:barn"));
        barn.setNodeType(profile.getBarnNodeType());
        barn.setFileInfo(createFileInfo("/farm/barn", "barn1", -1, 200, 400, false));

        Node sounds = new Node(URI.create("test:farm_sounds"));
        sounds.setNodeType(profile.getMediaNodeType());
        sounds.setFileInfo(
                createFileInfo("/farm/sounds.mp3", "sounds.mp3", 1300000, 8000, 19000, true));
        
        Node photo  = new Node(URI.create("test:barn_photo"));
        photo.setNodeType(profile.getMediaNodeType());
        photo.setFileInfo(
                createFileInfo("/farm/barn/photo.jpg", "photo.jpg", 300000, 80000, 100000, true));

        root.addChild(barn);
        root.addChild(sounds);
        barn.addChild(photo);

        return root;
    }

    private FileInfo createFileInfo(String path, String name, final long size, final long create_date_ms,
            final long mod_date_ms, boolean file) {
        FileInfo fileInfo = new FileInfo(Paths.get(path).toUri(), name);
        fileInfo.setSize(size);
        fileInfo.setCreationTime(FileTime.fromMillis(create_date_ms));
        fileInfo.setLastModifiedTime(FileTime.fromMillis(mod_date_ms));
        fileInfo.setIsFile(file);
        fileInfo.setIsDirectory(!file);

        if (file) {
            fileInfo.addFormat("application/octet-stream");
            fileInfo.addChecksum(Algorithm.MD5, "12345");
            fileInfo.addChecksum(Algorithm.SHA1, "54321");
        }
        return fileInfo;
    }

    public FarmDomainProfile getProfile() {
        return profile;
    }
}
