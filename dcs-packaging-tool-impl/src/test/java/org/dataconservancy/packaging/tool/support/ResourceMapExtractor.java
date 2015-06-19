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
package org.dataconservancy.packaging.tool.support;

import java.io.File;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.dcs.model.AttributeSetName;
import org.dataconservancy.dcs.model.AttributeValueType;
import org.dataconservancy.dcs.model.Metadata;
import org.dataconservancy.packaging.shared.PackageException;
import org.dataconservancy.packaging.shared.ResourceMapConstants;
import org.dataconservancy.packaging.shared.ResourceMapUtil;
import org.dataconservancy.packaging.validation.PackageValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Ingest service to walk the resource map graph of the package and extract necessary
 * attributes. This service produces AttributeSets for Business Objects.
 */
public class ResourceMapExtractor implements ResourceMapConstants {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    public Map<String, AttributeSet> execute(File baseDir, URI packageUri)
            throws PackageValidationException {

        State state = new State();
        
        state.baseDir = baseDir;
        
        Resource resmap = state.model.createResource(packageUri.toString());
        
        // Walk the graph constructing attribute sets for each object and
        // generate events.
        visitResourceMap(resmap, state);
        
        return state.attributeMap;
    }

    private boolean hasType(Resource subject, Resource type) {
        return subject.hasProperty(RDF.type, type);
    }

    private void visitResourceMap(final Resource resmap, State state) 
            throws PackageValidationException {
        
        /* Help battle infinite loops, we only need to visit a ReM once */
        if (!state.visitedResourceMaps.contains(resmap.getURI())) {
            state.visitedResourceMaps.add(resmap.getURI());
        } else {
            return;
        }

        if (!state.model.contains(resmap, DESCRIBES_PROPERTY)) {
            // Load the resource map and log any problems

            RDFReader reader = state.model.getReader();
            reader.setProperty(JENA_ERROR_MODE_URI, JENA_ERROR_MODE_STRICT);

            RDFErrorHandler handler = new RDFErrorHandler() {
                public void warning(Exception e) {
                    log.warn("Warning loading resource map: " + resmap, e);
                }

                @Override
                public void fatalError(Exception e) {
                    log.error("Fatal error loading resource map: " + resmap, e);

                }

                @Override
                public void error(Exception e) {
                    log.error("Error loading resource map: " + resmap, e);
                }
            };

            reader.setErrorHandler(handler);

            try {
                ResourceMapUtil.loadRDF(reader, state.model, state.baseDir, resmap.getURI());
            } catch (PackageException e1) {
                throw new PackageValidationException(e1);
            }
        }

        Resource agg = resmap.getPropertyResourceValue(DESCRIBES_PROPERTY);

        if (agg == null) {
            throw new PackageValidationException("The resource map <" + resmap.getURI()
                    + "> does not describe an aggregation.");
        }

        visitAggregation(agg, state);
    }

    private void visitAggregation(Resource agg, State state) 
            throws PackageValidationException {
        if (!state.model.contains(agg, AGGREGATES_PROPERTY)) {
            throw new PackageValidationException("The aggregation " + agg.getURI()
                    + " does not have an aggregates property.");
        }

        if (hasType(agg, DC_PACKAGE_TYPE)) {
            visitPackage(agg, state);
        } else if (hasType(agg, DC_PROJECT_TYPE)) {
            visitProject(agg, state);
        } else if (hasType(agg, DCMI_COLLECTION_TYPE)) {
            visitCollection(agg, state);
        } else if (hasType(agg, DC_DATA_ITEM_TYPE)) {
            visitDataItem(agg, state);
        } else {
            throw new PackageValidationException(
                    "Unable to find any datacons type in the aggregation " + agg.getURI());
        }
    }

    private void visitAggregatedResources(Resource agg, State state) 
            throws PackageValidationException {
        NodeIterator iter = state.model.listObjectsOfProperty(agg, AGGREGATES_PROPERTY);

        while (iter.hasNext()) {
            Resource res = iter.next().asResource();
            
            if (res.hasProperty(IS_DESCRIBED_BY_PROPERTY)) {
                visitResourceMap(res.getPropertyResourceValue(IS_DESCRIBED_BY_PROPERTY), state);
            } else {
                visitByteStream(res, state);
            }
        }
    }

    private void visitByteStream(Resource res, State state)
            throws PackageValidationException {
        createAttributeSet(res, state, Types.File);
    }

    private void visitPackage(Resource agg, State state)
            throws PackageValidationException {
        visitAggregatedResources(agg, state);
        createAttributeSet(agg, state, Types.Package);
    }

    private void visitProject(Resource agg, State state)
            throws PackageValidationException {
        visitAggregatedResources(agg, state);
        createAttributeSet(agg, state, Types.Project);
    }

    private void visitCollection(Resource agg, State state) 
            throws PackageValidationException {
        visitAggregatedResources(agg, state);
        createAttributeSet(agg, state, Types.Collection);
    }

    private void visitDataItem(Resource agg, State state)
            throws PackageValidationException {
        visitAggregatedResources(agg, state);
        createAttributeSet(agg, state, Types.DataItem);
    }

    /**
     * BusinessObject enums.
     */
    private enum Types {
        ResMap, Package, Project, Collection, DataItem, File;

        public String getString() {
            return this.toString();
        }
    }

    private void add(Collection<Attribute> attributes, String prefix, String name, String value) {
        add(attributes, AttributeValueType.STRING, prefix, name, value);
    }

    private void add(Collection<Attribute> attributes, String attr_type, String prefix,
            String name, String value) {
        attributes.add(new AttributeImpl(prefix + "-" + name, attr_type, value));
    }

    private void add(Collection<Attribute> attributes, String prefix, String name, Resource res,
            Property prop) throws PackageValidationException {
        add(attributes, AttributeValueType.STRING, prefix, name, res, prop);
    }

    /**
     * Add all objects of the given property with the given subject as
     * attributes.
     * 
     * @param attributes the Collection of Attributes
     * @param attr_type the Attribute Type
     * @param prefix the prefix
     * @param name the name
     * @param res
     *            subject
     * @param prop
     *            subject property
     * @throws PackageValidationException
     */
    private void add(Collection<Attribute> attributes, String attr_type, String prefix,
            String name, Resource res, Property prop) throws PackageValidationException {
        StmtIterator iter = res.listProperties(prop);

        while (iter.hasNext()) {
            add(attributes, attr_type, prefix, name, iter.next().getObject());
        }
    }

    private void add(Collection<Attribute> attributes, String prefix, String name, Resource res)
            throws PackageValidationException {
        add(attributes, AttributeValueType.STRING, prefix, name, res);
    }

    private void add(Collection<Attribute> attributes, String attr_type, String prefix,
            String name, RDFNode node) throws PackageValidationException {
        add(attributes, attr_type, prefix, name, nodeValue(node));
    }

    /**
     * Creates attribute set for a given business object type and adds it to the
     * manager.
     * 
     * @param resource the Resource
     * @param state the State
     * @param type  the BusinessObjectType
     * @throws PackageValidationException
     */
    private void createAttributeSet(Resource resource, State state,
            Types type) throws PackageValidationException {

        String attNamePrefix = type.getString();
        int attsSize;

        AttributeSet attSet = null;

        if (type == Types.Package) {
            attSet = state.attributeMap.get(AttributeSetName.ORE_REM_PACKAGE + "_" + resource.getURI());
            if (attSet == null) {
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_PACKAGE);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.PACKAGE_RESOURCEID, AttributeValueType.STRING, resource.getURI()));
            } else {
                return;
            }
        } else if (type == Types.Project) {
            attSet = state.attributeMap.get(AttributeSetName.ORE_REM_PROJECT + "_" + resource.getURI());
            if (attSet == null) {
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_PROJECT);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.PROJECT_RESOURCEID, AttributeValueType.STRING, resource.getURI()));
            } else {
                return;
            }
        } else if (type == Types.Collection) {
            attSet = state.attributeMap.get(AttributeSetName.ORE_REM_COLLECTION + "_"
                    + resource.getURI());
            if (attSet == null) {
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, resource.getURI()));
            } else {
                return;
            }
        } else if (type == Types.DataItem) {
            attSet = state.attributeMap
                    .get(AttributeSetName.ORE_REM_DATAITEM + "_" + resource.getURI());
            if (attSet == null) {
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, resource.getURI()));
            } else {
                return;
            }
        } else if (type == Types.File) {
            attSet = state.attributeMap.get(AttributeSetName.ORE_REM_FILE + "_" + resource.getURI());
            if (attSet == null) {
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, resource.getURI()));
            } else {
                return;
            }
        }

        if (attSet == null) {
            return;
        }

        Collection<Attribute> atts = attSet.getAttributes();
        attsSize = atts.size();


        if (type == Types.File) {
            add(atts, attNamePrefix, Metadata.PATH, resource.getURI());
        }

        if (resource.hasProperty(DCTerms.creator) || resource.hasProperty(DC.creator)) {
            List<RDFNode> nodes = state.model.listObjectsOfProperty(resource, DC.creator).toList();
            nodes.addAll(state.model.listObjectsOfProperty(resource, DCTerms.creator).toList());

            for (RDFNode node : nodes) {
                if (!node.isResource()) {
                    continue;
                }

                Resource creator = node.asResource();

                add(atts, attNamePrefix, Metadata.CREATOR_NAME, creator, FOAF.name);
                add(atts, attNamePrefix, Metadata.CREATOR_EMAIL, creator, FOAF.mbox);
                add(atts, attNamePrefix, Metadata.CREATOR_PHONE, creator, FOAF.phone);
                add(atts, attNamePrefix, "creator_page", creator, FOAF.page);
            }
        } else {
            log.debug("<creator> property doesn't exist.");
        }
        
        if (resource.hasProperty(ResourceMapConstants.contactPerson)) {
            List<RDFNode> nodes = state.model.listObjectsOfProperty(resource, ResourceMapConstants.contactPerson).toList();

            for (RDFNode node : nodes) {
                if (!node.isResource()) {
                    continue;
                }

                Resource creator = node.asResource();

                add(atts, attNamePrefix, Metadata.CONTACT_NAME, creator, FOAF.name);
                add(atts, attNamePrefix, Metadata.CONTACT_EMAIL, creator, FOAF.mbox);
                add(atts, attNamePrefix, Metadata.CONTACT_PHONE, creator, FOAF.phone);
                add(atts, attNamePrefix, "contact_page", creator, FOAF.page);
            }
        } else {
            log.debug("<creator> property doesn't exist.");
        }
        

        if (resource.hasProperty(AGGREGATES_PROPERTY)) {
            for (RDFNode node : state.model.listObjectsOfProperty(resource, AGGREGATES_PROPERTY).toList()) {
                if (!node.isResource()) {
                    continue;
                }
                
                Resource object = node.asResource();

                if (hasType(object, DC_PROJECT_TYPE)) {
                    add(atts, attNamePrefix, Metadata.AGGREGATES_PROJECT, object);
                } else if (hasType(object, DCMI_COLLECTION_TYPE)) {
                    /* 
                     * Add upward pointing Aggregated-By_Project to the child collection
                     * if this resource is a Project
                     */
                    if (type == Types.Project) {
                        
                        String collectionAttributeSetName = getAttributeSetNameFor(object.getURI(), state.attributeMap);
                        AttributeSet collectionAttributeSet = 
                                state.attributeMap.get(collectionAttributeSetName);

                        List<Attribute> collectionAttributes 
                                = new ArrayList<>(collectionAttributeSet.getAttributes());
                        
                        add(collectionAttributes, Types.Collection.toString(), Metadata.AGGREGATED_BY_PROJECT, resource.getURI());

                        AttributeSetImpl updatedCollectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
                        updatedCollectionAttributeSet.setAttributes(collectionAttributes);
                        state.attributeMap.put(collectionAttributeSetName, updatedCollectionAttributeSet);
                    }
                    
                    add(atts, attNamePrefix, Metadata.AGGREGATES_COLLECTION, object);                    
                } else if (hasType(object, DC_DATA_ITEM_TYPE)) {
                    add(atts, attNamePrefix, Metadata.AGGREGATES_DATAITEM, object);
                } else if (!object.hasProperty(RDF.type)) {
                    // Must be a file
                    add(atts, attNamePrefix, Metadata.AGGREGATES_FILE, object);
                }
            }
        } else {
            log.debug("<aggregates> property doesn't exist");
        }

        add(atts, attNamePrefix, Metadata.PROPERTY, resource);
        
        StmtIterator iterator = resource.listProperties();
        while (iterator.hasNext()) {
            Triple t = iterator.next().asTriple();
            if (t.getObject().isLiteral()) {
                add(atts, attNamePrefix, t.getPredicate().getURI(), t.getObject().getLiteralValue().toString());
            } else if (t.getObject().isURI()) {
                add(atts, attNamePrefix, t.getPredicate().getURI(), t.getObject().getURI());
            }
        }

        if (atts.size() > attsSize) {
            state.attributeMap.put(attSet.getName() + "_" + resource.getURI(), attSet);
        }

        else {
            throw new PackageValidationException("Unable to extract any attributes.");
        }
    }
    
    private String nodeValue(RDFNode node) throws PackageValidationException {

        String value;
        
        if (node.isLiteral()) {
            value = node.asLiteral().getString();
        } else if (node.isURIResource()) {
            value = node.asResource().getURI();
        } else {
            throw new PackageValidationException("Expected resource to be literal or uri: "
                    + node);
        }
        return value;
    }
    
    private class State {
        /* Keeps track of ReMs visited */
        public Set<String> visitedResourceMaps = new HashSet<>();
        
        Model model = ModelFactory.createDefaultModel();
        
        private File baseDir;

        Map<String, AttributeSet> attributeMap = new HashMap<>();
    }
    
    private static String getAttributeSetNameFor(String uri, Map<String,AttributeSet> attributes) {
        for (String attSetName : attributes.keySet()) {
            if (attSetName.contains(uri)) {
                return attSetName;
            }
        }
        return null;
    }
}
