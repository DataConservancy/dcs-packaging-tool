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

import javafx.scene.Node;

import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.presenter.Presenter;

import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;

/**
 * A View lays out widgets. The presenter bound to the view manipulates the
 * widgets. The division of responsibility between the Presenter is flexible,
 * but the View should be mostly passive.
 * 
 * @param <T> The type of the presenter
 */
public interface View<T extends Presenter> {
    /**
     * Bind the view to a presenter. The presenter must be set in the View
     * constructor.
     * 
     * @param presenter the Presenter
     */
    void setPresenter(T presenter);

    /**
     * @return node containing the view
     */
    Node asNode();
    
    /**
     * Allow view access to the continue button that appears in the footer.
     * @return the continue button that appears in the footer
     */
    public Button getContinueButton();
    
    /**
     * Allows views access to the cancel link that appears in the footer.
     * @return the cancel link that appears in the footer
     */
    public Hyperlink getCancelLink();
    
    /**
     * Allows views access to the package name label that shows up in the footer
     * @return the package name label that shows up in the footer
     */
    public Label getPackageNameLabel();
    
    /**
     * Allows view access to the save button that appears in the footer.
     * @return The button that allows the user to save.
     */
    public Button getSaveButton();
    
    /**
     * Shows the help popup linked to in the header. 
     */
    public void showHelpPopup();
    
    /**
     * Shows the about popup linked to in the header.
     */
    public void showAboutPopup();
    
    /**
     * Allows for customizing the help popup contents
     * @param content A node representing the contents the help popup should display.
     */
    public void setHelpPopupContent(Node content);
    
    /**
     * Allows for customizing the about popup content
     * @param content A node representing the contents the about popup should display.
     */
    public void setAboutPopupContent(Node content);
    
    /**
     * Gets the about hyperlink that's displayed in the header.
     * @return the about hyperlink that's displayed in the header
     */
    public Hyperlink getHeaderViewAboutLink();
    
    /**
     * Gets the help hyperlink that's displayed in the header.
     * @return  the help hyperlink that's displayed in the header
     */
    public Hyperlink getHeaderViewHelpLink();
    
    /**
     * Sets the header view that the view should display popups for, this <em>must</em> be the same view held by the controller.
     * @param headerView the HeaderView
     */
    public void setHeaderView(HeaderView headerView);
    
    public void setupHelp();
    
    /**
     * Sets the help object to use for the presenter.
     * Help is a helper class that loads help file paths from a property files and then loads the help text from that file.
     * @param help the help object to use for the presenter
     */
    public void setHelp(Help help);
}
