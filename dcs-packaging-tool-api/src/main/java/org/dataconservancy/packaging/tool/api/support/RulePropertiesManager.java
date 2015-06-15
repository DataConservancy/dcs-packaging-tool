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

package org.dataconservancy.packaging.tool.api.support;

import java.util.Map;

import org.dataconservancy.packaging.tool.model.description.RulesSpec;

public interface RulePropertiesManager {

    /**
     * Initialize based on a set of rules.
     * <p>
     * Scans the rule set for properties that need to be provided externally.
     * </p>
     * 
     * @param rules
     *        Rules to initialize the manager.
     */
    public void init(RulesSpec rules);

    /**
     * Determine if any rules reference undefined properties.
     * 
     * @return true if there are any undefined properties.
     */
    public boolean hasUndefinedProperties();

    /**
     * Return the key and description of undefined properties./
     * 
     * @return Map containing property names (key) and descriptions (value).
     */
    public Map<String, String> getUndefinedProperties();
    
    /** Returns the key and description of all properties.
     * 
     * @return Map containing property names (key) and descriptions (value).
     */
    public Map<String, String> getAllProperties();

    /**
     * Set the value of a rule property.
     * <p>
     * Implementations may choose to persist these values, using a means of
     * their own choosing.
     * </p>
     * 
     * @param key
     *        Name of the property.
     * @param value
     *        Desired value.
     */
    public void setProperty(String key, String value);

    /**
     * Ignore undefined properties.
     * 
     * @param ignore
     *        True if the manager should ignore undefined properties.
     */
    public void setIgnoreUndefinedProperties(boolean ignore);
}
