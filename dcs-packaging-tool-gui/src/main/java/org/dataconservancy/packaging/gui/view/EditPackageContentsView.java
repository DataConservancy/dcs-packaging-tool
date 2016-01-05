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

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import org.dataconservancy.packaging.gui.presenter.EditPackageContentsPresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.util.ProfilePropertyBox;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.util.UserDefinedPropertyBox;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A view that shows the pacakge description tree, and displays a popup to show package artifact properties.
 * It will also display a popup if the package description isn't valid. 
 */
public interface EditPackageContentsView extends View<EditPackageContentsPresenter> {
    
    /**
     * Gets the root element from the artifact tree.
     * @return  the root element
     */
    TreeItem<Node> getRoot();
    
    /**
     * Gets the artifact tree view that displays all the package artifacts. 
     * @return the artifact tree view
     */
    TreeTableView<Node> getArtifactTreeView();

    /**
     * Retrieves the stage that represents the window used for the editing package artifacts.
     * @return the stage that shows the artifact properties edit screen
     */
    Stage getNodePropertiesWindow();
    
    /**
     * Displays the artifact details window.
     * @param treeNode  the node
     * @param anchorNode The scene object the artifact details window should initially anchor too
     */
    void showNodePropertiesWindow(Node treeNode, javafx.scene.Node anchorNode);


    /**
     * Gets the checkbox used to determine if the full path should be shown or not
     * @return  he checkbox
     */
    CheckBox getFullPathCheckBox();
    
    /**
     * Gets the hyperlink for canceling the property popup. 
     * @return  the hyperlink for canceling the property popup
     */
    Hyperlink getCancelPopupHyperlink();
    
    /**
     * Gets the save property popup button, this button should apply any changes to the properties or relationships. 
     * @return  the save property popup button
     */
    Button getApplyPopupButton();

    /**
     * Gets the container that holds the text fields representing the properties. 
     * @return the container that holds the text fields representing the properties
     */
    List<ProfilePropertyBox> getProfilePropertyBoxes();

    /**
     * Gets the container that holds the user defined properties.
     * @return the container that holds the text fields representing user defined properties
     */
    List<UserDefinedPropertyBox> getUserDefinedPropertyBoxes();
    
    /**
     * Gets the artifact that is being displayed in the popup. 
     * @return    the artifact that is being displayed in the popup
     */
    Node getPopupNode();
    
    /**
     * Gets the warning popup that is diplayed to user in case of warnings or errors.
     * @return    the artifact that is being displayed in the popup
     */
    PackageToolPopup getWarningPopup();
    
    /**
     * Displays the warning popup, this should be used when there is a warning or error that requires action from the user.
     * @param title The title of the popup
     * @param errorMessage The error message to display.
     * @param allowNegative A boolean flag that shows whether the user should be allowed to cancel the popup.
     * @param allowFutureHide A boolean flag that shows whether the user should be allowed to hide the popup in the future.
     */
    void showWarningPopup(String title, String errorMessage,
                          boolean allowNegative, boolean allowFutureHide);
    
    /**
     * Gets the button that is used for a positive action for the warning warning popup.
     * @return  the button that is used for a positive action for the warning warning popup
     */
    Button getWarningPopupPositiveButton();

    /**
     * Gets the button that is used for a negative action for the warning popup.
     * @return the button that is used for a negative action for the warning popup.
     */
    Button getWarningPopupNegativeButton();

    /**
     * Gets the hide future warning checkbox for the warning popup.
     * @return  the hide future warning checkbox
     */
    CheckBox getHideFutureWarningPopupCheckbox();

    CheckBox getShowIgnored();

    /**
     * Gets metadata inheritance button
     * @return A map of the inherited property checkboxes.
     */
    Map<PropertyType, CheckBox> getInheritMetadataCheckBoxMap();

    /**
     * Gets the button to renable warnings
     * @return The button to reenable warnings
     */
    Button getReenableWarningsButton();

    void setupWindowBuilder();

    /**
     * Gets the refresh popup
     * @return refresh popup
     */
    PackageToolPopup getRefreshPopup();

    /**
     * Gets the button that is used for a positive action for the refresh popup.
     * @return  the button that is used for a positive action for the refres popup
     */
    Button getRefreshPopupPositiveButton();

    /**
     * Gets the button that is used for a negative action for the refresh popup.
     * @return the button that is used for a negative action for the refresh popup.
     */
    Button getRefreshPopupNegativeButton();

    /**
     * Gets the results of the refresh operation that's used to merge the tree if the result is accepted.
     * @return The map containing the results of the comparison generated by the refresh operation.
     */
    Map<Node, NodeComparison> getRefreshResult();

    /**
     * Gets the progress popup used when the tree properties are being validated.
     * @return The progres dialog popup that lets the user know the tree is being validated.
     */
    ProgressDialogPopup getValidationProgressPopup();

    /**
     * Gets the set of ids that are missing files.
     * @return The set of ids that are missing file or an empty set if all files are present.
     */
    Set<URI> getMissingFileNodes();
}