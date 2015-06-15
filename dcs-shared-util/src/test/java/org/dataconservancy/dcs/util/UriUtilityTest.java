/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.util;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;


public class UriUtilityTest {

    // Check that a URI with the http scheme is detected as an http url
    @Test
    public void uriWithHttpSchemeIsOk() throws URISyntaxException {
        URI uri = new URI("http", "somefakeorg.org", "/fake", null);
        assertTrue("Expected a Http/Https schema, was '" + uri.getScheme() + "'", UriUtility.isHttpUrl(uri));
    }

    // Check that a URI with the https scheme is detected as an http url
    @Test
    public void uriWithHttpsSchemeIsOk() throws URISyntaxException {
        URI uri = new URI("https", "somefakeorg.org", "/fake", null);
        assertTrue("Expected a Http/Https schema, was '" + uri.getScheme() + "'", UriUtility.isHttpUrl(uri));
    }

    // Check that a URI with the ftp scheme is detected as not being an http url
    @Test
    public void uriWithFtpSchemeIsNotOk() throws URISyntaxException {
        URI uri = new URI("ftp", "somefakeorg.org", "/fake", null);
        assertFalse("Expected a non-Http/Https schema, was '" + uri.getScheme() + "'", UriUtility.isHttpUrl(uri));
    }

    // Check that a URI with the file scheme is detected as not being an http url, and that a file object's to URI
    // does the same.
    @Test
    public void uriWithFileSchemeIsNotOk() throws URISyntaxException {
        URI uri = new URI("file:///somefake/path/fake.file");
        assertFalse("Expected a non-Http/Https schema, was '" + uri.getScheme() + "'", UriUtility.isHttpUrl(uri));

        File f = new File("/somefake/path/fake.file");
        assertFalse("Expected a non-Http/Https schema, was '" + f.toURI().getScheme() + "'", UriUtility.isHttpUrl(f.toURI()));
    }

    // Check that a URI with a null host is detected as not being an http url
    @Test
    public void uriWithNullHostIsNotOk() throws URISyntaxException {
        URI uri = new URI("http", null, "/fake", null);
        assertFalse("Expected failed return due to host, host was '" + uri.getHost() + "'", UriUtility.isHttpUrl(uri));
    }

    // Check that a URI with an empty string host is detected as not being an http url
    @Test
    public void uriWithEmptyHostIsNotOk() throws URISyntaxException {
        URI uri = new URI("http", "", "/fake", null);
        assertFalse("Expected failed return due to host, host was '" + uri.getHost() + "'", UriUtility.isHttpUrl(uri));
    }

    // Check that a valid http URL string is detected as such
    @Test
    public void stringHttpUrlIsOk() {
        String toCheck = "http://somefakeorg.org/fake";
        assertTrue("Expected a valid Http Url", UriUtility.isHttpUrl(toCheck));
    }

    // Check that a valid https URL string is detected as such
    @Test
    public void stringHttpsUrlIsOk() {
        String toCheck = "https://somefakeorg.org/fake";
        assertTrue("Expected a valid Https Url", UriUtility.isHttpUrl(toCheck));
    }

    // Check that a file URL string is detected as not http
    @Test
    public void stringFtpUrlIsNotOk() {
        String toCheck = "ftp://somefakeorg.org/fake";
        assertFalse("Expected a false result", UriUtility.isHttpUrl(toCheck));
    }

    // Check that a no-URL string is detected as bad
    @Test
    public void stringNotUrlIsNotOk() {
        String toCheck = "abcdefg";
        assertFalse("Expected a false result", UriUtility.isHttpUrl(toCheck));
    }

    // Check that a URI that is also a valid URL shows as resolvable
    @Test
    public void uriThatIsUrlIsOK() throws URISyntaxException {
        URI uri = new URI("http://somefakeorg.org/fake");
        assertTrue("Expected URI to be ok!", UriUtility.isResolvable(uri));
    }

    // Check that a URI that is not a valid URL shows as non-resolvable
    @Test
    public void uriThatIsNotUrlIsNotOK() throws URISyntaxException {
        URI uri = new URI("some:nonresolvable:uri");
        assertFalse("Expected URI to be bad!", UriUtility.isResolvable(uri));
    }

    // Check that when using the FileUtility to get a file's URI string that it has three slashes (IE, file:///)
    @Test
    public void fileUriIsProperWithThreeSlashes() throws URISyntaxException {
        File f = new File("fake.file");

        assertTrue(UriUtility.makeFileUriString(f, null).toString().startsWith("file:///"));
    }

    // Check that if a base directory is not passed in, it will just use the File URI absolute
    @Test
    public void fileUriProperlyCreatedWithNoBaseDirectory() throws URISyntaxException {
        File f = new File("/some/fake/dir/fake.file");

        assertEquals("file:///some/fake/dir/fake.file", UriUtility.makeFileUriString(f, null).toString());
    }

    // Check that if a base directory is passed in, it will create the File URI relative to the base directory
    @Test
    public void fileUriProperlyRelativeToBaseDirectory() throws URISyntaxException {
        File f = new File("/some/fake/dir/fake.file");
        File d = new File("/some/fake");

        assertEquals("file:///dir/fake.file", UriUtility.makeFileUriString(f, d).toString());
    }

    // Check that if a base directory is passed in that isn't part of the file's path, it just uses the files path
    @Test
    public void fileUriUsesOwnPathWhenBasedirNotInPath() throws URISyntaxException {
        File f = new File("/some/fake/dir/fake.file");
        File d = new File("/another");

        assertEquals("file:///some/fake/dir/fake.file", UriUtility.makeFileUriString(f, d).toString());
    }
}
