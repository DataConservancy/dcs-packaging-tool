package org.dataconservancy.packaging.tool.model.ipm;

import java.net.URI;
import java.nio.file.attribute.FileTime;
import java.util.List;

/**
 * Information about a file or directory.
 */
public interface FileInfo {
    /**
     * @return Location of file or directory.
     */
    URI getLocation();

    /**
     * @return Checksum in known format of the file or null if directory.
     */
    String getChecksum();

    /**
     * @return Name of the file.
     */
    String getName();

    /**
     * @return List of formats for the file.
     */
    List<URI> getFormats();

    /**
     * @return Size of the file or -1 if directory.
     */
    long getSize();

    /**
     * @return Whether or a file is being described.
     */
    boolean isFile();

    /**
     * @return Whether or a file is being described.
     */
    boolean isDirectory();

    /**
     * @return Creation time of file.
     */
    FileTime getCreationTime();

    /**
     * @return Last modification time of file.
     */
    FileTime getLastModifiedTime();
}
