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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class GenericCharacterMatcherTest {

    @Test
    public void testStringMatchesReservedCharacter() throws Exception {
        char reserved = 'f';
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        String toTest = "foo";

        assertTrue(underTest.matches(toTest));
    }

    @Test
    public void testStringNoMatchReservedCharacter() throws Exception {
        char reserved = 'f';
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        String toTest = "bar";

        assertFalse(underTest.matches(toTest));
    }

    @Test
    public void testCharacterMatchesReservedCharacter() throws Exception {
        char reserved = 'f';
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        char toTest = 'f';

        assertTrue(underTest.matches(toTest));
    }

    @Test
    public void testCharacterNoMatchReservedCharacter() throws Exception {
        char reserved = 'f';
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        char toTest = 'b';

        assertFalse(underTest.matches(toTest));
    }

    @Test
    public void testStringMatchesReservedString() throws Exception {
        String reserved = "fb";
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        String toTest = "foo";

        assertTrue(underTest.matches(toTest));
    }

    @Test
    public void testStringNoMatchesReservedString() throws Exception {
        String reserved = "foo";
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        String toTest = "bar";

        assertFalse(underTest.matches(toTest));
    }

    @Test
    public void testStringLastCharacterMatchesReservedString() throws Exception {
        String reserved = "for";
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        String toTest = "bar";

        assertTrue(underTest.matches(toTest));
    }

    @Test
    public void testStringACharacterMatchesReservedString() throws Exception {
        String reserved = "for";
        GenericCharacterMatcher underTest = new GenericCharacterMatcher(reserved);
        String toTest = "barn";

        assertTrue(underTest.matches(toTest));
    }
}