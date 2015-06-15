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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang.StringUtils;
import org.dataconservancy.dcs.util.FilePathUtil;

public class TarPackageExtractor extends BasePackageExtractor {

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<File> unpackFilesFromArchive(File archive, String packageDir) throws Exception {
        FileInputStream fileStream = new FileInputStream(archive);
        TarArchiveInputStream tarInStream = new TarArchiveInputStream(fileStream);
        return unpackFilesFromStream(tarInStream, packageDir, archive.getName());
    }

    @Override
    public List<File> unpackFilesFromStream(InputStream packageInputStream, String packageDir, String fileName) throws UnpackException {

        List<File> files = new ArrayList<>();
        //use try-with-resources to make sure tar input stream is closed even in the event of an Exception
        try(TarArchiveInputStream tarInStream = TarArchiveInputStream.class.isAssignableFrom(packageInputStream.getClass()) ?
                (TarArchiveInputStream)packageInputStream :  new TarArchiveInputStream(packageInputStream)){
            TarArchiveEntry entry = tarInStream.getNextTarEntry();
            //Get next tar entry returns null when there are no more entries
            while (entry != null) {
                //Directories are automatically handled by the base class so we can ignore them in this class.
                if (!entry.isDirectory()) {
                    File entryFile = new File(packageDir, FilePathUtil.convertToPlatformSpecificSlash(entry.getName()));
                    List<File> savedFiles = saveExtractedFile(entryFile, tarInStream);
                    files.addAll(savedFiles);
                }
                entry = tarInStream.getNextTarEntry();
            }

            tarInStream.close();
        } catch (IOException e) {
            final String msg = "Error processing TarArchiveInputStream: " + e.getMessage();
            log.error(msg, e);
            throw new UnpackException(msg, e);
        }

        return files;
    }
}