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
package org.dataconservancy.packaging.gui.view.impl;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.model.RelationshipGroup;
import org.dataconservancy.packaging.gui.model.RelationshipGroupJSONBuilder;
import org.dataconservancy.packaging.gui.util.ApplyButtonValidationListener;
import org.dataconservancy.packaging.gui.util.ProfilePropertyBox;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyCategory;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageArtifactWindowBuilder implements CssConstants {

    private BorderPane artifactDetailsLayout;

    //Controls that are displayed in the package artifact popup.
    private Hyperlink cancelPopupLink;
    private Button applyPopupButton;

    //maximum width for addNewButtons, so that text appears on button


    private Map<String, CheckBox> metadataInheritanceButtonMap;

    List<RelationshipGroup> availableRelationshipGroups;
    Map<String, List<String>> availableDisciplines;

    private ApplyButtonValidationListener applyButtonValidationListener;
    private DomainProfileService profileService;

    private Map<PropertyCategory, PropertyCategoryBox> categoryMap;
    private List<ProfilePropertyBox> nodePropertyBoxes;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public PackageArtifactWindowBuilder(Hyperlink cancelPopupLink,
                                        Button applyPopupButton,
                                        String availableRelationshipsPath,
                                        DisciplineLoadingService disciplineLoadingService) {
        this.cancelPopupLink = cancelPopupLink;
        this.applyPopupButton = applyPopupButton;

        applyButtonValidationListener = new ApplyButtonValidationListener(applyPopupButton);
        availableDisciplines = disciplineLoadingService.getAllDisciplines();
        loadAvailableRelationships(availableRelationshipsPath);
    }

    public Pane buildArtifactPropertiesLayout(Node node,
                                              Map<String, CheckBox> metadataInheritanceButtonMap,
                                              DomainProfileService profileService) {
        categoryMap = new HashMap<>();
        nodePropertyBoxes = new ArrayList<>();

        artifactDetailsLayout = new BorderPane();
        artifactDetailsLayout.setMinHeight(500);
        artifactDetailsLayout.setMinWidth(540);

        artifactDetailsLayout.getStylesheets().add("/css/app.css");
        artifactDetailsLayout.getStyleClass().add(PACKAGE_TOOL_POPUP_CLASS);

        this.metadataInheritanceButtonMap = metadataInheritanceButtonMap;
        this.profileService = profileService;

        createArtifactDetailsPopup(node);

        return artifactDetailsLayout;
    }

    public List<ProfilePropertyBox> getNodePropertyBoxes() {
        return nodePropertyBoxes;
    }

    /*
     * Creates an artifact details popup. This popup's content is a tabbed view, with a tab for general properties,
     * creator properties, and relationships.
     * @param artifact
     */
    private void createArtifactDetailsPopup(Node node) {

        //The property popup will consist of the three tabs, general, creator and relationships.
        TabPane propertiesPopup = new TabPane();

        //Create the property tab for the main node type.
        propertiesPopup.getTabs().add(createNodeTypeTab(node, node.getNodeType()));

        //Loop through and create the tabs for all the sub types
        if (node.getSubNodeTypes() != null) {
            for (NodeType type : node.getSubNodeTypes()) {
                propertiesPopup.getTabs().add(createNodeTypeTab(node, type));
            }
        }

        //Create the relationship tab that displays all relationships the artifact has.
        Tab relationshipTab = new Tab();
        relationshipTab.setClosable(false);
        relationshipTab.setText(TextFactory.getText(Labels.LabelKey.PACKAGE_ARTIFACT_RELATIONSHIPS));
        ScrollPane relationshipPane = new ScrollPane();
        relationshipPane.setHvalue(500);
        relationshipPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        relationshipPane.setContent(createRelationshipTab(node));
        relationshipPane.setMinWidth(500);
        relationshipPane.setFitToWidth(true);
        relationshipTab.setContent(relationshipPane);
        propertiesPopup.getTabs().add(relationshipTab);

        artifactDetailsLayout.setCenter(propertiesPopup);

        HBox popupControls = new HBox(24);
        popupControls.setAlignment(Pos.CENTER_RIGHT);

        popupControls.getStyleClass().add(VIEW_FOOTER_CLASS);
        popupControls.setPrefHeight(40);

        popupControls.getChildren().add(cancelPopupLink);

        popupControls.getChildren().add(applyPopupButton);

        artifactDetailsLayout.setBottom(popupControls);
    }

    private Tab createNodeTypeTab(Node node, NodeType nodeType) {
        Tab propertiesTab = new Tab();
        propertiesTab.setClosable(false);
        propertiesTab.setText(nodeType.getLabel());
        ScrollPane propertiesPane = new ScrollPane();
        propertiesPane.getStyleClass().add(PROPERTIES_POPUP_CLASS);
        propertiesPane.setHvalue(500);
        propertiesPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        propertiesPane.setMinWidth(500);
        propertiesPane.setFitToWidth(true);

        VBox propertyContent = new VBox(8);
        propertiesPane.setContent(propertyContent);
        propertiesTab.setContent(propertiesPane);

        PropertyCategoryBox generalPropertyBox = new PropertyCategoryBox(nodeType.getLabel());
        propertyContent.getChildren().add(generalPropertyBox);

        List<PropertyConstraint> sortedProperties = new ArrayList<>();

        //Get the property name key set and then create a sorted list from it.
        sortedProperties.addAll(nodeType.getPropertyConstraints());
        sortProperties(sortedProperties);

        //To get the properties on a node we loop through the constraints on the type
        for (PropertyConstraint propertyConstraint : sortedProperties) {
            PropertyType type = propertyConstraint.getPropertyType();
            if (type.getPropertyCategory() != null) {
                if (categoryMap.containsKey(type.getPropertyCategory())) {
                    categoryMap.get(type.getPropertyCategory()).addProperty(propertyConstraint, node);
                } else {
                    PropertyCategoryBox categoryBox = new PropertyCategoryBox(type.getPropertyCategory().getLabel());
                    categoryMap.put(type.getPropertyCategory(), categoryBox);

                    categoryBox.addProperty(propertyConstraint, node);
                }
            } else {
                generalPropertyBox.addProperty(propertyConstraint, node);
            }
        }

        for (PropertyCategory category : categoryMap.keySet()) {
            propertyContent.getChildren().add(categoryMap.get(category));
        }

        //Add the inheritance controls
        propertyContent.getChildren().add(new Label(TextFactory.getText(Labels.LabelKey.PACKAGE_ARTIFACT_INHERITANCE)));
        propertyContent.getChildren().add(createInheritanceGroup(node));

        return propertiesTab;

    }

    /**
     * Creates the inheritance tab in the popup.
     * @param node The popup node
     * @return inheritance tab or null if nothing to do
     */
    public VBox createInheritanceGroup(final Node node) {

        final VBox inheritanceBox = new VBox(12);
        if (node.getNodeType().getInheritableProperties() != null) {
            inheritanceBox.getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);
            boolean hasInheritableProperties = false;

            //create label to explain what this tab is about.
            Label inheritanceTabIntroLabel = new Label(TextFactory.getText(Labels.LabelKey.INHERITANCE_TAB_INTRO));
            inheritanceTabIntroLabel.setPrefWidth(450);
            inheritanceTabIntroLabel.setWrapText(true);
            inheritanceBox.getChildren().add(inheritanceTabIntroLabel);

            //create label to explain usage of buttons.
            Label inheritanceButtonExplainedLabel = new Label(TextFactory.getText(Labels.LabelKey.INHERITANCE_BUTTON_EXPLAINED));
            inheritanceButtonExplainedLabel.setPrefWidth(450);
            inheritanceButtonExplainedLabel.setWrapText(true);
            inheritanceBox.getChildren().add(inheritanceButtonExplainedLabel);

            Separator groupSeparator = new Separator();
            inheritanceBox.getChildren().add(groupSeparator);

            //Loop through properties for the given artifact.

            for (PropertyType inhertiableProperty : node.getNodeType().getInheritableProperties()) {
                //If the property is inheritable, create a button which would allow the values to be apply to children
                //appropriately

                inheritanceBox.getChildren().add(createInheritanceBox(inhertiableProperty.getLabel()));

                groupSeparator = new Separator();
                inheritanceBox.getChildren().add(groupSeparator);
            }
        }
        return inheritanceBox;
    }

    private HBox createInheritanceBox(String propertyName) {
        HBox propertyBox = new HBox(30);

        VBox propNameAndExplation = new VBox();
        final Label propertyNameLabel = new Label(propertyName);
        propertyNameLabel.setPrefWidth(400);
        propertyNameLabel.getStyleClass().add(CssConstants.BOLD_TEXT_CLASS);
        //propertyNameLabel.setStyle(CssConstants.BOLD_TEXT_CLASS);

        /* TODO: If we want to list the node types that can inherit a property we need to loop through types and check if it has that property
        StringBuilder sb = new StringBuilder();
        final String typeSeparator = ", ";
        for (String inheritingTypes : presenter.getInheritingTypes(artifactType, propertyName)) {
            sb.append(ontologyLabels.get(inheritingTypes));
            sb.append(typeSeparator);
        }
        sb.deleteCharAt(sb.lastIndexOf(typeSeparator));

        final Label descendantTypesLabel = new Label(String.format(labels.get(Labels.LabelKey.INHERITANCE_DESCENDANT_TYPE),
                sb.toString()));
        descendantTypesLabel.setPrefWidth(400);
        descendantTypesLabel.setWrapText(true);
        */

        propNameAndExplation.getChildren().add(propertyNameLabel);
        //propNameAndExplation.getChildren().add(descendantTypesLabel);

        //propertyBox.getChildren().add(propertyNameLabel);
        propertyBox.getChildren().add(propNameAndExplation);

        final CheckBox applyPropertyValueToChildrenCheckBox = new CheckBox();
        propertyBox.getChildren().add(applyPropertyValueToChildrenCheckBox);

        //Add inheritance to map
        metadataInheritanceButtonMap.put(propertyName, applyPropertyValueToChildrenCheckBox);
        return propertyBox;
    }

    /*
     * Creates the relationship tab in the popup. Relationships are handled differently from properties and are constructed using the {@code createRelationshipBox} method.
     * @param artifact
     * @return
     */
    private VBox createRelationshipTab(final Node node) {
        final VBox relationshipsBox = new VBox(38);

        /* TODO: NO idea how were going to do this just diplay all triples on the node??
        relationshipsBox.getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);
        //If there aren't any existing relationships add an empty relationship box

        //add advice explaining that hierarchical relationships are not modifiable
        Label hierarchicalAdviceLabel = new Label(labels.get(Labels.LabelKey.HIERARCHICAL_ADVICE_LABEL));
        hierarchicalAdviceLabel.setAlignment(Pos.TOP_LEFT);

        relationshipsBox.getChildren().add(hierarchicalAdviceLabel);
        //Create the button for adding new relationships this will add a new set of relationship controls.
        final Button addNewRelationshipButton = new Button(labels.get(Labels.LabelKey.ADD_RELATIONSHIP_BUTTON));
        addNewRelationshipButton.setMaxWidth(addNewButtonMaxWidth);

        final EmptyFieldButtonDisableListener addNewRelationshipListener = new EmptyFieldButtonDisableListener(addNewRelationshipButton);

        if (artifact.getRelationships().isEmpty()) {
            EditPackageContentsViewImpl.NodeRelationshipContainer container = new EditPackageContentsViewImpl.NodeRelationshipContainer();
            artifactRelationshipFields.add(container);
            relationshipsBox.getChildren().add(new RelationshipSelectionBox(artifact, null, container, availableRelationshipGroups, labels, packageOntologyService, addNewRelationshipListener));
            addNewRelationshipButton.setDisable(true);
        } else {
            //Otherwise loop through the relationships and create a box for each one.
            for (PackageRelationship relationship : artifact.getRelationships()) {
                EditPackageContentsViewImpl.NodeRelationshipContainer container = new EditPackageContentsViewImpl.NodeRelationshipContainer();
                artifactRelationshipFields.add(container);
                relationshipsBox.getChildren().add(new RelationshipSelectionBox(artifact, relationship, container, availableRelationshipGroups, labels, packageOntologyService, addNewRelationshipListener));
                if (relationship.getTargets() == null || relationship.getTargets().isEmpty()) {
                    addNewRelationshipButton.setDisable(true);
                }
            }
        }

        relationshipsBox.getChildren().add(addNewRelationshipButton);

        addNewRelationshipButton.setOnAction(arg0 -> {
            EditPackageContentsViewImpl.NodeRelationshipContainer container = new EditPackageContentsViewImpl.NodeRelationshipContainer();
            artifactRelationshipFields.add(container);
            VBox newRelationshipBox = new RelationshipSelectionBox(artifact, null, container, availableRelationshipGroups, labels, packageOntologyService, addNewRelationshipListener);
            int buttonIndex = relationshipsBox.getChildren().indexOf(addNewRelationshipButton);

            relationshipsBox.getChildren().add(buttonIndex, newRelationshipBox);

            addNewRelationshipButton.setDisable(true);
            requestFocusForNewGroup(newRelationshipBox);
        });
        */
        return relationshipsBox;
    }



    //Sorts properties in the order of single value required, multi value required, optional single value, optional multi value
    private void sortProperties(List<PropertyConstraint> propertyConstraints) {
        Collections.sort(propertyConstraints, (propertyOne, propertyTwo) -> {

            int propertyOneMaxOccurs = propertyOne.getMaximum();
            int propertyOneMinOccurs = propertyOne.getMinimum();

            int propertyTwoMaxOccurs = propertyTwo.getMaximum();
            int propertyTwoMinOccurs = propertyTwo.getMinimum();

            if (propertyOneMinOccurs == propertyTwoMinOccurs && propertyOneMaxOccurs == propertyTwoMaxOccurs) {
                return 0;
            }

            if (propertyOneMinOccurs == propertyTwoMinOccurs) {
                if (propertyOneMaxOccurs < propertyTwoMaxOccurs) {
                    return -1;
                }
            } else if (propertyOneMinOccurs > propertyTwoMinOccurs) {
                return -1;
            }

            return 1;
        });
    }

    private void loadAvailableRelationships(String relationshipsPath) {

        if (relationshipsPath != null && !relationshipsPath.isEmpty()) {
            if (relationshipsPath.startsWith("classpath:")) {
                String path = relationshipsPath.substring("classpath:".length());
                if(!path.startsWith("/")){
                    path = "/" + path;
                }
                InputStream fileStream = PackageArtifactWindowBuilder.class.getResourceAsStream(path);
                if (fileStream != null) {
                    availableRelationshipGroups = RelationshipGroupJSONBuilder.deserialize(fileStream);
                } else {
                    log.error("Error reading classpath relationships file: " + relationshipsPath);
                }

            } else {
                File paramFile = new File(relationshipsPath);
                if (paramFile.exists()) {
                    try {
                        availableRelationshipGroups = RelationshipGroupJSONBuilder.deserialize(new FileInputStream(paramFile));
                    } catch (FileNotFoundException e) {
                        log.error("Error reading selected relationships file: " + relationshipsPath + " " + e.getMessage());
                    }
                }
            }
        }

        //If the file is null attempt to load the built in resource file.
        if (availableRelationshipGroups.isEmpty()) {
            InputStream fileStream = PackageArtifactWindowBuilder.class.getResourceAsStream("/defaultRelationships");
            if (fileStream != null) {
                availableRelationshipGroups = RelationshipGroupJSONBuilder.deserialize(fileStream);
            }
            else {
                log.error("Error reading default relationships file. Couldn't find classpath file: /defaultRelationships");
            }
        }
    }

    private class PropertyCategoryBox extends VBox {
        PropertyCategoryBox(String title) {
            setSpacing(10);
            getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);
            Label titleLabel = new Label();
            titleLabel.setText(title);

            getChildren().add(titleLabel);
        }

        void addProperty(PropertyConstraint constraint, Node node) {
            ProfilePropertyBox profilePropertyBox = new ProfilePropertyBox(constraint, node, profileService);
            nodePropertyBoxes.add(profilePropertyBox);
            getChildren().add(profilePropertyBox);
        }
    }
}
