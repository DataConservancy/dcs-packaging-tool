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

import junit.framework.Assert;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for DroidIdentifier class
 */
public class DroidIdentifierTest extends BaseFileSetUpTest {

    /**
     * Test to ensure that PNG format detection attempt returns expected PRONOM output
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testDetectPNGFormat() throws URISyntaxException {
        File file = PNG_FILE;
        DroidIdentifier droidIdentifier = new DroidIdentifier();
        IdentificationResultCollection irc = droidIdentifier.detectFormat(file);
        Assert.assertTrue(0 < irc.getResults().size());
        IdentificationResult result = irc.getResults().get(0);

        Assert.assertTrue("image/png".equals(result.getMimeType()));
        Assert.assertTrue("fmt/11".equals(result.getPuid()));
        Assert.assertTrue("Portable Network Graphics".equals(result.getName()));
    }

    @Test
    public void testGetFormatsForExtension() throws Exception {
        DroidIdentifier droidIdentifier = new DroidIdentifier();

        List<FileFormat> formatList = droidIdentifier.getFileFormatByExtension("tar");
        assertNotNull(formatList);
        assertEquals(1, formatList.size());
        assertEquals("application/x-tar",formatList.get(0).getMimeType());


        formatList = droidIdentifier.getFileFormatByExtension("jpg");
        assertNotNull(formatList);
        assertTrue(formatList.get(0).getMimeType().equals("image/jpeg") ||
                    formatList.get(0).getMimeType().equals("image/jpg"));
    }

    @Test
    public void testGetFormatForExtension_BadExtensions() throws Exception {
        DroidIdentifier droidIdentifier = new DroidIdentifier();

        List<FileFormat> formatList = droidIdentifier.getFileFormatByExtension("alfee");
        assertNull(formatList);

        formatList = droidIdentifier.getFileFormatByExtension("willard");
        assertNull(formatList);
    }
}
