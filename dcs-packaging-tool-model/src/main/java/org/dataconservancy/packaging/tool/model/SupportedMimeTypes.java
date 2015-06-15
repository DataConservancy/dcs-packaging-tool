/*
 * Copyright 2014 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.model;

import java.util.HashMap;
import java.util.Map;

public class SupportedMimeTypes {

    private static Map<String, String> mimeTypesMap ;

    /**
     * <p>
     * Returns mimetype for file format that could be produced by a {@code PackageAssembler}. So far the supported
     * formats and mimetypes are the one supported by the Apache Commons Compress library v1.3.
     * </p>
     *
     * Returns a generic "application/octet-stream" mimetype for format not supported.
     *
     * @param format the format
     * @return   a generic "application/octet-stream" mimetype for format not supported.
     */
    public static String getMimeType(String format) {
        if (mimeTypesMap == null) {
            mimeTypesMap = new HashMap<>();
            mimeTypesMap.put("ar", "application/x-unix-archive");
            mimeTypesMap.put("tar", "application/x-tar");
            mimeTypesMap.put("jar", "application/java-archive");
            mimeTypesMap.put("cpio", "application/x-cpio");
            mimeTypesMap.put("zip", "application/zip");

            mimeTypesMap.put("gz", "application/x-gzip");
            mimeTypesMap.put("gzip", "application/x-gzip");
            mimeTypesMap.put("bzip2", "application/x-bzip2");
            mimeTypesMap.put("pack200", "application/x-java-pack200");
        }
        String mimeType = mimeTypesMap.get(format);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

}
