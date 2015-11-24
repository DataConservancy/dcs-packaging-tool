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

import java.net.URI;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.MapUtils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.ResourceUtils;

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;

import static org.dataconservancy.packaging.tool.impl.generator.IPMUtil.path;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.bare;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.cut;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.determineSerialization;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.selectLocal;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.toInputStream;

/**
 * Serializes domain object graphs into individual resources in a bag.
 * <p>
 * A "domain object graph" is a graph that contains all triples with the a
 * domain object as a URI, all triples that are hash fragments of the domain
 * object URI, and any triples with blank node subjects that are traversible
 * from either.
 * </p>
 * 
 * @author apb
 * @version $Id$
 */
class DomainObjectResourceBuilder
        implements NodeVisitor {

    PackageStateSerializer serializer;

    ExecutorService exe = Executors.newCachedThreadPool();

    @SuppressWarnings("unchecked")
    Map<String, String> PREFIX_MAP = MapUtils.invertMap(Ontologies.PREFIX_MAP);

    public void setPackageStateSerializer(PackageStateSerializer ser) {
        this.serializer = ser;
    }

    /* Reserve and translate URIs from opaque to resolvable via the Assembler. */
    @Override
    public void init(PackageModelBuilderState state) {

        /*
         * First, build a sorted map of all resources in the model. We'll be
         * doing URI swapping/remapping to be consistent with resources and
         * linking in the bag.
         */
        TreeMap<String, Resource> originalResources = new TreeMap<>();
        state.domainObjects.listSubjects()
                .forEachRemaining(s -> originalResources.put(s.toString(), s));
        state.domainObjects
                .listObjects()
                .filterKeep(o -> o.isResource() && !o.isAnon())
                .forEachRemaining(o -> originalResources.put(o.toString(),
                                                             o.asResource()));

        state.tree
                .walk(node -> {

                    /* Skip over removed nodes */
                    if (node.isIgnored()) {
                        /* Remove the domain object graph */
                        Model ignored =
                                cut(state.domainObjects,
                                    selectLocal(state.domainObjects
                                            .getResource(node.getDomainObject()
                                                    .toString())));

                        /* Remove triples that involve a subject defined in it */
                        ignored.listSubjects()
                                .filterKeep(r -> r.isURIResource())
                                .forEachRemaining(r -> state.domainObjects
                                        .removeAll(null, null, r));
                        return;
                    }

                    /* Get the former domain object URI */
                    URI originalDomainObjectURI = node.getDomainObject();

                    /* This is where the domain object will be serialized */
                    node.setIdentifier(state.assembler
                            .reserveResource(path(node,
                                    "." + determineSerialization(state.params, RDFFormat.TURTLE_PRETTY)
                                            .getLang()
                                            .getFileExtensions()
                                            .get(0)),
                                             PackageResourceType.DATA));

                    URI newDomainObjectURI = node.getIdentifier();

                    if (node.getFileInfo().isFile()) {
                        try {
                            URI binaryURI =
                                    state.assembler
                                            .createResource(path(node, ""),
                                                            PackageResourceType.DATA,
                                                            node.getFileInfo()
                                                                    .getLocation()
                                                                    .toURL()
                                                                    .openStream());

                            URI originalFileLocation =
                                    node.getFileInfo().getLocation();
                            if (!state.domainObjects
                                    .containsResource(state.domainObjects
                                            .getResource(originalFileLocation
                                                    .toString()))) {

                                /*
                                 * If the file content location is not linked
                                 * to, then the domain object URI *is* the
                                 * binary URI
                                 */
                                newDomainObjectURI = binaryURI;
                            } else {
                                /*
                                 * We replace references to file location with
                                 * the binary URI
                                 */
                                remap(state.domainObjects,
                                      bare(node.getFileInfo().getLocation()
                                              .toString()),
                                      binaryURI.toString(),
                                      originalResources,
                                      state.renamedResources);

                            }

                            node.getFileInfo().setLocation(binaryURI);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    node.setDomainObject(newDomainObjectURI);

                    /*
                     * Rebase all URIs and hash URIs to the assembler-provided
                     * URI
                     */
                    remap(state.domainObjects,
                          bare(originalDomainObjectURI.toString()),
                          node.getDomainObject().toString(),
                          originalResources,
                          state.renamedResources);

                });
    }

    /* Serialize the domain object, and save the binary content */
    @Override
    public void visitNode(Node node, PackageModelBuilderState state) {

        if (node.isIgnored()) {
            return;
        }

        Resource primaryDomainObject =
                state.domainObjects.getResource(node.getDomainObject()
                        .toString());

        /* Cut the domain object graph out of the graph of domain objects */
        Model domainObjectGraph =
                cut(state.domainObjects, selectLocal(primaryDomainObject));

        /*
         * If the domain object is not at the binary URI, give it the null
         * relative URI for serialization.
         */
        if (node.getDomainObject().equals(node.getIdentifier())) {
            String baseURI = bare(primaryDomainObject.getURI());
            domainObjectGraph
                    .listSubjects()
                    .toSet()
                    .stream()
                    .filter(subject -> subject.toString().contains(baseURI))
                    .forEach(subject -> ResourceUtils.renameResource(subject,
                                                                     subject.toString()
                                                                             .replaceFirst(baseURI,
                                                                                           "")));
        }

        try (InputStream stream =
                toInputStream(domainObjectGraph, determineSerialization(state.params, RDFFormat.TURTLE))) {
            state.assembler.putResource(node.getIdentifier(), stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * Verify that we have exhausted all domain object triples. If not,
     * something is amiss!
     */
    @Override
    public void finish(PackageModelBuilderState state) {
        if (state.domainObjects.listStatements().hasNext()) {
            throw new RuntimeException("Did not serialize all triples! "
                    + state.domainObjects);
        }
    }

    private static void remap(Model model,
                              String oldBaseURI,
                              String newBaseURI,
                              TreeMap<String, Resource> resources,
                              Map<String, String> renameMap) {

        Map<String, Resource> toReplace = new HashMap<>();

        /* Consider the the URI plus any hash fragments */
        toReplace.putAll(resources.subMap(oldBaseURI, true, oldBaseURI, true));
        toReplace.putAll(resources.subMap(oldBaseURI + "#", oldBaseURI + "#"
                + Character.MAX_VALUE));

        /* Swap out the base URI for each matching resource */
        toReplace.entrySet().forEach(res -> {
            String newURI = res.getKey().replaceFirst(oldBaseURI, newBaseURI);
            renameMap.put(res.getValue().toString(), newURI);
            ResourceUtils.renameResource(res.getValue(), newURI);
        });
    }
}
