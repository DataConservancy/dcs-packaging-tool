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
package org.dataconservancy.packaging.shared;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Constants needed for interpreting resource maps in a package.
 */
public interface ResourceMapConstants {
    String ORE_NS_URI = "http://www.openarchives.org/ore/terms/";

    String FEDORA_RELS_EXT_NS_URI = "info:fedora/fedora-system:def/relations-external#";

    String DCMI_TYPES_NS_URI = "http://purl.org/dc/dcmitype/";

    String SCORO_TYPES_NS_URI = "http://purl.org/spar/scoro/";

    String DATACONS_NS_URI = "http://dataconservancy.org/ns/types/";
    //public static final String OWLDOC_NS_URI = "http://wings.isi.edu/ontology/opmv/";
    String OWLDOC_NS_URI = "http://www.opmw.org/ontology/";


    Resource RESOURCE_MAP_TYPE = ResourceFactory
            .createResource(ORE_NS_URI + "ResourceMap");

    Resource AGGREGATION_TYPE = ResourceFactory.createResource(ORE_NS_URI + "Aggregation");
    
    Resource DC_PROJECT_TYPE = ResourceFactory.createResource(DATACONS_NS_URI + "Project");
    
    Resource DC_PACKAGE_TYPE = ResourceFactory.createResource(DATACONS_NS_URI + "Package");

    Resource DC_DATA_ITEM_TYPE = ResourceFactory.createResource(DATACONS_NS_URI + "DataItem");

    Resource DCMI_COLLECTION_TYPE = ResourceFactory.createResource(DCMI_TYPES_NS_URI + "Collection");

    Property AGGREGATES_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "aggregates");
    
    Property IS_AGGREGATED_BY_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "isAggregatedBy");
    
    Property DESCRIBES_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "describes");
    
    Property IS_DESCRIBED_BY_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "isDescribedBy");
    
    Property IS_METADATA_FOR_PROPERTY = ResourceFactory.createProperty(FEDORA_RELS_EXT_NS_URI, "isMetadataFor");


    Property citableLocator = ResourceFactory.createProperty(DATACONS_NS_URI, "citableLocator" );
    Property contactPerson = ResourceFactory.createProperty(SCORO_TYPES_NS_URI, "contact-person");
    Property size = ResourceFactory.createProperty(OWLDOC_NS_URI, "hasSize");
    Property fileName = ResourceFactory.createProperty(OWLDOC_NS_URI, "hasFileName");
    /**
     * URI for Jena's error-mode property
     */
    String JENA_ERROR_MODE_URI = "http://jena.hpl.hp.com/arp/properties/error-mode";

    /**
     * Jena Strict error handling
     */
    String JENA_ERROR_MODE_STRICT = "strict";
}
