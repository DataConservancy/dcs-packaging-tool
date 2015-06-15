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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper class for loading the latest signature file and container file from nationalarchives.gov.uk. Note that currently we only fetch the latest signature file.
 * Container files we retrieve the latest known version.
 */
public class DroidSignatureFileManager {

    private static final String SIGNATURE_FILE_BASE_URL = "http://www.nationalarchives.gov.uk/documents/";
    private static final String SIGNATURE_FILE_BASE_NAME = "DROID_SignatureFile_V";
    private static final String CONTAINER_FILE_BASE_URL = "http://www.nationalarchives.gov.uk/documents/";
    private static final String CONTAINER_FILE_BASE_NAME = "container-signature-";

    private static final String DROID_PREFERENCES_NAME = "droid_preferences";
    private static final String DROID_SIGNATURE_FILE_VERSION_PREFERENCE = "signature_version";
    private static final String DROID_CONTAINER_FILE_VERSION_PREFERENCE = "container_version";
    private static final String DROID_LAST_FILE_CHECK = "last_file_check";

    private final static String DEFAULT_SIGNATURE_FILE = "/SignatureFiles/DROID_SignatureFile_V68.xml";
    private final static String DEFAULT_CONTAINER_FILE = "/SignatureFiles/container-signature.xml";

    private static final int LATEST_KNOWN_SIGNATURE_VERSION = 77;
    private static final String LATEST_KNOWN_CONTAINER_VERSION = "20140717";
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Gets the latest signature file available by droid. Checks once a month to see if there is a new file, otherwise returns the currently saved file.
     *
     * @return A file object pointing to the latest droid signature file available.
     */
    public File getLatestSignatureFile() {

        //The code below to fetch the latest file is broken, so for now just read from the class path.
        try {
            File signatureFile = File.createTempFile("DROID_SignatureFile_V68", ".xml");
	    signatureFile.deleteOnExit();
            getClasspathSignatureFile(signatureFile);
            return signatureFile;
        } catch (IOException e) {
            log.error("Unable to create temp file for classpath signature file exception: " + e.getMessage());
        }

        return null;
        /*
        Preferences preferences = Preferences.userRoot().node(DROID_PREFERENCES_NAME);
        int lastVersion = preferences.getInt(DROID_SIGNATURE_FILE_VERSION_PREFERENCE, -1);

        File directory = getFileDirectory();
        File newSignatureFile = null;

        boolean newFileRetrieved = false;
        if (directory != null) {
            //If we don't have a last version get the last known version
            if (lastVersion == -1) {
                newSignatureFile = new File(directory, SIGNATURE_FILE_BASE_NAME + LATEST_KNOWN_SIGNATURE_VERSION + ".xml");
                newFileRetrieved = retrieveLatestSignatureFile(LATEST_KNOWN_SIGNATURE_VERSION, newSignatureFile);
                //Store the preference of what version we retrieved.
                if (newFileRetrieved) {
                    preferences.putInt(DROID_SIGNATURE_FILE_VERSION_PREFERENCE, LATEST_KNOWN_SIGNATURE_VERSION);
                }
            } else {
                //Try to get the current file;
                long lastCheck = preferences.getLong(DROID_LAST_FILE_CHECK, 0l);

                //If it's time to check or if the current file doesn't exist
                if (System.currentTimeMillis() - lastCheck > 2592000000l) {
                    //Droid skips versions if one isn't released so check for the next 3 version
                    int nextVersion = lastVersion + 1;
                    while (!newFileRetrieved && nextVersion < lastVersion + 4) {
                        newSignatureFile = new File(directory, SIGNATURE_FILE_BASE_NAME + nextVersion + ".xml");
                        newFileRetrieved = retrieveLatestSignatureFile(nextVersion, newSignatureFile);
                        if (newFileRetrieved) {
                            preferences.putInt(DROID_SIGNATURE_FILE_VERSION_PREFERENCE, lastVersion);
                        } else {
                            nextVersion++;
                        }
                    }

                    //Even if no file is retrieved update the date to check again in a month
                    preferences.putLong(DROID_LAST_FILE_CHECK, System.currentTimeMillis());

                }
            }
        } else {
            //If we can't create a file in the user directory just get the latest known and store it in temp.
            try {
                newSignatureFile = File.createTempFile(SIGNATURE_FILE_BASE_NAME + LATEST_KNOWN_SIGNATURE_VERSION, ".xml");
                newFileRetrieved = retrieveLatestSignatureFile(LATEST_KNOWN_SIGNATURE_VERSION, newSignatureFile);
            } catch (IOException e) {
                log.error("Unable to create temp file for signature file exception: " + e.getMessage());
            }
        }

        File signatureFile = null;
        if (newFileRetrieved) {
            signatureFile = newSignatureFile;
        } else {
            //Try the currently stored file.
            File currentSignatureFile = new File(directory, SIGNATURE_FILE_BASE_NAME + lastVersion + ".xml");
            if (currentSignatureFile.exists()) {
                signatureFile = currentSignatureFile;
                newFileRetrieved = true;
            } else {
                //Try to download the old file.
                signatureFile = new File(directory, SIGNATURE_FILE_BASE_NAME + lastVersion + ".xml");
                newFileRetrieved = retrieveLatestSignatureFile(lastVersion, newSignatureFile);
            }

            if (!newFileRetrieved) {
                //As a last resort try to get the signature file from the classpath.
                log.warn("Unable to download signature file attempting to read file from classpath.");
                try {
                    signatureFile = File.createTempFile("DROID_SignatureFile_V68", ".xml");
                    getClasspathSignatureFile(signatureFile);
                } catch (IOException e) {
                    log.error("Unable to create temp file for classpath signature file exception: " + e.getMessage());
                }
            }
        }
        */
    }

    /**
     * Gets the latest droid container file
     *
     * @return A file pointing to the latest droid container file.
     */
    public File getLatestContainerFile() {

        //The code below to get latest version doesn't work so just get file from classpath.
        try {
           File containerFile = File.createTempFile("container-signature", ".xml");
           getClasspathContainerFile(containerFile);
	   containerFile.deleteOnExit();
           return containerFile;
        } catch (IOException e) {
           log.error("Unable to create temp file for classpath signature file exception: " + e.getMessage());
        }

        return null;
       /*
        Preferences preferences = Preferences.userRoot().node(DROID_PREFERENCES_NAME);
        int lastVersion = preferences.getInt(DROID_CONTAINER_FILE_VERSION_PREFERENCE, -1);

        File directory = getFileDirectory();
        File newContainerFile = null;

        boolean newFileRetrieved = false;
        if (directory != null) {
            //If we don't have a last version get the last known version
            if (lastVersion == -1) {
                newContainerFile = new File(directory, CONTAINER_FILE_BASE_NAME + LATEST_KNOWN_CONTAINER_VERSION + ".xml");
                newFileRetrieved = retrieveLatestContainerFile(LATEST_KNOWN_CONTAINER_VERSION, newContainerFile);
                preferences.put(DROID_CONTAINER_FILE_VERSION_PREFERENCE, LATEST_KNOWN_CONTAINER_VERSION);
            } else {
                //TODO: Find out how to check for latest version
                //Grab the currently stored file.
                File currentSignatureFile = new File(directory, CONTAINER_FILE_BASE_NAME + lastVersion + ".xml");
                if (currentSignatureFile.exists()) {
                    newContainerFile = currentSignatureFile;
                    newFileRetrieved = true;
                } else {
                    //Try to download the old file.
                    newContainerFile = new File(directory, CONTAINER_FILE_BASE_NAME + lastVersion + ".xml");
                    newFileRetrieved = retrieveLatestSignatureFile(lastVersion, newContainerFile);
                }
            }
        } else {
            try {
                newContainerFile = File.createTempFile(CONTAINER_FILE_BASE_NAME + LATEST_KNOWN_CONTAINER_VERSION, ".xml");
                newFileRetrieved = retrieveLatestContainerFile(LATEST_KNOWN_CONTAINER_VERSION, newContainerFile);
            } catch (Exception e) {
                log.error("Error creating container temp file " + CONTAINER_FILE_BASE_NAME + LATEST_KNOWN_CONTAINER_VERSION + " exception: " + e.getMessage());
            }
        }

        File containerFile = null;
        if (newFileRetrieved) {
            containerFile = newContainerFile;
        } else {
            //As a last resort try to get the signature file from the classpath.
            log.warn("Unable to download container file attempting to read file from classpath.");

            try {
                containerFile = File.createTempFile("container-signature", ".xml");
                getClasspathContainerFile(containerFile);
            } catch (IOException e) {
                log.error("Unable to create temp file for classpath signature file exception: " + e.getMessage());
            }
        }

        return containerFile;
        */
    }

    /**
     * Gets the file directory that should be used for storing the signature files, or null if the directory could not be created.
     *
     * @return The directory where the signature files should be stored or null if the directory couldn't be created.
     */
    private File getFileDirectory() {
        String filePath = System.getProperty("user.home") + "/droid/SignatureFiles/";
        File fileDirectory = new File(filePath);
        if (!fileDirectory.exists()) {
            if (!fileDirectory.mkdirs()) {
                log.error("Error creating droid signature file directory, defaulting to tmp directory.");
                fileDirectory = null;
            }
        }

        return fileDirectory;
    }

    /**
     * Checks if the url of the of the file exists and then downloads it to the provided file if it does.
     * @param fileUrl The url of the file to retrieve.
     * @param signatureFile The file object that should be used to store the contents of the file at the url.
     * @return True if the url was found and the contents were able to be stored in the file, false otherwise.
     */
    private boolean downloadLatestFile(URL fileUrl, File signatureFile) {
        boolean fileRetrieved = true;
        log.info("Attempting to download droid file: " + fileUrl);
        String contentType = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.connect();
            contentType = connection.getHeaderField("Content-Type");
        } catch (IOException e) {
            fileRetrieved = false;
            log.error("Error connection to file url: " + fileUrl + " Exception: " + e.getMessage());
        }

        if (fileRetrieved) {
            //National Archives website returns 200 even if the url doesn't exist, so check to make sure the content type is xml and not html
            if (contentType.equalsIgnoreCase("text/xml")) {
                try {
                    FileUtils.copyURLToFile(fileUrl, signatureFile);
                } catch (IOException e) {
                    fileRetrieved = false;
                    log.error("Error connection to file url: " + fileUrl + " Exception: " + e.getMessage());
                }
                log.info("Successfully downloaded droid file: " + fileUrl);
            } else {
                fileRetrieved = false;
            }
        }
        return fileRetrieved;
    }

    /**
     * Downloads the latest signature file and stores in the given directory.
     *
     * @param version          The version of the new signature file to retrieve.
     * @param newSignatureFile The file to store the new signature file in.
     * @return True if the new file was able to be downloaded false otherwise.
     */
    private boolean retrieveLatestSignatureFile(int version, File newSignatureFile) {
        boolean newFileRetrieved = false;
        URL signatureFileURL = null;
        boolean urlBuilt = true;
        try {
            signatureFileURL = new URL(SIGNATURE_FILE_BASE_URL + SIGNATURE_FILE_BASE_NAME + version + ".xml");
        } catch (MalformedURLException e) {
            urlBuilt = false;
        }
        if (urlBuilt) {
            newFileRetrieved = downloadLatestFile(signatureFileURL, newSignatureFile);
        }

        return newFileRetrieved;
    }

    /**
     * Downloads the latest container file and stores in the given directory.
     *
     * @param version          The version of the new container file to retrieve.
     * @param newContainerFile The file to store the new container file in.
     * @return True if the new file was able to be downloaded false otherwise.
     */
    private boolean retrieveLatestContainerFile(String version, File newContainerFile) {
        boolean newFileRetrieved = false;
        URL containerFileURL = null;
        boolean urlBuilt = true;
        try {
            containerFileURL = new URL(CONTAINER_FILE_BASE_URL + CONTAINER_FILE_BASE_NAME + version + ".xml");
        } catch (MalformedURLException e) {
            urlBuilt = false;
        }
        if (urlBuilt) {
            newFileRetrieved = downloadLatestFile(containerFileURL, newContainerFile);
        }

        return newFileRetrieved;
    }

    /**
     * Method to get the signature file from the classpath and copy it to a file so it can be read by the droid library.
     * @param signatureFile The file to save the classpath file to.
     * @return True if the file was found and able to be copied false otherwise.
     */
    private boolean getClasspathSignatureFile(File signatureFile) {
        boolean fileCopied = true;
        InputStream signatureFileStream = DroidSignatureFileManager.class.getResourceAsStream(DEFAULT_SIGNATURE_FILE);
        try {
            FileOutputStream signatureFileOutStream = new FileOutputStream(signatureFile);
            IOUtils.copy(signatureFileStream, signatureFileOutStream);
        } catch (IOException e) {
            log.error("Error getting classpath signature file: " + DEFAULT_SIGNATURE_FILE + " exception: " + e.getMessage());
            fileCopied = false;
        }

        return fileCopied;
    }

    /**
     * Method to get the container file from the classpath and copy it to a file so it can be read by the droid library.
     * @param containerFile The file to save the classpath file to.
     * @return True if the file was found and able to be copied false otherwise.
     */
    private boolean getClasspathContainerFile(File containerFile) {
        boolean fileCopied = true;
        InputStream signatureFileStream = DroidSignatureFileManager.class.getResourceAsStream(DEFAULT_CONTAINER_FILE);
        try {
            FileOutputStream signatureFileOutStream = new FileOutputStream(containerFile);
            IOUtils.copy(signatureFileStream, signatureFileOutStream);
        } catch (IOException e) {
            log.error("Error getting classpath signature file: " + DEFAULT_SIGNATURE_FILE + " exception: " + e.getMessage());
            fileCopied = false;
        }

        return fileCopied;
    }
}
