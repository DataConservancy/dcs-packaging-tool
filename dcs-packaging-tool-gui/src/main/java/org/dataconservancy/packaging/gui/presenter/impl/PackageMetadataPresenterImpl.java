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

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.paint.Color;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.presenter.PackageGenerationPresenter;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.view.PackageGenerationView;
import org.dataconservancy.packaging.gui.view.PackageMetadataView;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.model.BagItParameterNames;
import org.dataconservancy.packaging.tool.model.BoremParameterNames;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageGenerationParametersBuilder;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.ParametersBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for the screen that will handle package metadata.
 *
 */
public class PackageMetadataPresenterImpl extends BasePresenterImpl implements PackageMetadataPresenter {
    private PackageMetadataView view;
    private PackageGenerationParametersBuilder packageGenerationParamsBuilder;
    private PackageGenerationParameters generationParams = null;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public PackageMetadataPresenterImpl(PackageMetadataView view) {
        super(view);
        this.view = view;

        view.setPresenter(this);
        bind();
    }

    @Override
    public void clear() {
        view.clearAllFields();
    }

    public Node display() {
        //Clear out any values from the previous run
        generationParams = null;
        //Setup help content and then rebind the base class to this view.
        view.setupHelp();
        setView(view);
        super.bindBaseElements();

        return view.asNode();        
    }

    private void bind() {
        view.getContinueButton().setOnAction(event -> {
            loadPackageGenerationParams();
            updateParamsFromForm();
            fillInMissingParams();
            getController().goToNextPage(Page.CREATE_NEW_PACKAGE);
        });
    }

    private void loadPackageGenerationParams() {
        String paramFilePath = controller.getDefaultPackageGenerationParametersFilePath();
        if (paramFilePath != null && !paramFilePath.isEmpty()) {
            File paramFile = new File(paramFilePath);
            if (paramFile.exists()) {
                try {
                    generationParams = packageGenerationParamsBuilder.buildParameters(new FileInputStream(paramFile));
                } catch (FileNotFoundException e) {
                    log.error("Error reading selected package parameters file: " + paramFilePath + " " + e.getMessage());
                } catch (ParametersBuildException e) {
                    log.error("Error creating params from file: " + paramFilePath + " " + e.getMessage());
                }
            }
        }
        
        //If the file is null attempt to load the built in resource file.
        if (generationParams == null) {
            InputStream fileStream = PackageMetadataPresenterImpl.class.getResourceAsStream("/packageGenerationParameters");
            if (fileStream != null) {
                try {
                    generationParams = packageGenerationParamsBuilder.buildParameters(fileStream);
                } catch (ParametersBuildException e) {
                    log.error("Error reading default params from file: " + e.getMessage());
                }
            }
            else {
                log.error("Error reading default params files. Couldn't find classpath file: /packageGenerationParameters");
            }
        }
        

        setViewToDefaults();
    }

    private void updateParamsFromForm() {
        updateSingleParam(BagItParameterNames.CONTACT_NAME, view.getContactNameTextField().getText());
        updateSingleParam(BagItParameterNames.CONTACT_EMAIL, view.getContactEmailTextField().getText());
        updateSingleParam(BagItParameterNames.CONTACT_PHONE, view.getContactPhoneTextField().getText());
        updateSingleParam(GeneralParameterNames.PACKAGE_NAME, view.getPackageNameField().getText());
        updateSingleParam(BagItParameterNames.EXTERNAL_IDENTIFIER, view.getExternalIdentifierTextField().getText());
    }

    private void updateSingleParam(String key, String value) {
        generationParams.removeParam(key);

        if (value != null && !value.isEmpty()) {
            generationParams.addParam(key, value);
        }
    }
    
    /**
     * If any required parameters are missing from the file fill them in with default values.
     */
    private void fillInMissingParams() {
        if (generationParams.getParam(GeneralParameterNames.PACKAGE_FORMAT_ID) == null ||
                generationParams.getParam(GeneralParameterNames.PACKAGE_FORMAT_ID).isEmpty()) {
            generationParams.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.BOREM.toString());            
        }
        
        if (generationParams.getParam(GeneralParameterNames.PACKAGE_NAME) == null || 
                generationParams.getParam(GeneralParameterNames.PACKAGE_NAME).isEmpty()) {
            generationParams.addParam(GeneralParameterNames.PACKAGE_NAME, getPackageName());
        }
        
        if (generationParams.getParam(GeneralParameterNames.CONTENT_ROOT_LOCATION) == null ||
                generationParams.getParam(GeneralParameterNames.CONTENT_ROOT_LOCATION).isEmpty()) {
            if (controller.getContentRoot() != null) {
                generationParams.addParam(GeneralParameterNames.CONTENT_ROOT_LOCATION, controller.getContentRoot().getPath());
            }
        }

        if (generationParams.getParam(BagItParameterNames.PKG_BAG_DIR) == null || 
                generationParams.getParam(BagItParameterNames.PKG_BAG_DIR).isEmpty()) {
            generationParams.addParam(BagItParameterNames.PKG_BAG_DIR, getPackageName());
        }
        
        if (generationParams.getParam(BagItParameterNames.CONTACT_EMAIL) == null ||
                generationParams.getParam(BagItParameterNames.CONTACT_EMAIL).isEmpty()) {
            if (view.getContactEmailTextField().getText() != null && !view.getContactEmailTextField().getText().isEmpty()) {
                generationParams.addParam(BagItParameterNames.CONTACT_EMAIL, view.getContactEmailTextField().getText());
            }
        }
        
        if (generationParams.getParam(BagItParameterNames.CONTACT_NAME) == null ||
                generationParams.getParam(BagItParameterNames.CONTACT_NAME).isEmpty()) {
            if (view.getContactNameTextField().getText() != null && !view.getContactNameTextField().getText().isEmpty()) {
                generationParams.addParam(BagItParameterNames.CONTACT_NAME, view.getContactNameTextField().getText());
            }
        }
        
        if (generationParams.getParam(BagItParameterNames.CONTACT_PHONE) == null ||
                generationParams.getParam(BagItParameterNames.CONTACT_PHONE).isEmpty()) {
            if (view.getContactPhoneTextField().getText() != null && !view.getContactPhoneTextField().getText().isEmpty()) {
                generationParams.addParam(BagItParameterNames.CONTACT_PHONE, view.getContactPhoneTextField().getText());
            }
        }
        
        if (generationParams.getParam(BagItParameterNames.BAGIT_PROFILE_ID) == null ||
                generationParams.getParam(BagItParameterNames.BAGIT_PROFILE_ID).isEmpty()) {
            generationParams.addParam(BagItParameterNames.BAGIT_PROFILE_ID, "http://dataconservancy.org/formats/data-conservancy-pkg-0.9");
        }
        
        if (generationParams.getParam(GeneralParameterNames.PACKAGE_LOCATION) == null ||
                generationParams.getParam(GeneralParameterNames.PACKAGE_LOCATION).isEmpty()) {
            if (controller.getPackageState().getOutputDirectory() != null) {
                generationParams.addParam(GeneralParameterNames.PACKAGE_LOCATION, controller.getPackageState().getOutputDirectory().getAbsolutePath());
            }
        }
        
        if (generationParams.getParam(BagItParameterNames.EXTERNAL_IDENTIFIER) == null ||
                generationParams.getParam(BagItParameterNames.EXTERNAL_IDENTIFIER).isEmpty()) {
            if (view.getExternalIdentifierTextField().getText() != null && !view.getExternalIdentifierTextField().getText().isEmpty()) {
                generationParams.addParam(BagItParameterNames.EXTERNAL_IDENTIFIER, view.getExternalIdentifierTextField().getText());

            } else {
                generationParams.addParam(BagItParameterNames.EXTERNAL_IDENTIFIER, "none");
            }
        }
        
        if (generationParams.getParam(BagItParameterNames.BAG_COUNT) == null ||
                generationParams.getParam(BagItParameterNames.BAG_COUNT).isEmpty()) {
            generationParams.addParam(BagItParameterNames.BAG_COUNT, "1 of 1");
        }
        
        if (generationParams.getParam(BagItParameterNames.BAG_GROUP_ID) == null ||
                generationParams.getParam(BagItParameterNames.BAG_GROUP_ID).isEmpty()) {
            generationParams.addParam(BagItParameterNames.BAG_GROUP_ID, "none");            
        }
    }
    
    private void setViewToDefaults() {

        
        if (generationParams.getParam(BagItParameterNames.CONTACT_EMAIL) != null
                && !generationParams.getParam(BagItParameterNames.CONTACT_EMAIL).isEmpty()) {
            view.getContactEmailTextField().setText(generationParams.getParam(BagItParameterNames.CONTACT_EMAIL).get(0));
        }
        
        if (generationParams.getParam(BagItParameterNames.CONTACT_NAME) != null
                && !generationParams.getParam(BagItParameterNames.CONTACT_NAME).isEmpty()) {
            view.getContactNameTextField().setText(generationParams.getParam(BagItParameterNames.CONTACT_NAME, 0));
        }
        
        if (generationParams.getParam(BagItParameterNames.CONTACT_PHONE) != null 
                && !generationParams.getParam(BagItParameterNames.CONTACT_PHONE).isEmpty()) {
            view.getContactPhoneTextField().setText(generationParams.getParam(BagItParameterNames.CONTACT_PHONE, 0));
        }
        
        if (generationParams.getParam(BagItParameterNames.EXTERNAL_IDENTIFIER) != null
                && !generationParams.getParam(BagItParameterNames.EXTERNAL_IDENTIFIER).isEmpty()) {
            view.getExternalIdentifierTextField().setText(generationParams.getParam(BagItParameterNames.EXTERNAL_IDENTIFIER, 0));
        }
        

        

    }

    private String getPackageName() {
        return view.getPackageNameField().getText();
    }

    @Override
    public void setPackageGenerationParametersBuilder(PackageGenerationParametersBuilder packageParamsBuilder) {
        this.packageGenerationParamsBuilder = packageParamsBuilder;
    }

}
