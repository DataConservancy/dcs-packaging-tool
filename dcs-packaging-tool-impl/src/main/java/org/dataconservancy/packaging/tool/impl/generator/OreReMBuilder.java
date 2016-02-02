/*
 * Copyright 2015 Johns Hopkins University
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.packaging.tool.impl.generator;

import java.io.InputStream;

import java.net.URI;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;

import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.determineSerialization;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_ORE;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_IANA;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_LDP;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.toInputStream;

/**
 * Populates and serializes the aggregation of the package ReM
 * 
 * @author apb
 * @version $Id$
 */
public class OreReMBuilder
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

        aggregation
                .addProperty(rdfType,
                             state.manifest
                                     .createResource(NS_ORE + "Aggregation"));

    }

    /** Add the resource that serializes the domain object to the rem */
    @Override
    public void visitNode(Node node, PackageModelBuilderState state) {
        Resource aggregation = state.manifest.getResource("#Aggregation");
        Resource ldpContainer =
                state.manifest.getResource(NS_LDP + "Container");
        Property aggregates = state.manifest.getProperty(NS_ORE + "aggregates");
        Property rdfType =
                state.manifest.getProperty(Ontologies.NS_RDF + "type");
        Property ianaDescribes =
                state.manifest.getProperty(NS_IANA + "describes");
        Property ldpContains = state.manifest.getProperty(NS_LDP + "contains");

        if (!node.isIgnored()) {
            URI serializationLocation = state.domainObjectSerializationLocations
                    .get(node.getIdentifier());

            aggregation.addProperty(aggregates,
                                    state.manifest
                                            .createResource(serializationLocation
                                                    .toString()));

            if (node.getFileInfo() != null && node.getFileInfo().isFile()) {
                Resource resource = state.manifest
                        .createResource(serializationLocation.toString());
                resource.addProperty(ianaDescribes,
                                     state.manifest.getResource(node
                                             .getFileInfo().getLocation()
                                             .toString()));
            } else {
                Resource resource = state.manifest
                        .createResource(serializationLocation.toString());
                resource.addProperty(rdfType, ldpContainer);
                if (node.hasChildren()) {
                    for (Node child : node.getChildren()) {
                        if (!child.isIgnored()) {
                            URI childSerializationLocation =
                                    state.domainObjectSerializationLocations
                                            .get(child.getIdentifier());

                            /*
                             * Point to file content if file, otherwise domain
                             * object serialization
                             */
                            if (child.getFileInfo() != null
                                    && child.getFileInfo().isFile()) {
                                resource.addProperty(ldpContains,
                                                     state.manifest
                                                             .createResource(child
                                                                     .getFileInfo()
                                                                     .getLocation()
                                                                     .toString()));
                            } else {
                                resource.addProperty(ldpContains,
                                                     state.manifest
                                                             .createResource(childSerializationLocation
                                                                     .toString()));
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void finish(PackageModelBuilderState state) {
        RDFFormat serializationFormat =
                determineSerialization(state.params, RDFFormat.TURTLE_PRETTY);
        String extension =
                serializationFormat.getLang().getFileExtensions().get(0);
        try (InputStream rem =
                toInputStream(state.manifest, serializationFormat)) {
            state.assembler.createResource("ORE-REM." + extension,
                                           PackageResourceType.ORE_REM,
                                           rem);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
