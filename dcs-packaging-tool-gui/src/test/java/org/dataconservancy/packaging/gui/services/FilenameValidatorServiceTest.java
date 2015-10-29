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
import java.util.List;

/**
 * Created by jrm on 10/22/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/config/applicationContext-test.xml"})
@Ignore("Ignored until file situation is straightened out.")
public class FilenameValidatorServiceTest {

    @Autowired
    private Configuration configuration;

    private FilenameValidatorService underTest;
    private String contentRootPath;

    @Before
    public void setup(){
        underTest = new FilenameValidatorService(configuration);
        //TODO Fix this when windows file issue is resolved
        //contentRootPath = this.getClass().getResource("/FileNameTest/").getPath();
    }


    @Test
    public void testInvalidFileNames() throws IOException, InterruptedException {
        List<String> badnames = underTest.findInvalidFilenames(contentRootPath);
        Assert.assertEquals(5,badnames.size());
        Assert.assertTrue(badnames.get(0).endsWith("has:Colon"));
        Assert.assertTrue(badnames.get(1).endsWith("file?3"));
        Assert.assertTrue(badnames.get(2).endsWith("has?questionMark"));
        Assert.assertTrue(badnames.get(3).endsWith("CON"));
        Assert.assertTrue(badnames.get(4).endsWith("LPT8"));
    }

    @Test(expected = IOException.class)
    public void testNoSuchPath() throws IOException, InterruptedException {
        List<String> badnames = underTest.findInvalidFilenames("noSuchFilePath");
    }
}
