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
package org.dataconservancy.dcs.model;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DetectedFormatTest {
    private DetectedFormat duplicatedFormat = new DetectedFormat();
    private DetectedFormat formatOne = new DetectedFormat();
    private DetectedFormat formatTwo = new DetectedFormat();
    private DetectedFormat formatThree = new DetectedFormat();

    @Before
    public void setUp() {

        formatOne.setId("fmt/11");
        formatOne.setName("FormatOne");
        formatOne.setVersion("version1");
        formatOne.setMimeType("application/one-format");
        List<String> possibleExtensions = new ArrayList<>();
        possibleExtensions.add("tar");
        possibleExtensions.add("gz");
        formatOne.setPossibleExtensions(possibleExtensions);

        formatTwo.setId("fmt/22");
        formatTwo.setName("FormatTwo");
        formatTwo.setVersion("version2");
        formatTwo.setMimeType("application/two-format");
        possibleExtensions = new ArrayList<>();
        possibleExtensions.add("jpeg");
        formatTwo.setPossibleExtensions(possibleExtensions);

        formatThree.setId("fmt/11");
        formatThree.setName("FormatOne");
        formatThree.setVersion("version1");
        formatThree.setMimeType("application/one-format");
        possibleExtensions = new ArrayList<>();
        possibleExtensions.add("tar");
        possibleExtensions.add("gz");
        formatThree.setPossibleExtensions(possibleExtensions);

        duplicatedFormat.setId(formatOne.getId());
        duplicatedFormat.setName(formatOne.getName());
        duplicatedFormat.setVersion(formatOne.getVersion());
        duplicatedFormat.setMimeType(formatOne.getMimeType());
        duplicatedFormat.setPossibleExtensions(formatOne.getPossibleExtensions());
    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(formatOne.equals(formatOne));
        assertFalse(formatOne.equals(formatTwo));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(formatOne.equals(duplicatedFormat));
        assertTrue(duplicatedFormat.equals(formatOne));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(formatOne.equals(duplicatedFormat));
        assertTrue(duplicatedFormat.equals(formatThree));
        assertTrue(formatOne.equals(formatThree));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(formatOne.equals(duplicatedFormat));
        assertTrue(formatOne.equals(duplicatedFormat));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(formatOne.equals(null));
    }
}
