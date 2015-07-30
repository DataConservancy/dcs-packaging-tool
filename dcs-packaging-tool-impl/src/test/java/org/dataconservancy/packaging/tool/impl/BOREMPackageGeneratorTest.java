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
package org.dataconservancy.packaging.tool.impl;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;

import org.dataconservancy.packaging.tool.api.*;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.model.*;
import org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: hanh
 * Date: 1/9/14
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/test-applicationContext.xml"})
public class BOREMPackageGeneratorTest {

    BOREMPackageGenerator underTest;
    PackageDescription desc;
    PackageGenerationParameters params;

    @Autowired
    File testContentLocation;

    @Autowired
    File packageLocation;

    @Autowired
    File packageStagingLocation;

    String packageName;
    String packageLocationName;
    String packageStagingLocationName;
    String pkgBagDir;
    String bagItProfileId;
    String contactName;
    String contactEmail;
    String contactPhone;
    String contentRootLocation;
    String compressionFormat = CompressorStreamFactory.GZIP;
    String checksumAlg;
    String dataFileOneName = "Data File One.txt";
    String dataFileTwoName = "DataFileTwo.txt";
    File pathToFileOne;
    File pathToFileTwo;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        //setup package description
        desc = new PackageDescription();
        desc.setPackageOntologyIdentifier(OntologyIdentifiers.DCSBO.toString());
        Set<PackageArtifact> artifacts = new HashSet<PackageArtifact>();

        //Project artifact
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId("artifact:id:1");
        artifact.setType(DcsPackageArtifactType.Project.name());
        URI contentRootURI = testContentLocation.toURI();
        File pathToProject = new File(testContentLocation, "ProjectOne");
        artifact.setArtifactRef("ProjectOne");
        //artifact.addProperty(DcsPackageDescriptionSpec.Property.PROJECT_NAME.toString(), "Test Project");
        List<PackageRelationship> relationships = new ArrayList<PackageRelationship>();
        artifact.setRelationships(relationships);
        artifacts.add(artifact);
        desc.setRootArtifactRef(artifact.getArtifactRef());

        //Collection artifact
        artifact = new PackageArtifact();
        artifact.setId("artifact:id:2");
        artifact.setType(DcsPackageArtifactType.Collection.name());
        File pathToCollection = new File(pathToProject, "Collection One");
        artifact.setArtifactRef("ProjectOne/Collection One");
        artifact.addSimplePropertyValue(DcsBoPackageOntology.TITLE, "Test collection One");
        artifact.addSimplePropertyValue(DcsBoPackageOntology.PUBLICATION_DATE, "2013-06-15");
        artifact.addSimplePropertyValue(DcsBoPackageOntology.DESCRIPTION, "Collection used in testing borem package generator");
        
        PropertyValueGroup creator = new PropertyValueGroup();
        creator.addSubPropertyValue(DcsBoPackageOntology.NAME, "Sergent Yoda");
        
        
        Set<String> relatedArtifacts = new HashSet<String>();
        relatedArtifacts.add("artifact:id:1");
        relationships = new ArrayList<PackageRelationship>();
        relationships.add(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true,
                relatedArtifacts));
        artifact.setRelationships(relationships);
        artifacts.add(artifact);

        //DataItem artifact
        artifact = new PackageArtifact();
        artifact.setId("artifact:id:3");
        artifact.setType(DcsPackageArtifactType.DataItem.name());
        File pathToDataItem = new File(pathToCollection, "DataItem One");
        artifact.setArtifactRef("ProjectOne/Collection One/DataItem One");
        artifact.addSimplePropertyValue(DcsBoPackageOntology.TITLE, "Test DataItem One");
        artifact.addSimplePropertyValue(DcsBoPackageOntology.DESCRIPTION, "Description for Test DataItem One");
        relatedArtifacts = new HashSet<String>();
        relatedArtifacts.add("artifact:id:2");
        relationships = new ArrayList<PackageRelationship>();
        relationships.add(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true,
                relatedArtifacts));
        artifact.setRelationships(relationships);
        artifacts.add(artifact);

        //DataFile 1 artifact
        artifact = new PackageArtifact();
        artifact.setId("artifact:id:4");
        artifact.setType(DcsPackageArtifactType.DataFile.name());
        pathToFileOne = new File(pathToDataItem, dataFileOneName);
        URI test = pathToFileOne.toURI() ;;
        artifact.setArtifactRef("ProjectOne/Collection One/DataItem One/" + dataFileOneName);
        //artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_NAME.toString(), "Test DataFile One");
        //artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_PATH.toString(), test.getPath());
        artifact.addSimplePropertyValue(DcsBoPackageOntology.FORMAT, "application/text");
        relatedArtifacts = new HashSet<String>();
        relatedArtifacts.add("artifact:id:3");
        relationships = new ArrayList<PackageRelationship>();
        relationships.add(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true,
                relatedArtifacts));
        artifact.setRelationships(relationships);
        artifacts.add(artifact);

        //DataFile 2 artifact
        artifact = new PackageArtifact();
        artifact.setId("artifact:id:5");
        artifact.setType(DcsPackageArtifactType.DataFile.name());
        pathToFileTwo = new File(pathToDataItem, dataFileTwoName);
        test = pathToFileTwo.toURI() ;
        artifact.setArtifactRef("ProjectOne/Collection One/DataItem One/" + dataFileTwoName);
        //artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_NAME.toString(), "Test DataFile Two");
        relatedArtifacts = new HashSet<String>();
        relatedArtifacts.add("artifact:id:3");
        relationships = new ArrayList<PackageRelationship>();
        relationships.add(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true,
                relatedArtifacts));
        artifact.setRelationships(relationships);
        artifacts.add(artifact);

        desc.setPackageArtifacts(artifacts);

        //set up parameters
        //Set up parameters
        packageName = "WillardDoodle";
        packageLocationName = packageLocation.getAbsolutePath();
        packageStagingLocationName = packageStagingLocation.getAbsolutePath();
        contentRootLocation = testContentLocation.getAbsolutePath();
        pkgBagDir = pathToProject.getName();
        bagItProfileId = "http://dataconservancy.org/formats/data-conservancy-pkg-0.9";
        contactName = "Willy Bean";
        contactEmail = "Willy.Bean@Bushs.com";
        contactPhone = "0000000000";
        checksumAlg = "md5";


        params = new PackageGenerationParameters();

        //Create an instance of BOREMPackageGEnerator
        underTest = new BOREMPackageGenerator();
    }

    /**
     * Tests generating a well formed package, with all required parameters and the following options:
     * <ul>
     *     <li>checksum alg: md5</li>
     *     <li>compression-format: gz</li>
     *     <li>archiving-format: not specified</li>
     * </ul>
     *
     * <p/>
     *
     * Expects the de-compressed, deserialized package to contain:
     * <ul>
     *     <li>bag-info.txt file: Besides the input parameters, bag-info.txt file is expected to contain reference
     *     to the ReM of the whole package, expressed in PKG-ORE-REM parameter</li>
     *     <li>bagit.txt file</li>
     *     <li>manifest-<checksumalg>.txt files</checksumalg></li>
     *     <li>tagmanifest-<checksumalg>.txt files</checksumalg></li>
     *     <li>data/ folder</li>
     *     <li>payload files in data/ folder</li>
     *     <li>ORE-REM folder</li>
     *     <li>description files in ORE-REM/folder</li>
     * </ul>
     *
     *
     * @throws CompressorException
     * @throws ArchiveException
     * @throws IOException
     */
    @Test
    public void testGeneratingAGoodPackage() throws CompressorException, ArchiveException, IOException {
        params.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.BOREM.toString());
        params.addParam(GeneralParameterNames.PACKAGE_NAME, packageName);
        params.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocationName);
        params.addParam(GeneralParameterNames.PACKAGE_STAGING_LOCATION, packageStagingLocationName);
        params.addParam(BagItParameterNames.BAGIT_PROFILE_ID, bagItProfileId);
        params.addParam(BagItParameterNames.CONTACT_NAME, contactName);
        params.addParam(BagItParameterNames.CONTACT_EMAIL, contactEmail);
        params.addParam(BagItParameterNames.CONTACT_PHONE, contactPhone);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(BagItParameterNames.COMPRESSION_FORMAT, compressionFormat);
        params.addParam(BagItParameterNames.PKG_BAG_DIR, packageName);
       // params.addParam(GeneralParameterNames.CONTENT_ROOT_LOCATION, pkgBagDir);
        params.addParam(GeneralParameterNames.CONTENT_ROOT_LOCATION, contentRootLocation);
        Package resultedPackage = underTest.generatePackage(desc, params);

        //Decompress and de archive files
        CompressorInputStream cis = new CompressorStreamFactory()
                .createCompressorInputStream(CompressorStreamFactory.GZIP, resultedPackage.serialize());
        TarArchiveInputStream ais = (TarArchiveInputStream)
                new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis);

        //get files from archive
        Set<String> files = new HashSet<String>();
        ArchiveEntry entry = ais.getNextEntry();
        while (entry != null) {
            files.add(entry.getName().replace("\\", "/"));
            if (entry.getName().equals(packageName + "/bag-info.txt") && ais.canReadEntryData(entry)) {
                verifyBagInfoContent(ais);
            }
            if (entry.getName().equals(packageName + "/data/ProjectOne/Collection One/DataItem One/" + dataFileOneName)) {
                compareDataFile(ais, pathToFileOne);
            }
            if (entry.getName().equals(packageName + "/data/ProjectOne/Collection One/DataItem One/" + dataFileTwoName)) {
                compareDataFile(ais, pathToFileTwo);
            }
            entry = ais.getNextEntry();
        }
        assertTrue(files.contains(packageName + "/bag-info.txt"));
        assertTrue(files.contains(packageName + "/bagit.txt"));
        assertTrue(files.contains(packageName + "/tagmanifest-md5.txt"));
        assertTrue(files.contains(packageName + "/manifest-md5.txt"));
        assertTrue(files.contains(packageName + "/data/"));
        assertTrue(files.contains(packageName + "/data/ProjectOne/Collection One/DataItem One/" + dataFileOneName));
        assertTrue(files.contains(packageName + "/data/ProjectOne/Collection One/DataItem One/" + dataFileTwoName));
        assertTrue(files.contains(packageName + "/ORE-REM/"));

        assertTrue(SupportedMimeTypes.getMimeType(compressionFormat).contains(resultedPackage.getContentType()));

    }

    private void compareDataFile(TarArchiveInputStream ais, File pathToFile) throws IOException {
        InputStream fileIS = new FileInputStream(pathToFile);
        assertTrue(IOUtils.contentEquals(ais, fileIS));
    }

    private void verifyBagInfoContent(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        boolean foundRefToPackageRem = false;
        while ((line = br.readLine()) != null) {
            if (line.contains(BoremParameterNames.PKG_ORE_REM)) {
                foundRefToPackageRem = true;
            }
        }

        assertTrue(foundRefToPackageRem);
    }

    /**
     * Test attempt to generate package with the wrong format.
     *
     * Expects exception regarding incompatible format to be thrown.
     */
    @Test
    public void testGeneratingPackageWithTheWrongFormat() {
        expectedException.expect(PackageToolException.class);
        expectedException.expectMessage(PackagingToolReturnInfo.PKG_UNEXPECTED_PACKAGING_FORMAT.stringMessage());
        params.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.TEST.toString());
        params.addParam(GeneralParameterNames.PACKAGE_NAME, packageName);
        params.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocationName);
        params.addParam(GeneralParameterNames.PACKAGE_STAGING_LOCATION, packageStagingLocationName);
        params.addParam(BagItParameterNames.BAGIT_PROFILE_ID, bagItProfileId);
        params.addParam(BagItParameterNames.CONTACT_NAME, contactName);
        params.addParam(BagItParameterNames.CONTACT_EMAIL, contactEmail);
        params.addParam(BagItParameterNames.CONTACT_PHONE, contactPhone);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(BagItParameterNames.COMPRESSION_FORMAT, compressionFormat);

        underTest.generatePackage(desc, params);
    }

    /**
     * Test attempts to generate package without required params.
     *
     * Expects exception to be thrown
     */
    @Test
    public void testGeneratingPackageLackingProperParams() {
        expectedException.expect(PackageToolException.class);
        expectedException.expectMessage(PackagingToolReturnInfo.PKG_REQUIRED_PARAMS_MISSING.stringMessage());

        params.addParam(GeneralParameterNames.PACKAGE_NAME, packageName);
        params.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocationName);
        params.addParam(GeneralParameterNames.PACKAGE_STAGING_LOCATION, packageStagingLocationName);
        params.addParam(BagItParameterNames.CONTACT_NAME, contactName);
        params.addParam(BagItParameterNames.CONTACT_EMAIL, contactEmail);
        params.addParam(BagItParameterNames.CONTACT_PHONE, contactPhone);
        underTest.generatePackage(desc, params);
    }

    @Rule
    public ExpectedException expectedException= ExpectedException.none();


    /**
     * Test attempts to generate package without required params.
     *
     * Expects exception escalated from BagItPackageAssembler to be thrown.
     */
    @Test
    public void testGeneratingPackageEscalatesException() {
        expectedException.expect(PackageToolException.class);
        expectedException.expectMessage(PackagingToolReturnInfo.PKG_REQUIRED_PARAMS_MISSING.stringMessage());
        params.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.BOREM.toString());
        params.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocationName);
        params.addParam(GeneralParameterNames.PACKAGE_STAGING_LOCATION, packageStagingLocationName);
        params.addParam(BagItParameterNames.BAGIT_PROFILE_ID, bagItProfileId);
        params.addParam(BagItParameterNames.CONTACT_NAME, contactName);
        params.addParam(BagItParameterNames.CONTACT_EMAIL, contactEmail);
        params.addParam(BagItParameterNames.CONTACT_PHONE, contactPhone);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(BagItParameterNames.COMPRESSION_FORMAT, compressionFormat);

        underTest.generatePackage(desc, params);
    }
}
