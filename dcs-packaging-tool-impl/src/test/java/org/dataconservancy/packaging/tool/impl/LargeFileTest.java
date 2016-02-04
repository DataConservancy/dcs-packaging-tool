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
package org.dataconservancy.packaging.tool.impl;

import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.impl.support.LargeFileTestHarness;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * Test to try to identify crashes when loading trees with large file counts.
 * File count parameters can be changed by running with:
 * -DfileCount=n To set the number of files that should be created in each directory of the tree
 * -DdirectoryDepth=n To set the depth of the directory tree.
 */
public class LargeFileTest extends LargeFileTestHarness {

    private IPMService ipmService;
    private final URIGenerator uriGenerator = new SimpleURIGenerator();
    private int fileCount;
    private int directoryDepth;

    @Before
    public void setup() throws IOException {
        fileCount = Integer.valueOf(System.getProperty("fileCount"));
        directoryDepth = Integer.valueOf(System.getProperty("directoryDepth"));

        ipmService = new IPMServiceImpl(uriGenerator);
    }

    /**
     * Initial large file test simply calls the IpmService create tree method and asserts that the resulting node is not null.
     * The expectation is that if this class is the culprit of the crash this test will fail with an exception.
     * @throws IOException
     */
    @Test
    public void testIpmServiceGenerateTree() throws IOException {
        File testDirectory = generateDirectoryTree(directoryDepth, fileCount);

        Node resultantTree = ipmService.createTreeFromFileSystem(testDirectory.toPath());

        assertNotNull(resultantTree);
    }

}
