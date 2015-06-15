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

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageGenerationParametersTest {

    /**
     * Test that information can be put in and retrieved from a
     * {@code PackageGenerationParameters} object
     */
    @Test
    public void testAddAndGetParams() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();
        Map<String, List<String>> addedParameters = new HashMap<String, List<String>>();

        List<String> paramValues = new ArrayList<String>();
        paramValues.add("Willard Poopa-doodle");
        parameters.addParam(BagItParameterNames.CONTACT_NAME, paramValues);
        addedParameters.put(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("w.poopadoodle@brownhound.net");
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);
        addedParameters.put(BagItParameterNames.CONTACT_EMAIL, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("org.dataconservancy.packaging.format:borem");
        parameters.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);
        addedParameters.put(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        //assert that there are 3 entry in the parameters map.
        assertEquals(3, parameters.getKeys().size());
        assertEquals(addedParameters.keySet(), parameters.getKeys());

        for (String key : addedParameters.keySet()){
            assertEquals(addedParameters.get(key),parameters.getParam(key));
        }
    }

    /**
     * Test that empty list of value(s) cannot be put into a
     * {@code BagItParameterNames} object
     */
    @Test (expected = IllegalArgumentException.class)
    public void testSetEmptyValuesListOnParams() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();
        Map<String, List<String>> newParameters = new HashMap<String, List<String>>();

        //Test add an empty list values:
        List<String> paramValues = new ArrayList<String>();
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);
    }

    /**
     * Test that null list of value(s) cannot be put into a
     * {@code PackageGenerationParameters} object
     */
    @Test (expected = IllegalArgumentException.class)
    public void testSetNullValuesListOnParams() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();
        Map<String, List<String>> newParameters = new HashMap<String, List<String>>();

        //Test add an empty list values:
        List<String> paramValues = null;
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);
    }

    /**
     * Test that a list of empty values cannot be put into a
     * {@code PackageGenerationParameters} object
     */
    @Test (expected = IllegalArgumentException.class)
    public void testSetListOfEmptyValuesOnParams() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();
        Map<String, List<String>> newParameters = new HashMap<String, List<String>>();

        //Test add an empty list values:
        List<String> paramValues = new ArrayList<String>();
        paramValues.add("");
        paramValues.add("");
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);
    }

    /**
     * Test that a list of null values cannot be put into a
     * {@code PackageGenerationParameters} object
     */
    @Test (expected = IllegalArgumentException.class)
    public void testSetListOfNullValuesOnParams() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();
        Map<String, List<String>> newParameters = new HashMap<String, List<String>>();

        //Test add an empty list values:
        List<String> paramValues = new ArrayList<String>();
        paramValues.add(null);
        paramValues.add(null);
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);
    }

    /**
     * Test getting a specific parameter value.
     */
    @Test
    public void testGetParamValue() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();

        //Test add an empty list values:
        List<String> paramValues = new ArrayList<String>();
        paramValues.add(null);
        paramValues.add("willardandfriends@gangsterhounds.com");
        paramValues.add("alfred@theduck.com");
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        assertNotNull(parameters.getParam(BagItParameterNames.CONTACT_EMAIL));
        assertTrue(paramValues.containsAll(parameters.getParam(BagItParameterNames.CONTACT_EMAIL)));

        assertEquals(paramValues.get(2), parameters.getParam(BagItParameterNames.CONTACT_EMAIL, 1));
    }

    /**
     * Test getting value for a non-existing param
     */
    @Test
    public void testGetNonExistentParamValue() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();

        //Test add an empty list values:
        List<String> paramValues = new ArrayList<String>();
        paramValues.add(null);
        paramValues.add("willardandfriends@gangsterhounds.com");
        paramValues.add("alfred@theduck.com");
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        assertNotNull(parameters.getParam(BagItParameterNames.CONTACT_EMAIL));
        assertTrue(paramValues.containsAll(parameters.getParam(BagItParameterNames.CONTACT_EMAIL)));

        assertEquals(paramValues.get(2), parameters.getParam(BagItParameterNames.CONTACT_EMAIL, 1));

        assertNull(parameters.getParam(BagItParameterNames.CONTACT_NAME, 0));
    }

    /**
     * Test that empty and null values get removed from parameters values.
     */
    @Test
    public void testEmptyNullValuesGetWeededOut() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();

        //Test add an empty list values:
        List<String> paramValues = new ArrayList<String>();
        paramValues.add(null);
        paramValues.add(" ");
        paramValues.add("alfred@theduck.com");
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        assertNotNull(parameters.getParam(BagItParameterNames.CONTACT_EMAIL));
        assertTrue(paramValues.containsAll(parameters.getParam(BagItParameterNames.CONTACT_EMAIL)));

        assertEquals(1, parameters.getParam(BagItParameterNames.CONTACT_EMAIL).size());
        assertEquals(paramValues.get(2), parameters.getParam(BagItParameterNames.CONTACT_EMAIL, 0));
    }


    /**
     * Test adding single-value parameter
     */
    @Test
    public void testAddSingleValueParameters() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();

        String paramValue = "Alfee@McDuck.com";

        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValue );

        assertNotNull(parameters.getParam(BagItParameterNames.CONTACT_EMAIL));
        assertEquals(paramValue, parameters.getParam(BagItParameterNames.CONTACT_EMAIL, 0));
    }

    /**
     * Test adding additional value to existing parameter
     */
    @Test
    public void testAddValueToExistingParameters() {
        PackageGenerationParameters parameters = new PackageGenerationParameters();

        String paramValue = "Alfee@McDuck.com";
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, paramValue);

        assertNotNull(parameters.getParam(BagItParameterNames.CONTACT_EMAIL));
        assertEquals(paramValue, parameters.getParam(BagItParameterNames.CONTACT_EMAIL, 0));
        assertEquals(1, parameters.getParam(BagItParameterNames.CONTACT_EMAIL).size());

        String additionalParamValue = "Willard@HoundDogz.com";
        parameters.addParam(BagItParameterNames.CONTACT_EMAIL, additionalParamValue);
        assertEquals(2, parameters.getParam(BagItParameterNames.CONTACT_EMAIL).size());
        assertTrue(parameters.getParam(BagItParameterNames.CONTACT_EMAIL).contains(paramValue));
        assertTrue(parameters.getParam(BagItParameterNames.CONTACT_EMAIL).contains(additionalParamValue));


    }

    @Test
    public void testEquals() {
        PackageGenerationParameters parametersOne = new PackageGenerationParameters();

        List<String> paramValues = new ArrayList<String>();
        paramValues.add("Willard Poopa-doodle");
        parametersOne.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("w.poopadoodle@brownhound.net");
        parametersOne.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("org.dataconservancy.packaging.format:borem");
        parametersOne.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        PackageGenerationParameters parametersTwo = new PackageGenerationParameters();

        paramValues = new ArrayList<String>();
        paramValues.add("Willard Poopa-doodle");
        parametersTwo.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("w.poopadoodle@brownhound.net");
        parametersTwo.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("org.dataconservancy.packaging.format:borem");
        parametersTwo.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        assertEquals(parametersOne, parametersTwo);

    }

    @Test
    public void testNotEquals() {
        PackageGenerationParameters parametersOne = new PackageGenerationParameters();

        List<String> paramValues = new ArrayList<String>();
        paramValues.add("Willard Poopa-doodle");
        parametersOne.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("w.poopadoodle@brownhound.net");
        parametersOne.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("org.dataconservancy.packaging.format:borem");
        parametersOne.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        PackageGenerationParameters parametersTwo = new PackageGenerationParameters();

        paramValues = new ArrayList<String>();
        paramValues.add("Willard Poopa-doodle");
        parametersTwo.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("org.dataconservancy.packaging.format:borem");
        parametersTwo.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        assertTrue(!parametersOne.equals(parametersTwo));

        PackageGenerationParameters parametersThree= new PackageGenerationParameters();

        paramValues = new ArrayList<String>();
        paramValues.add("Willard The Shinny");
        parametersThree.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("w.poopadoodle@brownhound.net");
        parametersThree.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("org.dataconservancy.packaging.format:borem");
        parametersThree.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        assertTrue(!parametersOne.equals(parametersThree));

    }

    @Test
    public void testRemoveParam() {
        PackageGenerationParameters parametersOne = new PackageGenerationParameters();

        List<String> paramValues = new ArrayList<String>();
        paramValues.add("Willard Poopa-doodle");
        parametersOne.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("w.poopadoodle@brownhound.net");
        parametersOne.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add("org.dataconservancy.packaging.format:borem");
        parametersOne.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        //make sure the above params were added
        assertTrue(parametersOne.getKeys().contains(BagItParameterNames.CONTACT_NAME));
        assertTrue(parametersOne.getKeys().contains(BagItParameterNames.CONTACT_EMAIL));
        assertTrue(parametersOne.getKeys().contains(BagItParameterNames.PACKAGE_FORMAT_ID));

        //remove one param
        parametersOne.removeParam(BagItParameterNames.PACKAGE_FORMAT_ID);

        //make sure that the remove params is no longer there.
        assertTrue(parametersOne.getKeys().contains(BagItParameterNames.CONTACT_NAME));
        assertTrue(parametersOne.getKeys().contains(BagItParameterNames.CONTACT_EMAIL));
        assertFalse(parametersOne.getKeys().contains(BagItParameterNames.PACKAGE_FORMAT_ID));

        //make sure that attempt to remove param that doesn't exist does not cause exception
        //remove one param
        parametersOne.removeParam(BagItParameterNames.PACKAGE_FORMAT_ID);

        //make sure that the remove params is no longer there.
        assertTrue(parametersOne.getKeys().contains(BagItParameterNames.CONTACT_NAME));
        assertTrue(parametersOne.getKeys().contains(BagItParameterNames.CONTACT_EMAIL));

    }


    @Test
    public void testOverride() {
        PackageGenerationParameters parametersOne = new PackageGenerationParameters();

        String contactOne = "Willard Poopa-doodle";
        String contactTwo = "Some Random Guy";

        String emailOne = "w.poopadoodle@brownhound.net";
        String emailTwo = "some.random@randomguyemailfake.com";

        String formatId = "org.dataconservancy.packaging.format:borem";

        List<String> paramValues = new ArrayList<String>();
        paramValues.add(contactOne);
        parametersOne.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add(emailOne);
        parametersOne.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add(formatId);
        parametersOne.addParam(BagItParameterNames.PACKAGE_FORMAT_ID, paramValues);

        PackageGenerationParameters parametersTwo = new PackageGenerationParameters();

        paramValues = new ArrayList<String>();
        paramValues.add(contactTwo);
        parametersTwo.addParam(BagItParameterNames.CONTACT_NAME, paramValues);

        paramValues = new ArrayList<String>();
        paramValues.add(emailTwo);
        parametersTwo.addParam(BagItParameterNames.CONTACT_EMAIL, paramValues);

        parametersOne.overrideParams(parametersTwo);

        assertEquals(contactTwo, parametersOne.getParam(BagItParameterNames.CONTACT_NAME).get(0));
        assertEquals(emailTwo, parametersOne.getParam(BagItParameterNames.CONTACT_EMAIL).get(0));
        assertEquals(formatId, parametersOne.getParam(BagItParameterNames.PACKAGE_FORMAT_ID).get(0));
    }


}

