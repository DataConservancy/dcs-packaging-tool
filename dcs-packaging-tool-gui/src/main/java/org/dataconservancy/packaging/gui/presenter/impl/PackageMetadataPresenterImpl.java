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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.services.PackageMetadataService;
import org.dataconservancy.packaging.gui.util.RemovableLabel;
import org.dataconservancy.packaging.gui.view.PackageMetadataView;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Implementation for the screen that will handle package metadata.
 */
public class PackageMetadataPresenterImpl extends BasePresenterImpl implements PackageMetadataPresenter {
    private PackageMetadataView view;
    private PackageMetadataService packageMetadataService;
    private boolean existingPackage = false;
    private static final Logger LOG = LoggerFactory.getLogger(PackageMetadataPresenterImpl.class);

    public PackageMetadataPresenterImpl(PackageMetadataView view) {
        super(view);
        this.view = view;

        view.setPresenter(this);
    }

    @Override
    public void clear() {
        view.clearAllFields();
    }

    public Node display() {
        //Setup help content and then rebind the base class to this view.
        view.setupHelp();
        bind();
        setView(view);
        super.bindBaseElements();

        return view.asNode();
    }

    @Override
    public void setExistingValues() {
        if (getController().getPackageState().hasPackageMetadataValues()) {
            view.clearAllFields();
            existingPackage = true;

            view.getPackageNameField().setText(getController().getPackageState().getPackageName());

            for (String domainProfile : getController().getPackageState().getPackageMetadataValues(GeneralParameterNames.DOMAIN_PROFILE)) {
                view.addDomainProfileLabel(domainProfile);
            }
            view.getDomainProfilesComboBox().setDisable(true);
            view.getAddDomainProfileButton().setDisable(true);

            for (Node node : view.getAllFields()) {
                if (getController().getPackageState().getPackageMetadataValues(node.getId()) != null) {
                    if (node instanceof TextField) {
                        ((TextField) node).setText(getController().getPackageState().getPackageMetadataValues(node.getId()).get(0));
                    } else if (node instanceof VBox) {
                        // don't add domain profile again.
                        if (!node.getId().equals(GeneralParameterNames.DOMAIN_PROFILE)) {
                            for (String value : getController().getPackageState().getPackageMetadataValues(node.getId())) {
                                ((VBox) node).getChildren().add(new RemovableLabel(value, (VBox) node));
                            }
                        }
                    } else if (node instanceof DatePicker) {
                        LocalDate localDate = ((DatePicker) node).getConverter().fromString(getController().getPackageState().getPackageMetadataValues(node.getId()).get(0));
                        ((DatePicker) node).setValue(localDate);
                    }
                }
            }
        }
    }

    private void bind() {

        // FIXME: The profile names should come from an actual service
        view.loadDomainProfileNames(Arrays.asList("Bag-It", "Custom Profile", "Custom Profile 2"));
        view.getAddDomainProfileButton().setOnAction(event -> {
            if (view.getDomainProfilesComboBox().getSelectionModel().getSelectedItem() != null &&
                    !view.getDomainProfilesComboBox().getSelectionModel().getSelectedItem().isEmpty()) {
                view.addDomainProfileRemovableLabel(view.getDomainProfilesComboBox().getSelectionModel().getSelectedItem());
            }
        });

        if (!view.isFormAlreadyDrawn()) {
            view.setupRecommendedFields(packageMetadataService.getRecommendedPackageMetadata());
            view.setupOptionalFields(packageMetadataService.getOptionalPackageMetadata());
        }


        view.getContinueButton().setOnAction(event -> {
            if (validateRequiredFields()) {
                updatePackageState();
                if (existingPackage) {
                    getController().goToNextPage(Page.DEFINE_RELATIONSHIPS);
                } else {
                    getController().goToNextPage(Page.CREATE_NEW_PACKAGE);
                }
            } else {
                view.showStatus(errors.get(ErrorKey.PACKAGE_NAME_OR_DOMAIN_PROFILE_MISSING));
            }

        });

        view.getCancelLink().setOnAction(event -> {
            view.showWarningPopup();
        });
        view.getWarningPopup().setCancelEventHandler(event -> view.getWarningPopup().hide());
        view.getWarningPopup().setConfirmEventHandler(event -> {
            view.getWarningPopup().hide();
            getController().goToPreviousPage();
        });


    }

    /**
     * helper method that updates the PackageState in the controller with the values in the form.
     */
    private void updatePackageState() {

        getController().getPackageState().setPackageName(view.getPackageNameField().getText());

        // Clear the package metadata list and reset the values based on the current state of form
        getController().getPackageState().setPackageMetadataList(new LinkedHashMap<>());

        for (Node removableLabel : view.getDomainProfileRemovableLabelVBox().getChildren()) {
            if (removableLabel instanceof RemovableLabel) {
                getController().getPackageState().addPackageMetadata(GeneralParameterNames.DOMAIN_PROFILE, ((RemovableLabel) removableLabel).getLabel().getText());
            } else {
                getController().getPackageState().addPackageMetadata(GeneralParameterNames.DOMAIN_PROFILE, ((Label) removableLabel).getText());
            }
        }

        for (Node node : view.getAllFields()) {
            if (node instanceof TextField) {
                getController().getPackageState().addPackageMetadata(node.getId(), ((TextField) node).getText());
            } else if (node instanceof VBox) {
                for (Node removableLabel : ((VBox) node).getChildren()) {
                    getController().getPackageState().addPackageMetadata(node.getId(), ((RemovableLabel) removableLabel).getLabel().getText());
                }
            } else if (node instanceof DatePicker) {
                if (((DatePicker) node).getValue() != null) {
                    DateTime dateTime = new DateTime(((DatePicker) node).getValue());
                    getController().getPackageState().addPackageMetadata(node.getId(), DateUtility.toCommonUSDate(dateTime));
                }
            }
        }
    }

    /**
     * Helper method that makes sure required fields are entered before letting the user move on to the next page.
     *
     * @return true or false based on validation
     */
    private boolean validateRequiredFields() {
        if (view.getPackageNameField().getText() != null && (view.getDomainProfileRemovableLabelVBox().getChildren() != null &&
                !view.getDomainProfileRemovableLabelVBox().getChildren().isEmpty())) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void setPackageMetadataService(PackageMetadataService packageMetadataService) {
        this.packageMetadataService = packageMetadataService;
    }

}
