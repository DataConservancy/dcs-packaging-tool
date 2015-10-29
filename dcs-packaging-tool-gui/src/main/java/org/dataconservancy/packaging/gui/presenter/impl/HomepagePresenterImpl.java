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

package org.dataconservancy.packaging.gui.presenter.impl;

import javafx.scene.Node;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.presenter.HomepagePresenter;
import org.dataconservancy.packaging.gui.view.HomepageView;

/**
 * The implementation for the presenter that will handle the homepage button actions
 */
public class HomepagePresenterImpl extends BasePresenterImpl implements HomepagePresenter {

    private HomepageView view;

    public HomepagePresenterImpl(HomepageView view) {
        super(view);

        this.view = view;

        view.setPresenter(this);
        bind();
    }

    private void bind() {
        view.getCreateNewPackageButton().setOnAction(event -> {
            getController().setCreateNewPackage(true);
            getController().goToNextPage();
        });
        view.getOpenExistingPackageButton().setOnAction(event -> {
            getController().setCreateNewPackage(false);
            getController().goToNextPage();
        });
    }

    @Override
    public Node display() {
        //Setup help content and then rebind the base class to this view.
        view.setupHelp();
        setView(view);
        super.bindBaseElements();

        return view.asNode();
    }

    @Override
    public void clear() {

    }
}
