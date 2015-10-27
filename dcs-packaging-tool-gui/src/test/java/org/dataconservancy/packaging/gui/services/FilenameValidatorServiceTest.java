package org.dataconservancy.packaging.gui.services;

import org.dataconservancy.packaging.gui.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
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
    private String contentRootPath;

    @Before
    public void setup(){
        underTest = new FilenameValidatorService(configuration);
        contentRootPath = this.getClass().getResource("/FileNameTest/").getPath();
    }


    @Test
    public void testInvalidFileNames() throws IOException, InterruptedException {
        List<String> badnames = underTest.findInvalidFilenames(contentRootPath);
        Assert.assertEquals(3,badnames.size());
        Assert.assertTrue(badnames.contains("has:Colon"));
        Assert.assertTrue(badnames.contains("file?3"));
        Assert.assertTrue(badnames.contains("has?questionMark"));
    }

    @Test(expected = IOException.class)
    public void testNoSuchPath() throws IOException, InterruptedException {
        List<String> badnames = underTest.findInvalidFilenames("noSuchFilePath");
    }
}
