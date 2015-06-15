/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.cli.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;

/**
 * Loads content from a list of locations specified on the commandline, or
 * STDIN.
 */
public class ContentLoader {

    public static Map<String, InputStream> loadContentFrom(List<String> inputLocations)
            throws PackageToolException {
        Map<String, InputStream> content = new HashMap<String, InputStream>();

        /* Default if no input locations is stdin */
        if (inputLocations == null || inputLocations.size() == 0) {
            content.put("-", System.in);
            return content;
        }

        try {
            for (String location : inputLocations) {
                if ("-".equals(location) || "".equals(location)) {
                    content.put("-", System.in);
                    continue;
                }

                File file = new File(location);
                if (file.exists()) {
                    content.put(location, new FileInputStream(file));
                } else {
                    /*
                     * See if it's a URL. If not, give up (it'll throw an
                     * exception)
                     */
                    content.put(location, URI.create(location).toURL()
                            .openStream());
                }
            }
        } catch (Exception e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION,
                                           e);
        }

        return content;
    }
}
