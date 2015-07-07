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

import java.util.Iterator;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PackageNodeTest {
    PackageNode root;
    PackageNode level1Tree1;
    PackageNode level1Tree2;
    PackageNode level1Tree3;
    PackageNode level2Tree11;
    PackageNode level2Tree12;
    PackageNode level2Tree21;
    PackageNode level2Tree31;
    PackageNode level2Tree32;
    PackageNode level2Tree33;
    PackageNode level3Tree331;

    PackageArtifact rootArtifact = new PackageArtifact();
    PackageArtifact level1Tree1Artifact = new PackageArtifact();
    PackageArtifact level1Tree2Artifact = new PackageArtifact();
    PackageArtifact level1Tree3Artifact = new PackageArtifact();
    PackageArtifact level2Tree11Artifact = new PackageArtifact();
    PackageArtifact level2Tree12Artifact = new PackageArtifact();
    PackageArtifact level2Tree21Artifact = new PackageArtifact();
    PackageArtifact level2Tree31Artifact = new PackageArtifact();
    PackageArtifact level2Tree32Artifact = new PackageArtifact();
    PackageArtifact level2Tree33Artifact = new PackageArtifact();
    PackageArtifact level3Tree331Artifact = new PackageArtifact();

    @Before
    public void setUp() {
        rootArtifact.setId("id:rootArtifact");
        level1Tree1Artifact.setId("id:1-1");
        level1Tree2Artifact.setId("id:1-2");
        level1Tree3Artifact.setId("id:1-3");
        level2Tree11Artifact.setId("id:2-11");
        level2Tree12Artifact.setId("id:2-12");
        level2Tree21Artifact.setId("id:2-21");
        level2Tree31Artifact.setId("id:2-31");
        level2Tree32Artifact.setId("id:2-32");
        level2Tree33Artifact.setId("id:2-33");
        level3Tree331Artifact.setId("id:3-331");

        root = new PackageNode(rootArtifact);
        level1Tree1 = new PackageNode(level1Tree1Artifact);
        level1Tree2 = new PackageNode(level1Tree2Artifact);
        level1Tree3 = new PackageNode(level1Tree3Artifact);
        level2Tree11 = new PackageNode(level2Tree11Artifact);
        level2Tree12 = new PackageNode(level2Tree12Artifact);
        level2Tree21 = new PackageNode(level2Tree21Artifact);
        level2Tree31 = new PackageNode(level2Tree31Artifact);
        level2Tree32 = new PackageNode(level2Tree32Artifact);
        level2Tree33 = new PackageNode(level2Tree33Artifact);
        level3Tree331 = new PackageNode(level3Tree331Artifact);

    }

    @Test
    public void testEqualToSelf() {
        assertEquals(level3Tree331, level3Tree331);
    }


    @Test
    public void testSetNGetParent() {
       level2Tree11.setParentNode(level1Tree1);
        assertEquals(level1Tree1, level2Tree11.getParentNode());
    }

    @Test
    public void testSetNGetChildren () {
        level1Tree1.getChildrenNodes().add(level2Tree11);
        level1Tree1.getChildrenNodes().add(level2Tree12);

        assertEquals(2, level1Tree1.getChildrenNodes().size());
        assertTrue(level1Tree1.getChildrenNodes().contains(level2Tree11));
        assertTrue(level1Tree1.getChildrenNodes().contains(level2Tree12));
    }

    // Construct a tree with a random structure
    private PackageTree constructRandomTree(long seed) {
        Random r = new Random(seed);

        PackageTree tree = new PackageTree();
        PackageArtifact art = new PackageArtifact();

        art.setId("bull:" + r.nextInt());
        tree.setRoot(new PackageNode(art));

        for (int i = 0; i < 3; i++) {
            constructRandomTree(r, tree.getRoot(), 0);
        }

        return tree;
    }
    
    private void constructRandomTree(Random r, PackageNode parent, int depth) {
        for (int i = 0; i < r.nextInt(4) + 1; i++) {
            PackageArtifact art = new PackageArtifact();
            art.setId("cow:" + r.nextInt());
            PackageNode node = new PackageNode(art, parent);
            
            if (r.nextBoolean() && depth < 4) {
                constructRandomTree(r, node, depth + 1);
            }
        }
    }
    
    @Test
    public void testEquality() {
        long seed = System.currentTimeMillis();
        PackageTree tree1 = constructRandomTree(seed);
        PackageTree tree2 = constructRandomTree(seed);

        assertEquals(tree1.getRoot(), tree2.getRoot());
        assertEquals(tree1.getRoot().hashCode(), tree2.getRoot().hashCode());
    }
    
    @Test
    public void testSetValue() {
        PackageArtifact pa = new PackageArtifact();
        pa.setId("moo:cow");
        
        root.setValue(pa);
        
        assertEquals(pa, root.getValue());
    }
    
    
    /**
     * Test removing a child from a node after mutation of the nodes children.
     */
    @Test
    public void testRemoveChild() {
        PackageArtifact art1 = new PackageArtifact();
        PackageArtifact art2 = new PackageArtifact();
        PackageArtifact art3 = new PackageArtifact();
                
        
        PackageNode parent = new PackageNode(new PackageArtifact());
        PackageNode kid1 = new PackageNode(art1);
        PackageNode kid2 = new PackageNode(art2);
        PackageNode grandkid1 = new PackageNode(art3);
        
        parent.getChildrenNodes().add(kid1);
        parent.getChildrenNodes().add(kid2);
        kid2.getChildrenNodes().add(grandkid1);

        art1.setId("id:mdf3");
        art1.setType("MetadataFile");
        art1.setArtifactRef("file:/some/file/path");
        art1.setByteStream(true);
        art1.setRelationships(new PackageRelationship("isMetadataFor", true, "id:col5"));
        
        art2.setId("id:mdf2#DataItem");
        art2.setType("DataItem");
        art2.setByteStream(false);
        art2.addSimplePropertyValue("name", "File to be transformed.");
        art2.addSimplePropertyValue("createDate", "08/07/2014");
        art2.addSimplePropertyValue("modDate", "2014-04-12");
        art2.setRelationships(new PackageRelationship("Relationship2", false, "target22", "target21"),
                new PackageRelationship("Relationship1", false, "target12", "target11"));
        
        art3.setId("id:mdf2#DataFile");
        art3.setType("MetadataFile");
        art3.setArtifactRef("file:/some/file/path");
        art3.setByteStream(true);
        art3.addSimplePropertyValue("name", "File to be transformed to cow.");
        art3.addSimplePropertyValue("format", "application/octet-stream");
        art2.addSimplePropertyValue("createDate", "2014-04-12");
        art2.addSimplePropertyValue("modDate", "2014-04-12");
        art3.setRelationships(new PackageRelationship("isMetadataFor", true, "id:mdf2#DataItem"));
        art3.addSimplePropertyValue("modDate", "2014-04-12");
        
        assertTrue(parent.getChildrenNodes().contains(kid1));
        assertTrue(parent.getChildrenNodes().contains(kid2));
        
        for (Iterator<PackageNode> iter = parent.getChildrenNodes().iterator(); iter.hasNext(); ) {
            if (iter.next().equals(kid2)) {
                iter.remove();
            }
        }
        

        assertEquals(1, parent.getChildrenNodes().size());
        assertEquals(kid1, parent.getChildrenNodes().iterator().next());
    }
}
