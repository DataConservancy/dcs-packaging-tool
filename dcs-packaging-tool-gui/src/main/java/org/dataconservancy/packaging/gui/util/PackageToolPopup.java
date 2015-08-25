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
package org.dataconservancy.packaging.gui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.*;
import org.dataconservancy.packaging.gui.CssConstants;

import javafx.scene.control.OverrunStyle;

import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;

/**
 * A helper class that creates a popup with a uniform look for the entire application. 
 * The popup is a simple border pane that contains a title header, a content section and then controls in the footer.
 * All sections are optional however a header title and content should be set. 
 * Default properties are for the popup to auto hide and auto fix.
 */
public class PackageToolPopup extends Stage implements CssConstants, EventHandler<MouseEvent>, ChangeListener<Boolean> {
    
    BorderPane popupView;
    Label titleLabel;

    private double offsetX;
    private double offsetY;
    private boolean moveable = true;
    private boolean autohide = false;

    public PackageToolPopup() {
        super();
        
        popupView = new BorderPane();
        popupView.getStylesheets().add("/css/app.css");
        popupView.getStyleClass().add(PACKAGE_TOOL_POPUP_CLASS);
        
        HBox header = new HBox();

        //Sets a max width for the title so that the window doesn't continue to grow. The beginning of the title will be ellipsized for long titles.
        titleLabel = new Label();
        titleLabel.setMaxWidth(400);
        titleLabel.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
        header.getStyleClass().add(PACKAGE_TOOL_POPUP_HEADER_CLASS);
        header.getChildren().add(titleLabel);
        
        popupView.setTop(header);

        header.setOnMouseDragged(this);
        header.setOnMousePressed(this);

        initStyle(StageStyle.UNDECORATED);
        initModality(Modality.APPLICATION_MODAL);
        setIconified(false);
        centerOnScreen();
        Scene scene = new Scene(popupView);
        scene.getStylesheets().add("/css/app.css");
        setScene(scene);
        resizableProperty().setValue(Boolean.FALSE);

        focusedProperty().addListener(this);
    }
    
    /**
     * Sets the title of the popup.
     * @param titleText The string that should be displayed as the header in the popup.
     */
    public void setTitleText(String titleText) {
        titleLabel.setText(titleText);
    }

    /**
     * Sets the owner window of this popup
     * @param owner The window that "owns" this window.
     */
    public void setOwner(final Window owner) {
        if (owner != null && getOwner() == null) {
            initOwner(owner);
            owner.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> PackageToolPopup.this.hide());
        }
    }
    
    /**
     * Sets the view that should make up the body of the popup.
     * @param content The node that will be displayed in the body of the popup.
     */
    public void setContent(Node content) {
        content.getStyleClass().add(PACKAGE_TOOL_POPUP_CONTENT_CLASS);
        popupView.setCenter(content);

    }
    
    /**
     * Sets the view that will display in the footer of the popup.
     * @param footer The node that will be displayed in the footer of the popup.
     */
    public void setFooter(Node footer) {
        popupView.setBottom(footer);
    }


    /**
     * Sets whether this popup window should be moveable or not.  Default is moveable.
     * @param moveable boolean
     */
    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }


    public void setAutohide(boolean autohide) {
        this.autohide = autohide;
        if (autohide) {
            initModality(Modality.NONE);
        } else {
            initModality(Modality.APPLICATION_MODAL);
        }
    }

    /**
     * Event handler for moving the window around.  If moveable is false, it will do nothing.
     * @param mouseEvent the MouseEvent
     */
    @Override
    public void handle(MouseEvent mouseEvent) {
        if (!moveable) return;
        double x = mouseEvent.getScreenX();
        double y = mouseEvent.getScreenY();

        if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            offsetX = x - this.getX();
            offsetY = y - this.getY();
        } else if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            setX(x - offsetX);
            setY(y - offsetY);
        }
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
        if (autohide && !newValue) {
            hide();
        }
    }

    public void show(double x, double y) {
        setX(x);
        setY(y);
        show();
    }


}