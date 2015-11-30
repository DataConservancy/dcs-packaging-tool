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

package org.dataconservancy.packaging.tool.integration;

import java.io.File;

import java.net.URI;

import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.SimpleSelector;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.copy;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.cut;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.selectLocal;

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
    DomainProfileServiceFactory profileServiceFactory;

    @Autowired
    PackageInitializer initializer;

    @Autowired
    public Packager packager;

    @Autowired
    public IpmRdfTransformService ipm2rdf;

    @Test
    public void fileExistsTest() throws Exception {

        PackageState state = initializer.initialize(DCS_PROFILE);

        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        opened.getPackageTree().walk(node -> {
            if (node.getFileInfo() != null && node.getFileInfo().isFile()) {
                File file =
                        Paths.get(node.getFileInfo().getLocation()).toFile();
                assertTrue(file.exists());
                assertTrue(file.isFile());
            }
        });
    }

    @Test
    public void directoriesExistTest() throws Exception {
        PackageState state = initializer.initialize(DCS_PROFILE);

        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        opened.getPackageTree()
                .walk(node -> {
                    if (node.getFileInfo() != null
                            && node.getFileInfo().isDirectory()) {
                        File dir =
                                Paths.get(node.getFileInfo().getLocation())
                                        .toFile();
                        assertTrue(dir.exists());
                        assertTrue(dir.isDirectory());

                    }
                });
    }

    /*
     * XXX It may be presumptuous to assume this should pass. This verifies that
     * there are no property errors (e.g. missing required properties). It may
     * be a conscious choice of certain profiles to have property requirements
     * that cannot be met by automated means, thus requiring inteligent
     * human/author action in the UI before this kind of test would pass.
     */
    @Test
    @Ignore
    public void propertyErrorTest() {
        PackageState state = initializer.initialize(DCS_PROFILE);

        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        DomainProfileService profileService =
                profileServiceFactory.getProfileService(opened
                        .getPackageState().getDomainObjectRDF());

        opened.getPackageTree().walk(node -> assertTrue(profileService
                .validateProperties(node, node.getNodeType()).isEmpty()));
    }

    /*
     * Verifies that the IPM tree in the opened package points to domain objects
     * in the opened domain object RDF model.
     */
    @Test
    public void domainObjectReferenceTest() throws Exception {
        PackageState state = initializer.initialize(DCS_PROFILE);

        Map<URI, Integer> originalDomainObjectSizes = domainObjectSizes(state);

        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        /*
         * Make sure the act of creating a package didn't alter domain object
         * references!
         */
        assertEquals(originalDomainObjectSizes, domainObjectSizes(state));

        opened.getPackageState().getDomainObjectRDF();
        
        // TODO  Compart to serialized package

    }

    private Map<URI, Integer> domainObjectSizes(PackageState state)
            throws RDFTransformException {
        Map<URI, Integer> originalDomainObjectSizes =
                new HashMap<URI, Integer>();

        Model model = copy(state.getDomainObjectRDF(), new SimpleSelector());

        ipm2rdf.transformToNode(state.getPackageTree())
                .walk(node -> {

                    if (node.getDomainObject() != null) {
                        originalDomainObjectSizes.put(node.getIdentifier(),
                                                      cut(model,
                                                          selectLocal(model
                                                                  .getResource(node
                                                                          .getDomainObject()
                                                                          .toString())))
                                                              .listStatements()
                                                              .toSet().size());
                    } else {
                        originalDomainObjectSizes.put(node.getIdentifier(), 0);
                    }
                });

        assertEquals("Model contained triples not reachable by domain objects!",
                     0,
                     model.listStatements().toSet().size());

        return originalDomainObjectSizes;
    }
}
