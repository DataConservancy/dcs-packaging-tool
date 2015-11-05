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
package org.dataconservancy.packaging.gui.presenter;

import javafx.scene.Node;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.InternalProperties;

/**
 * A presenter displays and updates a View in response to user actions.
 */
public interface Presenter {
    /**
     * Called every time the user navigates to the presenter.
     * @return a node which displays the presenter view.
     */
    Node display();

    /**
     * Clears out the presenter information.
     */
    void clear();

    /**
     * Getter and setter methods needed for every presenter. 
     * The controller handles navigation between views and presenters, as well as storing any state information of the package generation process.
     * @return the Controller
     */
    Controller getController();
    void setController(Controller controller);

    /**
     * Sets the internal properties object to use for the presenter.
     * InternalProperties is a helper class that loads strings from the internal properties resource bundle.
     * @param internalProperties  the InternalProperties object
     */
    void setInternalProperties(InternalProperties internalProperties);
}
