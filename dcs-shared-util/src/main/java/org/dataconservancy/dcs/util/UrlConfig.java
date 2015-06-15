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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@code UrlConfig} is used to configure the base URL of the dcs-ui web-app with hosting server information.
 */
public class UrlConfig {

    protected String hostname;
    protected int port;
    protected boolean isSecure;
    protected String contextPath;
    protected Scheme scheme;

    protected enum Scheme {HTTP, HTTPS}

    /**
     * Retrieve the base URL from the parameters used to configure this object
     * @return a URL corresponding to the parameters previously set.
     * @throws NullPointerException if Scheme, Host, or Context Path not set
     */
    public URL getBaseUrl() {
        URL u = null;
        try {
            u = new URL(
                    scheme.toString().toLowerCase(),
                    hostname,
                    port,
                    contextPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return u;
    }

    /**
     * Gets the currently configured host name
     * @return The host name previously set (null if none set)
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the host name.
     * @param hostname The host name.  Cannot be null or empty, and cannot
     *                 contain a colon (IE, scheme and port info should not be
     *                 part of it)
     */
    public void setHostname(String hostname) {
        if (hostname == null || hostname.trim().length() == 0) {
            throw new IllegalArgumentException("Hostname must not be empty or null.");
        }
        if (hostname.contains(":")) {
            throw new IllegalArgumentException("Hostname should not contain a port or url scheme, just a fully qualified domain name.");
        }
        this.hostname = hostname.toLowerCase().trim();
    }

    /**
     * Gets the port number previously set
     * @return The port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port
     * @param port The port number.  Cannot be less than 1
     */
    public void setPort(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("Port must be 1 or greater.");
        }
        this.port = port;
    }

    /**
     * Tells you if the URL uses secure protocol
     * @return True if using HTTPS, false if not
     */
    public boolean isSecure() {
        return isSecure;
    }

    /**
     * Sets whether the URL should be secure
     * @param secure Security to set.  Will change scheme to HTTP or HTTPS as appropriate
     */
    public void setSecure(boolean secure) {
        isSecure = secure;
        if (secure && scheme != Scheme.HTTPS) {
            scheme = Scheme.HTTPS;
        } else if (!secure && scheme == Scheme.HTTPS) {
            scheme = Scheme.HTTP;
        }
    }

    /**
     * Gets the current context path
     * @return the context path, or null if it hasn't been set
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path for the URL
     * @param contextPath the context path.  Cannot be null, but can be an empty string.
     *                    Trailing slashes will be removed.
     */
    public void setContextPath(String contextPath) {
        if (contextPath == null) {
            throw new IllegalArgumentException("Context path must not be null.");
        }
        this.contextPath = normalizePath(contextPath);
    }

    /**
     * Gets the scheme for the URL
     * @return http or https
     */
    public String getScheme() {
        return scheme.toString().toLowerCase();
    }

    /**
     * Sets the scheme for the URL
     * @param scheme should be either http or https (case doesn't matter)
     */
    public void setScheme(String scheme) {
        if (scheme == null || scheme.trim().length() == 0) {
            throw new IllegalArgumentException("Protocol scheme must not be empty or null.");
        }
        scheme = scheme.toUpperCase().trim();
        this.scheme = Scheme.valueOf(scheme);
        if (this.scheme == Scheme.HTTPS) {
            isSecure = true;
        }
    }

    /**
     * Builds a URL using previously set configuration and path segments as
     * specified
     * @param pathparts One or more path segments.  Leading slashes from each
     *                  segment will be removed.  Path segments will be joined
     *                  with slashes between them.  Path segments that start with
     *                  ? or &amp; (as part of a query string) will not have a slash
     *                  appended.
     * @return The URL representing the base URL and path segments combined.
     */
    public URL buildUrl(String... pathparts) {
        StringBuilder builder = new StringBuilder();
        for (String pathpart : pathparts) {
            pathpart = pathpart.replaceAll("^\\/*", "").replaceAll("\\/*$","");

            if (!pathpart.startsWith("?") && !pathpart.startsWith("&")) {
                builder.append("/");
            }

            builder.append(pathpart);
        }

        try {
            URL url = new URL(getBaseUrl().toString() + builder.toString());
            return url;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Lowercases and trims a String, and insures that the string doesn't
     * end with a "/".
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    public String normalizePath(String path) {
        path = path.toLowerCase().trim();
        while (path.endsWith("/") && path.length() > 0) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String toString() {
        return "UiBaseUrlConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", isSecure=" + isSecure +
                ", contextPath='" + contextPath + '\'' +
                ", scheme=" + scheme +
                '}';
    }
}
