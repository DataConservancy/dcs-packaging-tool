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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.model.Relationship;
import org.dataconservancy.packaging.gui.presenter.EditPackageContentsPresenter;
import org.dataconservancy.packaging.gui.util.ProfilePropertyBox;
import org.dataconservancy.packaging.gui.view.EditPackageContentsView;
import org.dataconservancy.packaging.gui.view.impl.EditPackageContentsViewImpl.NodeRelationshipContainer;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * Implementation for the presenter that displays the package description tree. Handles generation of the tree, changing types of artifacts,
 * sorting tree elements, validating the package description, and saving the changed package description. 
 */
public class EditPackageContentsPresenterImpl extends BasePresenterImpl implements EditPackageContentsPresenter, PreferenceChangeListener {

    private EditPackageContentsView view;
    private DomainProfileService profileService;
    private IPMService ipmService;
    private PropertyFormatService propertyFormatService;
    private File packageDescriptionFile;
    private Preferences preferences;

    private Set<URI> expandedNodes;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public EditPackageContentsPresenterImpl(EditPackageContentsView view) {
        super(view);
        this.view = view;
        expandedNodes = new HashSet<>();
        view.setPresenter(this);

        bind();
    }

    @Override
    public void clear() {
        //This presenter has no information to clear
    }

    public javafx.scene.Node display() {
        String disciplinePath = controller.getFactory().getConfiguration().getDisciplineMap();
        view.setupWindowBuilder(disciplinePath);
        final PackageArtifactTreeServiceWorker worker =
                new PackageArtifactTreeServiceWorker();

        view.getErrorMessageLabel().setVisible(false);
        
        worker.setOnFailed(workerStateEvent -> {
            Throwable e = workerStateEvent.getSource().getException();

            view.getErrorMessageLabel().setText(errors.get(ErrorKey.PACKAGE_DESCRIPTION_SAVE_ERROR) + e.getMessage());
            view.getErrorMessageLabel().setVisible(true);
            log.error("Error processing package description", e);

            controller.getCrossPageProgressIndicatorPopUp().hide();
            controller.showHome(false);
            worker.reset();
        });
        
        worker.setOnSucceeded(workerStateEvent -> {
            displayPackageTree();

            if (controller.getCrossPageProgressIndicatorPopUp() != null) {
                controller.getCrossPageProgressIndicatorPopUp().hide();
            }

            //Setup help content and then rebind the base class to this view.
            view.setupHelp();
            setView(view);
            worker.reset();
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
        view.getSaveButton().setOnAction(arg0 -> {
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
                trimEmptyProperties(controller.getPackageState().getPackageTree());
            }
        });
        
        //Validates the package description, saves it, then moves on to the next page.
        view.getContinueButton().setOnAction(arg0 -> {
            //Perform simple validation to make sure the package description is valid.
            if (!profileService.validateTree(controller.getPackageState().getPackageTree())) {
                view.getWarningPopupPositiveButton().setOnAction(arg01 -> {
                    if (view.getWarningPopup() != null &&
                        view.getWarningPopup().isShowing()) {
                        view.getWarningPopup().hide();
                    }
                });
                view.showWarningPopup(errors.get(ErrorKey.PACKAGE_DESCRIPTION_VALIDATION_ERROR), "Tree was not valid", false, false);
                return;
            }

            //bring up a save file dialog box
            packageDescriptionFile = controller.showSaveFileDialog(view.getPackageDescriptionFileChooser());

            if (packageDescriptionFile != null) {

                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(packageDescriptionFile);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    view.getErrorMessageLabel().setText(
                        errors.get(ErrorKey.PACKAGE_DESCRIPTION_SAVE_ERROR) +
                            e.getMessage());
                    view.getErrorMessageLabel().setVisible(true);
                }

                view.getErrorMessageLabel().setVisible(false);

                //save the PackageDescription to the file

                trimEmptyProperties(controller.getPackageState().getPackageTree());

                controller.setPackageDescriptionFile(packageDescriptionFile);
                controller.getPackageState().setOutputDirectory(packageDescriptionFile.getParentFile());
                controller.goToNextPage();
            }
        });

        //Cancels the property popup, which closes the popup with out saving any changes.
        view.getCancelPopupHyperlink().setOnAction(arg0 -> {
            if (view.getArtifactDetailsWindow() != null && view.getArtifactDetailsWindow().isShowing()) {
                view.getArtifactDetailsWindow().hide();
            }
        });

        if (view.getArtifactDetailsWindow() != null) {
            view.getArtifactDetailsWindow().setOnCloseRequest(event -> saveCurrentNode());
        }
        
        //Saves any changes made in the package artifact property popup
        view.getApplyPopupButton().setOnAction(arg0 -> {
            saveCurrentNode();
            if (view.getArtifactDetailsWindow() != null && view.getArtifactDetailsWindow().isShowing()) {
                view.getArtifactDetailsWindow().hide();
            }
        });

        //Gets the button that's used to dismiss validation error popup.
        view.getReenableWarningsButton().setOnAction(actionEvent -> preferences.putBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), false));

        view.getRefreshPopupPositiveButton().setOnAction(event -> {
            // TODO: Do something useful
            view.getRefreshPopup().hide();
        });

        view.getRefreshPopupNegativeButton().setOnAction(event -> {
            // TODO: Do something useful
            view.getRefreshPopup().hide();
        });
    }

    private void savePropertyFromBox(ProfilePropertyBox propertyBox) {
        //First remove all properties of the given type, to be replaced with the new ones
        profileService.removeProperty(view.getPopupNode(), propertyBox.getPropertyConstraint().getPropertyType());
        //If it's not complex loop through the values and set them on the node
        if (propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueType() !=
            PropertyValueType.COMPLEX) {
            for (String value : propertyBox.getValues()) {
                Property newProperty = new Property(propertyBox.getPropertyConstraint().getPropertyType());
                switch (propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueType()) {
                    case STRING:
                        newProperty.setStringValue(value);
                        break;
                    case LONG:
                        newProperty.setLongValue(Long.valueOf(value));
                        break;
                    case DATE_TIME:
                        //TODO: Parse and format date time
                        break;
                }

                profileService.addProperty(view.getPopupNode(), newProperty);
            }
        } else {
            propertyBox.getSubPropertyBoxes().forEach(this::savePropertyFromBox);
        }
    }
    @Override
    public void saveCurrentNode() {
        if (view.getPopupNode() != null) {
            //First loop through all the properties in the popup
            view.getProfilePropertyBoxes().forEach(this::savePropertyFromBox);

            //Then loop through all relationships and set them on the artifact.
            List<PackageRelationship> relationships = new ArrayList<>();
            for(NodeRelationshipContainer relationshipContainer : view.getArtifactRelationshipFields()) {
                if (relationshipContainer.getRelationship().getValue() != null) {
                    Relationship relationship = relationshipContainer.getRelationship().getValue();
                    if (relationship.getRelationshipUri() != null && !relationship.getRelationshipUri().isEmpty()) {

                        String relationshipUri = relationship.getRelationshipUri();
                        //Only save a hierarchical relationship if it was already on the object and thus created by the system.
                        //TODO Are we still going to ban structural relationships??
                        //TODO Can we store generic triples on domain objects?
                        /*
                        if (packageOntologyService.isRelationshipHierarchical(view.getPopupNode(), relationshipUri)) {
                            if (view.getPopupNode().getRelationshipByName(relationshipUri) != null) {
                                relationships.add(new PackageRelationship(relationshipUri, relationshipContainer.requiresURI.get(), view.getPopupNode().getRelationshipByName(relationshipUri).getTargets()));
                            }
                        } else if (RDFURIValidator.isValid(relationshipUri)) {
                            //If it's not hierarchical we just add it
                            Set<String> targets = new HashSet<>();
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
                        } */
                    }
                }

            }

            //Finally prune any empty properties that already exist on the artifact
            //TODO Figure out how to track and remove empty properties
            //view.getPopupNode().pruneEmptyProperties();

            //apply metadata inheritance
            applyMetadataInheritance();
        }
    }

    protected TreeItem<Node> buildTree(Node node, boolean showIgnoredArtifacts) {
        final TreeItem<Node> item = new TreeItem<>(node);

        item.expandedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue && newValue) {
                expandedNodes.add(item.getValue().getIdentifier());
            } else if (oldValue && !newValue) {
                expandedNodes.remove(item.getValue().getIdentifier());
            }
        });

        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                if (!showIgnoredArtifacts && child.isIgnored()) {
                    continue;
                } else {
                    item.getChildren().add(buildTree(child, showIgnoredArtifacts));
                }
            }
        }
        
        return item;
    }
    
    public TreeItem<Node> findItem(Node node) {
        return findItem(view.getArtifactTreeView().getRoot(), node);
    }

    private TreeItem<Node> findItem(TreeItem<Node> treeNode, URI id) {
        if (treeNode.getValue().getIdentifier().equals(id)) {
            return treeNode;
        }

        for (TreeItem<Node> child : treeNode.getChildren()) {
            TreeItem<Node> result = findItem(child, id);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private TreeItem<Node> findItem(TreeItem<Node> tree, Node node) {
        if (node.equals(tree.getValue())) {
            return tree;
        }
        
        for (TreeItem<Node> child : tree.getChildren()) {
            TreeItem<Node> result = findItem(child, node);
            
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }


    @Override
    public void trimEmptyProperties(Node node) {

    }

    protected void applyMetadataInheritance() {
        /*TODO Reimplement inheritance
        Set<String> inheritablePropertyNames = view.getInheritMetadataCheckBoxMap().keySet();
        TreeItem<Node> item = findItem(view.getRoot(), view.getPopupNode());
        
        for (final String inheritablePropertyName : inheritablePropertyNames) {
            if (view.getInheritMetadataCheckBoxMap().get(inheritablePropertyName).isSelected()) {
                try {
                    if (item != null) {
                        applyParentPropertyValue(view.getPopupNode(), item.getChildren(), inheritablePropertyName);
                    }
                } catch (PackageOntologyException e) {
                    log.error(e.getMessage());
                    view.getErrorMessageLabel().setText(e.getMessage());
                    view.getErrorMessageLabel().setVisible(true);
                }
            }

        }
        */
    }
    

    /**
     * Apply the named property's value of the parent artifact to its children's applicable property. This method is
     * recursively called to apply the inheritable property's value to all of the offsprings when applicable.
     *
     * <p/>
     * If the named property is not deemed inHeritable by the PackageOntologyService, then method is a no-op
     *
     */
    /* TODO: Reimplement inheritance
    private void applyParentPropertyValue(Node parent, ObservableList<TreeItem<Node>> children, String propertyName)
            throws PackageOntologyException {

        //If the named property is not an inheritable property on the parent artifact, return.
        if (!packageOntologyService.isInheritableProperty(parent, propertyName)) {
            return;
        }

        //Loop through the children to apply values.
        for (TreeItem<Node> child : children) {

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
    */
    @Override
    public void changeType(Node node, NodeTransform transform) {
        if (node != null && transform != null) {
            profileService.transformNode(node, transform);

            displayPackageTree();

            //Resort the tree if necessary
            TreeItem<Node> selectedItem = view.getArtifactTreeView().getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                //This should never be the case since we don't show the root but just to be safe
                if (selectedItem.getParent() != null) {
                    sortChildren(selectedItem.getParent().getChildren());
                }
            }
        }
    }

    @Override
    public void setProfileService(DomainProfileService profileService){
        this.profileService = profileService;
    }

    @Override
    public void setIpmService(IPMService ipmService) {
        this.ipmService = ipmService;
    }

    @Override
    public void setPropertyFormatService(PropertyFormatService formatService) {
        this.propertyFormatService = formatService;
    }

    //Recursively sorts all children elements of the tree.
    private void sortTree(TreeItem<Node> treeNode) {
        if (!treeNode.isLeaf()) {
            sortChildren(treeNode.getChildren());
            
            //Recurse through all the children and sort them
            treeNode.getChildren().forEach(this::sortTree);
        }    
    }

    //Sorts the tree items in the provided list. //This has been made profile agnostic it now just sorts based on whether the node is a directory
    private void sortChildren(ObservableList<TreeItem<Node>> children) {
        FXCollections.sort(children, (o1, o2) -> {

            Node nodeOne = o1.getValue();
            Node nodeTwo = o2.getValue();

            if (nodeOne.getFileInfo().isDirectory() == nodeTwo.getFileInfo().isDirectory()) {
                return 0;
            }

            if (nodeOne.getFileInfo().isFile()
                        && nodeTwo.getFileInfo().isDirectory()) {
                return -1;
            }

            return 1;
        });
    }

    /* TODO: RE-implement inheritance
    @Override
    public Set<String> getInheritingTypes(String parentType, String propertyName) {
        Set<String> typesWithProperty = packageOntologyService.getArtifactTypesContainProperty(propertyName);
        Set<String> inheritingType = packageOntologyService.getValidDescendantTypes(parentType);
        inheritingType.retainAll(typesWithProperty);
        return inheritingType;
    }
    */
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
                    return null;
                }
            };
        }
    }


    public void displayPackageTree() {
        if (controller.getPackageState() != null && controller.getPackageState().getPackageTree() != null) {
            view.getArtifactTreeView().setRoot(buildTree(controller.getPackageState().getPackageTree(),
                    view.getShowIgnored().selectedProperty().getValue()));
            view.getRoot().setExpanded(true);
            sortTree(view.getRoot());
            for (URI nodeID : expandedNodes) {
                if (nodeID !=null) {
                    TreeItem expandedItem = findItem(view.getRoot(), nodeID);
                    if (expandedItem != null) {
                        expandedItem.setExpanded(true);
                    }
                }
            }
        }
    }

    public void rebuildTreeView() {
        displayPackageTree();
    }
}