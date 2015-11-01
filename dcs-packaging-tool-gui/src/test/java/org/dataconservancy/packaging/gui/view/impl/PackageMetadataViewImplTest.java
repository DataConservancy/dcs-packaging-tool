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
package org.dataconservancy.packaging.gui.view.impl;

import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.gui.services.PackageMetadataService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that package metadata view impl initializes all fields correctly based on the given service.
 */
public class PackageMetadataViewImplTest extends BaseGuiTest {

    private PackageMetadataViewImpl view;
    private PackageMetadataService service;

    @Autowired
    private Configuration configuration;

    @Before
    public void setup() throws IOException {

        view = new PackageMetadataViewImpl(labels);
        service = new PackageMetadataService(configuration);

    }

    /**
     * Make sure controls can be retrieved.
     */
    @Test
    public void testComponentsNotNullOrEmpty() {
        view.setupRequiredFields(service.getRequiredPackageMetadata());
        view.setupRecommendedFields(service.getRecommendedPackageMetadata());
        view.setupOptionalFields(service.getOptionalPackageMetadata());

        assertNotNull(view.getContinueButton());
        assertNotNull(view.getAddDomainProfileButton());
        assertNotNull(view.getDomainProfileRemovableLabelVBox());
        assertNotNull(view.getDomainProfilesComboBox());
        assertNotNull(view.getErrorLabel());
        assertNotNull(view.getPackageMetadataFileChooser());
        assertNotNull(view.getAllFields());
        assertTrue(view.getAllFields().size() > 0);

    }


}
