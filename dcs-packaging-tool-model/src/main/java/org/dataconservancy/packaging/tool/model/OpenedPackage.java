package org.dataconservancy.packaging.tool.model;

import java.io.File;

import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Represents a serialized package which has been extracted to a location on the
 * file system. The package state is returned unmodified from the opened
 * package. The tree stored in this object has been modified from the package
 * state so that it references files extracted to the base directory.
 */
public class OpenedPackage {
    private PackageState state;
    private File basedir;
    private Node tree;

    /**
     * @return the state
     */
    public PackageState getPackageState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setPackageState(PackageState state) {
        this.state = state;
    }

    /**
     * @return the basedir of the extracted package
     */
    public File getBaseDirectory() {
        return basedir;
    }

    /**
     * @param basedir
     *            the basedir of the extracted package to set
     */
    public void setBaseDirectory(File basedir) {
        this.basedir = basedir;
    }

    /**
     * @return the tree which points to extracted files
     */
    public Node getPackageTree() {
        return tree;
    }

    /**
     * @param tree
     *            the tree which points to extracted files to set
     */
    public void setPackageTree(Node tree) {
        this.tree = tree;
    }
}
