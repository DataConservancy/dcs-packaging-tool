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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO: this test suite is written to demonstrate how PackageGenerationPreferences can be used. More thorough test
 * would be needed when this model is actually being worked on as part of a JIRA ticket.
 */
public class PackageGenerationPreferencesTest {

    PackageGenerationPreferences underTest;
    Preference packageName;
    Preference packageCreatorName;
    Preference packageCreatorEmail;
    Preference boremPreferencesRef;

    Preference checksumAlgs;

    String boremFormatId = "org.dataconvervancy.packaging.format:borem";

    @Before
    public void setUp() {
        underTest = new PackageGenerationPreferences();
        packageName = new Preference("package-name", Preference.Type.FreeText.stringValue(), "My Test Package");
        packageCreatorName = new Preference("package-creator-name", Preference.Type.FreeText.stringValue(), "Hanh Vu");
        packageCreatorEmail = new Preference("package-creator-email", Preference.Type.Email.stringValue(), "abc@cdef.com");

        checksumAlgs = new Preference("checksum-algorithms", Preference.Type.EnumeratedMultiple.stringValue(), "md5");

        boremPreferencesRef = new Preference("borem-preferences-ref", Preference.Type.EnumeratedSingle.stringValue(), boremFormatId);
    }

    @Test
    public void testSetAndGetGeneralPreferences() {
        underTest.setPreference(packageName);
        underTest.setPreference(packageCreatorName);
        underTest.setPreference(packageCreatorEmail);

        //assert that underTest now contain 3 preferences.
        assertEquals(3, underTest.getPreferencesKeys().size());

        assertEquals(packageName, underTest.getPreference(packageName.getName()));
        assertEquals(packageCreatorName, underTest.getPreference(packageCreatorName.getName()));
        assertEquals(packageCreatorEmail, underTest.getPreference(packageCreatorEmail.getName()));
    }

    @Ignore
    @Test
    public void testSetNullParameter() {
        //TODO: expects exception
    }

    @Ignore
    @Test
    public void testSetParameterWithEmptyName() {
        //TODO: expects exception
    }

    @Test
    public void testUpdateGeneralPreference () {
        underTest.setPreference(packageName);

        //assert that underTest now contains 1 preference.
        assertEquals(1, underTest.getPreferencesKeys().size());

        //assert that underTest contains packageName preference
        assertEquals(packageName, underTest.getPreference(packageName.getName()));

        //update package name preference.
        Preference newPackageName = new Preference("package-name", Preference.Type.FreeText.stringValue(), "Willard's christmas package");
        underTest.setPreference(newPackageName);

        //assert that underTest still contains 1 preference.
        assertEquals(1, underTest.getPreferencesKeys().size());
        //assert that underTest no longer has packageName preference
        assertFalse(packageName.equals(underTest.getPreference(packageName.getName())));
        //assert that underTest now contains newPackageName preference
        assertEquals(newPackageName, underTest.getPreference(packageName.getName()));
    }

    @Test
    public void testRemoveGeneralPreference() {
        underTest.setPreference(packageName);
        underTest.setPreference(packageCreatorName);
        underTest.setPreference(packageCreatorEmail);

        //assert that underTest now contain 3 preferences.
        assertEquals(3, underTest.getPreferencesKeys().size());

        assertEquals(packageName, underTest.getPreference(packageName.getName()));
        assertEquals(packageCreatorName, underTest.getPreference(packageCreatorName.getName()));
        assertEquals(packageCreatorEmail, underTest.getPreference(packageCreatorEmail.getName()));

        underTest.removePreference(packageName.getName());

        //assert that underTest now contain 3 preferences.
        assertEquals(2, underTest.getPreferencesKeys().size());

        assertFalse(packageName.equals(underTest.getPreference(packageName.getName())));
        assertEquals(packageCreatorName, underTest.getPreference(packageCreatorName.getName()));
        assertEquals(packageCreatorEmail, underTest.getPreference(packageCreatorEmail.getName()));

    }

    /**
     * Test removing non-existing preferences does not throw exception.
     */
    @Test
    public void testRemovingNonExistingPreference() {
        underTest.setPreference(packageName);
        underTest.setPreference(packageCreatorName);
        underTest.setPreference(packageCreatorEmail);

        //assert that underTest now contain 3 preferences.
        assertEquals(3, underTest.getPreferencesKeys().size());

        assertEquals(packageName, underTest.getPreference(packageName.getName()));
        assertEquals(packageCreatorName, underTest.getPreference(packageCreatorName.getName()));
        assertEquals(packageCreatorEmail, underTest.getPreference(packageCreatorEmail.getName()));

        underTest.removePreference("Bogus hogus");
        //assert that underTest now contain 3 preferences.
        assertEquals(3, underTest.getPreferencesKeys().size());

        assertEquals(packageName, underTest.getPreference(packageName.getName()));
        assertEquals(packageCreatorName, underTest.getPreference(packageCreatorName.getName()));
        assertEquals(packageCreatorEmail, underTest.getPreference(packageCreatorEmail.getName()));
    }

    @Ignore
    @Test
    public void testSetAndGetFormatSpecificPreferences() {
        //TODO
    }

    @Ignore
    @Test
    public void testSetNullFormatSpecificPreference() {
        //TODO
    }

    @Ignore
    @Test
    public void testSetFormatSpecificPreferenceWithEmptyName() {
        //TODO
    }

    @Ignore
    @Test
    public void testUpdateFormatSpecificPreference() {
        //TODO:
    }

    @Ignore
    @Test
    public void testRemoveFormatSpecificPreference() {
        //TODO
    }

}
