package org.dataconservancy.packaging.tool.model.ipm;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.model.Checksum;

/**
 * Information about a file or directory.
 */
public class FileInfo {

    private URI location;
    private String name;
    private List<URI> formats;
    private Map<String, Checksum> checksums;
    private BasicFileAttributes fileAttributes;

    public FileInfo(Path path) {
        location = path.toUri();
        name = path.getFileName().toString();

        try {
            fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @return Location of file or directory.
     */
    public URI getLocation() {
        return location;
    }

    /**
     * @param algorithm Either MD5 or SHA1
     * @return Checksum in known format of the file or null if directory.
     */
    public String getChecksum(String algorithm) {
        String value = null;
        Checksum checksum = checksums.get(algorithm);
        if (checksum != null) {
            value = checksum.getValue();
        }
        return value;
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
    List<URI> getFormats() {
        return formats;
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
     * @return Whether or not a file is being described.
     */
    boolean isFile() {
        boolean isFile = false;
        if (fileAttributes != null) {
            isFile = fileAttributes.isRegularFile();
        }

        return isFile;
    }

    /**
     * @return Whether or not a directory is being described.
     */
    boolean isDirectory() {
        boolean isDirectory = false;
        if (fileAttributes != null) {
            isDirectory = fileAttributes.isDirectory();
        }

        return isDirectory;
    }

    /**
     * @return Creation time of file.
     */
    FileTime getCreationTime() {
        FileTime creationTime = null;
        if (fileAttributes != null) {
            creationTime = fileAttributes.creationTime();
        }

        return creationTime;
    }

    /**
     * @return Last modification time of file.
     */
    FileTime getLastModifiedTime() {
        FileTime modifiedTime = null;
        if (fileAttributes != null) {
            modifiedTime = fileAttributes.lastModifiedTime();
        }

        return modifiedTime;
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

        if (!location.equals(fileInfo.location)) {
            return false;
        }
        if (!name.equals(fileInfo.name)) {
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
        return !(fileAttributes !=
                     null ? !fileAttributes.equals(fileInfo.fileAttributes) :
                     fileInfo.fileAttributes != null);

    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (formats != null ? formats.hashCode() : 0);
        result = 31 * result + (checksums != null ? checksums.hashCode() : 0);
        result = 31 * result +
            (fileAttributes != null ? fileAttributes.hashCode() : 0);
        return result;
    }
}
