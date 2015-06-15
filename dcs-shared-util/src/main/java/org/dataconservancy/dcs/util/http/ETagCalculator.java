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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.lang.Integer;import java.lang.RuntimeException;import java.lang.String;import java.lang.StringBuilder;import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * {@code ETagCalculator} is used by dcs-ui {@code Controllers} to calculate ETag to be set on the HTTP request/response headers.
 */
public class ETagCalculator {

    public static String calculate(String id) {
        if (id.isEmpty()) {
            return null;
        }

        NullOutputStream nullOut = new NullOutputStream();
        DigestOutputStream digestOut = null;

        try {
            digestOut =
                    new DigestOutputStream(nullOut,
                                           MessageDigest.getInstance("MD5"));
            IOUtils.write(id, digestOut);
            digestOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return digestToHexString(digestOut.getMessageDigest().digest());
    }

    private static String digestToHexString(byte[] digest) {
        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            final String hex = Integer.toHexString(b & 0x000000ff);
            if (hex.length() == 1) {
                result.append(0);
            }
            result.append(hex);
        }

        return result.toString();
    }
}
