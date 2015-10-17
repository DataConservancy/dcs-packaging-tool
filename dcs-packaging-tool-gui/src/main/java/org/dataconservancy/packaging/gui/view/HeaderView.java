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

import javafx.scene.control.Hyperlink;
import org.dataconservancy.packaging.gui.Page;

/**
 * The HeaderView appears at the top of every page, and indicates the position in the application as well as providing the 
 */
public interface HeaderView {
    /**
     * Highlights the correct segment of the location bar in the header.
     * @param nextPosition The next page to be highlighted, will automatically remove old highlights
     */
    public void highlightNextPage(Page page);
    
    /**
     * Gets the hyperlink that will show a popup containing help information when pressed.
     * @return The hyperlink for displaying help text.
     */
    public Hyperlink getHelpLink();
    
    /**
     * Gets the hyperlink that will show a popup containing information describing the screen when pressed.
     * @return The hyperlink for the displaying the about text.
     */
    public Hyperlink getAboutLink();
}