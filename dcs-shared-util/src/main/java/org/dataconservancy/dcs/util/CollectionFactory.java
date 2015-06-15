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

import java.util.ArrayList;
import java.util.Collection;

/**
 * A factory class that produces new, concrete, instances of {@link Collection} used as the backing data structure
 * for the DCS data model.
 */
public class CollectionFactory {

    public static <T> Collection<T> newCollection() {
        return newCollection(10);
    }

    public static <T> Collection<T> newCollection(int initialSize) {
        return new ArrayList<T>(initialSize);
    }
}
