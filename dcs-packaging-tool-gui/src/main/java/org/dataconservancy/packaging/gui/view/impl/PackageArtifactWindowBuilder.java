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

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.OntologyLabels;
import org.dataconservancy.packaging.gui.model.RelationshipGroup;
import org.dataconservancy.packaging.gui.model.RelationshipGroupJSONBuilder;
import org.dataconservancy.packaging.gui.presenter.PackageDescriptionPresenter;
import org.dataconservancy.packaging.gui.util.ApplyButtonValidationListener;
import org.dataconservancy.packaging.gui.util.DisciplinePropertyBox;
import org.dataconservancy.packaging.gui.util.EmptyFieldButtonDisableListener;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.util.RelationshipSelectionBox;
import org.dataconservancy.packaging.gui.util.TextPropertyBox;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackageArtifactWindowBuilder implements  CssConstants {

    private BorderPane artifactDetailsLayout;
    private Labels labels;
    private OntologyLabels ontologyLabels;
    private Messages messages;

    private Map<String, PackageDescriptionViewImpl.ArtifactPropertyContainer> artifactPropertyFields;
    private Set<PackageDescriptionViewImpl.ArtifactRelationshipContainer> artifactRelationshipFields;

    //Controls that are displayed in the package artifact popup.
    private Hyperlink cancelPopupLink;
    private Button applyPopupButton;

    private PackageOntologyService packageOntologyService;
    //maximum width for addNewButtons, so that text appears on button
    private double addNewButtonMaxWidth = 200;

    private Map<String, CheckBox> metadataInheritanceButtonMap;

    PackageDescriptionPresenter presenter;

    List<RelationshipGroup> availableRelationshipGroups;
    Map<String, List<String>> availableDisciplines;

    private ApplyButtonValidationListener applyButtonValidationListener;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public PackageArtifactWindowBuilder(Labels labels,
                                        OntologyLabels ontologyLabels,
                                        Hyperlink cancelPopupLink,
                                        Button applyPopupButton,
                                        String availableRelationshipsPath,
                                        DisciplineLoadingService disciplineLoadingService,
                                        Messages messages) {
        this.labels = labels;
        this.ontologyLabels = ontologyLabels;
        this.cancelPopupLink = cancelPopupLink;
        this.applyPopupButton = applyPopupButton;
        this.messages = messages;

        applyButtonValidationListener = new ApplyButtonValidationListener(applyPopupButton);
        availableDisciplines = disciplineLoadingService.getAllDisciplines();
        loadAvailableRelationships(availableRelationshipsPath);
    }

    public Pane buildArtifactPropertiesLayout(PackageArtifact artifact,
                                              Map<String, PackageDescriptionViewImpl.ArtifactPropertyContainer> artifactPropertyFields,
                                              Set<PackageDescriptionViewImpl.ArtifactRelationshipContainer> artifactRelationshipFields,
                                              Map<String, CheckBox> metadataInheritanceButtonMap,
                                              PackageDescriptionPresenter packageDescriptionPresenter,
                                              PackageOntologyService packageOntologyService) {

        this.artifactPropertyFields = artifactPropertyFields;
        this.artifactRelationshipFields = artifactRelationshipFields;

        artifactDetailsLayout = new BorderPane();
        artifactDetailsLayout.setMinHeight(500);
        artifactDetailsLayout.setMinWidth(540);

        artifactDetailsLayout.getStylesheets().add("/css/app.css");
        artifactDetailsLayout.getStyleClass().add(PACKAGE_TOOL_POPUP_CLASS);

        this.metadataInheritanceButtonMap = metadataInheritanceButtonMap;
        this.presenter = packageDescriptionPresenter;
        this.packageOntologyService = packageOntologyService;

        createArtifactDetailsPopup(artifact);

        return artifactDetailsLayout;
    }

    /*
     * Creates an artifact details popup. This popup's content is a tabbed view, with a tab for general properties,
     * creator properties, and relationships.
     * @param artifact
     */
    private void createArtifactDetailsPopup(PackageArtifact artifact) {

        //The property popup will consist of the three tabs, general, creator and relationships.
        TabPane propertiesPopup = new TabPane();
        propertiesPopup.getStyleClass().add(PROPERTIES_POPUP_CLASS);

        //Create the general tab, all the properties that are not creator properties, as
        //defined by the ontology will be located here.
        Tab generalTab = new Tab();
        generalTab.setClosable(false);
        generalTab.setText(labels.get(Labels.LabelKey.PACKAGE_ARTIFACT_GENERAL));
        ScrollPane generalPane = new ScrollPane();
        generalPane.setHvalue(500);
        generalPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        generalPane.setContent(createGeneralTab(artifact));
        generalPane.setMinWidth(500);
        generalPane.setFitToWidth(true);
        generalTab.setContent(generalPane);
        
        propertiesPopup.getTabs().add(generalTab);

        //Displays all the properties that are labeled as creator properties by the
        //ontology.
        Tab creatorTab = new Tab();
        creatorTab.setClosable(false);
        ScrollPane creatorPane = new ScrollPane();
        creatorPane.setHvalue(500);
        creatorPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        creatorPane.setContent(createCreatorTab(artifact));
        creatorPane.setMinWidth(500);
        creatorPane.setFitToWidth(true);
        creatorTab.setText(labels.get(Labels.LabelKey.PACKAGE_ARTIFACT_CREATOR));
        creatorTab.setContent(creatorPane);
        
        if (creatorPane.getContent() != null) {
            propertiesPopup.getTabs().add(creatorTab);
        }
        
        //Create the relationship tab that displays all relationships the artifact has.
        Tab relationshipTab = new Tab();
        relationshipTab.setClosable(false);
        relationshipTab.setText(labels.get(Labels.LabelKey.PACKAGE_ARTIFACT_RELATIONSHIPS));
        ScrollPane relationshipPane = new ScrollPane();
        relationshipPane.setHvalue(500);
        relationshipPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        relationshipPane.setContent(createRelationshipTab(artifact));
        relationshipPane.setMinWidth(500);
        relationshipPane.setFitToWidth(true);
        relationshipTab.setContent(relationshipPane);
        propertiesPopup.getTabs().add(relationshipTab);

        //Create the inheritance tab that displays all inheritable properties that an artifact has.
        Tab inheritanceTab = new Tab();
        inheritanceTab.setClosable(false);
        inheritanceTab.setText(labels.get(Labels.LabelKey.PACKAGE_ARTIFACT_INHERITANCE));
        ScrollPane inheritancePane = new ScrollPane();
        inheritancePane.setHvalue(500);
        inheritancePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        inheritancePane.setContent(createInheritanceTab(artifact));
        inheritancePane.setMinWidth(500);
        inheritancePane.setFitToWidth(true);
        inheritanceTab.setContent(inheritancePane);
        propertiesPopup.getTabs().add(inheritanceTab);

        artifactDetailsLayout.setCenter(propertiesPopup);

        HBox popupControls = new HBox(24);
        popupControls.setAlignment(Pos.CENTER_RIGHT);

        popupControls.getStyleClass().add(VIEW_FOOTER_CLASS);
        popupControls.setPrefHeight(40);

        popupControls.getChildren().add(cancelPopupLink);

        popupControls.getChildren().add(applyPopupButton);

        artifactDetailsLayout.setBottom(popupControls);
    }

    /*
     * Creates the general properties tab, general properties are any properties that aren't defined to be creator properties,
     * by the ontology.
     * @param artifact
     * @return the VBox for the general tab
     */
    private VBox createGeneralTab(final PackageArtifact artifact) {
        final VBox propertiesBox = new VBox(12);

        propertiesBox.getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);
        Set<String> creatorProperties = packageOntologyService.getCreatorProperties(artifact);
        final Map<String, String> properties = packageOntologyService.getProperties(artifact);

        Label requiredLabel = new Label(labels.get(Labels.LabelKey.REQUIRED_FIELDS_LABEL));
        requiredLabel.setMaxWidth(400);
        requiredLabel.setWrapText(true);
        requiredLabel.setTextAlignment(TextAlignment.CENTER);

        propertiesBox.getChildren().add(requiredLabel);
        List<String> sortedProperties = new ArrayList<>();

        //Get the property name key set and then create a sorted list from it.
        sortedProperties.addAll(properties.keySet());
        sortProperties(sortedProperties, artifact, "");

        //Loop through all the available properties
        for (final String property : sortedProperties) {
            //If the property isn't a creator property we include it in this tab
            if (!creatorProperties.contains(property)) {
                final PackageDescriptionViewImpl.ArtifactPropertyContainer container = new PackageDescriptionViewImpl.ArtifactPropertyContainer();

                //If the property is complex use the group property creation.
                if (packageOntologyService.isPropertyComplex(properties.get(property))) {
                    container.isComplex = true;
                    VBox complexPropertyBox = createGroupPropertySection(artifact, property, properties.get(property), false, container);
                    propertiesBox.getChildren().add(complexPropertyBox);
                    int maxOccurrences = packageOntologyService.getPropertyMaxOccurrences(artifact, property, "");

                    //If the property allows for more than one value include a button to add more fields.
                    if (maxOccurrences > 1) {
                        final Button addNewButton = new Button(labels.get(Labels.LabelKey.ADD_NEW_BUTTON) + " " + ontologyLabels.get(property));
                        addNewButton.setMaxWidth(addNewButtonMaxWidth);
                        addNewButton.setDisable(true);
                        propertiesBox.getChildren().add(addNewButton);

                        final GroupPropertyChangeListener listener = new GroupPropertyChangeListener(addNewButton, container);

                        for (Node n : propertiesBox.getChildren()) {
                            if (n instanceof VBox) {
                                addChangeListenerToSectionFields((VBox) n, listener);
                            }
                        }

                        listener.changed(null, "n/a", "n/a");

                        addNewButton.setOnAction(arg0 -> {
                            VBox complexPropertyBox1 = createGroupPropertySection(artifact, property, properties.get(property), true, container);
                            int buttonIndex = propertiesBox.getChildren().indexOf(addNewButton);

                            propertiesBox.getChildren().add(buttonIndex, complexPropertyBox1);

                            addChangeListenerToSectionFields(complexPropertyBox1, listener);
                            addNewButton.setDisable(true);
                            requestFocusForNewGroup(complexPropertyBox1);
                        });
                        Separator groupSeparator = new Separator();
                        propertiesBox.getChildren().add(groupSeparator);
                    }

                } else {
                    //If it's a simple property use the create property box.
                    int maxOccurances = packageOntologyService.getPropertyMaxOccurrences(artifact, property, "");
                    int minOccurances = packageOntologyService.getPropertyMinOccurrences(artifact, property, "");
                    boolean systemGenerated = packageOntologyService.isSystemSuppliedProperty(artifact, property);

                    Set<StringProperty> fieldProperties = new HashSet<>();
                    if (packageOntologyService.isDisciplineProperty(artifact, property)) {
                        propertiesBox.getChildren().add(new DisciplinePropertyBox(ontologyLabels.get(property), artifact.getSimplePropertyValues(property), maxOccurances, fieldProperties, minOccurances, systemGenerated, availableDisciplines));
                    } else {
                        propertiesBox.getChildren().add(new TextPropertyBox(artifact, "", ontologyLabels.get(property), property, artifact.getSimplePropertyValues(property),
                                maxOccurances, fieldProperties, minOccurances, systemGenerated, packageOntologyService, labels, messages, applyButtonValidationListener));
                    }
                    container.values = fieldProperties;
                }

                artifactPropertyFields.put(property, container);
            }
        }
        return propertiesBox;
    }

    /**
     * Creates the tab for displaying creator properties. This tab is constructed using the {@code createPropertyBox} and {@code createGroupPropertySection} methods found below.
     * @param artifact The popup artifact
     * @return content or null if nothing for user to do
     */
    private VBox createCreatorTab(final PackageArtifact artifact) {
        final VBox propertiesBox = new VBox(12);
        propertiesBox.getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);

        Label requiredLabel = new Label(labels.get(Labels.LabelKey.REQUIRED_FIELDS_LABEL));
        requiredLabel.setMaxWidth(300);
        requiredLabel.setWrapText(true);
        requiredLabel.setTextAlignment(TextAlignment.CENTER);

        propertiesBox.getChildren().add(requiredLabel);

        final Map<String, String> properties = packageOntologyService.getProperties(artifact);

        List<String> sortedProperties = new ArrayList<>();

        //Get the creator property set and then create a sorted list from it.
        sortedProperties.addAll(packageOntologyService.getCreatorProperties(artifact));
        sortProperties(sortedProperties, artifact, "");

        //Loop through all the creator properties as defined in the ontology.
        for (final String property : sortedProperties) {
            final PackageDescriptionViewImpl.ArtifactPropertyContainer container = new PackageDescriptionViewImpl.ArtifactPropertyContainer();

            //If the property is complex use the group property creation, otherwise use the simple property set up.
            if (packageOntologyService.isPropertyComplex(properties.get(property))) {
                container.isComplex = true;
                VBox complexPropertyBox = createGroupPropertySection(artifact, property, properties.get(property), false, container);
                propertiesBox.getChildren().add(complexPropertyBox);
                int maxOccurances = packageOntologyService.getPropertyMaxOccurrences(artifact, property, "");

                //If the ontology allows for more than one of the property add a button which will add more groups when pressed.
                if (maxOccurances > 1) {
                    final Button addNewButton = new Button(labels.get(Labels.LabelKey.ADD_NEW_BUTTON) + " " + property);
                    addNewButton.setMaxWidth(addNewButtonMaxWidth);
                    propertiesBox.getChildren().add(addNewButton);
                    addNewButton.setDisable(true);

                    final GroupPropertyChangeListener listener = new GroupPropertyChangeListener(addNewButton, container);

                    for (Node n : propertiesBox.getChildren()) {
                        if (n instanceof VBox) {
                            addChangeListenerToSectionFields((VBox) n, listener);
                        }
                    }

                    listener.changed(null, "n/a", "n/a");

                    addNewButton.setOnAction(arg0 -> {
                        VBox complexPropertyBox1 = createGroupPropertySection(artifact, property, properties.get(property), true, container);
                        int buttonIndex = propertiesBox.getChildren().indexOf(addNewButton);

                        propertiesBox.getChildren().add(buttonIndex, complexPropertyBox1);

                        addChangeListenerToSectionFields(complexPropertyBox1, listener);
                        addNewButton.setDisable(true);
                        requestFocusForNewGroup(complexPropertyBox1);
                    });
                }
            } else {
                //Otherwise create just the simple property
                int maxOccurances = packageOntologyService.getPropertyMaxOccurrences(artifact, property, "");
                int minOccurances = packageOntologyService.getPropertyMinOccurrences(artifact, property, "");
                boolean systemGenerated = packageOntologyService.isSystemSuppliedProperty(artifact, property);

                Set<StringProperty> fields = new HashSet<>();

                propertiesBox.getChildren().add(new TextPropertyBox(artifact, "", ontologyLabels.get(property), property, artifact.getSimplePropertyValues(property),
                        maxOccurances, fields, minOccurances, systemGenerated, packageOntologyService, labels, messages, applyButtonValidationListener));
                container.values = fields;
            }

            artifactPropertyFields.put(property, container);
        }
        
        // Return null if nothing to edit.
        if (propertiesBox.getChildren().size() == 1) {
            return null;
        }
        
        return propertiesBox;
    }

    /**
     * Handles the creation of group properties, group properties are properties that are linked together in some manner.
     * Group properties are constructed using the {@code createPropertyBox) method found below.
     *
     * @param artifact
     * @param propertyName
     * @param propertyType
     * @param empty
     * @param container
     * @return  the VBox
     */
    private VBox createGroupPropertySection(PackageArtifact artifact, String propertyName, String propertyType, boolean empty, PackageDescriptionViewImpl.ArtifactPropertyContainer container) {
        VBox complexPropertyBox = new VBox(8);
        Separator separator = new Separator();
        complexPropertyBox.getChildren().add(separator);


        //If the artifact has the property and we're not adding an empty field add the sub property values
        if (artifact.getPropertyNames().contains(propertyName) && !empty) {
            for (PackageArtifact.PropertyValueGroup group : artifact.getPropertyValueGroups(propertyName)) {
                Map<String, Set<StringProperty>> subPropertyFields = new HashMap<>();

                Label propertyNameLabel = new Label(ontologyLabels.get(propertyName));
                propertyNameLabel.setPrefWidth(100);
                propertyNameLabel.setWrapText(true);
                complexPropertyBox.getChildren().add(propertyNameLabel);

                List<String> sortedProperties = new ArrayList<>();

                //Get the creator property set and then create a sorted list from it.
                sortedProperties.addAll(packageOntologyService.getGroupPropertyNames(propertyType));
                sortProperties(sortedProperties, artifact, propertyType);

                for (String fieldName : sortedProperties) {
                    Set<String> values = group.getSubPropertyValues(fieldName);
                    int maxOccurs = packageOntologyService.getPropertyMaxOccurrences(artifact, fieldName, propertyType);
                    int minOccurs = packageOntologyService.getPropertyMinOccurrences(artifact, fieldName, propertyType);
                    boolean systemGenerated = packageOntologyService.isSystemSuppliedProperty(artifact, fieldName);
                    Set<StringProperty> fields = new HashSet<>();
                    complexPropertyBox.getChildren().add(new TextPropertyBox(artifact, propertyName, ontologyLabels.get(fieldName), fieldName, values,
                            maxOccurs, fields, minOccurs, systemGenerated, packageOntologyService, labels, messages, applyButtonValidationListener));
                    subPropertyFields.put(fieldName, fields);

                }
                container.subProperties.add(subPropertyFields);
            }
            //Otherwise just add the empty text fields for the possible property values.
        } else {
            Map<String, Set<StringProperty>> subPropertyFields = new HashMap<>();

            Label propertyNameLabel = new Label(ontologyLabels.get(propertyName));
            propertyNameLabel.setPrefWidth(100);
            propertyNameLabel.setWrapText(true);
            complexPropertyBox.getChildren().add(propertyNameLabel);

            List<String> sortedProperties = new ArrayList<>();

            //Get the creator property set and then create a sorted list from it.
            sortedProperties.addAll(packageOntologyService.getGroupPropertyNames(propertyType));
            sortProperties(sortedProperties, artifact, propertyType);

            //For each field create a property box
            for (String fieldName : sortedProperties) {
                //String fieldType = packageOntologyService.getComplexPropertySubPropertyType(propertyType, fieldName);
                int maxOccurs = packageOntologyService.getPropertyMaxOccurrences(artifact, fieldName, propertyType);
                int minOccurs = packageOntologyService.getPropertyMinOccurrences(artifact, fieldName, propertyType);
                boolean systemGenerated = packageOntologyService.isSystemSuppliedProperty(artifact, fieldName);
                Set<StringProperty> fields = new HashSet<>();
                complexPropertyBox.getChildren().add(new TextPropertyBox(artifact, propertyName, ontologyLabels.get(fieldName), fieldName,
                        null, maxOccurs, fields, minOccurs, systemGenerated, packageOntologyService, labels, messages, applyButtonValidationListener));

                subPropertyFields.put(fieldName, fields);
            }

            container.subProperties.add(subPropertyFields);
        }
        return complexPropertyBox;
    }

    /**
     * Adds a listener to all TextFields or TextInputControl within the specified pane.  Will dive into sub-panes as needed.
     *
     * @param group    The pane to add the listener to
     * @param listener The listener to attach to the TextInputControl
     */
    private void addChangeListenerToSectionFields(Pane group, ChangeListener<? super String> listener) {
        for (Node n : group.getChildren()) {
            if (n instanceof Pane) {
                addChangeListenerToSectionFields((Pane) n, listener);
            } else if (n instanceof TextInputControl) {
                TextInputControl text = (TextInputControl) n;
                text.textProperty().addListener(listener);
            }
        }
    }


    /**
     * Requests focus for the first TextInputControl within a pane.  Will dig into sub-panes as needed
     *
     * @param pane The pane to look for a TextInputControl in.
     * @return True if it successfully found a node to request focus for, false if not.  This is mostly
     * needed internally so it knows when to stop the recursion.
     */
    private boolean requestFocusForNewGroup(Pane pane) {
        for (Node n : pane.getChildren()) {
            if (n instanceof TextInputControl) {
                n.requestFocus();
                return true;
            } else if (n instanceof Pane) {
                Pane p = (Pane) n;
                if (requestFocusForNewGroup(p)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates the inheritance tab in the popup.
     * @param artifact The popup artifact
     * @return inheritance tab or null if nothing to do
     */
    public VBox createInheritanceTab(final PackageArtifact artifact) {

        final VBox inheritanceBox = new VBox(12);
        inheritanceBox.getStyleClass().add(PACKAGE_TOOL_POPUP_PROPERTY_TAB);
        boolean hasInheritableProperties = false;

        //create label to explain what this tab is about.
        Label inheritanceTabIntroLabel = new Label(labels.get(Labels.LabelKey.INHERITANCE_TAB_INTRO));
        inheritanceTabIntroLabel.setPrefWidth(450);
        inheritanceTabIntroLabel.setWrapText(true);
        inheritanceBox.getChildren().add(inheritanceTabIntroLabel);

        //create label to explain usage of buttons.
        Label inheritanceButtonExplainedLabel = new Label(labels.get(Labels.LabelKey.INHERITANCE_BUTTON_EXPLAINED));
        inheritanceButtonExplainedLabel.setPrefWidth(450);
        inheritanceButtonExplainedLabel.setWrapText(true);
        inheritanceBox.getChildren().add(inheritanceButtonExplainedLabel);

        Separator groupSeparator = new Separator();
        inheritanceBox.getChildren().add(groupSeparator);

        //Loop through properties for the given artifact.
        for (String propertyName : packageOntologyService.getProperties(artifact).keySet()) {
            //If the property is inheritable, create a button which would allow the values to be apply to children
            //appropriately
            if (packageOntologyService.isInheritableProperty(artifact, propertyName)) {
                inheritanceBox.getChildren().add(createInheritanceBox(artifact.getType(), propertyName));

                groupSeparator = new Separator();
                inheritanceBox.getChildren().add(groupSeparator);
                hasInheritableProperties = true;
            }
        }

        if (!hasInheritableProperties) {
            Label noInheritablePropertyLabel = new Label(labels.get(Labels.LabelKey.NO_INHERITABLE_PROPERTY));
            inheritanceTabIntroLabel.setPrefWidth(450);
            inheritanceTabIntroLabel.setWrapText(true);
            inheritanceBox.getChildren().add(noInheritablePropertyLabel);
        }

        return inheritanceBox;
    }

    private HBox createInheritanceBox(String artifactType, String propertyName) {
        HBox propertyBox = new HBox(30);

        VBox propNameAndExplation = new VBox();
        final Label propertyNameLabel = new Label(ontologyLabels.get(propertyName));
        propertyNameLabel.setPrefWidth(400);
        propertyNameLabel.getStyleClass().add(CssConstants.BOLD_TEXT_CLASS);
        //propertyNameLabel.setStyle(CssConstants.BOLD_TEXT_CLASS);

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


        propNameAndExplation.getChildren().add(propertyNameLabel);
        propNameAndExplation.getChildren().add(descendantTypesLabel);

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
    private VBox createRelationshipTab(final PackageArtifact artifact) {
        final VBox relationshipsBox = new VBox(38);
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
            PackageDescriptionViewImpl.ArtifactRelationshipContainer container = new PackageDescriptionViewImpl.ArtifactRelationshipContainer();
            artifactRelationshipFields.add(container);
            relationshipsBox.getChildren().add(new RelationshipSelectionBox(artifact, null, container, availableRelationshipGroups, labels, packageOntologyService, addNewRelationshipListener));
            addNewRelationshipButton.setDisable(true);
        } else {
            //Otherwise loop through the relationships and create a box for each one.
            for (PackageRelationship relationship : artifact.getRelationships()) {
                PackageDescriptionViewImpl.ArtifactRelationshipContainer container = new PackageDescriptionViewImpl.ArtifactRelationshipContainer();
                artifactRelationshipFields.add(container);
                relationshipsBox.getChildren().add(new RelationshipSelectionBox(artifact, relationship, container, availableRelationshipGroups, labels, packageOntologyService, addNewRelationshipListener));
                if (relationship.getTargets() == null || relationship.getTargets().isEmpty()) {
                    addNewRelationshipButton.setDisable(true);
                }
            }
        }

        relationshipsBox.getChildren().add(addNewRelationshipButton);

        addNewRelationshipButton.setOnAction(arg0 -> {
            PackageDescriptionViewImpl.ArtifactRelationshipContainer container = new PackageDescriptionViewImpl.ArtifactRelationshipContainer();
            artifactRelationshipFields.add(container);
            VBox newRelationshipBox = new RelationshipSelectionBox(artifact, null, container, availableRelationshipGroups, labels, packageOntologyService, addNewRelationshipListener);
            int buttonIndex = relationshipsBox.getChildren().indexOf(addNewRelationshipButton);

            relationshipsBox.getChildren().add(buttonIndex, newRelationshipBox);

            addNewRelationshipButton.setDisable(true);
            requestFocusForNewGroup(newRelationshipBox);
        });
        return relationshipsBox;
    }

    /**
     * Class to capture changes to Group Properties (ie, any change to a field within the group), so as
     * to enable/disable the "Add New" button as appropriate
     */
    private class GroupPropertyChangeListener implements ChangeListener<String> {
        private Button propertyAddButton;
        private PackageDescriptionViewImpl.ArtifactPropertyContainer container;

        public GroupPropertyChangeListener(Button propertyAddButton, PackageDescriptionViewImpl.ArtifactPropertyContainer container) {
            this.propertyAddButton = propertyAddButton;
            this.container = container;
        }

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            propertyAddButton.setDisable(anyGroupsEmpty());
        }

        /**
         * Determines if any of the groups are empty
         *
         * @return True if there is at least one group that has no values, false if every group has
         * at least one value in it.
         */
        private boolean anyGroupsEmpty() {
            for (Map<String, Set<StringProperty>> group : container.getSubProperties()) {
                if (groupEmpty(group)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Determines if a group is empty
         *
         * @param group The group to check
         * @return true if the group has no values, false if there is at least one value in the group
         */
        private boolean groupEmpty(Map<String, Set<StringProperty>> group) {
            for (Map.Entry<String, Set<StringProperty>> entry : group.entrySet()) {
                for (StringProperty property : entry.getValue()) {
                    if (property != null && property.getValue() != null && !property.getValue().isEmpty()) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    //Sorts properties in the order of single value required, multi value required, optional single value, optional multi value
    private void sortProperties(List<String> propertyNames, final PackageArtifact artifact, final String type) {
        Collections.sort(propertyNames, (propertyOne, propertyTwo) -> {

            int propertyOneMaxOccurs = packageOntologyService.getPropertyMaxOccurrences(artifact, propertyOne, type);
            int propertyOneMinOccurs = packageOntologyService.getPropertyMinOccurrences(artifact, propertyOne, type);

            int propertyTwoMaxOccurs = packageOntologyService.getPropertyMaxOccurrences(artifact, propertyTwo, type);
            int propertyTwoMinOccurs = packageOntologyService.getPropertyMinOccurrences(artifact, propertyTwo, type);

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
}
