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

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Truncates strings that are longer than the specified number of characters.  When a truncation takes place, the
 * truncation character, {@code X}, is substituted in place of truncated characters.  The solidus ({@code /}, 0x2f) and
 * full stop ({@code .}, 0x2e) are never truncated.  Strings may be truncated from the
 * {@link Strategy#LEADING_SUBSTITUTION beginning} or {@link Strategy#TRAILING_SUBSTITUTION ending} of the supplied
 * string.  If there are portions of a string that the caller never wishes to be truncated (e.g. {@link PackageResourceType
 * directories reserved} by the BagIt specification and profile), then those portions should <em>not</em> be passed to
 * this class for remediation.  If a string <em>cannot</em> be truncated to the specified limit, a
 * {@code RuntimeException} is thrown.
 * <p>
 * According to the Data Conservancy BagIt Profile 1.0, the paths within a bag may not be longer than 1024 characters,
 * and individual <em>path components</em> may not be longer than 255 characters.  Path components are delimited by
 * the forward slash (a.k.a solidus: {@code /}, 0x2f).  Example paths that conform to the Profile include:
 * </p>
 * <dl>
 *     <dt>data/bin/path/to/binary/file.bin</dt>
 *     <dd>a relative path with six <em>path components</em>, each component less than 255 characters, and an overall
 *         length less than 1025 characters.</dd>
 *     <dt>/data/bin/path/to/binary/file.bin</dt>
 *     <dd>an absolute form of the same path, still containing six path components.</dd>
 *     <dt>META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM/manifest.rdf</dt>
 *     <dd>another example path composed of five path components</dd>
 *     <dt>/data</dt>
 *     <dd>an absolute path composed of a single path component</dd>
 *     <dt>bag-info.txt</dt>
 *     <dd>a single path component that appears to be a file name</dd>
 * </dl>
 * <p>
 * This class provides special consideration to strings being truncated.  That is to say, the semantic of the string
 * supplied for remediation is considered to be a file path, which may be absolute (beginning with the solidus) or
 * relative.  Special consideration is given to two characters common in file paths: the solidus ({@code /}, 0x2f) and
 * the full stop ({@code .}, 0x2e).  The solidus is considered to be a path separator, and the full stop is often used
 * to delimit a file <em>name</em> from the <em>extension</em>.  Accordingly, these characters are <em>not</em> subject
 * to truncation.
 * </p>
 *
 * @see <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">Data Conservancy
 *      BagIt Profile 1.0 §2.2.2.1</a>
 */
public class TruncationRemediation {

    /**
     * Character that serves to separate <em>path components</em>
     */
    static final char PATH_SEPARATOR = '/';

    /**
     * Character that serves to separate a file <em>name</em> from a file name <em>extension</em>
     */
    static final char EXT_SEPARATOR = '.';

    /**
     * Character inserted into a remediated string signaling that a truncation has occurred.
     */
    static final char TRUNCATION_CHAR = 'X';

    /**
     * Characters that must not be truncated (i.e. they must be preserved in the remediated string)
     */
    static final char[] RESERVED_CHARS = {PATH_SEPARATOR, EXT_SEPARATOR};

    private GenericCharacterMatcher reservedCharacterMatcher = new GenericCharacterMatcher(RESERVED_CHARS);

    /**
     * A strategy used to perform truncation
     */
    public enum Strategy {

        /**
         * Truncates characters starting with those at the beginning of the string
         */
        LEADING_SUBSTITUTION,

        /**
         * Truncates characters starting with those at the end of the string
         */
        TRAILING_SUBSTITUTION,
    }

    /**
     * Remediates the supplied string by truncating it to the specified number of characters.  Characters will be
     * truncated according to the supplied {@link Strategy}.  If the string is already within the supplied limit, the
     * string is returned unaltered.  If the string cannot be truncated to the specified limit, a
     * {@code RuntimeException} is thrown.
     *
     * @param toRemediate the string to be remediated to the specified {@code length}
     * @param limit the length of the remediated string
     * @param strategy determines which characters are considered for remediation first
     * @return the remediate string, of length {@code limit}
     * @throws RuntimeException if the supplied string cannot be truncated to the specified length
     * @throws IllegalArgumentException if the limit is out of bounds (less than 1) or if any argument is {@code null}.
     */
    public String remediate(StringBuilder toRemediate, int limit, Strategy strategy) {
        if (toRemediate == null) {
            throw new IllegalArgumentException("The StringBuilder to remediate must not be null.");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("The remediation Strategy must not be null.");
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Remediation limit must be greater than 0.");
        }

        if (toRemediate.length() <= limit) {
            return toRemediate.toString();
        }

        final int ignoredCharacterCount = toRemediate.length() - limit;
        final AtomicInteger ignoredCharactersRemaining = new AtomicInteger(ignoredCharacterCount);
        final AtomicReference<STATE> state = new AtomicReference<>(STATE.INIT);

        switch (strategy) {
            case LEADING_SUBSTITUTION:
                toRemediate = toRemediate.chars()
                        .sequential()
                        .mapToObj(ch -> ((char) ch))
                        .reduce(new StringBuilder(),
                                (result, element) -> {
                                    switch (state.get()) {
                                        case INIT:
                                            if (reservedCharacterMatcher.matches(element)) {
                                                result.append(element);
                                            } else {
                                                result.append(TRUNCATION_CHAR);
                                            }
                                            state.set(STATE.IGNORE);
                                            break;
                                        case IGNORE:
                                            if (reservedCharacterMatcher.matches(element)) {
                                                result.append(element);
                                                break;
                                            }

                                            if (reservedCharacterMatcher.matches(result.charAt(result.length() - 1))
                                                    && ignoredCharactersRemaining.get() > 0) {
                                                result.append(TRUNCATION_CHAR);
                                                break;
                                            }

                                            if (ignoredCharactersRemaining.getAndDecrement() <= 0) {
                                                result.append(element);
                                                state.set(STATE.PASS_THROUGH);
                                            }

                                            break;
                                        case PASS_THROUGH:
                                            result.append(element);
                                            break;
                                        default:
                                            throw new IllegalStateException("Unknown state!");
                                    }
                                    return result;
                                },
                                StringBuilder::append);
                break;
            case TRAILING_SUBSTITUTION:
                StringBuilder result = toRemediate.reverse().chars()
                        .sequential()
                        .mapToObj(ch -> (char) ch)
                        .reduce(new StringBuilder(),
                                (builder, ch) -> {
                                    switch (state.get()) {
                                        case INIT:
                                            if (reservedCharacterMatcher.matches(ch)) {
                                                builder.append(ch);
                                            } else if (ignoredCharactersRemaining.get() > 0) {
                                                builder.append(TRUNCATION_CHAR);
                                                state.set(STATE.IGNORE);
                                            } else {
                                                builder.append(ch);
                                                state.set(STATE.PASS_THROUGH);
                                            }
                                            break;
                                        case IGNORE:
                                            if (reservedCharacterMatcher.matches(ch)) {
                                                builder.append(ch);
                                            } else if (reservedCharacterMatcher.matches(builder.charAt(builder.length() - 1))) {
                                                builder.append(TRUNCATION_CHAR);
                                            } else if (ignoredCharactersRemaining.get() == 0) {
                                                builder.append(ch);
                                                state.set(STATE.PASS_THROUGH);
                                            } else {
                                                ignoredCharactersRemaining.getAndDecrement();
                                            }
                                            break;
                                        case PASS_THROUGH:
                                            builder.append(ch);
                                            break;
                                        default:
                                            throw new IllegalStateException("Unknown state!");
                                    }

                                    return builder;
                                },
                                StringBuilder::append);
                toRemediate = result.reverse();
                break;
            default:
                throw new IllegalArgumentException("Unknown truncation remediation strategy!");
        }

        final String result = toRemediate.toString();
        if (result.length() > limit) {
            throw new RuntimeException("Failed to limit remediated string to " + limit + " characters (actual remediated length: " + result.length() + " characters): '" + result + "'");
        }

        return result;
    }

    private enum STATE {
        INIT,
        IGNORE,
        PASS_THROUGH
    }

}
