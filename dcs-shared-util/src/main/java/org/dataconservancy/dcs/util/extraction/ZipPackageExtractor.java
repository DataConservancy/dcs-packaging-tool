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

import org.dataconservancy.dcs.util.FilePathUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An implementation of {@link PackageExtractor} handling specifically .zip files.
 */
public class ZipPackageExtractor extends BasePackageExtractor {

    @Override
    protected List<File> unpackFilesFromArchive(File file, String packageDir) throws Exception {
        if (file != null) {
            return unpackFilesFromStream(new ZipInputStream(new FileInputStream(file)), packageDir, file.getName());
        }

        return Collections.emptyList();
    }

    @Override
    public List<File> unpackFilesFromStream(InputStream packageInputStream, String packageDir, String fileName) throws UnpackException {

        List<File> files = new ArrayList<>();
        //use try-with-resources to make sure zip input stream is closed even in the event of an Exception
        try(ZipInputStream zipInStream = ZipInputStream.class.isAssignableFrom(packageInputStream.getClass()) ?
                (ZipInputStream) packageInputStream : new ZipInputStream(packageInputStream)){
            ZipEntry entry = zipInStream.getNextEntry();
            //Get next tar entry returns null when there are no more entries
            while (entry != null) {
                //Directories are automatically handled by the base class so we can ignore them in this class.
                if (!entry.isDirectory()) {
                    File entryFile = new File(packageDir, FilePathUtil.convertToPlatformSpecificSlash(entry.getName()));
                    List<File> savedFiles = saveExtractedFile(entryFile, zipInStream);
                    files.addAll(savedFiles);
                }
                entry = zipInStream.getNextEntry();
            }

            zipInStream.close();
        } catch (IOException e) {
            final String msg = "Error processing ZipInputStream: " + e.getMessage();
            log.error(msg, e);
            throw new UnpackException(msg, e);
        }

        return files;
    }

    public List<File> unpackFilesFromStreamWithEmptyDirectories(InputStream packageInputStream, String packageDir, String fileName) throws UnpackException {

        List<File> files = new ArrayList<>();
        //use try-with-resources to make sure zip input stream is closed even in the event of an Exception
        try(ZipInputStream zipInStream = ZipInputStream.class.isAssignableFrom(packageInputStream.getClass()) ?
                (ZipInputStream) packageInputStream : new ZipInputStream(packageInputStream)) {
            ZipEntry entry = zipInStream.getNextEntry();
            //Get next tar entry returns null when there are no more entries
            while (entry != null) {
                File entryFile = new File(packageDir, FilePathUtil.convertToPlatformSpecificSlash(entry.getName()));
                //Directories are automatically handled by the base class so we can ignore them in this class.
                if (!entry.isDirectory()) {
                    List<File> savedFiles = saveExtractedFile(entryFile, zipInStream);
                    files.addAll(savedFiles);
                } else {
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                        files.add(entryFile);
                    }
                }
                entry = zipInStream.getNextEntry();
            }

            zipInStream.close();
        } catch (IOException e) {
            final String msg = "Error processing ZipInputStream: " + e.getMessage();
            log.error(msg, e);
            throw new UnpackException(msg, e);
        }

        return files;
    }
}