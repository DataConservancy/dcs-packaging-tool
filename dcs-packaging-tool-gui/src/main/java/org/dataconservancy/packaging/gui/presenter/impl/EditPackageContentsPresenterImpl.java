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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Errors;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.EditPackageContentsPresenter;
import org.dataconservancy.packaging.gui.util.ProfilePropertyBox;
import org.dataconservancy.packaging.gui.view.EditPackageContentsView;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.impl.support.Validator;
import org.dataconservancy.packaging.tool.impl.support.ValidatorFactory;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * Implementation for the presenter that displays the package description tree. Handles generation of the tree, changing types of artifacts,
 * sorting tree elements, validating the package description, and saving the changed package description. 
 */
public class EditPackageContentsPresenterImpl extends BasePresenterImpl implements EditPackageContentsPresenter, PreferenceChangeListener, CssConstants {

    private EditPackageContentsView view;
    private IPMService ipmService;
    private PropertyFormatService propertyFormatService;
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
        view.getHeaderViewHelpLink().setOnAction(arg0 -> view.showHelpPopup());

        view.setupWindowBuilder();

        clearError();

        displayPackageTree();

        if (controller.getCrossPageProgressIndicatorPopUp() != null) {
            controller.getCrossPageProgressIndicatorPopUp().hide();
        }

        preferences = Preferences.userRoot().node(internalProperties.get(InternalProperties.InternalPropertyKey.PREFERENCES_NODE_NAME));
        preferences.addPreferenceChangeListener(this);

        return view.asNode();
    }

    private void bind() {

        //Displays the file selector, and then saves the package description to the given file. 
        view.getSaveButton().setOnAction(arg0 -> {
            if (view.getNodePropertiesWindow() != null && view.getNodePropertiesWindow().isShowing()) {
                view.getNodePropertiesWindow().hide();
            }

            try {
                getController().savePackageStateFile();
            } catch (IOException | RDFTransformException e) {
                showError(TextFactory.getText(Errors.ErrorKey.IO_CREATE_ERROR));
            }
        });

        //Cancels the property popup, which closes the popup with out saving any changes.
        view.getCancelPopupHyperlink().setOnAction(arg0 -> {
            if (view.getNodePropertiesWindow() != null && view.getNodePropertiesWindow().isShowing()) {
                view.getNodePropertiesWindow().hide();
            }
        });

        if (view.getNodePropertiesWindow() != null) {
            view.getNodePropertiesWindow().setOnCloseRequest(event -> saveCurrentNode());
        }
        
        //Saves any changes made in the package artifact property popup
        view.getApplyPopupButton().setOnAction(arg0 -> {
            saveCurrentNode();
            if (view.getNodePropertiesWindow() != null && view.getNodePropertiesWindow().isShowing()) {
                view.getNodePropertiesWindow().hide();
            }
        });

        //Gets the button that's used to dismiss validation error popup.
        view.getReenableWarningsButton().setOnAction(actionEvent -> preferences.putBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), false));

        view.getRefreshPopupPositiveButton().setOnAction(event -> {
            ipmService.mergeTree(controller.getPackageTree(), view.getRefreshResult());
            List<Node> currentlyIgnoredNodes = new ArrayList<>();
            getIgnoredNodes(view.getRoot().getValue(), currentlyIgnoredNodes);

            //To assign node types from the refresh we must unignore nodes so all nodes are considered
            for (Node node : currentlyIgnoredNodes) {
                ipmService.ignoreNode(node, false);
            }

            controller.getDomainProfileService().assignNodeTypes(controller.getPrimaryDomainProfile(), controller.getPackageTree());

            //Once we're done assigning types we'll reignore whatever was previously ignored
            for (Node node : currentlyIgnoredNodes) {
                ipmService.ignoreNode(node, true);
            }

            displayPackageTree();
            view.getRefreshPopup().hide();
        });

        view.getRefreshPopupNegativeButton().setOnAction(event -> view.getRefreshPopup().hide());
    }

    private void getIgnoredNodes(Node node, List<Node> ignoredNodes) {
        if (node.isIgnored()) {
            ignoredNodes.add(node);
        }

        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                getIgnoredNodes(child, ignoredNodes);
            }
        }
    }

    @Override
    public void onContinuePressed() {
        //Close property window if it's showing
        if (view.getNodePropertiesWindow() != null && view.getNodePropertiesWindow().isShowing()) {
            view.getNodePropertiesWindow().hide();
        }

        //Perform simple validation to make sure the tree structure is valid.
        if (!controller.getDomainProfileService().validateTree(controller.getPackageTree())) {
            view.getWarningPopupPositiveButton().setOnAction(arg01 -> {
                if (view.getWarningPopup() != null &&
                    view.getWarningPopup().isShowing()) {
                    view.getWarningPopup().hide();
                }
            });
            view.showWarningPopup(TextFactory.getText(ErrorKey.PACKAGE_TREE_VALIDATION_ERROR), TextFactory.getText(ErrorKey.INVALID_TREE_ERROR), false, false);
            return;
        }

        //Check if any nodes are missing files, if so don't allow continue
        if (!view.getMissingFileNodes().isEmpty()) {
            view.getWarningPopupPositiveButton().setOnAction(arg01 -> {
                if (view.getWarningPopup() != null &&
                    view.getWarningPopup().isShowing()) {
                    view.getWarningPopup().hide();
                }
            });
            view.showWarningPopup(TextFactory.getText(ErrorKey.PACKAGE_TREE_VALIDATION_ERROR), TextFactory.getText(ErrorKey.MISSING_FILES_ERROR), false, false);
            return;
        }

        super.onContinuePressed();

    }

    private void markNodeAsInvalid(Node node) {
        TreeItem<Node> invalidItem = findItem(node);

        if(invalidItem != null) {
            ImageView exclaimImage = new ImageView("/images/red_exclamation.png");
            exclaimImage.setFitHeight(20);
            exclaimImage.setFitWidth(5);

            Tooltip exclaimTooltip = new Tooltip(TextFactory.getText(Labels.LabelKey.PROPERTY_MISSING_LABEL));
            Tooltip.install(exclaimImage, exclaimTooltip);

            invalidItem.setGraphic(exclaimImage);
        }
    }

    private void markNodeAsValid(Node node) {
        TreeItem<Node> validItem = findItem(node);

        if(validItem != null && validItem.getGraphic() != null) {
            validItem.getGraphic().setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (view.getNodePropertiesWindow() != null && view.getNodePropertiesWindow().isShowing()) {
            view.getNodePropertiesWindow().hide();
        }

        //Perform simple validation to make sure the package description is valid.
        if (!controller.getDomainProfileService().validateTree(controller.getPackageTree())) {
            view.getWarningPopupPositiveButton().setOnAction(arg01 -> {
                if (view.getWarningPopup() != null &&
                    view.getWarningPopup().isShowing()) {
                    view.getWarningPopup().hide();
                }
            });
            view.showWarningPopup(TextFactory.getText(ErrorKey.PACKAGE_TREE_VALIDATION_ERROR), TextFactory.getText(ErrorKey.INVALID_TREE_ERROR), false, false);
            return;
        }
        super.onBackPressed();
    }

    private List<Property> getPropertiesFromBox(ProfilePropertyBox propertyBox) {
        List<Property> properties = new ArrayList<>();
        if (propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueType() !=
            PropertyValueType.COMPLEX) {

            propertyBox.getValues().stream().filter(value -> value !=
                null).forEach(value -> {
                if (propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueType() !=
                    null) {
                    switch (propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueType()) {
                        case DATE_TIME:
                            Property newProperty = new Property(propertyBox.getPropertyConstraint().getPropertyType());
                            newProperty.setDateTimeValue(new DateTime(((LocalDate) value).getYear(), ((LocalDate) value).getMonthValue(), ((LocalDate) value).getDayOfMonth(), 0, 0, 0));
                            properties.add(newProperty);
                            break;
                        case LONG:
                            newProperty = new Property(propertyBox.getPropertyConstraint().getPropertyType());
                            newProperty.setLongValue((Long) value);
                            properties.add(newProperty);
                            break;
                        default:
                            if (!((String) value).isEmpty()) {
                                //Only save valid properties
                                Validator validator = null;
                                if (propertyBox.getPropertyConstraint().getPropertyType() !=
                                    null && propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueHint() !=
                                    null) {
                                    validator = ValidatorFactory.getValidator(propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueHint());
                                }
                                if (validator == null || validator.isValid((String) value).getResult()) {
                                    newProperty = propertyFormatService.parsePropertyValue(propertyBox.getPropertyConstraint().getPropertyType(), (String) value);
                                    properties.add(newProperty);
                                }
                            }
                    }
                } else {
                    if (value instanceof String) {
                        if (!((String) value).isEmpty()) {
                            //Only save valid properties
                            Validator validator = null;
                            if (propertyBox.getPropertyConstraint().getPropertyType() !=
                                null && propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueHint() !=
                                null) {
                                validator = ValidatorFactory.getValidator(propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueHint());
                            }
                            if (validator == null || validator.isValid((String) value).getResult()) {
                                Property newProperty = propertyFormatService.parsePropertyValue(propertyBox.getPropertyConstraint().getPropertyType(), (String) value);
                                properties.add(newProperty);
                            }
                        }
                    }
                }
            });
        }

        return properties;
    }

    private void savePropertyFromBox(ProfilePropertyBox propertyBox) {
        if (!propertyBox.getPropertyConstraint().getPropertyType().isReadOnly()) {
            //First remove all properties of the given type, to be replaced with the new ones
            controller.getDomainProfileService().removeProperty(view.getPopupNode(), propertyBox.getPropertyConstraint().getPropertyType());
            //If it's not complex loop through the values and set them on the node

            if (propertyBox.getPropertyConstraint().getPropertyType().getPropertyValueType() !=
                PropertyValueType.COMPLEX) {
                for (Property property : getPropertiesFromBox(propertyBox)) {
                    controller.getDomainProfileService().addProperty(view.getPopupNode(), property);
                }
            } else {
                for (List<ProfilePropertyBox> complexPropertyBoxes : propertyBox.getSubPropertyBoxes()) {
                    Property newComplexProperty = new Property(propertyBox.getPropertyConstraint().getPropertyType());
                    List<Property> subPropertyValues = new ArrayList<>();
                    for (ProfilePropertyBox subPropertyBox : complexPropertyBoxes) {
                        subPropertyValues.addAll(getPropertiesFromBox(subPropertyBox));
                    }

                    //Check to make sure the complex property wasn't empty
                    if (!subPropertyValues.isEmpty()) {
                        newComplexProperty.setComplexValue(subPropertyValues);
                        controller.getDomainProfileService().addProperty(view.getPopupNode(), newComplexProperty);
                    }
                }
            }
        }
    }


    @Override
    public void saveCurrentNode() {
        if (view.getPopupNode() != null) {
            //First loop through all the properties in the popup
            view.getProfilePropertyBoxes().forEach(this::savePropertyFromBox);

            List<Property> userDefinedProperties = new ArrayList<>();
            //Then loop through all the user defined properties in the popup and save them to package state
            //The user checked the URI box but didn't enter a URI this should never happen with validation, but fix it here
            view.getUserDefinedPropertyBoxes().stream().filter(userDefinedPropertyBox ->
                                                                   userDefinedPropertyBox.getUserDefinedPropertyType() !=
                                                                       null &&
                                                                       userDefinedPropertyBox.getUserDefinedPropertyValues() !=
                                                                           null &&
                                                                       !userDefinedPropertyBox.getUserDefinedPropertyValues().isEmpty()).forEach(userDefinedPropertyBox -> {
                PropertyType propertyType = userDefinedPropertyBox.getUserDefinedPropertyType();

                //The user checked the URI box but didn't enter a URI this should never happen with validation, but fix it here
                userDefinedPropertyBox.getUserDefinedPropertyValues().stream().filter(propertyBox ->
                                                                                          propertyBox.getValueAsString() !=
                                                                                              null &&
                                                                                              !propertyBox.getValueAsString().isEmpty()).forEach(propertyBox -> {
                    Property newProperty = new Property(propertyType);

                    if (propertyType.getPropertyValueType() != null &&
                        propertyType.getPropertyValueType().equals(PropertyValueType.URI)) {
                        Validator uriValidator = ValidatorFactory.getValidator(PropertyValueHint.URI);
                        if (uriValidator == null ||
                            uriValidator.isValid(propertyBox.getValueAsString()).getResult()) {
                            try {
                                newProperty.setUriValue(new URI(propertyBox.getValueAsString()));
                            } catch (URISyntaxException e) {
                                //The user checked the URI box but didn't enter a URI this should never happen with validation, but fix it here
                                newProperty.getPropertyType().setPropertyValueType(PropertyValueType.STRING);
                                newProperty.setStringValue(propertyBox.getValueAsString());
                            }
                        }
                    } else {
                        newProperty.setStringValue(propertyBox.getValueAsString());
                    }

                    userDefinedProperties.add(newProperty);
                });
            });

            if (controller.getPackageState().getUserSpecifiedProperties() == null) {
                controller.getPackageState().setUserSpecifiedProperties(new HashMap<>());
            }
            controller.getPackageState().getUserSpecifiedProperties().put(view.getPopupNode().getIdentifier(), userDefinedProperties);

            //apply metadata inheritance
            applyMetadataInheritance(view.getPopupNode());
        }
    }

    private TreeItem<Node> buildTree(Node node, boolean showIgnoredArtifacts) {
        final TreeItem<Node> item = new TreeItem<>(node);

        item.expandedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue && newValue) {
                expandedNodes.add(item.getValue().getIdentifier());
            } else if (oldValue && !newValue) {
                expandedNodes.remove(item.getValue().getIdentifier());
            }
        });

        if (node.getChildren() != null) {
            node.getChildren().stream().filter(child -> showIgnoredArtifacts ||
                !child.isIgnored()).forEach(child -> item.getChildren().add(buildTree(child, showIgnoredArtifacts)));
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

    private void applyMetadataInheritance(Node node) {
        Set<org.dataconservancy.packaging.tool.model.dprofile.PropertyType> inheritablePropertyTypes = view.getInheritMetadataCheckBoxMap().keySet();

        if (node.getChildren() != null) {
            inheritablePropertyTypes.stream().filter(inheritablePropertyType -> view.getInheritMetadataCheckBoxMap().get(inheritablePropertyType).isSelected()).forEach(inheritablePropertyType -> {
                List<Property> inheritablePropertyValues = controller.getDomainProfileService().getProperties(node, inheritablePropertyType);
                if (inheritablePropertyValues != null) {
                    for (Property inheritablePropertyValue : inheritablePropertyValues) {
                        node.getChildren().stream().filter(child ->
                                                               !child.isIgnored() &&
                                                                   child.getDomainObject() !=
                                                                       null).forEach(child -> {
                            child.getNodeType().getPropertyConstraints().stream().filter(constraint -> constraint.getPropertyType().equals(inheritablePropertyType)).forEach(constraint -> {
                                List<Property> existingPropertyValues = controller.getDomainProfileService().getProperties(child, inheritablePropertyType);
                                if (!existingPropertyValues.contains(inheritablePropertyValue)) {
                                    controller.getDomainProfileService().addProperty(child, inheritablePropertyValue);
                                }
                            });
                            if (child.getChildren() != null) {
                                applyMetadataInheritance(child);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void changeType(List<Node> nodes, NodeTransform transform) {
        if (nodes != null && !nodes.isEmpty() && transform != null) {
            for (Node node : nodes) {
                controller.getDomainProfileService().transformNode(node, transform);
            }

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
    public void toggleItemIgnore(ObservableList<TreeItem<Node>> nodesToIgnore,
                                 boolean ignored) {
        for (TreeItem<Node> nodeToIgnore : nodesToIgnore) {
            ipmService.ignoreNode(nodeToIgnore.getValue(), ignored);

            clearError();
        }

        //To aid in being able to unignore multiple items at once we just check the entire tree for validity and then walk the tree to find types that need to be changed.
        if (!ignored && !getController().getDomainProfileService().validateTree(controller.getPackageTree())) {
            updateUnassignedNode(controller.getPackageTree());
        }

        //If the tree still isn't valid, try reassigning types to all nodes we changes.
        //This is almost guaranteed to never happen but is here as an ultimate safety check to ensure we always have a valid tree.
        if (!ignored && !getController().getDomainProfileService().validateTree(controller.getPackageTree())) {

            nodesToIgnore.stream().filter(nodeToIgnore -> !getController().getDomainProfileService().assignNodeTypes(getController().getPrimaryDomainProfile(), nodeToIgnore.getValue())).forEach(nodeToIgnore -> {
                ipmService.ignoreNode(nodeToIgnore.getValue(), true);
                showError(TextFactory.getText(ErrorKey.UNIGNORE_ERROR));
            });
        }

        // Rebuild the entire TreeView and reselect the previously selected nodes
        rebuildTreeView();

        for (TreeItem<Node> ignoredNode : nodesToIgnore) {
            view.getArtifactTreeView().getSelectionModel().select(ignoredNode);
        }
    }

    private boolean updateUnassignedNode(Node node) {
        if (!node.isIgnored()) {
            if (node.getNodeType() == null) {
                if (!getController().getDomainProfileService().assignNodeTypes(getController().getPrimaryDomainProfile(), node)) {
                    return false;
                }
            }

            if (node.getChildren() != null) {
                for (Node child : node.getChildren()) {
                    if (!updateUnassignedNode(child)) {
                        return false;
                    }
                }
            }
        }

        return true;
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

            //File info is null if it's a data item created for a metadata file transformation.
            if (nodeOne.getFileInfo() == null) {
                if (nodeTwo.getFileInfo() == null) {
                    return 0;
                } else if (nodeTwo.getFileInfo().isDirectory()) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (nodeTwo.getFileInfo() == null) {
                if (nodeOne.getFileInfo().isDirectory()) {
                    return -1;
                } else {
                    return 1;
                }
            }

            if (nodeOne.getFileInfo().isDirectory() == nodeTwo.getFileInfo().isDirectory()) {
                return 0;
            }

            if (nodeOne.getFileInfo().isDirectory()
                        && nodeTwo.getFileInfo().isFile()) {
                return -1;
            }

            return 1;
        });
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if(evt.getKey().equals(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE))) {
            view.getReenableWarningsButton().setVisible(Boolean.parseBoolean(evt.getNewValue()));
            view.getHideFutureWarningPopupCheckbox().setSelected(Boolean.parseBoolean(evt.getNewValue()));
        }
    }

    @Override
    public List<NodeType> getPossibleChildTypes(Node node) {
        List<NodeType> childNodes = new ArrayList<>();
        for (NodeType nodeType : controller.getPrimaryDomainProfile().getNodeTypes()) {
            if (nodeType.getParentConstraints() != null) {
                for (NodeConstraint parentConstraint : nodeType.getParentConstraints()) {
                    if (parentConstraint.matchesAny() || (parentConstraint.getNodeType() != null && parentConstraint.getNodeType().equals(node.getNodeType()))) {
                        childNodes.add(nodeType);
                        break;
                    }
                }
            }
        }

        return childNodes;
    }

    @Override
    public void addToTree(Node parent, Path contentToAdd) {
        try {
            Node node = ipmService.createTreeFromFileSystem(contentToAdd);
            parent.addChild(node);
            controller.getDomainProfileService().assignNodeTypes(controller.getPrimaryDomainProfile(), parent);

            //Refresh the tree display
            displayPackageTree();

        } catch (IOException e) {
            log.error(e.getMessage());
            showError(
                TextFactory.getText(ErrorKey.ADD_CONTENT_ERROR) + "\n" +
                    e.getMessage());
        }
    }

    @Override
    public Map<Node, NodeComparison> refreshTreeContent(Node node) {
        Map<Node, NodeComparison> resultMap = new HashMap<>();
        try {
            resultMap = ipmService.refreshTreeContent(node);
        } catch (IOException e) {
            log.error(e.getMessage());
            showError(
                TextFactory.getText(ErrorKey.REFRESH_ERROR) + "\n" +
                    e.getMessage());
        }

        return resultMap;
    }

    public void displayPackageTree() {
        if (controller.getPackageTree() != null) {
            view.getArtifactTreeView().setRoot(buildTree(controller.getPackageTree(),
                    view.getShowIgnored().selectedProperty().getValue()));
            view.getRoot().setExpanded(true);
            sortTree(view.getRoot());
            expandedNodes.stream().filter(nodeID -> nodeID !=
                null).forEach(nodeID -> {
                TreeItem<?> expandedItem = findItem(view.getRoot(), nodeID);
                
                if (expandedItem != null) {
                    expandedItem.setExpanded(true);
                }
            });
        }
    }

    public void rebuildTreeView() {
        displayPackageTree();
    }

    /*
     * Validates that all required properties are filled in for a given node.
     */
    private void validateNodeProperties(Node node, StringBuilder errBuilder) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        List<PropertyConstraint> violatedConstraints = controller.getDomainProfileService().validateProperties(node, node.getNodeType());
        if (!violatedConstraints.isEmpty()) {
            markNodeAsInvalid(node);

            if (node.getFileInfo() != null) {
                errBuilder.append(node.getFileInfo().getLocation().toString()).append("\n");
            } else {
                errBuilder.append(node.getIdentifier().toString()).append("\n");
            }

            for (PropertyConstraint violation : violatedConstraints) {
                errBuilder.append("\t--").append(violation.getPropertyType().getLabel()).append("\n");
            }

            errBuilder.append("\n");
        } else {
            markNodeAsValid(node);
        }

        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                validateNodeProperties(child, errBuilder);
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        }
    }

    /**
     * A {@link javafx.concurrent.Service} which executes the {@link javafx.concurrent.Task} for validating the node properties in the tree.
     */
    @SuppressWarnings("unused")
    private class PropertyValidationService extends Service<String> {

        Node node;
        public PropertyValidationService() {
        }

        public void setNode(Node node) {
            this.node = node;
        }

        @Override
        protected Task<String> createTask() {
            return new Task<String>() {
                @Override
                protected String call() throws Exception {
                    StringBuilder builder = new StringBuilder();
                    validateNodeProperties(node, builder);

                    return builder.toString();
                }
            };
        }
    }
}