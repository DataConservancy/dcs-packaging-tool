/*
 * Copyright 2014 Johns Hopkins University
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
package org.dataconservancy.dcs.util;

import java.lang.CharSequence;import java.lang.String;import java.lang.StringBuilder;import java.lang.System; /**
 * Provides methods making it easier to pretty-print strings.
 */
public class PrettyPrinter {

    /**
     * String used to indent each line, supplied on construction
     */
    private final String indent;

    /**
     * The internal StringBuilder that is managed by this class
     */
    private final StringBuilder sb;

    /**
     * Platform-dependent new line character
     */
    static final String NL = System.getProperty("line.separator");

    /**
     * Constructs a default PrettyPrinter, which uses two spaces for indenting each line.
     */
    public PrettyPrinter() {
        this.indent = "  ";
        this.sb = new StringBuilder();
    }

    /**
     * Constructs a PrettyPrinter using the supplied {@code String} to indent each line.
     *
     * @param indent indent string
     */
    public PrettyPrinter(String indent) {
        this.indent = indent;
        this.sb = new StringBuilder();
    }

    /**
     * Constructs a PrettyPrinter using the supplied {@code String} to indent each line,
     * and the supplied StringBuilder.
     *
     * @param indent indent string
     * @param sb internal StringBuilder
     */
    public PrettyPrinter(String indent, StringBuilder sb) {
        this.indent = indent;
        this.sb = sb;
    }

    /**
     * Appends the character sequence to the internal StringBuilder
     *
     * @param cs the character sequence
     * @return this PrettyPrinter
     */
    public PrettyPrinter append(CharSequence cs) {
        sb.append(cs);
        return this;
    }

    /**
     * Appends the character sequence to the internal StringBuilder, followed by the platform-specific newline
     * character.
     *
     * @param cs the character sequence
     * @return this PrettyPrinter
     */
    public PrettyPrinter appendWithNewLine(CharSequence cs) {
        sb.append(cs).append(NL);
        return this;
    }

    /**
     * Appends the character sequence to the internal StringBuilder.  The character sequence will be indented
     * {@code depth} times, using the string supplied for indentation on construction.
     *
     * @param cs the character string to append to the internal StringBuilder
     * @param depth the number of times to repeate the indentation sequence prior to appending the {@code cs}
     * @return this PrettyPrinter
     */
    public PrettyPrinter append(CharSequence cs, int depth) {
        if (depth > 0) {
            for (int i = 1; i <= depth; i++) {
                sb.append(indent);
            }
        }

        sb.append(cs);

        return this;
    }

    /**
     * Returns the contents of the internal {@code StringBuilder} as a string.
     *
     * @return the contents of the internal {@code StringBuilder}
     */
    @java.lang.Override
    public String toString() {
        return sb.toString();
    }

    /**
     * Clears the internal {@code StringBuilder} instance.
     */
    public void reset() {
        sb.delete(0, sb.length());
    }
}
