package org.dataconservancy.packaging.tool.model;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Unit testing for ArtifactReference
 */
public class ArtifactReferenceTest {
        final String string1 = "foo/bar/baz";
        final String string2 = "foo/bar/baz";
        final String slashRoot = "/root/content/";
        final String noSlashRoot = "/root/content";

        File slashRootContentFile = new File(slashRoot);
        File noSlashRootContentFile = new File(noSlashRoot);


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
        Assert.assertTrue(ref.getResolvedAbsoluteRefString(slashRootContentFile).equals(slashRoot + string1));
        Assert.assertTrue(ref.getResolvedAbsoluteRefString(slashRootContentFile).equals(ref.getResolvedAbsoluteRefString(noSlashRootContentFile)));
    }

    @Test
    public void testAbsRefPath(){
        ArtifactReference ref = new ArtifactReference(string1);
        Assert.assertTrue(ref.getResolvedAbsoluteRefPath(slashRootContentFile).toString().equals(slashRoot + string1));
        Assert.assertTrue(ref.getResolvedAbsoluteRefPath(slashRootContentFile).toString().equals(ref.getResolvedAbsoluteRefPath(noSlashRootContentFile).toString()));
    }

}
