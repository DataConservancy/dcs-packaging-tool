package org.dataconservancy.packaging.tool.model;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
