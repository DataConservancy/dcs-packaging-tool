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

package org.dataconservancy.packaging.gui.presenter.impl;

import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.Errors;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.view.View;
import org.dataconservancy.packaging.tool.model.RDFTransformException;

import java.io.IOException;

/**
 * Base class that controls event handlers for UI elements common to each screen. 
 * As well as gets and sets objects that are needed by each presenter.
 */
public abstract class BasePresenterImpl implements Presenter {
    
    private View<?> view;
    protected InternalProperties internalProperties;
    protected Controller controller;

    public BasePresenterImpl(View<?> view) {
        this.view = view;
    }
    
    protected void bindBaseElements() {
        view.getHeaderViewAboutLink().setOnAction(arg0 -> view.showAboutPopup());
        
        view.getHeaderViewHelpLink().setOnAction(arg0 -> view.showHelpPopup());

        view.getCancelLink().setOnAction(event -> onBackPressed());

        view.getContinueButton().setOnAction(event -> onContinuePressed());
    }

    @Override
    public void setInternalProperties(InternalProperties internalProperties) {
        this.internalProperties = internalProperties;
    }
    
    @Override
    public Controller getController() {
        return controller;
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }
    
    public void setView(View<?> view) {
        this.view = view;
    }

    public void onBackPressed() {
        try {
            getController().savePackageStateFile();
        } catch (IOException | RDFTransformException e) {
            view.getErrorLabel().setText(TextFactory.getText(Errors.ErrorKey.IO_CREATE_ERROR));
        }
        getController().goToPreviousPage();
    }

    public void onContinuePressed() {
        try {
            getController().savePackageStateFile();
        } catch (IOException | RDFTransformException e) {
            view.getErrorLabel().setText(TextFactory.getText(Errors.ErrorKey.IO_CREATE_ERROR));
        }
        getController().goToNextPage();
    }
}