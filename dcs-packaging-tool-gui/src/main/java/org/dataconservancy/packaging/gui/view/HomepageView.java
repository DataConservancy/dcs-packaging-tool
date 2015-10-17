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

package org.dataconservancy.packaging.gui.view;

import javafx.scene.control.Button;
import org.dataconservancy.packaging.gui.presenter.HomepagePresenter;

/**
 * Created by pmeyer on 10/14/15.
 */
public interface HomepageView extends View<HomepagePresenter> {

    /**
     * Button that takes the user to the create new package page.
     * @return button
     */
    public Button getCreateNewPackageButton();

    /**
     * Button that takes the user to the page to open an existing package.
     * @return button
     */
    public Button getOpenExistingPackageButton();

}
