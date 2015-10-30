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
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.presenter.PackageDescriptionPresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.view.impl.PackageArtifactWindowBuilder;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl.ArtifactPropertyContainer;
import org.dataconservancy.packaging.gui.view.impl.PackageDescriptionViewImpl.ArtifactRelationshipContainer;
import org.dataconservancy.packaging.tool.model.PackageArtifact;

import javafx.scene.control.Label;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;

import javax.swing.Popup;

/**
 * A view that shows the pacakge description tree, and displays a popup to show package artifact properties.
 * It will also display a popup if the package description isn't valid. 
 */
public interface PackageDescriptionView extends View<PackageDescriptionPresenter> {  
    
    /**
     * Gets the root element from the artifact tree.
     * @return  the root element
     */
    public TreeItem<PackageArtifact> getRoot();
    
    /**
     * Gets the artifact tree view that displays all the package artifacts. 
     * @return the artifact tree view
     */
    public TreeTableView<PackageArtifact> getArtifactTreeView();

    /**
     * Retrieves the stage that represents the window used for the editing package artifacts.
     * @return the stage that shows the artifact properties edit screen
     */
    public Stage getArtifactDetailsWindow();
    
    /**
     * Displays the artifact details window.
     * @param artifact  the Artifact
     * @param anchorNode The scene object the artifact details window should initially anchor too
     */
    public void showArtifactDetails(PackageArtifact artifact, Node anchorNode);

    /**
     * Gets the file chooser that's used for saving the pacakge description file. 
     * @return the file chooser
     */
    public FileChooser getPackageDescriptionFileChooser();

    /**
     * Gets the checkbox used to determine if the full path should be shown or not
     * @return  he checkbox
     */
    public CheckBox getFullPathCheckBox();
    
    /**
     * Gets the hyperlink for canceling the property popup. 
     * @return  the hyperlink for canceling the property popup
     */
    public Hyperlink getCancelPopupHyperlink();
    
    /**
     * Gets the save property popup button, this button should apply any changes to the properties or relationships. 
     * @return  the save property popup button
     */
    public Button getApplyPopupButton();

    /**
     * Gets the container that holds the text fields representing the properties. 
     * @return the container that holds the text fields representing the properties
     */
    public Map<String, ArtifactPropertyContainer> getArtifactPropertyFields();
    
    /**
     * Gets the container that holds the text fields representing the relationships. 
     * @return  the container that holds the text fields representing the relationships
     */
    public Set<ArtifactRelationshipContainer> getArtifactRelationshipFields();
    
    /**
     * Gets the artifact that is being displayed in the popup. 
     * @return    the artifact that is being displayed in the popup
     */
    public PackageArtifact getPopupArtifact();
    
    /**
     * Gets the warning popup that is diplayed to user in case of warnings or errors.
     * @return    the artifact that is being displayed in the popup
     */
    public PackageToolPopup getWarningPopup();
    
    /**
     * Displays the warning popup, this should be used when there is a warning or error that requires action from the user.
     * @param title The title of the popup
     * @param errorMessage The error message to display.
     * @param allowNegative A boolean flag that shows whether the user should be allowed to cancel the popup.
     * @param allowFutureHide A boolean flag that shows whether the user should be allowed to hide the popup in the future.
     */
    public void showWarningPopup(String title, String errorMessage, boolean allowNegative, boolean allowFutureHide);
    
    /**
     * Gets the button that is used for a positive action for the warning warning popup.
     * @return  the button that is used for a positive action for the warning warning popup
     */
    public Button getWarningPopupPositiveButton();

    /**
     * Gets the button that is used for a negative action for the warning popup.
     * @return the button that is used for a negative action for the warning popup.
     */
    public Button getWarningPopupNegativeButton();

    /**
     * Gets the hide future warning checkbox for the warning popup.
     * @return  the hide future warning checkbox
     */
    public CheckBox getHideFutureWarningPopupCheckbox();
    
    /**
     * Gets the error message label that appears at the top of the screen.
     * @return The error message label
     */
    public Label getErrorMessageLabel();

    CheckBox getShowIgnored();

    /**
     * Gets metadata inheritance button
     * @return A map of the inherited property checkboxes.
     */
    public Map<String, CheckBox> getInheritMetadataCheckBoxMap();

    /**
     * Gets the button to renable warnings
     * @return The button to reenable warnings
     */
    public Button getReenableWarningsButton();

    public void setupWindowBuilder(String disciplinePath);

    /**
     * Gets the refresh popup
     * @return refresh popup
     */
    PackageToolPopup getRefreshPopup();

    /**
     * Gets the button that is used for a positive action for the refresh popup.
     * @return  the button that is used for a positive action for the refres popup
     */
    public Button getRefreshPopupPositiveButton();

    /**
     * Gets the button that is used for a negative action for the refresh popup.
     * @return the button that is used for a negative action for the refresh popup.
     */
    public Button getRefreshPopupNegativeButton();
}