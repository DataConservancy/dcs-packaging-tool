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

import java.util.List;

/**
 * A java model of a group of propertyTypes. These relationship groups are made up of one or more PropertyType objects.
 * RelationshipGroups are parsed from a configuration file and displayed to users to supply propertyTypes for artifact in the GUI.
 */
public class UserDefinedPropertyVocabulary {
    private String label;
    private String description;
    private String groupUrl;
    private List<PropertyType> propertyTypes;

    public UserDefinedPropertyVocabulary(String label, String description, String groupUrl, List<PropertyType> propertyTypes) {
        this.label = label;
        this.description = description;
        this.groupUrl = groupUrl;
        this.propertyTypes = propertyTypes;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupUrl() {
        return groupUrl;
    }

    public List<PropertyType> getPropertyTypes() {
        return propertyTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDefinedPropertyVocabulary)) return false;

        UserDefinedPropertyVocabulary other = (UserDefinedPropertyVocabulary) o;

        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (groupUrl == null) {
            if (other.groupUrl != null)
                return false;
        } else if (!groupUrl.equals(other.groupUrl))
            return false;
        if (propertyTypes == null) {
            if (other.propertyTypes != null)
                return false;
        } else if (!propertyTypes.equals(other.propertyTypes))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((groupUrl == null) ? 0 : groupUrl.hashCode());
        result = prime * result + ((propertyTypes == null) ? 0 : propertyTypes.hashCode());
        return result;
    }

    public String toString() {
        return "UserDefinedPropertyGroup{" + "label='" + label + '\'' + ", description='" + description + ", groupURL='" + groupUrl + ", propertyTypes='" +
            propertyTypes + '}';
    }
}
