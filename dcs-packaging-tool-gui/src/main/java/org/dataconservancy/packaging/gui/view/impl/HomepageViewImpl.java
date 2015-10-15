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

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.presenter.HomepagePresenter;
import org.dataconservancy.packaging.gui.view.HomepageView;

/**
 * The homepage view with two options: create a new package or open an existing one, each option takes the user through
 * a different path.
 */
public class HomepageViewImpl extends BaseViewImpl<HomepagePresenter> implements HomepageView, CssConstants {

    private Button createNewPackageButton;
    private Button openExistingPackageButton;

    public HomepageViewImpl(Labels labels) {
        super(labels);
        createNewPackageButton = new Button(labels.get(Labels.LabelKey.CREATE_NEW_PACKAGE));
        openExistingPackageButton = new Button(labels.get(Labels.LabelKey.OPEN_EXISTING_PACKAGE));

        getContinueButton().setVisible(false);
        getSaveButton().setVisible(false);

        VBox content = new VBox();
        content.setSpacing(32);
        content.getStyleClass().add(HOMEPAGE_VIEW_CLASS);

        setCenter(content);

        content.getChildren().add(createNewPackageButton);
        content.getChildren().add(openExistingPackageButton);

    }

    @Override
    public Button getCreateNewPackageButton() {
        return createNewPackageButton;
    }

    @Override
    public Button getOpenExistingPackageButton() {
        return openExistingPackageButton;
    }

    @Override
    public void setupHelp() {
        Label helpText = new Label(help.get(Help.HelpKey.HOMEPAGE_HELP));
        helpText.setMaxWidth(300);
        helpText.setWrapText(true);
        helpText.setTextAlignment(TextAlignment.CENTER);
        setHelpPopupContent(helpText);
    }
}
