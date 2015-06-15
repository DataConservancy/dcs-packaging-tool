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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A utility class for doing URI-related work for verification and uniform File handling
 */
public class UriUtility {


    /**
     * Determine if a URL is a URL using HTTP or HTTPS protocols
     * @param toCheck The URI to check
     * @return true if the URI is a URL with a non-empty host and uses either the http or https protocol
     */
    public static boolean isHttpUrl(URI toCheck) {
        if (toCheck.getHost() != null && (toCheck.getScheme().equals("http") || toCheck.getScheme().equals("https"))) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Determine if a string is a URL with HTTP or HTTPS protocols
     * @param toCheck the string to check
     * @return true if the string is a URL with http or https protocols
     */
    public static boolean isHttpUrl(String toCheck) {
        try {
            return isHttpUrl(new URI(toCheck));
        } catch (URISyntaxException e) {
            return false;
        }
    }


    /**
     * Determine if a URI is resolvable.  Currently this means the URI is a valid URL
     * @param toCheck the URI to check
     * @return true if resolvable, false if not
     */
    public static boolean isResolvable(URI toCheck) {
        try {
            toCheck.toURL();
        } catch (MalformedURLException e) {
            return false;
        }

        return true;
    }


    /**
     * Create a URI string for a file, ensuring that it has 3 slashes to meet File URL specifications
     * @param file The file to check.  This doesn't have to be an actual existing file
     * @param basedir The directory to make the file URI relative to.  Can be null.  If not null, the basedir must be
     *                in the path of the file parameter, or an exception will be thrown
     * @return A string representing the URI to the file on the local disk.
     * @throws URISyntaxException if there is an error in the URI syntax
     */
    public static URI makeFileUriString(File file, File basedir) throws URISyntaxException {
        if (basedir == null) {
            basedir = new File(".");
        }

        String path = FilePathUtil.convertToUnixSlashes(FilePathUtil.relativizePath(basedir.getPath(), file));

        // Remove leading slashes from the path
        path = path.replaceFirst("^\\/*", "");

        return new URI("file", null, "///"+path, null);
    }
}
