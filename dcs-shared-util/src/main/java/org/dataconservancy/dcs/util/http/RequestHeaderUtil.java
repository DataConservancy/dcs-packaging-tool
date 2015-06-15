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

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.dcs.util.http.RequestUtil;
import org.joda.time.DateTime;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.String;
import java.util.Date;
import java.util.List;


/**
 * Utility class for building an HTTP response header
 */
public class RequestHeaderUtil {

    public static final String ACCEPT = "Accept";
    public static final String ETAG = "ETag";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String LOCATION = "Location";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_JSON = "application/json";
    public static final String ACCEPT_WILDCARD = "*/*";
    public static final String ACCEPT_APPLICATION_WILDCARD = "application/*";
    public static final String ACCEPT_OCTET_STREAM = "application/octet-stream";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String IF_MATCH = "If-Match";
    public static final String IF_NONE_MATCH = "If-None-Match";

    /**
     *
     * @param req - the HttpServletRequest
     * @param res - the HttpServletResponse
     * @param ifMatch  - the match string
     * @param requestUtil - the request util
     * @param objectToMatch - the object to match
     * @param objectToMatchEtag - the etag to match
     * @param objectToMatchId - the id to match
     * @param objectToMatchClassname - the class name to match
     * @return true if the response has been committed
     * @throws java.io.IOException an IO exception
     */
    public boolean handleIfMatch(HttpServletRequest req, HttpServletResponse res,
                                 RequestUtil requestUtil, String ifMatch, Object objectToMatch,
                                 String objectToMatchEtag, String objectToMatchId,
                                 String objectToMatchClassname) throws IOException {

        if (ifMatch == null || ifMatch.trim().length() == 0) {
            return false;
        }

        if (objectToMatch == null) {
            if (ifMatch.contains("*")) {
                res.sendError(HttpServletResponse.SC_PRECONDITION_FAILED,
                        "Unable to resolve " + objectToMatchClassname + " '" + requestUtil.buildRequestUrl(req) + "'");
            }

            return true;
        }

        // Split the values
        String[] ifMatchValues = ifMatch.split(",");
        boolean match = false;
        for (String ifMatchValue : ifMatchValues) {
            ifMatchValue = ifMatchValue.trim();
            if (ifMatchValue.equals("*") || ifMatchValue.equals(objectToMatchEtag)) {
                match = true;
            }
        }

        if (!match) {
            res.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "If-Match header '" + ifMatch + "' " +
                    "did not match the requested " + objectToMatchClassname + " representation: '" + objectToMatchId +
                    "' ('" + objectToMatchEtag + "')");
            return true;
        }

        return false;
    }

    public boolean handleIfModifiedSince(HttpServletRequest req, HttpServletResponse res, Date ifModifiedSince,
                                         DateTime lastModified) throws IOException {

        if (lastModified == null || ifModifiedSince == null) {
            return false;
        }

        final DateTime ifModifiedInstant = new DateTime(ifModifiedSince);

        if (ifModifiedInstant.isAfter(DateTime.now())) {
            return false;
        }

        if (lastModified.isAfter(ifModifiedInstant)) {
            return false;
        }

        res.sendError(HttpServletResponse.SC_NOT_MODIFIED);

        return true;
    }

    /**
     * Implementation of RFC 2616 14.26
     *
     * @param req the HttpServletRequest
     * @param res  the HttpServletResponse
     * @param ifNoneMatch - comma separated list of etags to not match
     * @param objectToMatch - the object to match
     * @param objectToMatchEtag  - the etag to match
     * @param objectToMatchId  - the ID to match
     * @param lastModifiedDate - the last modified Date
     * @param ifModifiedSince  - the modification reference Date
     * @return true if the response has been committed
     * @throws IOException  an IO exception
     */
    public boolean handleIfNoneMatch(HttpServletRequest req, HttpServletResponse res, String ifNoneMatch,
                                     Object objectToMatch, String objectToMatchEtag, String objectToMatchId,
                                     DateTime lastModifiedDate, Date ifModifiedSince) throws IOException {
        // If the header is null or empty, we don't do anything, simply return.
        if (ifNoneMatch == null || ifNoneMatch.trim().length() == 0) {
            return false;
        }

        // A objectToMatch was resolved ...
        if (objectToMatch != null) {

            // The client is performing a conditional request, based on the existence (or not) of any
            // version of the objectToMatch.
            if (ifNoneMatch.equals("*")) {

                if (ifModifiedSince == null) {
                    // A objectToMatch exists, but If-None-Match was set to "*", and there is no If-Modified-Since header
                    // to consider.
                    res.addHeader(LAST_MODIFIED, DateUtility.toRfc822(lastModifiedDate));
                    res.addHeader(ETAG, objectToMatchEtag);
                    res.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                    return true;
                } else {
                    return handleIfModifiedSince(req, res, ifModifiedSince, lastModifiedDate);
                }

            }

            // The client is performing a conditional request, based on the existence (or not) of the specified
            // objectToMatch.
            final String[] candidateEtags = ifNoneMatch.split(",");
            for (String candidateEtag : candidateEtags) {
                if (candidateEtag.trim().equals(objectToMatchEtag)) {
                    res.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "If-None-Match header '" + ifNoneMatch +
                            "' matched the requested Collection representation: '" + objectToMatchId + "' ('" +
                            objectToMatchEtag + "')");
                    return true;
                }
            }

        }

        return false;
    }

    /**
     * Wrapper method to set fields in the HTTP response.
     *
     * @param response - the HttpServletResponse
     * @param etag - the etag
     * @param out - the output stream
     * @param lastModified - tha lsast modified DateTime
     * @throws IOException  an IO exception
     */
    public void setResponseHeaderFields(HttpServletResponse response, String etag,
                                        ByteArrayOutputStream out, DateTime lastModified) throws IOException {
        // Compose the Response (headers, entity body)
        response.setHeader(ETAG, etag);
        response.setHeader(CONTENT_LENGTH, String.valueOf(out.size()));
        response.setHeader(CONTENT_TYPE, APPLICATION_XML);
        response.setHeader(LAST_MODIFIED, DateUtility.toRfc822(lastModified));

        response.setStatus(HttpServletResponse.SC_OK);
    }

}
