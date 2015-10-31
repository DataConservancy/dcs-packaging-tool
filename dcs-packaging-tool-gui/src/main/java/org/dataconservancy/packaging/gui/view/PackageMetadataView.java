/*
 * Copyright 2015 Johns Hopkins University
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

package org.dataconservancy.packaging.gui.view;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.util.WarningPopup;
import org.dataconservancy.packaging.tool.model.PackageMetadata;

import java.util.List;

/**
 * The view that shows the package metadata screen. In this view, the user will be able to select a name for the package,
 * select an ontology as well as put in other package specific metadata.
 */
public interface PackageMetadataView extends View<PackageMetadataPresenter> {

    /**
     * A text field that is used for entering the package name.
     *
     * @return The text field for entering the package name.
     */
    TextField getPackageNameField();

    /**
     * A drop down list view with a list of domain profiles
     *
     * @return list view of domain profiles
     */
    ComboBox<String> getDomainProfilesComboBox();

    /**
     * A button that adds the selected profile
     *
     * @return add profile button
     */
    Button getAddDomainProfileButton();

    /**
     * Scrolls the view back to the top of the window.
     */
    void scrollToTop();

    /**
     * Load the current domain profiles for the user to select
     *
     * @param domainProfileNames
     */
    void loadDomainProfileNames(List<String> domainProfileNames);

    /**
     * Add the selected domain profile as a removable label
     *
     * @param domainProfile
     */
    void addDomainProfileRemovableLabel(String domainProfile);

    /**
     * the VBox that contains removable lables.
     *
     * @return the vbox containing removablelabels.
     */
    VBox getDomainProfileRemovableLabelVBox();

    /**
     * Sets up the required fields in the form.
     * @param requiredPackageMetadataList
     */
    void setupRequiredFields(List<PackageMetadata> requiredPackageMetadataList);

    /**
     * Sets up the recommended fields in the form.
     * @param recommendedPackageMetadataList
     */
    void setupRecommendedFields(List<PackageMetadata> recommendedPackageMetadataList);

    /**
     * Sets up the optional fields in the form.
     * @param optionalPackageMetadataList
     */
    void setupOptionalFields(List<PackageMetadata> optionalPackageMetadataList);

    /**
     * Shows any status messages.
     * @param status
     */
    Label getErrorLabel();

    /**
     * Clears all the fields
     *
     */
    void clearAllFields();

    /**
     * Gets all the fields in the form to set their values in the PackageState
     * @return list of nodes in the form
     */
    List<Node> getAllFields();

    /**
     * shows the warning popup if user is going back to the beginning.
     *
     */
    void showWarningPopup();

    /**
     * gets the warning popup.
     * @return the warning popup
     */
    WarningPopup getWarningPopup();

    /**
     * Similar to {@code} addDomainProfileRemovableLabel, it adds a label for profile but in a way so the user can't
     * remove it.
     * @param domainProfile
     */
    void addDomainProfileLabel(String domainProfile);

    /**
     * Allows the presenter to know whether the form needs to be drawn or not.
     * @return boolean true or false
     */
    boolean isFormAlreadyDrawn();

    /**
     * Gets the file chooser where the user saves the package metadata
     * @return file chooser
     */
    FileChooser getPackageMetadataFileChooser();

    /**
     * Given a field name, it tells the presenter whether it's failed GUI validation or not.
     * @param fieldName
     * @return true or false
     */
    boolean hasFailedValidation(String fieldName);
}