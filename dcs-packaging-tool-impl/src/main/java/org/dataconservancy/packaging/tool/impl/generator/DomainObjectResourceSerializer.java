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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.net.URI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.util.ResourceUtils;

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;

import static org.dataconservancy.packaging.tool.impl.generator.IPMUtil.path;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.bare;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.cut;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.selectLocal;

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
class DomainObjectResourceSerializer
        implements NodeVisitor {

    PackageStateSerializer serializer;

    ExecutorService exe = Executors.newCachedThreadPool();

    PrefixMap PREFIX_MAP = PrefixMapFactory.create(Ontologies.PREFIX_MAP);

    public void setPackageStateSerializer(PackageStateSerializer ser) {
        this.serializer = ser;
    }

    /* Reserve and translate URIs from opaque to resolvable via the Assembler. */
    @Override
    public void init(PackageModelBuilderState state) {

        state.tree.walk(node -> {
            URI former = node.getDomainObject();
            node.setDomainObject(state.assembler
                    .reserveResource(path(node, ".ttl"),
                                     PackageResourceType.DATA));

            ResourceUtils.renameResource(state.domainObjects.getResource(former
                    .toString()), node.getDomainObject().toString());
        });
    }

    /* Serialize the domain object */
    @Override
    public void visitNode(Node node, PackageModelBuilderState state) {

        Resource primaryDomainObject =
                state.domainObjects.getResource(node.getDomainObject()
                        .toString());

        /* Cut the domain object graph out of the graph of domain objects */
        Model domainObjectGraph =
                cut(state.domainObjects, selectLocal(primaryDomainObject));

        /* Give it the null relative URI */
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
        /* Now serialize. Note that PackageAssembler wants an InputStream */
        try (PipedOutputStream src = new PipedOutputStream();
                PipedInputStream sink = new PipedInputStream()) {

            sink.connect(src);

            exe.execute(new Runnable() {

                @Override
                public void run() {

                    RDFDataMgr.createGraphWriter(RDFFormat.TURTLE_PRETTY)
                            .write(src,
                                   domainObjectGraph.getGraph(),
                                   PREFIX_MAP,
                                   "",
                                   null);
                }
            });

            state.assembler.putResource(node.getDomainObject(), sink);
        } catch (IOException e) {
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

}
