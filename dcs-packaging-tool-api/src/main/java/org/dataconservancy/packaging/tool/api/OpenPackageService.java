package org.dataconservancy.packaging.tool.api;

import java.io.File;
import java.io.IOException;

import org.dataconservancy.packaging.tool.model.OpenedPackage;

/**
 * A service to open a package at various points in its life-cycle. The package
 * tree which is returned along with the package state is modified as needed so
 * that the nodes refer to files in the opened package.
 * 
 */
public interface OpenPackageService {
    /**
     * Open the package state of an package in the process of being created.
     * 
     * @param file
     *            Package state file
     * @return Opened package with base directory set to null.
     * @throws IOException
     */
    OpenedPackage openPackageState(File file) throws IOException;

    /**
     * Open a serialized package in a staging directory.
     * 
     * @param staging_dir
     *            Directory to uncompress the package.
     * @param file
     *            Serialized package.
     * @return Opened package.
     * @throws IOException
     */
    OpenedPackage openPackage(File staging_dir, File file) throws IOException;

    /**
     * Open a serialized package which has been uncompressed to a directory.
     * 
     * @param dir
     *            Directory of the uncompressed package
     * @return Opened package.
     * @throws IOException
     */
    OpenedPackage openExplodedPackage(File dir) throws IOException;
}
