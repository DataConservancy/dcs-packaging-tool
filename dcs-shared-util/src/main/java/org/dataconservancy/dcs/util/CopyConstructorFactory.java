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

import java.lang.Class;import java.lang.Exception;import java.lang.RuntimeException;import java.lang.String;import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates copies of DCS data model objects by reflectively invoking their copy constructor.
 */
public class CopyConstructorFactory {

    private static final String EXE_MSG = "Attempting to construct a copy of %s using its copy constructor failed: %s";

    /**
     * Create a copy of the supplied object by reflectively invoking its copy constructor.  The intent is that a new
     * instance of the supplied object is returned, with state equivalent to the supplied object, but this method does
     * not make that guarantee.
     * <p>
     * That intent is realized by the implementation of the object's copy constructor; that is, if the copy constructor
     * doesn't correctly copy state, then the object returned from this method will not have state equivalent to the
     * supplied object.
     * <p>
     * If there is a problem invoking the copy constructor, a RuntimeException is thrown.
     *
     * @param object the object to copy
     * @param <T> the type of the object
     * @return a new instance of the supplied object, ideally equivalent to the supplied object
     */
    public static <T> T copy(T object) {
        Class<T> classOfObject = (Class<T>) object.getClass();
        try {
            Constructor<T> copyConstructor = classOfObject.getConstructor(classOfObject);
            return copyConstructor.newInstance(object);
        } catch (Exception e) {
            throw new RuntimeException(String.format(EXE_MSG, classOfObject.getName(), e.getMessage()), e);
        }
    }
}
