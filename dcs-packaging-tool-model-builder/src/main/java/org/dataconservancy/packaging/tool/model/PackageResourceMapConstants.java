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
import org.apache.jena.rdf.model.ResourceFactory;

public interface PackageResourceMapConstants {
    
    String DATACONS_NS_URI = "http://dataconservancy.org/ns/types/";
    
    Property HAS_ID = ResourceFactory.createProperty(DATACONS_NS_URI, "hasId");
    
    Property HAS_NAME = ResourceFactory.createProperty(DATACONS_NS_URI, "hasName");

    Property IS_BYTE_STREAM = ResourceFactory.createProperty(DATACONS_NS_URI, "isByteStream");
    
    Property IS_IGNORED = ResourceFactory.createProperty(DATACONS_NS_URI, "isIgnored");

}