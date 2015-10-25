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

import com.hp.hpl.jena.sparql.function.library.date;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.Page;
import org.dataconservancy.packaging.gui.presenter.PackageMetadataPresenter;
import org.dataconservancy.packaging.gui.services.PackageMetadataService;
import org.dataconservancy.packaging.gui.util.RemovableLabel;
import org.dataconservancy.packaging.gui.view.PackageMetadataView;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Implementation for the screen that will handle package metadata.
 */
public class PackageMetadataPresenterImpl extends BasePresenterImpl implements PackageMetadataPresenter {
    private PackageMetadataView view;
    private PackageMetadataService packageMetadataService;
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
        /*
        if (getController().getPackageState().hasPackageMetadataValues()) {
            // TODO: set the existing values in the form. These need to come from package state previously set by
            // selecting an existing package.
        }
        */
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

        view.setupRecommendedFields(packageMetadataService.getRecommendedPackageMetadata());

        view.setupOptionalFields(packageMetadataService.getOptionalPackageMetadata());

        view.getContinueButton().setOnAction(event -> {
            if (validateRequiredFields()) {
                updatePackageState();
                getController().goToNextPage(Page.CREATE_NEW_PACKAGE);
            } else {
                view.showStatus(errors.get(ErrorKey.PACKAGE_NAME_MISSING));
            }

        });

    }

    private void updatePackageState() {

        getController().getPackageState().setPackageName(view.getPackageNameField().getText());

        for (Node node : view.getAllFields()) {
            if (node instanceof TextField) {
                getController().getPackageState().addPackageMetadata(node.getId(), ((TextField) node).getText());
            }
            else if (node instanceof VBox) {
                for (Node removableLabel : ((VBox) node).getChildren()) {
                    getController().getPackageState().addPackageMetadata(node.getId(), ((RemovableLabel) removableLabel).getLabel().getText());
                }
            }
            else if (node instanceof DatePicker) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate date = ((DatePicker) node).getValue();
                if (date != null) {
                    String dateString = formatter.format(date);
                    getController().getPackageState().addPackageMetadata(node.getId(), dateString);
                }
            }
        }
    }

    private boolean validateRequiredFields() {
        if (view.getPackageNameField().getText() != null) {
            return true;
        }
        else {
            return false;
        }
    }


    @Override
    public void setPackageMetadataService(PackageMetadataService packageMetadataService) {
        this.packageMetadataService = packageMetadataService;
    }

}
