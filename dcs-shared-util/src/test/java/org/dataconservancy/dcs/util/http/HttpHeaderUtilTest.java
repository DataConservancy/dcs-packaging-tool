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
package org.dataconservancy.dcs.util.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import org.dataconservancy.dcs.util.http.HttpHeaderUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HttpHeaderUtilTest {

    private static final String CONTENT_MD5 =
            "37c4b87edffc5d198ff5a185cee7ee09";

    private static final String CONTENT_SHA =
            "be417768b5c3c5c1d9bcb2e7c119196dd76b5570";

    private static final String CONTENT_SHA256 =
            "c03905fcdab297513a620ec81ed46ca44ddb62d41cbbd83eb4a5a3592be26a69";

    @Test
    public void getContentMD5Test() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();

        headers.put(HttpHeaderUtil.CONTENT_MD5, getBase64(CONTENT_MD5));

        Map<String, byte[]> out = HttpHeaderUtil.getDigests(headers);
        assertEquals(1, out.size());

        assertEquals("MD5", out.keySet().iterator().next());
        assertEquals(CONTENT_MD5, encodeHex(out.get("MD5")));
    }

    @Test
    public void getSingleShaTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();

        headers.put(HttpHeaderUtil.DIGEST, "SHA=" + getBase64(CONTENT_SHA));

        Map<String, byte[]> out = HttpHeaderUtil.getDigests(headers);
        assertEquals(1, out.size());

        assertEquals("SHA", out.keySet().iterator().next());
        assertEquals(CONTENT_SHA, encodeHex(out.get("SHA")));
    }

    @Test
    public void getMultipleDigestsTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();

        headers.put(HttpHeaderUtil.CONTENT_MD5, getBase64(CONTENT_MD5));
        headers.put(HttpHeaderUtil.DIGEST, "SHA=" + getBase64(CONTENT_SHA)
                + ",SHA256=" + getBase64(CONTENT_SHA256));

        Map<String, byte[]> out = HttpHeaderUtil.getDigests(headers);
        assertEquals(3, out.size());
        assertTrue(out.keySet().containsAll(Arrays.asList("MD5",
                                                          "SHA",
                                                          "SHA256")));
        assertEquals(CONTENT_MD5, encodeHex(out.get("MD5")));
        assertEquals(CONTENT_SHA, encodeHex(out.get("SHA")));
        assertEquals(CONTENT_SHA256, encodeHex(out.get("SHA256")));
    }

    @Test
    public void noisyDigestInputTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderUtil.DIGEST, "SHA = " + getBase64(CONTENT_SHA)
                + " ; stupidParam=nothing , SHA256 = "
                + getBase64(CONTENT_SHA256) + " ; stupidParam=nothing");
        Map<String, byte[]> out = HttpHeaderUtil.getDigests(headers);
        assertEquals(2, out.size());
        assertEquals(CONTENT_SHA, encodeHex(out.get("SHA")));
        assertEquals(CONTENT_SHA256, encodeHex(out.get("SHA256")));
    }

    @Test
    public void badDigestInputTest() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderUtil.DIGEST, ",,,==,===,=,,,=,,=");
        Map<String, byte[]> out = HttpHeaderUtil.getDigests(headers);
        assertEquals(0, out.size());
    }

    @Test
    public void roundTripTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("MD5", getBytes(CONTENT_MD5), headers);
        HttpHeaderUtil.addDigest("SHA", getBytes(CONTENT_SHA), headers);
        HttpHeaderUtil.addDigest("SHA256", getBytes(CONTENT_SHA256), headers);

        Map<String, byte[]> out = HttpHeaderUtil.getDigests(headers);

        assertEquals(3, out.size());
        assertTrue(out.keySet().containsAll(Arrays.asList("MD5",
                                                          "SHA",
                                                          "SHA256")));
        assertEquals(CONTENT_MD5, encodeHex(out.get("MD5")));
        assertEquals(CONTENT_SHA, encodeHex(out.get("SHA")));
        assertEquals(CONTENT_SHA256, encodeHex(out.get("SHA256")));

    }

    @Test
    public void fileNameNoInlineAttachment() {
        final String FILENAME = "test.tst";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderUtil.CONTENT_DISPOSITION, "filename=" + FILENAME);

        assertEquals(FILENAME, HttpHeaderUtil.getFileName(headers));
    }

    @Test
    public void fileNameInlineAttachment() {
        final String FILENAME = "test.tst";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderUtil.CONTENT_DISPOSITION, "inline;filename="
                + FILENAME);

        assertEquals(FILENAME, HttpHeaderUtil.getFileName(headers));
    }

    @Test
    public void fileNameNotPresent() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderUtil.CONTENT_DISPOSITION, "inline");

        assertNull(HttpHeaderUtil.getFileName(headers));
    }

    @Test
    public void fileNameHeaderNotPresent() {
        Map<String, String> headers = new HashMap<String, String>();
        assertNull(HttpHeaderUtil.getFileName(null));
        assertNull(HttpHeaderUtil.getFileName(headers));
    }

    @Test
    public void getValueNotPresentTest() {
        Map<String, String> headers = new HashMap<String, String>();
        assertNull(HttpHeaderUtil.getValue("key", headers));
    }

    @Test
    public void simplegetValueTest() {
        final String SIMPLE_VALUE = "val";
        final String key = "key";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(key, SIMPLE_VALUE);
        assertEquals(SIMPLE_VALUE, HttpHeaderUtil.getValue(key, headers));
    }

    @Test
    public void getValueWithNoiseTest() {
        final String SIMPLE_VALUE = "val";
        final String key = "key";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(key, SIMPLE_VALUE + "; x=y; z=q, another");
        assertEquals(SIMPLE_VALUE, HttpHeaderUtil.getValue(key, headers));
    }

    private String getBase64(String hex) throws DecoderException {
        return new String(Base64.encodeBase64(Hex.decodeHex(hex.toCharArray())));
    }

    private String encodeHex(byte[] input) {
        return new String(Hex.encodeHex(input));
    }

    private byte[] getBytes(String hex) throws DecoderException {
        return Hex.decodeHex(hex.toCharArray());
    }
}
