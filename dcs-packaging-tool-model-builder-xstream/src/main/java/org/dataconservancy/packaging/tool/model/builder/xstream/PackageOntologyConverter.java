/*
 * Copyright 2015 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.model.builder.xstream;


import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.packaging.tool.model.PackageOntology;

public class PackageOntologyConverter extends AbstractEntityConverter {
    public static final String E_PACKAGE_ONTOLOGY = "packageOntology";
    private static final String E_ONTOLOGY_ID = "id";
    private static final String E_ARTIFACT_TYPE = "artifactType";
    private static final String E_ARTIFACT_TYPES = "artifactTypes";
    private static final String E_PROPERTY_TYPE = "propertyType";
    private static final String E_PROPERTY_TYPES = "propertyTypes";
    private static final String E_PROPERTY_TYPE_NAME = "propertyTypeName";

    private static final String E_PROPERTY_TYPE_FIELDS = "fields";
    private static final String E_PROPERTY_TYPE_FIELD = "field";
    private static final String E_PROPERTY_TYPE_FIELD_NAME = "fieldName";
    private static final String E_PROPERTY_TYPE_FIELD_TYPE = "fieldType";
    private static final String E_PROPERTY_TYPE_FIELD_MIN_OCCURRENCE = "minOccurrence";
    private static final String E_PROPERTY_TYPE_FIELD_MAX_OCCURRENCE = "maxOccurrence";
    private static final String E_PROPERTY_TYPE_FIELD_SYSTEM_SUPPLIED = "isSystemSupplied";
    private static final String E_PROPERTY_TYPE_FIELD_INHERITABLE = "isInheritable";
    private static final String E_ARTIFACT_TYPE_NAME = "name";
    private static final String E_ARTIFACT_TYPE_PROPERTIES = "properties";
    private static final String E_ARTIFACT_TYPE_PROPERTY = "property";
    private static final String E_ARTIFACT_TYPE_PROPERTY_NAME = "name";
    private static final String E_ARTIFACT_TYPE_PROPERTY_VALUE_TYPE = "valueType";
    private static final String E_ARTIFACT_TYPE_PROPERTY_MIN_OCCURRENCE = "minOccurrence";
    private static final String E_ARTIFACT_TYPE_PROPERTY_MAX_OCCURRENCE = "maxOccurrence";
    private static final String E_ARTIFACT_TYPE_PROPERTY_SYSTEM_SUPPLIED = "isSystemSupplied";
    private static final String E_ARTIFACT_TYPE_PROPERTY_INHERITABLE = "isInheritable";

    private static final String E_ARTIFACT_TYPE_RELATIONSHIPS = "relationships";
    private static final String E_ARTIFACT_TYPE_RELATIONSHIP = "relationship";
    private static final String E_ARTIFACT_TYPE_RELATIONSHIP_NAME = "name";
    private static final String E_ARTIFACT_TYPE_HIERARCHICAL_RELATIONSHIP_MARKER = "isHierarchical";
    private static final String E_ARTIFACT_TYPE_RELATED_ARTIFACT_TYPE = "relatedArtifactType";
    private static final String E_ARTIFACT_TYPE_RELATIONSHIP_MIN_OCCURRENCE = "minOccurrence";
    private static final String E_ARTIFACT_TYPE_RELATIONSHIP_MAX_OCCURRENCE = "maxOccurrence";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (source == null) {
            final String msg = "Source object was null.";
            throw new IllegalArgumentException(msg);
        }

        PackageOntology pkgOntology = (PackageOntology)source;

        if (pkgOntology.getId()!= null) {
            writer.startNode(E_ONTOLOGY_ID);
            writer.setValue(pkgOntology.getId());
            writer.endNode();
        }

        if (pkgOntology.getArtifactTypes() != null) {
            writer.startNode(E_ARTIFACT_TYPES);
            for (String artifactType : pkgOntology.getArtifactTypes()) {
                writer.startNode(E_ARTIFACT_TYPE);

                writer.startNode(E_ARTIFACT_TYPE_NAME);
                writer.setValue(artifactType);
                writer.endNode();

                if (pkgOntology.getProperties(artifactType) != null &&
                        !pkgOntology.getProperties(artifactType).isEmpty()) {
                    writer.startNode(E_ARTIFACT_TYPE_PROPERTIES);

                    for (PackageOntology.Property property : pkgOntology.getProperties(artifactType)) {
                        writer.startNode(E_ARTIFACT_TYPE_PROPERTY);

                        writer.startNode(E_ARTIFACT_TYPE_PROPERTY_NAME);
                        writer.setValue(property.getName());
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_PROPERTY_VALUE_TYPE);
                        writer.setValue(property.getValueType());
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_PROPERTY_MIN_OCCURRENCE);
                        writer.setValue(Integer.toString(property.getMinOccurrence()));
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_PROPERTY_MAX_OCCURRENCE);
                        writer.setValue(Integer.toString(property.getMaxOccurrence()));
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_PROPERTY_SYSTEM_SUPPLIED);
                        writer.setValue(Boolean.toString(property.isSystemSupplied()));
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_PROPERTY_INHERITABLE);
                        writer.setValue(Boolean.toString(property.isInheritable()));
                        writer.endNode();

                        writer.endNode();
                    }

                    writer.endNode();
                }

                if (pkgOntology.getRelationships(artifactType) != null &&
                        !pkgOntology.getRelationships(artifactType).isEmpty()) {
                    writer.startNode(E_ARTIFACT_TYPE_RELATIONSHIPS);

                    for (PackageOntology.Relationship relationship : pkgOntology.getRelationships(artifactType)) {
                        writer.startNode(E_ARTIFACT_TYPE_RELATIONSHIP);

                        writer.startNode(E_ARTIFACT_TYPE_RELATIONSHIP_NAME);
                        writer.setValue(relationship.getName());
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_RELATED_ARTIFACT_TYPE);
                        writer.setValue(relationship.getRelatedArtifactType());
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_RELATIONSHIP_MIN_OCCURRENCE);
                        writer.setValue(Integer.toString(relationship.getMinOccurrence()));
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_RELATIONSHIP_MAX_OCCURRENCE);
                        writer.setValue(Integer.toString(relationship.getMaxOccurrence()));
                        writer.endNode();

                        writer.startNode(E_ARTIFACT_TYPE_HIERARCHICAL_RELATIONSHIP_MARKER);
                        writer.setValue(Boolean.toString(relationship.isHierarchical()));
                        writer.endNode();

                        writer.endNode();
                    }

                    writer.endNode();
                }

                writer.endNode();
            }
            writer.endNode();
        }

        if (pkgOntology.getCustomPropertyTypes() != null) {

            writer.startNode(E_PROPERTY_TYPES);
            for (PackageOntology.PropertyType propertyType : pkgOntology.getCustomPropertyTypes()) {
                writer.startNode(E_PROPERTY_TYPE);

                writer.startNode(E_PROPERTY_TYPE_NAME);
                writer.setValue(propertyType.getName());
                writer.endNode();

                if (propertyType.getFieldNames() !=null) {
                    writer.startNode(E_PROPERTY_TYPE_FIELDS);

                    for (String fieldName: propertyType.getFieldNames()) {
                        writer.startNode(E_PROPERTY_TYPE_FIELD);

                        writer.startNode(E_PROPERTY_TYPE_FIELD_NAME);
                        writer.setValue(fieldName);
                        writer.endNode();

                        writer.startNode(E_PROPERTY_TYPE_FIELD_TYPE);
                        writer.setValue(propertyType.getTypeFieldsMap().get(fieldName).getValueType());
                        writer.endNode();

                        writer.startNode(E_PROPERTY_TYPE_FIELD_MIN_OCCURRENCE);
                        writer.setValue(String.valueOf(propertyType.getTypeFieldsMap().get(fieldName).getMinOccurrence()));
                        writer.endNode();

                        writer.startNode(E_PROPERTY_TYPE_FIELD_MAX_OCCURRENCE);
                        writer.setValue(String.valueOf(propertyType.getTypeFieldsMap().get(fieldName).getMaxOccurrence()));
                        writer.endNode();

                        writer.startNode(E_PROPERTY_TYPE_FIELD_SYSTEM_SUPPLIED);
                        writer.setValue(String.valueOf(propertyType.getTypeFieldsMap().get(fieldName).isSystemSupplied()));
                        writer.endNode();

                        writer.startNode(E_PROPERTY_TYPE_FIELD_INHERITABLE);
                        writer.setValue(String.valueOf(propertyType.getTypeFieldsMap().get(fieldName).isInheritable()));
                        writer.endNode();

                        writer.endNode();
                    }
                    writer.endNode();
                }
                writer.endNode();
            }
        }
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        PackageOntology ontology = new PackageOntology();

        String artifactType = "";
        String propertyName = "";
        String propertyValueType = "";
        String propertyMinOccurrence = "";
        String propertyMaxOccurrence = "";
        boolean isSystemSupplied = false;
        boolean isInheritable = false;

        String relationshipName = "";
        String relatedArtifactType = "";
        boolean isHierarchicalRelationship = false;
        String relationshipMinOccurrence = "";
        String relationshipMaxOccurrence = "";


        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String name = getElementName(reader);
            if (name.equals(E_ONTOLOGY_ID)) {
                ontology.setId(reader.getValue());
            } else if (name.equals(E_ARTIFACT_TYPES)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    name = getElementName(reader);
                    if (name.equals(E_ARTIFACT_TYPE)) {
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            name = getElementName(reader);
                            if(name.equals(E_ARTIFACT_TYPE_NAME)) {
                                artifactType = reader.getValue();
                                ontology.addArtifactType(artifactType);
                            } else if (name.equals(E_ARTIFACT_TYPE_PROPERTIES)) {
                                while (reader.hasMoreChildren()) {
                                    reader.moveDown();
                                    name = getElementName(reader);
                                    if (name.equals(E_ARTIFACT_TYPE_PROPERTY)) {
                                        while (reader.hasMoreChildren()) {
                                            reader.moveDown();
                                            name = getElementName(reader);
                                            if (name.equals(E_ARTIFACT_TYPE_PROPERTY_NAME)) {
                                                propertyName = reader.getValue();
                                            } else if (name.equals(E_ARTIFACT_TYPE_PROPERTY_VALUE_TYPE)) {
                                                propertyValueType = reader.getValue();
                                            } else if (name.equals(E_ARTIFACT_TYPE_PROPERTY_MIN_OCCURRENCE)) {
                                                propertyMinOccurrence = reader.getValue();
                                            } else if (name.equals(E_ARTIFACT_TYPE_PROPERTY_MAX_OCCURRENCE)) {
                                                propertyMaxOccurrence = reader.getValue();
                                            } else if (name.equals(E_ARTIFACT_TYPE_PROPERTY_INHERITABLE)) {
                                                isInheritable = Boolean.parseBoolean(reader.getValue());
                                            } else if (name.equals(E_ARTIFACT_TYPE_PROPERTY_SYSTEM_SUPPLIED)) {
                                                isSystemSupplied = Boolean.parseBoolean(reader.getValue());
                                            }
                                            reader.moveUp();
                                        }
                                        ontology.addArtifactProperty(artifactType, propertyName, propertyValueType,
                                                Integer.parseInt(propertyMinOccurrence),
                                                Integer.parseInt(propertyMaxOccurrence),
                                                isSystemSupplied, isInheritable);
                                    }
                                    reader.moveUp();
                                }
                            } else if (name.equals(E_ARTIFACT_TYPE_RELATIONSHIPS)) {
                                while (reader.hasMoreChildren()) {
                                    reader.moveDown();
                                    name = getElementName(reader);
                                    if (name.equals(E_ARTIFACT_TYPE_RELATIONSHIP)) {
                                        while (reader.hasMoreChildren()) {
                                            reader.moveDown();
                                            name = getElementName(reader);
                                            if (name.equals(E_ARTIFACT_TYPE_RELATIONSHIP_NAME)) {
                                                relationshipName = reader.getValue();
                                            } else if (name.equals(E_ARTIFACT_TYPE_RELATED_ARTIFACT_TYPE)) {
                                                relatedArtifactType = reader.getValue();
                                            } else if (name.equals(E_ARTIFACT_TYPE_HIERARCHICAL_RELATIONSHIP_MARKER)) {
                                                isHierarchicalRelationship = Boolean.parseBoolean(reader.getValue());
                                            } else if (name.equals(E_ARTIFACT_TYPE_RELATIONSHIP_MIN_OCCURRENCE)) {
                                                relationshipMinOccurrence = reader.getValue();
                                            } else if (name.equals(E_ARTIFACT_TYPE_RELATIONSHIP_MAX_OCCURRENCE)) {
                                                relationshipMaxOccurrence = reader.getValue();
                                            }
                                            reader.moveUp();
                                        }
                                        ontology.addArtifactRelationship(artifactType, relationshipName,
                                                relatedArtifactType, isHierarchicalRelationship,
                                                Integer.parseInt(relationshipMinOccurrence),
                                                Integer.parseInt(relationshipMaxOccurrence));
                                    }
                                    reader.moveUp();
                                }
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
            } else if (name.equals(E_PROPERTY_TYPES)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    name = getElementName(reader);
                    if (name.equals(E_PROPERTY_TYPE)) {
                        PackageOntology.PropertyType propertyType = ontology.new PropertyType();
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            name = getElementName(reader);
                            if (name.equals(E_PROPERTY_TYPE_NAME)) {
                                propertyType.setName(reader.getValue());
                            } else if (name.equals(E_PROPERTY_TYPE_FIELDS)) {
                                while (reader.hasMoreChildren()) {
                                    reader.moveDown();
                                    name = getElementName(reader);
                                    if (name.equals(E_PROPERTY_TYPE_FIELD)) {
                                        PackageOntology.Property property = ontology.new Property();
                                        while (reader.hasMoreChildren()) {
                                            reader.moveDown();
                                            name = getElementName(reader);
                                            if (name.equals(E_PROPERTY_TYPE_FIELD_NAME)) {
                                                property.setName(reader.getValue());
                                            } else if (name.equals(E_PROPERTY_TYPE_FIELD_TYPE)) {
                                                property.setValueType(reader.getValue());
                                            } else if (name.equals(E_PROPERTY_TYPE_FIELD_MIN_OCCURRENCE)) {
                                                property.setMinOccurrence(Integer.parseInt(reader.getValue()));
                                            } else if (name.equals(E_PROPERTY_TYPE_FIELD_MAX_OCCURRENCE)) {
                                                property.setMaxOccurrence(Integer.parseInt(reader.getValue()));
                                            } else if (name.equals(E_PROPERTY_TYPE_FIELD_SYSTEM_SUPPLIED)) {
                                                property.setSystemSupplied(Boolean.parseBoolean(reader.getValue()));
                                            } else if (name.equals(E_PROPERTY_TYPE_FIELD_INHERITABLE)) {
                                                property.setInheritable(Boolean.parseBoolean(reader.getValue()));
                                            }
                                            reader.moveUp();
                                        }
                                        propertyType.getTypeFieldsMap().put(property.getName(), property);                                        
                                    }
                                    reader.moveUp();
                                }
                            }
                            reader.moveUp();
                        }
                        ontology.getCustomPropertyTypes().add(propertyType);
                    }
                    reader.moveUp();
                }
            }

            reader.moveUp();
        }
        return ontology;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(PackageOntology.class);
    }
}
