/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.api;

import org.dataconservancy.packaging.tool.model.*;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Provides convenient methods around the content of a PackageOntology instance.
 * Implementation of this service has to be provided with a concrete instance of a PackageOntology.
 *
 */
public interface PackageOntologyService {
    /**
     * Returns a set of valid types in the specified {@link org.dataconservancy.packaging.tool.model.PackageOntology}
     */
    public Set<String> getValidTypes();

    /**
     * Given a {@link org.dataconservancy.packaging.tool.model.PackageTree}, returns a set of valid alternative types
     * for the {@link org.dataconservancy.packaging.tool.model.PackageArtifact} identified by {@code currentArtifactId}
     * according to its position in the graph and the ontology associated with this service.
     *
     * @param tree  the PackageTree
     * @param  currentArtifactId the ID of the current artifact
     * @return  a set of valid alternative types
     * for the current artifact
     */
    public Set<String> getValidTypes(PackageTree tree, String currentArtifactId) throws PackageOntologyException;

    /**
     * Returns a map of valid property's names and types for a given {@link org.dataconservancy.packaging.tool.model.PackageArtifact}
     * @param currentArtifact the artifact for which the valid list of properties is being requested.
     * @return a map of String value as names of the properties and String value as type name of the property, or an empty map of String value if the provided artifact is empty (contains null, empty string values).
     */
    public Map<String, String> getProperties(PackageArtifact currentArtifact);

    /**
     * Returns a map of valid property's names and types for a given String name of an artifact Type.
     * @param typeName the name of artifact type for which the valid list of properties is being requested.
     * @return a map of String value as names of the properties and String value as type name of the property, or an empty map of String value if the provided artifact is empty (contains null, empty string values).
     */
    public Map<String, String> getProperties(String typeName);

    /**
     * Returns a set of all valid creator property for a given {@link org.dataconservancy.packaging.tool.model.PackageArtifact}
     * @param currentArtifact the artifact for which the valid list of creator properties is being requested.
     * @return a set of string values representing the property names for each valid creator property
     */
    public Set<String> getCreatorProperties(PackageArtifact currentArtifact);
    
    /**
     * Given a valid PackageDescription, returns a tree of {@link org.dataconservancy.packaging.tool.model.PackageNode}s,
     * keyed by the ids of the artifacts in the PackageNodes.
     * @param packageDescription  the PackageDescription
     * @param contentRoot the root directory of the package content
     * @return a tree of PackageNodes
     */
    public PackageTree buildPackageTree(PackageDescription packageDescription, File contentRoot) throws PackageOntologyException;

    /**
     * Ensure that the PackageArtifact has the required properties according to specified ontology. Throws
     * {@code PackageOntolgoException} when one or more required properties are not found.
     *
     */
    public void validateProperties(PackageArtifact artifact) throws PackageOntologyException;

    /**
     * Performs necessary mapping to change the {@code artifact}'s type to the new type specified by {@code newTypeName}.
     * @param packageDesc  the PackageDescription
     * @param packageTree tree containing the package artifact. Will be used to determine whether the new type is a valid one.
     * @param packageArtifact the artifact whose type is to be changed
     * @param contentRoot  the root directory of the content for this package
     * @param newTypeName the name of the new type
     */
    public void changeType(PackageDescription packageDesc, PackageTree packageTree, PackageArtifact packageArtifact, File contentRoot, String newTypeName) throws PackageOntologyException;
    
    /**
     * Gets the max number of times a property can occur. 
     * @param artifact The artifact which the property is attached to.
     * @param property The name of the property
     * @param type The containing property type if the property is a sub property contained in a group, can be empty or null.
     * @return The maximum number of times a property can occur, or -1 if the property can't be found.
     */
    public int getPropertyMaxOccurrences(PackageArtifact artifact, String property, String type);
    
    /**
     * Gets the minimum number of times a property can occur.
     * @param artifact The artifact which the property is attached to.
     * @param property The name of the property
     * @param type The containing property type if the property is a sub property contained in a group, can be empty or null.
     * @return The minimum number of times a property can occur, or -1 if the property can't be found.
     */
    public int getPropertyMinOccurrences(PackageArtifact artifact, String property, String type);
    
    /**
     * Returns a set of all the property names that are part of the given group type.
     * @param propertyType The group property to get the names for.
     * @return The set of all the property names in the group, or an empty set if the propertyType isn't a group.
     */
    public Set<String> getGroupPropertyNames(String propertyType);
    
    /**
     * Retrieves the type of a sub property that is part of a complex property type.
     * @param complexPropertyType The type of the complex property that contains the property.
     * @param subPropertyName The name of the property to retrieve the type for.
     * @return The string representing the registered type for the property, or an empty string if the property name isn't found in the complext type.
     */
    public String getComplexPropertySubPropertyType(String complexPropertyType, String subPropertyName);
    
    /**
     * Returns true if the given property type is a complex property made up of multiple fields, false otherwise.
     * @param propertyType The property type to check
     * @return True if the property is made up of several property fields, false otherwise.
     */
    public boolean isPropertyComplex(String propertyType);
    
    /**
     * Returns true if the given relationship type on the artifact is hierarchical
     * @param artifact The artifact the relationship is associated with
     * @param relationshipName The name of the relationship to check
     * @return True if the relationship is hierarchical, false otherwise, or if the relationship name, or artifact is not found. 
     */
    public boolean isRelationshipHierarchical(PackageArtifact artifact, String relationshipName);

    /**
     * Returns true if the specified property is one that is expected to be supplied by the system, ie. not supplied by
     * the users.
     * @param artifact the artifact with containing the property
     * @param propertyName name of the property in question
     * @return True is the property is expected to be supplied by the system. False otherwise, or if provided artifact
     * is null or if property in question cannot be found on the provided artifact.
     */
    public boolean isSystemSuppliedProperty(PackageArtifact artifact, String propertyName);

    /**
     * Returns true if the specified property is one that is inheritable from the artifact to its children.
     * @param artifact the artifact with containing the property
     * @param propertyName name of the property in question
     * @return True is the property is deemed inheritable. False otherwise, or if provided artifact
     * is null or if property in question cannot be found on the provided artifact.
     */
    public boolean isInheritableProperty(PackageArtifact artifact, String propertyName);

    /**
     * Returns a set of names for relationships which are known to the specific ontology. If no known relationship exists
     * for this type of artifact, an empty set is returned.
     * @return a set of names for relationships which are known to the specific ontology
     */
    public Set<String> getKnownRelationshipNames();


    /**
     * Returns a set of type names of artifact types which contains properties of given name. If no artifact type
     * contains the property of given name, empty set is returned.
     * @param propertyName  the name of the proeprty
     * @return  a set of type names of artifact types which contains properties of given name
     */
    public Set<String> getArtifactTypesContainProperty(String propertyName);

    /**
     * Given the name of an artifact type, returns a set of valid children types according to specified ontology.
     * @param parentType  the parent type
     * @return a set of valid children types according to specified ontology
     */
    public Set<String> getValidChildrenTypes(String parentType);

    /**
     * Given the name of an artifact type, returns a set of valid descendant types (children, children of children, etc.)
     * according to specified ontology.
     * @param parentType the parent type
     * @return  a set of valid descendant types
     */
    public Set<String> getValidDescendantTypes(String parentType);

    /**
     * Returns true if the specified property represents a date and should be handled as such.
     * @param artifact The artifact with the containing property
     * @param propertyName The name of the property to check
     * @return true if the given property name represents a date property, false otherwise
     */
    public boolean isDateProperty(PackageArtifact artifact, String propertyName);

    /**
     * Returns true if the specified property represents a size and should be handled as such.
     * @param artifact The artifact that contains the property.
     * @param propertyName The name of the property to check
     * @return true if the given property name represents a size property, false otherwise
     */
    public boolean isSizeProperty(PackageArtifact artifact, String propertyName);

    /**
     * Returns true if the property is a discipline property and is subject to the discipline controlled vocabulary
     * @param artifact The artifact that contains the property.
     * @param propertyName The name of the property to check
     * @return true if the given property name represents a discipline property, false otherwise
     */
    public boolean isDisciplineProperty(PackageArtifact artifact, String propertyName);

    /**
     * Formats a property value based on formatting specified by the ontology.
     * @param artifact The artifact that contains the property.
     * @param parentPropertyName The name of the containing property if the property is part of a complex type. Can be empty or null if the property is not a part of a complex property.
     * @param propertyName The name of the property to format.
     * @param propertyValue The current unformatted value of the property.
     * @return The value of the property formatted with whatever formatting is specified by the ontology. May be the original value if no special formatting is defined.
     */
    public String getFormattedProperty(PackageArtifact artifact, String parentPropertyName, String propertyName, String propertyValue);

    /**
     * For properties that are stored in a formatted state, this function returns a copy of the value without formatting.
     * @param artifact The artifact that contains the property.
     * @param parentPropertyName The name of the containing property if the property is part of a complex type. Can be empty or null if the property is not a part of a complex property.
     * @param propertyName The name of the property to transform.
     * @param propertyValue The current formatted value of the property.
     * @return The value of the property with any special formatting removed. May be the original value if no special formatting is defined.
     */
    public String getUnFormattedProperty(PackageArtifact artifact, String parentPropertyName, String propertyName, String propertyValue);

    /**
     * Checks whether a property value is valid based on the property type specified by the ontology.
     * @param artifact The artifact that contains the property.
     * @param parentPropertyName The name of the containing property if the property is part of a complex type. Can be empty or null if the property is not a part of a complex property.
     * @param propertyName The name of the property to check for validity.
     * @param propertyValue The value of the property to check for validity.
     * @return A {@link org.dataconservancy.packaging.tool.model.PropertyValidationResult} that contains whether or not the property is valid and key on what hint to use to notify the user.
     */
    public PropertyValidationResult validateProperty(PackageArtifact artifact, String parentPropertyName, String propertyName, String propertyValue);

    /**
     * Checks whether an artifact's parent can be removed from the tree, connecting the artifact directly to its grandparent.
     * @param tree  the  PackageTree
     * @param currentArtifactId Id of the artifact in question.
     * @return true of this is a legal operation. false if it is not.
     * @throws PackageOntologyException
     */
    public boolean canCollapseParentArtifact(PackageTree tree, String currentArtifactId) throws PackageOntologyException;

    /**
     * Performs the operations needed to remove an artifact's parent from the tree and connect the artifact directly to its grandparent.
     * The removed artifact will also be removed from the PackageDescription.
     * @param packageDesc  the PackageDescription
     * @param tree the   PackageTree
     * @param currentArtifactId  the ID of the current artifact
     * @return id of the removed artifact when successful.
     * @throws PackageOntologyException
     */
    public String collapseParentArtifact(PackageDescription packageDesc, PackageTree tree, String currentArtifactId) throws PackageOntologyException;

    /**
     * Determines if the property should support multiple line text entry.
     * @param artifact The artifact that contains the property.
     * @param propertyName The name of the property to check.
     * @return true if the property supports multiple lines, false otherwise
     */
    public boolean propertySupportsMultipleLines(PackageArtifact artifact, String propertyName);
}


