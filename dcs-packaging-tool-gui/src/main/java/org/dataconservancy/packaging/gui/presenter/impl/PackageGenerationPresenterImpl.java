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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.presenter.PackageGenerationPresenter;
import org.dataconservancy.packaging.gui.view.PackageGenerationView;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Toggle;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * Implementation for the screen that will handle generating the actual package. Controls the user selecting packaging options,
 * selecting a basic directory and then generating a package. Will present the user with an option to see if they wish to generate another package,
 * with different options or return to the main screen. 
 */
public class PackageGenerationPresenterImpl extends BasePresenterImpl implements PackageGenerationPresenter {
    private PackageGenerationView view;
    private File outputDirectory = null;
    private PackageGenerationService packageGenerationService;
    private PackageGenerationParametersBuilder packageGenerationParamsBuilder;
    private PackageDescriptionBuilder packageDescriptionBuilder;
    private PackageGenerationParameters generationParams = null;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private GeneratePackageService backgroundService;

    public PackageGenerationPresenterImpl(PackageGenerationView view) {
        super(view);
        this.view = view;

        view.setPresenter(this);
        bind();
    }

    @Override
    public void clear() {
        //This presenter has no information to clear
    }

    public Node display() {
        //Clear out any values from the previous run
        view.getStatusLabel().setText("");
        view.getCurrentOutputDirectoryTextField().setText("");
        generationParams = null;
        loadPackageGenerationParams();
        
        //Setup help content and then rebind the base class to this view.
        view.setupHelp();
        setView(view);
        super.bindBaseElements();

        return view.asNode();        
    }

    private void bind() {
        if (backgroundService == null) {
            backgroundService = new BackgroundPackageService();
        }

        backgroundService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                if (Platform.isFxApplicationThread()) {
                    view.getProgressPopup().hide();
                    view.showSuccessPopup();
                }
                backgroundService.reset();
            }
        });

        backgroundService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {

                view.getProgressPopup().hide();
                if (workerStateEvent.getSource().getMessage() == null || workerStateEvent.getSource().getMessage().isEmpty()) {
                    Throwable e = workerStateEvent.getSource().getException();
                    view.getStatusLabel().setText(errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR) + " " + e.getMessage());
                } else {
                    view.getStatusLabel().setText(workerStateEvent.getSource().getMessage());
                }

                view.getStatusLabel().setTextFill(Color.RED);
                view.getStatusLabel().setVisible(true);
                backgroundService.reset();
            }
        });

        backgroundService.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                if (Platform.isFxApplicationThread()) {
                    view.getProgressPopup().hide();
                }
                view.getStatusLabel().setText(workerStateEvent.getSource().getMessage());
                view.getStatusLabel().setTextFill(Color.RED);
                view.getStatusLabel().setVisible(true);
                backgroundService.reset();
            }
        });

        //Handles the user pressing the button to set an output directory where the package will be saved.
        view.getSelectOutputDirectoryButton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                File file = controller.showOpenDirectoryDialog(view.getOutputDirectoryChooser());
                if (file != null) {
                    controller.setOutputDirectory(file);
                    view.getOutputDirectoryChooser().setInitialDirectory(controller.getOutputDirectory());
                    //Set the package location parameter based on the new output directory.
                    generationParams.addParam(GeneralParameterNames.PACKAGE_LOCATION, controller.getOutputDirectory().getAbsolutePath());

                    setOutputDirectory(true);
                }
            }
            
        });

        //Handles the user changing the package name
        view.getPackageNameField().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldVal, String newVal) {
                setOutputDirectory(true);
            }
        });
        
        //Handles the user pressing the no thanks link on the create another package popup. This will take the user
        //back to the home screen. 
        view.getNoThanksLink().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                if (view.getSuccessPopup() != null && view.getSuccessPopup().isShowing()) {
                    view.getSuccessPopup().hide();
                }
                controller.showHome(true);
            }            
        });
        
        //Handles the user pressing the create another package button on the create another package popup. This will 
        //dismiss the popup and keep the user on the screen.
        view.getCreateNewPackageButton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                if (view.getSuccessPopup() != null && view.getSuccessPopup().isShowing()) {
                    view.getSuccessPopup().hide();
                }
                //this will get reset later - need to make sure this is empty now
                generationParams.removeParam(BoremParameterNames.PKG_ORE_REM);
            }
            
        });

        // This listener is for choosing cancel when overwriting a package file, just closes the popup
        view.getCancelFileOverwriteButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                view.getFileOverwriteWarningPopup().hide();
            }
        });

        // This listener is for choosing to overwrite a package file; closes the window and proceeds
        view.getOkFileOverwriteButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                view.getFileOverwriteWarningPopup().hide();
                view.getStatusLabel().setVisible(false);
                view.getProgressPopup().show();
                backgroundService.setOverwriteFile(true);
                backgroundService.execute();
            }
        });
        
        //This listener changes what is shown in the output directory box when the archiving format is changed.
        view.getArchiveToggleGroup().selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            
            public void changed(ObservableValue<? extends Toggle> ov,
                                    Toggle toggle, Toggle archiveToggle) {
                if (archiveToggle != null) {
                    setOutputDirectory(true);

                    //Set the parameter for the archive format.
                    String archiveExtension = (String) archiveToggle.getUserData();
                    generationParams.removeParam(GeneralParameterNames.ARCHIVING_FORMAT);

                    if (!archiveExtension.isEmpty()) {
                        generationParams.addParam(GeneralParameterNames.ARCHIVING_FORMAT, archiveExtension);                                
                    }

                    //when we select zip or exploded as our archiving format, we must select 'none' as our compression
                    if (archiveExtension.equals("zip") || archiveExtension.equals("exploded")){
                        Toggle noCompressionToggle = getNoCompressionToggle();
                        if(noCompressionToggle != null && noCompressionToggle != view.getCompressionToggleGroup().getSelectedToggle()) {
                            view.getCompressionToggleGroup().selectToggle(noCompressionToggle);
                        }
                    }
                }                        
             }
        });
        
        //This listener changes what is shown in the output directory box when the compression format is changed.
        view.getCompressionToggleGroup().selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            
            public void changed(ObservableValue<? extends Toggle> ov,
                                    Toggle toggle, Toggle compressionToggle) {
                if (compressionToggle != null) {
                    setOutputDirectory(true);
                    
                    //Set the parameter for compression format.
                    String compressionExtension = (String) compressionToggle.getUserData();
                    generationParams.removeParam(GeneralParameterNames.COMPRESSION_FORMAT);

                    if (!compressionExtension.isEmpty()) {
                        generationParams.addParam(GeneralParameterNames.COMPRESSION_FORMAT, compressionExtension);
                    }
                }                        
             }
        });
        
        view.getMd5CheckBox().selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean oldValue,
                                Boolean newValue) {
                List<String> params = generationParams.getParam(BagItParameterNames.CHECKSUM_ALGORITHMS);

                if (newValue) {
                    if (params != null && !params.isEmpty() && !params.contains("md5")) {
                        params.add("md5");
                    }
                } else {
                    if (params != null && !params.isEmpty()) {
                        params.remove("md5");
                    }
                }
            }
            
        });
        
        view.getSHA1CheckBox().selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean oldValue,
                                Boolean newValue) {
                List<String> params = generationParams.getParam(BagItParameterNames.CHECKSUM_ALGORITHMS);

                if (newValue) {
                    if (params != null && !params.isEmpty() && !params.contains("sha1")) {
                        params.add("sha1");
                    }
                } else {
                    if (params != null && !params.isEmpty()) {
                        params.remove("sha1");
                    }
                }
            }
            
        });
        
        /*Handles when the continue button is pressed in the footer. 
        * In this case it creates package params based on the options selected, it then tries to generate a package and save it to the output directory.
        * If successful a popup is shown asking the user if they want to create another package, otherwise an error message is displayed informing the user what went wrong
        * and error is logged.
        */
        view.getContinueButton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                view.getStatusLabel().setVisible(false);
                if (Platform.isFxApplicationThread()) {
                    view.getProgressPopup().show();
                }
                backgroundService.setOverwriteFile(false);
                backgroundService.execute();
            }
        });
    }

    /*
     * Generates and saves the package to a file if an error occurs the error message is returned so it can be properly handled.
     */
    private String generateAndSavePackage() {
        //PackageDescription packageDescription = controller.getPackageDescription();
        PackageDescription packageDescription;
        try {
            FileInputStream fis = new FileInputStream(controller.getPackageDescriptionFile());
            packageDescription = packageDescriptionBuilder.deserialize(fis);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return errors.get(ErrorKey.PACKAGE_DESCRIPTION_BUILD_ERROR) + " " + e.getMessage();
        } catch (NullPointerException e) {
            log.error(e.getMessage());
            return errors.get(ErrorKey.PACKAGE_DESCRIPTION_BUILD_ERROR) + " " + e.getMessage();
        }

        Package createdPackage;
        //If we have all the objects we need attempt to create a package with the package generation service.
        if (generationParams != null && packageDescription != null) {
            try {
                createdPackage = packageGenerationService.generatePackage(packageDescription, generationParams);
            } catch (PackageToolException e) {
                log.error(e.getMessage());
                return errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR) + " " + e.getMessage();
            } catch (RuntimeException e) {
                log.error(e.getMessage());
                return errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR) + " "  + e.getMessage();
            }

        } else {
            log.error(errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR) + " generation params or package description was null.");
            return errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR);
        }

        if(!generationParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT, 0).equals("exploded")) {
            //If we've successfully generated a package, save the package to the provided output directory,
            //unless we wanted the package exploded, in which case there is no package file produced
            if (createdPackage != null) {
                File packageFile = getPackageFile();

                try {
                    if (createdPackage.isAvailable()) {
                        FileOutputStream fos = new FileOutputStream(packageFile);
                        InputStream packageStream = createdPackage.serialize();
                        IOUtils.copy(packageStream, fos);
                        fos.close();
                        packageStream.close();
                        createdPackage.cleanupPackage();
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                    return errors.get(ErrorKey.PACKAGE_GENERATION_SAVE);
                }

            } else {
                log.error(errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR) + " created package was null");
                return errors.get(ErrorKey.PACKAGE_GENERATION_CREATION_ERROR);
            }
        }

        return "";
    }

    private void loadPackageGenerationParams() {
        String paramFilePath = controller.getPackageGenerationParamsFilePath();
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
            InputStream fileStream = PackageGenerationPresenterImpl.class.getResourceAsStream("/defaultGenerationParams");
            if (fileStream != null) {
                try {
                    generationParams = packageGenerationParamsBuilder.buildParameters(fileStream);
                } catch (ParametersBuildException e) {
                    log.error("Error reading default params from file: " + e.getMessage());
                }
            }
            else {
                log.error("Error reading default params files. Couldn't find classpath file: /defaultGenerationParams");
            }
        }
        
        //As an absolute fall back if the parameters can't be loaded from anywhere set them in the code.
        if (generationParams == null) {
            loadDefaultParams();            
        }
        
        setViewToDefaults();
    }

    private void updateParamsFromForm() {
        updateSingleParam(BagItParameterNames.CONTACT_NAME, view.getContactNameTextField().getText());
        updateSingleParam(BagItParameterNames.CONTACT_EMAIL, view.getContactEmailTextField().getText());
        updateSingleParam(BagItParameterNames.CONTACT_PHONE, view.getContactPhoneTextField().getText());
        updateSingleParam(GeneralParameterNames.PACKAGE_NAME, view.getPackageNameField().getText());
        updateSingleParam(BagItParameterNames.EXTERNAL_IDENTIFIER, view.getExternalIdentifierTextField().getText());
        updateSingleParam(BagItParameterNames.PKG_BAG_DIR, view.getPackageNameField().getText());
        updateSingleParam(GeneralParameterNames.EXTERNAL_PROJECT_ID, view.getExternalProjectIdentifierProperty().getValue());
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
        
        if (generationParams.getParam(GeneralParameterNames.CHECKSUM_ALGORITHMS) == null ||
                generationParams.getParam(GeneralParameterNames.CHECKSUM_ALGORITHMS).isEmpty()) {
            List<String> checksumAlgs = new ArrayList<>();
            
            if (view.getMd5CheckBox().isSelected()) {
                checksumAlgs.add("md5");
            } 
            
            if (view.getSHA1CheckBox().isSelected()) {
                checksumAlgs.add("sha1");
            }
            
            if (checksumAlgs.isEmpty()) {
                checksumAlgs.add("md5");
            }
            
            generationParams.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlgs);
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
            if (controller.getOutputDirectory() != null) {
                generationParams.addParam(GeneralParameterNames.PACKAGE_LOCATION, controller.getOutputDirectory().getAbsolutePath());
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
    
    /**
     * This method is a last resort to load default parameters in code, if none of the file options could be loaded. 
     */
    private void loadDefaultParams() {
        
        view.getStatusLabel().setText(errors.get(ErrorKey.PARAM_LOADING_ERROR));
        view.getStatusLabel().setTextFill(Color.BLACK);
        view.getStatusLabel().setVisible(true);
        
        generationParams = new PackageGenerationParameters();
        generationParams.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.BOREM.toString());

        generationParams.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, "md5");
        if (controller.getContentRoot() != null) {
            generationParams.addParam(GeneralParameterNames.CONTENT_ROOT_LOCATION, controller.getContentRoot().getPath());
        }

        if (view.getArchiveToggleGroup().getSelectedToggle() != null) {
            String archiveExtension = (String) view.getArchiveToggleGroup().getSelectedToggle().getUserData();
            if (!archiveExtension.isEmpty()) {
                generationParams.addParam(GeneralParameterNames.ARCHIVING_FORMAT, archiveExtension);
            }
        }

        if (view.getCompressionToggleGroup().getSelectedToggle() != null) {
            String compressionExtension = (String) view.getCompressionToggleGroup().getSelectedToggle().getUserData();
            if (!compressionExtension.isEmpty()) {
                generationParams.addParam(GeneralParameterNames.COMPRESSION_FORMAT, compressionExtension);
            }
        }

        generationParams.addParam(BagItParameterNames.BAGIT_PROFILE_ID, "http://dataconservancy.org/formats/data-conservancy-pkg-0.9");
    }
    
    private void setViewToDefaults() {
        if (generationParams.getParam(GeneralParameterNames.COMPRESSION_FORMAT) != null 
                && !generationParams.getParam(GeneralParameterNames.COMPRESSION_FORMAT).isEmpty()) {
            for (Toggle compressionToggle : view.getCompressionToggleGroup().getToggles()) {
                if (compressionToggle.getUserData().equals(generationParams.getParam(GeneralParameterNames.COMPRESSION_FORMAT).get(0))) {
                    compressionToggle.setSelected(true);
                    break;
                }
            }
        }
        
        if (generationParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT) != null
                && !generationParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT).isEmpty()) {
            for (Toggle archivingToggle : view.getArchiveToggleGroup().getToggles()) {
                if (archivingToggle.getUserData().equals(generationParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT).get(0))) {
                    archivingToggle.setSelected(true);
                    break;
                }
            }
        }
        
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
        
        if (generationParams.getParam(BagItParameterNames.CHECKSUM_ALGORITHMS) != null 
                && !generationParams.getParam(BagItParameterNames.CHECKSUM_ALGORITHMS).isEmpty()) {
            for (String checksumParam : generationParams.getParam(BagItParameterNames.CHECKSUM_ALGORITHMS)) {
                if (checksumParam.equalsIgnoreCase("md5")) {
                    view.getMd5CheckBox().setSelected(true);
                } else if (checksumParam.equalsIgnoreCase("sha1")) {
                    view.getSHA1CheckBox().setSelected(true);
                }
            }
            
        }
        
        if (generationParams.getParam(GeneralParameterNames.PACKAGE_LOCATION) != null
                && !generationParams.getParam(GeneralParameterNames.PACKAGE_LOCATION).isEmpty()) {
            String filePath = generationParams.getParam(GeneralParameterNames.PACKAGE_LOCATION, 0);
            if (filePath != null && !filePath.isEmpty()) {
                File outputDirectory = new File(filePath);
                if (!outputDirectory.exists()) {
                    if (!outputDirectory.mkdirs()) {
                        // If the directory could not be created, reset outputDirectory, and display a warning
                        controller.setOutputDirectory(null);
                        view.getStatusLabel().setText(errors.get(ErrorKey.OUTPUT_DIR_NOT_CREATED_ERROR));
                        view.getStatusLabel().setTextFill(Color.RED);
                        view.getStatusLabel().setVisible(true);
                    } else {
                        controller.setOutputDirectory(outputDirectory);
                    }
                }
            }
        }
        
        setOutputDirectory(false);
    }

    private String getPackageName() {
        return view.getPackageNameField().getText();
    }

    private File getPackageFile() {
        String packageName = "pkg";

        if (generationParams.getParam(GeneralParameterNames.PACKAGE_NAME) != null &&
                !generationParams.getParam(GeneralParameterNames.PACKAGE_NAME).isEmpty()) {
            packageName = generationParams.getParam(GeneralParameterNames.PACKAGE_NAME, 0);
        }

        if (generationParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT) != null &&
                !generationParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT).isEmpty()) {
            packageName += "." + generationParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT, 0);
        }

        if (generationParams.getParam(GeneralParameterNames.COMPRESSION_FORMAT) != null &&
                !generationParams.getParam(GeneralParameterNames.COMPRESSION_FORMAT).isEmpty()) {
            packageName += "." + generationParams.getParam(GeneralParameterNames.COMPRESSION_FORMAT, 0);
        }

        File packageFile;
        if (controller.getOutputDirectory() != null) {
            packageFile = new File(controller.getOutputDirectory(), packageName);
        } else {
            packageFile = new File ("./", packageName);
        }

        return packageFile;
    }
    
    /**
     * Sets the output name of the file that will be saved based on the path of the output directory the package name,
     * and the archive format, and the compression format.
     */
    private void setOutputDirectory(boolean overrideStatus) {
        String currentOutput = "";
        String errorText = "";
        boolean hasPackageName = (getPackageName() != null && !getPackageName().isEmpty());

        if (!hasPackageName || controller.getOutputDirectory() == null) {
            if (hasPackageName) {
                errorText = errors.get(ErrorKey.OUTPUT_DIRECTORY_MISSING);
            } else if (controller.getOutputDirectory() != null) {
                errorText = errors.get(ErrorKey.PACKAGE_NAME_MISSING);
            } else {
                errorText = errors.get(ErrorKey.OUTPUT_DIRECTORY_AND_PACKAGE_NAME_MISSING);
            }
        }

        if (StringUtils.containsAny(getPackageName(), controller.getPackageFilenameIllegalCharacters())) {
            errorText = errors.get(ErrorKey.PACKAGE_FILENAME_HAS_ILLEGAL_CHARACTERS)
                    + "   " + controller.getPackageFilenameIllegalCharacters();
        }

        if (errorText.isEmpty()) {
            currentOutput = controller.getOutputDirectory().getAbsolutePath() + File.separator + getPackageName();

            if (view.getArchiveToggleGroup().getSelectedToggle() != null && !view.getArchiveToggleGroup().getSelectedToggle().getUserData().equals("exploded")) {
                currentOutput += "." + view.getArchiveToggleGroup().getSelectedToggle().getUserData();
            }

            if (view.getCompressionToggleGroup().getSelectedToggle() != null) {
                String compressionExtension = (String) view.getCompressionToggleGroup().getSelectedToggle().getUserData();
                if (!compressionExtension.isEmpty()) {
                    currentOutput += "." + compressionExtension;
                }
            }

            view.getStatusLabel().setVisible(false);
            view.getContinueButton().setDisable(false);
        } else {
            if (overrideStatus || !view.getStatusLabel().isVisible()) {
                view.getStatusLabel().setText(errorText);
                view.getStatusLabel().setTextFill(Color.RED);
                view.getStatusLabel().setVisible(true);
            }
            view.getContinueButton().setDisable(true);
        }

        // Warning for long filenames, won't prevent you from continuing but may cause an error when you actually save
        // Mostly affects Windows machines
        if (currentOutput.length() > 259) {
            view.getStatusLabel().setText(messages.formatFilenameLengthWarning(currentOutput.length()));
            view.getStatusLabel().setTextFill(Color.RED);
            view.getStatusLabel().setVisible(true);
        }

        view.getCurrentOutputDirectoryTextField().setText(currentOutput);
    }


    /**
     * we use this method to get the compression toggle corresponding to None - have to set this
     * when zip archive format is selected.
     * @return toggle
     */
    private Toggle getNoCompressionToggle(){
        List<Toggle> compressionToggles = view.getCompressionToggleGroup().getToggles();
        for (Toggle toggle : compressionToggles){
            String compressionExtension = (String) toggle.getUserData();
            if (compressionExtension.isEmpty()){//"None"
                return toggle;
            }
        }
        return null;
    }

    @Override
    public void setPackageGenerationService(PackageGenerationService packageGenerationService) {
        this.packageGenerationService = packageGenerationService;
    }

    @Override
    public void setPackageGenerationParametersBuilder(PackageGenerationParametersBuilder packageParamsBuilder) {
        this.packageGenerationParamsBuilder = packageParamsBuilder;
    }

    @Override
    public void setPackageDescriptionBuilder(PackageDescriptionBuilder packageDescriptionBuilder) {
        this.packageDescriptionBuilder = packageDescriptionBuilder;
    }

    //Method should only be used for testing, will run all code on the same thread to simplify the test.
    protected void setTestBackgroundService() {
        this.backgroundService = new AsyncPackageService();
        bind();
    }

    /*
     * Simple interface that shadows JavaFX service this is used so we can create our own instance to use in testing.
     */
    protected interface GeneratePackageService {
        void execute();

        void setOnFailed(EventHandler<WorkerStateEvent> handler);

        void setOnCancelled(EventHandler<WorkerStateEvent> handler);

        void setOnSucceeded(EventHandler<WorkerStateEvent> handler);

        void setOverwriteFile(boolean overwriteFile);

        void reset();
    }


    private class BackgroundPackageService implements GeneratePackageService {

        BackgroundService service;
        public BackgroundPackageService() {
            service = new BackgroundService();
        }

        @Override
        public void execute() {
            service.start();
        }

        @Override
        public void setOnFailed(EventHandler<WorkerStateEvent> handler) {
            service.setOnFailed(handler);
        }

        @Override
        public void setOnCancelled(EventHandler<WorkerStateEvent> handler) {
            service.setOnCancelled(handler);
        }

        @Override
        public void setOnSucceeded(EventHandler<WorkerStateEvent> handler) {
            service.setOnSucceeded(handler);
        }

        @Override
        public void setOverwriteFile(boolean overwriteFile) {
            service.setOverwriteFile(overwriteFile);
        }

        @Override
        public void reset() {
            service.reset();
        }

        private class BackgroundService extends  Service<Void> {
            private boolean overwriteFile = false;

           public void setOverwriteFile(boolean overwriteFile) {
               this.overwriteFile = overwriteFile;
           }

           @Override
           protected Task<Void> createTask() {
               return new Task<Void>() {
                   @Override
                   protected Void call() throws Exception {

                       //If any parameters weren't set in the user provided files we'll supply defaults
                       fillInMissingParams();

                       //Update params based on what's on the form
                       updateParamsFromForm();

                       File packageFile = getPackageFile();

                       if (controller.getOutputDirectory() == null) {
                           updateMessage(errors.get(ErrorKey.OUTPUT_DIRECTORY_MISSING));
                           cancel();
                       } else {
                           if (!packageFile.exists() || overwriteFile) {
                               String errorMessage = generateAndSavePackage();
                               if (!errorMessage.isEmpty()) {
                                   updateMessage(errorMessage);
                                   cancel();
                               }
                           } else {

                               Platform.runLater(new Runnable() {
                                   @Override
                                   public void run() {
                                       view.getProgressPopup().hide();
                                       view.showFileOverwriteWarningPopup();
                                   }
                               });
                               cancel();

                           }
                       }
                       return null;
                   }
               };
           }
        }
    }

    /*
     * Generate Package Service used for testing, this implementation mimics JavaFX service but operates on the same thread, to make it easier for testing. Also prevents
     * tests from needing to run on the JavaFX application thread.
     */
    private class AsyncPackageService implements GeneratePackageService {

        private EventHandler<WorkerStateEvent> cancelledHandler;
        private EventHandler<WorkerStateEvent> successHandler;
        private boolean overwriteFile;

        @Override
        public void execute() {
            AsyncWorker worker = new AsyncWorker();

            //If any parameters weren't set in the user provided files we'll supply defaults
            fillInMissingParams();

            //Update params based on what's on the form
            updateParamsFromForm();

            File packageFile = getPackageFile();

            if (controller.getOutputDirectory() == null) {

                worker.setMessage(errors.get(ErrorKey.OUTPUT_DIRECTORY_MISSING));
                worker.setState(Worker.State.CANCELLED);
                cancelledHandler.handle(new WorkerStateEvent(worker, WorkerStateEvent.WORKER_STATE_CANCELLED));
            } else {
               if (!packageFile.exists() || overwriteFile) {
                   String errorMessage = generateAndSavePackage();
                   if (!errorMessage.isEmpty()) {
                       worker.setMessage(errorMessage);
                       worker.setState(Worker.State.CANCELLED);
                       cancelledHandler.handle(new WorkerStateEvent(worker, WorkerStateEvent.WORKER_STATE_CANCELLED));
                   }
               } else {

                   Platform.runLater(new Runnable() {
                       @Override
                       public void run() {
                           view.getProgressPopup().hide();
                           view.showFileOverwriteWarningPopup();
                       }
                   });
                   worker.setState(Worker.State.CANCELLED);
                   cancelledHandler.handle(new WorkerStateEvent(worker, WorkerStateEvent.WORKER_STATE_CANCELLED));
               }
            }

            worker.setState(Worker.State.SUCCEEDED);
            successHandler.handle(new WorkerStateEvent(worker, WorkerStateEvent.WORKER_STATE_SUCCEEDED));
        }

        @Override
        public void setOnFailed(EventHandler<WorkerStateEvent> handler) {
        }

        @Override
        public void setOnCancelled(EventHandler<WorkerStateEvent> handler) {
            cancelledHandler = handler;
        }

        @Override
        public void setOnSucceeded(EventHandler<WorkerStateEvent> handler) {
            successHandler = handler;
        }

        @Override
        public void setOverwriteFile(boolean overwriteFile) {
            this.overwriteFile = overwriteFile;
        }

        @Override
        public void reset() {
        }

        private class AsyncWorker implements Worker {


            private String message;
            private State state;

            protected void setMessage(String message) {
                this.message = message;
            }

            protected void setState(State state) {
                this.state = state;
            }

            @Override
            public State getState() {
                return state;
            }

            @Override
            public ReadOnlyObjectProperty<State> stateProperty() {
                return null;
            }

            @Override
            public Object getValue() {
                return null;
            }

            @Override
            public ReadOnlyObjectProperty valueProperty() {
                return null;
            }

            @Override
            public Throwable getException() {
                return null;
            }

            @Override
            public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
                return null;
            }

            @Override
            public double getWorkDone() {
                return 0;
            }

            @Override
            public ReadOnlyDoubleProperty workDoneProperty() {
                return null;
            }

            @Override
            public double getTotalWork() {
                return 0;
            }

            @Override
            public ReadOnlyDoubleProperty totalWorkProperty() {
                return null;
            }

            @Override
            public double getProgress() {
                return 0;
            }

            @Override
            public ReadOnlyDoubleProperty progressProperty() {
                return null;
            }

            @Override
            public boolean isRunning() {
                return false;
            }

            @Override
            public ReadOnlyBooleanProperty runningProperty() {
                return null;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public ReadOnlyStringProperty messageProperty() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public ReadOnlyStringProperty titleProperty() {
                return null;
            }

            @Override
            public boolean cancel() {
                return false;
            }
        }
    }
}
