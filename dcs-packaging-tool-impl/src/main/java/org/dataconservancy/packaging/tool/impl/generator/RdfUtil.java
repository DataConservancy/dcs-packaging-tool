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
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.PrefixMapFactory;

import org.dataconservancy.packaging.tool.model.GeneralParameterNames.SERIALIZATION_FORMAT;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.REM_SERIALIZATION_FORMAT;

/**
 * Collection of RDF slicing and dicing utilities.
 * 
 * @author apb
 * @version $Id$
 */
public class RdfUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RdfUtil.class);

    @SuppressWarnings("unchecked")
    private static final Map<String, String> PREFIX_MAP =
            MapUtils.invertMap(Ontologies.PREFIX_MAP);

    /**
     * Select all triples that are "local" to the given subject.
     * <p>
     * Given a URI, "local" triples include those who have a subject that:
     * </p>
     * <ul>
     * <li>is the URI</li>
     * <li>Has a 'hash' relationship to the URI (e.g.
     * <code>http://example.org/x</code> and
     * <code>http://example.org/x#foo</code>)</li>
     * <li>is a blank node traversable by the URI or any related hash URIs</li>
     * </ul>
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
                .filterKeep(s -> bare(s.toString()).equals(baseURI))
                .forEachRemaining(subjects::add);

        /* All traversable blank nodes are also included */
        subjects.addAll(subjects.stream().map(RdfUtil::blankNodesReachableFrom)
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

        from.listStatements(selector).forEachRemaining(extracted::add);

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
                .forEachRemaining(bnode -> blankNodes
                        .addAll(blankNodesReachableFrom(bnode)));

        return blankNodes;
    }

    /**
     * Get a bare (non-hashed) version of a URI, by stripping off any hash
     * portion.
     * 
     * @param uri
     *        the URI
     * @return the stripped version of the URI
     */
    public static String bare(String uri) {
        String s = uri + "#";
        return s.substring(0, s.indexOf('#'));
    }

    public static InputStream toInputStream(Model model, RDFFormat format) {
        /*
         * Maintain a local prefix map containing only prefixes/namespaces we
         * actually use
         */
        Map<String, String> prefixes = new HashMap<>();
        /* Predicates */
        model.listStatements()
                .mapWith(stmt -> stmt.getPredicate().getNameSpace())
                .filterKeep(PREFIX_MAP::containsKey).toSet()
                .forEach(ns -> prefixes.put(PREFIX_MAP.get(ns), ns));
        
        /* Objects  */
        model.listStatements().filterKeep(s -> s.getObject().isResource())
                .mapWith(s -> s.getObject().asResource().getNameSpace())
                .filterKeep(PREFIX_MAP::containsKey).toSet()
                .forEach(ns -> prefixes.put(PREFIX_MAP.get(ns), ns));

        /*
         * Now serialize the domain object. Note that PackageAssembler wants an
         * InputStream
         */

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // In order to serialize RDF/XML that contains '<>' denoting
        // a server-assigned resource URI, we have to configure the
        // RDF writer specially, otherwise Jena will barf
        if (format.toString().contains("XML")) {
            RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
            writer.setProperty("relativeURIs", "same-document");
            writer.setProperty("allowBadURIs", "true");
            writer.write(model, out, null);
        } else {
            RDFDataMgr.createGraphWriter(format).write(out,
                                                       model.getGraph(),
                                                       PrefixMapFactory
                                                               .create(prefixes),
                                                       null,
                                                       null);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Determines the preferred serialization for RDF-based package resources.
     * This method consults the supplied
     * {@code PackageGenerationParameters} for a preferred format. A default
     * {@code RDFFormat} may be supplied, which
     * will be returned if the preferred serialization cannot be determined. The
     * default format may be {@code null},
     * which is interpreted as {@code RDFFormat#TURTLE_PRETTY}.
     *
     * @param params
     *        the package generation parameters
     * @param defaultFormat
     *        the default format to use, may be {@code null}
     * @return the {@code RDFFormat} to use when serializing package resources
     * @throws IllegalArgumentException
     *         if the supplied {@code params} are {@code null}
     */
    public static RDFFormat determineSerialization(PackageGenerationParameters params,
                                                   RDFFormat defaultFormat) {

        if (params == null) {
            throw new IllegalArgumentException("Supplied PackageGenerationParameters must not be null.");
        }

        if (defaultFormat == null) {
            defaultFormat = RDFFormat.TURTLE_PRETTY;
        }

        RDFFormat format = defaultFormat;

        if (params.getParam(REM_SERIALIZATION_FORMAT) != null
                && !params.getParam(REM_SERIALIZATION_FORMAT).isEmpty()) {

            final String selectedSerialization =
                    params.getParam(REM_SERIALIZATION_FORMAT).get(0);
            try {
                SERIALIZATION_FORMAT selectedFormat =
                        SERIALIZATION_FORMAT.valueOf(selectedSerialization);

                switch (selectedFormat) {
                    case JSONLD:
                        format = RDFFormat.JSONLD_PRETTY;
                        break;
                    case TURTLE:
                        format = RDFFormat.TURTLE_PRETTY;
                        break;
                    case XML:
                        format = RDFFormat.RDFXML_PRETTY;
                        break;
                }
            } catch (IllegalArgumentException e) {
                LOG.warn("Unsupported serialization format requested: '"
                        + selectedSerialization + "', "
                        + "returning default serialization '"
                        + format.toString() + "'");
            }
        }

        return format;
    }
}
