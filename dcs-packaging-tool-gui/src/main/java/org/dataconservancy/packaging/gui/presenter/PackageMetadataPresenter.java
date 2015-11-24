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
package org.dataconservancy.packaging.gui.presenter;

import org.dataconservancy.packaging.gui.services.PackageMetadataService;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;

/**
 * Handles the screen related to package metadata.
 */
public interface PackageMetadataPresenter extends Presenter {

    /**
     * Sets the package metadata service to be used to handle package metadata properties
     * @param packageMetadataService the PackageMetadataService
     */
    void setPackageMetadataService(PackageMetadataService packageMetadataService);

    /**
     * Sets the DomainProfileStore to be used to retrieve the available DomainProfiles
     * @param domainProfileStore The domain profile store that will be used to retrieve the domain profiles.
     */
    void setDomainProfileStore(DomainProfileStore domainProfileStore);
}
