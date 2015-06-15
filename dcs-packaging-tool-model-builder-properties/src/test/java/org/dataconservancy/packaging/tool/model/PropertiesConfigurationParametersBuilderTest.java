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
package org.dataconservancy.packaging.tool.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PropertiesConfigurationParametersBuilderTest {

    private PackageGenerationParameters parameters;
    private PackageGenerationParametersBuilder underTest;
    
    @Rule
    public final TemporaryFolder tmpfolder = new TemporaryFolder();
    
    @Before
    public void setup() {
        parameters = new PackageGenerationParameters();
        underTest = new PropertiesConfigurationParametersBuilder();

        List<String> checksumAlgs = new ArrayList<String>();
        checksumAlgs.add("sha1");
        checksumAlgs.add("md5");
        parameters.addParam(BagItParameterNames.CHECKSUM_ALGORITHMS, checksumAlgs);

        List<String> creatorName = new ArrayList<String>();
        creatorName.add("Alfred Sirk");
        parameters.addParam(BagItParameterNames.CONTACT_NAME, creatorName);
    }

    @Test
    public void testWriteAndReadPackageGenerationParameters() throws IOException, ParametersBuildException {

        File tmp = tmpfolder.newFile("params");

        OutputStream os = new FileOutputStream(tmp);
        underTest.buildParameters(parameters, os);
        os.close();

        PackageGenerationParameters deserializedParams = underTest.buildParameters(new FileInputStream(tmp));
        assertEquals(parameters, deserializedParams);
    }

    /**
     * Test parsing "samplePackageGenParams.properties" file.
     * The assert statements in this test is based on direct knowledge of what's in the
     * "samplePackageGenParams.properties" file. Modifying or removing the file could result in this test's failure.
     */
    @Test
    public void testParsingPropertiesFile() throws ParametersBuildException {
        InputStream in = getClass().getResourceAsStream("/samplePackageGenParams.properties");

        assertNotNull(in);
        PackageGenerationParameters paramsFromFile = underTest.buildParameters(in);

        assertEquals(12, paramsFromFile.getKeys().size());
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.PACKAGE_FORMAT_ID));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.PACKAGE_NAME));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.SOURCE_ORG));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.ORG_ADDRESS));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.PACKAGE_NAME));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.CONTACT_NAME));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.CONTACT_EMAIL));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.CONTACT_PHONE));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.PACKAGE_LOCATION));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.COMPRESSION_FORMAT));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.ARCHIVING_FORMAT));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.CHECKSUM_ALGORITHMS));
        assertTrue(paramsFromFile.getKeys().contains(BagItParameterNames.BAGIT_PROFILE_ID));
    }


}
