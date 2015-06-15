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
package org.dataconservancy.dcs.util.droid;

import org.dataconservancy.dcs.model.DetectedFormat;
import org.dataconservancy.dcs.util.FilePathUtil;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps the Droid api and provides an easy method to be called by the service implementation.
 * 
 */
public class DroidDriver {
    public final static String PRONOM_SCHEME_URI = "http://www.nationalarchives.gov.uk/PRONOM/";
    public final static String MIME_TYPE_SCHEME_URI = "http://www.iana.org/assignments/media-types/";
    DroidIdentifier droidIdentifier = new DroidIdentifier();

    /**
     * Takes a file and returns a list of DetectedFormats after running file identification.  If the file
     * doesn't actually exist it will simply use detection based on the file name.
     * 
     * @param file  the file.
     * @return a List of the detected file formats
     */
    public List<DetectedFormat> detectFormats(File file) {
        List<DetectedFormat> formats = new ArrayList<DetectedFormat>();

        if (!file.exists()) {
            return detectFormats(file.getName());
        }

        IdentificationResultCollection identificationResultCollection = droidIdentifier.detectFormat(file);
        for (IdentificationResult identificationResult : identificationResultCollection.getResults()){
            DetectedFormat format = new DetectedFormat();
            format.setId(identificationResult.getPuid());
            if(identificationResult.getName() != null){
               format.setName(identificationResult.getName());
            }
            if(identificationResult.getVersion() != null){
                format.setVersion(identificationResult.getVersion());
            }
            if(identificationResult.getMimeType() !=  null && !identificationResult.getMimeType().isEmpty()){
                //TODO: Original code had splitting of MIME type here, in a loop.  Do we still need this, or will it not ever happen?
                format.setMimeType(identificationResult.getMimeType());
            } else {
                format.setMimeType("application/octet-stream");
            }

            if(!formats.contains(format)){
                formats.add(format);
            }
        }

        return formats;
    }


    /**
     * Detect expected format types based on a filename only
     * @param filename The name of the file to detect
     * @return A list of formats that the file would have based on its name, which will be empty if there are none.
     */
    public List<DetectedFormat> detectFormats(String filename) {
        List<DetectedFormat> formats = getApplicableFormats(FilePathUtil.getLastFileExtension(filename));

        if (formats == null) return new ArrayList<DetectedFormat>();

        return formats;
    }


    /**
     * Returns a list of formats based on file extension
     * @param extension The extension to check.  If any leading periods are included, they will be stripped off
     * @return a list of formats based on file extension
     */
    public List<DetectedFormat> getApplicableFormats(String extension) {
        // Strip off leading .'s
        if (extension.startsWith(".")) {
            extension = extension.replaceFirst("[.]*","");
        }

        List<FileFormat> fileFormats = droidIdentifier.getFileFormatByExtension(extension);
        if (fileFormats != null) {
            List<DetectedFormat> applicableFormats = new ArrayList<>();
            DetectedFormat format;
            for (FileFormat fileFormat : fileFormats) {
                format = new DetectedFormat();
                format.setId(fileFormat.getPUID());
                if (fileFormat.getMimeType() == null || fileFormat.getMimeType().isEmpty()) {
                    format.setMimeType("application/octet-stream");
                } else {
                    format.setMimeType(fileFormat.getMimeType());
                }
                format.setPossibleExtensions(fileFormat.getExtensions());
                format.setName(fileFormat.getName());
                format.setVersion(fileFormat.getVersion());
                applicableFormats.add(format);
            }
            return applicableFormats;
        }
        return null;
    }
}
