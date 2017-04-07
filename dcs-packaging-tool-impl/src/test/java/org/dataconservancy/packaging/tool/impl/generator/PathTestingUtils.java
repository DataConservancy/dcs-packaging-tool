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
package org.dataconservancy.packaging.tool.impl.generator;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Utility methods for creating path components.  Useful when testing remediation of long paths.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class PathTestingUtils {
    /**
     * Creates {@code componentCount} path components.  Each path component will be the specified {@code length}.  The
     * components will be joined together by {@code delimiter}.
     *
     * @param delimiter
     * @param componentCount
     * @param length
     * @return
     */
    public static String components(String delimiter, int componentCount, int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be a positive integer");
        }

        if (componentCount < 1) {
            throw new IllegalArgumentException("Component count must be a positive integer");
        }

        if (delimiter == null) {
            throw new IllegalArgumentException("Delimiter string must not be null (but it may be empty)");
        }

        StringBuilder componentString = new StringBuilder();

        for (int i = 0; i < componentCount; i++) {
            componentString.append(stringOf(length));
            if ((i + 1) < componentCount) {
                componentString.append(delimiter);
            }
        }

        return componentString.toString();
    }

    /**
     * Builds a string of specified length, containing <em>only</em> numbers (0-9) or letters (a-z, A-Z).
     *
     * @param length number of characters in the generated string
     * @return a string of {@code length} characters, containing <em>only</em> alphanumerics.
     */
    public static String stringOf(final int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be a positive integer.");
        }
        // valid 0x30 - 0x39 0-9 (10 characters, offset 0-9)
        // valid 0x41 - 0x5a A-Z (26 characters, offset 10-35)
        // valid 0x61 - 0x7a a-z (26 characters, offset 36-61)
        //                       (62 total, offset 0-61)

        final Random random = new Random();
        final StringBuilder str = random.ints(length, 0, 62)
                .mapToObj(offset -> {
                    int base;
                    int index;
                    if (offset < 10) {  // 0-9 (0x30 - 0x39)
                        base = 48; // 0x30;
                        index = base + offset;  // min: 48, max: 57 (0x39)
                    } else if (offset < 36) { // A-Z (0x41 - 0x5a)
                        base = 65; // 0x41;
                        index = base + (offset - 10);  // min: 65 max: 90 (0x5a)
                    } else {
                        base = 97; // 0x61;
                        index = base + (offset - 36);  // min: 97 max: 122 (0x7a)
                    }

                    return Character.valueOf((char) index);
                })
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

        return str.toString();
    }

    /**
     * Joins strings with the specified delimiter.
     *
     * @param delimiter
     * @param strings
     * @return
     */
    public static String join(String delimiter, String... strings) {
        if (delimiter == null) {
            throw new IllegalArgumentException("Delimter must not be null (but may be empty)");
        }
        return Arrays.stream(strings).collect(Collectors.joining(delimiter, "", ""));
    }

    /**
     * Joins strings with the specified delimiter.  The string will be prefixed with the delimiter.
     *
     * @param delimiter
     * @param strings
     * @return
     */
    public static String joinAbsolutely(String delimiter, String... strings) {
        if (delimiter == null) {
            throw new IllegalArgumentException("Delimter must not be null (but may be empty)");
        }
        return Arrays.stream(strings).collect(Collectors.joining(delimiter, delimiter, ""));
    }
}
