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

import de.schlichtherle.io.FileInputStream;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ontologies.ModelResources;
import org.dataconservancy.packaging.tool.profile.DcsBOProfile;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.nationalarchives.utf8.validator.Utf8Validator;
import uk.gov.nationalarchives.utf8.validator.ValidationException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.codec.digest.DigestUtils.shaHex;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.copy;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.cut;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.selectLocal;
import static org.dataconservancy.packaging.tool.model.BagItParameterNames.BAGIT_PROFILE_ID;
import static org.dataconservancy.packaging.tool.model.BagItParameterNames.PACKAGE_MANIFEST;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_DCS_ONTOLOGY_BOM;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_ORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
            .create("http://dataconservancy.org/ptg-profiles/dcs-bo-1.0");

    private DcsBOProfile bop = new DcsBOProfile();

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

    @Test
    public void verifyRemediationTest() throws Exception {

        PackageState state = initializer.initialize(DCS_PROFILE);

        Set<URI> originalFileLocations = new HashSet<>();

        ipm2rdf.transformToNode(state.getPackageTree())
                .walk(node -> originalFileLocations.add(node.getFileInfo()
                        .getLocation()));

        // The package should contain two files:
        // - READMX
        // - READMÉ
        //
        // The file with the acute E will be remediated to a resource named 'READMX', which will collide with
        // an existing resource of the same name.

        // assert that our sample problem files are in the content to be packaged
        assertTrue(originalFileLocations.stream().anyMatch(uri -> uri.getPath().endsWith("READMX")));
        // 0x0301 is the UTF-16 encoding of the 'COMBINING ACUTE ACCENT' combining diacritic
        // 0x00c9 is the UTF-16 encoding of 'LATIN CAPITAL LETTER E WITH ACUTE'
        assertTrue(originalFileLocations.stream()
                .anyMatch(uri -> (uri.getPath().endsWith("README" + '\u0301')) ||
                        (uri.getPath().endsWith("READM" + '\u00c9'))));

        OpenedPackage opened = packager.createPackage(state, folder.getRoot());

        AtomicBoolean foundIllegal = new AtomicBoolean(Boolean.FALSE);
        AtomicBoolean foundRemediated = new AtomicBoolean(Boolean.FALSE);
        AtomicReference<String> remediatedFilename = new AtomicReference<>();
        AtomicBoolean foundCollision = new AtomicBoolean(Boolean.FALSE);
        AtomicReference<String> collidingFilename = new AtomicReference<>();

        // Walk the generated package, and make sure that
        // 1. That a resource with illegal characters does not exist
        // 2. That a resource named 'READMX' does exist
        // 3. That a resource named after the SHA-1 hex of its identifier exists
        // 4. That those two resources originate from two different files in the original package content
        opened.getPackageTree()
                .walk(node -> {
                    if (node.getFileInfo() == null || !node.getFileInfo().isFile()) {
                        return;
                    }

                    System.err.println(node.getFileInfo().getName());
                    System.err.println("  " + node.getFileInfo().getLocation().toString());

                    // this should not happen, because a file name with invalid characters should have
                    // been remediated prior to being inserted into the package
                    if (node.getFileInfo().getLocation().getPath().endsWith("README" + '\u0301')) {
                        foundIllegal.set(Boolean.TRUE);
                    }

                    if (node.getFileInfo().getLocation().getPath()
                            .endsWith(shaHex(node.getIdentifier().toString()))) {
                        foundRemediated.set(Boolean.TRUE);
                        remediatedFilename.set(node.getFileInfo().getName());
                        // short circuit
                        return;
                    }

                    if (node.getFileInfo().getName().equals("READMX") ||
                            node.getFileInfo().getName().equals("READMÉ")) {
                        foundCollision.set(Boolean.TRUE);
                        collidingFilename.set(node.getFileInfo().getName());
                    }
                });

        assertFalse(foundIllegal.get());
        assertTrue(foundCollision.get());
        assertTrue(foundRemediated.get());

        assertNotNull(remediatedFilename.get());
        assertNotNull(collidingFilename.get());
        assertNotEquals(remediatedFilename.get(), collidingFilename.get());

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

    /*
     * Verifies that complex properties can successfully be persisted and
     * re-opened
     */
    @Test
    public void complexPropertiesTest() throws Exception {
        PackageState initialState = initializer.initialize(DCS_PROFILE);

        OpenedPackage opened =
                packager.createPackage(initialState, folder.getRoot());

        DomainProfileService profileService =
                profileServiceFactory.getProfileService(opened
                        .getPackageState().getDomainObjectRDF());

        Property creator1 = new Property(bop.getHasCreator());
        Property creator1_name = new Property(bop.getName());
        creator1_name.setStringValue("Fred");
        Property creator1_mbox = new Property(bop.getMbox());
        creator1_mbox.setStringValue("fred@mertz.org");
        creator1.setComplexValue(Arrays.asList(creator1_name, creator1_mbox));

        Property creator2 = new Property(bop.getHasCreator());
        Property creator2_name = new Property(bop.getName());
        creator2_name.setStringValue("Ethel");
        Property creator2_mbox = new Property(bop.getMbox());
        creator2_mbox.setStringValue("ethel@mertz.org");
        creator2.setComplexValue(Arrays.asList(creator2_name, creator2_mbox));

        AtomicInteger collectionCount = new AtomicInteger(0);

        /* Add two creators to each collection */
        opened.getPackageTree().walk(node -> {
            if (node.getNodeType().getDomainTypes()
                    .contains(URI.create(NS_DCS_ONTOLOGY_BOM + "Collection"))) {
                collectionCount.incrementAndGet();
                profileService.addProperty(node, creator1);
                profileService.addProperty(node, creator2);
            }
        });

        OpenedPackage afterSaveAndReopen =
                packager.createPackage(opened.getPackageState(),
                                       folder.getRoot());

        Set<String> initialObjects =
                initialState.getDomainObjectRDF().listObjects()
                        .filterKeep(RDFNode::isLiteral)
                        .mapWith(RDFNode::asLiteral)
                        .mapWith(Literal::getString).toSet();
        Set<String> openedObjects =
                opened.getPackageState().getDomainObjectRDF().listObjects()
                        .filterKeep(RDFNode::isLiteral)
                        .mapWith(RDFNode::asLiteral)
                        .mapWith(Literal::getString).toSet();
        Set<String> afterSaveAndReopenObjects =
                afterSaveAndReopen.getPackageState().getDomainObjectRDF()
                        .listObjects().filterKeep(RDFNode::isLiteral)
                        .mapWith(RDFNode::asLiteral)
                        .mapWith(Literal::getString).toSet();
        Set<String> afterSaveAndReopenCustodialObjects =
                custodialDomainObjects(afterSaveAndReopen).listObjects()
                        .filterKeep(RDFNode::isLiteral)
                        .mapWith(RDFNode::asLiteral)
                        .mapWith(Literal::getString).toSet();

        assertFalse(initialObjects.contains(creator1_name.getStringValue()));
        assertTrue(openedObjects.contains(creator1_name.getStringValue()));
        assertTrue(openedObjects.contains(creator2_name.getStringValue()));
        assertEquals(2 * collectionCount.get(),
                     opened.getPackageState()
                             .getDomainObjectRDF()
                             .listStatements(null,
                                             opened.getPackageState()
                                                     .getDomainObjectRDF()
                                                     .getProperty(creator1
                                                             .getPropertyType()
                                                             .getDomainPredicate()
                                                             .toString()),

                                             (RDFNode) null).toSet().size());
        assertTrue(afterSaveAndReopenObjects.contains(creator1_name
                .getStringValue()));
        assertTrue(afterSaveAndReopenObjects.contains(creator2_name
                .getStringValue()));
        assertTrue(afterSaveAndReopenCustodialObjects.contains(creator1_name
                .getStringValue()));
        assertTrue(afterSaveAndReopenCustodialObjects.contains(creator2_name
                .getStringValue()));

        assertNotEquals(domainObjectSizes(initialState),
                        domainObjectSizes(opened.getPackageState()));
        assertEquals(domainObjectSizes(opened.getPackageState()),
                     domainObjectSizes(afterSaveAndReopen.getPackageState()));

        Model custodialAfterSaveAndReopen =
                custodialDomainObjects(afterSaveAndReopen);

        assertEquals(afterSaveAndReopen.getPackageState().getDomainObjectRDF()
                .listStatements().toSet().size(), custodialAfterSaveAndReopen
                .listStatements().toSet().size());
    }

    /*
     * Verifies that all the links in the custodial domain objects resolve to
     * something, and that all the files are linked to.
     */
    @Test
    public void custodialDomainObjectReferenceTest() throws Exception {
        PackageState initialState = initializer.initialize(DCS_PROFILE);

        OpenedPackage opened =
                packager.createPackage(initialState, folder.getRoot());

        Path baseDir =
                opened.getBaseDirectory().getParentFile().getCanonicalFile()
                        .toPath();

        /*
         * Opened package re-map bah URIs to files. We need the original bag
         * URIs, so re-create them!
         */
        Set<String> fileLocations = new HashSet<>();
        opened.getPackageTree()
                .walk(node -> {
                    if (node.getFileInfo() != null
                            && node.getFileInfo().isFile()) {
                        try {
                            URI bagURIForFile =
                                    UriUtility.makeBagUriString(Paths.get(node
                                                                        .getFileInfo()
                                                                        .getLocation())
                                                                        .toFile()
                                                                        .getCanonicalFile(),
                                                                baseDir.toFile());
                            fileLocations.add(bagURIForFile.toString());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertFalse(fileLocations.isEmpty());

        Model custodialDomainObjects = custodialDomainObjects(opened);

        Set<URI> bagURIs = new HashSet<>();
        custodialDomainObjects.listSubjects().mapWith(Resource::getURI)
                .mapWith(URI::create).filterKeep(UriUtility::isBagUri)
                .forEachRemaining(bagURIs::add);
        custodialDomainObjects.listObjects().filterKeep(RDFNode::isURIResource)
                .mapWith(RDFNode::asResource).mapWith(Resource::getURI)
                .mapWith(URI::create).filterKeep(UriUtility::isBagUri)
                .forEachRemaining(bagURIs::add);

        bagURIs.forEach(bagURI -> {
            fileLocations.remove(bagURI.toString());
            File linkedFile =
                    UriUtility.resolveBagUri(baseDir, bagURI).toFile();
            assertTrue(linkedFile.exists());
            assertTrue(linkedFile.isFile());
        });

        assertTrue("Expected no file locations but found: " + fileLocations.stream().collect(Collectors.joining("\n", "\n", "")),
                fileLocations.isEmpty());
    }

    @Test
    public void customPropertiesTest() throws Exception {
        URI customPredicate = URI.create("urn:test:customPropertiesTest");
        PropertyType customPropertyType = new PropertyType();
        customPropertyType.setDomainPredicate(customPredicate);
        customPropertyType.setPropertyValueType(PropertyValueType.STRING);

        PackageState initialState = initializer.initialize(DCS_PROFILE);

        Node nodeForProperty =
                ipm2rdf.transformToNode(initialState.getPackageTree());
        Property property = new Property(customPropertyType);
        property.setStringValue(nodeForProperty.toString());

        initialState.getUserSpecifiedProperties()
                .put(nodeForProperty.getIdentifier(), Collections.singletonList(property));

        /* Now add a custom property. Unpack the tree, add, and re-pack */
        applyCustomProperties(initialState);

        OpenedPackage opened =
                packager.createPackage(initialState, folder.getRoot());

        assertEquals(domainObjectSizes(initialState),
                     domainObjectSizes(opened.getPackageState()));
        Model openedModel = opened.getPackageState().getDomainObjectRDF();
        Set<String> customValues =
                openedModel
                        .listObjectsOfProperty(openedModel.getProperty(customPredicate
                                .toString())).mapWith(RDFNode::asLiteral)
                        .mapWith(Literal::getString).toSet();

        assertEquals(1, customValues.size());
        assertTrue(customValues.contains(property.getStringValue()));

    }

    private void applyCustomProperties(PackageState state)
            throws RDFTransformException {
        DomainProfileService profileService =
                profileServiceFactory.getProfileService(state
                        .getDomainObjectRDF());

        /* Now add a custom property. Unpack the tree, add, and re-pack */
        Node initialTree = ipm2rdf.transformToNode(state.getPackageTree());

        state.getUserSpecifiedProperties()
                .forEach((nodeURI, properties) -> initialTree.walk(node -> {
                    if (node.getIdentifier().equals(nodeURI)) {
                        properties.forEach(prop -> profileService
                                .addProperty(node, prop));
                    }
                }));

        state.setPackageTree(ipm2rdf.transformToRDF(initialTree));
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

    /**
     * Insures the models from ModelResources are included in the final package.
     * Currently every model exposed by {@code ModelResources#RESOURCE_MAP}
     * should have a serialization in the final package under the ONT directory
     * per our spec.
     *
     * @throws Exception
     */
    @Test
    public void testOntologiesIncluded() throws Exception {
        PackageState state = initializer.initialize(DCS_PROFILE);
        OpenedPackage openedPackage =
                packager.createPackage(state, folder.getRoot());
        List<File> models = new ArrayList<>();
        OntDirectoryWalker walker = new OntDirectoryWalker();

        walker.doWalk(openedPackage.getBaseDirectory(), models);

        assertTrue(ModelResources.RESOURCE_MAP.size() > 0);
        assertEquals(ModelResources.RESOURCE_MAP.size(), models.size());
        List<String> packageModelNames =
                models.stream()
                        .collect(Collectors.mapping(File::getName,
                                                    Collectors.toList()));
        ModelResources.RESOURCE_MAP.values().stream().forEach(resource -> {
            if (resource.startsWith("/")) {
                resource = resource.substring(1, resource.length());
            }
            assertTrue(packageModelNames.contains(resource));
        });

    }

    /**
     * Insures that package generation results in a BagIt bag-info.txt file, that
     * the file contains the required metadata fields per our spec, and that any and all
     * package metadata fields on the package state are serialized to bag-info.txt.
     *
     * The ordering of values for multi-valued metadata fields are preserved.
     *
     * @throws Exception
     */
    @Test
    public void testVerifyBagInfoContents() throws Exception {
        PackageState state = initializer.initialize(DCS_PROFILE);

        // Add some package metadata; we'll check to make sure it appears
        // in the serialized package in 'bag-info.txt'

        state.addPackageMetadata("singleValue", "foo");
        state.addPackageMetadata("multiValue", "buzz");
        state.addPackageMetadata("multiValue", "bar");
        state.addPackageMetadata("multiValue", "baz");

        OpenedPackage openedPackage =
                packager.createPackage(state, folder.getRoot());

        File bagInfo = new File(openedPackage.getBaseDirectory(), "bag-info.txt");
        assertTrue("Expected bag-info.txt file to exist: " + bagInfo, bagInfo.exists());

        Map<String, List<String>> result = parseBagItKeyValuesFile(bagInfo);

        // Required fields per our 1.0 spec (note these do not appear in package metadata)
        assertTrue("Missing expected BagIt metadata field " + BAGIT_PROFILE_ID + " in " + bagInfo,
                result.containsKey(BAGIT_PROFILE_ID));
        assertEquals("http://dataconservancy.org/formats/data-conservancy-pkg-1.0",
                result.get(BAGIT_PROFILE_ID).get(0));

        assertTrue("Missing expected BagIt metadata field " + PACKAGE_MANIFEST + " in " + bagInfo,
                result.containsKey(PACKAGE_MANIFEST));
        assertEquals("bag://TestPackage/META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM/ORE-REM.ttl",
                result.get(PACKAGE_MANIFEST).get(0));

        // Package Metadata from the state is included
        assertEquals("foo", result.get("singleValue").get(0));

        // Order of multi-valued metadata elements should be preserved
        assertTrue(result.containsKey("multiValue"));
        assertTrue(result.get("multiValue").get(0).equals("buzz"));
        assertTrue(result.get("multiValue").get(1).equals("bar"));
        assertTrue(result.get("multiValue").get(2).equals("baz"));
    }


    /**
     * Insures that unicode package metadata will end up in bag-info.txt.
     *
     * @throws Exception
     */
    @Test
    public void testUtf8BagInfoContents() throws Exception {
        PackageState state = initializer.initialize(DCS_PROFILE);

        // Add some package metadata that contains unicode
        String literal = "S\u00ED Se\u00F1or!";

        state.addPackageMetadata("unicodeString", literal);

        OpenedPackage openedPackage =
                packager.createPackage(state, folder.getRoot());

        File bagInfo = new File(openedPackage.getBaseDirectory(), "bag-info.txt");

        ByteArrayOutputStream fileContents = new ByteArrayOutputStream();
        IOUtils.copy(new java.io.FileInputStream(bagInfo), fileContents);
        assertTrue(contains(literal.getBytes(Charset.forName("UTF-8")), fileContents));

        Map<String, List<String>> result = parseBagItKeyValuesFile(bagInfo);
        assertTrue(result.containsKey("unicodeString"));
        assertEquals(literal, result.get("unicodeString").get(0));
    }

    /**
     * Insures non-binary content are encoded in UTF-8
     *
     * @throws Exception
     */
    @Test
    public void testUtf8BagContents() throws Exception {
        PackageState state = initializer.initialize(DCS_PROFILE);
        OpenedPackage openedPackage =
                packager.createPackage(state, folder.getRoot());

        List<File> characterFiles = new ArrayList<>();
        new CharacterDirectoryWalker().doWalk(openedPackage.getBaseDirectory(), characterFiles);
        assertTrue("Found no character files in package!", characterFiles.size() > 0);

        StringBuilder errors = new StringBuilder();
        Utf8Validator utf8Validator = new Utf8Validator(((message, byteOffset) -> {
            if (errors.length() > 0) {
                return;  // only log the first error.
            }
            errors.append("byte offset ").append(byteOffset).append(" ").append(message).append("\n");
        }));

        characterFiles.forEach(f -> {
            try {
                utf8Validator.validate(f);
                if (errors.length() > 0) {
                    fail("Package file " + f + " contains non-UTF8 characters (is it binary?): " + errors.toString());
                }
            } catch (IOException | ValidationException e) {
                fail("UTF-8 validation of " + f + " failed unexpectedly: " + e.getMessage());
            }
        });
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
                new HashMap<>();

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

    private Model custodialDomainObjects(OpenedPackage pkg) throws Exception {
        /* Lame, hardcoded for now */
        URI remURI =
                URI.create(String
                        .format("bag://%s/META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM/ORE-REM.ttl",
                                Packager.PACKAGE_NAME));

        Path baseDir = pkg.getBaseDirectory().getParentFile().toPath();

        Path remPath = UriUtility.resolveBagUri(baseDir, remURI);

        Model rem = ModelFactory.createDefaultModel();
        try (FileInputStream in = new FileInputStream(remPath.toFile())) {
            rem.read(in, remURI.toString(), "TURTLE");
        }

        Model domainObjects = ModelFactory.createDefaultModel();

        rem.listObjectsOfProperty(rem.getProperty(NS_ORE + "aggregates"))
                .mapWith(RDFNode::asResource)
                .mapWith(Resource::getURI)
                .mapWith(URI::create)
                .forEachRemaining(bagUri -> {
                    try (FileInputStream in =
                            new FileInputStream(UriUtility
                                    .resolveBagUri(baseDir, bagUri).toFile())) {

                        domainObjects.read(in, bagUri.toString(), "TURTLE");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        return domainObjects;
    }

    /**
     * Reads in any BagIt file that uses a ':' to delimit a keyword and value pair.
     *
     * @param bagItFile the file to read
     * @return a Map keyed by the keywords, with the List of values as they appear in the file
     * @throws IOException
     */
    private Map<String, List<String>> parseBagItKeyValuesFile(File bagItFile) throws IOException {
        Map<String, List<String>> result = new HashMap<>();

        // Used to track state; a streams no-no.  Probably should do this the old-fashioned way.
        BitSet bitSet = new BitSet(1);
        bitSet.set(0);
        StringBuilder key = new StringBuilder();

        Files.lines(bagItFile.toPath(), Charset.forName("UTF-8"))
                .flatMap(line ->
                        Stream.of(line.substring(0, line.indexOf(":")), line.substring(line.indexOf(":") + 1).trim()))
                .forEach(token -> {
                    if (bitSet.get(0)) {
                        // key
                        key.delete(0, key.length());
                        result.putIfAbsent(token, new ArrayList<>());
                        key.append(token);
                        bitSet.clear(0);
                    } else {
                        // value
                        result.get(key.toString()).add(token);
                        bitSet.set(0);
                    }
                });

        return result;
    }

    private boolean contains(byte[] candidates, ByteArrayOutputStream sink) {
        byte[] sinkBytes = sink.toByteArray();

        OUTER:
        for (int i = 0; i < sinkBytes.length; i++) {
            for (int m = 0; m < candidates.length; m++) {
                if ((0x000000FF & candidates[m]) == (0x000000FF & sinkBytes[i])) {
                    if (m + 1 < candidates.length && i + 1 < sinkBytes.length) {
                        if ((0x000000FF & candidates[m + 1]) == (0x000000FF & sinkBytes[i + 1])) {
                            return true;
                        } else {
                            m = 0;
                            continue OUTER;
                        }
                    } else if (m + 1 >= candidates.length) {
                        // we've exhausted candidate bytes
                        return true;
                    }
                } else {
                    continue OUTER;
                }
            }
        }

        return false;
    }

    /**
     * Naive class that collects all Turtle serializations that are in the 'ONT' directory.
     */
    private class OntDirectoryWalker
            extends DirectoryWalker<File> {

        public void doWalk(File baseDir, List<File> models) throws IOException {
            walk(baseDir, models);
        }

        @Override
        protected void handleFile(File file, int depth, Collection<File> results)
                throws IOException {
            if (file.getParentFile().getName().equals("ONT")
                    && file.getName().endsWith(".ttl")) {
                results.add(file);
            }
        }
    }

    /**
     * Naive implementation that attempts to exclude directories in a package that are known to contain binary content.
     * The remaining directories will be scanned, and files collected for processing.
     */
    private class CharacterDirectoryWalker
            extends DirectoryWalker<File> {

        public void doWalk(File baseDir, List<File> results) throws IOException {
            walk(baseDir, results);
        }

        @Override
        protected boolean handleDirectory(File directory, int depth, Collection<File> results) throws IOException {
            if (directory.getName().equals("bin") || directory.getName().equals("STATE")) {
                return false;
            }

            return true;
        }

        @Override
        protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
            results.add(file);
        }
    }
}
