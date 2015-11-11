package org.dataconservancy.packaging.gui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.impl.PropertyFormatServiceImpl;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilePropertyBox extends VBox {
    PropertyConstraint propertyConstraint;
    List<PropertyBox> propertyBoxes;
    List<ProfilePropertyBox> subPropertyBoxes;
    private double addNewButtonMaxWidth = 200;
    private PropertyFormatService formatService;
    private final int prefWidth = 250;
    private DisciplineLoadingService disciplineLoadingService;

    public ProfilePropertyBox(PropertyConstraint propertyConstraint, Node node,
                              DomainProfileService profileService, DisciplineLoadingService disciplineLoadingService) {
        formatService = new PropertyFormatServiceImpl();

        this.propertyConstraint = propertyConstraint;
        this.disciplineLoadingService = disciplineLoadingService;

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

            propertyBoxes = new ArrayList<>();

            List<Property> existingProperties = profileService.getProperties(node, propertyConstraint.getPropertyType());

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
                    propertyBox.getPropertyInput().setPrefWidth(prefWidth);

                    propertyBoxes.add(propertyBox);
                    propertyValuesBox.getChildren().add(propertyBox.getView());
                    addChangeListenerToPropertyFields(propertyBox, listener);

                    listener.changed(null, "n/a", "n/a");

                }
            } else {
                PropertyBox propertyBox = generatePropertyBox("", editable, propertyConstraint.getPropertyType());
                propertyBox.getPropertyInput().setPrefWidth(prefWidth);

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
                    propertyBox.getPropertyInput().setPrefWidth(prefWidth);
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
                                return new DisciplinePropertyBox((String) initialValue, editable, disciplineLoadingService, prefWidth);
                            default:
                                return new TextPropertyBox(initialValue, editable, propertyType.getPropertyValueHint(), "");
                        }
                    }
            }

        }

        //If we dont' have a hint we just assume it's a text property.
        return new TextPropertyBox(initialValue, editable, null, "");
    }
    private void createChildProfilePropertyBoxes(List<ProfilePropertyBox> nodePropertyBoxes, Node node, DomainProfileService profileService, GroupPropertyChangeListener listener, VBox propertyValueBox) {
        for (PropertyConstraint subConstraint : propertyConstraint.getPropertyType().getComplexPropertyConstraints()) {
            ProfilePropertyBox subProfilePropertyBox = new ProfilePropertyBox(subConstraint, node, profileService, disciplineLoadingService);
            nodePropertyBoxes.add(subProfilePropertyBox);
            propertyValueBox.getChildren().add(subProfilePropertyBox);
            addChangeListenerToProfileBox(subProfilePropertyBox, listener);
        }
    }

    public List<Object> getValues() {
        return propertyBoxes.stream().map(PropertyBox::getValue).collect(Collectors.toList());
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
                for (ProfilePropertyBox profilePropertyBox : subPropertyBoxes) {
                    for (PropertyBox propertyBox : profilePropertyBox.propertyBoxes) {
                        if (propertyBox.getValue() != null) {
                            if (propertyBox.getValue() instanceof String && propertyBox.getValueAsString().isEmpty()) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
