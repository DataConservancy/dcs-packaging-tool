/*
 * Copyright 2014 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl.rules.operations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.packaging.tool.impl.rules.*;
import org.dataconservancy.packaging.tool.impl.rules.operations.Value_FileMetadata.FileAttribute;
import org.dataconservancy.packaging.tool.impl.rules.operations.Value_FileMetadata.FileType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class Value_FileMetadataTest {
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    /* Verifies that the fileType of a directory is Directory */
    @Test
    public void fileTypeDirectoryTest() throws IOException {
        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.fileType.toString());

        assertTrue(Arrays
                .equals(metadata.operate(new FileContextImpl(tmpfolder.newFolder("test"), false)),
                        new String[] {FileType.Directory.toString()}));

    }

    /* Verifies that the fileType of a file is File */
    @Test
    public void fileTypeFileTest() throws Exception {
        File file = tmpfolder.newFile("Value_FileMetadataTest");

        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.fileType.toString());

        assertTrue(Arrays.equals(metadata.operate(new FileContextImpl(file, false)),
                                 new String[] {FileType.File.toString()}));
    }

    /* Verifies that name gets the file's name */
    @Test
    public void fileNameTest() throws Exception {
        File file = tmpfolder.newFile("Value_FileMetadataTest");

        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.name.toString());

        assertTrue(Arrays.equals(metadata.operate(new FileContextImpl(file, false)),
                                 new String[] {file.getName()}));
    }

    /* Verifies that size gets the file's size */
    @Test
    public void fileSizeTest() throws Exception {
        int bytes = 14;

        File file = tmpfolder.newFile("Value_FileMetadataTest");

        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < bytes; i++) {
            out.write('x');
        }
        out.close();

        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.size.toString());

        assertTrue(Arrays.equals(metadata.operate(new FileContextImpl(file, false)),
                                 new String[] {Integer.toString(bytes)}));
    }

    /* Verifies that operands take precedence */
    @Test
    public void operandTest() throws Exception {
        File file = tmpfolder.newFile("Value_FileMetadataTest");
        File dir = tmpfolder.newFolder("moo");

        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.fileType.toString());
        metadata.setConstraints(new FileReturner[] {
                new FileReturner(new File[] {dir}),
                new FileReturner(new File[] {dir, file}),
                new FileReturner(new File[] {file})});

        assertTrue(Arrays.equals(metadata
                .operate(new FileContextImpl(dir, false)), new String[] {
                FileType.Directory.toString(), FileType.Directory.toString(),
                FileType.File.toString(), FileType.File.toString()}));
    }

    /* Verifies that creation date is retrieved */
    @Test
    public void fileCreationDateTest() throws Exception {
        File file = tmpfolder.newFile("Value_FileMetadataTest");

        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.createDate.toString());

        BasicFileAttributes fileMetadata;
        try {
            fileMetadata =
                    Files.getFileAttributeView(file.toPath(),
                                               BasicFileAttributeView.class)
                            .readAttributes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertTrue(Arrays.equals(metadata.operate(new FileContextImpl(file, false)),
                                 new String[] {DateUtility
                                         .toIso8601_DateTimeNoMillis(new Date(fileMetadata
                                                 .creationTime().toMillis()))}));
    }

    /* Verifies that last modified date is retrieved */
    @Test
    public void fileModificationDateTest() throws Exception {
        File file = tmpfolder.newFile("Value_FileMetadataTest");

        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.createDate.toString());

        BasicFileAttributes fileMetadata;
        try {
            fileMetadata =
                    Files.getFileAttributeView(file.toPath(),
                                               BasicFileAttributeView.class)
                            .readAttributes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String CREATE_DATE =
                DateUtility.toIso8601_DateTimeNoMillis(new Date(fileMetadata.creationTime().toMillis()));
        
        Thread.sleep(2000);
        
        /* Write to the file to modify it */
        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < 10; i++) {
            out.write('x');
        }
        out.close();
        
        fileMetadata =
                Files.getFileAttributeView(file.toPath(),
                                           BasicFileAttributeView.class)
                        .readAttributes();
        
        String MODIFIED_DATE = DateUtility.toIso8601_DateTimeNoMillis(new Date(fileMetadata.lastModifiedTime().toMillis()));
        
        assertTrue(CREATE_DATE != MODIFIED_DATE);
        
        metadata.setSpecifier(FileAttribute.modifiedDate.toString());

        assertTrue(Arrays.equals(metadata.operate(new FileContextImpl(file, false)),
                                 new String[] {MODIFIED_DATE}));
    }

    /* Verifies that operating on a non-readable path name causes OperationException */
    @Test (expected = OperationException.class)
    public void fileTypeDirectoryTest_Exception() throws IOException {
        File directory = tmpfolder.newFolder("dinosaurs");
        directory.delete();

        Value_FileMetadata metadata = new Value_FileMetadata();
        metadata.setSpecifier(FileAttribute.fileType.toString());

        metadata.operate(new FileContextImpl(directory, false));

    }

    private class FileReturner
            implements FileOperation {

        private File[] files;

        public FileReturner(File[] files) {
            this.files = files;
        }

        @Override
        public void setConstraints(TestOperation<?>... constraints) {
            /* nothing */
        }

        @Override
        public File[] operate(FileContext fileContext) {
            return files;
        }
    }

}
