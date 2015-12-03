/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.packaging.tool.ontologies;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides access to well-known schemas and ontologies used to interpret RDF instance documents.
 */
public class ModelResources {

    /**
     * Map of model resources, keyed by namespace.
     */
    public static final Map<String, String> RESOURCE_MAP =
            new HashMap<String, String>() {
                {
                    put(Ontologies.NS_DCAM, "/dcam.ttl");
                    put(Ontologies.NS_DCELEMENTS, "/dcelements.ttl");
                    put(Ontologies.NS_DCMITYPE, "/dcmitype.ttl");
                    put(Ontologies.NS_DCS_ONTOLOGY_BOM, "/dcs-bo.ttl");
                    put(Ontologies.NS_DCTERMS, "/dcterms.ttl");
                    put(Ontologies.NS_FOAF, "/foaf.ttl");
                    put(Ontologies.NS_LDP, "/ldp.ttl");
                    put(Ontologies.NS_ORE, "/ore.ttl");
                    put(Ontologies.NS_OWL2, "/owl.ttl");
                    put(Ontologies.NS_PCDM, "/pcdm.ttl");
                    put(Ontologies.NS_RDF, "/rdf.ttl");
                    put(Ontologies.NS_RDFG, "/rdfg.ttl");
                    put(Ontologies.NS_RDFS, "/rdfs.ttl");
                    put(Ontologies.NS_XSD, "/xsd.ttl");
                }
            };

    /**
     * Obtain a Model for the supplied namespace.  See {@link Ontologies} for commonly used namespaces.
     *
     * @param modelNs the namespace for the model
     * @return the {@code Model} instance, or {@code null} if it doesn't exist
     * @throws RuntimeException if the underlying resource for the {@code Model} is not found, or not readable
     */
    public static Model get(String modelNs) {
        if (!RESOURCE_MAP.containsKey(modelNs)) {
            return null;
        }

        Model m = ModelFactory.createDefaultModel();

        URL r = ModelResources.class.getResource(RESOURCE_MAP.get(modelNs));
        if (r == null) {
            throw new RuntimeException("Model resource '" + RESOURCE_MAP.get(modelNs) + "' cannot be found for " +
                    "namespace '" + modelNs + "'");
        }

        try {
            m.read(r.openStream(), modelNs, "TTL");
        } catch (Exception e) {
            throw new RuntimeException("Error reading model resource '" + RESOURCE_MAP.get(modelNs) + "': " +
                    e.getMessage(), e);
        }

        return m;
    }

}
