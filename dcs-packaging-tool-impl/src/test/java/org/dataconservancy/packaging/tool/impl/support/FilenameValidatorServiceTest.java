package org.dataconservancy.packaging.tool.impl.support;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.dataconservancy.packaging.tool.impl.Privileged;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class FilenameValidatorServiceTest {
    @ClassRule
    public static TemporaryFolder tmpfolder = new TemporaryFolder();

    @Test
    @Category(Privileged.class)
    public void testSymbolicLinkCycle() throws IOException, InterruptedException {
        File tempDir = tmpfolder.newFolder("moo");

        File subdir = new File(tempDir, "cow");
        subdir.mkdir();

        Path link = Paths.get(subdir.getPath(), "link");
        link.toFile().deleteOnExit();

        try {
            Files.createSymbolicLink(link, tempDir.toPath());
        } catch (UnsupportedOperationException e) {
            /* Nothing we can do if the system doesn't support symlinks */
            return;
        }

        FilenameValidatorService validatorService = new FilenameValidatorService();

        List<String> invalidNames = validatorService.findInvalidFilenames(tempDir.toPath());
        assertEquals(0, invalidNames.size());
    }

    @Test
    public void testGoodFileName() throws IOException {
        File tmpDir = tmpfolder.newFolder("DownloadsGood");
        File file = new File(tmpDir, "25fea3503a8be1be");
        file.createNewFile();

        FilenameValidatorService validatorService = new FilenameValidatorService();

        List<String> invalidNames = validatorService.findInvalidFilenames(tmpDir.toPath());
        assertEquals(0, invalidNames.size());
    }

    @Test
    public void testBadFileName() throws IOException {
        File tmpDir = tmpfolder.newFolder("DownloadsBad");
        File file = new File(tmpDir, "25fea3503a8be1be~") ;
        file.createNewFile();

        FilenameValidatorService validatorService = new FilenameValidatorService();

        List<String> invalidNames = validatorService.findInvalidFilenames(tmpDir.toPath());
        assertEquals(1, invalidNames.size());
    }

}
