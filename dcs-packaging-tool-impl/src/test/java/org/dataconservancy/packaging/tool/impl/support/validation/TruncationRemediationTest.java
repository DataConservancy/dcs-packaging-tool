/*
 * Copyright 2017 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl.support.validation;

import org.junit.Test;

import static org.dataconservancy.packaging.tool.impl.support.validation.TruncationRemediation.Strategy.LEADING_SUBSTITUTION;
import static org.dataconservancy.packaging.tool.impl.support.validation.TruncationRemediation.Strategy.TRAILING_SUBSTITUTION;
import static org.junit.Assert.assertEquals;

/**
 * Insures that the truncation remediation strategies work as expected.
 * <p>
 *
 * </p>
 */
public class TruncationRemediationTest {

    private TruncationRemediation underTest = new TruncationRemediation();

    @Test
    public void testLeadingSubstitutionPeriodAsReservedCharacter() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog.";
        final String expected = "X quick brown fox jumped over the lazy dog.";
        final int limit = 43;

        assertEquals(45, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testLeadingSubstitutionNoReservedCharacters() throws Exception {
        final String initial = "The quick brown fox jumped over the lazy dog";
        final String expected = "X quick brown fox jumped over the lazy dog";
        final int limit = 42;

        assertEquals(44, initial.length());
        assertEquals(limit, expected.length());


        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testTrailingSubstitutionPeriodAsReservedCharacter() throws Exception {
        final String initial = "The quick brown fox jumped over the lazy dog.";
        final String expected = "The quick brown fox jumped over the lazy X.";
        final int limit = 43;

        assertEquals(45, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, TRAILING_SUBSTITUTION));
    }

    @Test
    public void testTrailingSubstitutionNoReservedCharacters() throws Exception {
        final String initial = "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the lazy X";
        final int limit = 42;

        assertEquals(44, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, TRAILING_SUBSTITUTION));
    }

    @Test
    public void testLeadingSubstitutionWithReservedCharacterInIndex0() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);

        final String initial = "/The quick brown fox jumped over the lazy dog.";
        final String expected = "/X quick brown fox jumped over the lazy dog.";
        final int limit = 44;

        assertEquals(46, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testLeadingSubstitutionWithTwoReservedCharactersInIndex0() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);

        final String initial = "//The quick brown fox jumped over the lazy dog.";
        final String expected = "//X quick brown fox jumped over the lazy dog.";
        final int limit = 45;

        assertEquals(47, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testLeadingSubstitutionWithTwoConsecutiveReservedCharacters() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);

        final String initial = "The quick brown fox // jumped over the lazy dog.";
        final String expected = "X quick brown fox // jumped over the lazy dog.";
        final int limit = 46;

        assertEquals(48, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testLeadingSubstitutionWithFilePath() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);

        final String initial = "/path/to/a/file.txt";
        final String expected = "/X/X/a/file.txt";
        final int limit = 15;

        assertEquals(19, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testLeadingSubstitutionWithFilePathTwo() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);

        final String initial = "/path/to/another/file.txt";
        final String expected = "/X/X/X/file.txt";
        final int limit = 15;

        assertEquals(25, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testTrailingSubstitution() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);

        final String initial = "/path/to/a/directory/";
        final String expected = "/path/to/a/dirX/";
        final int limit = 16;

        assertEquals(21, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, TRAILING_SUBSTITUTION));
    }

    @Test
    public void testTrailingSubstitutionWithFilePath() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);

        final String initial = "/path/to/a/file.txt";
        final String expected = "/path/to/a/fX.X";
        final int limit = 15;

        assertEquals(19, initial.length());
        assertEquals(limit, expected.length());

        String actual = underTest.remediate(new StringBuilder(initial), limit, TRAILING_SUBSTITUTION);
        assertEquals(expected, actual);
    }

    @Test
    public void testLeadingSubstitutionWithFullTruncation() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);
        assertEquals('.', TruncationRemediation.RESERVED_CHARS[1]);

        final String initial = "/path/to/a/file.txt";
        final String expected = "/X/X/X/X.X";
        final int limit = 10;

        assertEquals(19, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test
    public void testTruncateAlreadyTruncatedLeading() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);
        assertEquals('.', TruncationRemediation.RESERVED_CHARS[1]);

        final String initial = "/X/X/X/X.X";
        final String expected = "/X/X/X/X.X";
        final int limit = 10;

        assertEquals(10, initial.length());
        assertEquals(limit, expected.length());

        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION));
    }

    @Test(expected = RuntimeException.class)
    public void testTruncateAlreadyTruncatedLeadingUnableToMeetLimit() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);
        assertEquals('.', TruncationRemediation.RESERVED_CHARS[1]);

        final String initial = "/X/X/X/X.X";
        final int limit = 9;

        assertEquals(10, initial.length());

        String result = underTest.remediate(new StringBuilder(initial), limit, LEADING_SUBSTITUTION);
        System.err.println(result);
    }

    @Test(expected = RuntimeException.class)
    public void testTruncateAlreadyTruncatedTrailingUnableToMeetLimit() throws Exception {
        assertEquals('/', TruncationRemediation.RESERVED_CHARS[0]);
        assertEquals('.', TruncationRemediation.RESERVED_CHARS[1]);

        final String initial = "/X/X/X/X.X";
        final int limit = 9;

        assertEquals(10, initial.length());

        String result = underTest.remediate(new StringBuilder(initial), limit, TRAILING_SUBSTITUTION);
        System.err.println(result);
    }

//
//    @Test
//    @Ignore("TODO")
//    public void testPreserve() throws Exception {
//        assertEquals(METADATA.getRelativePackageLocation(), TruncationRemediation.preserve(
//                new StringBuilder(join("/", METADATA.getRelativePackageLocation(), "path", "to", "metadata")),
//                TruncationRemediation.RESERVED_PATHS).toString());
//    }

    //    @Test
//    public void testLeadingDeletion() throws Exception {
//        final String initial =  "The quick brown fox jumped over the lazy dog.";
//        final String expected = " quick brown fox jumped over the lazy dog.";
//        final int limit = 42;
//
//        assertEquals(45, initial.length());
//        assertEquals(limit, expected.length());
//
//        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, LEADING_DELETION));
//    }
//    @Test
//    public void testTrailingSubstitution() throws Exception {
//        final String initial =  "The quick brown fox jumped over the lazy dog.";
//        final String expected = "The quick brown fox jumped over the lazy X";
//        final int limit = 42;
//
//        assertEquals(45, initial.length());
//        assertEquals(limit, expected.length());
//
//        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, TRAILING_SUBSTITUTION));
//    }
//
//    @Test
//    public void testTrailingDeletion() throws Exception {
//        final String initial =  "The quick brown fox jumped over the lazy dog.";
//        final String expected = "The quick brown fox jumped over the lazy ";
//        final int limit = 41;
//
//        assertEquals(45, initial.length());
//        assertEquals(limit, expected.length());
//
//        assertEquals(expected, underTest.remediate(new StringBuilder(initial), limit, TRAILING_DELETION));
//    }

    private void assertNoReservedChars(String str) {

    }
}