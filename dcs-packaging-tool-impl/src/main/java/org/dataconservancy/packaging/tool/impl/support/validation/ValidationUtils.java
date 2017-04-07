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

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
class ValidationUtils {

    /**
     * Returns {@code true} if the supplied character is between 0x00 and 0x1f (inclusive), or is greater than or equal
     * to 0x7f.
     * <p>
     * This comports with §2.2.2.1 of the Data Conservancy BagIt Profile version 1.0
     * </p>
     *
     * @param ch the character
     * @return true if the character matches
     * @see <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">Data Conservancy BagIt Profile 1.0</a>
     */
    static boolean isInvalidUf8Char(char ch) {
        if (((Integer.parseInt("00", 16) <= ch) && (ch <= Integer.parseInt("1f", 16))) ||
                (ch >= Integer.parseInt("7f", 16))) { //0x7f is Delete
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the supplied character is a:
     * <dl>
     *     <dt>"</dt>
     *     <dd>Quotation Mark, 0x22</dd>
     *     <dt>*</dt>
     *     <dd>Asterisk, 0x2a</dd>
     *     <dt>/</dt>
     *     <dd>Solidus, 0x2f</dd>
     *     <dt>:</dt>
     *     <dd>Colon, 0x3a</dd>
     *     <dt>&lt;</dt>
     *     <dd>Less than sign, 0x3c</dd>
     *     <dt>&gt;</dt>
     *     <dd>Greater than sign, 0x3e</dd>
     *     <dt>?</dt>
     *     <dd>Question mark, 0x3f</dd>
     *     <dt>\</dt>
     *     <dd>Reverse Solidus, 0x5c</dd>
     *     <dt>|</dt>
     *     <dd>Vertical line, 0x7c</dd>
     *     <dt>~</dt>
     *     <dd>Tilde, 0x7e</dd>
     * </dl>
     * <p>
     * This comports with §2.2.2.1 of the Data Conservancy BagIt Profile version 1.0
     * </p>
     *
     * @param ch the character
     * @return true if the character matches
     * @see <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">Data Conservancy BagIt Profile 1.0</a>
     */
    static boolean isBlacklistedChar(char ch) {
        return ch == (char) 0x22  ||  // "
                ch == (char) 0x2a ||  // *
                ch == (char) 0x2f ||  // /
                ch == (char) 0x3a ||  // :
                ch == (char) 0x3c ||  // <
                ch == (char) 0x3e ||  // >
                ch == (char) 0x3f ||  // ?
                ch == (char) 0x5c ||  // \
                ch == (char) 0x7c ||  // |
                ch == (char) 0x7e;    // ~
    }
}
