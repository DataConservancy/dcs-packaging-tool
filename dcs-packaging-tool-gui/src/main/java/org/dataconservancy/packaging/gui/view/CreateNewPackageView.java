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

package org.dataconservancy.packaging.gui.view;

import java.util.Map;

import javafx.scene.control.Button;

import org.dataconservancy.packaging.gui.presenter.CreateNewPackagePresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.tool.api.support.RulePropertiesManager;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * The view for creating a new package. This view gives the option of selecting a base directory to create a package from, or
 * loading an existing package description.
 */
public interface CreateNewPackageView extends View<CreateNewPackagePresenter> {

    /**
     * The button for choosing a base directory to generate a package description from.
     * @return The button for chossing the content directory
     */
    public Button getChooseContentDirectoryButton();
    
    /**
     * A text field that is used for displaying the currently selected directory. Note a text field
     * is used here to allow the user to scroll the text to see the entire path. The text field however is not
     * editable.
     * @return The TextField to use for entering the in base directory.
     */
    public TextField getSelectedBaseDirectoryTextField();

    /**
     * Label for displaying an error message to the user, typically when a required field has not been selected.
     * @return Label for displaying an error message to the user
     */
    public Label getErrorMessage();
    
    /**
     * A button for selecting an existing package description to load.
     * @return The button for selecting a package description file.
     */
    public Button getChoosePackageDescriptionButton();
    
    /**
     * A text field that displays the currently selected package description. Note a text field 
     * is used here to allow the user to scroll the text ot see the entire path. The text field however
     * is not editable.
     * @return The text field for displaying hte selected package description.
     */
    public TextField getSelectedPackageDescriptionTextField();    
    
    /**
     * This brings up input fields for defining any properties (such as project ID) to use during package description creation.
     * @param mgr The rules manager that will be used to determine undefined properties
     */
    public void promptForUndefinedProperties(RulePropertiesManager mgr);
    
    /**
     * This returns a map of property names (keys) and their associated value, as input by the user.
     * @return A map of the property values input by the user.
     */
    public Map<String, String> getPropertyValues();

    public void showProgressIndicatorPopUp();

    public PackageToolPopup getProgressIndicatorPopUp();
}
