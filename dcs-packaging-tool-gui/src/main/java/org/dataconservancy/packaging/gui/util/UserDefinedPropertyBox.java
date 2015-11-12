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
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.model.UserDefinedPropertyGroup;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Creates a VBox that contains the controls for adding a relationship to an object.
 * The main controls are a namespace combobox, a relationship combobox that allows selecting or entering a relationship.
 * A checkbox that denotes if the target is required to be a URI. And text boxes for the relationship targets. All fields are linked with listeners on their properties.
 * Of note relationship and target must be filled in to add a new relationship. Any relationship that is known or provided from a dropdown, controls the uri checkbox and disables it.
 */
public class UserDefinedPropertyBox extends VBox implements CssConstants {

    private List<UserDefinedPropertyGroup> availableGroups;
    private RelationshipGroupCellFactory relationshipGroupCellFactory;
    private RelationshipCellFactory relationshipCellFactory;
    private BooleanProperty requiresUri;
    private BooleanProperty requiresDisabled;
    private StringProperty targetType;

    private PropertyType userDefinedPropertyType;
    private List<TextPropertyBox> userDefinedPropertyObjects;

    public UserDefinedPropertyBox(PropertyType userDefinedPropertyType, List<String> userDefinedPropertyValues, List<UserDefinedPropertyGroup> availableGroups,
                                  EmptyFieldButtonDisableListener addNewRelationshipListener) {
        this.availableGroups = availableGroups;
        relationshipGroupCellFactory = new RelationshipGroupCellFactory();
        relationshipCellFactory = new RelationshipCellFactory();
        this.userDefinedPropertyType = userDefinedPropertyType;
        userDefinedPropertyObjects = new ArrayList<>();

        UserDefinedPropertyGroup startingGroup = null;
        targetType = new SimpleStringProperty(TextFactory.getText(Labels.LabelKey.URI_LABEL));

        if (userDefinedPropertyType != null) {
            startingGroup = findGroupForPropertyType(userDefinedPropertyType);
        }

        createdUserDefinedPropertyBox(startingGroup, userDefinedPropertyType, userDefinedPropertyValues, addNewRelationshipListener);
    }

    @SuppressWarnings("unchecked")
    private void createdUserDefinedPropertyBox(UserDefinedPropertyGroup startingGroup, PropertyType startingPropertyType, List<String> values,
                                               final EmptyFieldButtonDisableListener addNewRelationshipListener) {

        setSpacing(8);

        Separator groupSeparator = new Separator();
        getChildren().add(groupSeparator);

        Label relationshipDefintionLabel = new Label(TextFactory.getText(Labels.LabelKey.RELATIONSHIP_DEFINITION_LABEL));
        getChildren().add(relationshipDefintionLabel);

        //Create a box for the namespace selection elements
        final HBox namespaceBox = new HBox(15);
        Label schemaLabel = new Label(TextFactory.getText(Labels.LabelKey.NAMESPACE_LABEL));
        schemaLabel.setMinWidth(100);
        schemaLabel.setWrapText(true);
        namespaceBox.getChildren().add(schemaLabel);

        //The combobox that allows for selecting a namespace.
        final ComboBox<UserDefinedPropertyGroup> namespaceComboBox = (ComboBox) ControlFactory.createControl(ControlType.COMBO_BOX, null, null);
        //namespaceComboBox.setPrefWidth(800);
        namespaceComboBox.setCellFactory(relationshipGroupCellFactory);

        //Converts the selected relationship group to a string to display in the combobox.
        namespaceComboBox.setConverter(new StringConverter<UserDefinedPropertyGroup>() {
            @Override
            public String toString(UserDefinedPropertyGroup userDefinedPropertyGroup) {
                if (userDefinedPropertyGroup != null) {
                    return userDefinedPropertyGroup.getLabel();
                }
                return "";
            }

            @Override
            public UserDefinedPropertyGroup fromString(String s) {
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
            if (availableGroups != null && !availableGroups.isEmpty() && startingPropertyType == null) {
                startingGroup = availableGroups.iterator().next();
            } else {
                startingGroup = new UserDefinedPropertyGroup("", "", "", new ArrayList<>());
            }
        }

        namespaceComboBox.setValue(startingGroup);

        namespaceBox.getChildren().add(namespaceComboBox);
        getChildren().add(namespaceBox);

        //Create a box for the actual namespace definition.
        HBox relationshipDefinitionBox = new HBox(15);
        Label relationshipLabel = new Label(TextFactory.getText(Labels.LabelKey.RELATIONSHIP_LABEL));
        relationshipLabel.setMinWidth(100);
        relationshipLabel.setWrapText(true);
        relationshipDefinitionBox.getChildren().add(relationshipLabel);

        //Create a combobox to select or enter the relationship uri or name, this box selection is controlled by the namespace selection.
        final ComboBox<PropertyType> relationshipComboBox = (ComboBox) ControlFactory.createControl(ControlType.EDITABLE_COMBO_BOX, null, null);
        relationshipComboBox.setCellFactory(relationshipCellFactory);
        relationshipComboBox.getEditor().textProperty().addListener(addNewRelationshipListener);
        if (startingPropertyType == null) {
            addNewRelationshipListener.fieldAdded();
        }

        /*
         * Creates a converter that converts the combobox string into a PropertyType object. It's important to note this differs from the
         * cell factory that provides the dropdown list which uses the property type label, this uses the domain predicate.
         */
        relationshipComboBox.setConverter(new StringConverter<PropertyType>() {
            @Override
            public String toString(PropertyType propertyType) {
                if (propertyType != null) {
                    return propertyType.getDomainPredicate().toString();
                }
                return "";
            }

            @Override
            public PropertyType fromString(String s) {
                PropertyType propertyType = null;
                try {
                    propertyType = findPropertyTypeInGroup(new URI(s), namespaceComboBox.getValue());
                } catch (URISyntaxException e) {
                   //If the string isn't a uri then it must be a user entered property type
                }

                if (propertyType == null) {
                    propertyType = new PropertyType();
                    propertyType.setLabel(s);
                    propertyType.setDescription(s);

                    try {
                        propertyType.setDomainPredicate(new URI(s));
                    } catch (URISyntaxException e) {
                        //If the string isn't a uri then the user entered a string
                    }
                }

                return propertyType;
            }
        });

        //Either set the starting value to the passed in relationship or the first relationship in the starting group.
        if (startingPropertyType != null) {
            relationshipComboBox.setValue(startingPropertyType);
        } else if (startingGroup != null) {
            if (startingGroup.getPropertyTypes() != null && !startingGroup.getPropertyTypes().isEmpty()) {
                relationshipComboBox.getItems().addAll(startingGroup.getPropertyTypes());

                startingPropertyType = startingGroup.getPropertyTypes().iterator().next();

                relationshipComboBox.setValue(startingPropertyType);
            }
        }

        //Error label that lets the user know they either need to specify a URI or one of our known relationship types. Hopefully at some point known relationship types will go away.
        final Label requiresURILabel = new Label(TextFactory.getText(Labels.LabelKey.RELATIONSHIP_MUST_BE_URI_OR_KNOWN));
        requiresURILabel.setTextFill(Color.RED);
        requiresURILabel.setVisible(false);

        //Listens for changes to the relationship value and adjusts other fields accordingly.
        relationshipComboBox.valueProperty().addListener((observableValue, relationship, newPropertyType) -> {
            if (newPropertyType != null) {
                //If the current relationship group contains the relationship that means it was selected from the list.
                if (namespaceComboBox.getValue().getPropertyTypes().contains(newPropertyType)) {
                    requiresDisabled.setValue(true);
                    requiresUri.setValue(newPropertyType.getPropertyValueType() != null && newPropertyType.getPropertyValueType().equals(PropertyValueType.URI));
                } else {
                    requiresDisabled.setValue(false);

                    //Set the group to empty since it wasn't selected from a group.
                    namespaceComboBox.setValue(new UserDefinedPropertyGroup("", "", "", new ArrayList<>()));

                }
            } else {
                //If the relationship entered isn't valid or a known type, disable all other fields and display a warning.
                requiresURILabel.setVisible(true);
                requiresDisabled.setValue(true);
            }
        });

        relationshipDefinitionBox.getChildren().add(relationshipComboBox);
        getChildren().add(relationshipDefinitionBox);

        HBox errorBox = new HBox();
        errorBox.setAlignment(Pos.CENTER);
        errorBox.getChildren().add(requiresURILabel);
        getChildren().add(errorBox);

        //Set up the listener that will switch the values in the relationship box when the schema box changes.
        namespaceComboBox.valueProperty().addListener((observableValue, oldValue, newGroup) -> {
            if (newGroup != null) {

                if (newGroup.getPropertyTypes() != null && !newGroup.getPropertyTypes().isEmpty()) {
                    relationshipComboBox.getItems().clear();
                    //TODO: This show/hide is a hack to work around a javafx2 bug where the combo box list isn't correctly measured on the first pass. If we upgrade to java 8 this should be removed.
                    relationshipComboBox.show();
                    relationshipComboBox.getItems().addAll(newGroup.getPropertyTypes());
                    relationshipComboBox.hide();
                    relationshipComboBox.setValue(newGroup.getPropertyTypes().iterator().next());

                } else {
                    //The user typed in relationship so remove all the other relationships from the combo box.
                    PropertyType currentRelationship = relationshipComboBox.getValue();
                    ListIterator<PropertyType> iter = relationshipComboBox.getItems().listIterator();
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
        CheckBox requiresURICheckBox = new CheckBox(TextFactory.getText(Labels.LabelKey.REQURIRES_URI_LABEL));

        boolean startingRequiresURI = false;
        boolean startingRequiresDisabled = false;

        if (startingPropertyType != null) {
            startingRequiresURI = startingPropertyType.getPropertyValueType() != null && startingPropertyType.getPropertyValueType().equals(PropertyValueType.URI);
            //Were starting with a relationship from a group.
            if (!startingGroup.getLabel().isEmpty()) {
                startingRequiresDisabled = true;
            }
        }
        requiresUri = new SimpleBooleanProperty(startingRequiresURI);
        requiresDisabled = new SimpleBooleanProperty(startingRequiresDisabled);

        requiresURICheckBox.disableProperty().bind(requiresDisabled);
        requiresURICheckBox.selectedProperty().bindBidirectional(requiresUri);
        getChildren().add(requiresURICheckBox);

        final VBox relatedItemsBox = new VBox(3);
        relatedItemsBox.setPrefWidth(ControlFactory.textPrefWidth);

        Label relatedToLabel = new Label(TextFactory.getText(Labels.LabelKey.RELATIONSHIP_TARGET_LABEL));
        relatedItemsBox.getChildren().add(relatedToLabel);
        boolean empty = true;

        //create a HBox to hold text field and validating result image label
        final HBox singleItemInputBox = new HBox(15);
        targetType.bind(Bindings.when(requiresUri).then(TextFactory.getText(Labels.LabelKey.URI_LABEL)).otherwise(TextFactory.getText(Labels.LabelKey.LITERAL_LABEL)));

        Label typeLabel = new Label();
        typeLabel.textProperty().bind(targetType);
        typeLabel.setMaxWidth(100);
        typeLabel.setMinWidth(100);
        singleItemInputBox.getChildren().add(typeLabel);

        //Create a button that allows for adding a new relationship target field box.
        final Button addNewButton = new Button("+");
        final EmptyFieldButtonDisableListener targetChangeListener = new EmptyFieldButtonDisableListener(addNewButton);

        //If there aren't any related items already just create an empty box to specify one.
        if (values == null || values.isEmpty()) {
            final Label userInputImageLabel = new Label();
            userInputImageLabel.setPrefWidth(18);
            TextField relatedItem = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null, null);
            //Add listener which displays a blank, a green check or a red cross, according to the validity of user's input
            relatedItem.textProperty().addListener(getNewChangeListenerForRelatedItem(userInputImageLabel));
            relatedItem.setMinWidth(260);

            relatedItem.textProperty().addListener(targetChangeListener);
            relatedItem.textProperty().addListener(addNewRelationshipListener);
            targetChangeListener.fieldAdded();
            addNewRelationshipListener.fieldAdded();

            singleItemInputBox.getChildren().add(relatedItem);
            singleItemInputBox.getChildren().add(userInputImageLabel);
            relatedItemsBox.getChildren().add(singleItemInputBox);
        } else {
            empty = false;
            //Otherwise loop through the relationship targets and create a text field for each one.

            for (String relatedItem : values) {
                final Label userInputImageLabel = new Label();
                userInputImageLabel.setPrefWidth(18);

                TextField relatedItemTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, relatedItem, null);
                //Add listener which displays a blank, a green check or a red cross, according to the validity of user's input
                relatedItemTextField.textProperty().addListener(getNewChangeListenerForRelatedItem(userInputImageLabel));
                relatedItemTextField.textProperty().addListener(targetChangeListener);
                relatedItemTextField.textProperty().addListener(addNewRelationshipListener);

                singleItemInputBox.getChildren().add(relatedItemTextField);
                singleItemInputBox.getChildren().add(userInputImageLabel);

                if (relatedItemTextField.getText().isEmpty()) {
                    empty = true;
                }
                relatedItemsBox.getChildren().add(singleItemInputBox);
            }
        }

        getChildren().add(relatedItemsBox);

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
    private class RelationshipGroupCellFactory implements Callback<ListView<UserDefinedPropertyGroup>, ListCell<UserDefinedPropertyGroup>> {

        @Override
        public ListCell<UserDefinedPropertyGroup> call(ListView<UserDefinedPropertyGroup> relationshipGroupListView) {
            return new ListCell<UserDefinedPropertyGroup>() {
                @Override protected void updateItem(UserDefinedPropertyGroup item, boolean empty) {
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
    private class RelationshipCellFactory implements Callback<ListView<PropertyType>, ListCell<PropertyType>> {

        @Override
        public ListCell<PropertyType> call(ListView<PropertyType> relationshipListView) {
            return new ListCell<PropertyType>() {
                @Override protected void updateItem(PropertyType item, boolean empty) {
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

    //Helper function that finds the group for a passed in property type.
    private UserDefinedPropertyGroup findGroupForPropertyType(PropertyType relationship) {
        UserDefinedPropertyGroup userDefinedPropertyGroup = null;

        if (availableGroups != null) {
            Iterator<UserDefinedPropertyGroup> groupIterator = availableGroups.iterator();
            while (userDefinedPropertyGroup == null && groupIterator.hasNext()) {
                UserDefinedPropertyGroup group = groupIterator.next();
                for (PropertyType possibleRelationship : group.getPropertyTypes()) {
                    if (relationship == possibleRelationship) {
                        userDefinedPropertyGroup = group;
                        break;
                    }
                }
            }
        }

        return userDefinedPropertyGroup;
    }

    //Function to check to see if the property type is from the selected group, this is used to determine if the user has typed in a domain predicate.
    private PropertyType findPropertyTypeInGroup(URI domainPredicate, UserDefinedPropertyGroup group) {
        PropertyType propertyType = null;
        if (group != null) {
            for (PropertyType possiblePropertyType : group.getPropertyTypes()) {
                if(possiblePropertyType.getDomainPredicate().equals(domainPredicate)) {
                    propertyType = possiblePropertyType;
                    break;
                }
            }
        }

        return propertyType;
    }

    private ChangeListener<String> getNewChangeListenerForRelatedItem(final Label errorMessageLabel) {
        //Add a listener for text entry in the definition box. If the user enters an definition that is hierarchical an error message will be displayed.
        return (observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty() || !requiresUri.getValue()) {
                setLabelImage(errorMessageLabel, null);
          //  } else if (UrlValidator.isValid(newValue)) {
          //      setLabelImage(errorMessageLabel, GOOD_INPUT_IMAGE);
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

    public PropertyType getUserDefinedPropertyType() {
        return userDefinedPropertyType;
    }

    public List<TextPropertyBox> getUserDefinedPropertyObjects() {
        return userDefinedPropertyObjects;
    }
}
