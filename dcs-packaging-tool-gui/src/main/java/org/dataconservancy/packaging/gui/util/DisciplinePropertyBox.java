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
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;

import java.util.List;
import java.util.Map;

/**
 * Widget for displaying combo boxes for selecting disciplines to add to an artifact.
 */
public class DisciplinePropertyBox extends HBox implements PropertyBox {

    private ComboBox<String> disciplineSelectionBox;

    public DisciplinePropertyBox(String value, boolean editable, DisciplineLoadingService service, int prefWidth) {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(30);

        final Map<String, List<String>> disciplineMap = service.getAllDisciplines();
        getChildren().add(createDisciplineSelectors(disciplineMap, editable, findDisciplineGroup(value, service.getAllDisciplines()), value, prefWidth));
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

    private VBox createDisciplineSelectors(final Map<String, List<String>> availableDisciplines, boolean editable, String disciplineGroup, String disciplineValue, int prefWidth) {
        VBox disciplineSelectors = new VBox(8);

        ComboBox<String> disciplineGroupBox = new ComboBox<>();
        disciplineGroupBox.getItems().addAll(availableDisciplines.keySet());

        if ((disciplineGroup == null || disciplineGroup.isEmpty()) && !availableDisciplines.keySet().isEmpty()) {
            disciplineGroup = availableDisciplines.keySet().iterator().next();
        }

        if (disciplineGroup != null && !disciplineGroup.isEmpty()) {
            disciplineGroupBox.setValue(disciplineGroup);
        }

        disciplineGroupBox.setPrefWidth(prefWidth);
        disciplineGroupBox.setDisable(!editable);

        disciplineSelectors.getChildren().add(disciplineGroupBox);

        disciplineSelectionBox = new ComboBox<>();
        List<String> startingGroup;
        if (disciplineGroup != null && !disciplineGroup.isEmpty()) {
            startingGroup = availableDisciplines.get(availableDisciplines.keySet().iterator().next());
            disciplineSelectionBox.getItems().addAll(startingGroup);
        }

        if (disciplineValue != null && !disciplineValue.isEmpty()) {
            disciplineSelectionBox.setValue(disciplineValue);
        }

        disciplineSelectionBox.setPrefWidth(prefWidth);
        disciplineSelectionBox.setDisable(!editable);
        disciplineSelectors.getChildren().add(disciplineSelectionBox);

        disciplineGroupBox.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            disciplineSelectionBox.getItems().clear();
            if (availableDisciplines.get(newValue) != null) {
                disciplineSelectionBox.getItems().addAll(availableDisciplines.get(newValue));
            }
        });

        disciplineGroupBox.requestFocus();

        StringProperty propertyValue = new SimpleStringProperty();
        propertyValue.bind(disciplineSelectionBox.valueProperty());

        return disciplineSelectors;
    }

    @Override
    public Object getValue() {
        return disciplineSelectionBox.getValue();
    }

    @Override
    public Control getPropertyInput() {
        return disciplineSelectionBox;
    }

    @Override
    public String getValueAsString() {
        return disciplineSelectionBox.getValue();
    }

    @Override
    public PropertyValueType getPropertyBoxValueType() {
        return PropertyValueType.STRING;
    }

    @Override
    public Node getView() {
        return this;
    }
}
