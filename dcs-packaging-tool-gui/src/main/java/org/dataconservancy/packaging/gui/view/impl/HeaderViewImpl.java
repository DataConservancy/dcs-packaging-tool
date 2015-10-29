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

package org.dataconservancy.packaging.gui.view.impl;

import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.view.HeaderView;

/**
 * The HeaderView appears at the top of every page, and indicates the position in the application as well as providing the 
 */
public class HeaderViewImpl extends VBox implements HeaderView, CssConstants {

    private HBox locationBar;
    private Hyperlink helpLink;
    private Hyperlink aboutLink;
    
    public HeaderViewImpl(Labels labels) {

        HBox titleBar = new HBox();
        titleBar.getStyleClass().add(VIEW_HEADER_CLASS);
        titleBar.setPrefHeight(58);
        titleBar.setAlignment(Pos.CENTER_RIGHT);

        //Create the help and about links that will appear at the top of the screen.
        helpLink = new Hyperlink(labels.get(LabelKey.HELP_LABEL));
        titleBar.getChildren().add(helpLink);
        
        aboutLink = new Hyperlink(labels.get(LabelKey.ABOUT_LABEL));
        titleBar.getChildren().add(aboutLink);

        getChildren().add(titleBar);
        
        //Create the location bar and use css to set which location is highlighted.
        locationBar = new HBox();
        locationBar.getStyleClass().add(VIEW_LOCATION_CLASS);
        
        for (Page page : Page.values()) {
            Label pageLabel = new Label(labels.get(page.getLabelKey()));
            switch (page) {
                case HOMEPAGE:
                    break;
                case CREATE_NEW_PACKAGE:
                    locationBar.getChildren().add(pageLabel);
                    pageLabel.getStyleClass().add(PAGE_ONE_SELECTED);
                    break;
                 case DEFINE_RELATIONSHIPS:
                    pageLabel.getStyleClass().add(PAGE_TWO_UNSELECTED);
                    locationBar.getChildren().add(pageLabel);
                    break;
                case GENERATE_PACKAGE:
                    pageLabel.getStyleClass().add(PAGE_THREE_UNSELECTED);
                    locationBar.getChildren().add(pageLabel);
                    break;
                default:
                    //Set no style if we don't have a page for it
                    break;
            }
        }
        
        getChildren().add(locationBar);
    }
    
    @Override
    public void highlightNextPage(Page page) {
        switch (page) {
            case HOMEPAGE:
                // Don't show the location bar in the homepage
                locationBar.setVisible(false);
                break;
            case PACKAGE_METADATA:
                locationBar.setVisible(true);
                //Remove the old selection, occurs on cancel or finish
                locationBar.getChildren().get(2).getStyleClass().removeAll(PAGE_THREE_SELECTED);
                locationBar.getChildren().get(2).getStyleClass().add(PAGE_THREE_UNSELECTED);

                //This happens if cancel was selected
                locationBar.getChildren().get(1).getStyleClass().removeAll(PAGE_TWO_SELECTED);
                locationBar.getChildren().get(1).getStyleClass().add(PAGE_TWO_UNSELECTED);

                //Select the new page
                locationBar.getChildren().get(0).getStyleClass().removeAll(PAGE_ONE_UNSELECTED);
                locationBar.getChildren().get(0).getStyleClass().add(PAGE_ONE_SELECTED);
                break;
            case EXISTING_PACKAGE_METADATA:
                locationBar.setVisible(true);
                //Remove the old selection, occurs on cancel or finish
                locationBar.getChildren().get(2).getStyleClass().removeAll(PAGE_THREE_SELECTED);
                locationBar.getChildren().get(2).getStyleClass().add(PAGE_THREE_UNSELECTED);

                //This happens if cancel was selected
                locationBar.getChildren().get(1).getStyleClass().removeAll(PAGE_TWO_SELECTED);
                locationBar.getChildren().get(1).getStyleClass().add(PAGE_TWO_UNSELECTED);

                //Select the new page
                locationBar.getChildren().get(0).getStyleClass().removeAll(PAGE_ONE_UNSELECTED);
                locationBar.getChildren().get(0).getStyleClass().add(PAGE_ONE_SELECTED);
                break;
            case CREATE_NEW_PACKAGE:
                locationBar.setVisible(true);
                //Remove the old selection, occurs on cancel or finish
                locationBar.getChildren().get(2).getStyleClass().removeAll(PAGE_THREE_SELECTED);
                locationBar.getChildren().get(2).getStyleClass().add(PAGE_THREE_UNSELECTED);

                //This happens if cancel was selected
                locationBar.getChildren().get(1).getStyleClass().removeAll(PAGE_TWO_SELECTED);
                locationBar.getChildren().get(1).getStyleClass().add(PAGE_TWO_UNSELECTED);

                //Select the new page
                locationBar.getChildren().get(0).getStyleClass().removeAll(PAGE_ONE_UNSELECTED);
                locationBar.getChildren().get(0).getStyleClass().add(PAGE_ONE_SELECTED);
                break;
            case DEFINE_RELATIONSHIPS:
                locationBar.setVisible(true);
                //Remove the old selection
                locationBar.getChildren().get(0).getStyleClass().removeAll(PAGE_ONE_SELECTED);
                locationBar.getChildren().get(0).getStyleClass().add(PAGE_ONE_UNSELECTED);

                //Handle returning from page 3
                locationBar.getChildren().get(2).getStyleClass().removeAll(PAGE_THREE_SELECTED);
                locationBar.getChildren().get(2).getStyleClass().add(PAGE_THREE_UNSELECTED);

                //Select the new page
                locationBar.getChildren().get(1).getStyleClass().removeAll(PAGE_TWO_UNSELECTED);
                locationBar.getChildren().get(1).getStyleClass().add(PAGE_TWO_SELECTED);
                break;
            case GENERATE_PACKAGE:
                locationBar.setVisible(true);
                //Remove the old selection
                locationBar.getChildren().get(1).getStyleClass().removeAll(PAGE_TWO_SELECTED);
                locationBar.getChildren().get(1).getStyleClass().add(PAGE_TWO_UNSELECTED);

                //Select the new page
                locationBar.getChildren().get(2).getStyleClass().removeAll(PAGE_THREE_UNSELECTED);
                locationBar.getChildren().get(2).getStyleClass().add(PAGE_THREE_SELECTED);
                break;
            case OPEN_EXISTING_PACKAGE:
                locationBar.setVisible(true);
                //Remove the old selection, occurs on cancel or finish
                locationBar.getChildren().get(2).getStyleClass().removeAll(PAGE_THREE_SELECTED);
                locationBar.getChildren().get(2).getStyleClass().add(PAGE_THREE_UNSELECTED);

                //This happens if cancel was selected
                locationBar.getChildren().get(1).getStyleClass().removeAll(PAGE_TWO_SELECTED);
                locationBar.getChildren().get(1).getStyleClass().add(PAGE_TWO_UNSELECTED);

                //Select the new page
                locationBar.getChildren().get(0).getStyleClass().removeAll(PAGE_ONE_UNSELECTED);
                locationBar.getChildren().get(0).getStyleClass().add(PAGE_ONE_SELECTED);
                break;
            default:
                //Set no style if we don't have a page for it
                break;

        }
    }

    @Override
    public Hyperlink getHelpLink() {
        return helpLink;
    }

    @Override
    public Hyperlink getAboutLink() {
        return aboutLink;
    }
}