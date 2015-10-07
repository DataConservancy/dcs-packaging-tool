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

    private URI location;
    private String name;
    private List<String> formats;
    private Map<String, String> checksums;
    private BasicFileAttributes fileAttributes;

    public FileInfo(Path path) {
        location = path.toUri();
        name = path.getFileName().toString();

        try {
            fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (fileAttributes.isRegularFile()) {
                checksums = new HashMap<>();
                formats = new ArrayList<>();

                InputStream md5Fis = Files.newInputStream(path);
                String md5Checksum = ChecksumGeneratorVerifier.generateMD5checksum(md5Fis);
                checksums.put("MD5", md5Checksum);
                md5Fis.close();

                InputStream sha1Fis = Files.newInputStream(path);
                String sha1Checksum = ChecksumGeneratorVerifier.generateSHA1checksum(sha1Fis);
                checksums.put("SHA1", sha1Checksum);
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
        algorithm = algorithm.toUpperCase();
        String value = null;
        if (checksums != null) {
            value = checksums.get(algorithm);
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
    List<String> getFormats() {
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
        return !(fileAttributes !=
                     null ? !fileAttributes.equals(fileInfo.fileAttributes) :
                     fileInfo.fileAttributes != null);

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (formats != null ? formats.hashCode() : 0);
        result = 31 * result + (checksums != null ? checksums.hashCode() : 0);
        result = 31 * result + (fileAttributes != null ? fileAttributes.hashCode() : 0);
        return result;
    }
}
