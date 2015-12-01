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

import java.net.URI;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Holds internal state local to a single package model building operation.
 * <p>
 * NodeVisitors are intended to be stateless. This class encapsulates all state
 * local to single act of building a package model.
 * </p>
 * 
 * @author apb
 * @version $Id$
 */
public class PackageModelBuilderState {

    public PackageState pkgState;

    /**
     * Anything that renames a resource in the domain object graph MUST put it
     * here.
     * <p>
     * This is a map of old URI to new URI.
     * </p>
     */
    public Map<String, String> renamedResources = new HashMap<>();

    /**
     * This contains a mapping of renamed content location URIs.
     * <p>
     * This is a map of old URI to new URI
     * </p>
     */
    public Map<URI, URI> renamedContentLocations = new HashMap<>();

    /**
     * Maps IPM node URIs to the location of a serialization in a package that
     * contains it.
     */
    public Map<URI, URI> domainObjectSerializationLocations = new HashMap<>();

    /**
     * RDF manifest associated with this package.
     * <p>
     * A shared notion of a package manifest, accessible and mutable by
     * NodeVisitors (e.g. it may contain an ORE ReM).
     * </p>
     */
    public Model manifest;

    /**
     * Mutable model containing domain objects as they are intended to be
     * packaged.
     * <p>
     * The intent is that NodeVisitors may modify the contents if this graph if
     * they need to modify the domain object graph for any reason (e.g.
     * substitute links, add additional triples, etc)
     * </p>
     */
    public Model domainObjects;

    /** PackageAssembler that will do the packaging. */
    public PackageAssembler assembler;

    /** IPM node tree for the packaging operation */
    public Node tree;

    /** Package generation params */
    public PackageGenerationParameters params;
}