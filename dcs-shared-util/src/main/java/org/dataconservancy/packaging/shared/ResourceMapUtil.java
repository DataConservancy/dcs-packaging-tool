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
package org.dataconservancy.packaging.shared;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResourceMapUtil {

    private static Logger LOG = LoggerFactory.getLogger(ResourceMapUtil.class);

    /**
     * Using the given reader, load RDF into a model.
     * 
     * If the given rdf uri has a file scheme, it is treated as relative to the
     * base directory.
     * 
     * @param reader  the reader to be used
     * @param model the model to be used
     * @param base_dir the base directory
     * @param rdf_uri  the RDF URI
     * @throws PackageException
     */
    public static void loadRDF(RDFReader reader, Model model, File base_dir, String rdf_uri)
            throws PackageException {
        URI uri;

        try {
            uri = resolveURI(base_dir, new URI(rdf_uri));
        } catch (URISyntaxException e) {
            throw new PackageException("Resource map uri invalid: " + rdf_uri, e);
        }

        URL url;

        try {
            url = uri.toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new PackageException("Error creating resource map url: " + uri, e);
        }

        try {
            reader.read(model, url.toString());
        } catch (Exception e) {
            throw new PackageException("Error reading resource map url: " + url, e);
        }
    }

    /**
     * If the uri has a file scheme, return a new absolute file url joining it
     * to the base directory. Otherwise return the uri.
     * 
     * @param base_dir the base directory
     * @param uri the URI
     * @return URI the resolved URI
     */
    public static URI resolveURI(File base_dir, URI uri) {
        StringBuilder path = new StringBuilder();
        
        if (uri.getHost() != null) {
            path.append(uri.getHost());
        }
        
        if (uri.getPath() != null) {
            path.append(uri.getPath());
        }

        if (uri.getScheme().equals("file")) {
            return new File(base_dir, path.toString()).toURI();
        }

        return uri;
    }

    /**
     * Loads all of the resource maps and returns a unified Model of their contents.  This method starts by loading the
     * resource map located at {@code resourceMapUri} (resolved against {@code baseDir}).  Then, every resource
     * referenced by the {@code &lt;ore:isDescribedBy rdf:resource="file:///...."/>} predicate is resolved and loaded
     * recursively.
     * <p>
     * If a package named {@code my-bag.tar.gz} is unpacked to {@code /storage/bags/my-bag}, then the base directory
     * parameter should be {@code /storage/bags}.  If the resource map is located at
     * {@code /storage/bags/my-bag/ORE-REM/rem.xml}, then the {@code resourceMapUri} should be
     * {@code file:///my-bag/ORE-REM/rem.xml}.
     *
     * @param resourceMapUri the resource map URI of the package
     * @param baseDir the base directory of the package in the file system
     * @return a unified Model, composed of all of the ORE resource maps in the package
     * @throws PackageException
     */
    public static Model loadRems(URI resourceMapUri, File baseDir) throws PackageException {
        List<String> loadedRems = new ArrayList<>();
        loadedRems.add(resourceMapUri.toString());

        Model m = ModelFactory.createDefaultModel();
        ResourceMapUtil.loadRDF(m.getReader(), m, baseDir, resourceMapUri.toString());
        LOG.debug("Loaded ReM file {}", resourceMapUri);
        m = load(m, baseDir, loadedRems);
        return m;
    }

    /**
     * Recursively loads all resources into a unified Model.  Resources that are objects of
     * {@code &lt;ore:isDescribedBy.../>} predicates are parsed and loaded.
     *
     * @param m the model
     * @param baseDir the base directory against which resources are resolved
     * @param loadedRems A list of files containing the rems loaded so far
     * @throws PackageException
     */
    private static Model load(Model m, File baseDir, List<String> loadedRems) throws PackageException {
        Selector isDescribedBySelector =
                new SimpleSelector(null, ResourceMapConstants.IS_DESCRIBED_BY_PROPERTY, (Object) null);

        StmtIterator sItr = m.listStatements(isDescribedBySelector);
        while (sItr.hasNext()) {
            final Statement stmt = sItr.next();
            final String rdfUri = stmt.getObject().toString();
            if (loadedRems.contains(rdfUri)) {
                LOG.trace("Already loaded ReM from {}", rdfUri);
                continue;
            } else {
                LOG.debug("Loading ReM file {}", rdfUri);
            }

            Model loaded = ModelFactory.createDefaultModel();
            RDFReader reader = loaded.getReader();
            reader.setProperty(ResourceMapConstants.JENA_ERROR_MODE_URI, ResourceMapConstants.JENA_ERROR_MODE_STRICT);
            ResourceMapUtil.loadRDF(reader, loaded, baseDir, rdfUri);
            loadedRems.add(rdfUri);
            m = load(m.union(loaded), baseDir, loadedRems);
        }

        return m;
    }
}
