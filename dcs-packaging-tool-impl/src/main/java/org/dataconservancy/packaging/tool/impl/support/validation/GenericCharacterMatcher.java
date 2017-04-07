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
 * A Hamcrest {@link org.hamcrest.Matcher} which returns {@code true} if <em>any</em> of the characters supplied on
 * construction {@link #matches(Object) matches} the supplied {@code Object}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class GenericCharacterMatcher extends BaseMatcher<Character> {

    private final CharSequence toMatch;

    /**
     * Constructs a new matcher which will {@link #matches(Object) match} any <em>one</em> of the supplied characters.
     *
     * @param toMatch an array of possible characters to match
     */
    public GenericCharacterMatcher(CharSequence toMatch) {
        if (toMatch == null || toMatch.length() == 0) {
            throw new IllegalArgumentException("CharSequence toMatch must not be empty or null.");
        }
        this.toMatch = toMatch;
    }

    /**
     * Constructs a new matcher which will {@link #matches(Object) match} any <em>one</em> of the supplied characters.
     *
     * @param toMatch an array of possible characters to match
     */
    public GenericCharacterMatcher(char toMatch) {
        this.toMatch = String.valueOf(toMatch);
    }

    /**
     * Constructs a new matcher which will {@link #matches(Object) match} any <em>one</em> of the supplied characters.
     *
     * @param toMatch an array of possible characters to match
     */
    public GenericCharacterMatcher(char[] toMatch) {
        if (toMatch == null || toMatch.length == 0) {
            throw new IllegalArgumentException("char[] toMatch must not be empty or null.");
        }
        this.toMatch = String.valueOf(toMatch);
    }

    /**
     * <p>
     * Returns true if the supplied {@code Object} matches any <em>one</em> of the characters supplied at construction.
     * </p>
     * <p>
     * <em>Implementation note:</em> the supplied {@code Object} <em>must</em> be an instance of {@code Character} or
     * {@code CharSequence}.
     * </p>
     * {@inheritDoc}
     *
     * @param o the {@code Object} to match, which <em>must</em> be an instance if {@code Character}
     * @return true if the {@code Object} to match is <em>one</em> of the characters supplied at construction
     * @throws IllegalArgumentException if {@code o} is {@code null} or <em>is not</em> an instance of {@code Character}
     *                                  or {@code CharSequence}
     */
    @Override
    public boolean matches(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Supplied Character must not be null.");
        }

        if (!(o instanceof Character || o instanceof CharSequence)) {
            throw new IllegalArgumentException("Supplied object must be an instanceof Character or CharSequence (was: " + o.getClass().getName() + ")");
        }

        if (o instanceof Character) {
            char candidate = (char) o;
            return toMatch.chars().filter(toMatch -> toMatch == candidate).findFirst().isPresent();
        }
        CharSequence candidates = (CharSequence) o;

        return toMatch.chars().mapToObj(toMatch -> (char) toMatch)
                .reduce(Boolean.FALSE,
                        (result, toMatch) -> result ||
                            candidates.chars().filter(candidate -> candidate == toMatch).findFirst().isPresent(),
                        (one, two) -> one || two);
    }

    /**
     * <em>Implementation note:</em> a no-op
     * {@inheritDoc}
     *
     * @param description {@inheritDoc}
     */
    @Override
    public void describeTo(Description description) {

    }
}
