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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the Util class.
 */
public class UtilTest {

    private static final String ZERO_LENGTH_STRING = "";

    private static final String EMPTY_STRING = " ";

    private static final String NON_EMPTY_STRING = " foo ";

    private static final Object NOT_NULL_OBJECT = new Object();

    private static final Object NULL_OBJECT = null;

    private static final Collection NULL_COLLECTION = null;

    private static final Collection COLLECTION_CONTAINING_NULLS = new HashSet();

    private static final Collection COLLECTION_CONTAINING_NOT_NULLS = new HashSet();

    private static final Collection COLLECTION_CONTAINING_NOT_NULL_STRINGS = new HashSet();
    
    private static final Collection COLLECTION_CONTAINING_NULL_STRINGS = new HashSet();

    private static final Collection COLLECTION_CONTAINING_EMPTY_STRINGS = new HashSet();

    private static final Collection COLLECTION_CONTAINING_ZERO_LENGTH_STRINGS = new HashSet();

    private static final Object[] NULL_ARRAY = null;

    private static final Object[] OBJECT_ARRAY_CONTAINING_NULLS = new Object[] { NOT_NULL_OBJECT, NULL_OBJECT };

    private static final Object[] OBJECT_ARRAY_CONTAINING_NOT_NULLS = new Object[] { NOT_NULL_OBJECT };

    private static final String[] STRING_ARRAY_CONTAINING_NOT_NULL_STRINGS = new String[] { NON_EMPTY_STRING };
    
    private static final String[] STRING_ARRAY_CONTAINING_NULL_STRINGS = new String[] { (String)NULL_OBJECT };

    private static final String[] STRING_ARRAY_CONTAINING_EMPTY_STRINGS = new String[] { NON_EMPTY_STRING, EMPTY_STRING };

    private static final String[] STRING_ARRAY_CONTAINING_ZERO_LENGTH_STRINGS = new String[] { NON_EMPTY_STRING, ZERO_LENGTH_STRING };

    static {
        COLLECTION_CONTAINING_NULLS.add(NOT_NULL_OBJECT);
        COLLECTION_CONTAINING_NULLS.add(NULL_OBJECT);

        COLLECTION_CONTAINING_NOT_NULLS.add(NOT_NULL_OBJECT);

        COLLECTION_CONTAINING_NULL_STRINGS.add(NULL_OBJECT);

        COLLECTION_CONTAINING_EMPTY_STRINGS.add(NON_EMPTY_STRING);
        COLLECTION_CONTAINING_EMPTY_STRINGS.add(EMPTY_STRING);

        COLLECTION_CONTAINING_ZERO_LENGTH_STRINGS.add(NON_EMPTY_STRING);
        COLLECTION_CONTAINING_ZERO_LENGTH_STRINGS.add(ZERO_LENGTH_STRING);

        COLLECTION_CONTAINING_NOT_NULL_STRINGS.add(NON_EMPTY_STRING);
    }

    /**
     * Asserts that Util.isEmpty correctly handles zero-length and empty strings.
     *
     * @throws Exception
     */
    @Test
    public void testIsEmpty() throws Exception {
        assertTrue(Util.isEmpty(ZERO_LENGTH_STRING));
        assertTrue(Util.isEmpty(EMPTY_STRING));
        assertFalse(Util.isEmpty(NON_EMPTY_STRING));
    }

    /**
     * Asserts that Util.isNull correctly handles null objects.
     *
     * @throws Exception
     */
    @Test
    public void testIsNull() throws Exception {
        assertTrue(Util.isNull(NULL_OBJECT));
        assertFalse(Util.isNull(NOT_NULL_OBJECT));
    }

    /**
     * Asserts that Util.isEmptyOrNull correctly handles zero-length and empty strings.
     *
     * @throws Exception
     */
    @Test
    public void testIsEmptyOrNull() throws Exception {
        assertTrue(Util.isEmptyOrNull(EMPTY_STRING));
        assertTrue(Util.isEmptyOrNull(ZERO_LENGTH_STRING));
        assertFalse(Util.isEmptyOrNull(NON_EMPTY_STRING));
    }

    /**
     * Asserts that Util.containsNulls correctly handles Collections that contain null elements.
     *
     * @throws Exception
     */
    @Test
    public void testContainsNullsWithCollection() throws Exception {
        assertTrue(Util.containsNulls(COLLECTION_CONTAINING_NULLS));
        assertFalse(Util.containsNulls(COLLECTION_CONTAINING_NOT_NULLS));

    }

    /**
     * Supplying a null Collection to Util.containsNull results in an NPE
     *
     */
    @Test(expected = NullPointerException.class)
    public void testContainsNullWithNullCollection() {
        assertFalse(Util.containsNulls(NULL_COLLECTION));
    }

    /**
     * Asserts that Util.containsNulls correctly handles arrays that contain null elements.
     *
     * @throws Exception
     */
    @Test
    public void testContainsNullsWithArray() throws Exception {
        assertTrue(Util.containsNulls(OBJECT_ARRAY_CONTAINING_NULLS));
        assertFalse(Util.containsNulls(OBJECT_ARRAY_CONTAINING_NOT_NULLS));
    }

    /**
     * Supplying a null array to Util.containsNull results in an NPE
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void testContainsNullWithNullArray() throws Exception {
        assertFalse(Util.containsNulls(NULL_ARRAY));
    }

    /**
     * Asserts that Util.containsNullsOrEmptyStrings correctly handles Collections that contain null, empty,
     * or zero-length strings.
     *
     * @throws Exception
     */
    @Test
    public void testContainsNullsOrEmptyStringsWithCollection() throws Exception {
        assertTrue(Util.containsNullsOrEmptyStrings(COLLECTION_CONTAINING_NULL_STRINGS));
        assertTrue(Util.containsNullsOrEmptyStrings(COLLECTION_CONTAINING_EMPTY_STRINGS));
        assertTrue(Util.containsNullsOrEmptyStrings(COLLECTION_CONTAINING_ZERO_LENGTH_STRINGS));
        assertFalse(Util.containsNullsOrEmptyStrings(COLLECTION_CONTAINING_NOT_NULL_STRINGS));
    }

    /**
     * Asserts that Util.containsNullsOrEmptyStrings correctly handles arrays that contain null, empty,
     * or zero-length strings.
     *
     * @throws Exception
     */
    @Test
    public void testContainsNullsOrEmptyStringsWithArray() throws Exception {
        assertTrue(Util.containsNullsOrEmptyStrings(STRING_ARRAY_CONTAINING_NULL_STRINGS));
        assertTrue(Util.containsNullsOrEmptyStrings(STRING_ARRAY_CONTAINING_EMPTY_STRINGS));
        assertTrue(Util.containsNullsOrEmptyStrings(STRING_ARRAY_CONTAINING_ZERO_LENGTH_STRINGS));
        assertFalse(Util.containsNullsOrEmptyStrings(STRING_ARRAY_CONTAINING_NOT_NULL_STRINGS));
    }

    /**
     * Demonstrates that {@link Util#isEqual(java.util.Collection, java.util.Collection)} behaves the same as
     * {@link Collection#equals(Object)} when ArrayList is the implementation.
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testIsEqualWithArrayLists() throws Exception {
        Collection one = new ArrayList();
        Collection two = new ArrayList();

        one.add("foo");
        one.add("bar");

        two.add("foo");
        two.add("bar");

        assertTrue(Util.isEqual(one, two));
        assertTrue(Util.isEqual(two, one));
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
    }

    /**
     * Demonstrates that {@link Util#isEqual(java.util.Collection, java.util.Collection)} behaves the same as
     * {@link Collection#equals(Object)} when HashSet is the implementation.
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testIsEqualWithSets() throws Exception {
        Collection one = new HashSet();
        Collection two = new HashSet();

        one.add("foo");
        one.add("bar");

        two.add("foo");
        two.add("bar");

        assertTrue(Util.isEqual(one, two));
        assertTrue(Util.isEqual(two, one));
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
    }

    /**
     * Demonstrates that {@link Util#isEqual(java.util.Collection, java.util.Collection)} can test equality between
     * Collections with differing implementations.
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testIsEqualWithSetAndList() throws Exception {
        Collection one = new HashSet();
        Collection two = new ArrayList();

        one.add("foo");
        one.add("bar");

        // Add in different order
        two.add("bar");
        two.add("foo");

        assertTrue(Util.isEqual(one, two));
        assertTrue(Util.isEqual(two, one));

        // here is where Util.isEqual differs: it doesn't care about the underlying implementation of the
        // Collection.
        assertFalse(one.equals(two));
        assertFalse(two.equals(one));
    }

    /**
     * This test insures that {@link Util#hashCode(java.util.Collection)} produces equal hash codes for Collections that
     * are equal according to {@link Util#isEqual(java.util.Collection, java.util.Collection)}.
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testHashCodeCompliesWithEqualsContract() throws Exception {
        Collection one = new HashSet();
        Collection two = new ArrayList();

        one.add("foo");
        one.add("bar");

        // Add in different order
        two.add("bar");
        two.add("foo");

        assertTrue(Util.isEqual(one, two));

        // They should have the same hash code
        assertEquals(Util.hashCode(one), Util.hashCode(two));
    }

}
