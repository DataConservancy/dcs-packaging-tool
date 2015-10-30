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

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the serialization and deserialization of relationship groups to and from json.
 */
public class RelationshipGroupJSONBuilderTest {

    private List<RelationshipGroup> testGroups;
    private Relationship relationshipOne;
    private Relationship relationshipTwo;

    private String serializedGroup;

    @Before
    public void setup() {
        relationshipOne = new Relationship("one", "descriptionOne", "uri:one", true);
        relationshipTwo = new Relationship("two", "descriptionTwo", "uri:two", false);

        List<Relationship> relationshipList = new ArrayList<Relationship>();
        relationshipList.add(relationshipOne);
        relationshipList.add(relationshipTwo);

        RelationshipGroup testGroup = new RelationshipGroup("group", "group description", "http://testgroup.org", relationshipList);

        testGroups = new ArrayList<RelationshipGroup>();
        testGroups.add(testGroup);

        serializedGroup = "[{\n" +
            "  \"description\": \"" + testGroup.getDescription() + "\",\n" +
            "  \"label\": \"" + testGroup.getLabel() + "\",\n" +
            "  \"groupURL\": \"" + testGroup.getGroupUrl() + "\",\n" +
            "  \"relationships\": [\n" +
            "    {\n" +
                 "   \"description\": \"" + relationshipOne.getDescription() + "\",\n" +
                 "   \"requiresURI\": " + relationshipOne.requiresUri() + ",\n" +
                 "   \"label\": \"" + relationshipOne.getLabel() + "\",\n" +
                 "   \"relationshipURI\": \"" + relationshipOne.getRelationshipUri() + "\"\n" +
            "    },\n" +
            "    {\n" +
                 "   \"description\": \"" + relationshipTwo.getDescription() + "\",\n" +
                 "   \"requiresURI\": " + relationshipTwo.requiresUri() + ",\n" +
                 "   \"label\": \"" + relationshipTwo.getLabel() + "\",\n" +
                 "   \"relationshipURI\": \"" + relationshipTwo.getRelationshipUri() + "\"\n" +
            "   }\n" +
            "]\n" +
        "}]";
    }

    @Test
    public void testSerialization() throws FileNotFoundException, JSONException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        RelationshipGroupJSONBuilder.serialize(testGroups, outStream);

        String outputDescription = outStream.toString();
        JSONAssert.assertEquals(serializedGroup, outputDescription, false);
    }

    @Test
    public void testDeserialization() {
        InputStream is = new ByteArrayInputStream(serializedGroup.getBytes());
        List<RelationshipGroup> returnedGroups = RelationshipGroupJSONBuilder.deserialize(is);
        assertEquals(testGroups, returnedGroups);
    }

    @Test
    public void testRoundTrip() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        RelationshipGroupJSONBuilder.serialize(testGroups, outStream);

        InputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        List<RelationshipGroup> roundTrippedGroups = RelationshipGroupJSONBuilder.deserialize(inStream);

        assertEquals(testGroups, roundTrippedGroups);
    }
}
