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

import org.dataconservancy.packaging.gui.presenter.impl.OpenExistingPackagePresenterImpl;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * If the user selects to open an existing package, this view is shown.
 * 
 * Text fields are used to display file paths so a user an scroll the text to
 * see the entire path. The text field however is not editable.
 */
public interface OpenExistingPackageView extends View<OpenExistingPackagePresenterImpl> {

    /**
     * The button for choosing an in progress package state file.
     * 
     * @return The button for choosing the file.
     */
    Button getChoosePackageStateFileButton();

    /**
     * A text field that is used for displaying the currently selected package
     * state file.
     * 
     * 
     * @return The TextField to display the file.
     */
    TextField getChoosePackageStateFileTextField();

    /**
     * The button for choosing a content directory that is the location of the
     * package contents.
     * 
     * @return The button for choosing the directory
     */
    Button getChooseExplodedPackageDirectoryButton();

    /**
     * A text field that is used for displaying the currently selected directory
     * of an exploded package.
     * 
     * @return The TextField to display the directory.
     */
    TextField getChooseExplodedPackageDirectoryTextField();

    /**
     * The button for choosing a serialized package.
     * 
     * @return The button for choosing the file
     */
    Button getChoosePackageFileButton();
    
    /**
     * The button for choosing directory a serialized package will be extracted to.
     * 
     * @return The button for choosing the directory
     */
    Button getChoosePackageStagingDirectoryButton();
    
    /**
     * A text field that is used for displaying the currently selected directory
     * to stage package files.
     * 
     * @return The TextField to display the directory.
     */
    TextField getChoosePackageStagingDirectoryTextField();

    /**
     * A text field that is used for displaying the currently selected package
     * file.
     * 
     * @return The TextField to display the file.
     */
    TextField getChoosePackageFileTextField();
}
