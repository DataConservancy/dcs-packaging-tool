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
 */package org.dataconservancy.packaging.gui.model;

import java.util.List;

/**
 * A java model of a group of relationships. These relationship groups are made up of one or more {@link org.dataconservancy.packaging.gui.model.Relationship} objects.
 * RelationshipGroups are parsed from a configuration file and displayed to users to supply relationships for artifact in the GUI.
 */
public class RelationshipGroup {
    private String label;
    private String description;
    private String groupUrl;
    private List<Relationship> relationships;

    public RelationshipGroup(String label, String description, String groupUrl, List<Relationship> relationships) {
        this.label = label;
        this.description = description;
        this.groupUrl = groupUrl;
        this.relationships = relationships;
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

    public List<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationshipGroup)) return false;

        RelationshipGroup other = (RelationshipGroup) o;

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
        if (relationships == null) {
            if (other.relationships != null)
                return false;
        } else if (!relationships.equals(other.relationships))
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
        result = prime * result + ((relationships == null) ? 0 : relationships.hashCode());
        return result;
    }

    public String toString() {
        return "RelationshipGroup{" + "label='" + label + '\'' + ", description='" + description + ", groupURL='" + groupUrl + ", relationships='" + relationships + '}';
    }
}
