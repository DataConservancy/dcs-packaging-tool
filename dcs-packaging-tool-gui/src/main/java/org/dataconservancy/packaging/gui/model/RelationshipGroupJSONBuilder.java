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

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Convinence class for serializing/deserializing RelationshipGroups to and from json.
 */
public class RelationshipGroupJSONBuilder {
    private static final String LABEL = "label";
    private static final String DESCRIPTON = "description";
    private static final String GROUP_URL = "groupURL";
    private static final String RELATIONSHIPS = "relationships";

    private static final Logger log = LoggerFactory.getLogger(RelationshipGroupJSONBuilder.class);

    /**
     * Serializes the RelationshipGroup to the output stream with json formatting.
     * @param groups The relationship group to serialize.
     * @param out The output stream where the serialization should go.
     */
    public static void serialize(List<RelationshipGroup> groups, OutputStream out) {

        try {
            IOUtils.write(serializeRelationshipGroups(groups).toString(4), out, "UTF-8");
        } catch (IOException e) {
            log.error("Error serializing relationship group to file: " + e.getMessage());
        } catch (JSONException e) {
            log.error("Error serializing group to json: " + e.getMessage());
        }
    }

    /**
     * Serialize a RelationshipGroup object to a json object.
     * @param groups The relationship group to serialize.
     * @return The resulting JSONObject
     */
    public static JSONArray serializeRelationshipGroups(List<RelationshipGroup> groups) {
        JSONArray relationshipGroupArray = new JSONArray();
        for (RelationshipGroup group : groups) {
            JSONObject relationshipGroupObject = new JSONObject();
            try {
                relationshipGroupObject.put(LABEL, group.getLabel());
                relationshipGroupObject.put(DESCRIPTON, group.getDescription());
                relationshipGroupObject.put(GROUP_URL, group.getGroupUrl());

                //Create a json array of all the relationships
                JSONArray relationships = new JSONArray();
                for( Relationship relationship : group.getRelationships() )
                {
                   relationships.put(RelationshipJSONBuilder.serializeRelationship(relationship));
                }
                relationshipGroupObject.put(RELATIONSHIPS, relationships);
            } catch (JSONException e) {
                log.error("Error serializing group to json: " + e.getMessage());
            }

            relationshipGroupArray.put(relationshipGroupObject);
        }

        return relationshipGroupArray;
    }

    /**
     * Deserializes a RelationshipGroup from the json serialization pointed to by the input stream.
     * @param inStream The json serialized RelationshipGroup.
     * @return The resulting list of RelationshipGroups or an empty list if the input stream couldn't be deserialized.
     */
    public static List<RelationshipGroup> deserialize(InputStream inStream) {
        List<RelationshipGroup> groups = new ArrayList<RelationshipGroup>();
        String jsonString = "";
        if (inStream != null) {
            try {
                jsonString = IOUtils.toString(inStream);
            } catch (IOException e) {
                log.error("Error deserializing input stream to relationship group json: " + e.getMessage());
            }
        }

        if (jsonString != null && !jsonString.isEmpty()) {
            try {
                JSONArray relationshipGroupArray = new JSONArray(jsonString);
                for( int i = 0; i < relationshipGroupArray.length(); i++ )
                {
                    RelationshipGroup group = deserializeRelationshipGroup(relationshipGroupArray.getJSONObject(i));
                    if (group != null) {
                        groups.add(group);
                    }
                }
            } catch (JSONException e) {
                log.error("Error deserializing json to relationship group: " + e.getMessage());
            }
        }

        return groups;
    }

    /**
     * Deserialize a JSON object into a RelationshipGroup object.
     * @param object The json object to deserialize.
     * @return The resulting RelationshipGroup.
     */
    public static RelationshipGroup deserializeRelationshipGroup(JSONObject object) {
        RelationshipGroup relationshipGroup = null;
        String label = null;
        String description = null;
        String groupURL = null;
        List<Relationship> relationships = new ArrayList<Relationship>();
        try {
           if (object.has(LABEL)) {
               label = object.getString(LABEL);
           }

           if (object.has(DESCRIPTON)) {
               description = object.getString(DESCRIPTON);
           }

           if (object.has(GROUP_URL)) {
               groupURL = object.getString(GROUP_URL);
           }

           if (object.has(RELATIONSHIPS)) {
               JSONArray relationshipArray = object.getJSONArray(RELATIONSHIPS);

               for( int i = 0; i < relationshipArray.length(); i++ )
               {
                   Relationship relationship = RelationshipJSONBuilder.deserializeRelationship(relationshipArray.getJSONObject(i));
                   relationships.add(relationship);
               }
           }
        } catch (JSONException e) {
            log.error("Error deserializing json object to relationship group: " + e.getMessage());
        }

        if (label != null && groupURL != null) {
           relationshipGroup = new RelationshipGroup(label, description, groupURL, relationships);
        }
        return relationshipGroup;
    }
}
