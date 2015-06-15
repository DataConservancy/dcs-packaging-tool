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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Maintains a list of fields to filter.
 */
public class FieldFilter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Set<Field> fields = new HashSet<Field>();

    /**
     * Add a field to be ignored by this filter.
     *
     * @param f the field to isFiltered
     * @return this filter
     * @throws IllegalArgumentException if the field to be added is null
     */
    public FieldFilter addField(Field f) {
        if (f == null) {
            throw new IllegalArgumentException("Field must not be null.");
        }
        fields.add(f);
        return this;
    }

    /**
     * Returns true if the supplied field is filtered.
     *
     * @param candidate the candidate field
     * @return true if the supplied field is filtered
     */
    public boolean isFiltered(Field candidate) {
        return fields.contains(candidate);
    }

    /**
     * Filters the supplied Collection of fields.  The supplied collection <em>must</em> be mutable.  Specifically,
     * the Iterator returned by {@link Collection#iterator} must support {@link java.util.Iterator#remove()}
     *
     * @param toFilter the collection to be filtered
     * @throws IllegalArgumentException if the collection to be filtered is null
     */
    public void filter(Collection<Field> toFilter) {
        if (toFilter == null) {
            throw new IllegalArgumentException("The collection to be filtered must not be null.");
        }
        for (Iterator<Field> itr = toFilter.iterator(); itr.hasNext(); ) {
            Field candidate = itr.next();
            if (candidate == null) {
                continue;
            }
            if (isFiltered(candidate)) {
                log.trace("Filtering field {} {} {}",
                        new Object[]{candidate.getDeclaringClass().getName(), candidate.getType().getName(), candidate.getName()});
                itr.remove();
            }
        }
    }
}
