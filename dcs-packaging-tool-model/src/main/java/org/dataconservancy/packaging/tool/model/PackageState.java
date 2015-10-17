/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code PackageState} is an object responsible for maintaining the state information of individual package at any
 * given .
 */
public class PackageState {

    private String packageName;

    /**
     * Package's tree structure
     */
    //private Node packageTree;

    /**
     * List of domain profiles in-use in this package.
     */
    //private List<DomainProfile> domainProfileList;

    /**
     * Container of all of the domain objects in this package.
     */
    //private DomainProfileObjectStore domainProfileObjectStore;

    /**
     * A collection of metadata fields relevant to this package.
     */
    private List<PackageMetadata> packageMetadataList;

    /**
     * Package serialization/generation information
     */
    private File outputDirectory;

    /**
     * Metadata about the tools used to create this package
     */
    private ApplicationVersion creationToolVersion;

    public PackageState() {
        packageMetadataList = new ArrayList<>();
    }
    public PackageState(ApplicationVersion appVersion) {
        packageMetadataList = new ArrayList<>();
        this.creationToolVersion = appVersion;
    }

    /**
     * Name of the package, will be used as the name of the package file or folder
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * The default location at which the final package file/directory will be placed
     * @return
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Collection of other package level metadata that are specific to a packaging format, such as BagIt.
     * @return
     */
    public List<PackageMetadata> getPackageMetadataList() {
        return packageMetadataList;
    }

    public void setPackageMetadataList(List<PackageMetadata> packageMetadataList) {
        this.packageMetadataList = packageMetadataList;
    }

    public ApplicationVersion getCreationToolVersion() {
        return creationToolVersion;
    }

    public void setCreationToolVersion(ApplicationVersion creationToolVersion) {
        this.creationToolVersion = creationToolVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PackageState)) return false;

        PackageState that = (PackageState) o;

        if (creationToolVersion != null ? !creationToolVersion.equals(that.creationToolVersion) : that.creationToolVersion != null)
            return false;
        if (outputDirectory != null ? !outputDirectory.equals(that.outputDirectory) : that.outputDirectory != null)
            return false;
        if (packageMetadataList != null ? !packageMetadataList.equals(that.packageMetadataList) : that.packageMetadataList != null)
            return false;
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + (packageMetadataList != null ? packageMetadataList.hashCode() : 0);
        result = 31 * result + (outputDirectory != null ? outputDirectory.hashCode() : 0);
        result = 31 * result + (creationToolVersion != null ? creationToolVersion.hashCode() : 0);
        return result;
    }
}