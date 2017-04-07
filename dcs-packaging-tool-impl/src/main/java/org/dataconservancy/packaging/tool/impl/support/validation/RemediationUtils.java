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

import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Lower-level string examination and manipulation methods.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
class RemediationUtils {

    /**
     * Replaces the occurrence of each string in {@code stringsToReplace} with the {@code replacementChar}
     * in the {@code original} string.
     * <p>
     * Examples:
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "baz" }
     * result: "foo"
     * The string "baz" never appears in the original string.
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "o" }
     * result: "fXX"
     * The string "o" occurs twice in the original string.
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "fo" }
     * result: "XXo"
     * The string "fo" occurs once in the original string.
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "of" }
     * result: "foo"
     * The string "of" never appears in the original string
     * </p>
     *
     * @param original the string to perform a replacement on
     * @param replacementChar the character used to replace each character of a matching string
     * @param stringsToReplace strings that may appear in the original string which are subject to replacement
     * @return the {@code original} StringBuilder instance, which may have had a replacement operation performed on it
     */
    static StringBuilder replace(StringBuilder original, char replacementChar, String... stringsToReplace) {
        return replaceStr(original, replacementChar, Arrays.stream(stringsToReplace));
    }

    /**
     * Replaces the occurrence of each string in {@code stringsToReplace} with the {@code replacementChar}
     * in the {@code original} string.
     * <p>
     * Examples:
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "baz" }
     * result: "foo"
     * The string "baz" never appears in the original string.
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "o" }
     * result: "fXX"
     * The string "o" occurs twice in the original string.
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "fo" }
     * result: "XXo"
     * The string "fo" occurs once in the original string.
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "of" }
     * result: "foo"
     * The string "of" never appears in the original string
     * </p>
     *
     * @param original the string to perform a replacement on
     * @param replacementChar the character used to replace each character of a matching string
     * @param stringsToReplace strings that may appear in the original string which are subject to replacement
     * @return the {@code original} StringBuilder instance, which may have had a replacement operation performed on it
     */
    static StringBuilder replaceStr(StringBuilder original, char replacementChar, Stream<String> stringsToReplace) {
        stringsToReplace.forEach(toReplace ->
        {
            int index = -1;
            while ((index = original.indexOf(toReplace)) > -1) {
                for (int replacementIndex = index; replacementIndex < (index + toReplace.length()); replacementIndex++) {
                    original.setCharAt(replacementIndex, replacementChar);
                }
            }
        });

        return original;
    }

    /**
     * Replaces the occurrence of each string in {@code stringsToReplace} with the {@code replacementChar}
     * in the {@code original} string.
     * <p>
     * Examples:
     * </p>
     * <p>
     * original: "foo"
     * stringsToDelete: { "baz" }
     * result: "foo"
     * The string "baz" never appears in the original string.
     * </p>
     * <p>
     * original: "foo"
     * stringsToDelete: { "o" }
     * result: "f"
     * The string "o" occurs twice in the original string.
     * </p>
     * <p>
     * original: "foo"
     * stringsToDelete: { "fo" }
     * result: "o"
     * The string "fo" occurs once in the original string.
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToDelete: { "of" }
     * result: "foo"
     * The string "of" never appears in the original string
     * </p>
     *
     * @param original the string to perform a replacement on
     * @param stringsToDelete strings that may appear in the original string which are subject to deletion
     * @return the {@code original} StringBuilder instance, which may have had a deletion operation performed on it
     */
    static StringBuilder delete(StringBuilder original, String... stringsToDelete) {
        for (String toDelete : stringsToDelete) {
            int index = -1;
            while ((index = original.indexOf(toDelete)) > -1) {
                original.delete(index, index + toDelete.length());
            }
        }

        return original;
    }

    /**
     * Replaces each position in the original string with the replacement character.
     *
     * @param original the string to perform a replacement on
     * @param replacementChar the character used to replace each position
     * @param positionsToReplace the 0-based position(s) (index) in the original string to replace
     * @return the {@code original} StringBuilder instance, which may have had replacement operations performed on it
     * @throws IndexOutOfBoundsException if the position is not between 0 and original.length()
     */
    static StringBuilder replacePos(StringBuilder original, char replacementChar, Stream<Integer> positionsToReplace) {
        positionsToReplace.forEach((index) -> original.setCharAt(index, replacementChar));
        return original;
    }

    /**
     * Tests the supplied {@code StringBuilder} for the presence of {@code strings}, and returns the positions of
     * matching characters.
     *
     * @param toTest the {@code StringBuilder} being tested
     * @param strings the strings being tested for
     * @return a {@code Stream<Integer>} containing the positions (zero-indexed) of the characters matched by the
     *         {@code strings}
     */
    static Stream<Integer> stringPositions(StringBuilder toTest, Stream<String> strings) {
        if (toTest == null) {
            throw new IllegalArgumentException("StringBuilder must not be null.");
        }

        if (strings == null) {
            throw new IllegalArgumentException("Invalid strings must not be null");

        }
        Stream.Builder<Integer> positionStream = Stream.builder();
        strings.forEach(invalidString -> {
            int index = -1;
            while ((index = toTest.indexOf(invalidString, index)) > -1) {
                int endIndex = -1;
                for (int i = index; i < (index + invalidString.length()); i++) {
                    positionStream.accept(i);
                    endIndex = i;
                }
                index = endIndex + 1;
            }
        });

        return positionStream.build();
    }

    /**
     * Applies each {@code Matcher} to each character of the {@code StringBuilder} and returns the positions of
     * matching characters.
     *
     * @param toTest the {@code StringBuilder} being tested
     * @param matchers the {@code Matcher}s applied to each character in the {@code StringBuilder}
     * @return a {@code Stream<Integer>} containing the positions (zero-indexed) of characters matched by the
     *         {@code matchers}
     */
    static Stream<Integer> matchPositions(StringBuilder toTest, Stream<Matcher<Character>> matchers) {
        if (toTest == null) {
            throw new IllegalArgumentException("StringBuilder must not be null.");
        }

        if (matchers == null) {
            throw new IllegalArgumentException("Matchers must not be null");
        }

        Stream.Builder<Integer> positionStream = Stream.builder();

        matchers.forEach(matcher -> {
            for (int i = 0; i < toTest.length(); i++) {
                if (matcher.matches(toTest.charAt(i))) {
                    positionStream.accept(i);
                }
            }
        });

        return positionStream.build();
    }
}
