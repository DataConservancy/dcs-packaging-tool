package org.dataconservancy.packaging.tool.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Unit testing for ArtifactReference
 */
public class ArtifactReferenceTest {
    String string1;
    String string2;
    String slashRoot;
    String noSlashRoot;

    File slashRootContentFile;
    File noSlashRootContentFile;

    @Before
    public void setup() {
        string1 = "foo" + File.separator + "bar" + File.separator + "baz";
        string2 = "foo" + File.separator + "bar" + File.separator + "baz";

        slashRoot = File.separator + "root" + File.separator + "content" + File.separator;
        noSlashRoot = File.separator + "root" + File.separator + "content";

        slashRootContentFile = new File(slashRoot);
        noSlashRootContentFile = new File(noSlashRoot);
    }

    @Test
    public void testEquality(){

        ArtifactReference ref1 = new ArtifactReference(string1);
        ArtifactReference ref2 = new ArtifactReference(string2);

        Assert.assertNotNull(ref1.getRefString());
        Assert.assertTrue(ref1.equals(ref1));
        Assert.assertTrue(ref1.equals(ref2));
        Assert.assertTrue(ref2.equals(ref1));
    }

    @Test
    public void testAbsRefString(){
        ArtifactReference ref = new ArtifactReference(string1);
        Assert.assertTrue(ref.getResolvedAbsoluteRefString(slashRootContentFile).equalsIgnoreCase(
            slashRoot + string1));
        Assert.assertTrue(ref.getResolvedAbsoluteRefString(slashRootContentFile).equalsIgnoreCase(ref.getResolvedAbsoluteRefString(noSlashRootContentFile)));
    }

    @Test
    public void testAbsRefPath(){
        ArtifactReference ref = new ArtifactReference(string1);
        Assert.assertTrue(ref.getResolvedAbsoluteRefPath(slashRootContentFile).toString().equalsIgnoreCase(
            slashRoot + string1));
        Assert.assertTrue(ref.getResolvedAbsoluteRefPath(slashRootContentFile).toString().equalsIgnoreCase(ref.getResolvedAbsoluteRefPath(noSlashRootContentFile).toString()));
    }

}
