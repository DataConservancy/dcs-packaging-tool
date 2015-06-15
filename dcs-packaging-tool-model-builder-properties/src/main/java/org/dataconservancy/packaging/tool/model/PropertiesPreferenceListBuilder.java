/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Serialize and deserialize {@code PackageGenerationPreferences} objects to and from preferences files using
 * {@link org.apache.commons.configuration.PropertiesConfiguration}.
 *
 */
public class PropertiesPreferenceListBuilder implements PackageGenerationPreferencesBuilder {


    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation expects a property's name to include two elements about a preference. These two elements are
     * to be separated by a dot ('.'). Element preceding the dot is the preference's name. Element following the dot is
     * the preference's valueType. No other dots are to be included in the properties name.
     * </p>
     * <p>
     * This implementation expects references to format-specific preferences files to be held in properties for
     * preferences whose name ends with {@code "-PGP-ref"}.
     * </p>
     * If any format-specific preferences files are detected, Attempts to load them will be made. {@code
     * PreferencesBuildException} is thrown if one these attempts fails.
     * @param is The input stream representing the preferences
     * @return Fully populated {@code PackageGenerationPreferences} object
     * @throws PreferencesBuildException if the preferences can not be loaded or read.
     */
    @Override
    public PackageGenerationPreferences buildPackageGenerationPreferences(InputStream is)
            throws PreferencesBuildException {
        PackageGenerationPreferences preferences = new PackageGenerationPreferences();
        PropertiesConfiguration props = new PropertiesConfiguration();
        try {
            props.load(new InputStreamReader(is));
        } catch (ConfigurationException e) {
            throw new PreferencesBuildException(e);
        }

        Iterator<String> keyIter = props.getKeys();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            String[] propertyKeyTokens = key.split("\\.");
            String parameterName = propertyKeyTokens[0];
            String parameterValueType = propertyKeyTokens[1];

            preferences.setPreference(
                    new Preference(parameterName, parameterValueType, Arrays.asList(props.getStringArray(key))));
        }

        //Retrieves list of properties which hold references to format-specific preferences files.
        List<String> formatSpecificFileRefs = preferences.getReferencesToFormatSpecificPreferences();

        //If properties which hold references for format-specific preferences files exist,
        //Loop through these references and load the files' content into format-specific preferences.
        if (formatSpecificFileRefs != null && !formatSpecificFileRefs.isEmpty()) {
            Map<String, Preference> formatSpecificPrefsMap = new HashMap<>();
            String formatId = null;
            //Look up preferences
            for (String refKey : formatSpecificFileRefs) {
                //retrieve file's path and name from general preferences:
                String fileName = preferences.getPreference(refKey).getValues().get(0);
                //Create a file object from the provide fileName
                //TODO: should this be passed URI to a file instead of file path? How to load a file from anywhere
                //in the files system?
                File file = new File(fileName);

                props = new PropertiesConfiguration();

                try {
                    props.load(new FileReader(file));
                } catch (ConfigurationException | FileNotFoundException e) {
                    //TODO: Should builder just log this failure and move on? instead of throwing exception?
                    throw new PreferencesBuildException("Attempt to load package generation preferences for a specific" +
                            "packaging format failed. " + e);
                }

                keyIter = props.getKeys();
                while (keyIter.hasNext()) {
                    String key = keyIter.next();
                    String[] propertyKeyTokens = key.split("\\.");
                    String parameterName = propertyKeyTokens[0];
                    String parameterValueType = propertyKeyTokens[1];

                    formatSpecificPrefsMap.put(parameterName,
                            new Preference(parameterName, parameterValueType, Arrays.asList(props.getStringArray(key))));

                    if (parameterName.equals("package-format")) {
                        formatId = formatSpecificPrefsMap.get(parameterName).getValues().get(0);
                    }
                }
                if (formatId != null && !formatId.isEmpty()) {
                    preferences.setPreferences(formatId, formatSpecificPrefsMap);
                }
            }
        } //else no further lookup will be performed.

        return  preferences;
    }


    @Override
    public void buildGeneralPreferences(PackageGenerationPreferences preferences, OutputStream os) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

    @Override
    public void buildFormatSpecificPreferences(PackageGenerationPreferences preferences, String formatId, OutputStream os) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

}


