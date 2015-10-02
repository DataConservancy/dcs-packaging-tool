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
     * @param type The type of the node to create an object for
     * @return identifier of object in store.
     */
    URI createObject(NodeType type);

    /**
     * Change the type of an object, keeping as many properties as possible.
     * 
     * @param object The identifier of the object to update
     * @param type The new type of node associated with the object
     */
    void updateObject(URI object, NodeType type);

    /**
     * Add a property to a domain object.
     * 
     * @param object The identifier of the object to modify.
     * @param value The value to add.
     */
    void addProperty(URI object, PropertyValue value);

    /**
     * Remove a particular property from a object.
     * 
     * @param object The identifier of the object to modify.
     * @param value The value to remove.
     */ 
    void removeProperty(URI object, PropertyValue value);

    /**
     * Remove all properties of a given type from a object.
     * 
     * @param object The identifier of the object to modify.
     * @param type The type of the properties to remove.
     */
    void removeProperty(URI object, PropertyType type);

    /**
     * @param object The identifier of an object.
     * @return All properties of an object known about by the domain profile.
     */
    List<PropertyValue> getProperties(URI object);

    /**
     * @param object The identifier of an object.
     * @param type The type of property to retrieve.
     * @return All properties of a object of a certain type.
     */
    List<PropertyValue> getProperties(URI object, PropertyType type);

    /**
     * Check for the existence of a relationship.
     * 
     * @param subject The identifier of an object.
     * @param predicate The identifier of a predicate.
     * @param object The identifier of an object.
     * @return Whether or not the relationship exists.
     */
    boolean hasRelationship(URI subject, URI predicate, URI object);

}
