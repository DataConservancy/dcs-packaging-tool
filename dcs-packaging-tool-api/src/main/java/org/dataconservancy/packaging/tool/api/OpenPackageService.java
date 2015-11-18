package org.dataconservancy.packaging.tool.api;

import java.io.File;
import java.io.IOException;

import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageState;

/**
 * A service to open a package at various points in its life-cycle.
 */
public interface OpenPackageService {
    /**
     * Open the package state of an package in the process of being created.
     * 
     * @param file
     *            Package state file
     * @return State of the package.
     * @throws IOException
     */
    PackageState openPackageState(File file) throws IOException;

    /**
     * Open a serialized package in a staging directory.
     * 
     * @param staging_dir
     *            Directory to uncompress the package.
     * @param file
     *            Serialized package.
     * @return State of the package.
     * @throws IOException
     */
    OpenedPackage openPackage(File staging_dir, File file) throws IOException;

    /**
     * Open a serialized package which has been uncompressed to a directory.
     * 
     * @param dir
     *            Directory of the uncompressed package
     * @return State of of the package.
     * @throws IOException
     */
    OpenedPackage openExplodedPackage(File dir) throws IOException;
}
