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
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
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

    void setupStaticFields();

    void setupRecommendedFields(List<PackageMetadata> recommendedPackageMetadataList);

    void setupOptionalFields(List<PackageMetadata> optionalPackageMetadataList);

    void showStatus(String status);

    /**
     * Clears all the fields
     *
     */
    void clearAllFields();

    List<Node> getAllFields();


}