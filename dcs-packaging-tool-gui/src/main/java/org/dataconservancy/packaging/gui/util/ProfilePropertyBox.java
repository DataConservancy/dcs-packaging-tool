package org.dataconservancy.packaging.gui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
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

    public ProfilePropertyBox(PropertyConstraint propertyConstraint, Node node,
                              DomainProfileService profileService,
                              List<ProfilePropertyBox> nodePropertyBoxes) {
        this.propertyConstraint = propertyConstraint;

        if (propertyConstraint.getPropertyType().getPropertyValueType().equals(PropertyValueType.COMPLEX)) {
            getChildren().add(new Label(propertyConstraint.getPropertyType().getLabel()));
            subPropertyBoxes = new ArrayList<>();
            for (PropertyConstraint subConstraint : propertyConstraint.getPropertyType().getComplexPropertyConstraints()) {
                ProfilePropertyBox profilePropertyBox = new ProfilePropertyBox(subConstraint, node, profileService, nodePropertyBoxes);
                nodePropertyBoxes.add(profilePropertyBox);
                getChildren().add(profilePropertyBox);
            }
        } else {

            textPropertyBoxes = new ArrayList<>();

            List<Property> existingProperties = profileService.getProperties(node, propertyConstraint.getPropertyType());
            boolean readOnly = propertyConstraint.getPropertyType().isReadOnly();

            if (existingProperties != null) {
                for (Property property : existingProperties) {
                    String value = "";
                    switch (property.getPropertyType().getPropertyValueType()) {
                        case STRING:
                            value = property.getStringValue();
                            break;
                        case LONG:
                            value = String.valueOf(property.getLongValue());
                            break;
                        case DATE_TIME:
                            //TODO: Parse and format date time
                            break;
                    }
                    PropertyBox propertyBox = new PropertyBox(value, (
                        propertyConstraint.getPropertyType().getPropertyValueHint() ==
                            PropertyValueHint.MULTI_LINE_TEXT), readOnly);
                    textPropertyBoxes.add(propertyBox);
                    getChildren().add(propertyBox);
                }
            } else {
                PropertyBox propertyBox = new PropertyBox("", (
                    propertyConstraint.getPropertyType().getPropertyValueHint() ==
                        PropertyValueHint.MULTI_LINE_TEXT), readOnly);
                textPropertyBoxes.add(propertyBox);
                getChildren().add(propertyBox);
            }
        }

        if (propertyConstraint.getMaximum() > 1) {
            final Button addNewButton = new Button(
                TextFactory.getText(Labels.LabelKey.ADD_NEW_BUTTON));
            addNewButton.setMaxWidth(addNewButtonMaxWidth);
            addNewButton.setDisable(true);
            getChildren().add(addNewButton);

            final GroupPropertyChangeListener listener = new GroupPropertyChangeListener(addNewButton);

            getChildren().stream().filter(n -> n instanceof VBox).forEach(n -> addChangeListenerToSectionFields((VBox) n, listener));

            listener.changed(null, "n/a", "n/a");

            addNewButton.setOnAction(arg0 -> {
                ProfilePropertyBox profilePropertyBox = new ProfilePropertyBox(propertyConstraint, node, profileService, nodePropertyBoxes);
                                nodePropertyBoxes.add(profilePropertyBox);
                                getChildren().add(profilePropertyBox);
                int buttonIndex = getChildren().indexOf(addNewButton);

                getChildren().add(buttonIndex, profilePropertyBox);

                addChangeListenerToSectionFields(profilePropertyBox, listener);
                addNewButton.setDisable(true);
                requestFocusForNewGroup(profilePropertyBox);
            });
            Separator groupSeparator = new Separator();
            getChildren().add(groupSeparator);
        }
    }

    public List<String> getValues() {
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
     * @param group    The pane to add the listener to
     * @param listener The listener to attach to the TextInputControl
     */
    private void addChangeListenerToSectionFields(Pane group,
                                                  ChangeListener<? super String> listener) {
        for (javafx.scene.Node n : group.getChildren()) {
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
            for (PropertyBox textPropertyBox : textPropertyBoxes) {
                if (textPropertyBox.getValue() == null || textPropertyBox.getValue().isEmpty()) {
                    return true;
                }
            }

            for (ProfilePropertyBox profilePropertyBox : subPropertyBoxes) {
                for (PropertyBox textPropertyBox : profilePropertyBox.textPropertyBoxes) {
                    if (textPropertyBox.getValue() == null || textPropertyBox.getValue().isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
