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
     * Return a tree with types assigned.
     * 
     * <pre>
     *  /farm  (Farm)
     *  /farm/barn1/ (Barn)
     *  /farm/barn1/cow1 (Cow)
     *  /farm/barn1/cow1/lastgoodbye.mp4 (Media)
     * </pre>
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
        video.setFileInfo(createFileInfo("/farm/barn1/cow1/lastgoodbye.mp4", "lastgoodbye.mp4", 10000000, 800, 1000, true));
        
        root.addChild(barn);
        barn.addChild(cow);
        cow.addChild(video);
        
        return root;
    }

    private FileInfo createFileInfo(String path, String name, final long size, final long create_date_ms, final long mod_date_ms, boolean file) {
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
