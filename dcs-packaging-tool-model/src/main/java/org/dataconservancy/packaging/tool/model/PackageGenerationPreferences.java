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
 * {@code PackageGenerationPreferences} describes package generation parameters. The {@code
 * PackageGenerationPreferences} object is used to inform users of the parameters involved in creating a package, as
 * well as elicit user's input for the values of these parameters.
 * </p>
 * <p>
 * A {@code PackageGenerationPreferences} object is capable of containing both general package generation preferences
 * and format-specific package generation preferences. These sets of preferences have to be populated separately, with
 * the general preferences being populated first. References to format-specific preferences files are included in the
 * general preferences and have to be resolved after general preferences are loaded.
 * </p>
 * Null preferences and preferences with empty or null names cannot be added to {@code PackageGenerationPreferences},
 *
 */
public class PackageGenerationPreferences {

    private static final String FMT_SPECIFIC_PREFS_REFS_SUFFIX = "-PGP-ref";

    private static final String PACKAGING_FMT_PREF_NAME = "packaging-format";

    /**
     * Map of general package generation generatPreferences keys and their content.
     * <p/>
     * Preferences map is keyed by the name of parameter described by the preference, aka preference name.
     */
    private Map<String, Preference> generalPreferences = new HashMap<>();

    /**
     * Map of packaging format ids and their related preferences.
     */
    private Map<String, Map<String, Preference>> formatSpecificPreferences
            = new HashMap<>();

    /**
     * Retrieves a specific preference base on the preference name.
     * @param prefName the preference name
     * @return  a specific preference base
     */
    public Preference getPreference(String prefName) {
        return generalPreferences.get(prefName);
    }

    /**
     * Adds or update a preference. If a mapping exists for a preference key, it will be replaced by the specified
     * preference.
     * @param preference The preference to add or set in the Generation Preferences
     */
    public void setPreference(Preference preference) {
        if (preference == null) {
            throw new IllegalArgumentException("Preference may not be null");
        }

        generalPreferences.put(preference.getName(), preference);
    }

    /**
     * Retrieves a set of names of the parameters described in the general preferences.
     * @return The set of keys for all the preferences that make up this PackageGenerationPreferences instance.
     */
    public Set<String> getPreferencesKeys() {
        return generalPreferences.keySet();
    }

    /**
     * Set a new preference to the format-specific preferences based on the package format specified by
     * the format id, {@code formatId}.
     * @param formatId the format id
     * @param preference the preferecne
     */
    public void setPreference(String formatId, Preference preference) {
        if (formatId == null || formatId.isEmpty()) {
            throw new IllegalArgumentException("Packaging format id may not be null or empty");
        }
        if (preference == null) {
            throw new IllegalArgumentException("Preference may not be null");
        }

        if (formatSpecificPreferences.get(formatId) == null) {
            formatSpecificPreferences.put(formatId, new HashMap<>());
        }
        formatSpecificPreferences.get(formatId).put(preference.getName(), preference);
    }

    /**
     * Adds or update a map of format-specific preferences by format id.
     * @param formatId the format id
     * @param preferences the preference
     */
    public void setPreferences(String formatId, Map<String, Preference> preferences) {
        formatSpecificPreferences.put(formatId, preferences);
    }

    /**
     * Retrieves a preference by name from a format-specific set of preferences.
     * @param formatId id of the format by which the format-specific preferences are grouped.
     * @param prefName name of the preference whose content is being request.
     * @return  a preference by name from a format-specific set of preferences
     */
    public Preference getPreference(String formatId, String prefName) {
        return formatSpecificPreferences.get(formatId).get(prefName);
    }

    /**
     * Retrieves a set of names of the parameters described in the specified format's preferences.
     * @param formatId The id of the format to retrieve preference names for.
     * @return A set of all the preference names associated with the given format id.
     */
    public Set<String> getPreferencesKeys(String formatId) {
        return formatSpecificPreferences.get(formatId).keySet();
    }

    /**
     * Removes a preference by name from the general preferences, if one with matching name exists,
     * @param prefName the preference name
     */
    public void removePreference(String prefName) {
        this.generalPreferences.remove(prefName);
    }

    /**
     * Removes a preferences by name from the specified format-specific preferences, if one with matching name exists,
     * @param formatId  the format id
     * @param prefName  the preference name
     */
    public void removePreference(String formatId, String prefName) {
        this.formatSpecificPreferences.get(formatId).remove(prefName);
    }

    /**
     * Retrieves a list of names for properties whose value are references to format-specific preferences files.
     * @return a list of names for properties
     */
    public List<String> getReferencesToFormatSpecificPreferences() {
        List<String> references = new ArrayList<>();
        for (String prefName : getPreferencesKeys()) {
            if (prefName.endsWith(FMT_SPECIFIC_PREFS_REFS_SUFFIX)) {
               references.add(prefName);
            }
        }
        return references;
    }

    @Override
    public String toString() {
        return "PackageGenerationPreferences{" +
                "generalPreferences=" + generalPreferences +
                ", formatSpecificPreferences=" + formatSpecificPreferences +
                '}';
    }
}
