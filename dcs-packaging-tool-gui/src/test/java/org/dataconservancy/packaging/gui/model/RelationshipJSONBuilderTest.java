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


import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests that a relationship can be round tripped to and from json.
 */
public class RelationshipJSONBuilderTest {

    private Relationship testRelationship;
    private final String label = "label";
    private final String description = "description";
    private final String relationshipURI = "uri:relationship:test";
    private final boolean requiresURI = true;

    @Before
    public void setup() {
        testRelationship = new Relationship(label, description, relationshipURI, requiresURI);
    }

    @Test
    public void testRoundTrip() {
        JSONObject relationshipObject = RelationshipJSONBuilder.serializeRelationship(testRelationship);
        assertNotNull(relationshipObject);

        Relationship roundTrippedRelationship = RelationshipJSONBuilder.deserializeRelationship(relationshipObject);
        assertNotNull(roundTrippedRelationship);

        assertEquals(testRelationship, roundTrippedRelationship);
    }
}
