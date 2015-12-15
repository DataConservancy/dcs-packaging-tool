/*
 * Copyright 2015 Johns Hopkins University
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

import org.dataconservancy.dcs.model.DetectedFormat;
import org.dataconservancy.dcs.util.droid.DroidDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Detects file format base on file's content and/or filename extension.
 * Backed by DROID (Digital Record Object Identification) library.
 */
public class ContentDetectionService {

    public final static String MIME_TYPE_SCHEME_URI = "http://www.iana.org/assignments/media-types/";
    public final static String PRONOM_SCHEME_URI = "http://www.nationalarchives.gov.uk/PRONOM/";

    private final static String DROID_PROPERTIES_RESOURCE = "/droid-version.properties";

    private final static String DROID_NAME = "dcs.contentdetection.impl.droid.name";

    private final static String DROID_VERSION = "dcs.contentdetection.impl.droid.version";

    private DroidDriver droidDriver = new DroidDriver();

    private static final ContentDetectionService contentDetectionService = new ContentDetectionService();


    /**
     * Private constructor to make it a singleton
     */
    private ContentDetectionService() {
    }


    /**
     * Returns an instance of contentDetectionService.
     * @return an instance of ContentDetectionService
     */
    public static ContentDetectionService getInstance() {
        return contentDetectionService;
    }


    /**
     * Detect bytestream format of the provided file, using UK National Archives profiling tool DROID.
     * <p>
     * The method returns:
     * <ul>
     *     <li> List of size 1 if format was detected unambiguously </li>
     *     <li> List of multiple format if more than one format were possible for the provided file</li>
     * </ul>
     * <p>
     * If <b>file</b> does not exist, the returned value will be based on the name of the file only, as if
     * {@link #detectFormats(String)} was called.
     * @param file - whose formats are to be detected
     * @return {@link java.util.List} of {@link org.dataconservancy.dcs.model.DetectedFormat}s for the provided file.
     */
    public List<DetectedFormat> detectFormats(File file) {

        if (!file.exists()) {
            return detectFormats(file.getName());
        }

        List<DetectedFormat> detectedFormats =  droidDriver.detectFormats(file);
        //according to the contract, must return this format if none is detected
        if(detectedFormats.size() == 0){
            String unknownType = "application/octet-stream";
            DetectedFormat unknownFormat = new DetectedFormat();
            unknownFormat.setMimeType(unknownType);
            unknownFormat.setName("Unknown");
            detectedFormats.add(unknownFormat);
        }

        return detectedFormats;
    }


    /**
     * Detect format of provided file based on name, using UK National Archives profiling tool DROID
     * @param filename The name of the file to determine format for
     * @return A {@link java.util.List} of {@link org.dataconservancy.dcs.model.DetectedFormat}s found that match the file name.
     * Note that if no other formats detected, it will default to a pair of "no-extension" formats provided by DROID
     */
    public List<DetectedFormat> detectFormats(String filename) {
        return droidDriver.detectFormats(filename);
    }

    /**
     * Looks up in DROID signature file the file formats that correspond with the provided extension.
     * @param extension the file extension
     * @return {@link java.util.List} of possible formats for the given file extension, or null if no formats are found.
     */
    public List<DetectedFormat> getApplicableFormats(String extension) {
        return droidDriver.getApplicableFormats(extension);
    }

    /**
     *
     * Returns name of the tool used to detect file format
     * @return the name of the detector tool
     */
    public String getDetectorName() {
        return loadDroidProperties().getProperty(DROID_NAME);
    }

    /**
     *
     * Returns version of the tool used to detect file format
     * @return the version of the detector tool
     */
    public String getDetectorVersion() {
        return loadDroidProperties().getProperty(DROID_VERSION);
    }

    private Properties loadDroidProperties() {
        URL droidPropertiesResource = ContentDetectionService.class.getResource(DROID_PROPERTIES_RESOURCE);
        if (droidPropertiesResource == null) {
            throw new RuntimeException("Unable to locate " + DROID_PROPERTIES_RESOURCE + " on the classpath!");
        }

        Properties droidProps = new Properties();
        try {
            droidProps.load(droidPropertiesResource.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load DROID properties located at " +
                    droidPropertiesResource.toString() + ": " + e.getMessage(), e);
        }

        return droidProps;
    }
}