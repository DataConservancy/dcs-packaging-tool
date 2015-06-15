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

import java.lang.Object;import java.lang.String;import java.lang.System;import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Convenience methods to test for nullity or empty strings.
 */
public class Util {

    /**
     * The platform-specific new line character
     */
    public static final String NL = System.getProperty("line.separator");

    /**
     * Returns <code>true</code> if <code>s</code> is the empty string.  Returns
     * <code>false</code> if <code>s</code> is <code>null</code> or not empty.
     *
     * @param s a string
     * @return true if <code>s</code> is empty
     */
    public static boolean isEmpty(String s) {
        if (s == null) {
            return false;
        }
        return s.trim().length() == 0;
    }

    /**
     * Returns <code>true</code> if <code>o</code> is <code>null</code>.
     *
     * @param o a object
     * @return true if <code>o</code> is null
     */
    public static boolean isNull(Object o) {
        return o == null;
    }

    /**
     * Returns true if <code>s</code> is empty or <code>null</code>.
     *
     * @param s a string
     * @return true if <code>s</code> is empty or <code>null</code>.
     */
    public static boolean isEmptyOrNull(String s) {
        return isNull(s) || isEmpty(s);
    }

    /**
     * Returns true if {@code c} contains {@code null} references.
     *
     * @param c a Collection
     * @return true if <code>c</code> contains <code>null</code> references.
     */
    public static boolean containsNulls(Collection c) {
        for (Object o : c) {
            if (isNull(o)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if {@code array} contains {@code null} references.
     *
     * @param array an array
     * @return true if {@code array} contains {@code null} references.
     */
    public static boolean containsNulls(Object[] array) {
        for (Object o : array) {
            if (isNull(o)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if {@code c} contains empty Strings or {@code null} references.
     *
     * @param c a Collection
     * @return true if {@code c} contains empty Strings or {@code null} references.
     */
    public static boolean containsNullsOrEmptyStrings(Collection<String> c) {
        for (String s : c) {
            if (isEmptyOrNull(s)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if {@code array} contains empty Strings or {@code null} references.
     *
     * @param array an array
     * @return true if {@code array} contains empty Strings or {@code null} references.
     */
    public static boolean containsNullsOrEmptyStrings(String[] array) {
        for (String s : array) {
            if (isEmptyOrNull(s)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Copies the source Collection to the supplied destination Collection.  Each item in the source Collection is
     * deeply copied before being placed in the destination Collection.
     *
     * @param src the source Collection
     * @param dst the destination Collection
     * @param <T> the type of objects contained in the Collection
     */
    public static <T> void deepCopy(Collection<T> src, Collection<T> dst) {
        for (T item : src) {
            T copiedItem = CopyConstructorFactory.copy(item);
            dst.add(copiedItem);
        }
    }

    /**
     * Calculates the equality of two collections disregarding the underlying implementation.  Specifically this allows
     * (for example) a {@code Collection} implemented using an {@code ArrayList} and a {@code Collection} implemented
     * using a {@code HashSet} to be compared for equality.
     * <p>
     * This method uses {@link Collection#containsAll(java.util.Collection)} to determine equality of the supplied
     * collections.
     *
     * @param one a Collection
     * @param two a Collection
     * @return true if the collections are equal
     */
    public static boolean isEqual(Collection one, Collection two) {
        if (one == null && two != null) {
           return false;
        }

        if (one != null && two == null) {
            return false;
        }

        if (one == two) {
            return true;
        }

        return one.containsAll(two) && two.containsAll(one);
    }

    /**
     * Computes a hash code over {@code c} independent of the underlying {@code Collection} implementation.
     * <p>
     * It is the intent of this method to comply with the {@link Object#hashCode()} contract.
     *
     * @param c any Collection
     * @return the hash code of the Collection
     */
    public static int hashCode(Collection c) {
        int[] codes = new int[c.size()];

        // Different implementations will return an Iterator that returns hash codes in different orders
        // We keep the hash codes, order them, and then compute the final hash code.
        Iterator itr = c.iterator();
        int i = 0;
        while (itr.hasNext()) {
            codes[i++] = itr.next().hashCode();
        }

        Arrays.sort(codes);

        int prime = 53;
        int result = prime;

        for (int code : codes) {
            result = prime * result + code;
        }

        return result;
    }

}
