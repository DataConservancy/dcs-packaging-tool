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

import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;

import javafx.scene.control.RadioButton;
import org.dataconservancy.packaging.gui.presenter.PackageGenerationPresenter;

import javafx.scene.control.CheckBox;

import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Hyperlink;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Label;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;

/**
 * The view that shows the package generation screen. This screen allows the user to select package creation options,
 * a directory to save the file to, and the ability to then generate a package. If the package generation is successful the view shows a popup with
 * options for creating another package or starting over.
 */
public interface PackageGenerationView extends View<PackageGenerationPresenter> {

    /**
     * Gets the directory chooser for setting the output directory.
     * @return  the directory chooser
     */
    DirectoryChooser getOutputDirectoryChooser();

    /**
     * Gets the text field that displays the current output directory. 
     * Note: A text field is used so the user can scroll the text to view the entire path. The text field
     * however is not editable.
     * @return the text field that displays the current output directory.
     */
    TextField getCurrentOutputDirectoryTextField();
    
    /**
     * Returns the button that is used to select an output directory.
     * @return  the button that is used to select an output directory
     */
    Button getSelectOutputDirectoryButton();

    /**
     * A label that is used to display messages to the user, mostly used for displaying error messages.
     * @return   a   label that is used to display messages to the user
     */
    Label getStatusLabel();
    
    /**
     * Shows a popup when package generation was successful.
     */
    void showSuccessPopup();

    /**
     * Shows a popup when about to save and the package file already exists
     */
    void showFileOverwriteWarningPopup();
    
    /**
     * Gets the popup that is shown when package generation was successful.
     * @return The popup that is shown when package generation is successful, can be null if it wasn't shown.
     */
    PackageToolPopup getSuccessPopup();

    /**
     * Gets the popup that is shown when a package file might be overwritten
     * @return The popup that is shown when a package file is about to be saved, but to an existing file
     */
    PackageToolPopup getFileOverwriteWarningPopup();
    
    /**
     * Gets the no thanks link that appears on the package generation success popup. 
     * @return the no thanks link that appears on the package generation success popup.
     */
    Hyperlink getNoThanksLink();

    /**
     * Gets the create new package button that appears on the package generation success popup.
     * @return   the create new package button that appears on the package generation success popup.
     */
    Button getCreateNewPackageButton();

    /**
     * Gets the button for canceling a file overwrite that appears on the file overwrite popup
     * @return  the button for canceling a file overwrite that appears on the file overwrite popup
     */
    Button getCancelFileOverwriteButton();

    /**
     * Gets the button for OK'ing a file overwrite that appears on the file overwrite popup
     * @return the button for OK'ing a file overwrite that appears on the file overwrite popup
     */
    Button getOkFileOverwriteButton();
    
    /**
     * Gets the archive format toggle group. 
     * @return the archive format toggle group
     */
    ToggleGroup getArchiveToggleGroup();
    
    /**
     * Gets the compression format toggle group.
     * @return  the compression format toggle group.
     */
    ToggleGroup getCompressionToggleGroup();
    
    /**
     * Gets the md5 checksum checkbox.
     * @return The checkbox for selecting md5 generation.
     */
    CheckBox getMd5CheckBox();
    
    /**
     * Gets the sha1 checksum checkbox.
     * @return The checkbox for selecting sha1 generation.
     */
    CheckBox getSHA1CheckBox();

    /**
     * Gets the serialization format toggle group.
     * @return  the serialization format toggle group.
     */
    ToggleGroup getSerializationToggleGroup();

    /**
     * Gets the JSON format checkbox.
     * @return The checkbox for selecting JSON format.
     */
    RadioButton getJSONRadioButton();

    /**
     * Gets the XML format checkbox.
     * @return The checkbox for selecting XML format.
     */
    RadioButton getXMLRadioButton();

    /**
     * Gets the sha1 checksum checkbox.
     * @return The checkbox for selecting Turtle format.
     */
    RadioButton getTurtleRadioButton();
    
    /**
     * Gets the progress dialog popup that's used when the package is being generated.
     * @return The popup used to display package generation progress.
     */
    PackageToolPopup getProgressPopup();

    /**
     * Scrolls the view back to the top of the window.
     */
    void scrollToTop();

    void loadAvailableProjects(String availableProjectsFilePath);
}