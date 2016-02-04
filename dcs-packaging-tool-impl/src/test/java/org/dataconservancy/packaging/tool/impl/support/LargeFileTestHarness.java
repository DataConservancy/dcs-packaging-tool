/*
 * Copyright 2016 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl.support;

import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Helper class that generates a directory structure that matches the provided parameters for directory depth and file count.
 * The file count will represent the number of files created at each depth.
 */
public abstract class LargeFileTestHarness {

    @ClassRule
    public static TemporaryFolder tmpfolder = new TemporaryFolder();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public File generateDirectoryTree(int treeDepth, int fileCount)
        throws IOException {
        log.info("Generating test tree structure with depth: " + treeDepth + " and a file count of: " + fileCount);
        return populateDirectory(0, treeDepth, fileCount, null);
    }

    private File populateDirectory(int depth, int maxDepth, int fileCount, File parent)
        throws IOException {

        File dir;
        if (parent == null) {
            dir = tmpfolder.newFolder("depth" + depth);
        } else {
            dir = new File(parent, "depth" + depth);
            dir.mkdir();
        }

        for (int i = 0; i < fileCount; i++) {
            File file = new File(dir, "file" + i);
            Files.createFile(file.toPath());
        }

        if (depth < maxDepth) {
            populateDirectory(depth + 1, maxDepth, fileCount, dir);
        }
        return dir;
    }

}
