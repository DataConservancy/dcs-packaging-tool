package org.dataconservancy.packaging.tool.impl;

import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IPMServiceTest {

    @ClassRule
    public static TemporaryFolder tmpfolder = new TemporaryFolder();

    private IPMService underTest;

    @Before
    public void setup() {
        underTest = new IPMServiceImpl();
    }

    /**
     * Tests that a tree can successfully be generated from the file system. Tests that folders that begin with "." are ignored,
     * along with their children.
     * @throws IOException
     */
    @Test
    public void testGenerateTree() throws IOException {
        File mainDir = tmpfolder.newFolder("farm");
        File mainDirFile = new File(mainDir, "pig.txt");
        Files.createFile(mainDirFile.toPath());

        File subDir = new File(mainDir, "moo");
        subDir.mkdir();

        File subDirFile = new File(subDir, "cow.jpg");
        Files.createFile(subDirFile.toPath());

        File subDirFileTwo = new File(subDir, "chicken.txt");
        Files.createFile(subDirFileTwo.toPath());

        File subDirB = new File(subDir, ".steak");
        subDirB.mkdirs();

        File subDirBFile = new File(subDirB, "eggs.png");
        Files.createFile(subDirBFile.toPath());

        Node root = underTest.createTreeFromFileSystem(mainDir.toPath());
        assertNotNull(root);
        assertNotNull(root.getFileInfo());
        assertTrue(root.getFileInfo().isDirectory());

        assertEquals(2, root.getChildren().size());
        boolean mainFileFound = false;
        boolean subDirFound = false;

        for (Node child : root.getChildren()) {
            switch (child.getFileInfo().getName()) {
                case "pig.txt":
                    mainFileFound = true;
                    assertNull(child.getChildren());
                    break;
                case "moo":
                    assertEquals(3, child.getChildren().size());
                    //Check that the "." directory and it's children were ignored.
                    child.getChildren().stream().filter(subChild -> subChild.getFileInfo().getName().equalsIgnoreCase(".steak")).forEach(subChild -> {
                        assertTrue(subChild.isIgnored());
                        assertEquals(1, subChild.getChildren().size());
                        assertTrue(subChild.getChildren().get(0).isIgnored());
                    });
                    subDirFound = true;
                    break;
            }
        }

        assertTrue(mainFileFound);
        assertTrue(subDirFound);
    }

    /**
     * Tests that symbolic links that create cycles throw an exception during tree creation.
     * @throws Exception
     */
    @Test(expected=IOException.class)
    public void simLinkCycleTest() throws Exception {
        File tempDir = tmpfolder.newFolder("moo");

        File subdir = new File(tempDir, "cow");
        subdir.mkdir();

        Path link = Paths.get(subdir.getPath(), "link");
        link.toFile().deleteOnExit();

        try {
            Files.createSymbolicLink(link, subdir.toPath());
        } catch (UnsupportedOperationException e) {
            /* Nothing we can do if the system doesn't support symlinks */
            return;
        }

        underTest.createTreeFromFileSystem(tempDir.toPath());
    }

    /**
     * Tests that if two trees have nothing in common the existing one has all it's items marked as delete, and the new tree has all it's items marked as added.
     * @throws URISyntaxException
     */
    @Test
    public void testCompareSeparateTrees() throws URISyntaxException {
        FarmIpmTree farmTree = new FarmIpmTree();

        URI identifierOne = URI.create("compare:one");
        Node nodeOne = new Node(identifierOne);
        nodeOne.setFileInfo(new FileInfo(new URI("location:foo"), "one"));

        URI identifierTwo = URI.create("compare:two");
        Node nodeTwo = new Node(identifierTwo);
        nodeTwo.setFileInfo(new FileInfo(new URI("location:baz"), "two"));
        nodeOne.addChild(nodeTwo);

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(farmTree.getRoot(), nodeOne);
        assertNotNull(nodeMap);

        assertEquals(6, nodeMap.size());

        for (Node node : nodeMap.keySet()) {
            if (node.getIdentifier() == identifierOne || node.getIdentifier() == identifierTwo) {
                assertEquals(NodeComparison.Status.ADDED, nodeMap.get(node).getStatus());
            } else {
                assertEquals(NodeComparison.Status.DELETED, nodeMap.get(node).getStatus());
            }
        }
    }

    /**
     * Tests that if the exact same tree is compared all nodes are returned marked unchanged
     * @throws URISyntaxException
     */
    @Test
    public void testCompareSameTree() throws URISyntaxException {
        FarmIpmTree treeOne = new FarmIpmTree();
        FarmIpmTree treeTwo = new FarmIpmTree();
        updateNodeId(treeTwo.getRoot());

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(treeOne.getRoot(), treeTwo.getRoot());
        assertEquals(0, nodeMap.size());
    }

    /**
     * Tests that if a directory is changed the new directory and children are marked as added and the old ones are marked removed.
     *
     * This test is also to document current behavior note that even if two files remain the same, but their parent has changed they are considered moved.
     *
     * For example
     * Node -> C:\foo
     *  Node -> C:\cow\moo.txt
     *
     * Node -> C:\bar
     *  Node -> C:\cow\moo.txt
     *
     * Even though in this example moo.txt is at the same location since it's parent node has changed we label it as a move (i.e. delete existing/add new).
     * @throws URISyntaxException
     */
    @Test
    public void testCompareWithDirectoryChange() throws URISyntaxException {
        FarmIpmTree treeOne = new FarmIpmTree();
        FarmIpmTree treeTwo = new FarmIpmTree();

        updateNodeId(treeTwo.getRoot());

        URI oldDirectoryNodeId = treeOne.getRoot().getChildren().get(0).getChildren().get(0).getIdentifier();
        URI oldChildId = treeOne.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0).getIdentifier();

        //Get a directory node in the second tree
        Node directoryNode = treeTwo.getRoot().getChildren().get(0).getChildren().get(0);
        URI directoryNodeId = directoryNode.getIdentifier();

        URI childId = directoryNode.getChildren().get(0).getIdentifier();
        FileInfo newInfo = new FileInfo(URI.create("/foo/bar"), "bar");
        directoryNode.setFileInfo(newInfo);

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(treeOne.getRoot(), treeTwo.getRoot());

        assertEquals(4, nodeMap.size());

        for (Node node : nodeMap.keySet()) {
            if (node.getIdentifier().equals(directoryNodeId) || node.getIdentifier().equals(childId)) {
                assertEquals(NodeComparison.Status.ADDED, nodeMap.get(node).getStatus());
            } else if (node.getIdentifier().equals(oldDirectoryNodeId) || node.getIdentifier().equals(oldChildId)){
                assertEquals(NodeComparison.Status.DELETED, nodeMap.get(node).getStatus());
            }
        }
    }

    /**
     * Tests that if files are added and removed they are correctly designated in tree comparison
     * @throws URISyntaxException
     */
    @Test
    public void testCompareAddRemoveFile() throws URISyntaxException {
        FarmIpmTree treeOne = new FarmIpmTree();
        FarmIpmTree treeTwo = new FarmIpmTree();

        updateNodeId(treeTwo.getRoot());

        URI oldChildId = treeOne.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0).getIdentifier();

        //Get a directory node in the second tree
        Node directoryNode = treeTwo.getRoot().getChildren().get(0).getChildren().get(0);

        //Remove the child node from the directory
        directoryNode.removeChild(directoryNode.getChildren().get(0));

        //Add two new nodes to the directory node
        URI identifierOne = URI.create("compare:one");
        Node nodeOne = new Node(identifierOne);
        nodeOne.setFileInfo(new FileInfo(new URI("location:foo"), "one"));
        directoryNode.addChild(nodeOne);

        URI identifierTwo = URI.create("compare:two");
        Node nodeTwo = new Node(identifierTwo);
        nodeTwo.setFileInfo(new FileInfo(new URI("location:baz"), "two"));
        directoryNode.addChild(nodeTwo);

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(treeOne.getRoot(), treeTwo.getRoot());

        assertEquals(3, nodeMap.size());

        for (Node node : nodeMap.keySet()) {
            if (node.getIdentifier().equals(identifierOne) || node.getIdentifier().equals(identifierTwo)) {
                assertEquals(NodeComparison.Status.ADDED, nodeMap.get(node).getStatus());
            } else if (node.getIdentifier().equals(oldChildId)){
                assertEquals(NodeComparison.Status.DELETED, nodeMap.get(node).getStatus());
            }
        }
    }

    /**
     * Tests that if a file has different checksums it's marked as an update.
     * @throws URISyntaxException
     */
    @Test
    public void testCompareUpdateFile() throws URISyntaxException {
        FarmIpmTree treeOne = new FarmIpmTree();
        FarmIpmTree treeTwo = new FarmIpmTree();

        updateNodeId(treeTwo.getRoot());

        URI oldChildId = treeOne.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0).getIdentifier();

        //Get a directory node in the second tree
        Node childNode = treeTwo.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0);

        URI childId = childNode.getIdentifier();
        childNode.getFileInfo().addChecksum(FileInfo.Algorithm.MD5, UUID.randomUUID().toString());
        childNode.getFileInfo().addChecksum(FileInfo.Algorithm.SHA1, UUID.randomUUID().toString());

        Map<Node, NodeComparison> nodeMap = underTest.compareTree(treeOne.getRoot(), treeTwo.getRoot());

        assertEquals(1, nodeMap.size());

        for (Node node : nodeMap.keySet()) {
            if (node.getIdentifier().equals(oldChildId) || node.getIdentifier().equals(childId)) {
                assertEquals(NodeComparison.Status.UPDATED, nodeMap.get(node).getStatus());
            }
        }
    }

    @Test
    public void testMergeCompletelyDisparateTrees() throws URISyntaxException {
        FarmIpmTree farmTree = new FarmIpmTree();

        URI identifierOne = URI.create("compare:one");
        Node nodeOne = new Node(identifierOne);
        nodeOne.setFileInfo(new FileInfo(new URI("location:foo"), "one"));

        URI identifierTwo = URI.create("compare:two");
        Node nodeTwo = new Node(identifierTwo);
        nodeTwo.setFileInfo(new FileInfo(new URI("location:baz"), "two"));

        Map<Node, NodeComparison> nodeMap = new HashMap<>();
        nodeMap.put(nodeOne, new NodeComparison(NodeComparison.Status.ADDED, null));
        nodeMap.put(nodeTwo, new NodeComparison(NodeComparison.Status.ADDED, nodeOne));

        Node root = farmTree.getRoot();
        //Iterate through the farmTree and remove all the nodes
        markAllNodesRemoved(root, nodeMap);

        assertTrue(underTest.mergeTree(root, nodeMap));

        assertEquals(identifierOne, root.getIdentifier());

        assertEquals(1, root.getChildren().size());

    }

    private void markAllNodesRemoved(Node node, Map<Node, NodeComparison> nodeMap) {
        nodeMap.put(node, new NodeComparison(NodeComparison.Status.DELETED, node.getParent()));

        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                markAllNodesRemoved(child, nodeMap);
            }
        }
    }

    @Test
    public void testMergeDirectoryChange() {
        FarmIpmTree treeOne = new FarmIpmTree();
        FarmIpmTree treeTwo = new FarmIpmTree();

        updateNodeId(treeTwo.getRoot());

        Node oldDirectoryNode = treeOne.getRoot().getChildren().get(0).getChildren().get(0);
        Node oldChild = treeOne.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0);

        Map<Node, NodeComparison> nodeMap = new HashMap<>();
        nodeMap.put(oldDirectoryNode, new NodeComparison(NodeComparison.Status.DELETED, oldDirectoryNode.getParent()));
        nodeMap.put(oldChild, new NodeComparison(NodeComparison.Status.DELETED, oldDirectoryNode));

        //Get a directory node in the second tree
        Node directoryNode = treeTwo.getRoot().getChildren().get(0).getChildren().get(0);
        Node child = directoryNode.getChildren().get(0);
        nodeMap.put(directoryNode, new NodeComparison(NodeComparison.Status.ADDED, oldDirectoryNode.getParent()));
        nodeMap.put(child, new NodeComparison(NodeComparison.Status.ADDED, directoryNode));

        assertTrue(underTest.mergeTree(treeOne.getRoot(), nodeMap));

        //Check that the location in the tree has been correctly updated.
        Node returnedDirectory = treeOne.getRoot().getChildren().get(0).getChildren().get(0);
        assertEquals(directoryNode, returnedDirectory);

        Node returnedChild = returnedDirectory.getChildren().get(0);
        assertEquals(child, returnedChild);
    }

    @Test
    public void testMergeAddRemoveFiles() throws URISyntaxException {
        FarmIpmTree treeOne = new FarmIpmTree();
        FarmIpmTree treeTwo = new FarmIpmTree();

        updateNodeId(treeTwo.getRoot());

        Map<Node, NodeComparison> nodeMap = new HashMap<>();
        Node treeOneDirectory = treeOne.getRoot().getChildren().get(0).getChildren().get(0);

        Node oldChild = treeOneDirectory.getChildren().get(0);

        nodeMap.put(oldChild, new NodeComparison(NodeComparison.Status.DELETED, treeOneDirectory));

         //Add two new nodes to the directory node
        URI identifierOne = URI.create("compare:one");
        Node nodeOne = new Node(identifierOne);
        nodeOne.setFileInfo(new FileInfo(new URI("location:foo"), "one"));
        nodeMap.put(nodeOne, new NodeComparison(NodeComparison.Status.ADDED, treeOneDirectory));

        URI identifierTwo = URI.create("compare:two");
        Node nodeTwo = new Node(identifierTwo);
        nodeTwo.setFileInfo(new FileInfo(new URI("location:baz"), "two"));
        nodeMap.put(nodeTwo, new NodeComparison(NodeComparison.Status.ADDED, treeOneDirectory));

        assertTrue(underTest.mergeTree(treeOne.getRoot(), nodeMap));

        Node returnedDirectory = treeOne.getRoot().getChildren().get(0).getChildren().get(0);
        assertEquals(2, returnedDirectory.getChildren().size());

        boolean foundOldNode = false;
        boolean foundNodeOne = false;
        boolean foundNodeTwo = false;

        for (Node child : returnedDirectory.getChildren()) {
            if (child.getIdentifier().equals(oldChild.getIdentifier())) {
                foundOldNode = true;
            } else if (child.getIdentifier().equals(identifierOne)) {
                foundNodeOne = true;
            } else if (child.getIdentifier().equals(identifierTwo)) {
                foundNodeTwo = true;
            }
        }

        assertTrue(foundNodeOne);
        assertTrue(foundNodeTwo);
        assertFalse(foundOldNode);
    }

    @Test
    public void testMergeFileUpdate() {
        FarmIpmTree treeOne = new FarmIpmTree();
        FarmIpmTree treeTwo = new FarmIpmTree();

        updateNodeId(treeTwo.getRoot());

        Node oldChild = treeOne.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0);

        //Get a directory node in the second tree
        Node childNode = treeTwo.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0);

        childNode.getFileInfo().addChecksum(FileInfo.Algorithm.MD5, UUID.randomUUID().toString());
        childNode.getFileInfo().addChecksum(FileInfo.Algorithm.SHA1, UUID.randomUUID().toString());

        Map<Node, NodeComparison> nodeMap = new HashMap<>();
        nodeMap.put(childNode, new NodeComparison(NodeComparison.Status.UPDATED, oldChild));

        assertTrue(underTest.mergeTree(treeOne.getRoot(), nodeMap));

        Node returnedNode = treeOne.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0);
        assertEquals(oldChild.getIdentifier(), returnedNode.getIdentifier());

        assertTrue(childNode.getFileInfo().getChecksum(FileInfo.Algorithm.MD5).equalsIgnoreCase(returnedNode.getFileInfo().getChecksum(FileInfo.Algorithm.MD5)));
        assertTrue(childNode.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1).equalsIgnoreCase(returnedNode.getFileInfo().getChecksum(FileInfo.Algorithm.SHA1)));


    }

    //Used to update node ids of the FarmIpmTree so it's a different tree from the original
    private void updateNodeId(Node node) {
        node.setIdentifier(URI.create("test:" + UUID.randomUUID()));
        if (node.getChildren() != null) {
            node.getChildren().forEach(this::updateNodeId);
        }
    }
}
