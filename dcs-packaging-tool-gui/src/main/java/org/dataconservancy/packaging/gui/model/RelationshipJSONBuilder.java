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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convinence class for serializing/deserializing Relationship to and from json.
 */
public class RelationshipJSONBuilder {

    private static final String REQURIRES_URI = "requiresURI";
    private static final String LABEL = "label";
    private static final String DESCRIPTON = "description";
    private static final String RELATIONSHIP_URI = "relationshipURI";
    private static final Logger log = LoggerFactory.getLogger(RelationshipJSONBuilder.class);

    public static JSONObject serializeRelationship(Relationship relationship) {
        JSONObject relationshipObject = new JSONObject();
        try {
            relationshipObject.put(LABEL, relationship.getLabel());
            relationshipObject.put(DESCRIPTON, relationship.getDescription());
            relationshipObject.put(RELATIONSHIP_URI, relationship.getRelationshipUri());
            relationshipObject.put(REQURIRES_URI, relationship.requiresUri());

        } catch (JSONException e) {
            log.error("Error serializing relationship to json object: " + e.getMessage());
        }

        return relationshipObject;
    }

    public static Relationship deserializeRelationship(JSONObject object) {
        Relationship relationship = null;
        String label = null;
        String description = null;
        String relationshipURI = null;
        boolean requiresURI = false;

        try {
            if (object.has(LABEL)) {
                label = object.getString(LABEL);
            }

            if (object.has(DESCRIPTON)) {
                description = object.getString(DESCRIPTON);
            }

            if (object.has(RELATIONSHIP_URI)) {
                relationshipURI = object.getString(RELATIONSHIP_URI);
            }

            if (object.has(REQURIRES_URI)) {
                requiresURI = object.getBoolean(REQURIRES_URI);
            }
        } catch (JSONException e) {
            log.error("Error deserializing json object to relationship: " + e.getMessage());
        }

        if (label != null && relationshipURI != null) {
            relationship = new Relationship(label, description, relationshipURI, requiresURI);
        }
        return relationship;
    }
}
