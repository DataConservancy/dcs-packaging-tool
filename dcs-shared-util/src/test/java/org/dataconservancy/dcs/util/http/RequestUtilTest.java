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
package org.dataconservancy.dcs.util.http;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.Assert.assertEquals;

/**
 * Created by jrm on 3/23/15.
 */
public class RequestUtilTest {
    private RequestUtil requestUtil = new RequestUtil();

    /**
     * Tests that if the always include port flag is set the port is included in the returned request url.
     */
    @Test
    public void testAlwaysIncludePortFlag() {
        final String expectedNoPort = "http://test.org/file/foo";
        final String secureNoPort = "https://test.org/file/foo";
        final String expectedPort = "https://test.org:443/file/foo";
        final String expectedPortEighty = "http://test.org:80/file/foo";

        //Test that port 80 is not included when the always include flag is false
        final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/file/foo");
        req.setRemoteHost("test.org");
        req.setScheme("http");
        req.setRemotePort(80);
        req.setSecure(false);

        String url = requestUtil.buildRequestUrl(req);

        assertEquals(expectedNoPort, url);

        //Test that by setting the flag to true the port is included
        requestUtil.setAlwaysIncludePort(true);
        url = requestUtil.buildRequestUrl(req);
        assertEquals(expectedPortEighty, url);

        //Test that port 443 is not included when always include flag is false
        req.setRemotePort(443);
        req.setScheme("https");
        req.setSecure(true);
        requestUtil.setAlwaysIncludePort(false);
        url = requestUtil.buildRequestUrl(req);

        assertEquals(secureNoPort, url);

        //Test that by setting the flag to true the port is included
        requestUtil.setAlwaysIncludePort(true);
        url = requestUtil.buildRequestUrl(req);
        assertEquals(expectedPort, url);

    }

    /**
     * Tests that if the port is a default port 80 and the type is unsecure it's not included. And that port is included if request is secure.
     */
    @Test
    public void testSecurePortIncluded() {
        final String expectedNoPort = "http://test.org/file/foo";
        final String expectedSecurePort = "https://test.org:80/file/foo";

        //Test that port 80 is not included when the always include flag is false
        final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/file/foo");
        req.setRemoteHost("test.org");
        req.setScheme("http");
        req.setRemotePort(80);
        req.setSecure(false);

        String url = requestUtil.buildRequestUrl(req);

        assertEquals(expectedNoPort, url);

        //Test that by setting the request to secure the port is included
        req.setScheme("https");
        req.setSecure(true);
        url = requestUtil.buildRequestUrl(req);
        assertEquals(expectedSecurePort, url);
    }

    @Test
    public void testBuildRequestUrlPort80Secure() throws Exception {
        final String expected = "https://instance.org:80/lineage/123";

        final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("https");
        req.setRemotePort(80);
        req.setSecure(true);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort443() throws Exception {
        final String expected = "https://instance.org/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("https");
        req.setRemotePort(443);
        req.setSecure(true);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort443Insecure() throws Exception {
        final String expected = "http://instance.org:443/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("http");
        req.setRemotePort(443);
        req.setSecure(false);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort8080Insecure() throws Exception {
        final String expected = "http://instance.org:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("http");
        req.setRemotePort(8080);
        req.setSecure(false);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort8443Secure() throws Exception {
        final String expected = "https://instance.org:8443/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("https");
        req.setRemotePort(8443);
        req.setSecure(true);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort80() throws Exception {
        final String expected = "http://instance.org/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhost() throws Exception {
        final String expected = "http://localhost/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("localhost");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlSecureLocalhost() throws Exception {
        final String expected = "https://localhost:80/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("localhost");
        req.setScheme("https");
        req.setSecure(true);
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhost8080() throws Exception {
        final String expected = "http://localhost:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("localhost");
        req.setScheme("http");
        req.setRemotePort(8080);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }


    @Test
    public void testBuildRequestUrlLocalhostIp() throws Exception {
        final String expected = "http://localhost/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("127.0.0.1");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhostIp8080() throws Exception {
        final String expected = "http://localhost:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("127.0.0.1");
        req.setScheme("http");
        req.setRemotePort(8080);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhostIpv6() throws Exception {
        final String expected = "http://localhost/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("0:0:0:0:0:0:0:1%0");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhostIpv68080() throws Exception {
        final String expected = "http://localhost:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("0:0:0:0:0:0:0:1%0");
        req.setScheme("http");
        req.setRemotePort(8080);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlWithHostHeader() throws Exception {
        final String expected = "http://www.foo.com/lineage/123";
        final String hostHeader = "www.foo.com:80";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.addHeader("Host", hostHeader);
        req.setRemoteHost("bar.baz");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlWithHostHeaderNoPort() throws Exception {
        final String expected = "http://www.foo.com/lineage/123";
        final String hostHeader = "www.foo.com";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.addHeader("Host", hostHeader);
        req.setRemoteHost("bar.baz");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

}
