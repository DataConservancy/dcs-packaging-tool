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

import java.lang.CharSequence;import java.lang.String;import java.lang.StringBuilder; /**
 * Provides methods making it easier to pretty-print strings in a hierarchical fashion.  Especially where the goal is to
 * present the string form of an instance of the DCS data model.
 */
public class HierarchicalPrettyPrinter extends PrettyPrinter {

    /**
     * Default starting depth
     */
    private static final int DEFAULT_DEPTH = 0;

    /**
     * Initial depth, set on construction
     */
    private int initialDepth = DEFAULT_DEPTH;

    /**
     * The current depth, set on construction, reset to {@link #initialDepth} by {@link #reset()}
     */
    private int depth = DEFAULT_DEPTH;

    /**
     * Constructs a default PrettyPrinter, which uses two spaces for indenting each line.
     */
    public HierarchicalPrettyPrinter() {

    }

    public HierarchicalPrettyPrinter(int initialDepth) {
        this.initialDepth = this.depth = initialDepth;
    }

    /**
     * Constructs a PrettyPrinter using the supplied {@code String} to indent each line.
     *
     * @param indent indent string
     */
    public HierarchicalPrettyPrinter(String indent) {
        super(indent);
    }

    /**
     * Constructs a PrettyPrinter using the supplied {@code String} to indent each line.  The
     * lines will be indented {@code initalDepth} times, using the {@code indent} string.
     *
     * @param indent indent string
     * @param initialDepth the number of times each line will be indented
     */
    public HierarchicalPrettyPrinter(String indent, int initialDepth) {
        super(indent);
        this.initialDepth = this.depth = initialDepth;
    }

    /**
     * Constructs a PrettyPrinter using the supplied {@code String} to indent each line,
     * and the supplied StringBuilder.
     *
     * @param indent indent string
     * @param sb     internal StringBuilder
     */
    public HierarchicalPrettyPrinter(String indent, StringBuilder sb) {
        super(indent, sb);
    }

    /**
     * Constructs a PrettyPrinter using the supplied {@code String} to indent each line.  The
     * lines will be indented {@code initalDepth} times, using the {@code indent} string.
     *
     * @param indent indent string
     * @param sb the internal StringBuilder
     * @param initialDepth the number of times each line will be indented
     */
    public HierarchicalPrettyPrinter(String indent, StringBuilder sb, int initialDepth) {
        super(indent, sb);
        this.initialDepth = this.depth = initialDepth;
    }

    /**
     * Increments the number of times each line is indented by one.
     *
     * @return the new depth
     */
    public int incrementDepth() {
        this.depth++;
        return this.depth;
    }

    /**
     * Decrements the number of times each line is indented by one.  Cannot go below the initial depth (by default, 0).
     *
     * @return the new depth
     */
    public int decrementDepth() {
        if ((this.depth - 1) < initialDepth) {
            return initialDepth;
        }

        this.depth--;
        return this.depth;
    }

    /**
     * Indents the string before appending it to the internal StringBuilder
     *
     * @param cs the CharacterSequence to append
     * @return this HierarchicalPrettyPrinter
     */
    public PrettyPrinter appendWithIndent(CharSequence cs) {
        return super.append(cs, depth);
    }

    /**
     * Indents the string before appending it to the internal StringBuilder, then appends
     * the platform-specific new line.
     *
     * @param cs the CharacterSequence to append
     * @return this HierarchicalPrettyPrinter
     */
    public PrettyPrinter appendWithIndentAndNewLine(CharSequence cs) {
        appendWithIndent(cs);
        append(PrettyPrinter.NL);
        return this;
    }

    /**
     * Clears the internal {@code StringBuilder} instance, and resets the depth (the number of
     * times a string is indented) to the initial depth on construction (by default, 0).
     */
    public void reset() {
        this.depth = initialDepth;
        super.reset();
    }
}
