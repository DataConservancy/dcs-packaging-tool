/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Representation of a Package Artifact stores a string with the relationship name/uri, A set of strings representing the targets of the relationship,
 * and a boolean flag representing whether the targets of the relationship are required to be uris.
 */
public class PackageRelationship {
    private String relationshipName;
    private Set<String> relationshipTargets;
    private boolean requiresUriTargets;

    public PackageRelationship() {
        relationshipTargets = new HashSet<String>();
    }

    public PackageRelationship(String relationshipName, boolean requiresUriTargets, Set<String> relationshipTargets) {
        this.relationshipTargets = relationshipTargets;
        this.relationshipName = relationshipName;
        this.requiresUriTargets = requiresUriTargets;
    }

    public PackageRelationship(String relationshipName, boolean requiresUriTargets, String... relationshipTargets) {
        this.relationshipName = relationshipName;
        this.requiresUriTargets = requiresUriTargets;
        this.relationshipTargets = new HashSet<String>();
        this.relationshipTargets.addAll(Arrays.asList(relationshipTargets));
    }

    public String getName() {
        return relationshipName;
    }

    public void setName(String name) {
        this.relationshipName = name;
    }

    public Set<String> getTargets() {
        return relationshipTargets;
    }

    public void setRelationshipTargets(Set<String> relationshipTargets) {
        this.relationshipTargets = relationshipTargets;
    }

    public boolean requiresUriTargets() {
        return requiresUriTargets;
    }

    public void setRequiresUriTargets(boolean requiresUriTargets) {
        this.requiresUriTargets = requiresUriTargets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageRelationship)) return false;

        PackageRelationship relationship = (PackageRelationship) o;

        if (requiresUriTargets != relationship.requiresUriTargets) return false;
        if (relationshipName != null ? !relationshipName.equals(relationship.relationshipName) : relationship.relationshipName != null)
            return false;
        if (relationshipTargets != null ? !relationshipTargets.equals(relationship.relationshipTargets) : relationship.relationshipTargets != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = relationshipName != null ? relationshipName.hashCode() : 0;
        result = 31 * result + (relationshipTargets != null ? relationshipTargets.hashCode() : 0);
        result = 31 * result + (requiresUriTargets ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PackageRelationship{name=" + relationshipName + " targets=" + relationshipTargets + " requiresUri=" + requiresUriTargets + "}";
    }

 }
