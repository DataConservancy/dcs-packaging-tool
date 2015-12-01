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

import java.util.List;

import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.util.WarningPopup;
import org.dataconservancy.packaging.tool.model.PackageMetadata;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

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
     * Scrolls the view back to the top of the window.
     */
    void scrollToTop();

    /**
     * Load the current domain profiles for the user to select
     *
     * @param domainProfileNames The list of domain profile names to provide to the user
     */
    void loadDomainProfileNames(List<String> domainProfileNames);

    /**
     * Sets up the required fields in the form.
     * @param requiredPackageMetadataList This PackageMetadata that is required to be entered.
     */
    void setupRequiredFields(List<PackageMetadata> requiredPackageMetadataList);

    /**
     * Sets up the recommended fields in the form.
     * @param recommendedPackageMetadataList The package metadata that's recommended to be entered
     */
    void setupRecommendedFields(List<PackageMetadata> recommendedPackageMetadataList);

    /**
     * Sets up the optional fields in the form.
     * @param optionalPackageMetadataList The package metadata that's optional for the user.
     */
    void setupOptionalFields(List<PackageMetadata> optionalPackageMetadataList);

    /**
     * Gets the label that is used to show any error message at the top.
     * @return the errorLabel
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
    List<Node> getAllDynamicFields();

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
     * Allows the presenter to know whether the form needs to be drawn or not.
     * @return boolean true or false
     */
    boolean isFormAlreadyDrawn();

    /**
     * Given a field name, it tells the presenter whether it's failed GUI validation or not.
     * @param fieldName The name of the field that failed validation
     * @return true or false
     */
    boolean hasFailedValidation(String fieldName);
}