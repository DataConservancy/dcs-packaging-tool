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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.model.UserDefinedPropertyVocabulary;
import org.dataconservancy.packaging.tool.impl.support.Validator;
import org.dataconservancy.packaging.tool.impl.support.ValidatorFactory;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Creates a VBox that contains the controls for defining a user defined property.
 * The main controls are a vocabulary combobox, a predicate combobox that allows selecting or entering a predicate.
 * A checkbox that denotes if the target is required to be a URI. And text boxes for the property values. All fields are linked with listeners on their properties.
 * Of note predicate and target must be filled in to add a new predicate. Any property type that is known or provided from a dropdown, controls the uri checkbox and disables it.
 */
public class UserDefinedPropertyBox extends VBox implements CssConstants {

    private List<UserDefinedPropertyVocabulary> availableVocabularies;
    private RelationshipGroupCellFactory vocabularyCellFactory;
    private RelationshipCellFactory propertyPredicateCellFactory;
    private BooleanProperty requiresUri;
    private BooleanProperty requiresDisabled;
    private StringProperty targetType;

    private ComboBox<PropertyType> propertyTypeComboBox;
    private List<TextPropertyBox> userDefinedPropertyValues;

    public UserDefinedPropertyBox(PropertyType userDefinedPropertyType, List<String> userDefinedPropertyValues, List<UserDefinedPropertyVocabulary> availableVocabularies,
                                  EmptyFieldButtonDisableListener addNewPropertyListener) {
        this.availableVocabularies = availableVocabularies;
        vocabularyCellFactory = new RelationshipGroupCellFactory();
        propertyPredicateCellFactory = new RelationshipCellFactory();
        this.userDefinedPropertyValues = new ArrayList<>();

        UserDefinedPropertyVocabulary startingGroup = null;
        targetType = new SimpleStringProperty(TextFactory.getText(Labels.LabelKey.URI_LABEL));

        if (userDefinedPropertyType != null) {
            startingGroup = findGroupForPropertyType(userDefinedPropertyType);
        }

        createdUserDefinedPropertyBox(startingGroup, userDefinedPropertyType, userDefinedPropertyValues, addNewPropertyListener);
    }

    @SuppressWarnings("unchecked")
    private void createdUserDefinedPropertyBox(UserDefinedPropertyVocabulary startingVocabulary, PropertyType startingPropertyType, List<String> values,
                                               final EmptyFieldButtonDisableListener addNewPropertyListener) {

        setSpacing(8);

        Separator groupSeparator = new Separator();
        getChildren().add(groupSeparator);

        Label userPropertyDefinitionLabel = new Label(TextFactory.getText(Labels.LabelKey.USER_PROPERTY_DEFINITION_LABEL));
        getChildren().add(userPropertyDefinitionLabel);

        //Create a box for the namespace selection elements
        final HBox namespaceBox = new HBox(15);
        Label vocabularyLabel = new Label(TextFactory.getText(Labels.LabelKey.VOCABULARY_LABEL));
        vocabularyLabel.setMinWidth(100);
        vocabularyLabel.setWrapText(true);
        namespaceBox.getChildren().add(vocabularyLabel);

        //The combobox that allows for selecting a vocabulary.
        final ComboBox<UserDefinedPropertyVocabulary> vocabularyComboBox = (ComboBox) ControlFactory.createControl(ControlType.COMBO_BOX, null, null);
        vocabularyComboBox.setCellFactory(vocabularyCellFactory);

        //Converts the selected property vocabulary to a string to display in the combobox.
        vocabularyComboBox.setConverter(new StringConverter<UserDefinedPropertyVocabulary>() {
            @Override
            public String toString(UserDefinedPropertyVocabulary userDefinedPropertyVocabulary) {
                if (userDefinedPropertyVocabulary != null) {
                    return userDefinedPropertyVocabulary.getLabel();
                }
                return "";
            }

            @Override
            public UserDefinedPropertyVocabulary fromString(String s) {
                // Default method body
                return null;
            }
        });

        //Add all available groups from the relationship file to the combobox list.
        if (availableVocabularies != null) {
            vocabularyComboBox.getItems().addAll(availableVocabularies);
        }

        //If we haven't detected a starting vocabulary either show the next vocabulary(if we have no starting property, or blank if the starting property isn't in a vocabulary.
        if (startingVocabulary == null) {
            if (availableVocabularies != null && !availableVocabularies.isEmpty() && startingPropertyType == null) {
                startingVocabulary = availableVocabularies.iterator().next();
            } else {
                startingVocabulary = new UserDefinedPropertyVocabulary("", "", "", new ArrayList<>());
            }
        }

        vocabularyComboBox.setValue(startingVocabulary);

        namespaceBox.getChildren().add(vocabularyComboBox);
        getChildren().add(namespaceBox);

        //Create a box for the actual predicate definition.
        HBox predicateBox = new HBox(15);
        Label predicateLabel = new Label(TextFactory.getText(Labels.LabelKey.PREDICATE_LABEL));
        predicateLabel.setMinWidth(100);
        predicateLabel.setWrapText(true);
        predicateBox.getChildren().add(predicateLabel);

        //Create a combobox to select or enter the predicate uri, this box selection is controlled by the vocabulary selection.
        propertyTypeComboBox = (ComboBox) ControlFactory.createControl(ControlType.EDITABLE_COMBO_BOX, null, null);
        propertyTypeComboBox.setCellFactory(propertyPredicateCellFactory);
        propertyTypeComboBox.getEditor().textProperty().addListener(addNewPropertyListener);
        if (startingPropertyType == null) {
            addNewPropertyListener.fieldAdded();
        }

        //Error label that lets the user know they either need to specify a URI or one of our known property types.
        final Label requiresURILabel = new Label(TextFactory.getText(Labels.LabelKey.PREDICATE_MUST_BE_URI_OR_KNOWN));
        requiresURILabel.setTextFill(Color.RED);
        requiresURILabel.setVisible(false);

        /*
         * Creates a converter that converts the combobox string into a PropertyType object. It's important to note this differs from the
         * cell factory that provides the dropdown list which uses the property type label, this uses the domain predicate.
         */
        propertyTypeComboBox.setConverter(new StringConverter<PropertyType>() {
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
                    propertyType = findPropertyTypeInGroup(new URI(s), vocabularyComboBox.getValue());
                } catch (URISyntaxException e) {
                   //If the string isn't a uri then it must be a user entered property type
                }

                if (propertyType == null) {
                    Validator uriValidator = ValidatorFactory.getValidator(PropertyValueHint.URI);
                    if (uriValidator != null && uriValidator.isValid(s)) {
                        propertyType = new PropertyType();
                        propertyType.setLabel(s);
                        propertyType.setDescription(s);

                        try {
                            propertyType.setDomainPredicate(new URI(s));
                        } catch (URISyntaxException e) {
                            requiresDisabled.setValue(true);
                            requiresURILabel.setVisible(true);
                        }
                    } else {
                        requiresDisabled.setValue(true);
                        requiresURILabel.setVisible(true);
                    }

                    requiresDisabled.setValue(false);
                    requiresURILabel.setVisible(false);
                }

                return propertyType;
            }
        });

        //Either set the starting value to the passed in property type or the first property type in the starting group.
        if (startingPropertyType != null) {
            propertyTypeComboBox.setValue(startingPropertyType);
        } else if (startingVocabulary != null) {
            if (startingVocabulary.getPropertyTypes() != null && !startingVocabulary.getPropertyTypes().isEmpty()) {
                propertyTypeComboBox.getItems().addAll(startingVocabulary.getPropertyTypes());

                startingPropertyType = startingVocabulary.getPropertyTypes().iterator().next();

                propertyTypeComboBox.setValue(startingPropertyType);
            }
        }

        //Listens for changes to the predicate value and adjusts other fields accordingly.
        propertyTypeComboBox.valueProperty().addListener((observableValue, propertyType, newPropertyType) -> {
            if (newPropertyType != null) {
                //If the current vocabulary contains the predicate that means it was selected from the list.
                if (vocabularyComboBox.getValue().getPropertyTypes().contains(newPropertyType)) {
                    requiresDisabled.setValue(true);
                    requiresUri.setValue(newPropertyType.getPropertyValueType() != null && newPropertyType.getPropertyValueType().equals(PropertyValueType.URI));
                    requiresURILabel.setVisible(false);
                } else {
                    requiresDisabled.setValue(false);

                    //Set the vocabulary to empty since it wasn't selected from a vocabulary.
                    vocabularyComboBox.setValue(new UserDefinedPropertyVocabulary("", "", "", new ArrayList<>()));
                }
            } else {
                //If the predicate entered isn't valid or a known type, disable all other fields and display a warning.
                requiresDisabled.setValue(true);
                requiresURILabel.setVisible(true);
            }
        });

        predicateBox.getChildren().add(propertyTypeComboBox);
        getChildren().add(predicateBox);

        HBox errorBox = new HBox();
        errorBox.setAlignment(Pos.CENTER);
        errorBox.getChildren().add(requiresURILabel);
        getChildren().add(errorBox);

        //Set up the listener that will switch the values in the relationship box when the schema box changes.
        vocabularyComboBox.valueProperty().addListener((observableValue, oldValue, newGroup) -> {
            if (newGroup != null) {

                if (newGroup.getPropertyTypes() != null && !newGroup.getPropertyTypes().isEmpty()) {
                    propertyTypeComboBox.getItems().clear();
                    propertyTypeComboBox.getItems().addAll(newGroup.getPropertyTypes());
                    propertyTypeComboBox.setValue(newGroup.getPropertyTypes().iterator().next());

                } else {
                    //The user typed in relationship so remove all the other relationships from the combo box.
                    PropertyType currentPropertyType = propertyTypeComboBox.getValue();
                    ListIterator<PropertyType> iter = propertyTypeComboBox.getItems().listIterator();
                    while(iter.hasNext()){
                        if(iter.next().equals(currentPropertyType)){
                            iter.remove();
                        }
                    }
                }
            }
        });

        //Checkbox that denotes whether or not the value needs to be a uri or a literal string is allowed.
        //Built in property types define this value, and so the checkbox will be disabled for them. Entered fields can specify this value.
        CheckBox requiresURICheckBox = new CheckBox(TextFactory.getText(Labels.LabelKey.REQURIRES_URI_LABEL));

        boolean startingRequiresURI = false;
        boolean startingRequiresDisabled = false;

        if (startingPropertyType != null) {
            startingRequiresURI = startingPropertyType.getPropertyValueType() != null && startingPropertyType.getPropertyValueType().equals(PropertyValueType.URI);
            //Were starting with a property type from a vocabulary
            if (!startingVocabulary.getLabel().isEmpty()) {
                startingRequiresDisabled = true;
            }
        }
        requiresUri = new SimpleBooleanProperty(startingRequiresURI);
        requiresDisabled = new SimpleBooleanProperty(startingRequiresDisabled);

        requiresURICheckBox.disableProperty().bind(requiresDisabled);
        requiresURICheckBox.selectedProperty().bindBidirectional(requiresUri);
        getChildren().add(requiresURICheckBox);

        final VBox propertyValuesBox = new VBox(6);

        Label propertyValueLabel = new Label(TextFactory.getText(Labels.LabelKey.USER_DEFINED_PROPERTY_VALUE_LABEL));
        getChildren().add(propertyValueLabel);
        boolean empty = true;

        //create a HBox to hold text field and validating result image label
        final HBox valueInputBox = new HBox(12);
        targetType.bind(Bindings.when(requiresUri).then(TextFactory.getText(Labels.LabelKey.URI_LABEL)).otherwise(TextFactory.getText(Labels.LabelKey.LITERAL_LABEL)));

        Label typeLabel = new Label();
        typeLabel.textProperty().bind(targetType);
        typeLabel.setPrefWidth(100);
        valueInputBox.getChildren().add(typeLabel);

        //Create a button that allows for adding a new property value field box.
        final Button addNewButton = new Button("+");
        final EmptyFieldButtonDisableListener emptyPropertyValueListener = new EmptyFieldButtonDisableListener(addNewButton);

        //If there aren't any values already just create an empty box to specify one.
        if (values == null || values.isEmpty()) {
            PropertyBox propertyValueBox = createPropertyValueBox("", emptyPropertyValueListener, addNewPropertyListener);

            propertyValuesBox.getChildren().add(propertyValueBox.getView());
        } else {
            empty = false;
            //Otherwise loop through the relationship targets and create a text field for each one.

            for (String value : values) {
                PropertyBox propertyValueBox = createPropertyValueBox(value, emptyPropertyValueListener, addNewPropertyListener);

                if (((TextInputControl)propertyValueBox.getPropertyInput()).getText().isEmpty()) {
                    empty = true;
                }
                propertyValuesBox.getChildren().add(propertyValueBox.getView());
            }
        }

        valueInputBox.getChildren().add(propertyValuesBox);
        valueInputBox.getChildren().add(addNewButton);

        getChildren().add(valueInputBox);
        addNewButton.setDisable(empty);

        addNewButton.setOnAction(arg0 -> {

            PropertyBox propertyValueBox = createPropertyValueBox("", emptyPropertyValueListener, addNewPropertyListener);

            propertyValuesBox.getChildren().add(propertyValueBox.getView());
            addNewButton.setDisable(true);
            propertyValueBox.getPropertyInput().requestFocus();
        });
    }

    private PropertyBox createPropertyValueBox(String value, EmptyFieldButtonDisableListener emptyPropertyValueListener, EmptyFieldButtonDisableListener emptyPropertyListener) {
        TextPropertyBox propertyValueBox;
        if (requiresUri.getValue()) {
            propertyValueBox = new TextPropertyBox(value, true, PropertyValueHint.URI, "");
        } else {
            propertyValueBox = new TextPropertyBox(value, true, PropertyValueHint.TEXT, "");
        }

        requiresUri.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                propertyValueBox.changePropertyValidationType(PropertyValueHint.URI);
            } else {
                propertyValueBox.changePropertyValidationType(PropertyValueHint.TEXT);
            }
        });

        userDefinedPropertyValues.add(propertyValueBox);
        propertyValueBox.getPropertyInput().setPrefWidth(250);

        ((TextInputControl)propertyValueBox.getPropertyInput()).textProperty().addListener(emptyPropertyValueListener);
        ((TextInputControl)propertyValueBox.getPropertyInput()).textProperty().addListener(emptyPropertyListener);
        emptyPropertyValueListener.fieldAdded();
        emptyPropertyListener.fieldAdded();

        return propertyValueBox;
    }
    /*
     * Cell factor for the Namespace selection box, basically we just display the label of the the relationship group, in the drop down list.
     */
    private class RelationshipGroupCellFactory implements Callback<ListView<UserDefinedPropertyVocabulary>, ListCell<UserDefinedPropertyVocabulary>> {

        @Override
        public ListCell<UserDefinedPropertyVocabulary> call(ListView<UserDefinedPropertyVocabulary> relationshipGroupListView) {
            return new ListCell<UserDefinedPropertyVocabulary>() {
                @Override protected void updateItem(UserDefinedPropertyVocabulary item, boolean empty) {
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
    private UserDefinedPropertyVocabulary findGroupForPropertyType(PropertyType relationship) {
        UserDefinedPropertyVocabulary userDefinedPropertyVocabulary = null;

        if (availableVocabularies != null) {
            Iterator<UserDefinedPropertyVocabulary> groupIterator = availableVocabularies.iterator();
            while (userDefinedPropertyVocabulary == null && groupIterator.hasNext()) {
                UserDefinedPropertyVocabulary group = groupIterator.next();
                for (PropertyType possibleRelationship : group.getPropertyTypes()) {
                    if (relationship == possibleRelationship) {
                        userDefinedPropertyVocabulary = group;
                        break;
                    }
                }
            }
        }

        return userDefinedPropertyVocabulary;
    }

    //Function to check to see if the property type is from the selected group, this is used to determine if the user has typed in a domain predicate.
    private PropertyType findPropertyTypeInGroup(URI domainPredicate, UserDefinedPropertyVocabulary group) {
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

    public PropertyType getUserDefinedPropertyType() {
        PropertyType type = propertyTypeComboBox.getValue();
        if (type != null) {
            if (requiresUri.getValue()) {
                type.setPropertyValueType(PropertyValueType.URI);
            } else {
                type.setPropertyValueType(PropertyValueType.STRING);
            }
        }
        return type;
    }

    public List<TextPropertyBox> getUserDefinedPropertyValues() {
        return userDefinedPropertyValues;
    }
}
