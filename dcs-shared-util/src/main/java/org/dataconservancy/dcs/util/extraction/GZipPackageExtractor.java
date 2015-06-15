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
import java.util.zip.GZIPInputStream;

public class GZipPackageExtractor extends BasePackageExtractor {

    private File compressedFile;

    /**
     * {@inheritDoc}
     * Note: Due to the nature of GZIP files this method will only return a single file in the list.
     */
    @Override
    protected List<File> unpackFilesFromArchive(File archive, String packageDir) throws Exception {
        FileInputStream fileStream = new FileInputStream(archive);

        //Create a new file from the gzipped file name minus the extension
        this.compressedFile = new File(packageDir,
                archive.getName().substring(0, archive.getName().lastIndexOf('.')));

        //Create a GZIPInputStream to uncompress the file 
        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);

        return unpackFilesFromStream(gzipStream, packageDir, archive.getName());
    }

    @Override
    public List<File> unpackFilesFromStream(InputStream packageInputStream, String packageDir, String fileName) throws UnpackException {

        List<File> files = new ArrayList<>();
        //use try-with-resources to make sure gzip input stream is closed even in the event of an Exception
        try (GZIPInputStream gzipStream = GZIPInputStream.class.isAssignableFrom(packageInputStream.getClass()) ?
                (GZIPInputStream) packageInputStream : new GZIPInputStream(packageInputStream)){
            // Create a new file from the gzipped file name minus the extension
            if (this.compressedFile == null) {
                this.compressedFile = new File(packageDir, fileName.substring(0, fileName.lastIndexOf('.')));
            }

            //Save the extracted file into the new file
            List<File> savedFiles = saveExtractedFile(compressedFile, gzipStream);
            //Add the extracted file to the list, there should be only one file at this point.
            files.addAll(savedFiles);
        } catch (Exception e) {
            this.compressedFile = null;
            final String msg =
                "Error processing GZIPInputStream: " + e.getMessage();
            log.error(msg, e);
            throw new UnpackException(msg, e);
        }

        // reset internal state
        this.compressedFile = null;

        return files;
    }
}