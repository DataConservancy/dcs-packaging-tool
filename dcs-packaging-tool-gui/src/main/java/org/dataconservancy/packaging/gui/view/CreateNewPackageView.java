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

import org.dataconservancy.packaging.gui.presenter.CreateNewPackagePresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;

import javafx.scene.control.Button;
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
    Button getChooseContentDirectoryButton();
    
    /**
     * A text field that is used for displaying the currently selected directory. Note a text field
     * is used here to allow the user to scroll the text to see the entire path. The text field however is not
     * editable.
     * @return The TextField to use for entering the in base directory.
     */
    TextField getChooseContentDirectoryTextField();

    void showProgressIndicatorPopUp();

    PackageToolPopup getProgressIndicatorPopUp();
}
