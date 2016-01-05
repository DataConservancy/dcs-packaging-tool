package org.dataconservancy.packaging.tool.api;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;

/**
 * Service to display and parse property values.
 */
public interface PropertyFormatService {
    /**
     * Format property value as a string according to its type and hint.
     * 
     * @param prop
     *            The value of the property to format.
     * @return Formatted property value
     */
    String formatPropertyValue(Property prop);

    /**
     * Attempt to parse a string into a property value according to its type and
     * hint.
     * 
     * @param type
     *            The type of the property that's going to be parsed.
     * @param value
     *            The value of the property to be parsed.
     * @return value on success and null on failure
     */
    Property parsePropertyValue(PropertyType type, String value);
}
