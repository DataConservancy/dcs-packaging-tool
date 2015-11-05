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

import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;

/**
 * Used for controlling the screen related to creating a new package, handles loading either a content directory or an existing package description.
 */
public interface CreateNewPackagePresenter extends Presenter {

    /**
     * Sets the profile service that will be used to assign types to the newly created package tree.
     * @param profileService The profile service to use for assigning types.
     */
    void setProfileService(DomainProfileService profileService);

    /**
     * Sets the IPM Service that should be used for building the tree.
     * @param ipmService The Ipm Service to use for building the tree.
     */
    void setIpmService(IPMService ipmService);
}
