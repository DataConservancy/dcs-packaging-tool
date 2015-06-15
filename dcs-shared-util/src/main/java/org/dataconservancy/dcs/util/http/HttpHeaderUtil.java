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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools for manipulating http headers or http header-like maps.
 * <p>
 * Provides convenience methods for representing maps of key+value pairs using
 * http header conventions.
 * </p>
 */
public class HttpHeaderUtil {

    private static final Logger log =
            LoggerFactory.getLogger(HttpHeaderUtil.class);

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String CONTENT_MD5 = "Content-MD5";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String DIGEST = "Digest";

    /**
     * Parse and return all content digests.
     * <p>
     * Looks for headers {@code Content-Type} (<a
     * href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>) and
     * {@code Digest} (<a href="http://www.ietf.org/rfc/rfc3230.txt">RFC 3230</a>)
     * for any declared digests.  Produces a map keyed on algorithm and containing 
     * the digest bytes.
     * </p>
     * 
     * @param headers
     *        Map of key/value pairs using http header naming and value
     *        conventions.
     * @return Map containing all digest algorithms and associated values.
     */
    public static Map<String, byte[]> getDigests(Map<String, String> headers) {
        Map<String, byte[]> digests = new HashMap<String, byte[]>();
        if (headers == null) return digests;

        if (headers.containsKey(CONTENT_MD5)) {
            digests.put("MD5", Base64.decodeBase64(cleanDigest(headers
                    .get(CONTENT_MD5)).getBytes()));
        }

        if (headers.containsKey(DIGEST)) {
            String[] values = cleanDigest(headers.get(DIGEST)).split(",");

            for (String value : values) {
                try {
                    String[] decomp = value.split("=", 2);
                    if (decomp[0].length() > 0 && decomp[1].length() > 0) {
                        digests.put(decomp[0], Base64.decodeBase64(decomp[1]
                                .getBytes()));
                    }
                } catch (Exception e) {
                    log.warn("Could not parse digest header, skipping: '%s'",
                             headers.get(DIGEST));
                }
            }
        }

        return digests;
    }

    /**
     * Serializes a digest ad adds to existing http header map.
     * <p>
     * Follows <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a> for
     * md5 values, and <a href="http://www.ietf.org/rfc/rfc3230.txt">RFC
     * 3230</a> for everything else.
     * </p>
     * 
     * @param algo
     *        Digest algorithm
     * @param val
     *        Digest value as byte array.
     * @param headers
     *        Map containing http-like headers.
     */
    public static void addDigest(String algo,
                                 byte[] val,
                                 Map<String, String> headers) {
        if (headers == null) return;
        String base64 = new String(Base64.encodeBase64(val));
        if (algo.equals("MD5")) {
            headers.put(CONTENT_MD5, base64);
        } else {
            String digestString = String.format("%s=%s", algo, base64);
            if (headers.containsKey(DIGEST)) {
                headers.put(DIGEST, String.format("%s,%s=%s", headers
                        .get(DIGEST), algo, base64));
            } else {
                headers.put(DIGEST, digestString);
            }
        }
    }

    /**
     * Get a file name from a map of http-style headers.
     * <p>
     * Will introspect into the Content-disposition value (if present), and
     * return a filename (if present). Null otherwise.
     * </p>
     * 
     * @param headers
     *        Map containing header names and encoded values as per http header
     *        conventions
     * @return Filename if found, null otherwise.
     */
    public static String getFileName(Map<String, String> headers) {
        if (headers == null) return null;
        String disposition = headers.get(CONTENT_DISPOSITION);
        if (disposition != null) {
            String[] parts = disposition.split("filename=");
            if (parts.length == 2) return parts[1];
        }
        return null;
    }

    /**
     * Get a single http header-style value, where only one value is expected.
     * <p>
     * Will simply discard parameters. In the case of multiple values, it will
     * return the first.
     * </p>
     * 
     * @param key
     *        header name
     * @param headers
     *        Map containing header names and encoded values as per http header
     *        conventions.
     * @return String value if found, null if not.
     */
    public static String getValue(String key, Map<String, String> headers) {
        if (headers == null || !headers.containsKey(key)) return null;

        String[] values = headers.get(key).split(",");

        return values[0].split(";")[0];
    }

    private static String cleanDigest(String input) {
        String out =
                input.replaceAll(" ", "").replaceAll(";.+?\\,", ",")
                        .replaceAll(";.+", "");
        return out;
    }
}
