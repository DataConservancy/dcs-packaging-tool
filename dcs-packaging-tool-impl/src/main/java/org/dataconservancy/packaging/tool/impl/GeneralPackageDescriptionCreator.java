/*
 * Copyright 2014 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl;

import java.io.File;
import java.io.IOException;

import java.net.URI;

import java.net.URISyntaxException;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.utils.URIBuilder;
import org.dataconservancy.packaging.tool.api.PackageDescriptionCreator;
import org.dataconservancy.packaging.tool.api.PackageDescriptionCreatorException;
import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.FileContextImpl;
import org.dataconservancy.packaging.tool.impl.rules.Mapping;
import org.dataconservancy.packaging.tool.impl.rules.Rule;
import org.dataconservancy.packaging.tool.impl.rules.RuleImpl;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.description.Action;
import org.dataconservancy.packaging.tool.model.description.RuleSpec;
import org.dataconservancy.packaging.tool.model.description.RulesSpec;

public class GeneralPackageDescriptionCreator
        implements PackageDescriptionCreator {

    private final List<Rule> rules = new ArrayList<Rule>();

    private Set<String> visitedFiles = new HashSet<String>();

    public GeneralPackageDescriptionCreator(RulesSpec rulesSpec) {
        for (RuleSpec ruleSpec : rulesSpec.getRule()) {
            rules.add(new RuleImpl(ruleSpec));
        }
    }

    @Override
    public PackageDescription createPackageDescription(String packageOntologyIdentifier, File directoryTreeRoot)
            throws PackageDescriptionCreatorException {
        if (directoryTreeRoot == null) {
            throw new PackageDescriptionCreatorException("The provided directory is null.");
        } else if (!directoryTreeRoot.exists()) {
            throw new PackageDescriptionCreatorException("The directory specified by file path \'"
                    + directoryTreeRoot.getPath() + "\' does not exist");
        } else if (!directoryTreeRoot.canRead()) {
            throw new PackageDescriptionCreatorException("The specified directory cannot be read.");
        }

        if (packageOntologyIdentifier == null) {
            throw new PackageDescriptionCreatorException("The package ontology specification must not be null.");
        }

        visitedFiles.clear(); /*
                               * make sure we have a clear file Set before we
                               * start
                               */
        PackageDescription desc = new PackageDescription();

        Map<String, PackageArtifact> artifacts =
                new HashMap<String, PackageArtifact>();

        visitFile(new FileContextImpl(directoryTreeRoot, directoryTreeRoot, false),
                  artifacts);

        desc.setPackageArtifacts(new HashSet<PackageArtifact>(artifacts
                .values()));
        desc.setRootArtifactRef(directoryTreeRoot.toURI().toString());
        desc.setPackageOntologyIdentifier(packageOntologyIdentifier);
        return desc;
    }

    private void visitFile(FileContext cxt,
                           Map<String, PackageArtifact> artifacts)
            throws PackageDescriptionCreatorException {

        try {
            String path = cxt.getFile().getCanonicalPath();
            if (visitedFiles.contains(path)) {
                if (Files.isSymbolicLink(cxt.getFile().toPath())) {
                    throw new PackageDescriptionCreatorException("Symbolic link cycle detected",
                                                                 "Fix offending symbolic link at "
                                                                         + cxt.getFile()
                                                                                 .toString()
                                                                         + ", which points to "
                                                                         + path);
                } else {
                    throw new PackageDescriptionCreatorException("Symbolic link cycle detected",
                                                                 "There is a symbolic link under "
                                                                         + cxt.getRoot()
                                                                                 .toString()
                                                                         + " which points to "
                                                                         + path
                                                                         + ".  Find the link and remove it.");
                }
            } else {
                visitedFiles.add(path);
            }
        } catch (IOException e) {
            throw new PackageDescriptionCreatorException("Error determining canonical path of "
                                                                 + cxt.getFile(),
                                                         e);
        }

        try {
            for (Rule rule : rules) {
                if (rule.select(cxt)) {
                    if (Action.EXCLUDE.equals(rule.getAction())) {
                        cxt.setIgnored(true);
                        continue;
                    } else if (Action.INCLUDE.equals(rule.getAction())) {
                        populate(cxt, rule, artifacts);
                    }

                    break;
                }
            }

        } catch (Exception e) {
            throw new PackageDescriptionCreatorException("Error applying package description generation rules to pathname "
                                                                 + cxt.getFile()
                                                                         .toString()
                                                                 + ": \n"
                                                                 + e.getMessage(),
                                                         e);
        }

        if (cxt.getFile().isDirectory()) {
            for (File child : cxt.getFile().listFiles()) {
                visitFile(new FileContextImpl(child, cxt.getRoot(), cxt.isIgnored()), artifacts);
            }
        }
    }

    /*
     * Create PakageArtifact from the file, populate PackageDescription with it
     */
    private void populate(FileContext cxt,
                          Rule rule,
                          Map<String, PackageArtifact> artifacts) {

        List<Mapping> mappings = rule.getMappings(cxt);

        for (Mapping mapping : mappings) {

            /* We are using file URI as artifact IDs, unless multiple mappings */
            URIBuilder urib = new URIBuilder(cxt.getFile().toURI());
            //String id = cxt.getFile().toURI().toString();

            /*
             * If multiple mappings implicated by this file, then make sure
             * they're differentiated
             */
            if (mappings.size() > 1) {
                String specifier = mapping.getSpecifier();
                if (specifier != null) {
                    urib.setFragment(specifier);
                }
            }

            URI uri = null;
            try {
                uri = urib.build();
            } catch (URISyntaxException e){

            }
            String id = uri.toString();

            PackageArtifact artifact = new PackageArtifact();
            artifacts.put(id, artifact);
            artifact.setId(id);
            artifact.setIgnored(cxt.isIgnored());
            //we need to relativize against the content root, not the supplied root artifact dir
            Path rootPath = Paths.get(cxt.getRoot().getParentFile().getPath());
            Path filePath = Paths.get(cxt.getFile().getPath());
            artifact.setArtifactRef(String.valueOf(rootPath.relativize(filePath)));
            artifact.getArtifactRef().setFragment(uri.getFragment());
            /*
             * if file is a normal file, set the isByteStream flag to true on
             * PackageArtifact
             */

            if (cxt.getFile().isFile()) {
                artifact.setByteStream(true);
            }

            artifact.setType(mapping.getType().getValue());
            
            if (mapping.getType().isByteStream() != null) {
                artifact.setByteStream(mapping.getType().isByteStream());
            } else {
                artifact.setByteStream(cxt.getFile().isFile());
            }

            for (Map.Entry<String, List<String>> entry : mapping
                    .getProperties().entrySet()) {
                Set<String> valueSet = new HashSet<String>(entry.getValue());
                artifact.setSimplePropertyValues(entry.getKey(), valueSet);
            }
            /*
             * Since we use file URI as artifact IDs (with optional specifier as
             * URI fragment), we just need to use the relationship's target
             * file's URI as the relationship target, and we're done!.
             */
            List<PackageRelationship> rels = new ArrayList<PackageRelationship>();
            
            for (Map.Entry<String, Set<URI>> rel : mapping.getRelationships()
                    .entrySet()) {
                Set<String> relTargets = new HashSet<String>();
                for (URI target : rel.getValue()) {
                    relTargets.add(target.toString());
                }

                rels.add(new PackageRelationship(rel.getKey(), true, relTargets));
            
            }
            artifact.setRelationships(rels);
        }
    }
}
