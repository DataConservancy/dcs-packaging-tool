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
package org.dataconservancy.packaging.tool.model.impl;

import gov.loc.repository.bagit.BagInfoTxtWriter;
import gov.loc.repository.bagit.BagItTxtReader;
import gov.loc.repository.bagit.impl.BagInfoTxtWriterImpl;
import gov.loc.repository.bagit.impl.BagItTxtReaderImpl;
import gov.loc.repository.bagit.utilities.namevalue.NameValueReader;
import org.apache.commons.io.FileUtils;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.PackageStateBuilder;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class PackageStateBuilderImpl implements PackageStateBuilder{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String UTF_8 = "utf-8";
    private static final String PACKAGE_NAME = "Package-Name";
    private static final String TOOL_BUILD_REVISION = "Tool-Build-Revision";
    private static final String TOOL_BUILD_NUMBER = "Tool-Build-Number";
    private static final String TOOL_BUILD_TIMESTAMP = "Tool-Build-Timestamp";

    private File stagingDirectory;

    public static final String PACKAGE_TOOL_METADATA_FILE = "package-tool-metadata.txt";
    public PackageStateBuilderImpl(String stagingDirectoryName) {
        this.stagingDirectory = new File(stagingDirectoryName);
    }

    /**
     * This implementation of the {@link org.dataconservancy.packaging.tool.model.PackageStateBuilder} produce the
     * serialization of the {@link org.dataconservancy.packaging.tool.model.PackageState} object in a
     * {@link java.util.zip.ZipOutputStream} which contains multiple {@link java.util.zip.ZipEntry} of its content.
     * These are the {@link java.util.zip.ZipEntry} that can be expected to be in the {@link java.util.zip.ZipOutputStream}
     * produced by this method: Name:Value file of Package and Tool Metadata, Serialized {@code DomainObjectStore},
     * Serialized package tree {@code Node}, Serialized list of {@code DomainProfile}s relevant to the package.
     * @param state
     *        {@link org.dataconservancy.packaging.tool.model.PackageState} to serialize.
     * @param stream
     *        The {@link java.io.OutputStream} to serialize to.
     * @throws PackageToolException
     */
    @Override
    public void serialize(PackageState state, OutputStream stream) throws PackageToolException {
        //Create a staging directory for this package:
        File packageStageDir = new File(this.stagingDirectory, state.getPackageName());

        //Creating a staging directory for the package state file
        if (!packageStageDir.exists()) {
            log.info("Creating bag base dir: " + packageStageDir.getPath());
            boolean isDirCreated = packageStageDir.mkdirs();
            if (!isDirCreated) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_DIR_CREATION_EXP,
                        "Attempt to create a base directory for bag at " + packageStageDir.getPath() + " failed.");
            }
        } else {
            //If it exists remove everything in it and start fresh
            try {
                FileUtils.cleanDirectory(packageStageDir);
            } catch (IOException e) {
                log.warn("Exception thrown when cleaning existing directory: " + e.getMessage());
            }
        }
        //create zip outputstream
        ZipOutputStream zipOStream = new ZipOutputStream(stream);

        try {
            /****************************************************
             * Write Package and tool metadata to file
             ****************************************************/
            //Create file to write package and tool metadata into
            File packageToolMetadataFile = new File(packageStageDir, PACKAGE_TOOL_METADATA_FILE);
            //write package and tool metadata from state object to package-tool-metadata file
            writePackageMetadata(state, packageToolMetadataFile);
            //create package-tool-metadata file to the zip outputstream
            writeToZipOutputStream(zipOStream, packageToolMetadataFile);
            /****************************************************
             * TODO: Write DomainObjectStore to file
             ****************************************************/
             /****************************************************
             * TODO: Write DomainProfileList to file
             ****************************************************/
             /****************************************************
             * TODO: Write Package tree to file
             ****************************************************/
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e);
        } finally {
            try {
                zipOStream.close();
            } catch (IOException e) {
                log.error("Unable to close zip output stream for the serialized package state file.", e);
                throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e);            }
        }

        try {
            FileUtils.deleteDirectory(packageStageDir);
        } catch (IOException e) {
            log.warn("Could not clean up staging directory created during the serialization of package state ." + e.getMessage());

        }
    }

    @Override
    public PackageState deserialize(InputStream stream) {
        ZipInputStream zipInputStream = new ZipInputStream(stream);

        PackageState state = new PackageState();

        ZipEntry entry = null;

        try {

            while ((entry = zipInputStream.getNextEntry()) != null) {
                //Deserialize package-tool-metadata file
                if (entry.getName().equals(PackageStateBuilderImpl.PACKAGE_TOOL_METADATA_FILE)) {
                    //copy this entry into a byte array. This is to prevent BagItTxtReader from closing the whole
                    // zipInputStream after it's done reading this one entry.
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int n = 0;
                    while ((n = zipInputStream.read(buffer)) >= 0)
                        baos.write(buffer, 0, n);
                    byte[] content = baos.toByteArray();

                    BagItTxtReader reader = new BagItTxtReaderImpl("UTF-8", new ByteArrayInputStream(content));
                    NameValueReader.NameValue nameValue;
                    state.setCreationToolVersion(new ApplicationVersion());
                    while (reader.hasNext()) {
                        nameValue = reader.next();
                        if (nameValue.getName().equals(PACKAGE_NAME)) {
                            state.setPackageName(nameValue.getValue());
                        } else if (nameValue.getName().equals(TOOL_BUILD_NUMBER)) {
                            state.getCreationToolVersion().setBuildNumber(nameValue.getValue());
                        } else if (nameValue.getName().equals(TOOL_BUILD_REVISION)) {
                            state.getCreationToolVersion().setBuildRevision(nameValue.getValue());
                        } else if (nameValue.getName().equals(TOOL_BUILD_TIMESTAMP)) {
                            state.getCreationToolVersion().setBuildTimeStamp(nameValue.getValue());
                        } else {
                            state.addPackageMetadata(nameValue.getName(), nameValue.getValue());
                        }
                    }
                } //else if it's something else, parse that stream differently
            }
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e);
        }
        return state;
    }

    private void writeToZipOutputStream (ZipOutputStream outputStream, File fileToWrite) throws IOException {
        //Add packageToolMetadata to the ZipOutputStream

        //Write bagInfoFile to Package state file
        outputStream.putNextEntry(new ZipEntry(fileToWrite.getName()));
        FileInputStream fis = new FileInputStream(fileToWrite);
        byte[] buffer = new byte[1024];

        int length;
        while ((length = fis.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.closeEntry();
        // close the InputStream
        fis.close();
    }

    private void writePackageMetadata(PackageState state, File packageMetadataFile) throws IOException {
        List<String> packageMetadataValues;

        BagInfoTxtWriter writer = new BagInfoTxtWriterImpl(new FileOutputStream(packageMetadataFile), UTF_8);
        //write out package name
        writer.write(PACKAGE_NAME, state.getPackageName());
        //write out tool metadata
        writer.write(TOOL_BUILD_REVISION, state.getCreationToolVersion().getBuildRevision());
        writer.write(TOOL_BUILD_NUMBER, state.getCreationToolVersion().getBuildNumber());
        writer.write(TOOL_BUILD_TIMESTAMP, state.getCreationToolVersion().getBuildTimeStamp());
        //write out package level metadata
        //Loop through the set of metadata fields for the package
        for (String metadataField : state.getMetadataFields()) {
            //retrieve the value for this particular field
            packageMetadataValues = state.getPackageMetadataValues(metadataField);
            //print value into a comma delimited list
            packageMetadataValues.forEach(metadataValue -> {
                writer.write(metadataField, metadataValue);
            });
        }

        writer.close();

    }

}
