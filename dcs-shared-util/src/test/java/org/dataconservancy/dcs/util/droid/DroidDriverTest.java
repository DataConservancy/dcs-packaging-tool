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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests DetectedFormats derived from DROID content detection process
 */
public class DroidDriverTest extends BaseFileSetUpTest {

    private static final DroidDriver dd = new DroidDriver();

    /**
     * Test to ensure that PNG format detection attempt returns expected DetectedFormat output
     * @throws java.net.URISyntaxException
     */
//    @Ignore
    @Test
    public void testDetectPNGFormat() throws URISyntaxException {
        File file = PNG_FILE;
        List<DetectedFormat> formats = dd.detectFormats(file);
        assertTrue(formats.size() > 0);
        for (DetectedFormat format : formats) {
            assertEquals(PNG_MIMETYPE, format.getMimeType());
        }
    }

    /**
     * Test to ensure that text format detection attempt returns expected DetectedFormat output
     * @throws java.net.URISyntaxException
     */
//    @Ignore
    @Test
    public void testDetectTextFormat() throws URISyntaxException {
        File file = TEXT_FILE;
        List<DetectedFormat> formats = dd.detectFormats(file);
        assertTrue(formats.size() > 0);
        for (DetectedFormat format : formats) {
            assertEquals(TEXT_MIMETYPE, format.getMimeType());
        }
    }

    /**
     * Test to ensure that JPG format detection attempt returns expected DcsFormat output
     * @throws java.net.URISyntaxException
     */
 //   @Ignore
    @Test
    public void testDetectJPGFormat() throws URISyntaxException {
        File file = JPG_FILE;
        List<DetectedFormat> formats = dd.detectFormats(file);
        assertTrue(formats.size() > 0);
        for (DetectedFormat format : formats) {
            assertEquals(JPG_MIMETYPE, format.getMimeType());
        }
    }


    @Test
    public void testPngFileWithTxtExtension() {
        File file = PNG_TXT_FILE;
        List<DetectedFormat> formats = dd.detectFormats(file);
        assertTrue(formats.size() > 0);
        for (DetectedFormat format : formats) {
            assertEquals(PNG_MIMETYPE, format.getMimeType());
        }
    }

    /**
     * Test to ensure that a file with unknown format returns no results from DROID
     *
     */
    @Test
    public void testDetectUnknownFormat() throws URISyntaxException{
        File file = MALFORMED_XML_FILE;
        List<DetectedFormat> formats = dd.detectFormats(file);
        assertEquals(0, formats.size());
    }


    /**
     * Test to ensure that both MIME and PRONOM format information for a format are in the same object
     */
    @Test
    public void testMimeAndPronomInSameFormatObject() {
        File file = TAR_GZ_FILE;
        List<DetectedFormat> formats = dd.detectFormats(TAR_GZ_FILE);
        assertEquals(1, formats.size());
        DetectedFormat f = formats.get(0);

        assertTrue(f.getId().contains("x-fmt/265"));
        assertTrue(f.getMimeType().contains("tar"));
        assertTrue(f.getName().contains("Tape Archive"));
    }


    /**
     * Test to ensure that if the file doesn't exist, it will still try to detect based on extension
     */
    @Test
    public void testNonexistentTextFile() {
        File file = new File("fakefile.txt");
        List<DetectedFormat> formats = dd.detectFormats(file);

        assertEquals(1, formats.size());
        assertEquals(TEXT_MIMETYPE, formats.get(0).getMimeType());
    }


    @Test
    public void testNonexistentTextFileByName() {
        List<DetectedFormat> formats = dd.detectFormats("fakefile.txt");

        assertEquals(1, formats.size());
        assertEquals(TEXT_MIMETYPE, formats.get(0).getMimeType());
    }
}
