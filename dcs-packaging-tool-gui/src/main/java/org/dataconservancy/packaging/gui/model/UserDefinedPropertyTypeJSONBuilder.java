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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class for serializing/deserializing PropertyTypes used for user defined properties to and from json.
 */
public class UserDefinedPropertyTypeJSONBuilder {

    private static final String LABEL = "label";
    private static final String DESCRIPTON = "description";
    private static final String PREDICATE = "predicate";
    private static final String REQUIRES_URI = "requiresURI";
    private static final Logger log = LoggerFactory.getLogger(UserDefinedPropertyTypeJSONBuilder.class);

    public static JSONObject serializeProperty(PropertyType propertyType) {
        JSONObject relationshipObject = new JSONObject();
        try {
            relationshipObject.put(LABEL, propertyType.getLabel());
            relationshipObject.put(DESCRIPTON, propertyType.getDescription());
            relationshipObject.put(PREDICATE, propertyType.getDomainPredicate());
            if (propertyType.getPropertyValueType().equals(PropertyValueType.URI)) {
                relationshipObject.put(REQUIRES_URI, true);
            } else {
                relationshipObject.put(REQUIRES_URI, false);
            }
        } catch (JSONException e) {
            log.error("Error serializing relationship to json object: " + e.getMessage());
        }

        return relationshipObject;
    }

    public static PropertyType deserializePropertyType(JSONObject object) {
        PropertyType propertyType = new PropertyType();

        try {
            if (object.has(LABEL)) {
                propertyType.setLabel(object.getString(LABEL));
            }

            if (object.has(DESCRIPTON)) {
                propertyType.setDescription(object.getString(DESCRIPTON));
            }

            if (object.has(PREDICATE)) {
                propertyType.setDomainPredicate(new URI(object.getString(PREDICATE)));
            }

            if (object.has(REQUIRES_URI) && object.getBoolean(REQUIRES_URI)) {
                propertyType.setPropertyValueType(PropertyValueType.URI);
            } else {
                propertyType.setPropertyValueType(PropertyValueType.STRING);
            }

        } catch (JSONException | URISyntaxException e) {
            log.error("Error deserializing json object to relationship: " + e.getMessage());
        }

        return propertyType;
    }
}
