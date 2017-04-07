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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SubstitutionRemediationTest {

    private SubstitutionRemediation underTest = new SubstitutionRemediation();

    @Test
    public void testSingleCharSingleOccurrenceUsingMatchingStrings() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the laiy dog";

        assertEquals(expected,
                underTest.remediateMatchingStrings(new StringBuilder(initial), 'i', "z").toString());
    }

    @Test
    public void testSingleCharSingleOccurrenceUsingPositions() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the laiy dog";

        assertEquals(expected,
                underTest.remediatePositions(new StringBuilder(initial), 'i', Stream.of(38)).toString());
    }

    @Test
    public void testSingleCharSingleOccurrenceUsingMatcher() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the laiy dog";

        assertEquals(expected,
                underTest.remediateMatchingCharacters(new StringBuilder(initial), 'i', Stream.of(new BaseMatcher<Character>() {
                    @Override
                    public boolean matches(Object o) {
                        return (char) o == 'z';
                    }

                    @Override
                    public void describeTo(Description description) {

                    }
                })).toString());
    }

    @Test
    public void testSingleCharMultipleOccurrenceUsingMatchingStrings() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "Thq quick brown fox jumpqd ovqr thq lazy dog";

        assertEquals(expected,
                underTest.remediateMatchingStrings(new StringBuilder(initial), 'q', "e").toString());
    }

    @Test
    public void testSingleCharMultipleOccurrenceUsingPositions() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "Thq quick brown fox jumpqd ovqr thq lazy dog";

        assertEquals(expected,
                underTest.remediatePositions(new StringBuilder(initial), 'q', Stream.of(2, 24, 29, 34)).toString());
    }

    @Test
    public void testSingleCharMultipleOccurrenceUsingMatcher() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "Thq quick brown fox jumpqd ovqr thq lazy dog";

        assertEquals(expected,
                underTest.remediateMatchingCharacters(new StringBuilder(initial), 'q', Stream.of(new BaseMatcher<Character>() {
                    @Override
                    public boolean matches(Object o) {
                        return (char) o == 'e';
                    }

                    @Override
                    public void describeTo(Description description) {

                    }
                })).toString());
    }

    @Test
    public void testMultipleCharactersSingleOccurrence() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the lazy xxx";

        assertEquals(expected,
                underTest.remediateMatchingStrings(new StringBuilder(initial), 'x', "dog").toString());
    }

    @Test
    public void testMultipleCharactersMultipleOccurrence() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over xxe lazy dog";

        assertEquals(expected,
                underTest.remediateMatchingStrings(new StringBuilder(initial), 'x', "th").toString());
    }

    @Test
    public void testMultipleCharactersMultipleOccurrenceMultipleStrings() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over xxe lazy xxx";

        assertEquals(expected,
                underTest.remediateMatchingStrings(new StringBuilder(initial), 'x', "th", "dog").toString());
    }

    @Test
    public void testRemediateMatchingStringRegex() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the lazy xxx";

        assertEquals(expected,
                underTest.remediateMatchingStrings(new StringBuilder(initial), 'x', "dog").toString());
    }

    @Test
    public void testRemediateMatchingStringRegexMultipleMatches() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brxwn fxx jumped xver the lazy dxg";

        assertEquals(expected,
                underTest.remediateMatchingStrings(new StringBuilder(initial), 'x', "o").toString());
    }

    @Test
    public void testRemediateMatchingStringsJavadocExamples() throws Exception {
        assertEquals("foo",
                underTest.remediateMatchingStrings(
                        new StringBuilder("foo"), 'X', "baz").toString());

        assertEquals("fXX",
                underTest.remediateMatchingStrings(
                        new StringBuilder("foo"), 'X', "o").toString());

        assertEquals("foo",
                underTest.remediateMatchingStrings(
                        new StringBuilder("foo"), 'X', "of").toString());

        assertEquals("XXo",
                underTest.remediateMatchingStrings(
                        new StringBuilder("foo"), 'X', "fo").toString());
    }

    @Test
    public void testRemediateEqualStringsJavadocExamples() throws Exception {
        assertEquals("foo",
                underTest.remediateEqualStrings(
                        new StringBuilder("foo"), 'X', "baz").toString());

        assertEquals("foo",
                underTest.remediateEqualStrings(
                        new StringBuilder("foo"), 'X', "o").toString());

        assertEquals("foo",
                underTest.remediateEqualStrings(
                        new StringBuilder("foo"), 'X', "of").toString());

        assertEquals("foo",
                underTest.remediateEqualStrings(
                        new StringBuilder("foo"), 'X', "fo").toString());

        assertEquals("XXX",
                underTest.remediateEqualStrings(
                        new StringBuilder("foo"), 'X', "foo").toString());
    }
}