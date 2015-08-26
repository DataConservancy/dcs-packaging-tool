/*
 * Copyright 2014 Johns Hopkins University
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
package org.dataconservancy.packaging.gui.presenter;

import javafx.scene.control.TreeItem;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.impl.PackageDescriptionValidator;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * This presenter controls the display of the package description view. 
 * It also handles validating a package description, before a package is allowed to be created. *
 */
public interface PackageDescriptionPresenter extends Presenter {

    /**
     * Sets the ontology service that is used for determining property and relationship types, as well as acceptable artifact types.
     * @param packageOntologyService the PackageOntologyService
     */
    void setPackageOntologyService(
        PackageOntologyService packageOntologyService);

    /**
     * Given an artifact queries the ontology service to determine the acceptable artifact types. 
     * @param packageArtifact The artifact to check for available types.
     * @return The set of acceptable types for the artifact.
     */
    Set<String> getValidTypes(PackageArtifact packageArtifact);

    /**
     * Used to serialize the package description, for saving to a file.
     * @param packageDescriptionBuilder The package description builder to use for serialization.
     */
    void setPackageDescriptionBuilder(
        PackageDescriptionBuilder packageDescriptionBuilder);

    /**
     * Used to validate the package description before the user is allowed to move on to generating a package.
     * Validation is only performed when this presenter is being exited, we don't validate before saving a description.
     * @param packageDescriptionValidator the PackageDescriptionValidator
     */
    void setPackageDescriptionValidator(
        PackageDescriptionValidator packageDescriptionValidator);

    /**
     * Changes the type of the provided artifact to the provided type. 
     * @param packageArtifact The artifact to change the type of.
     * @param type The new type of the artifact.
     */
    void changeType(PackageArtifact packageArtifact, String type);

    /**
     * Trims out invalid and empty properties from the package description, primarily before serializing,
     * to keep it clean and uncluttered.
     * @param packageDescription The package description to clean
     */
    void trimInvalidProperties(PackageDescription packageDescription);

    /**
     * Goes through all the artifacts in the package description and finds any properties that aren't valid.
     * This can occur when a property has been set and then artifact type has change, and the previously set property doesn't exist on the
     * new type.
     * @param packageArtifact the package description to look for invalid properties
     * @param type The type to check the properties of the artifact against, if you want to test if the artifact has invalid properties pass it's current type.
     * @return A map of strings with properties that are invalid keyed by the artifact, maybe empty but never null.
     */
    List<String> findInvalidProperties(PackageArtifact packageArtifact,
                                       String type);

    /**
     * Given the type of the current artifact and a propertyName, this method returns a Set of names of the artifact types
     * which can inherit the specified property fro the current artifact.
     * @param parentType  the parent's Type
     * @param propertyName the property name
     * @return  a Set of name of the artifact types which can inherit the specified property fro the current artifact
     */
    Set<String> getInheritingTypes(String parentType, String propertyName);
    
    /**
     * Rerun the ontology service on the PackageDescription and redisplay the resulting PackageTree.
     */
    void rebuildTreeView();

    /**
     * Refresh the display of a new PackageTree.
     */
    void displayPackageTree();
    
    /**
     * Return the TreeItem containing the given PackageArtifact or null if none found.
     * 
     * @param packageArtifact the PackageArtifact
     * @return matching TreeItem
     */
    TreeItem<PackageArtifact> findItem(PackageArtifact packageArtifact);

    /**
     * Collapses synthesized DI + F pair into MdF for the containing collection
     * @param packageArtifact The package artifact to collapse into a metadata file.
     */
    void collapseParentArtifact(PackageArtifact packageArtifact);

    /**
     * Determines whether the current artifact can be attached to its grandparent artifact, essentially cutting off
     * its parent from the of artifact.
     * @param packageArtifact  the PackageArtifact
     * @return True if the artifact can be attached to a grandparent, false otherwise
     */
    boolean canCollapseParentArtifact(PackageArtifact packageArtifact);

    /**
     * Saves the artifact that's currently being displayed in the properties window.
     */
    void saveCurrentArtifact();
}