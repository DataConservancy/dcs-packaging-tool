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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Widget for displaying combo boxes for selecting disciplines to add to an artifact.
 */
public class DisciplinePropertyBox extends HBox {

    public DisciplinePropertyBox(String propertyLabel, Set<String> propertyValues, int maxOccurs, final Set<StringProperty> fields, int minOccurs, final boolean systemGenerated,
                                 final Map<String, List<String>> availableDisciplines) {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(30);

        if (minOccurs > 0) {
            propertyLabel += "*";
        }

        Label propertyNameLabel = new Label(propertyLabel);
        propertyNameLabel.setPrefWidth(84);
        propertyNameLabel.setWrapText(true);
        getChildren().add(propertyNameLabel);
        boolean hasValue = false;

        final Button addNewButton = new Button("+");
        final EmptyFieldButtonDisableListener listener = new EmptyFieldButtonDisableListener(addNewButton);


        final VBox propertyValuesBox = new VBox(24);
        //If the property has values already add a text field for each one
        if (propertyValues != null && !propertyValues.isEmpty()) {
            for (String disciplineName : propertyValues) {
                String disciplineGroup = findDisciplineGroup(disciplineName, availableDisciplines);

                VBox disciplineSelectors = createDisciplineSelectors(availableDisciplines, systemGenerated, listener, addNewButton, fields, disciplineGroup, disciplineName);
                propertyValuesBox.getChildren().add(disciplineSelectors);
            }
            hasValue = true;
        //Otherwise create an empty text field for the value.
        } else {
            VBox disciplineSelectors = createDisciplineSelectors(availableDisciplines, systemGenerated, listener, addNewButton, fields, "", "");
            propertyValuesBox.getChildren().add(disciplineSelectors);
        }

        getChildren().add(propertyValuesBox);

        //If the ontology allows for more than one value on a property add a button to add more fields.
        if (maxOccurs > 1) {
            addNewButton.setDisable(!hasValue);
            getChildren().add(addNewButton);

            addNewButton.setOnAction(arg0 -> {
                VBox disciplineSelectors = createDisciplineSelectors(availableDisciplines, systemGenerated, listener, addNewButton, fields, "", "");
                propertyValuesBox.getChildren().add(disciplineSelectors);
            });
        }
    }

    private String findDisciplineGroup(String discipline, Map<String, List<String>> disciplineMap) {
        String groupName = "";
        for (String name : disciplineMap.keySet()) {
            List<String> discplines = disciplineMap.get(name);
            if (discplines.contains(discipline)) {
                groupName = name;
                break;
            }
        }

        return groupName;
    }

    private VBox createDisciplineSelectors(final Map<String, List<String>> availableDisciplines, boolean systemGenerated, EmptyFieldButtonDisableListener listener,
                                           Button addNewButton, Set<StringProperty> fields, String disciplineGroup, String disciplineValue) {
        VBox disciplineSelectors = new VBox(8);

        ComboBox<String> disciplineGroupBox = new ComboBox<>();
        disciplineGroupBox.getItems().addAll(availableDisciplines.keySet());

        if ((disciplineGroup == null || disciplineGroup.isEmpty()) && !availableDisciplines.keySet().isEmpty()) {
            disciplineGroup = availableDisciplines.keySet().iterator().next();
        }

        if (disciplineGroup != null && !disciplineGroup.isEmpty()) {
            disciplineGroupBox.setValue(disciplineGroup);
        }

        disciplineGroupBox.setPrefWidth(250);
        disciplineGroupBox.setDisable(systemGenerated);

        disciplineSelectors.getChildren().add(disciplineGroupBox);

        final ComboBox<String> disciplineBox = new ComboBox<>();
        List<String> startingGroup;
        if (disciplineGroup != null && !disciplineGroup.isEmpty()) {
            startingGroup = availableDisciplines.get(availableDisciplines.keySet().iterator().next());
            disciplineBox.getItems().addAll(startingGroup);
        }

        if (disciplineValue != null && !disciplineValue.isEmpty()) {
            disciplineBox.setValue(disciplineValue);
        }

        disciplineBox.setPrefWidth(250);
        disciplineBox.setDisable(systemGenerated);
        disciplineBox.valueProperty().addListener(listener);
        disciplineSelectors.getChildren().add(disciplineBox);

        disciplineGroupBox.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            disciplineBox.getItems().clear();
            if (availableDisciplines.get(newValue) != null) {
                disciplineBox.getItems().addAll(availableDisciplines.get(newValue));
            }
        });

        disciplineBox.valueProperty().addListener(listener);
        addNewButton.setDisable(true);
        disciplineGroupBox.requestFocus();

        StringProperty propertyValue = new SimpleStringProperty();
        propertyValue.bind(disciplineBox.valueProperty());
        fields.add(propertyValue);


        return disciplineSelectors;
    }
}
