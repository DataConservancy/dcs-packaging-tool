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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
;

/**
 * Created by jrm on 3/20/15.
 */
public class ServletUtil {

    /**
     * Obtain the requested resource from the HttpServletRequest. The resource
     * is encoded in the path of the request URL.
     *
     * @param req
     *        the HttpServletRequest
     * @return resource specified by request
     */
    public static String getResource(HttpServletRequest req) {
        String path = req.getPathInfo();

        if (path == null || path.length() < 2 || path.charAt(0) != '/') {
            return null;
        }

        return path.substring(1);
    }

    /**
     * @param req the HttpServletRequest
     * @return entity identifier specified by request
     * @throws java.io.UnsupportedEncodingException if the the request encoding is unsupported
     */
    public static String getEntityId(HttpServletRequest req)
            throws UnsupportedEncodingException {
        return req.getRequestURL().toString();
    }

    /**
     * @param path the path to be encoded
     * @return path encoded for inclusion in a URL.
     */
    public static String encodeURLPath(String path) {
        try {
            String s = URLEncoder.encode(path, "UTF-8");

            // Have to encode spaces (now plus) using %20

            return s.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
