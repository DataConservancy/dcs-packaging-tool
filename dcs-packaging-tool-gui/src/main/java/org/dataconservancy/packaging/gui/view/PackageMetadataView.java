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

import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;

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
    ListView<String> getDomainProfilesListView();

    /**
     * A button that adds the selected profile
     *
     * @return add profile button
     */
    Button getAddDomainProfileButton();

    /**
     * The list of domain profiles currently selected.
     *
     * @return the list of domain profiles user has selected.
     */
    List<String> getDomainProfileLabelsList();

    /**
     * Gets the contact information text field, that will be supplied in the bag.
     *
     * @return the contact information text field
     */
    TextField getContactEmailTextField();

    /**
     * Gets the contact name text field, that will be supplied in the bag.
     *
     * @return the contact name text field
     */
    TextField getContactNameTextField();

    /**
     * Gets the contact phone number text field, that will be supplied in the bag.
     *
     * @return The text field for entering in a contact phone number.
     */
    TextField getContactPhoneTextField();

    /**
     * Gets the keyword text field, that will be supplied in the bag.
     *
     * @return The text field for entering in a keyword
     */
    TextField getKeywordTextField();

    /**
     * Gets the list of labels that display the keywords
     *
     * @return The table with the keywords.
     */
    List<String> getKeywordsList();

    /**
     * Gets the text field for setting the external identifier if there is one.
     *
     * @return The text field for entering in an external identifier.
     */
    TextField getExternalIdentifierTextField();

    /**
     * Gets the text area for setting the external description
     *
     * @return the text area for external description
     */
    TextArea getExternalDescriptionTextArea();

    /**
     * Gets the text field for setting the internal sender identifier.
     *
     * @return The text field for entering the internal sender identifier.
     */
    TextField getInternalSenderIdentifierTextField();

    /**
     * Gets the text area for setting the internal sender description
     *
     * @return the text area for external description
     */
    TextArea getInternalSenderDescriptionTextArea();

    /**
     * Gets the list of labels that display the keywords
     *
     * @return The table with the keywords.
     */
    List<String> getInternalIdentifiersList();

    /**
     * Gets the text field for setting the source organization.
     *
     * @return The text field for entering the source organization.
     */
    TextField getSourceOrganizationTextField();

    /**
     * Gets the text field for setting the organization address.
     *
     * @return The text field for entering the organization address.
     */
    TextField getOrganizationAddressTextField();

    /**
     * Gets the text field for setting the bag count.
     *
     * @return The text field for entering the bag count.
     */
    TextField getBagCountTextField();

    /**
     * Gets the text field for setting the bag group identifier.
     *
     * @return The text field for entering the bag group identifier.
     */
    TextField getBagGroupIdentifierTextField();

    /**
     * Gets the text field for setting the rights.
     *
     * @return The text field for entering the rights.
     */
    TextField getRightsTextField();

    /**
     * Gets the text field for setting the rights uri.
     *
     * @return The text field for entering the rights uri.
     */
    TextField getRightsUriTextField();

    /**
     * Gets the list of rights uri
     *
     * @return The list of rights uris.
     */
    List<String> getRightsUriList();

    /**
     * Gets the date picker for setting the bagging date.
     *
     * @return The date picker for entering bagging date.
     */
    DatePicker getBaggingDateDatePicker();

    /**
     * Gets the text field for setting the bag size.
     *
     * @return The text field for entering the bag size.
     */
    TextField getBagSizeTextField();

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
     * Add the keyword as a removable label
     *
     * @param keyword
     */
    void addKeywordRemovableLabel(String keyword);

    /**
     * Add the id as a removable label
     *
     * @param id
     */
    void addInternalSenderIdentifierRemovableLabel(String id);

    /**
     * Add the rights uri as a removable label
     *
     * @param rightsUri
     */
    void addRightsUriRemovableLabel(String rightsUri);

    /**
     * Determines whether to show the package metadata section or not
     *
     * @param show
     */
    void showBottomContent(boolean show);

    /**
     * Clears all the fields
     *
     */
    void clearAllFields();


}