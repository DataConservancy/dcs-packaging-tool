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

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageModelBuilder;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Builds package models by crawling an IPM tree and invoking a set of
 * {@link NodeVisitor}.
 * <p>
 * NodeVisitor implementations are responsible for interacting with the
 * PackageAssembler to create the ultimate package. The PackageModelBuilderImpl
 * coordinates the effort of all NodeVisitors.The general workflow over the
 * course of creating a package is:
 * </p>
 * <ul>
 * <li>All visitors are given an opportunity to perform some initial action via
 * {@link NodeVisitor#init(PackageModelBuilderState)}</li>
 * <li>All IPM nodes are iterated in an unspecified order, invoking
 * {@link NodeVisitor#visitNode(Node, PackageModelBuilderState)} for all
 * visitors</li>
 * <li>Finally, {@link NodeVisitor#finish(PackageModelBuilderState)} for all
 * visitors</li>
 * </ul>
 *
 * @author apb
 * @version $Id$
 */
public class PackageModelBuilderImpl
        implements PackageModelBuilder {

    IpmRdfTransformService rdf2ipm = new IpmRdfTransformService();

    List<NodeVisitor> visitors = new ArrayList<>();

    private PackageGenerationParameters params;

    public void setNodeVisitors(List<NodeVisitor> visitors) {
        this.visitors = visitors;
    }

    @Override
    public void init(PackageGenerationParameters params) {
        this.params = params;
    }

    @Override
    public void buildModel(PackageState pstate, PackageAssembler assembler) {

        PackageModelBuilderState builderState = new PackageModelBuilderState();
        builderState.domainObjects = pstate.getDomainObjectRDF();
        builderState.assembler = assembler;
        builderState.params = params;

        try {

            builderState.tree =
                    rdf2ipm.transformToNode(pstate.getPackageTree());

            visitors.forEach(v -> v.init(builderState));

            builderState.tree.walk(node -> visitors.forEach(v -> v
                    .visitNode(node, builderState)));

            visitors.forEach(v -> v.finish(builderState));

        } catch (Exception e) {
            throw new RuntimeException("Error deserializing package tree", e);
        }

        assembler.assemblePackage();

    }

}
