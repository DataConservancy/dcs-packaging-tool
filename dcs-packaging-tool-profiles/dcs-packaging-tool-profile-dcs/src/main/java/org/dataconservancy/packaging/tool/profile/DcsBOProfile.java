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

package org.dataconservancy.packaging.tool.profile;

import java.io.FileOutputStream;

import java.net.URI;

import java.util.HashMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

import org.dataconservancy.packaging.tool.impl.DomainProfileRdfTransformService;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;

/** Default Data Conservancy business object profile */
public class DcsBOProfile
        extends DomainProfile {

    /*
     * TODO: Maybe it makes sense to have a central location for prefix maps,
     * project-wide
     */
    @SuppressWarnings("serial")
    private static final PrefixMap PREFIX_MAP = PrefixMapFactory
            .create(new HashMap<String, String>() {

                {
                    put("prof", "http://www.dataconservancy.org/ptg-prof/");
                    put("datacons", "http://dataconservancy.org/ns/types/");
                }
            });

    public DcsBOProfile() {
        setDomainIdentifier(URI.create("http://example.org/myDomainIdentifier"));
        setIdentifier(URI.create("http://example.org/myProfileIdentifier"));
    }

    /**
     * Serialize the profile to a file.
     * 
     * @param args
     *        Expects two arguments args[0] is the name of a file, and args[1]
     *        is a RIOT name for a serialization format (an {@link RDFFormat},
     *        e.g. TURTLE_PRETTY).
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        DomainProfileRdfTransformService xform =
                new DomainProfileRdfTransformService();

        Model model = xform.transformToRdf(new DcsBOProfile(), "");

        try (FileOutputStream out = new FileOutputStream(args[0])) {

            RDFDataMgr.createGraphWriter((RDFFormat) RDFFormat.class
                    .getDeclaredField(args[1]).get(null))
                    .write(out, model.getGraph(), PREFIX_MAP, null, null);

        }
    }
}
