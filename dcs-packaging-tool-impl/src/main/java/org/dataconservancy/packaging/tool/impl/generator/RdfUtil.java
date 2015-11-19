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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.PrefixMapFactory;

import org.dataconservancy.packaging.tool.ontologies.Ontologies;

/**
 * Collection of RDF slicing and dicing utilities.
 * 
 * @author apb
 * @version $Id$
 */
class RdfUtil {

    @SuppressWarnings("unchecked")
    private static final Map<String, String> PREFIX_MAP = MapUtils
            .invertMap(Ontologies.PREFIX_MAP);

    /**
     * Select all triples that are "local" to the given subject.
     * <p>
     * Given a URI, "local" triples include those who have a subject that:
     * <ul>
     * <li>is the URI</li>
     * <li>Has a 'hash' relationship to the URI (e.g.
     * <code>http://example.org/x</code> and
     * <code>http://example.org/x#foo</code>)</li>
     * <li>is a blank node traversable by the URI or any related hash URIs</li>
     * </ul>
     * </p>
     * 
     * @param subject
     *        a resource that is the Subject of at least one statement.
     * @return Selector that selects all local triples with respect to the
     *         subject.
     */
    public static Selector selectLocal(Resource subject) {
        Set<Resource> subjects = new HashSet<>();

        /* Add all triples that are related fragments */
        String baseURI = bare(subject.toString());
        subject.getModel().listSubjects()
                .filterKeep(s -> s.toString().startsWith(baseURI))
                .forEachRemaining(subjects::add);

        /* All traversable blank nodes are also included */
        subjects.addAll(subjects.stream().map(s -> blankNodesReachableFrom(s))
                .flatMap(Collection::stream).collect(Collectors.toList()));

        return new SimpleSelector() {

            public boolean selects(Statement s) {
                return subjects.contains(s.getSubject());
            }

        };
    }

    /**
     * Remove a subset of a Model with the given Selector.
     * 
     * @param from
     *        Model that will have statements removed
     * @param selector
     *        Selector for matching statements to remove
     * @return a Model containing all the extracted triples.
     */
    public static Model cut(Model from, Selector selector) {

        Model excised = ModelFactory.createDefaultModel();
        List<Statement> toRemove = new ArrayList<>();

        from.listStatements(selector).forEachRemaining(s -> {
            excised.add(s);
            toRemove.add(s);
        });

        from.remove(toRemove);

        return excised;
    }

    public static Model copy(Model from, Selector selector) {
        Model extracted = ModelFactory.createDefaultModel();

        from.listStatements(selector).forEachRemaining(s -> {
            extracted.add(s);
        });

        return extracted;
    }

    static Collection<Resource> blankNodesReachableFrom(Resource subject) {
        Set<Resource> blankNodes = new HashSet<>();

        if (subject.isAnon()) {
            blankNodes.add(subject);
        }

        subject.getModel().listStatements(subject, null, (RDFNode) null)
                .filterKeep(stmnt -> stmnt.getObject().isAnon())
                .mapWith(stmnt -> stmnt.getObject().asResource())
                .forEachRemaining(bnode -> {
                    blankNodes.addAll(blankNodesReachableFrom(bnode));
                });

        return blankNodes;
    }

    /**
     * Get a bare (non-hashed) version of a URI, by stripping off any hash
     * portion.
     */
    public static String bare(String uri) {
        String s = uri.toString() + "#";
        return s.substring(0, s.indexOf('#'));
    }

    public static InputStream toInputStream(Model model, RDFFormat format) {
        /*
         * Maintain a local prefix map containing only prefixes/namespaces we
         * actually use
         */
        Map<String, String> prefixes = new HashMap<>();
        model.listStatements()
                .mapWith(stmt -> stmt.getPredicate().getNameSpace())
                .filterKeep(ns -> PREFIX_MAP.containsKey(ns)).toSet()
                .forEach(ns -> prefixes.put(PREFIX_MAP.get(ns), ns));

        /*
         * Now serialize the domain object. Note that PackageAssembler wants an
         * InputStream
         */

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.createGraphWriter(format).write(out,
                                                   model.getGraph(),
                                                   PrefixMapFactory
                                                           .create(prefixes),
                                                   null,
                                                   null);

        return new ByteArrayInputStream(out.toByteArray());
    }
}