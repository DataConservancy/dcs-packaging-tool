package org.dataconservancy.packaging.tool.api;

import java.net.URI;
import java.util.List;

import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;

/**
 * Maintain a set of domain objects described by a domainm profile and allow
 * their properties to be manipulated.
 */
public interface DomainProfileObjectStore {
    /**
     * Create an object in the store for a given type.
     * 
     * @param type
     * @return identifier of object in store.
     */
    URI createObject(NodeType type);

    /**
     * Change the type of an object, keeping as many properties as possible.
     * 
     * @param type
     */
    void updateObject(NodeType type);

    /**
     * Add a property to a domain object.
     * 
     * @param object
     * @param value
     */
    void addProperty(URI object, PropertyValue value);

    /**
     * Remove a particular property from a object.
     * 
     * @param object
     * @param value
     */
    void removeProperty(URI object, PropertyValue value);

    /**
     * Remove all properties of a given type from a object.
     * 
     * @param object
     * @param type
     */
    void removeProperty(URI object, PropertyType type);

    /**
     * @param object
     * @return All properties of an object known about by the domain profile.
     */
    List<PropertyValue> getProperties(URI Object);

    /**
     * @param object
     * @param type
     * @return All properties of a object of a certain type.
     */
    List<PropertyValue> getProperties(URI object, PropertyType type);

    /**
     * Check for the existence of a relationship.
     * 
     * @param subject
     * @param predicate
     * @param object
     * @return Whether or not the relationship exists.
     */
    boolean hasRelationship(URI subject, URI predicate, URI object);

}
