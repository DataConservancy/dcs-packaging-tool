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

import java.lang.IllegalArgumentException;import java.lang.Object;import java.lang.Override;import java.lang.String;import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * Compares two {@link Field}s for equality.  If two fields are not equal according to {@link Field#equals( Object )},
 * the declaring class name, field name, and type are subsequently compared using {@link String#compareTo(Object)}.
 */
public class DefaultFieldComparator implements Comparator<Field> {

    /**
     * Compares two fields for equality: fields must be in the same {@link java.lang.reflect.Field#getDeclaringClass() declaring class},
     * have the same {@link java.lang.reflect.Field#getName() name}, and have the same {@link java.lang.reflect.Field#getType() type} to be
     * considered equal.
     * <p>
     * <em><strong>N.B.</strong></em> When testing the type, generics are not taken into consideration.
     * <em><strong>N.B.</strong></em> Field value is not taken into consideration.
     * <p>
     * This comparator delegates to the behavior of {@link Field#equals(Object)} and {@link String#compareTo(Object)}.
     * <p>
     * This comparator does not allow null objects, and will throw IllegalArgumentException if they are present.
     *
     * @param one the first field
     * @param two the second field
     * @return 0 if the fields are equal.
     * @throws IllegalArgumentException if either field is null
     */
    @Override
    public int compare(Field one, Field two) {

        if (one == null) {
            throw new IllegalArgumentException("Field one cannot be null.");
        }

        if (two == null) {
            throw new IllegalArgumentException("Field two cannot be null.");
        }

        if (one.equals(two)) {
            return 0;
        }

        // Fields must be the the same type, have the same name, and be in the same declaring class to be equal
        final int classResult = one.getDeclaringClass().getName().compareTo(two.getDeclaringClass().getName());

        if (classResult != 0) {
            return classResult;
        }

        final int typeResult = one.getType().getName().compareTo(two.getType().getName()); // note this doesn't handle generics

        if (typeResult != 0) {
            return typeResult;
        }

        final int nameResult = one.getName().compareTo(two.getName());

        if (nameResult != 0) {
            return nameResult;
        }

        return 0; // declaring class, field type, and field name are equal according to String.compareTo.
    }
}
