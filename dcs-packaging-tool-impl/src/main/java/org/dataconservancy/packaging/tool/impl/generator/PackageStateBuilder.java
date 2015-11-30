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
import org.springframework.beans.factory.annotation.Autowired;

public class PackageStateBuilder
        implements NodeVisitor {

    @Autowired
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
         * Update file locations in the IPM tree
         */
        try {
            Node tree =
                    rdf2ipm.transformToNode(state.pkgState.getPackageTree());

            tree.walk(node -> {
                if (node.getFileInfo() != null
                        && state.renamedContentLocations.containsKey(node
                                .getFileInfo().getLocation())) {
                    node.getFileInfo()
                            .setLocation(state.renamedContentLocations.get(node
                                    .getFileInfo().getLocation()));

                }
            });

            state.pkgState.setPackageTree(rdf2ipm.transformToRDF(tree));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*
         * Re-map file URIs to bag URIs the package state domain object graph
         */
        state.renamedContentLocations.forEach((oldURI, newURI) -> ResourceUtils
                .renameResource(state.pkgState.getDomainObjectRDF()
                        .getResource(oldURI.toString()), newURI.toString()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pkgSer.serialize(state.pkgState, out);

        /* Finally, serialize the package state */
        state.assembler.createResource("pkgState.bin",
                                       PackageResourceType.PACKAGE_STATE,
                                       new ByteArrayInputStream(out
                                               .toByteArray()));

    }
}
