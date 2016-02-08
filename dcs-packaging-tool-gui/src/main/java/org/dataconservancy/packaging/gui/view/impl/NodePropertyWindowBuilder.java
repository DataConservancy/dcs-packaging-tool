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
import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.model.UserDefinedPropertyVocabulary;
import org.dataconservancy.packaging.gui.model.UserDefinedPropertyGroupJSONBuilder;
import org.dataconservancy.packaging.gui.util.EmptyFieldButtonDisableListener;
import org.dataconservancy.packaging.gui.util.ProfilePropertyBox;
import org.dataconservancy.packaging.gui.util.UserDefinedPropertyBox;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyCategory;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for building the Node Property window. This class has no control over how this window is displayed it just returns a simple Pane.
 */
public class NodePropertyWindowBuilder implements CssConstants {

    private BorderPane nodePropertiesWindow;

    //Controls that are displayed in the package artifact popup.
    private Hyperlink cancelPopupLink;
    private Button applyPopupButton;

    //maximum width for addNewButtons, so that text appears on button


    private Map<PropertyType, CheckBox> metadataInheritanceButtonMap;

    List<UserDefinedPropertyVocabulary> availableUserDefinedPropertyVocabularies;
    private DisciplineLoadingService disciplineLoadingService;
    private DomainProfileService profileService;

    private Map<PropertyCategory, PropertyCategoryBox> categoryMap;
    private List<ProfilePropertyBox> nodePropertyBoxes;
    private List<UserDefinedPropertyBox> userDefinedProperties;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public NodePropertyWindowBuilder(Hyperlink cancelPopupLink, Button applyPopupButton,
                                     DisciplineLoadingService disciplineLoadingService) {
        this.cancelPopupLink = cancelPopupLink;
        this.applyPopupButton = applyPopupButton;

        this.disciplineLoadingService = disciplineLoadingService;
        loadAvailableUserDefinedProperties();
    }

    /**
     * Function for building the artifact properties window.
     * @param node The node whose properties should be displayed
     * @param metadataInheritanceButtonMap The properties and checkboxes corresponding to available inheritable fields.
     * @param profileService The profile service to user for retrieving properties of the node.
     * @param userDefinedPropertyValues Any user defined properties that are created.
     * @return A pane with the node properties views.
     */
    public Pane buildArtifactPropertiesLayout(Node node,
                                              Map<PropertyType, CheckBox> metadataInheritanceButtonMap,
                                              DomainProfileService profileService, List<Property> userDefinedPropertyValues) {
        categoryMap = new HashMap<>();
        nodePropertyBoxes = new ArrayList<>();
        userDefinedProperties = new ArrayList<>();

        nodePropertiesWindow = new BorderPane();
        nodePropertiesWindow.setMinHeight(500);
        nodePropertiesWindow.setMinWidth(540);

        nodePropertiesWindow.getStylesheets().add("/css/propertywindow.css");
        nodePropertiesWindow.getStyleClass().add(PACKAGE_TOOL_POPUP_CLASS);

        this.metadataInheritanceButtonMap = metadataInheritanceButtonMap;
        this.profileService = profileService;

        createNodePropertiesView(node, userDefinedPropertyValues);

        return nodePropertiesWindow;
    }

    public List<ProfilePropertyBox> getNodePropertyBoxes() {
        return nodePropertyBoxes;
    }

    public List<UserDefinedPropertyBox> getUserDefinedPropertyBoxes() {
        return userDefinedProperties;
    }

    /**
     * Creates an artifact details view. This popup's content is a tabbed view, with a tab for each domain object,
     * and a tab for user defined properties.
     * @param node The node whose properties will be diplayed.
     * @param userDefinedProperties The list of user defined properties created for this node.
     */
    private void createNodePropertiesView(Node node, List<Property> userDefinedProperties) {

        //The tab pane will consist of a tab for each domain object and one for user defined properties
        TabPane propertiesPopup = new TabPane();

        //Create the property tab for the main node type.
        propertiesPopup.getTabs().add(createNodeTypeTab(node, node.getNodeType()));

        //Loop through and create the tabs for all the sub types
        if (node.getSubNodeTypes() != null) {
            for (NodeType type : node.getSubNodeTypes()) {
                propertiesPopup.getTabs().add(createNodeTypeTab(node, type));
            }
        }

        //Create the user defined properties tab that displays all user defined properties the artifact has.
        Tab relationshipTab = new Tab();
        relationshipTab.setClosable(false);
        relationshipTab.setText(TextFactory.getText(Labels.LabelKey.USER_PROPERTIES_LABEL));
        ScrollPane relationshipPane = new ScrollPane();
        relationshipPane.setHvalue(500);
        relationshipPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        relationshipPane.setContent(createUserDefinedPropertiesTab(userDefinedProperties));
        relationshipPane.setMinWidth(500);
        relationshipPane.setFitToWidth(true);
        relationshipTab.setContent(relationshipPane);
        propertiesPopup.getTabs().add(relationshipTab);

        nodePropertiesWindow.setCenter(propertiesPopup);

        HBox popupControls = new HBox(24);
        popupControls.setAlignment(Pos.CENTER_RIGHT);

        popupControls.getStyleClass().add(VIEW_FOOTER_CLASS);
        popupControls.setPrefHeight(40);

        popupControls.getChildren().add(cancelPopupLink);

        popupControls.getChildren().add(applyPopupButton);

        nodePropertiesWindow.setBottom(popupControls);
    }

    /**
     * Creates a tab for the all the domain properties the node has for the given node type.
     * These will typically correspond to different domain objects the node represents.
     * @param node The node being displayed.
     * @param nodeType The specific node type for this tab.
     * @return The tab view to add to the tab pane
     */
    private Tab createNodeTypeTab(Node node, NodeType nodeType) {
        Tab propertiesTab = new Tab();
        propertiesTab.setClosable(false);
        propertiesTab.setText(nodeType.getLabel());
        ScrollPane propertiesPane = new ScrollPane();
        propertiesPane.setHvalue(500);
        propertiesPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        propertiesPane.setMinWidth(500);
        propertiesPane.setFitToWidth(true);

        VBox propertyContent = new VBox(8);
        propertyContent.getStyleClass().add(NODE_PROPERTY_WINDOW_CLASS);
        propertiesPane.setContent(propertyContent);
        propertiesTab.setContent(propertiesPane);

        PropertyCategoryBox generalPropertyBox = new PropertyCategoryBox(TextFactory.format(Messages.MessageKey.TYPE_PROPERTY_LABEL, nodeType.getLabel()));
        propertyContent.getChildren().add(generalPropertyBox);

        Label requiredPropertiesExplanation = new Label(TextFactory.getText(Labels.LabelKey.REQUIRED_PROPERTIES_EXPLANATION_LABEL));
        requiredPropertiesExplanation.setWrapText(true);
        requiredPropertiesExplanation.getStyleClass().add(REQUIRED_PROPERTY_EXPLANATION);
        generalPropertyBox.getChildren().add(requiredPropertiesExplanation);

        List<PropertyConstraint> sortedProperties = new ArrayList<>();

        //Get the property name key set and then create a sorted list from it.
        //Currently properties are sorted so that required properties appear first
        sortedProperties.addAll(nodeType.getPropertyConstraints());
        sortProperties(sortedProperties);

        Label requiredLabel = new Label();
        requiredLabel.getStyleClass().add(CATEGORY_SUB_TITLE);
        requiredLabel.setText(TextFactory.getText(Labels.LabelKey.REQUIRED_PROPERTIES_LABEL));
        generalPropertyBox.getChildren().add(requiredLabel);

        Label optionalLabel = new Label();
        optionalLabel.getStyleClass().add(CATEGORY_SUB_TITLE);
        optionalLabel.setText(TextFactory.getText(Labels.LabelKey.OPTIONAL_PROPERTIES_LABEL));

        boolean shownOptionalLabel = false;

        //To get the properties on a node we loop through the constraints on the type
        for (PropertyConstraint propertyConstraint : sortedProperties) {
            if (propertyConstraint.getMinimum() == 0 && !shownOptionalLabel) {
                generalPropertyBox.getChildren().add(optionalLabel);
                shownOptionalLabel = true;
            }

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
        propertyContent.getChildren().add(createInheritanceGroup(node));

        return propertiesTab;

    }

    /**
     * Creates the inheritance section of the view
     * @param node The popup node
     * @return The vbox with the inheritance controls, if there are no inheritable fields it will return an empty vbox.
     */
    public VBox createInheritanceGroup(final Node node) {

        final VBox inheritanceBox = new VBox(12);
        if (node.getNodeType().getInheritableProperties() != null) {
            Label inheritanceTitle = new Label(TextFactory.getText(Labels.LabelKey.INHERITANCE_LABEL));
            inheritanceTitle.getStyleClass().add(GROUP_TITLE);

            inheritanceBox.getChildren().add(inheritanceTitle);

            inheritanceBox.getStyleClass().add(INHERITANCE_CONTROLS);

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

            for (PropertyType inheritableProperty : node.getNodeType().getInheritableProperties()) {
                //If the property is inheritable, create a button which would allow the values to be apply to children
                //appropriately

                inheritanceBox.getChildren().add(createInheritanceBox(inheritableProperty));

                groupSeparator = new Separator();
                inheritanceBox.getChildren().add(groupSeparator);
            }
        }
        return inheritanceBox;
    }

    /**
     * Creates an Hbox that represents the inheritance control, this is currently a label and a checkbox
     * @param propertyType The property type that can be inherited.
     * @return The HBox with the inheritance controls for the property.
     */
    private HBox createInheritanceBox(PropertyType propertyType) {
        HBox propertyBox = new HBox(30);

        VBox propNameAndExplation = new VBox();
        final Label propertyNameLabel = new Label(propertyType.getLabel());
        propertyNameLabel.setPrefWidth(400);
        propertyNameLabel.getStyleClass().add(CssConstants.BOLD_TEXT_CLASS);

        propNameAndExplation.getChildren().add(propertyNameLabel);

        propertyBox.getChildren().add(propNameAndExplation);

        final CheckBox applyPropertyValueToChildrenCheckBox = new CheckBox();
        propertyBox.getChildren().add(applyPropertyValueToChildrenCheckBox);

        //Add inheritance to map
        metadataInheritanceButtonMap.put(propertyType, applyPropertyValueToChildrenCheckBox);
        return propertyBox;
    }

    /**
     * Creates the user defined properties tab in the popup. User defined properties are handled differently because they break the notion of the profile
     * @param userDefinedPropertyValues The list of existing user defined properties
     * @return A VBox with all of the user defined properties and the controls for adding new properties.
     */
    private VBox createUserDefinedPropertiesTab(List<Property> userDefinedPropertyValues) {
        final VBox userDefinedPropertiesBox = new VBox(38);
        userDefinedPropertiesBox.getStyleClass().add(NODE_PROPERTY_WINDOW_CLASS);

        final double addNewButtonMaxWidth = 200;

        //Create the button for adding new relationships this will add a new set of relationship controls.
        final Button addNewUserDefinedPropertyButton = new Button(TextFactory.getText(Labels.LabelKey.ADD_NEW_PROPERTY_BUTTON));

        addNewUserDefinedPropertyButton.setMaxWidth(addNewButtonMaxWidth);

        //Listener for disabling the add new button when fields of a user defined property box are empty.
        final EmptyFieldButtonDisableListener addNewRelationshipListener = new EmptyFieldButtonDisableListener(addNewUserDefinedPropertyButton);

        //If there are no user defined properties already set create a new one and we're done
        if (userDefinedPropertyValues == null || userDefinedPropertyValues.isEmpty()) {
            UserDefinedPropertyBox userDefinedPropertyBox = new UserDefinedPropertyBox(null, null, availableUserDefinedPropertyVocabularies, addNewRelationshipListener);
            userDefinedPropertiesBox.getChildren().add(userDefinedPropertyBox);
            userDefinedProperties.add(userDefinedPropertyBox);
            addNewUserDefinedPropertyButton.setDisable(true);
        } else {
            //Get the string labels for all of the user defined properties to display in the GUI.
            Map<PropertyType, List<String>> propertyMap = getLabelsForExistingUserDefinedProperties(userDefinedPropertyValues);
            //Now loop through the properties and create controls for each
            for (PropertyType propertyType : propertyMap.keySet()) {
                UserDefinedPropertyBox userDefinedPropertyBox = new UserDefinedPropertyBox(propertyType, propertyMap.get(propertyType), availableUserDefinedPropertyVocabularies, addNewRelationshipListener);
                userDefinedPropertiesBox.getChildren().add(userDefinedPropertyBox);
                userDefinedProperties.add(userDefinedPropertyBox);

                if (propertyMap.get(propertyType) == null || propertyMap.get(propertyType).isEmpty()) {
                    addNewUserDefinedPropertyButton.setDisable(true);
                }
            }
        }

        userDefinedPropertiesBox.getChildren().add(addNewUserDefinedPropertyButton);


        addNewUserDefinedPropertyButton.setOnAction(arg0 -> {
            UserDefinedPropertyBox newUserDefinedPropertyBox = new UserDefinedPropertyBox(null, null, availableUserDefinedPropertyVocabularies, addNewRelationshipListener);
            userDefinedProperties.add(newUserDefinedPropertyBox);
            int buttonIndex = userDefinedPropertiesBox.getChildren().indexOf(addNewUserDefinedPropertyButton);

            userDefinedPropertiesBox.getChildren().add(buttonIndex, newUserDefinedPropertyBox);

            addNewUserDefinedPropertyButton.setDisable(true);
        });

        return userDefinedPropertiesBox;
    }

    /**
     * Loops through all the user defined properties and fetches the string values for them for displaying in the GUI.
     * @param propertyValues The list of user defined properties.
     * @return A map with the property type and the list of string values for each property.
     */
    private Map<PropertyType, List<String>> getLabelsForExistingUserDefinedProperties(List<Property> propertyValues) {
        Map<PropertyType, List<String>> propertyMap = new HashMap<>();

        for (Property property : propertyValues) {

            String propertyValue;

            if (property.getPropertyType().getPropertyValueType() != null && property.getPropertyType().getPropertyValueType().equals(PropertyValueType.URI)) {
                propertyValue = property.getUriValue().toString();
            } else {
                propertyValue = property.getStringValue();
            }

            if (propertyMap.keySet().contains(property.getPropertyType())) {
                propertyMap.get(property.getPropertyType()).add(propertyValue);
            } else {
                List<String> newPropertyValues = new ArrayList<>();
                newPropertyValues.add(propertyValue);
                propertyMap.put(property.getPropertyType(), newPropertyValues);
            }
        }

        return propertyMap;
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

    /**
     * There are certain defined properties that can be used in the user defined properties window. This code loads them.
     * They are used to populate drop down lists for when users don't actually want to enter their properties.
     */
    private void loadAvailableUserDefinedProperties() {

        try {
            InputStream is = Configuration.getConfigurationFileInputStream(Configuration.ConfigFile.USER_PROPS);
            availableUserDefinedPropertyVocabularies = UserDefinedPropertyGroupJSONBuilder.deserialize(is);
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * PropertyCategoryBox is a class that represents grouping of property controls.
     */
    private class PropertyCategoryBox extends VBox {
        PropertyCategoryBox(String title) {
            setSpacing(10);
            getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);
            Label titleLabel = new Label();
            titleLabel.getStyleClass().add(GROUP_TITLE);
            titleLabel.setText(title);

            getChildren().add(titleLabel);
        }

        void addProperty(PropertyConstraint constraint, Node node) {
            List<Property> existingProperties = profileService.getProperties(node, constraint.getPropertyType());
            ProfilePropertyBox profilePropertyBox = new ProfilePropertyBox(constraint, existingProperties, disciplineLoadingService);
            profilePropertyBox.getStyleClass().add(PROPERTY);

            nodePropertyBoxes.add(profilePropertyBox);
            getChildren().add(profilePropertyBox);
        }
    }
}
