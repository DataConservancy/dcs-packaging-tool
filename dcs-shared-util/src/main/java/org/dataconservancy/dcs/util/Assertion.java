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

import java.util.Collection;

/**
 * Common assertions.  {@link IllegalArgumentException}s are thrown if an assertion is violated.
 */
public class Assertion {

    /**
     * Throws {@link IllegalArgumentException} if <code>s</code> is <code>null</code> or the empty string.
     *
     * @param s a string
     * @return the string
     */
    static public String notEmptyOrNull(String s) {
        final String msg = "String must not be empty or null.";
        if (Util.isEmptyOrNull(s)) {
            throw new IllegalArgumentException(msg);
        }
        return s;
    }

    /**
     * Throws {@link IllegalArgumentException} if <code>o</code> is <code>null</code>.
     *
     * @param o an object
     * @return the object
     */
    static public Object notNull(Object o) {
        final String msg = "Object must not be null";
        if (Util.isNull(o)) {
            throw new IllegalArgumentException(msg);
        }
        return o;
    }

    /**
     * Throws {@link IllegalArgumentException} if <code>c</code> contains <code>null</code> references.
     *
     * @param c a Collection
     * @return the Collection
     */
    static public Collection doesNotContainNull(Collection c) {
        final String msg = "Collection must not contain null references.";
        notNull(c);
        if (Util.containsNulls(c)) {
            throw new IllegalArgumentException(msg);
        }
        return c;
    }

    /**
     * Throws {@link IllegalArgumentException} if <code>members</code> contains <code>null</code> references.
     *
     * @param <T> the type of the array
     * @param members an array of &gt;T&lt;
     * @return the array
     */
    static public <T> T[] doesNotContainNull(T[] members) {
        final String msg = "Array must not contain null references.";
        notNull(members);
        if (Util.containsNulls(members)) {
            throw new IllegalArgumentException(msg);
        }
        return members;
    }

    /**
     * Throws {@link IllegalArgumentException} if <code>c</code> contains <code>null</code> references or
     * empty strings.
     *
     * @param c a Collection
     * @return the Collection
     */
    static public Collection<String> doesNotContainNullOrEmptyString(Collection<String> c) {
        final String msg = "Collection must not contain null references or empty Strings";
        notNull(c);
        if (Util.containsNullsOrEmptyStrings(c)) {
            throw new IllegalArgumentException(msg);
        }
        return c;
    }

    /**
     * Throws {@link IllegalArgumentException} if <code>s</code> contains <code>null</code> references or empty
     * strings.
     *
     * @param s an array of Strings
     * @return the Collection
     */
    static public String[] doesNotContainNullOrEmptyString(String[] s) {
        final String msg = "Array must not contain null references or empty Strings";
        notNull(s);
        if (Util.containsNullsOrEmptyStrings(s)) {
            throw new IllegalArgumentException(msg);
        }
        return s;
    }
    
}
