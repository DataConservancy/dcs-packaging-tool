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
package org.dataconservancy.packaging.tool.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PackageTreeTest {
    @Test
    public void testRoot() {
        PackageTree tree = new PackageTree();
        
        PackageArtifact pa = new PackageArtifact();
        pa.setId("bunny:dancer");
        
        PackageNode root = new PackageNode(pa);
        
        tree.setRoot(root);
        
        assertEquals(root, tree.getRoot());
    }

    @Test
    public void testNodeMapping() {
        PackageTree tree = new PackageTree();
        
        PackageArtifact pa = new PackageArtifact();
        pa.setId("bunny:warrior");
        
        PackageNode root = new PackageNode(pa);
        
        tree.setRoot(root);
        
        Map<String, PackageNode> map = tree.getNodesMap();
        
        assertNotNull(map);
        assertTrue(map.isEmpty());
                
        map = new HashMap<String, PackageNode>();
        map.put(root.getValue().getId(), root);
        tree.setNodesMap(map);
        
        assertEquals(map, tree.getNodesMap());
    }

}
