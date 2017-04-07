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

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.dataconservancy.packaging.tool.impl.support.validation.RemediationUtils.matchPositions;

/**
 * Remediates strings by substituting a replacement character for unwanted characters or positions within a string.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SubstitutionRemediation {

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
     * @param toRemediate the string to perform a replacement on
     * @param replacementChar the character used to replace each character of a matching string
     * @param stringsToReplace strings that may appear in the original string which are subject to replacement
     * @return the {@code original} StringBuilder instance, which may have had a replacement operation performed on it
     */
    public StringBuilder remediateMatchingStrings(StringBuilder toRemediate, char replacementChar, String... stringsToReplace) {
        return RemediationUtils.replace(toRemediate, replacementChar, stringsToReplace);
    }

    /**
     * Replaces the occurrence of each character in {@code stringsToReplace} with the {@code replacementChar}
     * if the replacement string is <em>equal to</em> the string to remediate.
     * <p>
     * Examples:
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "baz" }
     * result: "foo"
     * The string "baz" is not equal to "foo".
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "o" }
     * result: "foo"
     * The string "o" is not equal to "foo".
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "fo" }
     * result: "foo"
     * The string "fo" is not equal to "foo"
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "of" }
     * result: "foo"
     * The string "of" is not equal to "foo"
     * </p>
     * <p>
     * original: "foo"
     * replacementChar: 'X'
     * stringsToReplace: { "foo" }
     * result: "XXX"
     * The strings are equal
     * </p>
     *
     * @param toRemediate the string to perform a replacement on
     * @param replacementChar the character used to replace each character of the string to remediate
     * @param stringsToReplace strings that may equal to the original string which are subject to replacement
     * @return the {@code original} StringBuilder instance, which may have had a replacement operation performed on it
     */
    public StringBuilder remediateEqualStrings(StringBuilder toRemediate, char replacementChar, String... stringsToReplace) {
        String stringToRemediate = toRemediate.toString();
        for (String stringToReplace : stringsToReplace) {
            if (stringToRemediate.equals(stringToReplace)) {
                RemediationUtils.replace(toRemediate, replacementChar, stringToReplace);
                return toRemediate;
            }
        }

        return toRemediate;
    }

    /**
     * Replaces each character that matches in the original string with the replacement character.
     *
     * @param toRemediate the string to perform a replacement on
     * @param replacementChar the character used to replace each invalid character
     * @param invalidChMatcher matchers that are applied to each character in the string being remediated
     * @return the {@code original} StringBuilder instance, which may have had replacement operations performed on it
     * @throws IndexOutOfBoundsException if the position is not between 0 and original.length()
     */
    public StringBuilder remediateMatchingCharacters(StringBuilder toRemediate, char replacementChar, Stream<Matcher<Character>> invalidChMatcher) {
        return RemediationUtils.replacePos(toRemediate, replacementChar, matchPositions(toRemediate, invalidChMatcher));
    }

    /**
     * Replaces each character that matches the supplied regex in the original string with the replacement character.
     *
     * @param toRemediate the string to perform a replacement on
     * @param replacementChar the character used to replace each character that matched the regular expression
     * @param regexPattern a regular expression applied to the string being remediated
     * @return the {@code original} StringBuilder instance, which may have had replacement operations performed on it
     * @throws IndexOutOfBoundsException if the position is not between 0 and original.length()
     */
    public StringBuilder remediateMatchingStrings(StringBuilder toRemediate, char replacementChar, String regexPattern) {
        Stream.Builder<Integer> positions = Stream.builder();
        Pattern pattern = Pattern.compile(regexPattern);
        java.util.regex.Matcher matcher = pattern.matcher(toRemediate);

        int index = 0;
        while (matcher.find(index)) {
            MatchResult mr = matcher.toMatchResult();
            for (int i = mr.start() ; i < mr.end(); i++) {
                positions.accept(i);
            }
            index = mr.end();
        }

        return RemediationUtils.replacePos(toRemediate, replacementChar, positions.build());
    }

    /**
     * Replaces each position in the original string with the replacement character.
     *
     * @param toRemediate the string to perform a replacement on
     * @param replacementChar the character used to replace each position
     * @param invalidPositions the 0-based position(s) (index) in the original string to replace
     * @return the {@code original} StringBuilder instance, which may have had replacement operations performed on it
     * @throws IndexOutOfBoundsException if the position is not between 0 and original.length()
     */
    public StringBuilder remediatePositions(StringBuilder toRemediate, char replacementChar, Stream<Integer> invalidPositions) {
        return RemediationUtils.replacePos(toRemediate, replacementChar, invalidPositions);
    }

}
