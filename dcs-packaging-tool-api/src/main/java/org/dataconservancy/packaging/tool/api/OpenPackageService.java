package org.dataconservancy.packaging.tool.api;

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
     * @throws IOException if we cannot open the file
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
     * @throws IOException if we cannot open the serialized package file
     */
    OpenedPackage openPackage(File staging_dir, File file) throws IOException;

    /**
     * Open a serialized package which has been uncompressed to a directory.
     * 
     * @param dir
     *            Directory of the uncompressed package
     * @return Opened package.
     * @throws IOException if we cannot open the file
     */
    OpenedPackage openExplodedPackage(File dir) throws IOException;
}
