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

package org.dataconservancy.packaging.tool.ontologies;

import java.util.HashMap;
import java.util.Map;

/** Contains useful ontology constants like namespaces and prefixes. */
public interface Ontologies {

    String NS_DCS_ONTOLOGY_BOM =
            "http://dataconservancy.org/business-object-model#";

    String NS_DCS_PKGTOOL_PROFILE =
            "http://dataconservancy.org/ptg-prof/";

    String NS_DCS_PKGTOOL_PROFILE_BOM =
            "http://dataconservancy.org/ptg-profiles/dcs-bo-1.0#";

    String NS_DCS_PKGTOOL_PROFILE_PCDM =
            "http://dataconservancy.org/ptg-profiles/PCDM-1.0#";

    String NS_DCS_TYPES =
            "http://dataconservancy.org/ns/types/";

    String NS_DCTERMS = "http://purl.org/dc/terms/";

    String NS_FOAF = "http://xmlns.com/foaf/0.1/";

    String NS_PCDM = "http://pcdm.org/models#";

    String NS_ORE =
            "http://www.openarchives.org/ore/terms/";

    String NS_RDF =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    String NS_RDFS =
            "http://www.w3.org/2000/01/rdf-schema#";

    String NS_XSD = "http://www.w3.org/2001/XMLSchema#";

    String NS_DCAM = "http://purl.org/dc/dcam/";

    String NS_DCMITYPE = "http://purl.org/dc/dcmitype/";

    String NS_DCELEMENTS = "http://purl.org/dc/elements/1.1/";

    String NS_LDP = "http://www.w3.org/ns/ldp#";

    String NS_OWL2 = "http://www.w3.org/2002/07/owl#";

    String NS_RDFG = "http://www.w3.org/2004/03/trix/rdfg-1/";


    /**
     * Central map of prefixes, consistent with all ontologies used herein.
     * <p>
     * May be used to aid serialization in jena RIOT via
     * <code>PrefixMapFactory.create(Ontologies.PREFIX_MAP))</code>.
     * </p>
     */
    @SuppressWarnings("serial")
    Map<String, String> PREFIX_MAP =
            new HashMap<String, String>() {

                {
                    put("bom", NS_DCS_ONTOLOGY_BOM);
                    put("boprof", NS_DCS_PKGTOOL_PROFILE_BOM);
                    put("dcterms", NS_DCTERMS);
                    put("dcs", NS_DCS_TYPES);
                    put("foaf", NS_FOAF);
                    put("ore", NS_ORE);
                    put("pcdm", NS_PCDM);
                    put("prof", NS_DCS_PKGTOOL_PROFILE);
                }
            };
}
