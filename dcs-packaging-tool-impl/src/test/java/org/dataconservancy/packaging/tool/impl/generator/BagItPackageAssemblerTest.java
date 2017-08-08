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
package org.dataconservancy.packaging.tool.impl.generator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.NullInputStream;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.impl.ResourceConstrained;
import org.junit.Assert;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;

import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.model.BagItParameterNames;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageToolException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Paths;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/test-applicationContext.xml"})
public class BagItPackageAssemblerTest {

    BagItPackageAssembler underTest;

    @Autowired
    File packageLocation;

    @Autowired
    File packageStagingLocation;

    String packageName;
    String packageLocationName;
    String packageStagingLocationName;
    String contentLocation;
    File contentLocationFile;
    URI contentLocationURI;
    String bagItProfileId;
    String contactName;
    String contactEmail;
    String contactPhone;
    String checksumAlg;

    String stateDir = "META-INF/org.dataconservancy.packaging/STATE";
    String pkgInfoDir = "META-INF/org.dataconservancy.packaging/PKG-INFO";
    String remDir = "META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM";
    String ontologyDir = "META-INF/org.dataconservancy.packaging/ONT";
    String RemURI;

    Map<String, List<String>> packageMetadata = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws URISyntaxException {
        //Set up parameters
        packageName = "WillardDoodle";
        packageLocationName = packageLocation.getAbsolutePath();
        packageStagingLocationName = packageStagingLocation.getAbsolutePath();
        bagItProfileId = "http://dataconservancy.org/formats/data-conservancy-pkg-0.9";
        contactName = "Willy Bean";
        contactEmail = "Willy.Bean@Bushs.com";
        contactPhone = "0000000000";
        checksumAlg = "md5";
        RemURI = "bag://" + remDir + "remFile.ttl";

        contentLocation = this.getClass().getResource("/TestContent/").getPath();
        contentLocationFile = new File(contentLocation);
        contentLocationURI = contentLocationFile.toURI();

        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, CompressorStreamFactory.GZIP);

        //Set up package assembler
        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);
    }

    @After
    public void cleanUp() {
        File packageDir = new File(packageLocationName);
        cleanupDirectory(packageDir);

        File tempDir = new File(packageName);
        if (tempDir.exists()) {
            cleanupDirectory(tempDir);
            if (!tempDir.delete()) {
                log.info("Couldn't delete temporary directory: " + tempDir.getPath());
            }
        }

        File tempArchive = new File(packageName + ".tar");
        tempArchive.deleteOnExit();
        File fakeArchive = new File(packageName + ".fake");
        fakeArchive.deleteOnExit();

        assertTrue("Failed to delete package staging directory " + packageStagingLocation,
                FileUtils.deleteQuietly(packageStagingLocation));
    }

    @Test
    public void testReserveURIForMetadataFile() {
        String filePath = "myProject/dataFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        String expectedURI = "bag://" + packageName + "/" + pkgInfoDir + "/"
                + filePath;
        assertTrue(expectedURI.equals(result.toString()));
    }

    @Test
    public void testReserveURIForMultipleDataFiles() {
        String filePath1 = "myProject/dataFile1.txt";
        URI result1 = underTest.reserveResource(filePath1, PackageResourceType.DATA);
        String expectedURI1 = "bag://" + packageName + "/"
                +  "data" + "/" + filePath1;
        log.debug("URI expected: " + expectedURI1);
        log.debug("URI result: " + result1.toString());
        assertTrue(expectedURI1.equals(result1.toString()));

        String filePath2 = "myProject/dataFile2.txt";
        URI result2 = underTest.reserveResource(filePath2, PackageResourceType.DATA);
        String expectedURI2 = "bag://" + packageName + "/"
                +  "data" + "/" + filePath2;
        assertTrue(expectedURI2.equals(result2.toString()));
    }

    @Test
    public void testReserveURIForDataFile() {
        String filePath = "myProject/myTextFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.DATA);
        String expectedURI = "bag://" + packageName + "/"
                +  "data" + "/" + filePath ;
        assertTrue(expectedURI.equals(result.toString()));
    }
    
    @Test
    public void testReserveURIForDirectory() {
        String filePath = "myProject/myTextFile.txt";
        URI result = underTest.reserveDirectory(filePath, PackageResourceType.DATA);
        String expectedURI = "bag://" + packageName + "/"
                +  "data" + "/" + filePath + '/';
        assertTrue(expectedURI.equals(result.toString()));

        final File stagingDirectory = findStagingDirectory(packageStagingLocation);

        assertTrue(Paths
                   .get(URI.create(
                           stagingDirectory.toURI().toString()
                           + packageName + "/data/" + filePath)).toFile().exists());
    }

    @Test
    public void testReserveURIForPackageStateFile() {
        String filePath = "packageStateFile.zip";
        URI result = underTest.reserveResource(filePath, PackageResourceType.PACKAGE_STATE);
        String expectedURI = "bag://" + packageName + "/" + stateDir +"/" + filePath;
        assertTrue(expectedURI.equals(result.toString()));
    }

    @Test
    public void testReserveURIForRemFile() {
        String filePath = "ORE-ReMFile";
        URI result = underTest.reserveResource(filePath, PackageResourceType.ORE_REM);
        String expectedURI = "bag://" + packageName + "/" + remDir +"/" + filePath;
        assertTrue(expectedURI.equals(result.toString()));
    }

    @Test
    public void testReserveURIForOntologyFile() {
        String filePath = "ontology.xml";
        URI result = underTest.reserveResource(filePath, PackageResourceType.ONTOLOGY);
        String expectedURI = "bag://" + packageName + "/" + ontologyDir +"/" + filePath;
        assertTrue(expectedURI.equals(result.toString()));
    }

    @Test
    public void testReserveDuplicateResource() throws Exception {
        underTest.reserveResource("path/to/file.txt", PackageResourceType.DATA);
        try {
            underTest.reserveResource("path/to/file.txt", PackageResourceType.DATA);
            fail("Expected a PackageToolException to be thrown!");
        } catch (PackageToolException e) {
            // expected
            assertEquals(409, e.getCode());
        }
    }

    @Test
    public void testCreateDuplicateResource() throws Exception {
        underTest.createResource("path/to/file.txt", PackageResourceType.DATA, new NullInputStream(-1));
        try {
            underTest.createResource("path/to/file.txt", PackageResourceType.DATA, new NullInputStream(-1));
            fail("Expected a PackageToolException to be thrown!");
        } catch (PackageToolException e) {
            // expected
            assertEquals(409, e.getCode());
        }
    }

    @Category(ResourceConstrained.class)
    @Test
    public void testAssembleTarWithLargeFile() throws Exception {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.removeParam(BagItParameterNames.ARCHIVING_FORMAT);
        params.addParam(BagItParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.TAR);

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        underTest.createResource("path/to/largefile.bin", PackageResourceType.DATA,
                new NullInputStream(077777777777L + 1));
        underTest.assemblePackage();
    }

    @Test
    public void testPutResource() throws IOException {
        //Reserve a URI
        String filePath = "metadataFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        String expectedURI = "bag://" + packageName + "/" + pkgInfoDir + "/"
                + filePath;
        assertTrue(expectedURI.equals(result.toString()));

        //Put content into the space specified by the URI
        String fileContent = "Today is a rainy day: good for napping, bad for coding.";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        //Verify that the content at the specified URI is retrievable and is the same as the input content.
        URI contentURI = underTest.getResolvableURI(result);
        BufferedReader br = new BufferedReader(new FileReader(contentURI.getPath()));
        String line;
        while ((line = br.readLine()) != null) {
            assertTrue(fileContent.equals(line));
        }
        br.close();
    }

    @Test
    public void testCreateResourceForOneDataFile() throws IOException, URISyntaxException {
        // Prepare and create the resource

        InputStream df1IS = this.getClass().getResourceAsStream("/TestContent/ProjectOne/Collection One/DataItem One/Data File One.txt");
        URL df1URL = this.getClass().getResource("/TestContent/ProjectOne/Collection One/DataItem One/Data File One.txt");
        URI result = underTest.createResource("/ProjectOne/Collection One/DataItem One/Data File One.txt", PackageResourceType.DATA, df1IS);
        // Verify that the URI is as expected
        String expectedURI = "bag://" + packageName + "/data/" + contentLocationURI.relativize(df1URL.toURI()).toString();
        assertTrue(expectedURI.equals(result.toString()));

        // Verify the content of the URI is as expected
        URI contentURI = underTest.getResolvableURI(result);
        File fileWithContent = new File(contentURI);
        InputStream inputStream = new FileInputStream(fileWithContent);

        IOUtils.contentEquals(inputStream, df1IS);
        inputStream.close();
    }

    @Test
    public void testCreateResourceForTwoDataFilesWithSameName() throws IOException, URISyntaxException {
        // Prepare and create the resource
        InputStream df1ISa = this.getClass().getResourceAsStream("/TestContent/ProjectOne/Collection One/DataItem One/Data File One.txt");
        URL df1URLa = this.getClass().getResource("/TestContent/ProjectOne/Collection One/DataItem One/Data File One.txt");
        URI resulta = underTest.createResource("/ProjectOne/Collection One/DataItem One/Data File One.txt", PackageResourceType.DATA, df1ISa);

        InputStream df1ISb = this.getClass().getResourceAsStream("/TestContent/ProjectOne/CollectionTwo/DataItemTwo/DataFileOne.txt");
        URL df1URLb = this.getClass().getResource("/TestContent/ProjectOne/CollectionTwo/DataItemTwo/DataFileOne.txt");
        URI resultb = underTest.createResource("/ProjectOne/CollectionTwo/DataItemTwo/DataFileOne.txt", PackageResourceType.DATA, df1ISb);

        // Verify that the URI is as expected
        String expectedURIa = "bag://" + packageName + "/data/" + contentLocationURI.relativize(df1URLa.toURI()).toString();
        assertTrue(expectedURIa.equals(resulta.toString()));

        // Verify that the URI is as expected
        String expectedURIb = "bag://" + packageName + "/data/" + contentLocationURI.relativize(df1URLb.toURI()).toString();
        assertTrue(expectedURIb.equals(resultb.toString()));

        // Verify the content of the URI is as expected
        URI contentURIa = underTest.getResolvableURI(resulta);
        URI contentURIb = underTest.getResolvableURI(resultb);
        File fileWithContenta = new File(contentURIa);
        File fileWithContentb = new File(contentURIb);
        InputStream inputStreama = new FileInputStream(fileWithContenta);
        InputStream inputStreamb = new FileInputStream(fileWithContentb);

        IOUtils.contentEquals(inputStreama, df1ISa);
        IOUtils.contentEquals(inputStreamb, df1ISa);

        inputStreama.close();
        inputStreamb.close();
    }

    /**
     * Test assembling a bag which contain a data file and a metadata file.
     * @throws IOException
     */
    private void testAssembleBag_TAR_GZIP(String dataFileName) throws IOException, CompressorException, ArchiveException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.TAR);
        params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, CompressorStreamFactory.GZIP);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, "sha1");

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        //Reserve a URI for data file
        String filePath = "myProject/" + dataFileName;
        URI result = underTest.reserveResource(filePath, PackageResourceType.DATA);
        String expectedURI = "bag://" + packageName + "/" + "data"
                + "/"  + filePath;
        log.debug("URI expected: " + expectedURI);
        log.debug("URI result: " + result.toString());
        assertTrue(expectedURI.equals(result.toString()));

        //Put content into the space specified by the URI
        String fileContent = "This is the data file. data data data data data data data data data data data data.";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        //Reserve a URI for metadata file
        filePath = "metadataFile.txt";
        result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        expectedURI = "bag://" + packageName + "/"  + pkgInfoDir + "/" + filePath;
        assertTrue(expectedURI.equals(result.toString()));

        //Put content into the space specified by the URI
        fileContent = "This is the metadata file. metadata metadata metadata metadata metadata metadata metadata .";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, serializedPackage);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis)) {

            Set<String> files = new HashSet<>();
            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                files.add(entry.getName().replace("\\", "/"));
                entry = ais.getNextEntry();
            }

            //Make sure that the packageName is set properly (packageName + contentype extension)
            String expectedPackageName = packageName.concat(".tar").concat(".gz");
            Assert.assertEquals(expectedPackageName, pkg.getPackageName());

            // There should be 10 files: the 2 files above, bagit.txt, bag-info.txt
            // directories data and data/myProject, 2 manifest files and 2 tag-manifest files
            // and the directory structure under META-INF
            assertEquals(16, files.size());

            // make sure that expected files are in there
            String pathSep = "/";
            String bagFilePath = packageName + pathSep;
            assertTrue(files.contains(bagFilePath + "bagit.txt"));
            assertTrue(files.contains(bagFilePath + "bag-info.txt"));
            assertTrue(files.contains(bagFilePath + "META-INF" + pathSep + "org.dataconservancy.packaging" + pathSep + "PKG-INFO" + pathSep + "metadataFile.txt"));
            assertTrue(files.contains(bagFilePath + "data" + pathSep));
            assertTrue(files.contains(bagFilePath + "data" + pathSep + "myProject" + pathSep));
            assertTrue(files.contains(bagFilePath + "data" + pathSep + "myProject" + pathSep + dataFileName));
            assertTrue(files.contains(bagFilePath + "META-INF" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/PKG-INFO" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/STATE" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/ONT" + pathSep));
        }
    }

    /**
     * Test assembling a bag with a long name to ensure that tar archive out put properly handle long file name
     * @throws CompressorException
     * @throws ArchiveException
     * @throws IOException
     */
    @Test
    public void testAssembleBag_WithLongFileName() throws CompressorException, ArchiveException, IOException {
        testAssembleBag_TAR_GZIP("datafileWithLoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooName.txt");
    }

    /**
     * Test assembling a bag with a long name to ensure that tar archive out put properly handle long file name
     * @throws CompressorException
     * @throws ArchiveException
     * @throws IOException
     */
    @Test
    public void testAssembleBag() throws CompressorException, ArchiveException, IOException {
        testAssembleBag_TAR_GZIP("datafile.txt");
    }
    /**
     * Test assembling a bag which contain a data file and a metadata file with ZIP format
     * @throws IOException
     */
    @Test
    public void testAssembleZipBag() throws IOException, CompressorException, ArchiveException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.ZIP);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, "sha1");

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        //Reserve a URI for data file
        String filePath = "myProject/dataFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.DATA);
        String expectedURI = "bag://" + packageName + "/" + "data"
                + "/"  + filePath;
        log.debug("URI expected: " + expectedURI);
        log.debug("URI result: " + result.toString());
        assertTrue(expectedURI.equals(result.toString()));

        //Put content into the space specified by the URI
        String fileContent = "This is the data file. data data data data data data data data data data data data.";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        //Reserve a URI for metadata file
        filePath = "metadataFile.txt";
        result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        expectedURI = "bag://" + packageName + "/" + pkgInfoDir + "/" + filePath;
        assertTrue(expectedURI.equals(result.toString()));

        //Put content into the space specified by the URI
        fileContent = "This is the metadata file. metadata metadata metadata metadata metadata metadata metadata .";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, serializedPackage)) {

            Set<String> files = new HashSet<>();
            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                files.add(entry.getName().replace("\\", "/"));
                entry = ais.getNextEntry();
            }

            //Make sure that the packageName is set properly (packageName + contentype extension)
            String expectedPackageName = packageName.concat(".zip");
            Assert.assertEquals(expectedPackageName, pkg.getPackageName());


            // There should be 10 files: the 2 files above, bagit.txt, bag-info.txt
            // directories data and data/myProject, 2 manifest files and 2 tag-manifest files
            // and the directory structure under META-INF
            assertEquals(16, files.size());

            // make sure that expected files are in there
            String pathSep = "/";
            String bagFilePath = packageName + pathSep;
            assertTrue(files.contains(bagFilePath + "bagit.txt"));
            assertTrue(files.contains(bagFilePath + "bag-info.txt"));
            assertTrue(files.contains(bagFilePath + "META-INF" + pathSep + "org.dataconservancy.packaging" + pathSep + "PKG-INFO" + pathSep + "metadataFile.txt"));
            assertTrue(files.contains(bagFilePath + "data" + pathSep));
            assertTrue(files.contains(bagFilePath + "data" + pathSep + "myProject" + pathSep));
            assertTrue(files.contains(bagFilePath + "data" + pathSep + "myProject" + pathSep + "dataFile.txt"));
            assertTrue(files.contains(bagFilePath + "META-INF" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/PKG-INFO" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/STATE" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/ONT" + pathSep));
        }
    }

    /**
     * Test assembling a bag which contain a data file and a metadata file with TAR format
     * @throws IOException
     */
    @Test
    public void testAssembleTARBagNoCompression() throws IOException, CompressorException, ArchiveException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.TAR);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, "sha1");

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        //Reserve a URI for data file
        String filePath = "myProject/dataFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.DATA);
        String expectedURI = "bag://" + packageName + "/" + "data"
                + "/"  + filePath;
        log.debug("URI expected: " + expectedURI);
        log.debug("URI result: " + result.toString());
        assertTrue(expectedURI.equals(result.toString()));

        //Put content into the space specified by the URI
        String fileContent = "This is the data file. data data data data data data data data data data data data.";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        //Reserve a URI for metadata file
        filePath = "metadataFile.txt";
        result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        expectedURI = "bag://" + packageName + "/"  + pkgInfoDir + "/" + filePath;
        assertTrue(expectedURI.equals(result.toString()));

        //Put content into the space specified by the URI
        fileContent = "This is the metadata file. metadata metadata metadata metadata metadata metadata metadata .";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, serializedPackage)) {
            
            Set<String> files = new HashSet<>();
            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                files.add(entry.getName().replace("\\", "/"));
                entry = ais.getNextEntry();
            }

            //Make sure that the packageName is set properly (packageName + contentype extension)
            String expectedPackageName = packageName.concat(".tar");
            Assert.assertEquals(expectedPackageName, pkg.getPackageName());

            // There should be 10 files: the 2 files above, bagit.txt, bag-info.txt
            // directories data and data/myProject, 2 manifest files and 2 tag-manifest files
            assertEquals(16, files.size());

            // make sure that expected files are in there
            String pathSep = "/";
            String bagFilePath = packageName + pathSep;
            assertTrue(files.contains(bagFilePath + "bagit.txt"));
            assertTrue(files.contains(bagFilePath + "bag-info.txt"));
            assertTrue(files.contains(bagFilePath + "META-INF" + pathSep +"org.dataconservancy.packaging" + pathSep + "PKG-INFO" + pathSep +"metadataFile.txt"));
            assertTrue(files.contains(bagFilePath + "data" + pathSep));
            assertTrue(files.contains(bagFilePath + "data" + pathSep + "myProject" + pathSep));
            assertTrue(files.contains(bagFilePath + "data" + pathSep + "myProject" + pathSep + "dataFile.txt"));
            assertTrue(files.contains(bagFilePath + "META-INF" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/PKG-INFO"+ pathSep ));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/STATE" + pathSep));
            assertTrue(files.contains(bagFilePath + "META-INF/org.dataconservancy.packaging/ONT" + pathSep));
        }
    }

    /**
     * Test that the bag-info.txt file contains the parameter information passed in, including
     * parameters passed in after the initialization.
     *
     * Note that the package being generated in this case is empty, so the bag size and payload
     * oxum values will be 0.
     * @throws CompressorException
     * @throws ArchiveException
     * @throws IOException
     */
    @Test
    @Ignore("TODO: Is 'Package-Name' really expected to be in bag-info.txt?")
    public void testBagItInfoFile() throws CompressorException, ArchiveException, IOException {
        final String paramName = "TEST_PARAMETER";
        final String paramValue = "test parameter";
        underTest.addParameter(paramName, paramValue);

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, serializedPackage);
                TarArchiveInputStream ais = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis)) {
            
            String bagInfo = "";
            TarArchiveEntry entry = ais.getNextTarEntry();
            while (entry != null) {
                if (entry.getName().contains("bag-info.txt")) {
                    byte[] content = new byte[(int)entry.getSize()];
                    ais.read(content, 0, (int)entry.getSize());
                    bagInfo = new String(content);
                    break;
                }
                entry = ais.getNextTarEntry();
            }

            // Test that expected initial parameters are present
            String expected = GeneralParameterNames.PACKAGE_NAME + ": " + packageName;
            assertTrue("Expected to find: " + expected, bagInfo.contains(expected));

            // These two values should be 0 since there is nothing in the test package this time.
            expected = BagItParameterNames.BAG_SIZE + ": 0";
            assertTrue("Expected to find: " + expected, bagInfo.contains(expected));
            expected = BagItParameterNames.PAYLOAD_OXUM + ": 0";
            assertTrue("Expected to find: " + expected, bagInfo.contains(expected));

            // Test the post-init parameter
            expected = paramName + ": " + paramValue;
            assertTrue("Expected to find: " + expected, bagInfo.contains(expected));
        }
    }

    /**
     * Test that the assembler includes the proper manifest file (and no other manifest files)
     * @throws IOException
     */
    @Test
    public void testSingleChecksum() throws IOException, CompressorException, ArchiveException {
        // The default setup only uses md5 checksum, so we'll stick with that one

        //Reserve a URI for data file and put it in package
        String filePath = "myProject/dataFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.DATA);
        String fileContent = "This is the data file. data data data data data data data data data data data data.";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        //Reserve a URI for metadata file and put it in package
        filePath = "metadataFile.txt";
        result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        fileContent = "This is the metadata file. metadata metadata metadata metadata metadata metadata metadata .";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, serializedPackage);
                TarArchiveInputStream ais = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis)) {

            Set<String> files = new HashSet<>();
            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                files.add(entry.getName());
                entry = ais.getNextEntry();
            }

            // make sure that expected files are in there
            String bagFilePath = packageName + "/";
            assertTrue(files.contains(bagFilePath + "manifest-md5.txt"));
            assertTrue(files.contains(bagFilePath + "tagmanifest-md5.txt"));
            assertFalse(files.contains(bagFilePath + "manifest-sha1.txt"));
            assertFalse(files.contains(bagFilePath + "tagmanifest-sha1.txt"));
        }
    }

    /**
     * Test that the assembler generates both manifest files one for sha1 and one for md5
     * @throws IOException
     */
    @Test
    public void testMultipleChecksum() throws IOException, CompressorException, ArchiveException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.TAR);
        params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, CompressorStreamFactory.GZIP);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksumAlg);
        params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, "sha1");

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        //Reserve a URI for data file and put it in package
        String filePath = "myProject/dataFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.DATA);
        String fileContent = "This is the data file. data data data data data data data data data data data data.";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        //Reserve a URI for metadata file and put it in package
        filePath = "metadataFile.txt";
        result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        fileContent = "This is the metadata file. metadata metadata metadata metadata metadata metadata metadata .";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, serializedPackage);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis)) {

            Set<String> files = new HashSet<>();
            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                files.add(entry.getName());
                entry = ais.getNextEntry();
            }

            // make sure that expected files are in there
            String bagFilePath = packageName + "/";
            assertTrue(files.contains(bagFilePath + "manifest-md5.txt"));
            assertTrue(files.contains(bagFilePath + "tagmanifest-md5.txt"));
            assertTrue(files.contains(bagFilePath + "manifest-sha1.txt"));
            assertTrue(files.contains(bagFilePath + "tagmanifest-sha1.txt"));
        }
    }

    /**
     * Test that the assembler includes a manifest file for the default checksum
     * @throws IOException
     */
    @Test
    public void testDefaultChecksum() throws IOException, CompressorException, ArchiveException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.TAR);
        params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, CompressorStreamFactory.GZIP);
        // No checksum parameter is given, so md5 should be used by default

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        //Reserve a URI for data file and put it in package
        String filePath = "myProject/dataFile.txt";
        URI result = underTest.reserveResource(filePath, PackageResourceType.DATA);
        String fileContent = "This is the data file. data data data data data data data data data data data data.";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        //Reserve a URI for metadata file and put it in package
        filePath = "metadataFile.txt";
        result = underTest.reserveResource(filePath, PackageResourceType.METADATA);
        fileContent = "This is the metadata file. metadata metadata metadata metadata metadata metadata metadata .";
        underTest.putResource(result, new ByteArrayInputStream(fileContent.getBytes()));

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, serializedPackage);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis)) {

            Set<String> files = new HashSet<>();
            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                files.add(entry.getName());
                entry = ais.getNextEntry();
            }

            // make sure that expected files are in there
            String bagFilePath = packageName + "/";
            assertTrue(files.contains(bagFilePath + "manifest-md5.txt"));
            assertTrue(files.contains(bagFilePath + "tagmanifest-md5.txt"));
            assertFalse(files.contains(bagFilePath + "manifest-sha1.txt"));
            assertFalse(files.contains(bagFilePath + "tagmanifest-sha1.txt"));
        }
    }

    @Test
    public void testValidArchiveValidCompressionAssemblesAsSpecified() throws CompressorException, ArchiveException, IOException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.TAR);
        params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, CompressorStreamFactory.GZIP);

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        // no data files need to be added, as the bag-it files will be sufficient to test

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, serializedPackage);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis)) {

            assertNotNull(ais.getNextEntry());
        }
    }

    @Test
    public void testExplodedArchiveFormatProducesNullPackageAsSpecified() {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, "exploded");

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        Package pkg = underTest.assemblePackage();

        assertNull(pkg);
    }

    @Test
    public void testInvalidArchiveThrowsError() {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, "fake");

        expected.expect(PackageToolException.class);
        expected.expectMessage("One or more initial parameters for the package assembler was invalid : Specified archiving format <fake> is not supported. The supported archiving formats are: ar, cpio, jar, tar, zip, exploded.");

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        // no data files need to be added, as the bag-it files will be sufficient to test
    }

    @Test
    public void testInvalidCompressionThrowsError() {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, "fake");

        // no data files need to be added, as the bag-it files will be sufficient to test

        expected.expect(PackageToolException.class);
        expected.expectMessage("One or more initial parameters for the package assembler was invalid : " +
                "Specified compression format fake is not supported. The supported compression formats are: " +
                "gz (or gzip), bzip2, pack200, none.");

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);
    }

    @Test
    public void testValidArchiveNoCompressionAssemblesUncompressedArchive() throws ArchiveException, IOException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, ArchiveStreamFactory.TAR);

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        // no data files need to be added, as the bag-it files will be sufficient to test

        Package pkg = underTest.assemblePackage();

        try (
                InputStream serializedPackage = pkg.serialize();
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, serializedPackage)) {

            assertNotNull(ais.getNextEntry());
        }
    }

    @Test
    public void testNoArchiveValidCompressionAssemblesCompressedTar() throws CompressorException, ArchiveException, IOException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);
        params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, CompressorStreamFactory.GZIP);

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        // no data files need to be added, as the bag-it files will be sufficient to test

        Package pkg = underTest.assemblePackage();

        try (   
                InputStream serializedPackage = pkg.serialize();
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, serializedPackage);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, cis)) {

            assertNotNull(ais.getNextEntry());
        }
    }

    @Test
    public void testNoArchiveNoCompressionAssemblesUncompressedTar() throws ArchiveException, IOException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        setupCommonPackageParams(params, packageMetadata);

        underTest = new BagItPackageAssembler();
        underTest.init(params, packageMetadata);

        // no data files need to be added, as the bag-it files will be sufficient to test

        Package pkg = underTest.assemblePackage();

        try (
                InputStream serializedPackage = pkg.serialize();
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, serializedPackage)) {

            assertNotNull(ais.getNextEntry());
        }
    }

    private void setupCommonPackageParams(PackageGenerationParameters params, Map<String, List<String>> packageMetadata) {
        params.addParam(GeneralParameterNames.PACKAGE_NAME, packageName);
        params.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocationName);
        params.addParam(GeneralParameterNames.PACKAGE_STAGING_LOCATION, packageStagingLocationName);
        params.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.BOREM.toString());
        // Arguably the profile id could be both a package metadata field and a generation parameter.
        params.addParam(BagItParameterNames.BAGIT_PROFILE_ID, bagItProfileId);

        packageMetadata.put(BagItParameterNames.BAGIT_PROFILE_ID, Collections.singletonList(bagItProfileId));
        packageMetadata.put(BagItParameterNames.CONTACT_NAME, Collections.singletonList(contactName));
        packageMetadata.put(BagItParameterNames.CONTACT_EMAIL, Collections.singletonList(contactEmail));
        packageMetadata.put(BagItParameterNames.CONTACT_PHONE, Collections.singletonList(contactPhone));
        packageMetadata.put(BagItParameterNames.PACKAGE_MANIFEST, Collections.singletonList(RemURI));
    }

    private void cleanupDirectory(File directory) {
        if (!directory.isDirectory()) return;

        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                cleanupDirectory(f);
            }

            if (!f.delete()) {
                log.info("Couldn't delete: " + f.getPath());
            }
        }
    }

    private static File findStagingDirectory(File baseDir) {
        assertNotNull("Supplied baseDir must not be null", baseDir);
        final File[] candidateDirs = baseDir.listFiles(File::isDirectory);
        assertNotNull("Expected to find a staging directory under '" + baseDir + "', but no directories were found.",
                candidateDirs);
        assertEquals("Expected to find a single staging directory under '" + baseDir + "', but found " +
                candidateDirs.length + " instead: " + Stream.of(candidateDirs).map(File::toString).collect(Collectors.joining(",")), 1, candidateDirs.length);

        return candidateDirs[0];
    }
}