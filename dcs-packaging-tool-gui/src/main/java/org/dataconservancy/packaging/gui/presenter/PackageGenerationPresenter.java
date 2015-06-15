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
package org.dataconservancy.packaging.gui.presenter;

import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.PackageGenerationParametersBuilder;

/**
 * Handles the screen related to generating a package. Allows the user to select packaging options and an output directory to save to.
 */
public interface PackageGenerationPresenter extends Presenter {

    /**
     * Sets the service that will generate package descriptions to refresh after building a new package
     * @param packageDescriptionBuilder  the  PackageDescriptionBuilder
     */
    public void setPackageDescriptionBuilder(PackageDescriptionBuilder packageDescriptionBuilder);
    
    /**
     * Sets the service that will be used for generating a package from the previously created package description, and the defined package parameters.
     * @param packageGenerationService  the  PackageGenerationService
     */
    public void setPackageGenerationService(PackageGenerationService packageGenerationService);
    
    /**
     * Sets the package parameters builder that will be used for deserializing package parameters from a file. Note currently this is not used. 
     * @param packageParamsBuilder the PackageGenerationParametersBuilder
     */
    public void setPackageGenerationParametersBuilder(PackageGenerationParametersBuilder packageParamsBuilder);
}
