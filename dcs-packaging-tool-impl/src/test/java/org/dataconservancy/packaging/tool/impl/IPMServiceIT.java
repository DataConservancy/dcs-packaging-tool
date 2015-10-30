package org.dataconservancy.packaging.tool.impl;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.dcs.util.ChecksumGeneratorVerifier;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class IPMServiceIT {

    @ClassRule
    public static TemporaryFolder tmpfolder = new TemporaryFolder();

    public IPMService underTest;

    @Before
    public void setup() {
        underTest = new IPMServiceImpl();
    }
    /**
     * Tests creating a tree from the file system, modifies the file system, creates a new tree,
     * compares it to the original and finally merges the result.
     */
    @Test
    public void testLoadAndMergeTrees() throws IOException {
        File mainDir = tmpfolder.newFolder("farm");
        File mainDirFile = new File(mainDir, "pig.txt");
        Files.createFile(mainDirFile.toPath());

        File subDir = new File(mainDir, "moo");
        subDir.mkdir();

        File secondSubDir = new File(mainDir, "oink");
        secondSubDir.mkdir();

        File subDirFile = new File(subDir, "cow.jpg");
        Files.createFile(subDirFile.toPath());

        File subDirFileTwo = new File(subDir, "chicken.txt");
        Files.createFile(subDirFileTwo.toPath());

        File subDirB = new File(subDir, ".steak");
        subDirB.mkdirs();

        File subDirBFile = new File(subDirB, "eggs.png");
        subDirBFile.createNewFile();

        Node root = underTest.createTreeFromFileSystem(mainDir.toPath());
        assertNotNull(root);

        //This is sadly required to make the delete work on Windows
        System.gc();
        FileUtils.forceDelete(subDirBFile);

        File horseFile = new File(secondSubDir, "horse.pdf");
        Files.createFile(horseFile.toPath());

        BufferedWriter writer = new BufferedWriter(new FileWriter(subDirFile));
        writer.write("MMMoooo");
        writer.flush();
        writer.close();

        InputStream md5Fis = Files.newInputStream(subDirFile.toPath());
        String newMd5Checksum = ChecksumGeneratorVerifier.generateMD5checksum(md5Fis);
        md5Fis.close();

        InputStream sha1Fis = Files.newInputStream(subDirFile.toPath());
        String newSha1Checksum = ChecksumGeneratorVerifier.generateSHA1checksum(sha1Fis);
        sha1Fis.close();

        Node newTree = underTest.createTreeFromFileSystem(mainDir.toPath());
        assertNotNull(newTree);

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(root, newTree);
        assertNotNull(nodeMap);
        assertEquals(3, nodeMap.size());

        assertTrue(underTest.mergeTree(root, nodeMap));

        Node horseNode = findNodeForFile(root, horseFile.toURI());
        assertNotNull(horseNode);
        assertEquals(horseNode.getParent().getFileInfo().getLocation(), secondSubDir.toURI());

        Node subDirBFileNode = findNodeForFile(root, subDirBFile.toURI());
        assertNull(subDirBFileNode);

        Node subDirFileNode = findNodeForFile(root, subDirFile.toURI());
        assertNotNull(subDirFileNode);
        assertTrue(subDirFileNode.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1).equalsIgnoreCase(newSha1Checksum));
        assertTrue(subDirFileNode.getFileInfo().getChecksum(FileInfo.Algorithm.MD5).equalsIgnoreCase(newMd5Checksum));
    }

    /**
     * Tests that if a completely different location is compared and merged the tree is essentially replaced by the new tree.
     * @throws IOException
     */
    @Test
    public void testMergeCompareDifferentLocations() throws IOException {
        File mainDir = tmpfolder.newFolder("differentLocationTest");
        File mainDirFile = new File(mainDir, "pig.txt");
        Files.createFile(mainDirFile.toPath());

        File subDir = new File(mainDir, "moo");
        subDir.mkdir();

        File secondSubDir = new File(mainDir, "oink");
        secondSubDir.mkdir();

        File subDirFile = new File(subDir, "cow.jpg");
        Files.createFile(subDirFile.toPath());

        File subDirFileTwo = new File(subDir, "chicken.txt");
        Files.createFile(subDirFileTwo.toPath());

        File subDirB = new File(subDir, ".steak");
        subDirB.mkdirs();

        File subDirBFile = new File(subDirB, "eggs.png");
        subDirBFile.createNewFile();

        Node root = underTest.createTreeFromFileSystem(mainDir.toPath());
        assertNotNull(root);

        File newDir = tmpfolder.newFolder("newDir");
        File newSubDir = new File(newDir, "newSubDir");
        newSubDir.mkdir();

        Node newTree = underTest.createTreeFromFileSystem(newDir.toPath());
        assertNotNull(newTree);

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(root, newTree);
        assertNotNull(nodeMap);
        assertEquals(10, nodeMap.size());

        assertTrue(underTest.mergeTree(root, nodeMap));

        assertEquals(newDir.toURI(), root.getFileInfo().getLocation());
        assertEquals(1, root.getChildren().size());
        assertEquals(newSubDir.toURI(), root.getChildren().get(0).getFileInfo().getLocation());
    }

    /**
     * Tests that if a node is deleted it's child node is deleted even if it's a different location
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testMergeAndCompareRemoveDifferentLocationParent()
        throws IOException, URISyntaxException {
        File mainDir = tmpfolder.newFolder("differentLocationParentTest");
        File mainDirFile = new File(mainDir, "pig.txt");
        Files.createFile(mainDirFile.toPath());

        File subDir = new File(mainDir, "moo");
        subDir.mkdir();

        File secondSubDir = new File(mainDir, "oink");
        secondSubDir.mkdir();

        File subDirFile = new File(subDir, "cow.jpg");
        Files.createFile(subDirFile.toPath());

        File subDirFileTwo = new File(subDir, "chicken.txt");
        Files.createFile(subDirFileTwo.toPath());

        File subDirB = new File(subDir, ".steak");
        subDirB.mkdirs();

        Node root = underTest.createTreeFromFileSystem(mainDir.toPath());
        assertNotNull(root);
        
        Node subDirFileTwoNode = findNodeForFile(root, subDirFileTwo.toURI());
        assertNotNull(subDirFileTwoNode);
        
        URI identifierTwo = URI.create("compare:two");
        Node nodeTwo = new Node(identifierTwo);
        nodeTwo.setFileInfo(new FileInfo(new URI("file://foo"), "two"));
        subDirFileTwoNode.addChild(nodeTwo);
        
        //This is sadly required to make the delete work on Windows
        System.gc();
        FileUtils.forceDelete(subDirFileTwo);

        Node newTree = underTest.createTreeFromFileSystem(mainDir.toPath());
        assertNotNull(newTree);

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(root, newTree);
        assertNotNull(nodeMap);
        assertEquals(2, nodeMap.size());

        assertTrue(underTest.mergeTree(root, nodeMap));

        assertNull(findNodeForFile(root, subDirFileTwoNode.getFileInfo().getLocation()));
        assertNull(findNodeForFile(root, nodeTwo.getFileInfo().getLocation()));
    }

    private Node findNodeForFile(Node root, URI location) {

        if (root.getFileInfo().getLocation().equals(location)) {
            return root;
        } else if (root.getChildren() != null) {
            for (Node node : root.getChildren()) {
                Node result = findNodeForFile(node, location);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
