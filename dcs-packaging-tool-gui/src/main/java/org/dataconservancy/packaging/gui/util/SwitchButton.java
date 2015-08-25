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
package org.dataconservancy.packaging.gui.util;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

/**
 * A helper class the creates the appearance of a two state switch button. The button takes two parameters on text and off text, these are the labels that will be 
 * displayed when the button is in it's two state. 
 */
public class SwitchButton extends Label
{
    private SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(true);
    
    /**
     * Basic constructor for creating the switch button.
     * @param onText The text to display when the button is in the "on" state.
     * @param offText The text to display when the button is in the "off" state.
     */
    public SwitchButton(final String onText, final String offText)
    {
        Button switchBtn = new Button();
        switchBtn.setPrefWidth(45);
        
        //When the button is pressed toggle the boolean state backing the button.
        switchBtn.setOnAction(t -> switchedOn.set(!switchedOn.get()));

        //Set the graphic of the label to be the button.
        setGraphic(switchBtn);

        //The listener for the boolean property backing the button. This is what actually performs the appearance of a toggle.
        switchedOn.addListener((ov, t, t1) -> {
            //If the button is "on" mode set the on text and the desired on appearance.
            if (t1)
            {
                setText(onText);
                setStyle("-fx-background-color: #48A79C; -fx-text-fill:white; -fx-padding: 0px 0px 0px 4px; -fx-background-radius: 6 6 6 6; -fx-background-insets: 0px 2px 0px 0px;");
                setContentDisplay(ContentDisplay.RIGHT);
            }
            else //Otherwise set the button "off" mode
            {
                setText(offText);
                setStyle("-fx-background-color: #EEEEDD; -fx-text-fill:black; -fx-padding: 0px 4px 0px 0px; -fx-background-radius: 6 6 6 6; -fx-background-insets: 0px 0px 0px 2px;");
                setContentDisplay(ContentDisplay.LEFT);
            }
        });

        switchedOn.set(false);
    }

    public SimpleBooleanProperty switchOnProperty() { return switchedOn; }
}