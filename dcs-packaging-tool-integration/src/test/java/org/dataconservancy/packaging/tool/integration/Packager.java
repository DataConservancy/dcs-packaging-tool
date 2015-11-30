/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.packaging.tool.api.OpenPackageService;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.PropertiesConfigurationParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.PACKAGE_LOCATION;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.PACKAGE_NAME;

public class Packager {

    PropertiesConfigurationParametersBuilder paramsBuilder =
            new PropertiesConfigurationParametersBuilder();

    @Autowired
    public OpenPackageService openPackageService;

    @Autowired
    public PackageGenerationService createPakageService;

    @Autowired
    public OpenPackageService opener;

    public OpenedPackage createPackage(PackageState state, File staging) {
        try (InputStream props =
                this.getClass()
                        .getResourceAsStream("/PackageGenerationParams.properties")) {

            PackageGenerationParameters params =
                    paramsBuilder.buildParameters(props);

            params.addParam(PACKAGE_NAME, "TestPackage");
            params.addParam(PACKAGE_LOCATION, "/tmp");

            Package pkg = createPakageService.generatePackage(state, params);

            File packageFile = new File(staging, pkg.getPackageName());

            try (OutputStream out = new FileOutputStream(packageFile)) {
                IOUtils.copy(pkg.serialize(), out);
            }
            pkg.cleanupPackage();

            File pkgStage = new File(staging, UUID.randomUUID().toString());
            pkgStage.mkdirs();

            return opener.openPackage(pkgStage, packageFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
