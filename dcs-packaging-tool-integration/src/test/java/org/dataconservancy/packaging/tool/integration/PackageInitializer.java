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

import java.net.URI;

import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.springframework.beans.factory.annotation.Autowired;

public class PackageInitializer {

    @Autowired
    public IPMService ipmService;

    @Autowired
    public DomainProfileStore profileStore;

    @Autowired
    public IpmRdfTransformService ipm2rdf;

    @Autowired
    DomainProfileServiceFactory profileFactory;

    public PackageState initialize(URI profile) {
        try {
            Node tree =
                    ipmService
                            .createTreeFromFileSystem(Paths
                                    .get(this.getClass()
                                            .getResource("/TestContent/foo")
                                            .toURI()).getParent()
                                    .resolve("Parent_Dir"));

            Map<URI, DomainProfile> profiles =
                    profileStore
                            .getPrimaryDomainProfiles()
                            .stream()
                            .collect(Collectors.toMap(DomainProfile::getIdentifier,
                                                      Function.identity()));

            Model model = ModelFactory.createDefaultModel();

            DomainProfileService profileService =
                    profileFactory.getProfileService(model);

            profileService.assignNodeTypes(profiles.get(profile), tree);

            PackageState state = new PackageState();

            state.setDomainObjectRDF(model);
            state.setPackageTree(ipm2rdf.transformToRDF(tree));
            state.setUserSpecifiedProperties(new HashMap<>());
            return state;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
