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

import java.net.URI;

import java.nio.file.Paths;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.OpenPackageService;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStoreImpl;
import org.dataconservancy.packaging.tool.impl.DomainProfileServiceImpl;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.PropertiesConfigurationParametersBuilder;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.PACKAGE_NAME;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.PACKAGE_LOCATION;

@ContextConfiguration({
        "classpath*:org/dataconservancy/config/applicationContext.xml",
        "classpath*:org/dataconservancy/packaging/tool/ser/config/applicationContext.xml",
        "classpath*:applicationContext.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class PackageGenerationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void clearTempFolder() {
        folder.delete();
        folder.getRoot().mkdirs();
    }

    URI DCS_PROFILE = URI
            .create("http//dataconservancy.org/ptg-profiles/dcs-bo-1.0");

    @Autowired
    public IPMService ipmService;

    @Autowired
    public OpenPackageService openPackageService;

    @Autowired
    public PackageGenerationService createPakageService;

    @Autowired
    public URIGenerator uriGen;

    @Autowired
    public DomainProfileStore profileStore;

    @Autowired
    public IpmRdfTransformService ipm2rdf;

    PropertiesConfigurationParametersBuilder paramsBuilder =
            new PropertiesConfigurationParametersBuilder();

    @Test
    public void createPackageTest() throws Exception {
        Node tree =
                ipmService.createTreeFromFileSystem(Paths
                        .get(this.getClass().getResource("/TestContent/README")
                                .toURI()).getParent().resolve("Parent_Dir"));

        Map<URI, DomainProfile> profiles =
                profileStore
                        .getPrimaryDomainProfiles()
                        .stream()
                        .collect(Collectors.toMap(DomainProfile::getIdentifier,
                                                  Function.identity()));

        Model model = ModelFactory.createDefaultModel();
        DomainProfileObjectStoreImpl objectStore =
                new DomainProfileObjectStoreImpl(model, uriGen);
        DomainProfileService profileService =
                new DomainProfileServiceImpl(objectStore, uriGen);

        profileService.assignNodeTypes(profiles.get(DCS_PROFILE), tree);

        PackageState state = new PackageState();
        state.setDomainObjectRDF(model);
        state.setPackageTree(ipm2rdf.transformToRDF(tree));

        try (InputStream props =
                this.getClass()
                        .getResourceAsStream("/PackageGenerationParams.properties")) {

            PackageGenerationParameters params =
                    paramsBuilder.buildParameters(props);

            params.addParam(PACKAGE_NAME, "TestPackage");
            params.addParam(PACKAGE_LOCATION, "/tmp");

            Package pkg = createPakageService.generatePackage(state, params);
            
            try (OutputStream out =
                    new FileOutputStream(new File(folder.getRoot(), pkg.getPackageName()))) {
                IOUtils.copy(pkg.serialize(), out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            pkg.cleanupPackage();
        }
    }
}
