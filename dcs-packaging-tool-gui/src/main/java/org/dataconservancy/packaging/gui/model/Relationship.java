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

/**
 * Models a relationship that is read from a configuration file, to be displayed as an option to users in specfying relationships for artifacts.
 */
public class Relationship {
    private boolean requiresUri = false;
    private String relationshipUri;
    private String label;
    private String description;

    public Relationship(String label, String description, String relationshipUri, boolean requiresUri) {
        this.requiresUri = requiresUri;
        this.relationshipUri = relationshipUri;
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getRelationshipUri() {
        return relationshipUri;
    }

    public boolean requiresUri() {
        return requiresUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relationship)) return false;

        Relationship other = (Relationship) o;

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
        if (relationshipUri == null) {
            if (other.relationshipUri != null)
                return false;
        } else if (!relationshipUri.equals(other.relationshipUri))
            return false;
        if (requiresUri != other.requiresUri)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((relationshipUri == null) ? 0 : relationshipUri.hashCode());
        result = prime * result + (requiresUri ? 0 : 1);
        return result;
    }

    public String toString() {
        return "Relationship{" + "label='" + label + '\'' + ", description='" + description + "', relationshipURI='" + relationshipUri + "', requiresURI='" + requiresUri + '}';
    }
}
