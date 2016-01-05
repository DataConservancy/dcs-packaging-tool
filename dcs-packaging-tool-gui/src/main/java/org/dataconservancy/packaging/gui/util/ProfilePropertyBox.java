package org.dataconservancy.packaging.gui.util;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.impl.PropertyFormatServiceImpl;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilePropertyBox extends VBox implements CssConstants {
    PropertyConstraint propertyConstraint;
    List<PropertyBox> propertyBoxes;

    //This is a little ugly but needed to keep complex properties grouped properly together.
    List<List<ProfilePropertyBox>> subPropertyBoxes;

    private DisciplineLoadingService disciplineLoadingService;

    public ProfilePropertyBox(PropertyConstraint propertyConstraint, List<Property> existingProperties, DisciplineLoadingService disciplineLoadingService) {
        PropertyFormatService formatService = new PropertyFormatServiceImpl();

        this.propertyConstraint = propertyConstraint;
        this.disciplineLoadingService = disciplineLoadingService;

        setSpacing(6);

        HBox propertyLabelAndValueBox = new HBox(12);
        propertyLabelAndValueBox.setPadding(new Insets(0, 24, 0, 0));

        propertyLabelAndValueBox.setAlignment(Pos.CENTER_LEFT);
        Label propertyNameLabel = new Label(propertyConstraint.getPropertyType().getLabel());

        if (propertyConstraint.getMinimum() > 0) {
            propertyNameLabel.getStyleClass().add(REQUIRED_PROPERTY);
        }

        propertyNameLabel.setPrefWidth(100);
        propertyNameLabel.setMinWidth(100);
        propertyNameLabel.setWrapText(true);
        propertyLabelAndValueBox.getChildren().add(propertyNameLabel);

        getChildren().add(propertyLabelAndValueBox);

        final VBox propertyValuesBox = new VBox(6);

        final Button addNewButton = new Button(
           TextFactory.getText(Labels.LabelKey.ADD_NEW_BUTTON));
        double addNewButtonMaxWidth = 200;
        addNewButton.setMaxWidth(addNewButtonMaxWidth);
        addNewButton.setDisable(true);

        final boolean editable = !propertyConstraint.getPropertyType().isReadOnly();

        final GroupPropertyChangeListener listener = new GroupPropertyChangeListener(addNewButton);

        if (propertyConstraint.getPropertyType().getPropertyValueType().equals(PropertyValueType.COMPLEX)) {
            addNewButton.setText(TextFactory.format(Messages.MessageKey.ADD_NEW_MESSAGE, propertyConstraint.getPropertyType().getLabel()));
            subPropertyBoxes = new ArrayList<>();
            createChildProfilePropertyBoxes(existingProperties, listener, propertyValuesBox);
            listener.changed(null, "n/a", "n/a");
            getChildren().add(propertyValuesBox);

            if (propertyConstraint.getMaximum() > 1 || propertyConstraint.getMaximum() == -1) {

                getChildren().add(addNewButton);

                addNewButton.setOnAction(arg0 -> {
                    createChildProfilePropertyBoxes(null, listener, propertyValuesBox);

                    addNewButton.setDisable(true);
                    requestFocusForNewGroup(subPropertyBoxes.get(subPropertyBoxes.size()-1).get(0));
                });

                Separator groupSeparator = new Separator();
                getChildren().add(groupSeparator);
            }
        } else {

            propertyBoxes = new ArrayList<>();

            if (existingProperties != null && !existingProperties.isEmpty()) {
                for (Property property : existingProperties) {
                    Object value;
                    if (property.getPropertyType().getPropertyValueType() != null && property.getPropertyType().getPropertyValueType().equals(PropertyValueType.DATE_TIME)) {
                        value = property.getDateTimeValue();
                    } else if(property.getPropertyType().getPropertyValueType() != null && property.getPropertyType().getPropertyValueType().equals(PropertyValueType.LONG)
                        && property.getPropertyType().getPropertyValueHint() != null && !property.getPropertyType().getPropertyValueHint().equals(PropertyValueHint.FILE_SIZE)) {
                        value = property.getLongValue();
                    } else {
                        value = formatService.formatPropertyValue(property);
                    }
                    PropertyBox propertyBox = generatePropertyBox(value, editable, property.getPropertyType());
                    propertyBox.getPropertyInput().setPrefWidth(1600);

                    propertyBoxes.add(propertyBox);
                    propertyValuesBox.getChildren().add(propertyBox.getView());
                    addChangeListenerToPropertyFields(propertyBox, listener);

                    listener.changed(null, "n/a", "n/a");

                }
            } else {
                PropertyBox propertyBox = generatePropertyBox("", editable, propertyConstraint.getPropertyType());
                propertyBox.getPropertyInput().setPrefWidth(1600);

                propertyBoxes.add(propertyBox);
                addChangeListenerToPropertyFields(propertyBox, listener);
                listener.changed(null, "n/a", "n/a");

                propertyValuesBox.getChildren().add(propertyBox.getView());
            }

            propertyLabelAndValueBox.getChildren().add(propertyValuesBox);

            if (propertyConstraint.getMaximum() > 1 || propertyConstraint.getMaximum() == -1) {
                propertyLabelAndValueBox.getChildren().add(addNewButton);

                addNewButton.setOnAction(arg0 -> {
                    PropertyBox propertyBox = generatePropertyBox("", editable, propertyConstraint.getPropertyType());
                    propertyBox.getPropertyInput().setPrefWidth(1600);

                    propertyValuesBox.getChildren().add(propertyBox.getView());

                    propertyBoxes.add(propertyBox);
                    addChangeListenerToPropertyFields(propertyBox, listener);
                    listener.changed(null, "n/a", "n/a");

                    addNewButton.setDisable(true);
                });
            }
        }

    }

    private PropertyBox generatePropertyBox(Object initialValue, boolean editable, PropertyType propertyType) {
        if (propertyType != null && propertyType.getPropertyValueType() != null) {
            switch (propertyType.getPropertyValueType()) {
                case LONG:
                    //We display file sizes as formatted text, with size labels so we can't use a NumericPropertyBox
                    if (propertyType.getPropertyValueHint() != null && propertyType.getPropertyValueHint().equals(PropertyValueHint.FILE_SIZE)) {
                        return new TextPropertyBox(initialValue, editable, propertyType.getPropertyValueHint(), "");
                    } else {
                        return new NumericPropertyBox(initialValue, editable, "");
                    }
                case DATE_TIME:
                    return new DatePropertyBox(initialValue, editable, "");
                case STRING:
                    if (propertyType.getPropertyValueHint() != null) {
                        switch (propertyType.getPropertyValueHint()) {
                            case DCS_DISCIPLINE:
                                return new DisciplinePropertyBox((String) initialValue, editable, disciplineLoadingService);
                            default:
                                return new TextPropertyBox(initialValue, editable, propertyType.getPropertyValueHint(), "");
                        }
                    }
            }

        }

        //If we dont' have a hint we just assume it's a text property.
        return new TextPropertyBox(initialValue, editable, null, "");
    }

    private void createChildProfilePropertyBoxes(List<Property> existingProperties, GroupPropertyChangeListener listener, VBox propertyValueBox) {
        List<PropertyConstraint> sortedProperties = new ArrayList<>();

        //Get the property name key set and then create a sorted list from it.
        sortedProperties.addAll(propertyConstraint.getPropertyType().getComplexPropertyConstraints());
        sortProperties(sortedProperties);

        if (existingProperties != null && !existingProperties.isEmpty()) {
            //Loop through each individual complex property we don't want their sub properties combined.
            for (Property existingProperty : existingProperties) {
                List<ProfilePropertyBox> complexPropertyBoxList = new ArrayList<>();
                //Then loop through all the sorted sub properties and add them.
                for (PropertyConstraint subConstraint : sortedProperties) {
                    List<Property> existingSubProperties = existingProperty.getComplexValue().stream().filter(existingSubProperty -> existingSubProperty.getPropertyType().equals(subConstraint.getPropertyType())).collect(Collectors.toList());
                    ProfilePropertyBox subProfilePropertyBox = new ProfilePropertyBox(subConstraint, existingSubProperties, disciplineLoadingService);
                    subProfilePropertyBox.getStyleClass().add(SUB_PROPERTY);
                    complexPropertyBoxList.add(subProfilePropertyBox);
                    propertyValueBox.getChildren().add(subProfilePropertyBox);
                    addChangeListenerToProfileBox(subProfilePropertyBox, listener);
                }


                subPropertyBoxes.add(complexPropertyBoxList);
            }
        } else {
            List<ProfilePropertyBox> complexPropertyBoxList = new ArrayList<>();
            for (PropertyConstraint subConstraint : sortedProperties) {
                ProfilePropertyBox subProfilePropertyBox = new ProfilePropertyBox(subConstraint, null, disciplineLoadingService);
                subProfilePropertyBox.getStyleClass().add(SUB_PROPERTY);
                complexPropertyBoxList.add(subProfilePropertyBox);
                propertyValueBox.getChildren().add(subProfilePropertyBox);
                addChangeListenerToProfileBox(subProfilePropertyBox, listener);
            }

            subPropertyBoxes.add(complexPropertyBoxList);
        }

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

    public List<Object> getValues() {
        return propertyBoxes.stream().map(PropertyBox::getValue).collect(Collectors.toList());
    }

    public List<List<ProfilePropertyBox>> getSubPropertyBoxes() {
        return subPropertyBoxes;
    }

    public PropertyConstraint getPropertyConstraint() {
        return propertyConstraint;
    }

    /**
     * Adds a listener to all TextFields or TextInputControl within the specified pane.  Will dive into sub-panes as needed.
     *
     * @param propertyBox    The property box to listen to
     * @param listener The listener to attach to the TextInputControl
     */
    @SuppressWarnings("unchecked")
    private void addChangeListenerToPropertyFields(PropertyBox propertyBox,
                                                   ChangeListener<? super String> listener) {

        if (propertyBox.getPropertyInput() instanceof TextInputControl) {
            ((TextInputControl) propertyBox.getPropertyInput()).textProperty().addListener(listener);
        } else if (propertyBox.getPropertyInput() instanceof ComboBox) {
            ((ComboBox) propertyBox.getPropertyInput()).valueProperty().addListener(listener);
        }
    }

    private void addChangeListenerToProfileBox(ProfilePropertyBox profilePropertyBox,
                                                    ChangeListener<? super String> listener) {

        for (PropertyBox propertyBox : profilePropertyBox.propertyBoxes) {
            addChangeListenerToPropertyFields(propertyBox, listener);
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
        for (javafx.scene.Node n : pane.getChildren()) {
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
     * Class to capture changes to Group Properties (ie, any change to a field within the group), so as
     * to enable/disable the "Add New" button as appropriate
     */
    private class GroupPropertyChangeListener implements ChangeListener<String> {
        private Button propertyAddButton;

        public GroupPropertyChangeListener(Button propertyAddButton) {
            this.propertyAddButton = propertyAddButton;
        }

        @Override
        public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
            propertyAddButton.setDisable(anyGroupsEmpty());
        }

        /**
         * Determines if any of the groups are empty
         *
         * @return True if there is at least one group that has no values, false if every group has
         * at least one value in it.
         */
        private boolean anyGroupsEmpty() {
            if (propertyBoxes != null) {
                for (PropertyBox propertyBox : propertyBoxes) {
                    if (propertyBox.getValue() != null) {
                        if (propertyBox.getValue() instanceof String && propertyBox.getValueAsString().isEmpty()) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }

            if (subPropertyBoxes != null) {
                for (List<ProfilePropertyBox> complexPropertyList : subPropertyBoxes) {
                    for (ProfilePropertyBox profilePropertyBox : complexPropertyList) {
                        //For complex properties we should only care if required properties are filled in
                        if (profilePropertyBox.getPropertyConstraint().getMinimum() >= 1) {
                            for (PropertyBox propertyBox : profilePropertyBox.propertyBoxes) {
                                if (propertyBox.getValue() != null) {
                                    if (propertyBox.getValue() instanceof String &&
                                        propertyBox.getValueAsString().isEmpty()) {
                                        return true;
                                    }
                                } else {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    }
}
