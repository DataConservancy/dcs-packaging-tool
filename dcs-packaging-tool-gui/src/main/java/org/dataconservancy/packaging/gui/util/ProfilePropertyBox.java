package org.dataconservancy.packaging.gui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.impl.PropertyFormatServiceImpl;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilePropertyBox extends VBox {
    PropertyConstraint propertyConstraint;
    List<PropertyBox> textPropertyBoxes;
    List<ProfilePropertyBox> subPropertyBoxes;
    private double addNewButtonMaxWidth = 200;
    private PropertyFormatService formatService;

    public ProfilePropertyBox(PropertyConstraint propertyConstraint, Node node,
                              DomainProfileService profileService) {
        formatService = new PropertyFormatServiceImpl();

        this.propertyConstraint = propertyConstraint;

        setSpacing(6);

        HBox propertyLabelAndValueBox = new HBox(12);

        Label propertyNameLabel = new Label(propertyConstraint.getPropertyType().getLabel());
        propertyNameLabel.setPrefWidth(100);
        propertyNameLabel.setWrapText(true);
        propertyLabelAndValueBox.getChildren().add(propertyNameLabel);

        getChildren().add(propertyLabelAndValueBox);

        final VBox propertyValuesBox = new VBox(6);

        final Button addNewButton = new Button(
           TextFactory.getText(Labels.LabelKey.ADD_NEW_BUTTON));
        addNewButton.setMaxWidth(addNewButtonMaxWidth);
        addNewButton.setDisable(true);
        addNewButton.setAlignment(Pos.BOTTOM_RIGHT);

        final boolean editable = !propertyConstraint.getPropertyType().isReadOnly();

        final GroupPropertyChangeListener listener = new GroupPropertyChangeListener(addNewButton);

        if (propertyConstraint.getPropertyType().getPropertyValueType().equals(PropertyValueType.COMPLEX)) {
            subPropertyBoxes = new ArrayList<>();
            createChildProfilePropertyBoxes(subPropertyBoxes, node, profileService, listener, propertyValuesBox);
            getChildren().add(propertyValuesBox);

            if (propertyConstraint.getMaximum() > 1 || propertyConstraint.getMaximum() == -1) {

                getChildren().add(addNewButton);

                addNewButton.setOnAction(arg0 -> {
                    createChildProfilePropertyBoxes(subPropertyBoxes, node, profileService, listener, propertyValuesBox);

                    addNewButton.setDisable(true);
                    requestFocusForNewGroup(subPropertyBoxes.get(subPropertyBoxes.size()-1));
                });

                Separator groupSeparator = new Separator();
                getChildren().add(groupSeparator);
            }
        } else {

            textPropertyBoxes = new ArrayList<>();

            List<Property> existingProperties = profileService.getProperties(node, propertyConstraint.getPropertyType());

            if (existingProperties != null && !existingProperties.isEmpty()) {
                for (Property property : existingProperties) {
                    Object value;
                    if (property.getPropertyType().getPropertyValueHint() != null && property.getPropertyType().getPropertyValueHint().equals(PropertyValueHint.DATE_TIME)) {
                        value = property.getDateTimeValue();
                    } else {
                        value = formatService.formatPropertyValue(property);
                    }
                    PropertyBox propertyBox = new PropertyBox(value, editable, property.getPropertyType().getPropertyValueHint(), "");
                    propertyBox.getPropertyInput().setPrefWidth(250);

                    textPropertyBoxes.add(propertyBox);
                    propertyValuesBox.getChildren().add(propertyBox);
                    addChangeListenerToPropertyFields(propertyBox, listener);

                    listener.changed(null, "n/a", "n/a");

                }
            } else {
                PropertyBox propertyBox = new PropertyBox("", editable, propertyConstraint.getPropertyType().getPropertyValueHint(), "");
                propertyBox.getPropertyInput().setPrefWidth(250);

                textPropertyBoxes.add(propertyBox);
                addChangeListenerToPropertyFields(propertyBox, listener);
                listener.changed(null, "n/a", "n/a");

                propertyValuesBox.getChildren().add(propertyBox);
            }

            propertyLabelAndValueBox.getChildren().add(propertyValuesBox);

            if (propertyConstraint.getMaximum() > 1 || propertyConstraint.getMaximum() == -1) {
                propertyLabelAndValueBox.getChildren().add(addNewButton);

                addNewButton.setOnAction(arg0 -> {
                    PropertyBox propertyBox = new PropertyBox("", editable, propertyConstraint.getPropertyType().getPropertyValueHint(), "");
                    propertyBox.getPropertyInput().setPrefWidth(250);

                    propertyValuesBox.getChildren().add(propertyBox);
                    addChangeListenerToPropertyFields(propertyBox, listener);
                    addNewButton.setDisable(true);
                    requestFocusForNewGroup(propertyBox);
                });
            }
        }

    }

    private void createChildProfilePropertyBoxes(List<ProfilePropertyBox> nodePropertyBoxes, Node node, DomainProfileService profileService, GroupPropertyChangeListener listener, VBox propertyValueBox) {
        for (PropertyConstraint subConstraint : propertyConstraint.getPropertyType().getComplexPropertyConstraints()) {
            ProfilePropertyBox subProfilePropertyBox = new ProfilePropertyBox(subConstraint, node, profileService);
            nodePropertyBoxes.add(subProfilePropertyBox);
            propertyValueBox.getChildren().add(subProfilePropertyBox);
            addChangeListenerToProfileBox(subProfilePropertyBox, listener);
        }
    }

    public List<Object> getValues() {
        return textPropertyBoxes.stream().map(PropertyBox::getValue).collect(Collectors.toList());
    }

    public List<ProfilePropertyBox> getSubPropertyBoxes() {
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
    private void addChangeListenerToPropertyFields(PropertyBox propertyBox,
                                                   ChangeListener<? super String> listener) {

        if (propertyBox.getPropertyInput() instanceof TextInputControl) {
            ((TextInputControl) propertyBox.getPropertyInput()).textProperty().addListener(listener);
        }
    }

    private void addChangeListenerToProfileBox(ProfilePropertyBox profilePropertyBox,
                                                    ChangeListener<? super String> listener) {

        for (PropertyBox propertyBox : profilePropertyBox.textPropertyBoxes) {
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
            if (textPropertyBoxes != null) {
                for (PropertyBox textPropertyBox : textPropertyBoxes) {
                    if (textPropertyBox.getPropertyInput() instanceof TextInputControl) {
                        if (textPropertyBox.getValueAsString() == null ||
                            textPropertyBox.getValueAsString().isEmpty()) {
                            return true;
                        }
                    } else {
                        if (textPropertyBox.getValueAsDate() == null) {
                            return true;
                        }
                    }
                }
            }

            if (subPropertyBoxes != null) {
                for (ProfilePropertyBox profilePropertyBox : subPropertyBoxes) {
                    for (PropertyBox textPropertyBox : profilePropertyBox.textPropertyBoxes) {
                        if (textPropertyBox.getPropertyInput() instanceof TextInputControl) {
                            if (textPropertyBox.getValueAsString() == null ||
                                textPropertyBox.getValueAsString().isEmpty()) {
                                return true;
                            }
                        } else {
                            if (textPropertyBox.getValueAsDate() == null) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }
}
