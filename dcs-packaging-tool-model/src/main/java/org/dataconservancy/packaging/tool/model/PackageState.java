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

import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.io.File;

import org.dataconservancy.packaging.tool.model.ser.SerializationScope;
import org.dataconservancy.packaging.tool.model.ser.Serialize;
import org.dataconservancy.packaging.tool.model.ser.StreamId;

import java.net.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;


/**
 * {@code PackageState} is an object responsible for maintaining the state information of individual package at any
 * given .
 */
public class PackageState {

    @Serialize(streamId = StreamId.PACKAGE_NAME)
    private String packageName;

    /**
     * Package's tree structure
     */
    private Model packageTree;

    /**
     * List of domain profile ids in-use in this package.
     */
    @Serialize(streamId = StreamId.DOMAIN_PROFILE_LIST)
    private List<URI> domainProfileIdList;

    /**
     * A map of all of the user specified properties that are set outside of the profiles, keyed by the ID of the node.
     */
    @Serialize(streamId = StreamId.USER_SPECIFIED_PROPERTIES, scope = {SerializationScope.WIP})
    private Map<URI, List<Property>> userSpecifiedProperties;

    /**
     * Container of all of the domain objects in this package.
     */
    @Serialize(streamId = StreamId.DOMAIN_OBJECTS, scope = {SerializationScope.WIP, SerializationScope.PACKAGE})
    private Model domainObjectRDF;

    /**
     * A map of metadata fields relevant to this package, keyed by the name of the metadata field to their associated
     * list of values. The map maintains entry by the order of insertion.
     */
    @Serialize(streamId = StreamId.PACKAGE_METADATA)
    private LinkedHashMap<String, List<String>> packageMetadataList;

    /**
     * Metadata about the tools used to create this package
     */
    @Serialize(streamId = StreamId.APPLICATION_VERSION)
    private ApplicationVersion creationToolVersion;

    // TODO: remove; I don't think this should be part of the package state
    private File outputDirectory;

    public PackageState() {
        packageMetadataList = new LinkedHashMap<>();
    }

    public PackageState(ApplicationVersion appVersion) {
        packageMetadataList = new LinkedHashMap<>();
        this.creationToolVersion = appVersion;
    }

    /**
     * @return Name of the package, will be used as the name of the package file or folder
     */
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the set of metadata fields used to describe this package.
     */
    public Set<String> getMetadataFields() {
        return packageMetadataList.keySet();
    }

    /**
     * Returns list of values for package metadata named {@code fieldName}
     */
    public List<String> getPackageMetadataValues(String fieldName) {
        return packageMetadataList.get(fieldName);
    }

    /**
     * Allows one or more values of the specified package metadata to be added.
     * @param fieldName
     * @param values
     */
    public void addPackageMetadata(String fieldName, String ... values) {
        if (packageMetadataList.get(fieldName) == null) {
            packageMetadataList.put(fieldName, new ArrayList<>());
        }
        packageMetadataList.get(fieldName).addAll(Arrays.asList(values));
    }

    /**
     * Assign a map metadata fields and their values to this package state.
     * @param metadataList
     */
    public void setPackageMetadataList(LinkedHashMap <String, List<String>> metadataList) {
        this.packageMetadataList = metadataList;
    }

    public LinkedHashMap<String, List<String>> getPackageMetadataList() {
        return packageMetadataList;
    }

    public Model getPackageTree() {
        return packageTree;
    }

    public void setPackageTree(Model packageTree) {
        this.packageTree = packageTree;
    }

    /**
     * Returns version information about the tool used to create this package.
     */
    public ApplicationVersion getCreationToolVersion() {
        return creationToolVersion;
    }

    public void setCreationToolVersion(ApplicationVersion creationToolVersion) {
        this.creationToolVersion = creationToolVersion;
    }

    public void setDomainProfileIdList(List<URI> domainProfileIdList){
        this.domainProfileIdList = domainProfileIdList;
    }

    public List<URI> getDomainProfileIdList() {
        return domainProfileIdList;
    }
    
    public Model getDomainObjectRDF() {
        return domainObjectRDF;
    }

    
    public void setDomainObjectRDF(Model domainObjectRDF) {
        this.domainObjectRDF = domainObjectRDF;
    }

    /**
     * Determines whether this package state currently has any metadata or not, mainly used for opening existing packages.
     * @return true or false based on availability.
     */
    public boolean hasPackageMetadataValues() {
        return packageMetadataList != null && !packageMetadataList.isEmpty();
    }

    // TODO: remove; I don't think this should be part of the package state
    public File getOutputDirectory() {
        return outputDirectory;
    }

    // TODO: remove; I don't think this should be part of the package state
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Map<URI, List<Property>> getUserSpecifiedProperties() {
        return userSpecifiedProperties;
    }

    public void setUserSpecifiedProperties(
        Map<URI, List<Property>> userSpecifiedProperties) {
        this.userSpecifiedProperties = userSpecifiedProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PackageState)) {
            return false;
        }

        PackageState that = (PackageState) o;

        if (packageName != null ? !packageName.equals(that.packageName) :
            that.packageName != null) {
            return false;
        }
        if (outputDirectory != null ? !outputDirectory.equals(that.outputDirectory) :
                that.outputDirectory != null) {
                return false;
        }
        if (packageTree != null ? !packageTree.equals(that.packageTree) :
            that.packageTree != null) {
            return false;
        }
        if (domainProfileIdList !=
            null ? !domainProfileIdList.equals(that.domainProfileIdList) :
            that.domainProfileIdList != null) {
            return false;
        }
        if (packageMetadataList !=
            null ? !packageMetadataList.equals(that.packageMetadataList) :
            that.packageMetadataList != null) {
            return false;
        }
        if (userSpecifiedProperties !=
            null ? !userSpecifiedProperties.equals(that.userSpecifiedProperties) :
            that.userSpecifiedProperties != null) {
            return false;
        }
        /* This is not the notion of equality we want here, but there's nothing we can reasonably do about it */
        if (domainObjectRDF !=
                null ? !(domainObjectRDF == that.domainObjectRDF) :
                that.domainObjectRDF != null) {
                return false;
            }
        return !(creationToolVersion !=
                     null ? !creationToolVersion.equals(that.creationToolVersion) :
                     that.creationToolVersion != null);

    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result =
            31 * result + (packageTree != null ? packageTree.hashCode() : 0);
        result = 31 * result +
            (domainProfileIdList != null ? domainProfileIdList.hashCode() : 0);
        result = 31 * result +
            (packageMetadataList != null ? packageMetadataList.hashCode() : 0);
        result = 31 * result +
            (outputDirectory != null ? outputDirectory.hashCode() : 0);
        result = 31 * result +
            (creationToolVersion != null ? creationToolVersion.hashCode() : 0);
        result = 31 * result +
                (domainObjectRDF != null ? domainObjectRDF.hashCode() : 0);
        result = 31 * result +
            (userSpecifiedProperties != null ? userSpecifiedProperties.hashCode() : 0);
        return result;
    }
}
