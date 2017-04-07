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

/**
 * A Hamcrest matcher that will match a subset of the illegal characters defined by
 * <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">section 2.2.2.1</a>
 * of the the Data Conservancy BagIt Profile.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @see <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">Data Conservancy BagIt Profile 1.0</a>
 */
public class InvalidUtf8CharacterMatcher extends BaseMatcher<Character> {

    /**
     * Returns {@code true} if the supplied character is between 0x00 and 0x1f (inclusive), or is greater than or equal
     * to 0x7f.
     * <p>
     * This comports with §2.2.2.1 of the Data Conservancy BagIt Profile version 1.0
     * </p>
     *
     * @param o the character to match
     * @return true if the character matches
     * @see <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">Data Conservancy BagIt Profile 1.0</a>
     */
    @Override
    public boolean matches(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Supplied Character must not be null.");
        }

        if (!(o instanceof Character)) {
            throw new IllegalArgumentException("Supplied object must be an instanceof Character");
        }

        Character ch = (Character)o;

        return ValidationUtils.isInvalidUf8Char(ch);
    }

    @Override
    public void describeTo(Description description) {

    }
}
