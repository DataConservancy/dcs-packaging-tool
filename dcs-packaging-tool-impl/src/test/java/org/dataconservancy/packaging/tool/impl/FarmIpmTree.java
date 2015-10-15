package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo.Algorithm;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Tree of in memory IPM node used for testing.
 */
public class FarmIpmTree {
    private final Node root;
    private final FarmDomainProfile profile;

    public FarmIpmTree() {
        this.profile = new FarmDomainProfile();
        this.root = new Node(URI.create("test:farm"));

        root.setNodeType(profile.getFarmNodeType());
        root.setFileInfo(createFileInfo("/farm", -1, -1, 100));

        Node barn = new Node(URI.create("test:barn1"));
        barn.setNodeType(profile.getBarnNodeType());
        barn.setFileInfo(createFileInfo("/farm/barn1", -1, 200, 400));
        
        Node cow = new Node(URI.create("test:cow1"));
        cow.setNodeType(profile.getCowNodeType());
        cow.setFileInfo(createFileInfo("/farm/barn1/cow1", -1, 600, 600));
        
        Node video = new Node(URI.create("test:cow1_video"));
        video.setNodeType(profile.getMediaNodeType());
        video.setFileInfo(createFileInfo("/farm/barn1/cow1/lastgoodbye.mp4", 10000000, 800, 1000));
        
        root.addChild(barn);
        barn.addChild(cow);
        cow.addChild(video);
    }
    
    // TODO Modify FileInfo not to use BasicFileAttributes?
    
    private FileInfo createFileInfo(String path, final long size, final long create_date_ms, final long mod_date_ms) {
        BasicFileAttributes attrs = new BasicFileAttributes() {
            @Override
            public long size() {
                return size;
            }

            @Override
            public FileTime lastModifiedTime() {
                return FileTime.fromMillis(mod_date_ms);
            }

            @Override
            public FileTime lastAccessTime() {
                return FileTime.fromMillis(mod_date_ms);
            }

            @Override
            public boolean isSymbolicLink() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isRegularFile() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isOther() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isDirectory() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Object fileKey() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public FileTime creationTime() {
                return FileTime.fromMillis(create_date_ms);
            }
        };

        List<String> formats = new ArrayList<String>();
        Map<Algorithm, String> checksums = new HashMap<>();

        return new FileInfo(Paths.get(path), attrs, formats, checksums);
    }

    public Node getRoot() {
        return root;
    }

    public FarmDomainProfile getProfile() {
        return profile;
    }
}
