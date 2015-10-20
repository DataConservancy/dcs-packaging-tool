package org.dataconservancy.packaging.gui.services;


import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.junit.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import java.util.List;

/**
 * Created by jrm on 10/20/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/config/applicationContext-test.xml"})
public class PackageMetadataServiceTest {

    @Autowired
    private Configuration configuration;

    private PackageMetadataService underTest;

    @Before
    public void setup() throws IOException{
        String fileName = configuration.getPackageMetadataParametersFile();
        underTest = new PackageMetadataService(configuration.resolveConfigurationFile(fileName));
    }

    @Test
    public void testGetRequiredPackageMetadata(){
        List<PackageMetadata> requiredMetadata = underTest.getRequiredPackageMetadata();
        Assert.assertNotNull(requiredMetadata);
        Assert.assertEquals(1, requiredMetadata.size());
        PackageMetadata pm = requiredMetadata.get(0);
        Assert.assertEquals("Package-Name", pm.getName());
        Assert.assertEquals(1,pm.getMinOccurrence());
        Assert.assertEquals(1,pm.getMaxOccurrence());
        Assert.assertFalse(pm.isEditable());
    }

    @Test
    public void testGetRecommendedPackageMetadata(){
        List<PackageMetadata> recommendedMetadata = underTest.getRecommendedPackageMetadata();
        Assert.assertNotNull(recommendedMetadata);
        Assert.assertEquals(5,recommendedMetadata.size());
        PackageMetadata pm = recommendedMetadata.get(0);
        Assert.assertEquals("Bag-Count", pm.getName());
        Assert.assertEquals(0,pm.getMinOccurrence());
        Assert.assertEquals(1,pm.getMaxOccurrence());
        Assert.assertFalse(pm.isEditable());
    }

    @Test
    public void testGetOptionalPackageMetadata(){
        List<PackageMetadata> optionalMetadata = underTest.getOptionalPackageMetadata();
        Assert.assertNotNull(optionalMetadata);
        Assert.assertEquals(9, optionalMetadata.size());
        PackageMetadata pm = optionalMetadata.get(0);
        Assert.assertEquals("Keyword", pm.getName());
        Assert.assertEquals(0,pm.getMinOccurrence());
        Assert.assertEquals(Integer.MAX_VALUE,pm.getMaxOccurrence());
        Assert.assertTrue(pm.isEditable());
    }
}
