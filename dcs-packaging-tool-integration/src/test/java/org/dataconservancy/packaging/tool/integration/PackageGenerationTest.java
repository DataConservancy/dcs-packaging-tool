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
import java.io.IOException;

import java.net.URI;

import java.nio.file.Paths;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.SimpleSelector;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.copy;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.cut;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.selectLocal;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_DCS_ONTOLOGY_BOM;

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

    @Autowired
    public IPMService ipmService;

    @Test
    public void fileLocationTest() throws Exception {

        PackageState state = initializer.initialize(DCS_PROFILE);

        Set<URI> originalFileLocations = new HashSet<>();

        ipm2rdf.transformToNode(state.getPackageTree())
                .walk(node -> originalFileLocations.add(node.getFileInfo()
                        .getLocation()));

        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        opened.getPackageTree()
                .walk(node -> {
                    if (node.getFileInfo() != null
                            && node.getFileInfo().isFile()) {
                        File file =
                                Paths.get(node.getFileInfo().getLocation())
                                        .toFile();
                        assertTrue(file.exists());
                        assertTrue(file.isFile());
                        assertFalse(originalFileLocations.contains(node
                                .getFileInfo().getLocation()));
                    } else if (node.getFileInfo() != null
                            && node.getFileInfo().isDirectory()) {
                        File dir =
                                Paths.get(node.getFileInfo().getLocation())
                                        .toFile();
                        assertTrue(dir.exists());
                        assertTrue(dir.isDirectory());
                    }

                    assertFalse(originalFileLocations.contains(node
                            .getFileInfo().getLocation()));

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

        /* Make sure opened package still has valid references */
        assertEquals(originalDomainObjectSizes,
                     domainObjectSizes(opened.getPackageState()));

        /*
         * Now re-package the opened package, and verify that the references are
         * yet again valid
         */
        assertEquals(originalDomainObjectSizes, domainObjectSizes(packager
                .createPackage(opened.getPackageState(), folder.getRoot())
                .getPackageState()));

    }

    @Test
    public void transformTest() throws Exception {

        PackageState state = initializer.initialize(DCS_PROFILE);
        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        DomainProfileService profileService =
                profileServiceFactory.getProfileService(opened
                        .getPackageState().getDomainObjectRDF());

        Map<Node, NodeTransform> toTransform = new HashMap<>();

        opened.getPackageTree().walk(node -> {

            /* Transform all Metadata to DI + File */
            if (node.getNodeType().getDomainTypes()
                    .contains(URI.create(NS_DCS_ONTOLOGY_BOM + "Metadata"))) {
                profileService
                        .getNodeTransforms(node)
                        .stream()
                        .filter(xform -> xform
                                .getResultNodeType()
                                .getDomainTypes()
                                .contains(URI.create(NS_DCS_ONTOLOGY_BOM
                                        + "File")))
                        .forEach(xform -> toTransform.put(node, xform));
            }
        });

        toTransform.forEach(profileService::transformNode);

        opened.getPackageState().setPackageTree(ipm2rdf.transformToRDF(opened
                .getPackageTree()));

        OpenedPackage openedAfterTransform =
                packager.createPackage(opened.getPackageState(),
                                       folder.getRoot());

        assertEquals(domainObjectSizes(opened.getPackageState()),
                     domainObjectSizes(openedAfterTransform.getPackageState()));

    }

    /*
     * Verify that when 'refreshed' after opening from a package, no IPM tree
     * changes are suggested.
     */
    @Test
    public void refreshTest() throws Exception {
        PackageState state = initializer.initialize(DCS_PROFILE);
        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        Node rescannedTree =
                ipmService.createTreeFromFileSystem(Paths.get(opened
                        .getPackageTree().getFileInfo().getLocation()));
        buildContentRoots(opened.getPackageTree(), rescannedTree);
        Map<Node, NodeComparison> comparisons =
                ipmService.compareTree(opened.getPackageTree(), rescannedTree);

        assertTrue(comparisons.isEmpty());
    }

    /*
     * TODO: Copied verbatim from EditPackageContentPresenterImpl - maybe these
     * generic tree operations should be in a common library?
     */
    private void buildContentRoots(Node node, Node newTree) throws IOException {
        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                if (child.getFileInfo() != null
                        && Paths.get(child.getFileInfo().getLocation())
                                .toFile().exists()) {
                    if (!Paths.get(child.getFileInfo().getLocation())
                            .startsWith(Paths.get(node.getFileInfo()
                                    .getLocation()))) {
                        Node newTreeParent =
                                getNewTreeNodeForExistingNode(node, newTree);
                        if (newTreeParent != null) {
                            newTreeParent.addChild(buildComparisonTree(child));
                        } else {
                            newTree.addChild(buildComparisonTree(child));
                        }
                    } else if (child.getChildren() != null) {
                        buildContentRoots(child, newTree);
                    }
                } else if (child.getChildren() != null) {
                    buildContentRoots(child, newTree);
                }
            }
        }
    }

    /*
     * TODO: Copied verbatim from EditPackageContentPresenterImpl - maybe these
     * generic tree operations should be in a common library?
     */
    private Node buildComparisonTree(Node node) throws IOException {
        Node newTree =
                ipmService.createTreeFromFileSystem(Paths.get(node
                        .getFileInfo().getLocation()));
        buildContentRoots(node, newTree);

        return newTree;
    }

    /*
     * TODO: Copied verbatim from EditPackageContentPresenterImpl - maybe these
     * generic tree operations should be in a common library?
     */
    private Node getNewTreeNodeForExistingNode(Node node, Node newTree) {
        Node foundNode = null;
        if (node.getFileInfo() != null
                && newTree.getFileInfo() != null
                && node.getFileInfo().getLocation()
                        .equals(newTree.getFileInfo().getLocation())) {
            foundNode = newTree;
        } else if (newTree.getChildren() != null) {
            for (Node newTreeChild : newTree.getChildren()) {
                foundNode = getNewTreeNodeForExistingNode(node, newTreeChild);
                if (foundNode != null) {
                    break;
                }
            }
        }

        return foundNode;
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
