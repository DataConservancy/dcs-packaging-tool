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
package org.dataconservancy.packaging.gui.model;

import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the serialization and deserialization of relationship groups to and from json.
 */
public class UserDefinedPropertyGroupJSONBuilderTest {

    private List<UserDefinedPropertyGroup> testGroups;
    private PropertyType propertyTypeOne;
    private PropertyType propertyTypeTwo;

    private String serializedGroup;

    @Before
    public void setup() throws URISyntaxException {
        propertyTypeOne = new PropertyType();
        propertyTypeOne.setLabel("one");
        propertyTypeOne.setDescription("descriptionOne");
        propertyTypeOne.setDomainPredicate(new URI("uri:one"));
        propertyTypeOne.setPropertyValueType(PropertyValueType.URI);

        propertyTypeTwo = new PropertyType();
        propertyTypeTwo.setLabel("two");
        propertyTypeTwo.setDescription("desciptionTwo");
        propertyTypeTwo.setDomainPredicate(new URI("uri:two"));
        propertyTypeTwo.setPropertyValueType(PropertyValueType.STRING);

        List<PropertyType> propertyList = new ArrayList<>();
        propertyList.add(propertyTypeOne);
        propertyList.add(propertyTypeTwo);

        UserDefinedPropertyGroup testGroup = new UserDefinedPropertyGroup("group", "group description", "http://testgroup.org", propertyList);

        testGroups = new ArrayList<>();
        testGroups.add(testGroup);

        serializedGroup = "[{\n" +
            "  \"description\": \"" + testGroup.getDescription() + "\",\n" +
            "  \"label\": \"" + testGroup.getLabel() + "\",\n" +
            "  \"groupURL\": \"" + testGroup.getGroupUrl() + "\",\n" +
            "  \"propertyTypes\": [\n" +
            "    {\n" +
                 "   \"description\": \"" + propertyTypeOne.getDescription() + "\",\n" +
                 "   \"requiresURI\": " + true + ",\n" +
                 "   \"label\": \"" + propertyTypeOne.getLabel() + "\",\n" +
                 "   \"predicate\": \"" + propertyTypeOne.getDomainPredicate() + "\"\n" +
            "    },\n" +
            "    {\n" +
                 "   \"description\": \"" + propertyTypeTwo.getDescription() + "\",\n" +
                 "   \"requiresURI\": " + false + ",\n" +
                 "   \"label\": \"" + propertyTypeTwo.getLabel() + "\",\n" +
                 "   \"predicate\": \"" + propertyTypeTwo.getDomainPredicate() + "\"\n" +
            "   }\n" +
            "]\n" +
        "}]";
    }

    @Test
    public void testSerialization() throws FileNotFoundException, JSONException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        UserDefinedPropertyGroupJSONBuilder.serialize(testGroups, outStream);

        String outputDescription = outStream.toString();
        JSONAssert.assertEquals(serializedGroup, outputDescription, false);
    }

    @Test
    public void testDeserialization() {
        InputStream is = new ByteArrayInputStream(serializedGroup.getBytes());
        List<UserDefinedPropertyGroup> returnedGroups = UserDefinedPropertyGroupJSONBuilder.deserialize(is);
        assertEquals(testGroups, returnedGroups);
    }

    @Test
    public void testRoundTrip() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        UserDefinedPropertyGroupJSONBuilder.serialize(testGroups, outStream);

        InputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        List<UserDefinedPropertyGroup> roundTrippedGroups = UserDefinedPropertyGroupJSONBuilder.deserialize(inStream);

        assertEquals(testGroups, roundTrippedGroups);
    }
}
