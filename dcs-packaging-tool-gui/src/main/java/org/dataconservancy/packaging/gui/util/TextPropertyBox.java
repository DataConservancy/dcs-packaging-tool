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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.dataconservancy.packaging.gui.App;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.model.PackageArtifact;

import java.text.DecimalFormat;
import java.util.Set;

/**
 * A simple widget for properties that have text values. The box hooks to the ontology service for validation, and handles the addition of new fields.
 */
public class TextPropertyBox extends VBox implements CssConstants {

    // array of labels used to format file size into B, kB, MB, GB, TB, PB, EB, ZB or YB value
    private static final String[] sizeLabels = {" Bytes", " kB", " MB", " GB", " TB", " PB", " EB", " ZB", " YB"};

    //Starting height of a multiline text area, just picked a value that looks good
    private final double startingTextHeight = 105.0;

    public TextPropertyBox(PackageArtifact artifact, final String complexPropertyName, String propertyLabel, final String propertyName, Set<String> propertyValues,
                                            int maxOccurs, final Set<StringProperty> fields, int minOccurs, boolean systemGenerated,
                                            final PackageOntologyService packageOntologyService, final Labels labels, final Messages messages,
                                            ApplyButtonValidationListener applyButtonValidationListener) {
        setSpacing(6);

        HBox propertyBox = new HBox(12);
        propertyBox.setAlignment(Pos.TOP_LEFT);

        if (minOccurs > 0) {
            propertyLabel += "*";
        }

        Label propertyNameLabel = new Label(propertyLabel);
        propertyNameLabel.setPrefWidth(100);
        propertyNameLabel.setWrapText(true);
        propertyBox.getChildren().add(propertyNameLabel);
        boolean hasValue = false;

        final Button addNewButton = new Button("+");
        final EmptyFieldButtonDisableListener addNewListener = new EmptyFieldButtonDisableListener(addNewButton);

        final VBox propertyValuesBox = new VBox(6);

        //If the property has values already add a text field for each one
        if (propertyValues != null && !propertyValues.isEmpty()) {
            for (String originalValue : propertyValues) {

                HBox propertyEntryBox = createPropertyEntryBox(addNewListener, fields, systemGenerated, packageOntologyService, propertyName, labels, applyButtonValidationListener,
                                    artifact, complexPropertyName, propertyBox, messages, originalValue);
                propertyValuesBox.getChildren().add(propertyEntryBox);
            }
            hasValue = true;
            //Otherwise create an empty text field for the value.
        } else {

            HBox propertyEntryBox = createPropertyEntryBox(addNewListener, fields, systemGenerated, packageOntologyService, propertyName, labels, applyButtonValidationListener,
                    artifact, complexPropertyName, propertyBox, messages, "");
            propertyValuesBox.getChildren().add(propertyEntryBox);
        }

        propertyBox.getChildren().add(propertyValuesBox);
        HBox.setHgrow(propertyValuesBox, Priority.ALWAYS);
        //If the ontology allows for more than one value on a property add a button to add more fields.
        if (maxOccurs > 1) {
            addNewButton.setDisable(!hasValue);
            propertyBox.getChildren().add(addNewButton);

            addNewButton.setOnAction(arg0 -> {
                HBox propertyEntryBox = createPropertyEntryBox(addNewListener, fields, systemGenerated, packageOntologyService, propertyName, labels, applyButtonValidationListener,
                        artifact, complexPropertyName, propertyBox, messages, "");
                propertyValuesBox.getChildren().add(propertyEntryBox);
                addNewButton.setDisable(true);
                propertyEntryBox.getChildren().get(0).requestFocus();
            });
        } else { //hack to make spacing look better when resizing window
            addNewButton.setDisable(true);
            addNewButton.setVisible(false);
            propertyBox.getChildren().add(addNewButton);
        }

        getChildren().add(propertyBox);
    }

    private String formatPropertyValue(String propertyName, String originalValue, PackageOntologyService packageOntologyService) {


        if (packageOntologyService.isSizeProperty(null, propertyName)) {
            final DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
            double doubleValue = Double.parseDouble(originalValue);
            int i=0;
            int test = 1;
            while(doubleValue >= test*1000 && i < sizeLabels.length - 1){
                test *= 1000;
                i++;
            }
            String sizeLabel = (doubleValue == 1) ? " Byte" : sizeLabels[i];
            return twoDecimalForm.format(doubleValue / test) + sizeLabel;
        }
        //there is currently no other formatting required for other fields.
        return originalValue;
    }

    private HBox createPropertyEntryBox(EmptyFieldButtonDisableListener addNewListener, Set<StringProperty> fields, boolean systemGenerated, PackageOntologyService packageOntologyService,
                                        String propertyName, Labels labels, ApplyButtonValidationListener applyButtonValidationListener, PackageArtifact artifact, String complexPropertyName,
                                        HBox propertyBox, Messages messages, String value) {

        HBox propertyEntryBox = new HBox(2);

        TextInputControl propertyControl;

        if (packageOntologyService.propertySupportsMultipleLines(artifact, propertyName)) {
            final TextArea propertyField = new TextArea();
            propertyField.setPrefRowCount(5);
            propertyField.setWrapText(true);

            //The following code handles the growing of the text area to fit the text. It starts as 5 rows of text and is locked to never go below that size.
            //This code only handles changes when the box is already visible for handling when the box is first visible see above.
            propertyField.textProperty().addListener((observableValue, s, newValue) -> {

                //Account for the padding inside of the text area
                final int textAreaPaddingSize = 20;

                // This code can only be executed after the window is shown, because it needs to be laid out to get sized, and for the stylesheet to be set:

                //Hide the vertical scroll bar, the scroll bar sometimes appears briefly when resizing, this prevents that.
                ScrollBar scrollBarv = (ScrollBar)propertyField.lookup(".scroll-bar:vertical");
                if (scrollBarv != null ) {
                    scrollBarv.setDisable(true);
                }

                if (newValue.length() > 0) {
                    // Perform a lookup for an element with a css class of "text"
                    // This will give the Node that actually renders the text inside the
                    // TextArea
                    final Node text = propertyField.lookup(".text");

                    //Text will be null if the view has text already when the pop up is being shown
                    //TODO: In java 8 this can be avoided by calling applyCSS
                    if (text != null) {
                        //If the text area is now bigger then starting size increase the size to fit the text plus the space for padding.
                        if (text.getBoundsInLocal().getHeight() + textAreaPaddingSize > startingTextHeight) {

                            propertyField.setPrefHeight(text.getBoundsInLocal().getHeight() + textAreaPaddingSize);
                        } else { //Otherwise set to the minimum size, this needs to be checked everytime in case the user selects all the text and deletes it
                            propertyField.setPrefHeight(startingTextHeight);
                        }
                    } else {
                        //In the case where the text is set before the view is laid out we measure the text and then set the size to it.
                        double textHeight = computeTextHeight(newValue, 170.0) + textAreaPaddingSize;
                        if (textHeight + textAreaPaddingSize > startingTextHeight) {
                             propertyField.setPrefHeight(textHeight);
                        } else {
                            propertyField.setPrefHeight(startingTextHeight);
                        }
                    }
                } else {
                    propertyField.setPrefHeight(startingTextHeight);
                }
            });
            propertyControl = propertyField;
        } else {
            TextField propertyField = new TextField();
            propertyControl = propertyField;
        }

        StringProperty propertyValue = new SimpleStringProperty();

        if (value != null && !value.isEmpty()) {
            //Remove any formatting the ontology has put on the field.
            String unFormattedValue = packageOntologyService.getUnFormattedProperty(artifact, complexPropertyName, propertyName, value);

            //Add any UI formatting that needs to be done to the field.
            String formattedValue = formatPropertyValue(propertyName, unFormattedValue, packageOntologyService);
            if (formattedValue.equals(unFormattedValue)) {
                propertyValue.bind(propertyControl.textProperty());
            } else {
                propertyValue.setValue(unFormattedValue);
            }

            propertyControl.setText(formattedValue);

        } else {
            propertyValue.bind(propertyControl.textProperty());
        }

        propertyControl.textProperty().addListener(addNewListener);
        //addNewListener.fieldAdded();

        fields.add(propertyValue);
        propertyControl.setPrefWidth(250);
        propertyControl.setEditable(!systemGenerated);
        if (packageOntologyService.isDateProperty(null, propertyName)) {
            propertyControl.setPromptText(labels.get(Labels.LabelKey.UTC_HINT));
        }

        if(!propertyControl.isEditable()){
            propertyControl.getStyleClass().add(UNEDITABLE_PROPERTY_VALUE);
        }
        propertyEntryBox.getChildren().add(propertyControl);
        HBox.setHgrow(propertyControl, Priority.ALWAYS);
        final Label userInputImageLabel = new Label();
        userInputImageLabel.setPrefWidth(18);

        //Add validation listener to the field to perform validation
        BooleanProperty fieldValidProperty = new SimpleBooleanProperty(true);
        fieldValidProperty.addListener(applyButtonValidationListener);

        //Add validation listener to the field to perform validation
        propertyValue.addListener(new PropertyValidationListener(artifact, complexPropertyName, propertyName, this, propertyBox, userInputImageLabel, packageOntologyService, messages, fieldValidProperty));

        propertyEntryBox.getChildren().add(userInputImageLabel);

        return  propertyEntryBox;
    }

    private double computeTextHeight(String text, double wrappingWidth) {
        Text helper = new Text();
        helper.setText(text);
        helper.setFont(Font.loadFont(App.class.getResource("/fonts/OpenSans-Regular.ttf").toExternalForm(), 14));
        helper.setWrappingWidth((int)wrappingWidth);
        return helper.getLayoutBounds().getHeight();
    }
}
