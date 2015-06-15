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

package org.dataconservancy.packaging.tool.impl.rules;

import java.io.File;

import java.net.URI;

import java.util.*;

import org.dataconservancy.packaging.tool.model.description.Action;
import org.dataconservancy.packaging.tool.model.description.ArtifactTypeSpec;
import org.dataconservancy.packaging.tool.model.description.MappingSpec;
import org.dataconservancy.packaging.tool.model.description.MappingsSpec;
import org.dataconservancy.packaging.tool.model.description.PropertySpec;
import org.dataconservancy.packaging.tool.model.description.RelationshipSpec;
import org.dataconservancy.packaging.tool.model.description.RuleSpec;

/** Simple implementation of a Rule as constructed from a specification */
public class RuleImpl
        implements Rule {

    private final Action action;

    private final TestOperation<?> selectOp;

    private final MappingsSpec mappingsSpec;

    public RuleImpl(RuleSpec spec) {
        this.action = spec.getSelect().getAction();
        this.selectOp =
                TestOperationFactory.getOperation(spec.getSelect().getTest());
        this.mappingsSpec = spec.getMappings();
    }

    @Override
    public boolean select(FileContext candidate) {
        for (Boolean selectValue : selectOp.operate(candidate)) {
            if (!selectValue.booleanValue()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Action getAction() {
        return action;
    }

    private Mapping getMapping(final FileContext candidate,
                               final MappingSpec spec) {

        final Map<RelationshipSpec, FileOperation> fileRelationshipMap =
                new HashMap<RelationshipSpec, FileOperation>();

        final Map<String, ValueOperation> valueRelationshipMap =
                new HashMap<String, ValueOperation>();

        final Map<String, ValueOperation> propertyMap =
                new HashMap<String, ValueOperation>();

        final ArtifactTypeSpec artifactType = spec.getType();

        /* Parse property specs */
        if (spec.getProperties() != null) {
            for (PropertySpec propSpec : spec.getProperties().getProperty()) {
                propertyMap.put(propSpec.getName(), ValueOperationFactory
                        .getOperation(propSpec.getValue()));
            }
        }

        /* Parse relationship specs */

        if (spec.getRelationships() != null) {
            for (RelationshipSpec relSpec : spec.getRelationships()
                    .getRelationship()) {
                if (relSpec.getFile() != null) {
                    fileRelationshipMap.put(relSpec, FileOperationFactory
                            .getOperation(relSpec.getFile()));
                } else if (relSpec.getValue() != null) {
                    valueRelationshipMap.put(relSpec.getName(),
                                             ValueOperationFactory
                                                     .getOperation(relSpec
                                                             .getValue()));
                } else {
                    throw new RuntimeException("Relationship spec needs a file or value operation!");
                }
            }
        }

        return new Mapping() {

            @Override
            public ArtifactTypeSpec getType() {
                return artifactType;
            }

            @Override
            public Map<String, Set<URI>> getRelationships() {
                Map<String, Set<URI>> rels = new HashMap<String, Set<URI>>();

                for (Map.Entry<RelationshipSpec, FileOperation> rel : fileRelationshipMap
                        .entrySet()) {
                    if (!rels.containsKey(rel.getKey().getName())) {
                        rels.put(rel.getKey().getName(), new HashSet<URI>());
                    }

                    for (File file : rel.getValue().operate(candidate)) {

                        if (rel.getKey().getSpecifier() == null) {
                            rels.get(rel.getKey().getName()).add(file.toURI());
                        } else {
                            URI uri =
                                    URI.create(file.toURI().toString() + "#"
                                            + rel.getKey().getSpecifier());
                            rels.get(rel.getKey().getName()).add(uri);
                        }
                    }

                    if (rels.get(rel.getKey().getName()).isEmpty()) {
                        rels.remove(rel.getKey().getName());
                    }
                }

                for (Map.Entry<String, ValueOperation> rel : valueRelationshipMap
                        .entrySet()) {
                    if (!rels.containsKey(rel.getKey())) {
                        rels.put(rel.getKey(), new HashSet<URI>());
                    }

                    for (String value : rel.getValue().operate(candidate)) {
                        rels.get(rel.getKey()).add(URI.create(value));
                    }

                    if (rels.get(rel.getKey()).isEmpty()) {
                        rels.remove(rel.getKey());
                    }
                }

                return rels;
            }

            @Override
            public Map<String, List<String>> getProperties() {
                Map<String, List<String>> properties =
                        new HashMap<String, List<String>>();

                for (Map.Entry<String, ValueOperation> prop : propertyMap
                        .entrySet()) {
                    List<String> values =
                            Arrays.asList(prop.getValue().operate(candidate));
                    if (!values.isEmpty()) {
                        properties.put(prop.getKey(), values);
                    }
                }

                return properties;
            }

            @Override
            public String getSpecifier() {
                return spec.getSpecifier();
            }
        };
    }

    @Override
    public List<Mapping> getMappings(FileContext candidate) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        if (mappingsSpec == null) {
            return mappings;
        }

        for (MappingSpec spec : mappingsSpec.getMapping()) {
            mappings.add(getMapping(candidate, spec));
        }

        return mappings;
    }

}
