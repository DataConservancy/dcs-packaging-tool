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

package org.dataconservancy.packaging.tool.impl.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.jena.util.ResourceUtils;

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;

public class PackageStateBuilder
        implements NodeVisitor {

    IpmRdfTransformService rdf2ipm = new IpmRdfTransformService();

    PackageStateSerializer pkgSer;

    public void setPackageStateSerializer(PackageStateSerializer ser) {
        this.pkgSer = ser;
    }

    @Override
    public void init(PackageModelBuilderState state) {
        /* nothing! */
    }

    @Override
    public void visitNode(Node node, PackageModelBuilderState state) {
        /* Nothing! */
    }

    @Override
    public void finish(PackageModelBuilderState state) {

        /*
         * We use the mutated IPM tree, which has updated references for file
         * URIs, domain object URIs.
         */
        try {
            state.pkgState.setPackageTree(rdf2ipm.transformToRDF(state.tree));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /* Now, re-map resources of the domain object graph */
        state.renamedResources.forEach((oldURI, newURI) -> ResourceUtils
                .renameResource(state.pkgState.getDomainObjectRDF()
                        .getResource(oldURI), newURI));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pkgSer.serialize(state.pkgState, out);

        /* Finally, serialize the package state */
        state.assembler.createResource("pkgState.bin",
                                       PackageResourceType.PACKAGE_STATE,
                                       new ByteArrayInputStream(out
                                               .toByteArray()));

    }
}
