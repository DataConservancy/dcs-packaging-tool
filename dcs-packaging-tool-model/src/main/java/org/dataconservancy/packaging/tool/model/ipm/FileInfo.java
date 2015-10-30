package org.dataconservancy.packaging.tool.model.ipm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.model.DetectedFormat;
import org.dataconservancy.dcs.util.ChecksumGeneratorVerifier;
import org.dataconservancy.dcs.util.ContentDetectionService;

/**
 * Information about a file or directory.
 */
public class FileInfo {

    public enum Algorithm {
        SHA1,
        MD5
    }

    private URI location;
    private String name;
    private List<String> formats;
    private Map<Algorithm, String> checksums;
    private FileInfoAttributes fileAttributes;

    /**
     * Default constructor that should be used in most cases. Will read the file at the path location and load the necessary file attributes.
     * @param path The path to the file.
     */
    public FileInfo(Path path) {
        location = path.toUri();
        name = path.getFileName().toString();

        try {
            fileAttributes = new FileInfoAttributes(Files.readAttributes(path, BasicFileAttributes.class));
            if (fileAttributes.isRegularFile()) {
                checksums = new HashMap<>();
                formats = new ArrayList<>();

                InputStream md5Fis = Files.newInputStream(path);
                String md5Checksum = ChecksumGeneratorVerifier.generateMD5checksum(md5Fis);
                checksums.put(Algorithm.MD5, md5Checksum);
                md5Fis.close();

                InputStream sha1Fis = Files.newInputStream(path);
                String sha1Checksum = ChecksumGeneratorVerifier.generateSHA1checksum(sha1Fis);
                checksums.put(Algorithm.SHA1, sha1Checksum);
                sha1Fis.close();

                List<DetectedFormat> fileFormats = ContentDetectionService.getInstance().detectFormats(path.toFile());
                for (DetectedFormat format : fileFormats) {
                    formats.add(createFormatURIString(format));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor to use when loading existing file information.
     * @param path The path of the File info
     * @param fileAttributes The basic file attibutes of the file.
     */
    public FileInfo(Path path, BasicFileAttributes fileAttributes, List<String> formats, Map<Algorithm, String> checksums) {
        location = path.toUri();
        name = path.getFileName().toString();

        this.fileAttributes = new FileInfoAttributes(fileAttributes);
        this.formats = formats;
        this.checksums = checksums;
    }

    /**
     * Constructor that creates an essentially empty FileInfo object. Only location and name will be set.
     * Note: FileAttributes will not be created with this constructor and must be set manually.
     * @param location The location of the File system entity referenced by this FileInfo object.
     * @param name The name of the File System Entity referenced by this FileInfo object.
     */
    public FileInfo(URI location, String name) {
        this.location = location;
        this.name = name;
    }

    public FileInfo(URI location, String name, FileTime creationTime, FileTime modifiedTime, boolean isFile, boolean isDirectory, long size,
                    List<String> formats, Map<Algorithm, String> checksums) {
        this.location = location;
        this.name = name;

        this.formats = formats;
        this.checksums = checksums;

        fileAttributes = new FileInfoAttributes();
        fileAttributes.setCreationTime(creationTime);
        fileAttributes.setLastModifiedTime(modifiedTime);
        fileAttributes.setIsRegularFile(isFile);
        fileAttributes.setIsDirectory(isDirectory);
        fileAttributes.setSize(size);
    }

    /**
     * @return Location of file or directory.
     */
    public URI getLocation() {
        return location;
    }

    /**
     * @param algorithm The algorithm of the checksum either MD5 or SHA1
     * @return Checksum in known format of the file or null if directory.
     */
    public String getChecksum(Algorithm algorithm) {
        String value = null;
        if (checksums != null) {
            value = checksums.get(algorithm);
        }
        return value;
    }

    /**
     * Add a checksum for this FileInfo object.
     * @param algorithm The algorithm of the FileInfo object.
     * @param value The value of checksum.
     */
    public void addChecksum(Algorithm algorithm, String value) {
        if (checksums == null) {
            checksums = new HashMap<>();
        }

        checksums.put(algorithm, value);
    }

    /**
     * Sets the map of checksums
     * @param checksumMap The map of checksums for the FileInfo object.
     */
    public void setChecksums(Map<Algorithm, String> checksumMap) {
        this.checksums = checksumMap;
    }

    /**
     * @return Name of the file.
     */
    public String getName() {
        return name;
    }

    /**
     * @return List of formats for the file.
     */
    public List<String> getFormats() {
        return formats;
    }

    /**
     * Add a new format for this FileInfo object.
     * @param format The format to add.
     */
    public void addFormat(String format) {
        if (formats == null) {
            formats = new ArrayList<>();
        }

        formats.add(format);
    }

    /**
     * Sets the list of formats for the File backing this FileInfo object.
     * @param formats The list of formats to add.
     */
    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    /**
     * @return Size of the file or -1 if directory.
     */
    public long getSize() {
        long size = -1;
        if (fileAttributes != null) {
            size = fileAttributes.size();
        }

        return size;
    }

    /**
     * Sets the size of the File backing this FileInfo object.
     * @param size The size in bytes of the file backing this FileInfo object.
     */
    public void setSize(long size) {
        if (fileAttributes == null) {
            fileAttributes = new FileInfoAttributes();
        }

        fileAttributes.setSize(size);
    }

    /**
     * @return Whether or not a file is being described.
     */
    public boolean isFile() {
        boolean isFile = false;
        if (fileAttributes != null) {
            isFile = fileAttributes.isRegularFile();
        }

        return isFile;
    }

    /**
     * Sets whether or not the FileInfo describes a file on the file system.
     * @param isFile True if the FileInfo object describes a file, false otherwise.
     */
    public void setIsFile(boolean isFile) {
        if (fileAttributes == null) {
            fileAttributes = new FileInfoAttributes();
        }

        fileAttributes.setIsRegularFile(isFile);
    }

    /**
     * @return Whether or not a directory is being described.
     */
    public boolean isDirectory() {
        boolean isDirectory = false;
        if (fileAttributes != null) {
            isDirectory = fileAttributes.isDirectory();
        }

        return isDirectory;
    }

    /**
     * Sets whether or not the FileInfo describes a directory on the file system.
     * @param isDirectory True if the FileInfo object describes a directory, false otherwise.
     */
    public void setIsDirectory(boolean isDirectory) {
        if (fileAttributes == null) {
            fileAttributes = new FileInfoAttributes();
        }

        fileAttributes.setIsDirectory(isDirectory);
    }

    /**
     * @return Creation time of file.
     */
    public FileTime getCreationTime() {
        FileTime creationTime = null;
        if (fileAttributes != null) {
            creationTime = fileAttributes.creationTime();
        }

        return creationTime;
    }

    /**
     * Sets the time this file was created.
     * @param creationTime The FileTime representing when this file was created.
     */
    public void setCreationTime(FileTime creationTime) {
        if (fileAttributes == null) {
            fileAttributes = new FileInfoAttributes();
        }

        fileAttributes.setCreationTime(creationTime);
    }

    /**
     * @return Last modification time of file.
     */
    public FileTime getLastModifiedTime() {
        FileTime modifiedTime = null;
        if (fileAttributes != null) {
            modifiedTime = fileAttributes.lastModifiedTime();
        }

        return modifiedTime;
    }

    /**
     * Sets the time this file was last modified.
     * @param modifiedTime The FileTime representing when this file was last modified.
     */
    public void setLastModifiedTime(FileTime modifiedTime) {
        if (fileAttributes == null) {
            fileAttributes = new FileInfoAttributes();
        }

        fileAttributes.setLastModifiedTime(modifiedTime);
    }

    /**
     * Converts format id from the DcsFormat objects into formatURI string with qualifying namespace. Only applicable
     * to pronom format identifier at this point.
     * @param format the DetectedFormat object
     * @return a formatURI string with qualifying namespace
     */
    private String createFormatURIString(DetectedFormat format) {
        String formatString = "";
        if (format.getId() != null && !format.getId().isEmpty()) {
            formatString = "info:pronom/" + format.getId();
        } else if (format.getMimeType() != null && !format.getMimeType().isEmpty()) {
            formatString = format.getMimeType();
        }

        return formatString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileInfo)) {
            return false;
        }

        FileInfo fileInfo = (FileInfo) o;

        if (location != null ? !location.equals(fileInfo.location) :
            fileInfo.location != null) {
            return false;
        }
        if (name != null ? !name.equals(fileInfo.name) :
            fileInfo.name != null) {
            return false;
        }
        if (formats != null ? !formats.equals(fileInfo.formats) :
            fileInfo.formats != null) {
            return false;
        }
        if (checksums != null ? !checksums.equals(fileInfo.checksums) :
            fileInfo.checksums != null) {
            return false;
        }

        return true;

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (formats != null ? formats.hashCode() : 0);
        result = 31 * result + (checksums != null ? checksums.hashCode() : 0);
        return result;
    }
    
    

    @Override
    public String toString() {
        return "FileInfo [location=" + location + ", name=" + name + ", formats=" + formats + ", checksums=" + checksums
                + ", fileAttributes=" + fileAttributes + "]";
    }


    private class FileInfoAttributes implements BasicFileAttributes {
        private FileTime lastModifiedTime;
        private FileTime creationTime;
        private boolean isRegularFile;
        private boolean isDirectory;
        private boolean isSymbolicLink;
        private long size;

        public FileInfoAttributes() {

        }

        public FileInfoAttributes(BasicFileAttributes superAttributes) {
            lastModifiedTime = superAttributes.lastModifiedTime();
            creationTime = superAttributes.creationTime();
            isRegularFile = superAttributes.isRegularFile();
            isDirectory = superAttributes.isDirectory();
            isSymbolicLink = superAttributes.isSymbolicLink();
            size = superAttributes.size();
        }

        @Override
        public FileTime lastModifiedTime() {
            return lastModifiedTime;
        }

        public void setLastModifiedTime(FileTime lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
        }

        @Override
        public FileTime lastAccessTime() {
            return null;
        }

        @Override
        public FileTime creationTime() {
            return creationTime;
        }

        public void setCreationTime(FileTime creationTime) {
            this.creationTime = creationTime;
        }

        @Override
        public boolean isRegularFile() {
            return isRegularFile;
        }

        public void setIsRegularFile(boolean regularFile) {
            this.isRegularFile = regularFile;
        }

        @Override
        public boolean isDirectory() {
            return isDirectory;
        }

        public void setIsDirectory(boolean isDirectory) {
            this.isDirectory = isDirectory;
        }

        @Override
        public boolean isSymbolicLink() {
            return isSymbolicLink;
        }

        public void setIsSymbolicLink(boolean symbolicLink) {
            this.isSymbolicLink = symbolicLink;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        @Override
        public Object fileKey() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FileInfoAttributes)) {
                return false;
            }

            FileInfoAttributes that = (FileInfoAttributes) o;

            if (isRegularFile != that.isRegularFile) {
                return false;
            }
            if (isDirectory != that.isDirectory) {
                return false;
            }
            if (isSymbolicLink != that.isSymbolicLink) {
                return false;
            }
            if (size != that.size) {
                return false;
            }
            if (lastModifiedTime !=
                null ? !lastModifiedTime.equals(that.lastModifiedTime) :
                that.lastModifiedTime != null) {
                return false;
            }
            return !(
                creationTime != null ? !creationTime.equals(that.creationTime) :
                    that.creationTime != null);
        }

        @Override
        public int hashCode() {
            int result =
                lastModifiedTime != null ? lastModifiedTime.hashCode() : 0;
            result = 31 * result +
                (creationTime != null ? creationTime.hashCode() : 0);
            result = 31 * result + (isRegularFile ? 1 : 0);
            result = 31 * result + (isDirectory ? 1 : 0);
            result = 31 * result + (isSymbolicLink ? 1 : 0);
            result = 31 * result + (int) (size ^ (size >>> 32));
            return result;
        }
        
        @Override
        public String toString() {
            return "FileInfoAttributes [lastModifiedTime=" + lastModifiedTime + ", creationTime=" + creationTime
                    + ", isRegularFile=" + isRegularFile + ", isDirectory=" + isDirectory + ", isSymbolicLink="
                    + isSymbolicLink + ", size=" + size + "]";
        }
    }
}
