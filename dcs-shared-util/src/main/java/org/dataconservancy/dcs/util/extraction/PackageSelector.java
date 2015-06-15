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
package org.dataconservancy.dcs.util.extraction;

import java.io.InputStream;
import java.util.Map;

/**
 * Interface for providing the correct extractor for a given package file. 
 */
public interface PackageSelector {
    
    public static final String ZIP_KEY = "ZIP";
    public static final String GZIP_KEY = "GZIP";
    public static final String TAR_KEY = "TAR";

    /**
     * Returns the correct extractor to use to unpack the supplied package. 
     * @param packageStream The input stream of the package file. 
     * Currently not used, but callers should ensure they maintain a copy of the input stream, for extraction.
     * @param metadata Metadata to help detemine the type of the package, can be empty or null.
     * Currently empty or null metadata will result in null being returned.
     * @return The package extractor to use to unpack the supplied package, or null if none can be found.
     */
    public PackageExtractor selectPackageExtractor(InputStream packageStream, Map<String, String> metadata);
}