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

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PropertiesPreferenceListBuilderTest {

    private PackageGenerationPreferencesBuilder preferences_builder;
        
    @Before
    public void setUp() {
        preferences_builder = new PropertiesPreferenceListBuilder();
    }

    @Test
    public void testBuildPreferencesFromInputStream() throws PreferencesBuildException {
        InputStream in = getClass().getResourceAsStream("/samplePackageGenPreferences.properties");
        PackageGenerationPreferences preferences = preferences_builder.buildPackageGenerationPreferences(in);

        assertEquals(7, preferences.getPreferencesKeys().size());
        //assertEquals(8, preferences.getPreferencesKeys().size());
        //assertEquals(1, preferences.getReferencesToFormatSpecificPreferences().size());

    }

    @Ignore
    @Test
    public void testBuildGeneralPGPFile() throws FileNotFoundException {

        PackageGenerationPreferences preferences = new PackageGenerationPreferences();
        //TODO - fully hydrate PackageGenerationPreferences object

        FileOutputStream fileOut = new FileOutputStream("/tmp/testBuildGeneratlPGPFile.properties");
        preferences_builder.buildGeneralPreferences(preferences, fileOut);

        //TODO deserialize the fileOut content and make assertions

    }

    @Ignore
    @Test
    public void testBuildFormatSpecificPGPFile() throws FileNotFoundException {

        PackageGenerationPreferences preferences = new PackageGenerationPreferences();
        //TODO - fully hydrate PackageGenerationPreferences object including the format specific preferences.

        FileOutputStream fileOut = new FileOutputStream("/tmp/testBuildFormatSpecificPGPFile.properties");
        preferences_builder.buildGeneralPreferences(preferences, fileOut);

        //TODO deserialize the fileOut content and make assertions

    }
}
