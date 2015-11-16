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

package org.dataconservancy.packaging.tool.impl.generator;

import java.io.InputStream;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;

import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_ORE;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.toInputStream;

/**
 * Populates and serializes the aggregation of the package ReM
 * 
 * @author apb
 * @version $Id$
 */
public class OreReMSerializer
        implements NodeVisitor {

    @Override
    public void init(PackageModelBuilderState state) {
        Resource rem = state.manifest.createResource("");
        Resource aggregation = state.manifest.createResource("#Aggregation");

        Property rdfType =
                state.manifest.createProperty(Ontologies.NS_RDF + "type");
        Property describes =
                state.manifest.createProperty(NS_ORE + "describes");

        rem.addProperty(rdfType,
                        state.manifest.createResource(NS_ORE + "ResourceMap"));
        rem.addProperty(describes, aggregation);

        aggregation.addProperty(rdfType,
                                state.manifest.createResource(NS_ORE
                                        + "Aggregation"));

    }

    /** Add the resource that serializes the domain object to the rem */
    @Override
    public void visitNode(Node node, PackageModelBuilderState state) {
        Resource aggregation = state.manifest.getResource("#Aggregation");
        Property aggregates = state.manifest.getProperty(NS_ORE + "aggregates");

        /*
         * The resource containing the domain object serialization is stored in
         * node.getIdentifier()
         */
        if (!node.isIgnored()) {
            aggregation.addProperty(aggregates, state.manifest
                    .createResource(node.getIdentifier().toString()));
        }

    }

    @Override
    public void finish(PackageModelBuilderState state) {
        try (InputStream rem = toInputStream(state.manifest, RDFFormat.TURTLE)) {
            state.assembler.createResource("ORE-REM.ttl",
                                           PackageResourceType.ORE_REM,
                                           rem);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
