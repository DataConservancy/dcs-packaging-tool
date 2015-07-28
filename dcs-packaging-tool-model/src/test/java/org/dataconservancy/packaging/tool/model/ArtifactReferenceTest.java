package org.dataconservancy.packaging.tool.model;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Unit testing for ArtifactReference
 */
public class ArtifactReferenceTest {
        String uriString1 = "foo/bar/baz";
        String uriString2 = "foo/bar/baz";
    @Test
    public void testEquality(){
        String uriString1 = "file:/foo/bar/baz";
        String uriString2 = "file:/foo/bar/baz";

        ArtifactReference ref1 = new ArtifactReference(uriString1);
        ArtifactReference ref2 = new ArtifactReference(uriString2);

        Assert.assertNotNull(ref1.getRefString());
        Assert.assertTrue(ref1.equals(ref1));
        Assert.assertTrue(ref1.equals(ref2));
        Assert.assertTrue(ref2.equals(ref1));
    }

}
