/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.packaging.tool.impl;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.OutputStream;
import java.nio.file.attribute.FileTime;

/**
 *
 */
public class ZipArchiveStreamFactory implements ArchiveStreamFactory {

    private String encoding;

    private boolean fallbackToUTF8;

    private boolean useLanguageEncodingFlag;

    private int level;

    private int method;

    private Zip64Mode useZip64;

    public ZipArchiveOutputStream newArchiveOutputStream(OutputStream out) {
        ZipArchiveOutputStream zipOs = new ZipArchiveOutputStream(out);
        zipOs.setEncoding(encoding);
        zipOs.setFallbackToUTF8(fallbackToUTF8);
        zipOs.setUseLanguageEncodingFlag(useLanguageEncodingFlag);
        zipOs.setLevel(level);
        zipOs.setMethod(method);
        zipOs.setUseZip64(useZip64);

        return zipOs;
    }

    public ZipArchiveEntry newArchiveEntry(String name, long sizeBytes, FileTime created, FileTime lastModified,
                                           int unixPermissions, long crc) {
        ZipArchiveEntry zipArxEntry = new ZipArchiveEntry(name);
        zipArxEntry.setSize(sizeBytes);
        zipArxEntry.setUnixMode(unixPermissions);
        zipArxEntry.setLastModifiedTime(lastModified);
        zipArxEntry.setCreationTime(created);
        zipArxEntry.setCrc(crc);
        return zipArxEntry;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isFallbackToUTF8() {
        return fallbackToUTF8;
    }

    public void setFallbackToUTF8(boolean fallbackToUTF8) {
        this.fallbackToUTF8 = fallbackToUTF8;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public boolean isUseLanguageEncodingFlag() {
        return useLanguageEncodingFlag;
    }

    public void setUseLanguageEncodingFlag(boolean useLanguageEncodingFlag) {
        this.useLanguageEncodingFlag = useLanguageEncodingFlag;
    }

    public Zip64Mode getUseZip64() {
        return useZip64;
    }

    public void setUseZip64(Zip64Mode useZip64) {
        this.useZip64 = useZip64;
    }
}
