package org.dataconservancy.packaging.gui.services;


import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.junit.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
        underTest = new PackageMetadataService(configuration);
    }

    @Test
    public void testGetRequiredPackageMetadata(){
        List<PackageMetadata> requiredMetadata = underTest.getRequiredPackageMetadata();
        Assert.assertNotNull(requiredMetadata);
        Assert.assertEquals(1, requiredMetadata.size());
        PackageMetadata pm = requiredMetadata.get(0);
        Assert.assertEquals("Package-Metadata-Required", pm.getName());
        Assert.assertEquals(PackageMetadata.Requiredness.REQUIRED, pm.getRequiredness());
        Assert.assertFalse(pm.isRepeatable());
        Assert.assertFalse(pm.isEditable());
        Assert.assertEquals("This is the help text for Package-Metadata-1",pm.getHelpText());
        Assert.assertEquals(PackageMetadata.ValidationType.NONE, pm.getValidationType());
    }

    @Test
    public void testGetRecommendedPackageMetadata(){
        List<PackageMetadata> recommendedMetadata = underTest.getRecommendedPackageMetadata();
        Assert.assertNotNull(recommendedMetadata);
        Assert.assertEquals(3,recommendedMetadata.size());
        PackageMetadata pm = recommendedMetadata.get(0);
        Assert.assertEquals("Package-Metadata-Recommended", pm.getName());
        Assert.assertEquals(PackageMetadata.Requiredness.RECOMMENDED, pm.getRequiredness());
        Assert.assertFalse(pm.isRepeatable());
        Assert.assertFalse(pm.isEditable());
        Assert.assertEquals("This is the help text for Package-Metadata-2",pm.getHelpText());
        Assert.assertEquals(PackageMetadata.ValidationType.NONE, pm.getValidationType());

        pm = recommendedMetadata.get(1);
        Assert.assertEquals("Email-Metadata", pm.getName());
        Assert.assertEquals(PackageMetadata.ValidationType.EMAIL, pm.getValidationType());

        pm = recommendedMetadata.get(2);
        Assert.assertEquals("Phone-Metadata", pm.getName());
        Assert.assertEquals(PackageMetadata.ValidationType.PHONE, pm.getValidationType());
    }

    @Test
    public void testGetOptionalPackageMetadata(){
        List<PackageMetadata> optionalMetadata = underTest.getOptionalPackageMetadata();
        Assert.assertNotNull(optionalMetadata);
        Assert.assertEquals(3, optionalMetadata.size());
        PackageMetadata pm = optionalMetadata.get(0);
        Assert.assertEquals("Package-Metadata-Optional", pm.getName());
        Assert.assertEquals(PackageMetadata.Requiredness.OPTIONAL, pm.getRequiredness());
        Assert.assertFalse(pm.isRepeatable());
        Assert.assertFalse(pm.isEditable());
        Assert.assertEquals("This is the help text for Package-Metadata-2",pm.getHelpText());
        Assert.assertEquals(PackageMetadata.ValidationType.NONE, pm.getValidationType());

        pm = optionalMetadata.get(1);
        Assert.assertEquals("Repeatable-Metadata", pm.getName());
        //This last assert checks that misspelled or invalid specification for the requiredness
        //defaults to OPTIONAL - was set to MISSPELLED in the xml file
        Assert.assertEquals(PackageMetadata.Requiredness.OPTIONAL, pm.getRequiredness());
        Assert.assertTrue(pm.isRepeatable());
        Assert.assertFalse(pm.isEditable());
        Assert.assertNull(pm.getHelpText());
        //This last assert checks that misspelled or invalid specification for the validation type
        //defaults to NONE - was set to MISSPELLED in the xml file
        Assert.assertEquals(PackageMetadata.ValidationType.NONE, pm.getValidationType());

        pm = optionalMetadata.get(2);
        Assert.assertEquals("Editable-Metadata", pm.getName());
        Assert.assertEquals(PackageMetadata.Requiredness.OPTIONAL, pm.getRequiredness());
        Assert.assertFalse(pm.isRepeatable());
        Assert.assertTrue(pm.isEditable());
        Assert.assertNull(pm.getHelpText());
        Assert.assertEquals(PackageMetadata.ValidationType.NONE, pm.getValidationType());
    }
}
