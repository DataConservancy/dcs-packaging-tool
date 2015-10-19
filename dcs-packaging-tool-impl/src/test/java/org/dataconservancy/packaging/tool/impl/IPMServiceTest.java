package org.dataconservancy.packaging.tool.impl;

import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
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
}
