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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.dataconservancy.packaging.tool.impl.support.validation.RemediationUtils.delete;
import static org.dataconservancy.packaging.tool.impl.support.validation.RemediationUtils.replace;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class RemediationUtilsTest {

    /**
     * "héllö wörld!"
     * <p>
     * Invalid indexes are 1, 4, 7
     * </p>
     * <p>
     * Contains illegal characters per
     * <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">version 1.0 of
     * the DC BagIt profile</a>
     */
    private static final String ILLEGAL_CHARS = "h\u00e9ll\u00F6 w\u00f6rld!";

    @Test
    public void testSingleCharSingleOccurrence() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the laiy dog";

        assertEquals(expected,
                replace(new StringBuilder(initial), 'i', "z").toString());

    }

    @Test
    public void testSingleCharMultipleOccurrence() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "Thq quick brown fox jumpqd ovqr thq lazy dog";

        assertEquals(expected,
                replace(new StringBuilder(initial), 'q', "e").toString());
    }

    @Test
    public void testMultipleCharactersSingleOccurrence() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the lazy xxx";

        assertEquals(expected,
                replace(new StringBuilder(initial), 'x', "dog").toString());
    }

    @Test
    public void testMultipleCharactersMultipleOccurrence() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over xxe lazy dog";

        assertEquals(expected,
                replace(new StringBuilder(initial), 'x', "th").toString());
    }

    @Test
    public void testMultipleCharactersMultipleOccurrenceMultipleStrings() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over xxe lazy xxx";

        assertEquals(expected,
                replace(new StringBuilder(initial), 'x', "th", "dog").toString());
    }

    @Test
    public void testSingleCharSingleOccurrenceDelete() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the lay dog";

        assertEquals(expected,
                RemediationUtils.delete(new StringBuilder(initial), "z").toString());

    }

    @Test
    public void testSingleCharMultipleOccurrenceDelete() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "Th quick brown fox jumpd ovr th lazy dog";

        assertEquals(expected,
                RemediationUtils.delete(new StringBuilder(initial), "e").toString());
    }

    @Test
    public void testMultipleCharactersSingleOccurrenceDelete() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over the lazy ";

        assertEquals(expected,
                RemediationUtils.delete(new StringBuilder(initial), "dog").toString());
    }

    @Test
    public void testMultipleCharactersMultipleOccurrenceDelete() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over e lazy dog";

        assertEquals(expected,
                RemediationUtils.delete(new StringBuilder(initial), "th").toString());
    }

    @Test
    public void testMultipleCharactersMultipleOccurrenceMultipleStringsDelete() throws Exception {
        final String initial =  "The quick brown fox jumped over the lazy dog";
        final String expected = "The quick brown fox jumped over e lazy ";

        assertEquals(expected,
                RemediationUtils.delete(new StringBuilder(initial),"th", "dog").toString());
    }

    @Test
    public void testReplacementJavadocExamples() throws Exception {
        assertEquals("foo",
                replace(new StringBuilder("foo"), 'X', "baz").toString());

        assertEquals("fXX",
                replace(new StringBuilder("foo"), 'X', "o").toString());

        assertEquals("foo",
                replace(new StringBuilder("foo"), 'X', "of").toString());

        assertEquals("XXo",
                replace(new StringBuilder("foo"), 'X', "fo").toString());
    }

    @Test
    public void testDeletionJavadocExamples() throws Exception {
        assertEquals("foo",
                delete(new StringBuilder("foo"), "baz").toString());

        assertEquals("f",
                delete(new StringBuilder("foo"), "o").toString());

        assertEquals("foo",
                delete(new StringBuilder("foo"),"of").toString());

        assertEquals("o",
                delete(new StringBuilder("foo"), "fo").toString());
    }

    @Test
    public void testPositionFound() throws Exception {
        Stream<Integer> invalidPositions = RemediationUtils.stringPositions(new StringBuilder("abc"), Stream.of("a"));
        assertEquals(1, invalidPositions.count());
        invalidPositions = RemediationUtils.stringPositions(new StringBuilder("abc"), Stream.of("a"));
        assertEquals(0, (long)invalidPositions.findFirst().get());
    }

    @Test
    public void testPositionNotFound() throws Exception {
        Stream<Integer> invalidPositions = RemediationUtils.stringPositions(new StringBuilder("abc"), Stream.of("z"));
        assertEquals(0, invalidPositions.count());
        invalidPositions = RemediationUtils.stringPositions(new StringBuilder("abc"), Stream.of("z"));
        assertFalse(invalidPositions.findFirst().isPresent());
    }

    @Test
    public void testPositionsMultiple() throws Exception {
        Stream<Integer> invalidPositions = RemediationUtils.stringPositions(new StringBuilder("abc"), Stream.of("a", "c"));

        final AtomicInteger index = new AtomicInteger(0);
        long found = invalidPositions.peek(pos -> {
            if (index.get() == 0) {
                assertEquals(0, (long)pos);
            }
            if (index.get() == 1) {
                assertEquals(2, (long)pos);
            }
            index.getAndIncrement();
        }).count();

        assertEquals(2, found);
    }

    @Test
    public void testPositionsUtf8InvalidCharacters() throws Exception {
        Stream<Integer> invalidPositions = RemediationUtils.matchPositions(new StringBuilder(ILLEGAL_CHARS), Stream.of(new InvalidUtf8CharacterMatcher()));

        final AtomicInteger index = new AtomicInteger(0);
        long found = invalidPositions.peek(pos -> {
            if (index.get() == 0) {
                assertEquals(1, (long)pos);
            }
            if (index.get() == 1) {
                assertEquals(4, (long)pos);
            }
            if (index.get() == 2) {
                assertEquals(7, (long)pos);
            }
            index.getAndIncrement();
        }).count();

        assertEquals(3, found);
    }
}