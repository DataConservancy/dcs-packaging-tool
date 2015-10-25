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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.dataconservancy.packaging.gui.presenter.impl.OpenExistingPackagePresenterImpl;

/**
 * This view is an intermediary step in the workflow. If the user selects to open an existing package description this view is shown for them to supply a content directory.
 * This step was added to make the initial screen less confusing.
 */
public interface OpenExistingPackageView extends View<OpenExistingPackagePresenterImpl> {

    /**
     * The button for choosing a content directory that is the location of the package contents.
     * @return The button for choosing the content directory
     */
    Button getChooseContentDirectoryButton();

    /**
     * A text field that is used for displaying the currently selected directory. Note a text field
     * is used here to allow the user to scroll the text to see the entire path. The text field however is not
     * editable.
     * @return The TextField to use for entering the in content directory.
     */
    TextField getSelectedContentDirectoryTextField();

    /**
     * The button for choosing a package file.
     * @return The button for choosing the content directory
     */
    Button getChooseFileButton();

    /**
     * A text field that is used for displaying the currently selected file. Note a text field
     * is used here to allow the user to scroll the text to see the entire path. The text field however is not
     * editable.
     * @return The TextField to use for entering the in content directory.
     */
    TextField getChooseFileTextField();

    /**
     * Label for displaying an error message to the user, typically when a required field has not been selected.
     * @return Label for displaying an error message to the user
     */
    Label getErrorMessage();
}
