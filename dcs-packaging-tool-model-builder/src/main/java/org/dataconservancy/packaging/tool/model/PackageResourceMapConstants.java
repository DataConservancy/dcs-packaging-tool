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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public interface PackageResourceMapConstants {
    
    public static final String DATACONS_NS_URI = "http://dataconservancy.org/ns/types/";
    
    public static final Resource PACKAGE_DESCRIPTION_TYPE = ResourceFactory
            .createResource(DATACONS_NS_URI + "PackageDescription");
    
    public static final Resource ARTIFACT_TYPE = ResourceFactory
            .createResource(DATACONS_NS_URI + "Artifact");
    
    public static final Resource PROPERTY_TYPE = ResourceFactory
            .createResource(DATACONS_NS_URI + "Property");
    
    public static final Resource RELATIONSHIP_TYPE = ResourceFactory
            .createProperty(DATACONS_NS_URI, "Relationship");

    public static final Property HAS_ARTIFACT = ResourceFactory.createProperty(DATACONS_NS_URI, "hasArtifact");
    
    public static final Property HAS_SPECIFICATION_ID = ResourceFactory.createProperty(DATACONS_NS_URI, "hasSpecificationId");
    
    public static final Property HAS_TYPE = ResourceFactory.createProperty(DATACONS_NS_URI, "hasType");
    
    public static final Property HAS_REF = ResourceFactory.createProperty(DATACONS_NS_URI, "hasRef");
    
    public static final Property HAS_ID = ResourceFactory.createProperty(DATACONS_NS_URI, "hasId");
    
    public static final Property HAS_PROPERTY = ResourceFactory.createProperty(DATACONS_NS_URI, "hasProperty");
    
    public static final Property HAS_NAME = ResourceFactory.createProperty(DATACONS_NS_URI, "hasName");
    
    public static final Property HAS_VALUE = ResourceFactory.createProperty(DATACONS_NS_URI, "hasValue");
    
    public static final Property HAS_RELATIONSHIP = ResourceFactory.createProperty(DATACONS_NS_URI, "hasRelationship");
    
    public static final Property HAS_TARGET = ResourceFactory.createProperty(DATACONS_NS_URI, "hasTarget");

    public static final Property IS_BYTE_STREAM = ResourceFactory.createProperty(DATACONS_NS_URI, "isByteStream");
    
    public static final Property IS_IGNORED = ResourceFactory.createProperty(DATACONS_NS_URI, "isIgnored");

    public static final Property REQUIRES_URI = ResourceFactory.createProperty(DATACONS_NS_URI, "requiresURI");

}