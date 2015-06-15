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
import org.dataconservancy.dcs.util.droid.BaseFileSetUpTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for Content Detection Service
 */
public class ContentDetectionServiceTest extends BaseFileSetUpTest {

    private static ContentDetectionService underTest;

    @BeforeClass
    public static void setUpStaticObjects() throws URISyntaxException {
        underTest = ContentDetectionService.getInstance();
    }

    /**
     * Test that given a file with no extension on its name, format and mimetype can still be detected
     */
    @Test
    public void testDetectMimeTypeFromNameWithNoExtension() {
        List<DetectedFormat> formats = underTest.detectFormats(JPG_FILE_NO_EXTENSION);
        assertFalse(formats.size() == 0);
        List<String> mimetypes = new ArrayList<>();
        List<String> pronomTypes = new ArrayList<>();
        for (DetectedFormat format : formats) {
            mimetypes.add(format.getMimeType());
            pronomTypes.add(format.getId());
        }
        assertTrue(mimetypes.contains(JPG_MIMETYPE));
        assertTrue(pronomTypes.contains("fmt/41"));

    }
    /**
     * Test that given a file object service can detect its mime type
     */
    @Test
    public void testDetectFileMimeTypeByFileObject() {
        List<DetectedFormat> formats = underTest.detectFormats(PNG_FILE);
        assertFalse(formats.size() == 0);
        List<String> mimetypes = new ArrayList<>();
        for (DetectedFormat format : formats) {
            mimetypes.add(format.getMimeType());
        }
        assertTrue(mimetypes.contains(PNG_MIMETYPE));

        formats = underTest.detectFormats(WAV_FILE);
        assertFalse(formats.size() == 0);
        mimetypes = new ArrayList<>();
        for (DetectedFormat format : formats) {
            mimetypes.add(format.getMimeType());
        }
        assertTrue(mimetypes.contains(WAV_MIMETYPE));

        formats = underTest.detectFormats(TEXT_FILE);
        assertFalse(formats.size() == 0);
        mimetypes = new ArrayList<>();
        for (DetectedFormat format : formats) {
            mimetypes.add(format.getMimeType());
        }
        assertTrue(mimetypes.contains(TEXT_MIMETYPE));

        formats = underTest.detectFormats(JPG_FILE);
        assertFalse(formats.size() == 0);
        mimetypes = new ArrayList<>();
        for (DetectedFormat format : formats) {
            mimetypes.add(format.getMimeType());
        }
        assertTrue(mimetypes.contains(JPG_MIMETYPE));

        formats = underTest.detectFormats(ZIP_FILE);
        assertFalse(formats.size() == 0);
        mimetypes = new ArrayList<>();
        for (DetectedFormat format : formats) {
            mimetypes.add(format.getMimeType());
        }
        assertTrue(mimetypes.contains(ZIP_MIMETYPE));
    }

    /**
     * Test that given a valiD file extension, getApplicableFormats would return formats applicable to that extension.
     * Given an invalid format, null would be returned.
     */
    @Test
    public void testGetApplicableFormatsForExtension() {
        List<DetectedFormat> detectedFormats = underTest.getApplicableFormats("doc");
        assertNotNull(detectedFormats);

        detectedFormats = underTest.getApplicableFormats("henry");
        assertNull(detectedFormats);
    }

    /**
     * Test that getDetectorName return "DROID"
     */
    @Test
    public void testGetDetectorName() {
        assertNotNull(underTest.getDetectorName());
        assertEquals("DROID", underTest.getDetectorName());
    }

    /**
     * Tests that getDetectorVersion return the appropriate version of droid
     */
    @Test
    public void testGetDetectorVersion() {
        assertNotNull(underTest.getDetectorVersion());
        assertEquals("6.1.5", underTest.getDetectorVersion());
    }

    /**
     * Test to ensure that a file with unknown format returns expected "application/octet-stream" type
     */
    @Test
    public void testDetectUnknownFormat() throws URISyntaxException {
        List<DetectedFormat> detectedFormats = underTest.detectFormats(MALFORMED_XML_FILE);
        assertEquals(1, detectedFormats.size());
        assertEquals("application/octet-stream", detectedFormats.get(0).getMimeType());
    }


    /**
     * Tests that given an empty File object, format would be detected base on file extension
     */
    @Test
    public void testNonExistentFileWillDetectBasedOnExtension() {
        List<DetectedFormat> detectedFormats = underTest.detectFormats(new File("fakefile.txt"));
        assertEquals(1, detectedFormats.size());
        assertEquals(TEXT_MIMETYPE, detectedFormats.get(0).getMimeType());
    }

    /**
     * Tests that an empty File object with bogus filename extensions would be given "application/octet-stream" mimetype
     *
     */
    @Test
    public void testNonExistentFileWithUnknownExtensionReturnsOctetStream() {
        List<DetectedFormat> detectedFormats = underTest.detectFormats(new File("fakefile.xyzfake"));
        assertEquals(2, detectedFormats.size());
        assertEquals("application/octet-stream", detectedFormats.get(0).getMimeType());
    }

    /**
     * Tests that given a file name with valid extension, detectFormats() return a format base on the extension
     */
    @Test
    public void testNonExistentFileByNameWillDetectBasedOnExtension() {
        List<DetectedFormat> detectedFormats = underTest.detectFormats("fakefile.txt");
        assertEquals(1, detectedFormats.size());
        assertEquals(TEXT_MIMETYPE, detectedFormats.get(0).getMimeType());
    }


    /**
     * Tests that given a file name with invalid extension, format returned is "application/octet-stream"
     */
    @Test
    public void testNonExistentFileByNameWithUnknownExtensionReturnsOctetStream() {
        List<DetectedFormat> detectedFormats = underTest.detectFormats("fakefile.xyzfake");
        assertEquals(2, detectedFormats.size());
        assertEquals("application/octet-stream", detectedFormats.get(0).getMimeType());
    }


    /**
     * Tests that given a file name with extension, detectFormats() method returns correct format based on the file's
     * extension
     */
    @Test
    public void testNonExistentTarGzFile() {
        List<DetectedFormat> detectedFormats = underTest.detectFormats("fakefile.tar.gz");
        assertEquals(1, detectedFormats.size());
        assertEquals(XGZIP_MIMETYPE, detectedFormats.get(0).getMimeType());
    }


    /**
     * This test insures that the Droid version information contained in the droid-version.properties file is
     * up-to-date with the current DROID implementation.  The version and name of the current DROID implementation is
     * obtained from a resource bundle in the droid-command-line module.
     *
     * We maintain a separate droid-version.properties file to avoid the hassle of a runtime dependency on
     * droid-command-line and its resource bundle.
     *
     * @throws Exception
     */
    @Test
    public void testPropertiesAndResourceBundleInSync() throws Exception {
        ContentDetectionService dcdsi = ContentDetectionService.getInstance();
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ResourceBundle bundle = ResourceBundle.getBundle("options", Locale.US, cl);
        String versionString = bundle.getString("version_no");
        assertEquals(underTest.getDetectorVersion(), versionString);
    }

}
