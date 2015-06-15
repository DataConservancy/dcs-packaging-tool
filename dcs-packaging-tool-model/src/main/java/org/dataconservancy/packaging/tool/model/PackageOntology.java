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
package org.dataconservancy.packaging.tool.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Package Ontologies define the set of artifacts, properties, and relationships within a PackageDescription,
 * as well as the fundamental domain constraints among them.
 */
public class PackageOntology {

    String id;
    Set<String> artifactTypes;
    Map<String, Set<Property>> artifactProperties;
    Map<String, Set<Relationship>> artifactRelationships;
    Set<PropertyType> customPropertyTypes;

    protected static final String PROPERTY_TO_NONEXISTING_TYPE_ERR_MSG =
            "Artifact type does not exist in this ontology. If it should be " +
            "included in this ontology, add the artifactType to the ontology first (using addArtifactType() " +
            "method), then add allowable property to the type using this method.";

    protected static final String RELATIONSHIP_TO_NONEXISTING_TYPE_ERR_MSG =
            "Artifact type does not exist in this ontology. If it should be " +
            "included in this ontology, add the artifactType to the ontology first (using addArtifactType() " +
            "method), then add allowable relationship to the type using this method.";

    /**
     * Constructs an empty ontology instance.
     */
    public PackageOntology() {
        this.artifactTypes = new HashSet<>();
        this.customPropertyTypes = new HashSet<>();
        this.artifactProperties = new HashMap<>();
        this.artifactRelationships = new HashMap<>();
    }

    public void setId(String id) {
        this.id = id;
    }
    /**
     * Add an Artifact Type to ontology instance.
     * @param type the Artifact Type
     */
    public void addArtifactType(String type) {
        artifactTypes.add(type);
        artifactProperties.put(type, new HashSet<>());
        artifactRelationships.put(type, new HashSet<>());

    }

    /**
     * Adds an allowable property to existing Artifact Type. If the Artifact Type specified in the argument does not
     * exist in the ontology instance, {@code IllegalArgumentException} will be thrown.
     * @param artifactType The Artifact Type which this property can be associated with.
     * @param propertyName  the property name
     * @param valueType Simple types, such as String, integer, DateTime, etc.
     * @param minOccurrence the minimal occurrence count for this property.
     * @param maxOccurrence the maximum occurrence count for this property.
     * @param isSystemSupplied A flag denoting whether the property is supplied by the system, false if the user provides the property
     * @param isInheritable A flag denoting whether or not the value of the property can be inherited by children of the artifact
     */
    public void addArtifactProperty(String artifactType, String propertyName, String valueType,
                                    int minOccurrence, int maxOccurrence, boolean isSystemSupplied, boolean isInheritable
    ) {
        if ( !artifactTypes.contains(artifactType)) {
            throw new IllegalArgumentException(PROPERTY_TO_NONEXISTING_TYPE_ERR_MSG);
        }
        Property newProperty = new Property(propertyName, valueType, minOccurrence, maxOccurrence, isSystemSupplied, isInheritable);
        artifactProperties.get(artifactType).add(newProperty);
    }

    /**
     * Adds an allowable relationship to existing Artifact Type. If the Artifact Type specified in the argument does
     * not exist in the ontology instance, {@code IllegalArgumentException} will be thrown
     * @param artifactType  The Artifact Type which this relationship can be associated with
     * @param relationshipName Name of relationship
     * @param relatedArtifactType The Type of Artifact that can be related to the given Artifact Type via this relationship.
     * @param minOccurrence the minimal occurrence count for this relationship.
     * @param maxOccurrence the maximum occurrence count for this relationship.
     * @param isHierarchical A flag denoting that the relationship contains heirarchical information between two artifact.
     */
    public void addArtifactRelationship(String artifactType, String relationshipName, String relatedArtifactType,
                                    boolean isHierarchical, int minOccurrence, int maxOccurrence) {
        if ( !artifactTypes.contains(artifactType)) {
            throw new IllegalArgumentException(RELATIONSHIP_TO_NONEXISTING_TYPE_ERR_MSG);
        }
        Relationship newRelationship = new Relationship(relationshipName, relatedArtifactType, isHierarchical, minOccurrence, maxOccurrence);
        artifactRelationships.get(artifactType).add(newRelationship);
    }

    /**
     * Gets the type of the given property on the given artifact, or null if the property doesn't exist on the artifact.
     * @param artifactType The type of the artifact that contains the property.
     * @param propertyName The name of the property to retrieve the type from.
     * @param complexPropertyName The name of the complex property the property belongs to, or null if it doesn't belong to a complex property.
     * @return The type name of the property, or null if the property isn't found on the artifact.
     */
    public String getPropertyType(String artifactType, String complexPropertyName, String propertyName) {
        String propertyType = null;

        //If the complex property name is specified find that type first, then find the property type, otherwise find the property type.
        if (complexPropertyName != null && !complexPropertyName.isEmpty()) {
            Set<Property> properties = artifactProperties.get(artifactType);
            if (properties != null) {
                for (Property property : properties) {
                    if (property != null && property.getName().equalsIgnoreCase(complexPropertyName)) {
                        for (PropertyType customType : customPropertyTypes) {
                            if (customType.getName().equals(property.valueType)) {
                                propertyType = customType.getTypeFieldsMap().get(propertyName).getValueType();
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            Set<Property> properties = artifactProperties.get(artifactType);
            if (properties != null) {
                for (Property property : properties) {
                    if (property != null && property.getName().equalsIgnoreCase(propertyName)) {
                        propertyType = property.getValueType();
                        break;
                    }
                }
            }
        }


        return propertyType;
    }

    /**
     * Gets id of the ontology.
     * @return The identifier of the ontology.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get a set of artifact types allowed in this ontology instance.
     * @return  a set of artifact types allowed in this ontology instance
     */
    public Set<String> getArtifactTypes() {
        return artifactTypes;
    }

    /**
     * Get the set of custom property types defined in this ontology instance.
     * @return  the set of custom property types defined in this ontology instance
     */
    public Set<PropertyType> getCustomPropertyTypes() {
        return customPropertyTypes;
    }

    /**
     * <p>
     * Get a set of {@link org.dataconservancy.packaging.tool.model.PackageOntology.Property} associated with the
     * given {@code artifactType}.
     * </p>
     * Given an Artifact Type that does not exist in the ontology instance, this method returns {@code null}.
     * @param artifactType The type of artifact to retrieve the properties for.
     * @return The set of properties associated with the given artifact type.
     */
    public Set<Property> getProperties(String artifactType) {
        return artifactProperties.get(artifactType);
    }

    /**
     * <p>
     * Get a set of {@link org.dataconservancy.packaging.tool.model.PackageOntology.Relationship} associated with the
     * given {@code artifactType}.
     * <p>
     * Given an Artifact Type that does not exist in the ontology instance, this method returns {@code null}.
     * @param artifactType The type of the artifact to retrieve the relationships for.
     * @return The set of all relationships associated with the given artifact type.
     */
    public Set<Relationship> getRelationships(String artifactType) {
        return artifactRelationships.get(artifactType);
    }

    @Override
    public String toString() {
        return "PackageOntology{" +
                "id='" + id + '\'' +
                ", artifactTypes=" + artifactTypes +
                ", artifactProperties=" + artifactProperties +
                ", artifactRelationships=" + artifactRelationships +
                ", customPropertyTypes=" + customPropertyTypes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageOntology)) return false;

        PackageOntology ontology = (PackageOntology) o;

        if (artifactProperties != null ? !artifactProperties.equals(ontology.artifactProperties) : ontology.artifactProperties != null)
            return false;
        if (artifactRelationships != null ? !artifactRelationships.equals(ontology.artifactRelationships) : ontology.artifactRelationships != null)
            return false;
        if (artifactTypes != null ? !artifactTypes.equals(ontology.artifactTypes) : ontology.artifactTypes != null)
            return false;
        if (id != null ? !id.equals(ontology.id) : ontology.id != null) return false;
        if (customPropertyTypes != null ? !customPropertyTypes.equals(ontology.customPropertyTypes) : ontology.customPropertyTypes != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (artifactTypes != null ? artifactTypes.hashCode() : 0);
        result = 31 * result + (artifactProperties != null ? artifactProperties.hashCode() : 0);
        result = 31 * result + (artifactRelationships != null ? artifactRelationships.hashCode() : 0);
        result = 31 * result + (customPropertyTypes != null ? customPropertyTypes.hashCode() : 0);
        return result;
    }

    /**
     * Defines non-standard types used in the Ontology.
     */
    public class PropertyType {
        private String name;
        private Map<String, Property> typeFieldsMap;

        public PropertyType() {
            this.typeFieldsMap = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<String> getFieldNames() {
            return typeFieldsMap.keySet();
        }

        public Map<String, Property> getTypeFieldsMap() {
            return typeFieldsMap;
        }

        public void addField(Property field) {
            typeFieldsMap.put(field.getName(), field);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PropertyType)) return false;

            PropertyType that = (PropertyType) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (typeFieldsMap != null ? !typeFieldsMap.equals(that.typeFieldsMap) : that.typeFieldsMap != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (typeFieldsMap != null ? typeFieldsMap.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PropertyType{" +
                    "name='" + name + '\'' +
                    ", typeFieldsMap=" + typeFieldsMap +
                    '}';
        }
    }


    /**
     * Defines aspects of a property to be described in PackageOntology.
     */
    public class Property {
        private String  name;
        private String valueType;
        private int minOccurrence;
        private int maxOccurrence;
        private boolean isSystemSupplied;
        private boolean isInheritable;

        public Property() {}

        public Property(String name, String valueType, int minOccurrence, int maxOccurrence, boolean isSystemSupplied,
                        boolean isInheritable) {
            this.name = name;
            this.valueType = valueType;
            this.minOccurrence = minOccurrence;
            this.maxOccurrence = maxOccurrence;
            this.isInheritable = isInheritable;
            this.isSystemSupplied = isSystemSupplied;

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValueType() {
            return valueType;
        }

        public void setValueType(String valueType) {
            this.valueType = valueType;
        }

        public int getMinOccurrence() {
            return minOccurrence;
        }

        public void setMinOccurrence(int minOccurrence) {
            this.minOccurrence = minOccurrence;
        }

        public int getMaxOccurrence() {
            return maxOccurrence;
        }

        public void setMaxOccurrence(int maxOccurrence) {
            this.maxOccurrence = maxOccurrence;
        }

        /**
         * indicates that whether the property's value is expected to be supplied by the system.
         * @return boolean
         */
        public boolean isSystemSupplied() {
            return isSystemSupplied;
        }

        /**
         * indicates that whether the property's value is expected to be supplied by the system.
         * @param isSystemSupplied  boolean
         */
        public void setSystemSupplied(boolean isSystemSupplied) {
            this.isSystemSupplied = isSystemSupplied;
        }

        /**
         * indicates that whether the property's value could be cascaded down to a similar property on an artifact's children.
         * @return  boolean
         */
        public boolean isInheritable() {
            return isInheritable;
        }

        /**
         * indicates that whether the property's value could be cascaded down to a similar property on an artifact's children.
         * @param isInheritable boolean
         */
        public void setInheritable(boolean isInheritable) {
            this.isInheritable = isInheritable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Property)) return false;

            Property property = (Property) o;

            if (isInheritable != property.isInheritable) return false;
            if (isSystemSupplied != property.isSystemSupplied) return false;
            if (maxOccurrence != property.maxOccurrence) return false;
            if (minOccurrence != property.minOccurrence) return false;
            if (name != null ? !name.equals(property.name) : property.name != null) return false;
            if (valueType != null ? !valueType.equals(property.valueType) : property.valueType != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
            result = 31 * result + minOccurrence;
            result = 31 * result + maxOccurrence;
            result = 31 * result + (isSystemSupplied ? 1 : 0);
            result = 31 * result + (isInheritable ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Property{" +
                    "name='" + name + '\'' +
                    ", valueType='" + valueType + '\'' +
                    ", minOccurrence=" + minOccurrence +
                    ", maxOccurrence=" + maxOccurrence +
                    ", isSystemSupplied=" + isSystemSupplied +
                    ", isInheritable=" + isInheritable +
                    '}';
        }
    }

    /**
     * Defines aspects of relationships to be describe in PackageOntology.
     */
    public class Relationship {
        private String name;
        private String relatedArtifactType;
        private int minOccurrence;
        private int maxOccurrence;
        private boolean isHierarchical;

        public Relationship() {
        }

        public Relationship(String name, String relatedArtifactType, boolean isHierarchical, int minOccurrence, int maxOccurrence) {
            this.name = name;
            this.relatedArtifactType = relatedArtifactType;
            this.minOccurrence = minOccurrence;
            this.maxOccurrence = maxOccurrence;
            this.isHierarchical = isHierarchical;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRelatedArtifactType() {
            return relatedArtifactType;
        }

        public void setRelatedArtifactType(String relatedArtifactType) {
            this.relatedArtifactType = relatedArtifactType;
        }

        public int getMinOccurrence() {
            return minOccurrence;
        }

        public void setMinOccurrence(int minOccurrence) {
            this.minOccurrence = minOccurrence;
        }

        public int getMaxOccurrence() {
            return maxOccurrence;
        }

        public void setMaxOccurrence(int maxOccurrence) {
            this.maxOccurrence = maxOccurrence;
        }

        public boolean isHierarchical() {
            return isHierarchical;
        }

        public void setHierarchical(boolean isHierarchical) {
            this.isHierarchical = isHierarchical;
        }

        @Override
        public String toString() {
            return "Relationship{" +
                    "name='" + name + '\'' +
                    ", relatedArtifactType='" + relatedArtifactType + '\'' +
                    ", minOccurrence=" + minOccurrence +
                    ", maxOccurrence=" + maxOccurrence +
                    ", isHierarchical=" + isHierarchical +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Relationship)) return false;

            Relationship that = (Relationship) o;

            if (isHierarchical != that.isHierarchical) return false;
            if (maxOccurrence != that.maxOccurrence) return false;
            if (minOccurrence != that.minOccurrence) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (relatedArtifactType != null ? !relatedArtifactType.equals(that.relatedArtifactType) : that.relatedArtifactType != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (relatedArtifactType != null ? relatedArtifactType.hashCode() : 0);
            result = 31 * result + minOccurrence;
            result = 31 * result + maxOccurrence;
            result = 31 * result + (isHierarchical ? 1 : 0);
            return result;
        }
    }

}
