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

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for providing the correct extractor for a given package file. 
 */
public class PackageSelectorImpl implements PackageSelector {
    private Map<String, PackageExtractor> extractors;
    
 
    public PackageSelectorImpl() {
        extractors = new HashMap<String, PackageExtractor>();        
    }
    
    @Override
    public PackageExtractor selectPackageExtractor(InputStream packageStream, Map<String, String> metadata) {
        PackageExtractor extractor = null;
        
        if (metadata.containsKey("Content-Disposition")) {
            String disposition = metadata.get("Content-Disposition");
            
            //This is order dependent as we want files that are packed then gzip (i.e tar.gz) to return the gzip extractor.
            if (disposition.contains(".gz") || disposition.contains(".gzip")) {
                extractor = extractors.get(GZIP_KEY);
            } else if (disposition.contains(".zip")) {
                extractor = extractors.get(ZIP_KEY);
            } else if (disposition.contains(".tar")) {
                extractor = extractors.get(TAR_KEY);                        
            }
            
        } 
        
        if (extractor == null && metadata.containsKey("Content-Type")) {
            String mimeType = metadata.get("Content-Type");
            
            if (mimeType.equalsIgnoreCase("application/x-gzip")) {
                extractor = extractors.get(GZIP_KEY);
            } else if (mimeType.equalsIgnoreCase("application/zip")) {
                extractor = extractors.get(ZIP_KEY);
            } else if (mimeType.equalsIgnoreCase("application/x-tar")) {
                extractor = extractors.get(TAR_KEY);
            }
        }
        return extractor;
    }
    
    public void setExtractors(Map<String, PackageExtractor> packageExtractors) {
        this.extractors = packageExtractors;
    }

}