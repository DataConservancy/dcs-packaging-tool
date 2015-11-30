package org.dataconservancy.packaging.tool.impl.support;

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
}
