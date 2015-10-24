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
package org.dataconservancy.packaging.tool.model;

import gov.loc.repository.bagit.BagItTxtReader;
import gov.loc.repository.bagit.impl.BagItTxtReaderImpl;
import gov.loc.repository.bagit.utilities.namevalue.NameValueReader;
import org.dataconservancy.packaging.tool.model.impl.PackageStateBuilderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class PackageStateBuilderImplTest {

    private PackageState state;

    private LinkedHashMap<String, List<String>> packageMetadata;
    private static final String PACKAGE_NAME = "Package-Name";
    private static final String TOOL_BUILD_REVISION = "Tool-Build-Revision";
    private static final String TOOL_BUILD_NUMBER = "Tool-Build-Number";
    private static final String TOOL_BUILD_TIMESTAMP = "Tool-Build-Timestamp";
    private static final String CONTACT_NAME = "Contact-Name";
    private static final String CONTACT_EMAIL= "Contact-Email";
    private static final String BAG_COUNT= "Bag-Count";
    private static final String BAG_GROUP_ID= "Bag-Group-Id";
    private static final String KEYWORD= "Keyword";

    private static final String packageNameString = "WillardsPlayThings";
    private static final String buildRevision = "r2345";
    private static final String buildNumber = "656";
    private static final String buildTimestamp = "2015-10-22T12:01:01Z";

    PackageStateBuilder builder;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setBuildTimeStamp("2015-10-22T12:01:01Z");
        applicationVersion.setBuildNumber("656");
        applicationVersion.setBuildRevision("r2345");
        state = new PackageState(applicationVersion);

        packageMetadata = new LinkedHashMap<>();
        packageMetadata.put(CONTACT_NAME, Arrays.asList("Willard Brown Sirk"));
        packageMetadata.put(CONTACT_EMAIL, Arrays.asList("Willard.Sirk@tastybone.com"));
        packageMetadata.put(BAG_COUNT, Arrays.asList("2 of 3"));
        packageMetadata.put(BAG_GROUP_ID, Arrays.asList("HVU295"));
        packageMetadata.put(KEYWORD, Arrays.asList("treasure", "bone", "toys", "willard"));

        state.setPackageName("WillardsPlayThings");
        state.setPackageMetadataList(packageMetadata);

        //create builder
        builder = new PackageStateBuilderImpl(temporaryFolder.newFolder("PackageStagingFolder").getName());

    }

    @Test
    public void testDeserializingPackageState() throws IOException {

        //create file for builder output
        File newFile = temporaryFolder.newFile("PackageStateBuilderImplTest.zip");
        builder.serialize(state, new FileOutputStream(newFile));
        PackageState deserializedState = builder.deserialize(new FileInputStream(newFile));
        Assert.assertEquals(deserializedState, state);

    }
    @Test
    public void testSerializingPackageState() throws IOException {

        //create file for builder output
        File newFile = temporaryFolder.newFile("PackageStateBuilderImplTest.zip");
        builder.serialize(state, new FileOutputStream(newFile));

        boolean packageToolMetadataFound = false;
        //Testing output from builder
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(newFile));

        ZipEntry entry;
        entry = zipInputStream.getNextEntry();
        if (entry.getName().equals(PackageStateBuilderImpl.PACKAGE_TOOL_METADATA_FILE)) {
            packageToolMetadataFound = true;
            BagItTxtReader reader = new BagItTxtReaderImpl("UTF-8", zipInputStream);
            NameValueReader.NameValue nameValue;

            //Make sure package name was found
            nameValue = reader.next();
            Assert.assertEquals(PACKAGE_NAME, nameValue.getName());
            Assert.assertEquals(packageNameString, nameValue.getValue());

            //Make sure TOOL BUILD REVISION was found
            nameValue = reader.next();
            Assert.assertEquals(TOOL_BUILD_REVISION, nameValue.getName());
            Assert.assertEquals(buildRevision, nameValue.getValue());

            //Make sure TOOL BUILD NUMBER was found
            nameValue = reader.next();
            Assert.assertEquals(TOOL_BUILD_NUMBER, nameValue.getName());
            Assert.assertEquals(buildNumber, nameValue.getValue());

            //Make sure TOOL BUILD TIMESTAMP was found
            nameValue = reader.next();
            Assert.assertEquals(TOOL_BUILD_TIMESTAMP, nameValue.getName());
            Assert.assertEquals(buildTimestamp, nameValue.getValue());

            while (reader.hasNext()) {
                nameValue = reader.next();
                Assert.assertTrue(packageMetadata.keySet().contains(nameValue.getName()));
                Assert.assertTrue(packageMetadata.get(nameValue.getName()).contains(nameValue.getValue()));
            }
        }
        Assert.assertTrue(packageToolMetadataFound);
    }
}
