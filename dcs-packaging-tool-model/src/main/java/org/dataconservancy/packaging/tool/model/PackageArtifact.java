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


import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PackageArtifact {
    private String id;
    private String type;
    private ArtifactReference artifactRef;
    private boolean isByteStream;
    private boolean isIgnored;
    
    /**
     * Key-value pair set of properties describing the artifact.
     * Examples: (name: DataItem1), (created-date:12-12-2010), etc.
     */
    private Map<String, Set<Object>> properties;

    /**
     * A set of relationships between this artifact and other artifacts.
     * Keys are relationship names, Set<String> holds ids of the related artifact)
     * 
     * By default a PackageArtifact is not ignored.
     */
    private List<PackageRelationship> relationships;

    public PackageArtifact() {
        properties = new HashMap<>();
        relationships = new ArrayList<>();
        this.isIgnored = false;
    }
    
    /**
     * Returns the id of the artifact. This id uniquely identifies an artifact within a PackageDescription. It does not
     * serve as identifier of the object, which it represents.
     * @return The string representing the ID of the artifact.
     */
    public String getId() {
        return id;
    }
  
    /**
     * Set the unique identifier of this artifact.
     * @param id The unique identifier to assign to the artifact
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @return the Type of the artifact
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set the type of this artifact.
     * @param type The type to assign to the artifact
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get the ArtifactReference for this artifact
     * @return the artifactRef
     */
    public ArtifactReference getArtifactRef() {
        return artifactRef;
    }


    /**
     * Sets the reference to artifact usually a path to a file or folder. String must represent a valid URI
     * @param artifactRefString The string representing a reference to the object this artifact represents
     */
    public void setArtifactRef(String artifactRefString) {
        this.artifactRef = new ArtifactReference(artifactRefString);
    }


    /**
     * Returns the map of properties as map from properties' names to set of its values.
     * @return  the map of properties from properties' names to set of its values.
     */
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    /**
     * Removes any empty properties from the artifact.
     */
    public void pruneEmptyProperties() {
        Iterator<Map.Entry<String, Set<Object>>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<Object>> entry = iterator.next();
            if(entry.getValue() == null || entry.getValue().isEmpty()){
                iterator.remove();
            }
        }
    }

    /**
     * Remove property with name {@code propertyName} from the properties maps.
     * @param propertyName the property name
     */
    public void removeProperty(String propertyName) {
        properties.remove(propertyName);
    }

    /**
     * Remove all properties from the properties maps.
     */
    public void removeAllProperties() {
        properties.clear();
    }

    /**********************************************************
     * for String type property (ones with set of strings as value)
     **********************************************************/
    /**
     * Returns values for properties whose values are expected to of String type. These properties are considered
     * Simple Property.
     * <p>
     * In event that the values found are not of String type, {@code null is returned}
     * </p>
     * @param propertyName the name of the property
     * @return  values for properties whose values are expected to of String type
     */
    public Set<String> getSimplePropertyValues(String propertyName) {
        if (properties.get(propertyName) != null) {
            Set<String> values = new HashSet<>();
            try {
                for (Object obj : properties.get(propertyName)) {
                    values.add((String)obj);
                }
            } catch (ClassCastException e) {
                return null;
            }
            return values;
        }
        return null;
    }

    /**
     * Allows adding of a property in its entirety, names and all of its values.
     * <p>
     * If a property of identical name exist, this method will overwrite the old values with the new values.
     * </p>
     * <p>
     * {@code null} values is accepted.
     * </p>
     * @param propertyName  the property name
     * @param values  the values
     */
    public void setSimplePropertyValues(String propertyName, Set<String> values) {

        Set<Object> valueObjects = new HashSet<>();
        if (values != null) {
            valueObjects.addAll(values);
        }
        properties.put(propertyName, valueObjects);

    }

    public void setSimplePropertyValues(String propertyName, String ... values) {
        Set<Object> valueObjects = new HashSet<>();
        valueObjects.addAll(Arrays.asList(values));
        properties.put(propertyName, valueObjects);
    }


    /**
     * Accept a single String value for the named property. If no property of that name exists, one will be created, and
     * given the {@code propertyValue} as its first value.
     * @param propertyName the property name
     * @param propertyValue  the property value
     */
    public void addSimplePropertyValue(String propertyName, String propertyValue) {
        if (properties.get(propertyName) == null) {
            properties.put(propertyName, new HashSet<>());
        }
        properties.get(propertyName).add(propertyValue);
    }

    /*********************************************************************
     * for Map type property (ones with set of Map<String,String> as value
     *********************************************************************/

    /**
     * Returns a Set of {@link org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup} for property
     * whose values are a groups of other values. whose name is specified by {@code propertyGroupName}.
     * is {@code propertyGroupName}.
     * <p>
     * Returns {@code null} if no property of such name exists, or if one exist but does not have PropertyValueGroup as
     * value type.
     * </p>
     * @param propertyGroupName the property group name
     * @return a Set of {@link org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup} for property
     * whose values are a groups of other values. whose name is specified by {@code propertyGroupName}.
     */
    public Set<PropertyValueGroup> getPropertyValueGroups(String propertyGroupName) {
        if (properties.get(propertyGroupName) != null) {
            Set<PropertyValueGroup> propertyGroups = new HashSet<>();
            for (Object object : properties.get(propertyGroupName)) {
                if (object instanceof PropertyValueGroup) {
                    propertyGroups.add((PropertyValueGroup) object);

                } else {
                    return null;
                }
            }
            return propertyGroups;
        }
        return null;
    }

    /**
     * Add a new PropertyValueGroup to property with name {@code propertyGroupName}.
     * @param propertyGroupName  the property group name
     * @param propertyValueGroup the PropertyValueGroup
     */
    public void addPropertyValueGroup(String propertyGroupName, PropertyValueGroup propertyValueGroup) {
        if (properties.get(propertyGroupName) == null) {
            properties.put(propertyGroupName, new HashSet<>());
        }
        properties.get(propertyGroupName).add(propertyValueGroup);
    }
    
    /**
     * Sets the property values groups for a given name. Will remove any previous groups that already existed.
     * @param propertyGroupName The name of the property group.
     * @param propertyValueGroups The set of property groups assigned to the given name.
     */
    public void setPropertyValueGroups(String propertyGroupName, Set<PropertyValueGroup> propertyValueGroups) {
        Set<Object> valueObjects = new HashSet<>();
        if (propertyValueGroups != null) {
            valueObjects.addAll(propertyValueGroups);
        }
        properties.put(propertyGroupName, valueObjects);
    }
    
    public boolean hasSimpleProperty(String key) {
        if (!properties.containsKey(key) || properties.get(key).isEmpty()) {
            return false;
        }
        
        return !hasPropertyValueGroup(key);
    }

    public boolean hasPropertyValueGroup(String key) {
        if (!properties.containsKey(key) || properties.get(key).isEmpty()) {
            return false;
        }
        return properties.get(key).iterator().next() instanceof PropertyValueGroup;
    }

    /**
     * @return the relationships
     */
    public List<PackageRelationship> getRelationships() {
        return relationships;
    }
    
    /**
     * @param relationships the relationships to set
     */
    public void setRelationships(List<PackageRelationship> relationships) {
        this.relationships = relationships;
    }

    public void setRelationships(PackageRelationship... relationships) {
        this.relationships = new ArrayList<>(Arrays.asList(relationships));
    }

    /**
     * Convenience method used by the builder to find a relationship for a given name.
     * @param name The name of the relationship to find
     * @return The first PackageRelationship matching the given name, or null if none are found
     */
    public PackageRelationship getRelationshipByName(String name) {
        PackageRelationship relationship = null;

        for (PackageRelationship rel : relationships) {
            if (rel.getName().equals(name)) {
                relationship = rel;
                break;
            }
        }

        return relationship;
    }

    /**
     * Convienence method that returns all relationships on the artifact with the given name.
     * @param name The name of the relationship to find.
     * @return A list of all relationships with the given name, or an empty list if none are found.
     */
    public List<PackageRelationship> getAllRelationshipsByName(String name) {
        List<PackageRelationship> matchingRelationships = new ArrayList<>();

        for (PackageRelationship rel : relationships) {
            if (rel.getName().equals(name)) {
                matchingRelationships.add(rel);
            }
        }

        return matchingRelationships;
    }

    /**
     * Convenience method that finds a relationship with the provided name and targets.
     * @param name The name of the relationship to find.
     * @param targets The targets of the relationship to find.
     * @return The package relationship with the given name that has the provided targets or null if none can be found.
     */
    public PackageRelationship findRelationship(String name, String... targets) {
        PackageRelationship relationship = null;

        for (PackageRelationship rel : relationships) {
            if (rel.getName().equals(name) && rel.getTargets().containsAll(Arrays.asList(targets))) {
                relationship = rel;
                break;
            }
        }

        return relationship;
    }

    /**
     * Returns a set of the all relationship names currently on the artifact
     * @return The set of all the relationships currently on the artifact or an empty set if there are none
     */
    public Set<String> getAllRelationshipNamesOnArtifact() {
        Set<String> names = new HashSet<>();

        for (PackageRelationship rel : relationships) {
            names.add(rel.getName());
        }

        return names;
    }

    /**
     * Convenience method that removes a relationship with the given name.
     * @param name The name of the relationship to remove.
     */
    public void removeRelationshipByName(String name) {
        Iterator<PackageRelationship> relationshipIterator = relationships.iterator();
        while (relationshipIterator.hasNext()) {
            if (relationshipIterator.next().getName().equals(name)) {
                relationshipIterator.remove();
            }
        }
    }

    /**
     * Remove a specific relationship identified by its name and target.
     * In the case of either, or both, of the name and target is null or empty, this function is a no-op.
     * @param name Name of the relationship to be removed
     * @param target target of the relationship to be removed
     */
    public void removeRelationship(String name, String target) {
        if (name == null || name.isEmpty() || target == null || target.isEmpty()) {
            return;
        }
        Set<PackageRelationship> relationshipsToRemove = new HashSet<>();
        Iterator<PackageRelationship> relationshipIterator = relationships.iterator();
        while (relationshipIterator.hasNext()) {
            PackageRelationship relationship = relationshipIterator.next();
            if (relationship.getName().equals(name)) {
                for (String relTarget : relationship.getTargets()) {
                    if (relTarget.equals(target)) {
                        relationshipsToRemove.add(relationship);
                    }
                }
            }
        }
        relationships.removeAll(relationshipsToRemove);
    }
    /**
     * Indicates whether the artifact represents a bytestream.
     * @return {@code true} if artifact represents a bytestream. {@code false} if artifact does not represent a
     * bytestream.
     */
    public boolean isByteStream() {
        return isByteStream;
    }

    public void setByteStream(boolean isByteStream) {
        this.isByteStream = isByteStream;
    }
    
    /**
     * An ignored artifact should be preserved by any processing and ignored only when interpreting the semantics of the
     * holding PackageDescription.
     * 
     * @return whether or not the artifact should be ignored
     */
    public boolean isIgnored() {
        return isIgnored;
    }

    public void setIgnored(boolean status) {
        this.isIgnored = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageArtifact)) return false;

        PackageArtifact artifact = (PackageArtifact) o;

        if (isByteStream != artifact.isByteStream) return false;
        if (isIgnored != artifact.isIgnored) return false;
        if (artifactRef != null ? !artifactRef.equals(artifact.artifactRef) : artifact.artifactRef != null)
            return false;
        if (id != null ? !id.equals(artifact.id) : artifact.id != null) return false;
        if (properties != null ? !properties.equals(artifact.properties) : artifact.properties != null) return false;
        if (relationships != null ? !relationships.equals(artifact.relationships) : artifact.relationships != null)
            return false;
        if (type != null ? !type.equals(artifact.type) : artifact.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (artifactRef != null ? artifactRef.hashCode() : 0);
        result = 31 * result + (isByteStream ? 1 : 0);
        result = 31 * result + (isIgnored ? 1 : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (relationships != null ? relationships.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "Artifact{id=" + id + " type=" + type + " artifactRef=" + artifactRef + " isByteStream=" + isByteStream 
                + " properties=" + properties.toString() + " relationships=" + relationships.toString() + "}"; 
    }

    /**
     * Describes a Map of String to Set of Strings which represents a group of related artifact properties' values. Each
     * map entry represents a sub property which makes up part of a value group for a top-level property,
     */
    public static class PropertyValueGroup {
        private Map<String, Set<String>> valueGroup;

        public PropertyValueGroup() {
            valueGroup = new HashMap<>();
        }

        /**
         * Add new String value {@code subPropertyValue} to a sub property with name {@code subPropertyName}
         * @param subPropertyName  the sub-property name
         * @param subPropertyValue the sub-property value
         */
        public void addSubPropertyValue(String subPropertyName, String subPropertyValue) {
            if (subPropertyName == null || subPropertyValue == null) {
                throw new IllegalArgumentException("subPropertyName and subPropertyValue cannot be null;");
            }
            if (valueGroup.get(subPropertyName) == null) {
                valueGroup.put(subPropertyName, new HashSet<>());
            }
            valueGroup.get(subPropertyName).add(subPropertyValue);
        }

        /**
         * Set values for sub property named {@code subPropertyName}. If there exist values for a subProperty of the same
         * name, those values will be overwritten by the new ones.
         * @param subPropertyName  the sub-property name
         * @param subPropertyValues   the sub-property value
         */
        public void setSubPropertyValues(String subPropertyName, Set<String> subPropertyValues) {
            if (subPropertyName == null) {
                throw new IllegalArgumentException("subPropertyName cannot be null or empty.");
            }
            valueGroup.put(subPropertyName, subPropertyValues);
        }

        /**
         * Returns value of subProperty of name {@code subPropertyName}
         * @param subPropertyName the sub-property name
         * @return   value of subProperty
         */
        public Set<String> getSubPropertyValues(String subPropertyName) {
            return valueGroup.get(subPropertyName);
        }

        /**
         * Returns the names of all of the sub properties contains in this value group.
         * @return  the names of all of the sub properties contains in this value group
         */
        public Set<String> getSubPropertyNames() {
            return this.valueGroup.keySet();
        }

        public void removeSubProperty(String subPropertyName) {
            valueGroup.remove(subPropertyName);
        }
        
        @Override
        public int hashCode() {
            return valueGroup != null ? valueGroup.hashCode() : 0;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PropertyValueGroup)) return false;

            PropertyValueGroup group = (PropertyValueGroup) o;

            if (valueGroup != null ? !valueGroup.equals(group.valueGroup) : group.valueGroup != null)
                return false;

            return true;
        }
        
        @Override
        public String toString() {
            return "PropertyValueGroup {properties=" + valueGroup.toString() + "}";
        }
    }
}