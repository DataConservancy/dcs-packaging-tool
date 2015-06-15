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
import javafx.scene.control.Button;

/**
 * Keeps track of properties and if they are all valid enables the apply button, otherwise disables the button
 */
public class ApplyButtonValidationListener implements ChangeListener<Boolean> {
    private Button buttonToDisable;
    private int invalidFieldCount = 0;

    public ApplyButtonValidationListener(Button buttonToDisable)
    {
        this.buttonToDisable = buttonToDisable;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal) {
       if (newVal == null || !newVal) {
           buttonToDisable.setDisable(true);
           if (oldVal) {
               invalidFieldCount++;
           }
       } else {
           if (oldVal == null || !oldVal) {
               if (invalidFieldCount > 0) {
                   invalidFieldCount--;
               }
           }

           if (invalidFieldCount == 0) {
               buttonToDisable.setDisable(false);
           }
       }
    }

}
