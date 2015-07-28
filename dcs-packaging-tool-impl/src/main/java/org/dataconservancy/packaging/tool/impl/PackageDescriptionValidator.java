/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.validation.PackageValidationException;

/**
 * Validates {@link PackageDescription} documents to assure that they are
 * well-formed.
 * <p>
 * Checks to make sure the basic structure of a {@link PackageDescription} is
 * valid. That is to say:
 * <ul>
 * <li>All properties and sub-properties have non-null and non-empty names</li>
 * <li>All required fields are populated: artifactId and artifactType</li>
 * <li>All populated values are of the right basic type (free form text, URI,
 * etc)</li>
 * <li>Relationships between artifacts are expressed correctly</li>
 * <li>Artifact file content is expressed in terms of resolvable URIs</li>
 * </ul>
 *
 */
public class PackageDescriptionValidator {

    public void validate(PackageDescription description)
            throws PackageValidationException {

        Set<String> artifactIds = new HashSet<>();
        Set<String> relatedArtifactIds = new HashSet<>();

        /* Package specification id should be defined */
        if (!isDefined(description.getPackageOntologyIdentifier())) {
            throw new PackageValidationException("Package description specification should be defined");
        }

        /*
         * Iterate through all artifacts, checking validity constraints and
         * recording relationships
         */
        for (PackageArtifact artifact : description.getPackageArtifacts()) {

            /* Every artifact must have an ID */
            if (!isDefined(artifact.getId())) {
                throw new PackageValidationException("A package artifact does not have an identifier");
            }

            /* All artifact IDs must be unique */
            if (artifactIds.contains(artifact.getId())) {
                throw new PackageValidationException(String.format("Duplicate artifact ID '%s' found",
                                                                   artifact.getId()));
            } else {
                artifactIds.add(artifact.getId());
            }

            /* Artifact type must be defined */
            if (artifact.getType() == null
                    || !isDefined(artifact.getType())) {
                throw new PackageValidationException(String.format("Package artifact '%s' does not have a defined type",
                                                                   artifact.getId()));
            }

            /* Make sure artifact ref is a valid, resolvable, protocol-based URI */
            validateArtifactRef(artifact);

            /* Make sure properties are sane */
            validateProperties(artifact);

            /*
             * Make sure relationships are sane, record relationship targets in
             * order to later on verify that artifact rels only point to other
             * artifacts within the same PackageDescription.
             */
            validateRelationships(artifact, relatedArtifactIds);
        }

        /*
         * Lastly, once we've collected all necessary data, make sure
         * hierarchical relationships' targets are valid artifact ids encountered in this
         * PackageDescription. Other types of relationships can have unrestricted targets.
         */
        for (PackageArtifact artifact : description.getPackageArtifacts()) {
            Set<String> relationshipNames = artifact.getAllRelationshipNamesOnArtifact();
            for (String relationshipName : relationshipNames) {
                if (relationshipName.equals(DcsBoPackageOntology.IS_MEMBER_OF) ||
                        relationshipName.equals(DcsBoPackageOntology.IS_METADATA_FOR)) {
                    PackageRelationship relationship = artifact.getRelationshipByName(relationshipName);
                    if (relationship != null) {
                        Set<String> targets = relationship.getTargets();
                        for (String target : targets) {
                            if (!artifactIds.contains(target)) {
                                throw new PackageValidationException(String.format("Bad relationship:  " +
                                        "Hierarchical relationship %s  from artifact '%s' and points to '%s' which does not" +
                                        " exists in the package.", relationshipName, artifact.getId(), target));
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Make sure artifact ref is a valid, resolvable, protocol-based URI.
     */
    private void validateArtifactRef(PackageArtifact artifact)
            throws PackageValidationException {

        /* Artifact ref must be defined */
        if (artifact.getArtifactRef()==null) {
            throw new PackageValidationException(String.format("Artifact ref is not defined. for artifact '%s'",
                                                               artifact.getId()));
        }

     /*   if (!UriUtility.isResolvable(artifact.getArtifactRef().getRefURI())) {
            throw new PackageValidationException(String.format("Artifact ref '%s' must be resolvable for artifact '%s'",
                                                               artifact.getArtifactRef(),
                                                               artifact.getId()));
        }  */
    }

    /**
     * Make sure properties are minimally defined.
     * <p>
     * Properties are basically opaque, just check to make sure that property
     * keys are defined (not null and not empty string). We don't check values,
     * as any possible value (including null) may be meaningful to a given
     * ontology.
     * </p>
     */
    private void validateProperties(PackageArtifact artifact)
            throws PackageValidationException {

        if (artifact.getPropertyNames() == null) {
            throw new PackageValidationException(String.format("Property set for artifact '%s' should be empty, not null",
                                                               artifact.getId()));
        }

        for (String propertyKey : artifact.getPropertyNames()) {
            if (!isDefined(propertyKey)) {
                throw new PackageValidationException(String.format("Property keys must not be null or empty string in artifact '%s'",
                                                                   artifact.getId()));
            }
            if (artifact.getPropertyValueGroups(propertyKey) != null) {
                Set<PackageArtifact.PropertyValueGroup> valueGroups = artifact.getPropertyValueGroups(propertyKey);
                for (PackageArtifact.PropertyValueGroup group : valueGroups) {
                    for (String subPropertyName : group.getSubPropertyNames()) {
                        if (!isDefined(subPropertyName)) {
                            throw new PackageValidationException(String.format("Keys for SubProperties in PropertyValueGroup" +
                                    " must not be null or empty string in artifact '%s'",
                                    artifact.getId()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Make sure relationships are fully-formed, and keep track of target
     * artifact IDs for later verification.
     * 
     * @param artifact the PackageArtifact
     * @param targetIds the target IDs
     */
    private void validateRelationships(PackageArtifact artifact,
                                       Set<String> targetIds)
            throws PackageValidationException {

        if (artifact.getRelationships() == null) {
            throw new PackageValidationException(String.format("Relationship map for artifact '%s' should be empty, not null",
                                                               artifact.getId()));
        }

        for (PackageRelationship rel : artifact.getRelationships()) {

            /* Relationship name must be defined */
            if (!isDefined(rel.getName())) {
                throw new PackageValidationException(String.format("Relationship names must be defined (not null or empty string) in artifact '%s'",
                                                                   artifact.getId()));
            }

            /*
             * There must be at least one relationship target whenever a
             * relationship name is declared
             */
            if (rel.getTargets() == null || rel.getTargets().isEmpty()) {
                throw new PackageValidationException(String.format("Relationship '%s' doesn't point to anything (is null or empty) in artifact '%s'",
                                                                   rel.getName(),
                                                                   artifact.getId()));
            }

            for (String target : rel.getTargets()) {
                if (!isDefined(target)) {
                    throw new PackageValidationException(String.format("One of the '%s' relationships in artifact '%s' points to null",
                                                                       rel.getName(),
                                                                       artifact.getId()));
                }

                targetIds.add(target);
            }
        }
    }

    private boolean isDefined(String value) {
        return (value != null && !value.equalsIgnoreCase(""));
    }

}
