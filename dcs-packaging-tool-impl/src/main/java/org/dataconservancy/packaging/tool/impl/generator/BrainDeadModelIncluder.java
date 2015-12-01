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

package org.dataconservancy.packaging.tool.impl.generator;

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.determineSerialization;
import static org.dataconservancy.packaging.tool.impl.generator.RdfUtil.toInputStream;
import static org.dataconservancy.packaging.tool.ontologies.ModelResources.RESOURCE_MAP;
import static org.dataconservancy.packaging.tool.ontologies.ModelResources.get;

/**
 * Includes serializations of models used to interpret RDF in package serializations.
 */
public class BrainDeadModelIncluder implements NodeVisitor {

    @Override
    public void init(PackageModelBuilderState state) {

    }

    @Override
    public void visitNode(Node node, PackageModelBuilderState state) {

    }

    /**
     * {@inheritDoc}
     * <p>
     * Places a serialization of each model from {@link org.dataconservancy.packaging.tool.ontologies.ModelResources} into
     * the package as a {@link PackageResourceType#ONTOLOGY} package resource.
     * </p>
     *
     * @param state {@inheritDoc}
     */
    @Override
    public void finish(PackageModelBuilderState state) {
        RESOURCE_MAP.forEach((namespace, resourcePath) -> {
            state.assembler.createResource(
                    toPackagePath(resourcePath) + "." + determineSerialization(state.params, null)
                            .getLang()
                            .getFileExtensions()
                            .get(0),
                    PackageResourceType.ONTOLOGY,
                    toInputStream(get(namespace), determineSerialization(state.params, null)));

        });
    }

    /**
     * Converts the full classpath resource string into a file name.  It strips
     * any preceding package names, forward slashes, and extensions from the resource
     * and returns a bare name, suitable for use in the package.  Given the classpath
     * resource {@code /org/dataconservancy/packaging/tool/ontologies/owl.ttl}, this method
     * will return {@code owl}.
     *
     * @param resourcePath a classpath resource
     * @return a simple string suitable for naming the resource in the package
     */
    private String toPackagePath(String resourcePath) {
        return stripExt(resourceName(resourcePath));
    }

    /**
     * Returns a name from from a classpath resource.  For {@code resourcePath}
     * {@code /foo/bar/baz.txt}, this method returns {@code baz.txt}.  For
     * {@code resourcePath org/dataconservancy/foo/bar.biz} this method returns
     * {@code bar.biz}.
     *
     * @param resourcePath a classpath resource
     * @return the name of the resource
     */
    private String resourceName(String resourcePath) {
        Path path = Paths.get(resourcePath);
        return path.getName(path.getNameCount() - 1).toString();
    }

    /**
     * Strips file extentions off of a file name.  For {@code fileName}
     * {@code foo.txt}, this method returns {@code foo}.  For {@code fileName}
     * {@code multiple_extensions.foo.bar}, this method returns
     * {@code multiple_extensions}.
     *
     * @param fileName the name of a file, which may or may not have a file extension
     * @return the file name with out the extention (including not having the ".")
     */
    private String stripExt(String fileName) {
        int i = -1;
        if (fileName.length() > 0 && (i = fileName.lastIndexOf(".")) > -1) {
            return stripExt(fileName.substring(0, i));
        } else {
            return fileName;
        }
    }

}
