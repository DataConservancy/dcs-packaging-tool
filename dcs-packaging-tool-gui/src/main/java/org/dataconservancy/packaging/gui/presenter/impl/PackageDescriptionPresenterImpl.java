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

package org.dataconservancy.packaging.gui.presenter.impl;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.model.Relationship;
import org.dataconservancy.packaging.gui.presenter.PackageDescriptionPresenter;
import org.dataconservancy.packaging.gui.util.RDFURIValidator;
import org.dataconservancy.packaging.gui.view.PackageDescriptionView;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl.ArtifactPropertyContainer;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl.ArtifactRelationshipContainer;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.impl.PackageDescriptionValidator;
import org.dataconservancy.packaging.tool.model.DcsPackageDescriptionSpec.ArtifactType;
import org.dataconservancy.packaging.tool.model.*;
import org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup;
import org.dataconservancy.packaging.validation.PackageValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * Implementation for the presenter that displays the package description tree. Handles generation of the tree, changing types of artifacts,
 * sorting tree elements, validating the package description, and saving the changed package description. 
 */
public class PackageDescriptionPresenterImpl extends BasePresenterImpl implements PackageDescriptionPresenter, PreferenceChangeListener {

    private PackageDescriptionView view;
    private PackageDescription packageDescription; 
    private PackageOntologyService packageOntologyService;
    private PackageTree packageTree;
    private PackageDescriptionBuilder packageDescriptionBuilder;
    private PackageDescriptionValidator packageDescriptionValidator;
    private File packageDescriptionFile;
    private Preferences preferences;

    private Set<String> expandedArtifacts;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public PackageDescriptionPresenterImpl(PackageDescriptionView view) {
        super(view);
        this.view = view;
        packageDescription = null;
        expandedArtifacts = new HashSet<String>();
        view.setPresenter(this);
        bind();
    }

    public Node display(boolean clear) {
        final PackageArtifactTreeServiceWorker worker =
                new PackageArtifactTreeServiceWorker();

        view.getErrorMessageLabel().setVisible(false);
        
        worker.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                Throwable e = workerStateEvent.getSource().getException();
                
                view.getErrorMessageLabel().setText(errors.get(ErrorKey.PACKAGE_DESCRIPTION_SAVE_ERROR) + e.getMessage());
                view.getErrorMessageLabel().setVisible(true);
                log.error("Error processing package description", e);
                
                controller.getCrossPageProgressIndicatorPopUp().hide();
                controller.showHome(false);
                worker.reset();
            }
        });
        
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                displayPackageTree();
                
                if (controller.getCrossPageProgressIndicatorPopUp() != null) {
                    controller.getCrossPageProgressIndicatorPopUp().hide();
                }

                //Setup help content and then rebind the base class to this view.
                view.setupHelp();
                setView(view);
                worker.reset();
            }
        });

        if (Platform.isFxApplicationThread()) {
            worker.start();
        }

        preferences = Preferences.userRoot().node(internalProperties.get(InternalProperties.InternalPropertyKey.PREFERENCES_NODE_NAME));
        preferences.addPreferenceChangeListener(this);

        super.bindBaseElements();
        
        return view.asNode();
    }

    private void bind() {

        //Displays the file selector, and then saves the package description to the given file. 
        view.getSaveButton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                packageDescriptionFile = controller.showSaveFileDialog(view.getPackageDescriptionFileChooser());

                //Still check if it's null in case user hit cancel
                if (packageDescriptionFile != null) {
                    controller.setPackageDescriptionFile(packageDescriptionFile);

                    FileOutputStream stream = null;
                    try{
                        stream = new FileOutputStream(packageDescriptionFile);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        view.getErrorMessageLabel().setText(errors.get(ErrorKey.PACKAGE_DESCRIPTION_SAVE_ERROR) + e.getMessage());
                        view.getErrorMessageLabel().setVisible(true);
                    }

                    if (view.getErrorMessageLabel().isVisible()) {
                        view.getErrorMessageLabel().setVisible(false);
                    }
                    
                    //save the PackageDescription to the file
                    trimInvalidProperties(packageDescription);
                    packageDescriptionBuilder.serialize(packageDescription, stream);
                }
            }
        });
        
        //Validates the package description, saves it, then moves on to the next page.
        view.getContinueButton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                //Perform simple validation to make sure the package description is valid. 
                try {
                    packageDescriptionValidator.validate(packageDescription);
                } catch (PackageValidationException e1) {
                    //Gets the button that's used to dismiss validation error popup.
                    view.getWarningPopupPositiveButton().setOnAction(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent arg0) {
                            if (view.getWarningPopup() != null && view.getWarningPopup().isShowing()) {
                                view.getWarningPopup().hide();
                            }
                        }
                    });
                    view.showWarningPopup(errors.get(ErrorKey.PACKAGE_DESCRIPTION_VALIDATION_ERROR), e1.getMessage(), false, false);
                    log.error(e1.getMessage());
                    return;
                }

                //bring up a save file dialog box
                packageDescriptionFile = controller.showSaveFileDialog(view.getPackageDescriptionFileChooser());

                if (packageDescriptionFile != null) {
                    
                    FileOutputStream stream = null;
                    try{
                        stream = new FileOutputStream(packageDescriptionFile);
                    } catch (IOException e){
                        log.error(e.getMessage());
                        view.getErrorMessageLabel().setText(errors.get(ErrorKey.PACKAGE_DESCRIPTION_SAVE_ERROR) + e.getMessage());
                        view.getErrorMessageLabel().setVisible(true);
                    }
    
                    view.getErrorMessageLabel().setVisible(false);

                    //save the PackageDescription to the file

                    trimInvalidProperties(packageDescription);
                    packageDescriptionBuilder.serialize(packageDescription, stream);

                    controller.setPackageDescriptionFile(packageDescriptionFile);
                    controller.setOutputDirectory(packageDescriptionFile.getParentFile());
                    controller.goToNextPage();
                }                
            }
        });
        
        //Cancels the property popup, which closes the popup with out saving any changes.
        view.getCancelPopupHyperlink().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                if (view.getPackageArtifactPopup() != null && view.getPackageArtifactPopup().isShowing()) {
                    view.getPackageArtifactPopup().hide();
                }
            }
            
        });
        
        //Saves any changes made in the package artifact property popup
        view.getApplyPopupButton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                saveCurrentArtifact();
                if (view.getPackageArtifactPopup() != null && view.getPackageArtifactPopup().isShowing()) {
                    view.getPackageArtifactPopup().hide();
                }
            }
            
        });

        //Gets the button that's used to dismiss validation error popup.
        view.getReenableWarningsButton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                preferences.putBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), false);
            }
        });
    }

    private void saveCurrentArtifact() {
        if (view.getPopupArtifact() != null) {
            //First loop through all the properties in the popup
            for (String property : view.getArtifactPropertyFields().keySet()) {

                //Get the field container for the property
                ArtifactPropertyContainer container = view.getArtifactPropertyFields().get(property);
                Set<PropertyValueGroup> propertyGroups = new HashSet<PropertyValueGroup>();

                //If the property is complex create a property group
                if (container.isComplex()) {

                    //Loop through all the sub properties making up the property group.
                    for (Map<String, Set<StringProperty>> groupValues : container.getSubProperties()) {
                        PropertyValueGroup group = new PropertyValueGroup();
                        for (String subPropertyName : groupValues.keySet()) {
                            Set<String> values = new HashSet<String>();
                            for (StringProperty field : groupValues.get(subPropertyName)) {
                                if (field.getValue() != null && !field.getValue().isEmpty()) {
                                    values.add(packageOntologyService.getFormattedProperty(view.getPopupArtifact(), property, subPropertyName,field.getValue()));
                                }
                            }

                            group.setSubPropertyValues(subPropertyName, values);
                        }

                        propertyGroups.add(group);
                    }

                    //Set the property groups on the artifact, this will wipe out any previous property groups.
                    view.getPopupArtifact().setPropertyValueGroups(property, propertyGroups);

                } else {
                    //Otherwise set the simple property values.
                    Set<String> values = new HashSet<String>();
                    for (StringProperty propertyValue : container.getValues()) {
                        if (propertyValue.getValue() != null && !propertyValue.getValue().isEmpty()) {
                            values.add(packageOntologyService.getFormattedProperty(view.getPopupArtifact(), "", property, propertyValue.getValue()));
                        }
                    }
                    //Sets the simple property values this will wipe out any previously existing simple properties.
                    view.getPopupArtifact().setSimplePropertyValues(property, values);
                }
            }

            //Then loop through all relationships and set them on the artifact.
            List<PackageRelationship> relationships = new ArrayList<PackageRelationship>();
            for(ArtifactRelationshipContainer relationshipContainer : view.getArtifactRelationshipFields()) {
                if (relationshipContainer.getRelationship().getValue() != null) {
                    Relationship relationship = relationshipContainer.getRelationship().getValue();
                    if (relationship.getRelationshipUri() != null && !relationship.getRelationshipUri().isEmpty()) {

                        String relationshipUri = relationship.getRelationshipUri();
                        //Only save a hierarchical relationship if it was already on the object and thus created by the system.
                        if (packageOntologyService.isRelationshipHierarchical(view.getPopupArtifact(), relationshipUri)) {
                            if (view.getPopupArtifact().getRelationshipByName(relationshipUri) != null) {
                                relationships.add(new PackageRelationship(relationshipUri, relationshipContainer.requiresURI.get(), view.getPopupArtifact().getRelationshipByName(relationshipUri).getTargets()));
                            }
                        } else if (RDFURIValidator.isValid(relationshipUri)) {
                            //If it's not hierarchical we just add it
                            Set<String> targets = new HashSet<String>();
                            for (StringProperty field : relationshipContainer.getRelationshipTargets()) {
                                //If target is not empty or null and is a valid RDF URI
                                if (field.getValue() != null && !field.getValue().isEmpty()) {
                                    if (relationshipContainer.requiresURI().getValue()) {
                                        if (RDFURIValidator.isValid(field.getValue())) {
                                            targets.add(field.getValue());
                                        }
                                    } else {
                                        targets.add(field.getValue());
                                    }
                                }
                            }

                            //If we have target values add the relationship to the set to be added to the artifact.
                            //Any partially completed relationships will be discarded, both a definition
                            //and at least one target need to be specified.
                            if (!targets.isEmpty()) {
                                relationships.add(new PackageRelationship(relationshipUri, relationshipContainer.requiresURI.get(), targets));
                            }
                        }
                    }
                }

            }

            //Finally prune any empty properties that already exist on the artifact
            view.getPopupArtifact().pruneEmptyProperties();

            view.getPopupArtifact().setRelationships(relationships);

            //apply metadata inheritance
            applyMetadataInheritance();
        }
    }

    protected TreeItem<PackageArtifact> buildTree(PackageNode pkg_node, boolean showIgnoredArtifacts) {
        final TreeItem<PackageArtifact> item = new TreeItem<PackageArtifact>(pkg_node.getValue());

        item.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (!oldValue && newValue) {
                    expandedArtifacts.add(item.getValue().getId());
                } else if (oldValue && !newValue) {
                    expandedArtifacts.remove(item.getValue().getId());
                }
            }
        });
        for (PackageNode pkg_kid: pkg_node.getChildrenNodes()) {
            if (showIgnoredArtifacts == false && pkg_kid.getValue().isIgnored()) {
                continue;
            } else {
                item.getChildren().add(buildTree(pkg_kid, showIgnoredArtifacts));
            }
        }
        
        return item;
    }
    
    public TreeItem<PackageArtifact> findItem(PackageArtifact artifact) {
        return findItem(view.getArtifactTreeView().getRoot(), artifact);
    }

    @Override
    public void collapseParentArtifact(PackageArtifact packageArtifact) {
        try {
            String removedArtifactId = packageOntologyService.collapseParentArtifact(packageDescription, packageTree, packageArtifact.getId());

            if (removedArtifactId != null) {
                expandedArtifacts.remove(removedArtifactId);
            }
        } catch (PackageOntologyException e) {
            view.getErrorMessageLabel().setText(errors.get(ErrorKey.ARTIFACT_GRAPH_ERROR) + e.getMessage());
            view.getErrorMessageLabel().setVisible(true);
            log.error(e.getMessage());
        }

        if (view.getErrorMessageLabel().isVisible()) {
            view.getErrorMessageLabel().setVisible(false);
        }
    }

    @Override
    public boolean canCollapseParentArtifact(PackageArtifact packageArtifact) {
        try {
            return packageOntologyService.canCollapseParentArtifact(packageTree, packageArtifact.getId());
        } catch (PackageOntologyException e) {
            view.getErrorMessageLabel().setText(e.getMessage());
            view.getErrorMessageLabel().setVisible(true);
            log.error(e.getMessage());
        }

        if (view.getErrorMessageLabel().isVisible()) {
            view.getErrorMessageLabel().setVisible(false);
        }

        return false;
    }

    private TreeItem<PackageArtifact> findItem(TreeItem<PackageArtifact> tree, PackageArtifact artifact) {
        if (artifact.equals(tree.getValue())) {
            return tree;
        }
        
        for (TreeItem<PackageArtifact> child : tree.getChildren()) {
            TreeItem<PackageArtifact> result = findItem(child, artifact);
            
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    @Override
    public List<String> findInvalidProperties(PackageArtifact packageArtifact, String type) {
        List<String> invalidProps = new ArrayList<String>();

        // If there's no description object, nothing to trim
        if (packageArtifact == null) {
            return invalidProps;
        }

        Set<String> validProps = packageOntologyService.getProperties(type).keySet();
        for (String prop : packageArtifact.getPropertyNames()) {
            // Make sure the property is valid for the artifact type
            if (!validProps.contains(prop)) {
                invalidProps.add(prop);
            }
        }

        return invalidProps;
    }

    @Override
    public void trimInvalidProperties(PackageDescription packageDescription) {
        // If there's no description object, nothing to trim
        if (packageDescription == null) {
            return;
        }

        for (PackageArtifact artifact : packageDescription.getPackageArtifacts()) {
            List<String> invalidArtifactProps = findInvalidProperties(artifact, artifact.getType());

            for (String prop : artifact.getPropertyNames()) {
                // Make sure the property is valid for the artifact type
                if (!invalidArtifactProps.contains(prop)) {

                    // Also remove properties with no non-empty values
                    boolean hasValue = false;
                    if (artifact.getSimplePropertyValues(prop) != null) {
                        for (String val : artifact.getSimplePropertyValues(prop)) {
                            if (val != null && !val.trim().isEmpty()) {
                                hasValue = true;
                                break;
                            }
                        }
                    } else if (artifact.getPropertyValueGroups(prop) != null) {
                        // clean up property groups

                        Set<PropertyValueGroup> emptyGroups = new HashSet<PropertyValueGroup>();
                        for (PropertyValueGroup group : artifact.getPropertyValueGroups(prop)) {
                            boolean groupEmpty = true;
                            Set<String> invalidSubProps = new HashSet<String>();

                            // Clean up any empty subproperties from a group.  If the group has at least one
                            // non-empty subproperty, the group is not empty
                            for (String subProp : group.getSubPropertyNames()) {
                                boolean hasSubPropValue = false;
                                if (group.getSubPropertyValues(subProp) != null) {
                                    for (String val : group.getSubPropertyValues(subProp)) {
                                        if (val != null && !val.trim().isEmpty()) {
                                            groupEmpty = false;
                                            hasSubPropValue = true;
                                            break;
                                        }
                                    }
                                }
                                if (!hasSubPropValue) {
                                    invalidSubProps.add(subProp);
                                }
                            }
                            // remove the subproperties outside the loop to not collide with the iterator
                            for (String invalidProp : invalidSubProps) {
                                group.removeSubProperty(invalidProp);
                            }
                            if (groupEmpty) {
                                emptyGroups.add(group);
                            }
                        }

                        // If there is at least one empty group, get a set of non-empty groups and reset the
                        // artifact's property with the new set
                        if (!emptyGroups.isEmpty()) {
                            Set<PropertyValueGroup> goodGroups = artifact.getPropertyValueGroups(prop);
                            goodGroups.removeAll(emptyGroups);
                            artifact.setPropertyValueGroups(prop, goodGroups);
                        }
                        // If there is at least one non-empty group for the property, the property is valid
                        if (!artifact.getPropertyValueGroups(prop).isEmpty()) {
                            hasValue = true;
                        }
                    }

                    // if no value (simple or complex) is found for the property, prepare to trim it
                    if (!hasValue) {
                        invalidArtifactProps.add(prop);
                    }
                }
            }

            // Remove all invalid properties from the artifact.  Done outside the property loop to avoid
            // colliding with the iterator
            for (String prop : invalidArtifactProps) {
                artifact.removeProperty(prop);
            }
        }
    }

    protected void applyMetadataInheritance() {
        Set<String> inheritablePropertyNames = view.getInheritMetadataCheckBoxMap().keySet();
        TreeItem<PackageArtifact> item = findItem(view.getRoot(), view.getPopupArtifact());
        
        for (final String inheritablePropertyName : inheritablePropertyNames) {
            if (view.getInheritMetadataCheckBoxMap().get(inheritablePropertyName).isSelected()) {
                try {
                    applyParentPropertyValue(view.getPopupArtifact(), item.getChildren(), inheritablePropertyName);
                } catch (PackageOntologyException e) {
                    log.error(e.getMessage());
                    view.getErrorMessageLabel().setText(e.getMessage());
                    view.getErrorMessageLabel().setVisible(true);
                }
            }

        }
    }
    

    /**
     * Apply the named property's value of the parent artifact to its children's applicable property. This method is
     * recursively called to apply the inheritable property's value to all of the offsprings when applicable.
     *
     * <p/>
     * If the named property is not deemed inHeritable by the PackageOntologyService, then method is a no-op
     *
     */
    private void applyParentPropertyValue(PackageArtifact parent, ObservableList<TreeItem<PackageArtifact>> children, String propertyName)
            throws PackageOntologyException {

        //If the named property is not an inheritable property on the parent artifact, return.
        if (!packageOntologyService.isInheritableProperty(parent, propertyName)) {
            return;
        }

        //Loop through the children to apply values.
        for (TreeItem<PackageArtifact> child : children) {

            //get the type of the named property
            String propertyType = packageOntologyService.getProperties(parent).get(propertyName);
            if (propertyType != null && !propertyType.isEmpty()) {
                //assign parent's property's value to child's property
                //if child already has a value for the named property, that value will be overwritten.
                if (!packageOntologyService.isPropertyComplex(propertyType)) {
                    child.getValue().setSimplePropertyValues(propertyName, parent.getSimplePropertyValues(propertyName));
                } else {
                    child.getValue().setPropertyValueGroups(propertyName, parent.getPropertyValueGroups(propertyName));
                }
                //if child has children, trigger cascading inheritance by calling the method recursively.
                if (child.getChildren().size() > 0) {
                    applyParentPropertyValue(child.getValue(), child.getChildren(), propertyName);
                }
            }
        }
    }

    @Override
    public Set<String> getValidTypes(PackageArtifact packageArtifact){
        Set<String> validTypeSet = new HashSet<String>();
        if(packageArtifact.getId() != null && packageTree != null) {
            try {
                validTypeSet.addAll(packageOntologyService.getValidTypes(packageTree, packageArtifact.getId()));
            } catch (PackageOntologyException e) {
                view.getErrorMessageLabel().setText(errors.get(ErrorKey.ARTIFACT_TYPE_ERROR) + e.getMessage());
                view.getErrorMessageLabel().setVisible(true);
                log.error(e.getMessage());
            }
            
            if (view.getErrorMessageLabel().isVisible()) {
                view.getErrorMessageLabel().setVisible(false);
            }
        }
        return validTypeSet;
    }

    @Override
    public void changeType(PackageArtifact packageArtifact, String type) {
        if (packageArtifact.getId() != null && packageTree != null) {
            try {
                packageOntologyService.changeType(packageDescription, packageTree, packageArtifact, controller.getContentRoot(), type);

                displayPackageTree();

                //Resort the tree if necessary
                TreeItem<PackageArtifact> selectedItem = view.getArtifactTreeView().getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    //This should never be the case since we don't show the root but just to be safe
                    if (selectedItem.getParent() != null) {
                        sortChildren(selectedItem.getParent().getChildren());
                    }
                }

            } catch (PackageOntologyException e) {
                view.getErrorMessageLabel().setText(errors.get(ErrorKey.ARTIFACT_TYPE_ERROR) + e.getMessage());
                view.getErrorMessageLabel().setVisible(true);
                log.error(e.getMessage());
            }

            if (view.getErrorMessageLabel().isVisible()) {
                view.getErrorMessageLabel().setVisible(false);
            }
        }
    }

    @Override
    public void setPackageOntologyService(PackageOntologyService packageOntologyService){
        this.packageOntologyService = packageOntologyService;
    }

    @Override
    public void setPackageDescriptionBuilder(PackageDescriptionBuilder packageDescriptionBuilder){
        this.packageDescriptionBuilder = packageDescriptionBuilder;
    }
    
    @Override
    public void setPackageDescriptionValidator(PackageDescriptionValidator packageDescriptionValidator) {
        this.packageDescriptionValidator = packageDescriptionValidator;
    }
    
    //Recursively sorts all children elements of the tree.
    private void sortTree(TreeItem<PackageArtifact> node) {
        if (!node.isLeaf()) {
            sortChildren(node.getChildren());
            
            //Recurse through all the children and sort them
            for (TreeItem<PackageArtifact> child : node.getChildren()) {
                sortTree(child);
            }
        }    
    }
    
    //Sorts the tree items in the provided list. The order of the list will be dataFiles, followed by metadata files, followed by collections, followed by data items
    private void sortChildren(ObservableList<TreeItem<PackageArtifact>> children) {
        FXCollections.sort(children, new Comparator<TreeItem<PackageArtifact>>() {

            @Override
            public int compare(TreeItem<PackageArtifact> o1, TreeItem<PackageArtifact> o2) {
                
                PackageArtifact artifactOne = o1.getValue();
                PackageArtifact artifactTwo = o2.getValue();
                
                if (artifactOne.getType().equalsIgnoreCase(artifactTwo.getType())) {
                    return 0;
                }
                
                if (artifactOne.getType().equalsIgnoreCase(ArtifactType.DataFile.name())) {
                    return -1;
                } else if (artifactOne.getType().equalsIgnoreCase(ArtifactType.MetadataFile.name())) {
                    if (artifactTwo.getType().equalsIgnoreCase(ArtifactType.Collection.name()) 
                            || artifactTwo.getType().equalsIgnoreCase(ArtifactType.DataItem.name())) {
                        return -1;
                    }
                } else if (artifactOne.getType().equalsIgnoreCase(ArtifactType.Collection.name()) 
                            && artifactTwo.getType().equalsIgnoreCase(ArtifactType.DataItem.name())) {
                    return -1;
                }
                
                return 1;
            }
            
        });
    }
    @Override
    public Set<String> getInheritingTypes(String parentType, String propertyName) {
        Set<String> typesWithProperty = packageOntologyService.getArtifactTypesContainProperty(propertyName);
        Set<String> inheritingType = packageOntologyService.getValidDescendantTypes(parentType);
        inheritingType.retainAll(typesWithProperty);
        return inheritingType;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if(evt.getKey().equals(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE))) {
            view.getReenableWarningsButton().setVisible(Boolean.parseBoolean(evt.getNewValue()));
            view.getHideFutureWarningPopupCheckbox().setSelected(Boolean.parseBoolean(evt.getNewValue()));
        }
    }

    /**
     * A {@link javafx.concurrent.Service} which executes the {@link javafx.concurrent.Task} of obtaining a package tree
     * from PackageOntologyService given a
     * {@link org.dataconservancy.packaging.tool.model.PackageDescription}.
     */
    private class PackageArtifactTreeServiceWorker extends Service<Void> {
        public PackageArtifactTreeServiceWorker() {
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Thread cannot modify UI so call displayPackageTree on success.
                    setupPackageTree();
                    return null;
                }
            };
        }
    }
    
    private void setupPackageTree() {
        packageDescription = controller.getPackageDescription();
        
        if (packageDescription != null) {
            try {
                packageTree = packageOntologyService.buildPackageTree(packageDescription, controller.getContentRoot());
                //controller.setContentRoot(new File(packageTree.getRoot().getValue().getArtifactRef().getRefString()));
            } catch (PackageOntologyException e) {
                log.error("Unable to create package tree", e);
                // TODO User message?
            }
        }
    }

    public void displayPackageTree() {
        if (packageDescription != null && packageTree != null) {
            view.getArtifactTreeView().setRoot(buildTree(packageTree.getRoot(),
                    view.getShowIgnored().selectedProperty().getValue()));
            view.getRoot().setExpanded(true);
            sortTree(view.getRoot());
            for (String artifactId : expandedArtifacts) {
                PackageNode node = packageTree.getNodesMap().get(artifactId);
                if (node !=null) {
                    TreeItem expandedItem = findItem(node.getValue());
                    if (expandedItem != null) {
                        expandedItem.setExpanded(true);
                    }
                }
            }
        }
    }

    public void rebuildTreeView() {
        setupPackageTree();
        displayPackageTree();
    }
}