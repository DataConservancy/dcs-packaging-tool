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

package org.dataconservancy.packaging.gui;

import java.util.HashMap;
import java.util.Map;

import javafx.stage.Stage;

import org.dataconservancy.packaging.gui.presenter.*;
import org.dataconservancy.packaging.gui.presenter.impl.SelectContentDirectoryPresenter;
import org.dataconservancy.packaging.gui.view.*;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageModelBuilder;
import org.dataconservancy.packaging.tool.impl.BOREMPackageGenerator;
import org.dataconservancy.packaging.tool.impl.PackageDescriptionValidator;
import org.dataconservancy.packaging.tool.impl.TestPackageGenerator;
import org.dataconservancy.packaging.tool.impl.generator.BagItPackageAssembler;
import org.dataconservancy.packaging.tool.impl.generator.OrePackageModelBuilder;
import org.dataconservancy.packaging.tool.impl.generator.PackageAssemblerFactory;
import org.dataconservancy.packaging.tool.impl.generator.PackageModelBuilderFactory;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.PackageGenerationParametersBuilder;

/**
 * Factory that returns singletons of various objects needed by the application.
 */
public class Factory {
    private Stage stage;
    private Controller controller;

    private HomepageView homepageView;
    private HomepagePresenter homepagePresenter;
    private CreateNewPackageView createNewPackageView;
    private CreateNewPackagePresenter createNewPackagePresenter;
    private PackageDescriptionValidator packageDescriptionValidator;
    private PackageDescriptionBuilder packageDescriptionBuilder;
    private PackageGenerationService pkgGenerationService;
    private PackageGenerationParametersBuilder pkgGenParamsBuilder;
    private BOREMPackageGenerator boremPkgGenerator;
    private TestPackageGenerator testPkgGenerator;
    private PackageGenerationPresenter pkgGenerationPresenter;
    private PackageGenerationView pkgGenerationView;
    private PackageDescriptionPresenter pkgDescriptionPresenter;
    private PackageDescriptionView pkgDescriptionView;
    private HeaderView headerView;
    private SelectContentDirectoryPresenter contentDirectoryPresenter;
    private SelectContentDirectoryView contentDirectoryView;
    
    private Labels labels;
    private Messages messages;

    private Configuration config;

    public Factory() {
        initializeAssemblers();
    }
    
    private void initializeAssemblers() {
        Map<String, Class<? extends PackageAssembler>> assemblers = new HashMap<>();
        assemblers.put("BOREM",  BagItPackageAssembler.class);
        PackageAssemblerFactory.setAssemblers(assemblers);
        
        Map<String, Class<? extends PackageModelBuilder>> builders = new HashMap<>();
        builders.put("http://dataconservancy.org/spec/dcs-pkg-desc-BOREM", OrePackageModelBuilder.class);
        PackageModelBuilderFactory.setBuilders(builders);
    }


    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }

    public Labels getLabels() { return labels; }
    public void setLabels(Labels labels) { this.labels = labels; }

    public Messages getMessages() { return messages; }
    public void setMessages(Messages messages) { this.messages = messages; }

    public Configuration getConfiguration() { return config; }
    public void setConfiguration(Configuration config) { this.config = config; }

    public Controller getController() { return controller; }
    public void setController(Controller controller) { this.controller = controller; }

    public HomepageView getHomepageView() { return homepageView; }

    public void setHomepageView(HomepageView homepageView) { this.homepageView = homepageView; }

    public HomepagePresenter getHomepagePresenter() { return homepagePresenter; }

    public void setHomepagePresenter(HomepagePresenter homepagePresenter) { this.homepagePresenter = homepagePresenter; }

    public CreateNewPackageView getCreateNewPackageView() { return createNewPackageView; }
    public void setCreateNewPackageView(CreateNewPackageView view) { this.createNewPackageView = view; }

    public CreateNewPackagePresenter getCreateNewPackagePresenter() { return createNewPackagePresenter; }
    public void setCreateNewPackagePresenter(CreateNewPackagePresenter presenter) { this.createNewPackagePresenter = presenter; }

    public PackageGenerationView getPackageGenerationView() { return pkgGenerationView; }
    public void setPackageGenerationView(PackageGenerationView view) { this.pkgGenerationView = view; }

    public PackageGenerationPresenter getPackageGenerationPresenter() { return pkgGenerationPresenter; }
    public void setPackageGenerationPresenter(PackageGenerationPresenter presenter) { this.pkgGenerationPresenter = presenter; }

    public PackageDescriptionView getPackageDescriptionView() { return pkgDescriptionView; }
    public void setPackageDescriptionView(PackageDescriptionView view) { this.pkgDescriptionView = view; }

    public PackageDescriptionPresenter getPackageDescriptionPresenter() { return pkgDescriptionPresenter; }
    public void setPackageDescriptionPresenter(PackageDescriptionPresenter presenter) { this.pkgDescriptionPresenter = presenter; }

    public PackageDescriptionValidator getPackageDescriptionValidator() { return packageDescriptionValidator; }
    public void setPackageDescriptionValidator(PackageDescriptionValidator validator ) { this.packageDescriptionValidator = validator; }

    public PackageDescriptionBuilder getPackageDescriptionBuilder() { return packageDescriptionBuilder; }
    public void setPackageDescriptionBuilder(PackageDescriptionBuilder builder) { this.packageDescriptionBuilder = builder; }
    
    public PackageGenerationService getPackageGenerationService() { return pkgGenerationService; }
    public void setPackageGenerationService(PackageGenerationService service) { this.pkgGenerationService = service; }
    
    public PackageGenerationParametersBuilder getPackageGenerationParametersBuilder() { return pkgGenParamsBuilder; }
    public void setPackageGenerationParametersBuilder(PackageGenerationParametersBuilder builder) { pkgGenParamsBuilder = builder; }

    public BOREMPackageGenerator getBOREMPackageGenerator() { return boremPkgGenerator; }
    public void setBOREMPackageGenerator(BOREMPackageGenerator generator) { boremPkgGenerator = generator; }
    
    public TestPackageGenerator getTestPackageGenerator() { return testPkgGenerator; }
    public void setTestPackageGenerator(TestPackageGenerator generator) { testPkgGenerator = generator; }
    
    public HeaderView getHeaderView() { return headerView; }
    public void setHeaderView(HeaderView headerView) { this.headerView = headerView; }

    public SelectContentDirectoryPresenter getContentDirectoryPresenter() { return contentDirectoryPresenter; }
    public void setContentDirectoryPresenter(SelectContentDirectoryPresenter presenter) {this.contentDirectoryPresenter = presenter; }

    public SelectContentDirectoryView getContentDirectoryView() { return contentDirectoryView; }
    public void setContentDirectoryView(SelectContentDirectoryView contentDirectoryView) { this.contentDirectoryView = contentDirectoryView; }
}
