/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A map of parameters and their values needed in generating a package. This map
 * will need to be populated by the caller of {@code PackageGeneratorService}
 * </p>
 * <p>
 * The parameters included in this object vary depending on the packaging format
 * selected.
 * </p>
 * It is the responsibility of the specific PackageGeneratorImpl to know what parameters
 * it needs to generate a package and retrieve them from this {@code PackageGenerationParameters}
 * object.
 */
public class PackageGenerationParameters {
    /**
     * <p>
     * Map of package generation parameter names and their values.
     * </p>
     * If a parameter is included in this object, it will have at least one non-null,
     * non-empty value.
     */
    private Map<String, List<String>> params = new HashMap<>();

    /**
     * <p>
     * Adds new or updates an entry to the parameters map.  If the map previously contained a mapping for
     * the key, the old values will be replaced by the specified values.
     * </p>
     * If the specified values include null and/or empty strings, the null and/or empty strings will be removed
     * before the values list is mapped to the key.
     *
     * @param key name of a parameter
     * @param values list of values cannot be null or empty.
     */
    public void addParam(String key, List<String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Parameter's value cannot be null or empty");
        } else {
            List<String> trimmedValues = getTrimParamValues(values);

            if (trimmedValues.size() == 0) {
                throw new IllegalArgumentException("Parameter's value cannot be null or empty");
            }
            this.params.put(key, trimmedValues);
        }
    }

    /**
     * Adds new or updates a value to the specified parameter in the map. If a parameter with matching key exists in
     * in the map, new value will be added to the parameter's list of values. If a parameter with matching key does
     * not exist in the map, it will be created and added to the map with the specified value as initial value.
     *
     * @param key name of a parameter
     * @param value Parameter's value cannot be null or empty.
     */
    public void addParam(String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Parameter's value cannot be null or empty");
        }
        if (this.params.get(key) == null) {
            this.params.put(key, new ArrayList<>());
        }

        this.params.get(key).add(value);
    }

    /**
     * Removes a parameter and all of its values from the collection of parameters. If a parameter with matching
     * key does not exist, no operation will be performed.
     *
     * @param key name of a parameter to be removed
     */
    public void removeParam(String key) {
        this.params.remove(key);
    }

    /**
     * Retrieves set of keys contained in this object.
     * @return The set of keys for parameters of this object.
     */
    public Set<String> getKeys() {
        return params.keySet();
    }

    /**
     * Retrieves parameter's values based on given key (parameter's name).
     * @param key  the key
     * @return value of the matching key.
     */
    public List<String> getParam(String key) {
        return params.get(key);
    }

    /**
     * <p>
     * Returns a single String value at the specified index location
     * in the list of String associated with the given key.
     * </p>
     * <p>
     * This is useful in retrieving value for keys that are expected to
     * have only one value.
     * </p>
     * Returns null if no parameter exists with matching key.
     * @param key The key of the parameter to retrieve.
     * @param index The index location of the parameter to retrieve
     * @return The string representing the value of the parameter
     */
    public String getParam(String key, int index) {
        if (params.get(key) != null && params.get(key).size() > index) {
            return params.get(key).get(index);
        } else {
            return null;
        }
    }


    /**
     * Overrides this set of parameters with those in another parameters bundle.  Objects in this
     * set that are not in the other set will be left alone.  Objects in the other set that are not
     * in this set will be added.  Objects in both sets will be changed to the values in the other
     * set.
     * @param override The other parameter set to import values for.
     */
    public void overrideParams(PackageGenerationParameters override) {
        for (String key : override.getKeys()) {
            List<String> vals = override.getParam(key);
            if (vals != null && !vals.isEmpty()) {
                this.removeParam(key);
                for (String val : vals) {
                    this.addParam(key, val);
                }
            }
        }
    }

    /**
     * Removes null and empty strings from list of values.
     * @param values  the list of values
     * @return the trimmed list
     */
    private List<String> getTrimParamValues(List<String> values) {
        List<String> trimmedValues = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                trimmedValues.add(value);
            }
        }
        return trimmedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageGenerationParameters)) return false;

        PackageGenerationParameters that = (PackageGenerationParameters) o;

        if (params != null ? !params.equals(that.params) : that.params != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return params != null ? params.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PackageGenerationParameters{" +
                "params=" + params +
                '}';
    }
}
