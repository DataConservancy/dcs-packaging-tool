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

package org.dataconservancy.packaging.tool.impl.generator;

import java.net.URI;

import java.nio.file.Paths;

import java.util.UUID;

import org.junit.Test;

import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import static org.junit.Assert.assertEquals;

public class IPMUtilTest {

    URI TMP = Paths.get(System.getProperty("java.io.tmpdir")).toUri();

    @Test
    public void filePathTest() {

        String PATH = "path/to/file.jpg";
        String SUFFIX = ".bin";
        String reconstituted = IPMUtil.path(toIPMTree(PATH), SUFFIX);
        
        assertEquals(PATH + SUFFIX , reconstituted);
       

    }

    private Node toIPMTree(String path) {

        Node current = null;

        for (String segment : path.split("/")) {
            Node n = pathSegment(segment);
            if (current != null) {
                current.addChild(n);
                current = n;
            } else {
                current = n;
            }
        }

        return current;
    }

    private Node pathSegment(String name) {
        Node node = new Node(URI.create("urn:" + UUID.randomUUID().toString()));
        node.setFileInfo(new FileInfo(TMP, name));
        return node;
    }
}
