/*
 * Copyright 2015 Johns Hopkins University
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

import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlConfigTest {

    private UrlConfig underTest;
    private UrlConfig buildUrlTest;
    private String buildUrlBase;

    // Create an empty UrlConfig and one pre-built for use with the buildUrl stuff
    @Before
    public void setup() {
        underTest = new UrlConfig();

        buildUrlTest = new UrlConfig();
        buildUrlTest.setScheme("http");
        buildUrlTest.setHostname("fakeorg.org");
        buildUrlTest.setPort(8080);
        buildUrlTest.setContextPath("/some/path");
        buildUrlBase = "http://fakeorg.org:8080/some/path";
    }

    // null host names throw errors
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void nullHostNameCausesError() {
        underTest.setHostname(null);
    }

    // empty host names throw errors
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void emptyHostNameCausesError() {
        underTest.setHostname("");
    }

    // host names should not have port info
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void colonInHostCausesError() {
        underTest.setHostname("fakeorg.org:80");
    }

    // valid host names
    @Test
    public void validHostIsOk() {
        underTest.setHostname("fakeorg.org");
        underTest.setHostname("www.fakeorg.org");
    }

    // negative ports not allowed
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void negativePortCausesError() {
        underTest.setPort(-1);
    }

    // zero ports not allowed
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void zeroPortCausesError() {
        underTest.setPort(0);
    }

    // positive ports ok
    @Test
    public void positivePortIsOk() {
        underTest.setPort(1);
    }

    // null scheme throws errors
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void nullSchemeCausesError() {
        underTest.setScheme(null);
    }

    // emtpy scheme throws errors
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void emptySchemeCausesError() {
        underTest.setScheme("");
    }

    // if http is used, should not show as secure
    @Test
    public void httpSchemeIsNotSecure() {
        underTest.setScheme("http");
        assertFalse("http scheme should not be secure.", underTest.isSecure());
    }

    // if https is used, should show as secure
    @Test
    public void httpsSchemeIsSecure() {
        underTest.setScheme("https");
        assertTrue("https scheme should be secure.", underTest.isSecure());
    }

    // if secure is set to false when not secure, leave alone
    @Test
    public void setSecureFalseDoesNotChangeSchemeIfAlreadyNotSecure() {
        underTest.setScheme("http");
        underTest.setSecure(false);
        assertEquals("http", underTest.getScheme());
    }

    // if secure is set to false when https, revert to http
    @Test
    public void setSecureFalseChangesHttpsToHttp() {
        underTest.setScheme("https");
        underTest.setSecure(false);
        assertEquals("http", underTest.getScheme());
    }

    // if secure is set to true when http, set to https
    @Test
    public void setSecureTrueChangesToHttps() {
        underTest.setScheme("http");
        underTest.setSecure(true);
        assertEquals("https", underTest.getScheme());
    }

    // null paths throw errors
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void nullContextPathCausesError() {
        underTest.setContextPath(null);
    }

    // empty paths are OK
    @Test
    public void emptyContextPathIsOk() {
        underTest.setContextPath("");
        assertEquals("", underTest.getContextPath());
    }

    // context paths are lowercased and tailing slash removed
    @Test
    public void validContextPathIsNormalized() {
        underTest.setContextPath("Some/Path/");
        assertEquals("some/path", underTest.getContextPath());
    }

    // calling getBaseUrl when no scheme fails
    @Test(expected=NullPointerException.class)
    public void getBaseUrlFailsIfNoScheme() {
        underTest.setHostname("fakeorg.org");
        underTest.setPort(8080);
        underTest.setContextPath("/some/path");

        underTest.getBaseUrl();
    }

    // calling getBaseUrl with no host fails
    @Test
    public void getBaseUrlFailsIfNoHost() {
        underTest.setScheme("http");
        underTest.setPort(8080);
        underTest.setContextPath("/some/path");

        underTest.getBaseUrl();
    }

    // calling getBaseUrl with no port set is OK
    @Test
    public void getBaseUrlOKIfNoPort() {
        underTest.setScheme("http");
        underTest.setHostname("fakeorg.org");
        underTest.setContextPath("/some/path");

        underTest.getBaseUrl();
    }

    // calling getBaseUrl with no context path set (null) fails
    @Test(expected=NullPointerException.class)
    public void getBaseUrlFailsIfNoContextPath() {
        underTest.setScheme("http");
        underTest.setHostname("fakeorg.org");
        underTest.setPort(8080);

        underTest.getBaseUrl();
    }

    // calling getBaseUrl with an empty context path is OK
    @Test
    public void getBaseUrlOkWithEmptyContextPathParams() {
        underTest.setScheme("http");
        underTest.setHostname("fakeorg.org");
        underTest.setPort(8080);
        underTest.setContextPath("");

        URL url = underTest.getBaseUrl();
        assertEquals("http://fakeorg.org:8080", url.toString());
    }

    // calling getBaseUrl with all parameters is OK
    @Test
    public void getBaseUrlOkWithAllParams() {
        underTest.setScheme("http");
        underTest.setHostname("fakeorg.org");
        underTest.setPort(8080);
        underTest.setContextPath("/some/path");

        URL url = underTest.getBaseUrl();
        assertEquals("http://fakeorg.org:8080/some/path", url.toString());
    }

    // calling buildURL when not initialized is an error
    @Test(expected=NullPointerException.class)
    public void buildUrlFailsIfNotInitialized() {
        underTest.buildUrl();
    }

    // calling buildURL with no params returns the base URL
    @Test
    public void buildUrlWithNoParamsReturnsBaseUrl() {
        URL url = buildUrlTest.buildUrl();
        assertEquals(buildUrlBase, url.toString());
    }

    // calling buildURL with only one param is OK
    @Test
    public void buildUrlWithOneParamAppendsProperly() {
        URL url = buildUrlTest.buildUrl("here");
        assertEquals(buildUrlBase + "/here", url.toString());
    }

    // calling buildUrl with multiple params is OK, slashes are added as needed
    @Test
    public void buildUrlWithMultipleParamsAppendsAndSlashesProperly() {
        URL url = buildUrlTest.buildUrl("here", "there", "everywhere");
        assertEquals(buildUrlBase + "/here/there/everywhere", url.toString());
    }

    // calling buildUrl with params that have slashes are trimmed
    @Test
    public void buildUrlStripsOffSlashes() {
        URL url = buildUrlTest.buildUrl("here/", "/there", "/everywhere");
        assertEquals(buildUrlBase + "/here/there/everywhere", url.toString());
    }

    // calling buildUrl with a param that is a query string doesn't put a slash between them
    @Test
    public void buildUrlWithQuestionMarkWorks() {
        URL url = buildUrlTest.buildUrl("here", "there", "?query=query");
        assertEquals(buildUrlBase + "/here/there?query=query", url.toString());
    }

    // calling buildUrl with a param that has a & for a new query param does not put a slash there
    @Test
    public void buildUrlWithQueryMultipleParamsWorks() {
        URL url = buildUrlTest.buildUrl("here", "there", "?query=query", "&bob=bob");
        assertEquals(buildUrlBase + "/here/there?query=query&bob=bob", url.toString());
    }
}
