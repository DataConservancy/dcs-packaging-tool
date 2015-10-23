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
package org.dataconservancy.packaging.gui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.model.Relationship;
import org.dataconservancy.packaging.gui.model.RelationshipGroup;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageRelationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Creates a VBox that contains the controls for adding a relationship to an object.
 * The main controls are a namespace combobox, a relationship combobox that allows selecting or entering a relationship.
 * A checkbox that denotes if the target is required to be a URI. And text boxes for the relationship targets. All fields are linked with listeners on their properties.
 * Of note relationship and target must be filled in to add a new relationship. Any relationship that is known or provided from a dropdown, controls the uri checkbox and disables it.
 */
public class RelationshipSelectionBox extends VBox implements CssConstants {

    private Labels labels;
    private List<RelationshipGroup> availableGroups;
    private RelationshipGroupCellFactory relationshipGroupCellFactory;
    private RelationshipCellFactory relationshipCellFactory;
    private PackageOntologyService ontologyService;
    private BooleanProperty editable;
    private BooleanProperty requiresUri;
    private BooleanProperty requiresDisabled;
    private StringProperty targetType;

    public RelationshipSelectionBox(final PackageArtifact artifact, PackageRelationship packageRelationship,
                                    PackageDescriptionViewImpl.ArtifactRelationshipContainer container, List<RelationshipGroup> availableGroups,
                                    Labels labels, PackageOntologyService ontologyService, EmptyFieldButtonDisableListener addNewRelationshipListener) {
        this.labels = labels;
        this.availableGroups = availableGroups;
        relationshipGroupCellFactory = new RelationshipGroupCellFactory();
        relationshipCellFactory = new RelationshipCellFactory();

        this.ontologyService = ontologyService;

        if (packageRelationship != null) {
            editable = new SimpleBooleanProperty(!ontologyService.isRelationshipHierarchical(artifact, packageRelationship.getName()));
        } else {
            editable = new SimpleBooleanProperty(true);
        }

        RelationshipGroup startingGroup = null;
        Relationship startingRelationship = null;
        targetType = new SimpleStringProperty(labels.get(Labels.LabelKey.URI_LABEL));

        if (packageRelationship != null) {
            if (packageRelationship.getName() != null && !packageRelationship.getName().isEmpty()) {
                startingRelationship = getRelationshipForURI(packageRelationship.getName());
                if (startingRelationship != null) {
                    startingGroup = findGroupForRelationship(startingRelationship);
                } else {
                    startingRelationship = new Relationship(packageRelationship.getName(), "", packageRelationship.getName(), packageRelationship.requiresUriTargets());
                }
            }
        }

        Set<String> relationshipTargets = null;
        if (packageRelationship != null) {
            relationshipTargets = packageRelationship.getTargets();
        }
        createRelationshipBox(artifact, startingGroup, startingRelationship, relationshipTargets, container, addNewRelationshipListener);
    }

    private void createRelationshipBox(final PackageArtifact artifact, RelationshipGroup startingGroup, Relationship startingRelationship, Set<String> relatedItems,
                                       final PackageDescriptionViewImpl.ArtifactRelationshipContainer container, final EmptyFieldButtonDisableListener addNewRelationshipListener) {

        setSpacing(8);

        Separator groupSeparator = new Separator();
        getChildren().add(groupSeparator);

        Label relationshipDefintionLabel = new Label(labels.get(Labels.LabelKey.RELATIONSHIP_DEFINITION_LABEL));
        getChildren().add(relationshipDefintionLabel);

        //Create a box for the namespace selection elements
        final HBox namespaceBox = new HBox(15);
        Label schemaLabel = new Label(labels.get(Labels.LabelKey.NAMESPACE_LABEL));
        schemaLabel.setMinWidth(100);
        schemaLabel.setWrapText(true);
        namespaceBox.getChildren().add(schemaLabel);

        //The combobox that allows for selecting a namespace.
        final ComboBox<RelationshipGroup> namespaceComboBox = (ComboBox) ControlFactory.createControl(ControlType.COMBO_BOX, null, null);
        //namespaceComboBox.setPrefWidth(800);
        namespaceComboBox.setCellFactory(relationshipGroupCellFactory);
        namespaceComboBox.setDisable(!editable.getValue());

        //Converts the selected relationship group to a string to display in the combobox.
        namespaceComboBox.setConverter(new StringConverter<RelationshipGroup>() {
            @Override
            public String toString(RelationshipGroup relationshipGroup) {
                if (relationshipGroup != null) {
                    return relationshipGroup.getLabel();
                }
                return "";
            }

            @Override
            public RelationshipGroup fromString(String s) {
                // Default method body
                return null;
            }
        });

        //Add all available groups from the relationship file to the combobox list.
        if (availableGroups != null) {
            namespaceComboBox.getItems().addAll(availableGroups);
        }

        //If we haven't detected a starting group either show the next group(if we have no starting relationship, or blank if the starting relationship isn't in a group.
        if (startingGroup == null) {
            if (availableGroups != null && !availableGroups.isEmpty() && startingRelationship == null) {
                startingGroup = availableGroups.iterator().next();
            } else {
                startingGroup = new RelationshipGroup("", "", "", new ArrayList<>());
            }
        }

        namespaceComboBox.setValue(startingGroup);

        namespaceBox.getChildren().add(namespaceComboBox);
        getChildren().add(namespaceBox);

        //Create a box for the actual namespace definition.
        HBox relationshipDefinitionBox = new HBox(15);
        Label relationshipLabel = new Label(labels.get(Labels.LabelKey.RELATIONSHIP_LABEL));
        relationshipLabel.setMinWidth(100);
        relationshipLabel.setWrapText(true);
        relationshipDefinitionBox.getChildren().add(relationshipLabel);

        //Create a combobox to select or enter the relationship uri or name, this box selection is controlled by the namespace selection.
        final ComboBox<Relationship> relationshipComboBox = (ComboBox) ControlFactory.createControl(ControlType.EDITABLE_COMBO_BOX, null, null);
        relationshipComboBox.setDisable(!editable.getValue());
        relationshipComboBox.setCellFactory(relationshipCellFactory);
        relationshipComboBox.getEditor().textProperty().addListener(addNewRelationshipListener);
        if (startingRelationship == null) {
            addNewRelationshipListener.fieldAdded();
        }

        /*
         * Creates a converter that converts the combobox string into a relationship object. It's important to note this differs from the
         * cell factory that provides the dropdown list which uses the relationship label, this uses the relationship uri.
         */
        relationshipComboBox.setConverter(new StringConverter<Relationship>() {
            @Override
            public String toString(Relationship relationship) {
                if (relationship != null) {
                    return relationship.getRelationshipUri();
                }
                return "";
            }

            @Override
            public Relationship fromString(String s) {
                Relationship relationship = findRelationshipInGroup(s, namespaceComboBox.getValue());
                if (relationship == null) {
                    relationship = new Relationship(s, "", s, false);
                }

                return relationship;
            }
        });

        //Either set the starting value to the passed in relationship or the first relationship in the starting group.
        if (startingRelationship != null) {
            relationshipComboBox.setValue(startingRelationship);
        } else if (startingGroup != null) {
            if (startingGroup.getRelationships() != null && !startingGroup.getRelationships().isEmpty()) {
                relationshipComboBox.getItems().addAll(startingGroup.getRelationships());

                startingRelationship = startingGroup.getRelationships().iterator().next();

                relationshipComboBox.setValue(startingRelationship);
            }
        }

        //Error label that lets the user know they either need to specify a URI or one of our known relationship types. Hopefully at some point known relationship types will go away.
        final Label requiresURILabel = new Label(labels.get(Labels.LabelKey.RELATIONSHIP_MUST_BE_URI_OR_KNOWN));
        requiresURILabel.setTextFill(Color.RED);
        requiresURILabel.setVisible(false);

        //Listens for changes to the relationship value and adjusts other fields accordingly.
        relationshipComboBox.valueProperty().addListener((observableValue, relationship, newRelationship) -> {
            if (newRelationship != null) {
                if (RDFURIValidator.isValid(newRelationship.getRelationshipUri()) || ontologyService.getKnownRelationshipNames().contains(newRelationship.getLabel())
                        || ontologyService.getKnownRelationshipNames().contains(newRelationship.getRelationshipUri())) {
                    requiresURILabel.setVisible(false);
                    editable.setValue(!ontologyService.isRelationshipHierarchical(artifact, newRelationship.getLabel()));

                    requiresUri.setValue(newRelationship.requiresUri());
                    //If the current relationship group contains the relationship that means it was selected from the list.
                    if (namespaceComboBox.getValue().getRelationships().contains(newRelationship)) {
                        requiresDisabled.setValue(true);
                        requiresUri.setValue(newRelationship.requiresUri());
                    } else {
                        //If it's a known relationship disable checkbox and set it to true
                        if (ontologyService.getKnownRelationshipNames().contains(newRelationship.getLabel())
                                || ontologyService.getKnownRelationshipNames().contains(newRelationship.getRelationshipUri())) {
                            requiresDisabled.setValue(true);
                            requiresUri.setValue(true);

                        } else { //Otherwise the user typed it in so they can change it.
                            requiresDisabled.setValue(false);
                        }

                        //Set the group to empty since it wasn't selected from a group.
                        namespaceComboBox.setValue(new RelationshipGroup("", "", "", new ArrayList<>()));

                    }
                } else {
                    //If the relationship entered isn't valid or a known type, disable all other fields and display a warning.
                    requiresURILabel.setVisible(true);
                    editable.setValue(false);
                    requiresDisabled.setValue(true);
                }
            }
        });

        //Link the combo box to the container
        container.relationship = relationshipComboBox.valueProperty();

        relationshipDefinitionBox.getChildren().add(relationshipComboBox);
        getChildren().add(relationshipDefinitionBox);

        HBox errorBox = new HBox();
        errorBox.setAlignment(Pos.CENTER);
        errorBox.getChildren().add(requiresURILabel);
        getChildren().add(errorBox);

        //Set up the listener that will switch the values in the relationship box when the schema box changes.
        namespaceComboBox.valueProperty().addListener((observableValue, oldValue, newGroup) -> {
            if (newGroup != null) {

                if (newGroup.getRelationships() != null && !newGroup.getRelationships().isEmpty()) {
                    relationshipComboBox.getItems().clear();
                    //TODO: This show/hide is a hack to work around a javafx2 bug where the combo box list isn't correctly measured on the first pass. If we upgrade to java 8 this should be removed.
                    relationshipComboBox.show();
                    relationshipComboBox.getItems().addAll(newGroup.getRelationships());
                    relationshipComboBox.hide();
                    relationshipComboBox.setValue(newGroup.getRelationships().iterator().next());

                } else {
                    //The user typed in relationship so remove all the other relationships from the combo box.
                    Relationship currentRelationship = relationshipComboBox.getValue();
                    ListIterator<Relationship> iter = relationshipComboBox.getItems().listIterator();
                    while(iter.hasNext()){
                        if(iter.next().equals(currentRelationship)){
                            iter.remove();
                        }
                    }
                }
            }
        });

        //Checkbox that denotes whether or not the target needs to be a uri or a literal string is allowed.
        //Built in relationships define this value, and so the checkbox will be disabled for them. Entered fields can specify this value.
        //This controls whether we do URI checking in the presenter so it's important this field is correct.
        CheckBox requiresURICheckBox = new CheckBox(labels.get(Labels.LabelKey.REQURIRES_URI_LABEL));

        boolean startingRequiresURI = false;
        boolean startingRequiresDisabled = false;

        if (startingRelationship != null) {
            startingRequiresURI = startingRelationship.requiresUri();
            //Were starting with a relationship from a group.
            if (!startingGroup.getLabel().isEmpty()) {
                startingRequiresDisabled = true;
            } else if (ontologyService.getKnownRelationshipNames().contains(startingRelationship.getLabel())
                    || ontologyService.getKnownRelationshipNames().contains(startingRelationship.getRelationshipUri())) {
                startingRequiresDisabled = true;
            }
        }
        requiresUri = new SimpleBooleanProperty(startingRequiresURI);
        requiresDisabled = new SimpleBooleanProperty(startingRequiresDisabled);

        requiresURICheckBox.disableProperty().bind(requiresDisabled);
        requiresURICheckBox.selectedProperty().bindBidirectional(requiresUri);
        container.requiresURI = requiresURICheckBox.selectedProperty();
        getChildren().add(requiresURICheckBox);

        final VBox relatedItemsBox = new VBox(3);
        relatedItemsBox.setPrefWidth(ControlFactory.textPrefWidth);

        Label relatedToLabel = new Label(labels.get(Labels.LabelKey.RELATIONSHIP_TARGET_LABEL));
        relatedItemsBox.getChildren().add(relatedToLabel);
        boolean empty = true;

        //create a HBox to hold text field and validating result image label
        final HBox singleItemInputBox = new HBox(15);
        targetType.bind(Bindings.when(requiresUri).then(labels.get(Labels.LabelKey.URI_LABEL)).otherwise(labels.get(Labels.LabelKey.LITERAL_LABEL)));

        Label typeLabel = new Label();
        typeLabel.textProperty().bind(targetType);
        typeLabel.setMaxWidth(100);
        typeLabel.setMinWidth(100);
        singleItemInputBox.getChildren().add(typeLabel);

        //Create a button that allows for adding a new relationship target field box.
        final Button addNewButton = new Button("+");
        final EmptyFieldButtonDisableListener targetChangeListener = new EmptyFieldButtonDisableListener(addNewButton);

        //If there aren't any related items already just create an empty box to specify one.
        if (relatedItems == null || relatedItems.isEmpty()) {
            final Label userInputImageLabel = new Label();
            userInputImageLabel.setPrefWidth(18);
            TextField relatedItem = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null, null);
            //Add listener which displays a blank, a green check or a red cross, according to the validity of user's input
            relatedItem.textProperty().addListener(getNewChangeListenerForRelatedItem(userInputImageLabel));;
            relatedItem.setMinWidth(260);

            relatedItem.textProperty().addListener(targetChangeListener);
            relatedItem.textProperty().addListener(addNewRelationshipListener);
            targetChangeListener.fieldAdded();
            addNewRelationshipListener.fieldAdded();

            relatedItem.editableProperty().bind(editable);

            singleItemInputBox.getChildren().add(relatedItem);
            singleItemInputBox.getChildren().add(userInputImageLabel);
            container.relationshipTargets.add(relatedItem.textProperty());
            relatedItemsBox.getChildren().add(singleItemInputBox);
        } else {
            empty = false;
            //Otherwise loop through the relationship targets and create a text field for each one.

            for (String relatedItem : relatedItems) {
                final Label userInputImageLabel = new Label();
                userInputImageLabel.setPrefWidth(18);

                TextField relatedItemTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, relatedItem, null);
                //Add listener which displays a blank, a green check or a red cross, according to the validity of user's input
                relatedItemTextField.textProperty().addListener(getNewChangeListenerForRelatedItem(userInputImageLabel));
                relatedItemTextField.textProperty().addListener(targetChangeListener);
                relatedItemTextField.textProperty().addListener(addNewRelationshipListener);

                relatedItemTextField.editableProperty().bind(editable);
                singleItemInputBox.getChildren().add(relatedItemTextField);
                singleItemInputBox.getChildren().add(userInputImageLabel);

                container.relationshipTargets.add(relatedItemTextField.textProperty());
                if (relatedItemTextField.getText().isEmpty()) {
                    empty = true;
                }
                relatedItemsBox.getChildren().add(singleItemInputBox);
            }
        }

        getChildren().add(relatedItemsBox);

        if (editable.getValue()) {
            singleItemInputBox.getChildren().add(addNewButton);
        }
        addNewButton.setDisable(empty);

        addNewButton.setOnAction(arg0 -> {

            final HBox singleItemInputBox1 = new HBox(15);

            Label typeLabel1 = new Label();
            typeLabel1.textProperty().bind(targetType);
            typeLabel1.setMaxWidth(100);
            typeLabel1.setMinWidth(100);
            singleItemInputBox1.getChildren().add(typeLabel1);

            final Label userInputImageLabel = new Label();

            TextField relatedItem = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null, null);
            //Add listener which displays a blank, a green check or a red cross, according to the validity of user's input
            relatedItem.textProperty().addListener(getNewChangeListenerForRelatedItem(userInputImageLabel));
            relatedItem.textProperty().addListener(targetChangeListener);
            targetChangeListener.fieldAdded();
            relatedItem.textProperty().addListener(addNewRelationshipListener);
            addNewRelationshipListener.fieldAdded();

            relatedItem.setMinWidth(300);
            container.relationshipTargets.add(relatedItem.textProperty());

            singleItemInputBox1.getChildren().add(relatedItem);
            singleItemInputBox1.getChildren().add(userInputImageLabel);

            relatedItemsBox.getChildren().add(singleItemInputBox1);
            addNewButton.setDisable(true);
            relatedItem.requestFocus();
        });
    }

    /*
     * Cell factor for the Namespace selection box, basically we just display the label of the the relationship group, in the drop down list.
     */
    private class RelationshipGroupCellFactory implements Callback<ListView<RelationshipGroup>, ListCell<RelationshipGroup>> {

        @Override
        public ListCell<RelationshipGroup> call(ListView<RelationshipGroup> relationshipGroupListView) {
            return new ListCell<RelationshipGroup>() {
                @Override protected void updateItem(RelationshipGroup item, boolean empty) {
                     super.updateItem(item, empty);

                     if (item == null || empty) {
                         setText("");
                     } else {
                         setText(item.getLabel());
                     }
                }
            };
        }
    }

    /*
     * Cell factor for the Relationship box, we show the label for the relationship, important to note this is different from the converter,
      * which shows the relationship uri in the box when it's selected.
     */
    private class RelationshipCellFactory implements Callback<ListView<Relationship>, ListCell<Relationship>> {

        @Override
        public ListCell<Relationship> call(ListView<Relationship> relationshipListView) {
            return new ListCell<Relationship>() {
                @Override protected void updateItem(Relationship item, boolean empty) {
                     super.updateItem(item, empty);

                     if (item == null || empty) {
                         setText("");
                     } else {
                         setText(item.getLabel());
                     }
                }
            };
        }
    }

    //Helper function that finds the group for a passed in relationship.
    private RelationshipGroup findGroupForRelationship(Relationship relationship) {
        RelationshipGroup relationshipGroup = null;

        if (availableGroups != null) {
            Iterator<RelationshipGroup> groupIterator = availableGroups.iterator();
            while (relationshipGroup == null && groupIterator.hasNext()) {
                RelationshipGroup group = groupIterator.next();
                for (Relationship possibleRelationship : group.getRelationships()) {
                    if (relationship == possibleRelationship) {
                        relationshipGroup = group;
                        break;
                    }
                }
            }
        }

        return relationshipGroup;
    }

    /*
     * Helper function that returns the relationship object for a given uri. This is used to keep the relationship combobox back by a Relationship Object,
     * while allowing the combobox to be editable.
     */
    private Relationship getRelationshipForURI(String relationshipURI) {
        Relationship relationship = null;
        if (availableGroups != null) {
            Iterator<RelationshipGroup> groupIterator = availableGroups.iterator();
            while (relationship == null && groupIterator.hasNext()) {
                RelationshipGroup group = groupIterator.next();
                for (Relationship possibleRelationship : group.getRelationships()) {
                    if (possibleRelationship.getRelationshipUri().equalsIgnoreCase(relationshipURI)) {
                        relationship = possibleRelationship;
                        break;
                    }
                }
            }
        }

        return relationship;
    }

    //Function to check to see if the relationship is from the selected group, this is used to determine if the user has typed in a relationship.
    private Relationship findRelationshipInGroup(String relationshipURI, RelationshipGroup group) {
        Relationship relationship = null;
        if (group != null) {
            for (Relationship possibleRelationship : group.getRelationships()) {
                if(possibleRelationship.getRelationshipUri().equalsIgnoreCase(relationshipURI)) {
                    relationship = possibleRelationship;
                    break;
                }
            }
        }

        return relationship;
    }

    private ChangeListener<String> getNewChangeListenerForRelatedItem(final Label errorMessageLabel) {
        //Add a listener for text entry in the definition box. If the user enters an definition that is hierarchical an error message will be displayed.
        return (observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty() || !requiresUri.getValue()) {
                setLabelImage(errorMessageLabel, null);
            } else if (RDFURIValidator.isValid(newValue)) {
                setLabelImage(errorMessageLabel, GOOD_INPUT_IMAGE);
            } else {
                setLabelImage(errorMessageLabel, BAD_INPUT_IMAGE);
            }
        };
    }

    /*
     * Set image on the provided label, using a String imageKey
     * @param label
     * @param imageKey
     */
    private void setLabelImage(Label label, String imageKey) {
        if (imageKey != null) {
            ImageView image = new ImageView();
            image.getStyleClass().add(imageKey);
            label.setGraphic(image);
        } else {
            label.setGraphic(null);
        }
    }

}
