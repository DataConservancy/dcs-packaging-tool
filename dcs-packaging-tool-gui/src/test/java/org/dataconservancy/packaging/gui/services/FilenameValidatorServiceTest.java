package org.dataconservancy.packaging.gui.services;

import org.dataconservancy.packaging.gui.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by jrm on 10/22/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/config/applicationContext-test.xml"})
public class FilenameValidatorServiceTest {

    @Autowired
    private Configuration configuration;

    private FilenameValidatorService underTest;

    @Before
    public void setup() {
        underTest = new FilenameValidatorService(configuration);
    }


    @Test
    public void testIsInvalidPathComponent() throws IOException, InterruptedException {
        Assert.assertTrue(underTest.isInvalidPathComponent("has:Colon"));
        Assert.assertTrue(underTest.isInvalidPathComponent("file?3"));
        Assert.assertTrue(underTest.isInvalidPathComponent("has?questionMark"));
        Assert.assertTrue(underTest.isInvalidPathComponent("CON"));
        Assert.assertTrue(underTest.isInvalidPathComponent("LPT8"));

        Assert.assertFalse(underTest.isInvalidPathComponent("LPT0"));
        Assert.assertFalse(underTest.isInvalidPathComponent("LPT11"));
    }

    @Test(expected = IOException.class)
    public void testNoSuchPath() throws IOException, InterruptedException {
        List<String> badnames = underTest.findInvalidFilenames(Paths.get("noSuchFilePath"));
    }
}
