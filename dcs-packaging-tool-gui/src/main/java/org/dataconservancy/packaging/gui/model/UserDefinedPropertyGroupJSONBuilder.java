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
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
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
 * Class for serializing/deserializing UserDefinedPropertyGroups to and from json.
 */
public class UserDefinedPropertyGroupJSONBuilder {
    private static final String LABEL = "label";
    private static final String DESCRIPTON = "description";
    private static final String GROUP_URL = "groupURL";
    private static final String PROPERTY_TYPES = "propertyTypes";

    private static final Logger log = LoggerFactory.getLogger(UserDefinedPropertyGroupJSONBuilder.class);

    /**
     * Serializes the UserDefinedPropertyGroup to the output stream with json formatting.
     * @param groups The relationship group to serialize.
     * @param out The output stream where the serialization should go.
     */
    public static void serialize(List<UserDefinedPropertyVocabulary> groups, OutputStream out) {

        try {
            IOUtils.write(serializeUserDefinedPropertyGroups(groups).toString(4), out, "UTF-8");
        } catch (IOException e) {
            log.error("Error serializing relationship group to file: " + e.getMessage());
        } catch (JSONException e) {
            log.error("Error serializing group to json: " + e.getMessage());
        }
    }

    /**
     * Serialize a list of UserDefinedPropertyGroups object to a json object.
     * @param groups The list of Property groups to serialize.
     * @return The resulting JSONObject
     */
    public static JSONArray serializeUserDefinedPropertyGroups(List<UserDefinedPropertyVocabulary> groups) {
        JSONArray userDefinedPropertyGroupArray = new JSONArray();
        for (UserDefinedPropertyVocabulary group : groups) {
            JSONObject userDefinedPropertyGroupObject = new JSONObject();
            try {
                userDefinedPropertyGroupObject.put(LABEL, group.getLabel());
                userDefinedPropertyGroupObject.put(DESCRIPTON, group.getDescription());
                userDefinedPropertyGroupObject.put(GROUP_URL, group.getGroupUrl());

                //Create a json array of all the relationships
                JSONArray propertyTypes = new JSONArray();
                for (PropertyType propertyType : group.getPropertyTypes() )
                {
                   propertyTypes.put(UserDefinedPropertyTypeJSONBuilder.serializeProperty(propertyType));
                }
                userDefinedPropertyGroupObject.put(PROPERTY_TYPES, propertyTypes);
            } catch (JSONException e) {
                log.error("Error serializing group to json: " + e.getMessage());
            }

            userDefinedPropertyGroupArray.put(userDefinedPropertyGroupObject);
        }

        return userDefinedPropertyGroupArray;
    }

    /**
     * Deserializes a list of UserDefinedPropertyGroups from the json serialization pointed to by the input stream.
     * @param inStream The json serialized UserDefinedPropertyGroups.
     * @return The resulting list of UserDefinedPropertyGroups or an empty list if the input stream couldn't be deserialized.
     */
    public static List<UserDefinedPropertyVocabulary> deserialize(InputStream inStream) {
        List<UserDefinedPropertyVocabulary> groups = new ArrayList<>();
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
                JSONArray userDefinedPropertyGroupArray = new JSONArray(jsonString);
                for( int i = 0; i < userDefinedPropertyGroupArray.length(); i++ )
                {
                    UserDefinedPropertyVocabulary group = deserializedUserDefinedPropertyGroup(userDefinedPropertyGroupArray.getJSONObject(i));
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
     * Deserialize a JSON object into a UserDefinedPropertyGroup object.
     * @param object The json object to deserialize.
     * @return The resulting UserDefinedPropertyGroup.
     */
    public static UserDefinedPropertyVocabulary deserializedUserDefinedPropertyGroup(JSONObject object) {
        UserDefinedPropertyVocabulary userDefinedPropertyVocabulary = null;
        String label = null;
        String description = null;
        String groupURL = null;
        List<PropertyType> propertyTypes = new ArrayList<>();
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

           if (object.has(PROPERTY_TYPES)) {
               JSONArray propertyTypeArray = object.getJSONArray(PROPERTY_TYPES);

               for( int i = 0; i < propertyTypeArray.length(); i++ )
               {
                   PropertyType propertyType = UserDefinedPropertyTypeJSONBuilder.deserializePropertyType(propertyTypeArray.getJSONObject(i));
                   propertyTypes.add(propertyType);
               }
           }
        } catch (JSONException e) {
            log.error("Error deserializing json object to relationship group: " + e.getMessage());
        }

        if (label != null && groupURL != null) {
           userDefinedPropertyVocabulary = new UserDefinedPropertyVocabulary(label, description, groupURL, propertyTypes);
        }
        return userDefinedPropertyVocabulary;
    }
}
