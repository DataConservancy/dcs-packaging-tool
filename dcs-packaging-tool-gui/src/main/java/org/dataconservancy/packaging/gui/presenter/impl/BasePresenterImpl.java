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
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.view.View;

/**
 * Base class that controls event handlers for UI elements common to each screen. 
 * As well as gets and sets objects that are needed by each presenter.
 */
public abstract class BasePresenterImpl implements Presenter {
    
    private View<?> view;
    protected Labels labels;
    protected Messages messages;
    protected Errors errors;
    protected InternalProperties internalProperties;
    protected Controller controller;

    public BasePresenterImpl(View<?> view) {
        this.view = view;
    }
    
    protected void bindBaseElements() {
        view.getHeaderViewAboutLink().setOnAction(arg0 -> view.showAboutPopup());
        
        view.getHeaderViewHelpLink().setOnAction(arg0 -> view.showHelpPopup());

        bindCancelLink();
    }

    @Override
    public void setMessages(Messages messages) {
        this.messages = messages;        
    }

    @Override
    public void setErrors(Errors errors) {
        this.errors = errors;        
    }

    @Override
    public void setLabels(Labels labels) {
        this.labels = labels;        
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

    public void bindCancelLink() {
        view.getCancelLink().setOnAction(event -> getController().goToPreviousPage());
    }
}