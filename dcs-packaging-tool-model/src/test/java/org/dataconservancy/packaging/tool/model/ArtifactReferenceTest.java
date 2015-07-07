package org.dataconservancy.packaging.tool.model;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Unit testing for ArtifactReference
 */
public class ArtifactReferenceTest {

    @Test
    public void testEquality(){
        String uriString1 = "file:/foo/bar/baz";
        String uriString2 = "file:/foo/bar/baz";

        ArtifactReference ref1 = new ArtifactReference(uriString1);
        ArtifactReference ref2 = new ArtifactReference(uriString2);

        Assert.assertNotNull(ref1.getRefString());
        Assert.assertNotNull(ref1.getRefURI());
        Assert.assertNotNull((ref1.getRefURL()));
        Assert.assertTrue(ref1.equals(ref1));
        Assert.assertTrue(ref1.equals(ref2));
        Assert.assertTrue(ref2.equals(ref1));
    }

    @Test
    public void testConstructorsEqual() throws URISyntaxException {
        String uriString1 = "file:/foo/bar/baz";
        URI uri = new URI(uriString1);
        ArtifactReference ref1 = new ArtifactReference(uriString1);
        ArtifactReference ref2 = new ArtifactReference(uri);

        Assert.assertTrue(ref1.equals(ref2));

        //we don't have a fragemnt, so check this is null so that we can trust the next test
        Assert.assertNull(ref1.getRefURI().getFragment());
    }

    @Test
    public void testFragmentsHandledInConstructor() throws URISyntaxException {
        String uriString = "file:/foo/bar#baz";
        URI uri = new URI(uriString);
        ArtifactReference ref1 = new ArtifactReference(uriString);
        ArtifactReference ref2 = new ArtifactReference(uri);

        Assert.assertTrue(ref1.equals(ref2));
        Assert.assertNotNull(ref1.getRefString());
        Assert.assertNotNull(ref1.getRefURI());
        Assert.assertNotNull(ref1.getRefURL());
        Assert.assertTrue(ref1.getRefURI().getFragment().equals("baz"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadUriStringBreaksConstruction(){
        String uriString = "bad/string"; //not a URI
        ArtifactReference ref = new ArtifactReference(uriString);
    }

}
