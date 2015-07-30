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

package org.dataconservancy.packaging.tool.impl.generator;

import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.dataconservancy.dcs.model.Pair;
import org.dataconservancy.packaging.shared.ResourceMapConstants;
import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageModelBuilder;
import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.dspace.foresite.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.dataconservancy.packaging.tool.model.DcsPackageDescriptionSpec.ArtifactType;

/**
 * Builds an ORE packaging of the DCS business object model.
 * <p>
 * Given a {@link PackageDescription} full of {@link PackageArtifact} containing
 * types, properties, and relationships enumerated in
 * {@link org.dataconservancy.packaging.tool.model.DcsPackageDescriptionSpec}, this builder will translate each artifact
 * to ORE and include it in an ORE ResourceMap. The ReMs created from all the
 * artifacts, as well as artifact content, is then sent to a
 * {@link PackageAssembler} for inclusion in the package.
 * </p>
 * 
 * @version $Id$
 */
@SuppressWarnings("serial")
public class OrePackageModelBuilder
        implements PackageModelBuilder {

    private static final URI ORE_AGGREGATES = URI
            .create(ResourceMapConstants.AGGREGATES_PROPERTY.getURI());

    private final Map<String, URI> orePropertyMap = new HashMap<String, URI>() {

        {
            put(DcsBoPackageOntology.ID,
                URI.create(DCTerms.identifier.getURI()));
            put(DcsBoPackageOntology.ALTERNATE_ID,
                URI.create(DCTerms.identifier.getURI()));
            put(DcsBoPackageOntology.TITLE, URI.create(DC.title.getURI()));
            put(DcsBoPackageOntology.NAME, URI.create(DCTerms.title.getURI()));
            put(DcsBoPackageOntology.DESCRIPTION,
                URI.create(DC.description.getURI()));

            put(DcsBoPackageOntology.DISCIPLINE,
                URI.create(DC.subject.getURI()));
            put(DcsBoPackageOntology.CITABLE_LOCATOR,
                URI.create(ResourceMapConstants.citableLocator.getURI()));
            put(DcsBoPackageOntology.PUBLISHER,
                URI.create(DCTerms.publisher.getURI()));
            put(DcsBoPackageOntology.PUBLICATION_DATE,
                URI.create(DCTerms.issued.getURI()));
            put(DcsBoPackageOntology.CREATE_DATE,
                URI.create(DCTerms.created.getURI()));
            put(DcsBoPackageOntology.MODIFIED_DATE,
                URI.create(DCTerms.modified.getURI()));

            put(DcsBoPackageOntology.CREATOR, URI.create(DC.creator.getURI()));
            put(DcsBoPackageOntology.CONTACT_INFO,
                URI.create(ResourceMapConstants.contactPerson.getURI()));

            put(DcsBoPackageOntology.PERSON_NAME, URI.create(FOAF.name.getURI()));
            put(DcsBoPackageOntology.EMAIL, URI.create(FOAF.mbox.getURI()));
            put(DcsBoPackageOntology.PHONE, URI.create(FOAF.phone.getURI()));
            put(DcsBoPackageOntology.PAGE, URI.create(FOAF.page.getURI()));

            put(DcsBoPackageOntology.FORMAT,
                URI.create(DCTerms.conformsTo.getURI()));
            put(DcsBoPackageOntology.SIZE,
                URI.create(ResourceMapConstants.size.getURI()));
            
            put(DcsBoPackageOntology.FILE_NAME, 
                URI.create(ResourceMapConstants.fileName.getURI()));
            
            put(DcsBoPackageOntology.CONTENT_MODEL, URI.create(DCTerms.conformsTo.getURI()));
        }
    };

    /*
     * We use relationships in the ORE pointing from parent to child, which is
     * the reverse of the way they're represented in the PackageDescription. So
     * this maps the child->parent PackageDescription relationship to the
     * parent-child ORE relationship
     */
    private final Map<String, URI> oreReverseRelationshipMap =
            new HashMap<String, URI>() {

                {
                    put(DcsBoPackageOntology.IS_MEMBER_OF, ORE_AGGREGATES);
                    put(DcsBoPackageOntology.IS_METADATA_FOR, ORE_AGGREGATES);
                }
            };

    private final Map<String, URI> oreForwardRelationshipMap =
            new HashMap<String, URI>() {

                {
                    put(DcsBoPackageOntology.IS_METADATA_FOR,
                        URI.create(ResourceMapConstants.IS_METADATA_FOR_PROPERTY
                                .toString()));
                }
            };

    private static final Map<ArtifactType, URI> oreAggregationTypes =
            new HashMap<ArtifactType, URI>() {

                {
                    put(ArtifactType.DataItem,
                        URI.create(ResourceMapConstants.DC_DATA_ITEM_TYPE
                                .getURI()));
                    put(ArtifactType.Collection,
                        URI.create(ResourceMapConstants.DCMI_COLLECTION_TYPE
                                .getURI()));
                    put(ArtifactType.Project,
                        URI.create(ResourceMapConstants.DC_PROJECT_TYPE
                                .getURI()));
                }
            };

    private final Map<ArtifactType, Set<PackageArtifact>> artifactsByType =
            new HashMap<>();

    private final Map<String, PackageArtifact> artifactsById =
            new HashMap<>();

    private final Map<PackageArtifact, Set<Pair<URI, PackageArtifact>>> oreRelationships =
            new HashMap<>();

    /* Maps an artifact to its rdf identity in a ReM */
    private final Map<PackageArtifact, URI> oreIdentities =
            new HashMap<>();

    /* Maps an artifact to the containing ReM */
    private final Map<PackageArtifact, URI> oreRemForArtifact =
            new HashMap<>();

    private final Map<URI, ResourceMap> oreRems =
            new HashMap<>();

    private URI packageRemURI;

    private File rootContentFile;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PackageGenerationParameters params) {
        rootContentFile = new File(params.getParam(GeneralParameterNames.CONTENT_ROOT_LOCATION,0));
    }

    /**
     * Get the URI of the package ReM.
     * <p>
     * This is not part of the interface, but is an add-on for the BOREM spec.
     * The BOREM spec requires that the uppermost ReM be identified. This
     * assures that an assembler can know what that ReM is.
     * 
     * @return the URI of the package rem.
     */
    public URI getPackageRemURI() {
        return packageRemURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildModel(PackageDescription desc, PackageAssembler assembler) {
        try {

            /*
             * Do a first pass on artifacts, collecting data about type,
             * relationships, etc
             */
            analyzeArtifacts(desc);

            /*
             * Now, we'll add the aggregating types and all their descendants.
             * All DataFiles and MetadataFiles will implicitly be added at this
             * point.
             */

            /* First, add all DataItems and descendants */
            for (PackageArtifact dataItem : artifactsByType
                    .get(ArtifactType.DataItem)) {
                addAggregationFor(dataItem, assembler);
            }

            /*
             * Next, do Collections and all descendants.
             */
            for (PackageArtifact dataItem : artifactsByType
                    .get(ArtifactType.Collection)) {
                addAggregationFor(dataItem, assembler);
            }

            /*
             * Finally, any Projects.
             */
            for (PackageArtifact dataItem : artifactsByType
                    .get(ArtifactType.Project)) {
                addAggregationFor(dataItem, assembler);
            }

            /* Now, create the package ReM */
            ResourceMap packageRem = createREM(assembler);
            Aggregation packageAggregation =
                    packageRem.createAggregation(URI.create(packageRem.getURI()
                            .toString() + "#Aggregation"));
            packageAggregation.addType(URI
                    .create(ResourceMapConstants.DC_PACKAGE_TYPE.toString()));
            packageRemURI = packageRem.getURI();

            ORESerialiser serializer =
                    ORESerialiserFactory.getInstance("RDF/XML");

            /* Add the root artifact aggregation to the package */
            if (desc.getRootArtifact() != null) {
                ResourceMap artifactRem =
                        oreRems.get(oreRemForArtifact.get(desc.getRootArtifact()));
                if (artifactRem != null) {
                    AggregatedResource remAggregation =
                            packageAggregation.createAggregatedResource(artifactRem
                                    .getAggregation().getURI());
                    remAggregation.addResourceMap(artifactRem.getURI());
                }
            }
            for (ResourceMap rem : oreRems.values()) {
                writeREM(rem, serializer, assembler);
            }

            /* Finally, validate that all artifacts made their way into a ReM */
            for (PackageArtifact artifact : desc.getPackageArtifacts()) {
                if (!oreRemForArtifact.containsKey(artifact) && !artifact.isIgnored()) {
                    throw new RuntimeException(String.format("Artifact %s (%s) is not in a ReM.  "
                                                                     + "This is likely because it is an orphan. "
                                                                     + "Check its relationships.",
                                                             artifact.getId(),
                                                             artifact.getType()));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void analyzeArtifacts(PackageDescription desc) {

        /* First, initialize the maps */
        for (ArtifactType type : ArtifactType.values()) {
            artifactsByType.put(type, new HashSet<>());
        }

        /* Then categorize by id and type */
        for (PackageArtifact artifact : desc.getPackageArtifacts()) {
            if (!artifact.isIgnored()) {
                /* First, categorize artifacts by ID and type */
                ArtifactType type = ArtifactType.valueOf(artifact.getType());

                artifactsByType.get(type).add(artifact);
                artifactsById.put(artifact.getId(), artifact);
                oreRelationships.put(artifact,
                        new HashSet<>());
            }
        }

        /* Finally, categorize relationships */
        for (PackageArtifact artifact : desc.getPackageArtifacts()) {
            if (!artifact.isIgnored()) {
                /*
                 * Then, construct a map relationships, in the direction ORE wants.
                 * Some are reversed with respect to their representation in the
                 * PackageArtifact, some are forward.
                 */
                for (PackageRelationship rel : artifact.getRelationships()) {

                /* Record reverse relationship (if defined) */
                    URI oreRel = getReverseRelationshipURI(rel.getName());

                    if (oreRel != null) {
                        registerRels(true, artifact, oreRel, rel.getTargets());

                        // Hack to handle relationships present in both forward and reverse maps
                        if (oreForwardRelationshipMap.containsKey(rel.getName())) {
                            registerRels(false, artifact, getForwardRelationshipURI(rel.getName()), rel.getTargets());
                        }
                    } else {
                        oreRel = getForwardRelationshipURI(rel.getName());

                        if (oreRel != null) {
                            registerRels(false, artifact, oreRel, rel.getTargets());
                        } else {
                            registerRels(false, artifact, URI.create(rel.getName()), rel.getTargets());
                        }
                    }
                }
            }
        }
    }

    private void registerRels(boolean relationshipIsReversed,
                              PackageArtifact artifact,
                              URI rel,
                              Set<String> targets) {
        for (String target : targets) {
            PackageArtifact subject;
            PackageArtifact object;

            /* target may be an artifact ID, or may not.  If it's an artifact ID, it'll 
             * match an artifact, which will be the object of the relationship.  If not, 
             * the object will be null.
             */
            if (relationshipIsReversed) {
                subject = artifactsById.get(target);
                object = artifact;
            } else {
                subject = artifact;
                object = artifactsById.get(target);
            }

            if (subject != null ) {
                oreRelationships.get(subject)
                        .add(new Pair<>(rel, object));
            }

        }
    }

    private ResourceMap createREM(PackageAssembler assembler)
            throws OREException {
        URI remURI =
                assembler.reserveResource(String.format("/ORE-REM/%s-REM.xml",
                                                        UUID.randomUUID()
                                                                .toString()),
                                          PackageResourceType.METADATA);
        ResourceMap rem = OREFactory.createResourceMap(remURI);
        rem.clearCreators();
        oreRems.put(remURI, rem);
        return rem;
    }

    private void writeREM(ResourceMap rem,
                          ORESerialiser serializer,
                          PackageAssembler assembler) throws OREException,
            ORESerialiserException {

        Aggregation aggregation = rem.getAggregation();
        aggregation.clearReMSerialisations();
        aggregation
                .removeTriple(OREFactory.createTriple(aggregation,
                                                      new Predicate(URI
                                                              .create(ResourceMapConstants.IS_DESCRIBED_BY_PROPERTY
                                                                      .getURI())),
                                                      rem));
        rem.clearCreators();

        assembler.putResource(rem.getURI(), IOUtils.toInputStream(serializer
                .serialise(rem).toString()));
    }

    private URI getPropertyURI(String pkgDescriptionPropertyName) {

        if (!orePropertyMap.containsKey(pkgDescriptionPropertyName)) {
            try {
                URI uri = new URI(pkgDescriptionPropertyName);
                if (uri.getScheme() == null) {
                    throw new URISyntaxException(pkgDescriptionPropertyName,
                                                 "scheme");
                }
                return new URI(pkgDescriptionPropertyName);
            } catch (URISyntaxException e) {
                throw new RuntimeException(pkgDescriptionPropertyName
                        + " is neither a known property name, nor a URI");
            }
        }

        return orePropertyMap.get(pkgDescriptionPropertyName);
    }

    private URI getReverseRelationshipURI(String pkgDescriptionRelName) {
        return oreReverseRelationshipMap.get(pkgDescriptionRelName);
    }

    private URI getForwardRelationshipURI(String pkgDescriptionRelName) {

        if (!oreForwardRelationshipMap.containsKey(pkgDescriptionRelName)) {
            try {
                URI uri = new URI(pkgDescriptionRelName);
                if (uri.getScheme() == null) {
                    throw new URISyntaxException(pkgDescriptionRelName,
                                                 "scheme");
                }
                
                return uri;
            } catch (URISyntaxException e) {
                throw new RuntimeException(pkgDescriptionRelName
                        + " is neither a known relationship name, nor a URI");
            }
        }

        return oreForwardRelationshipMap.get(pkgDescriptionRelName);
    }

    private void addAggregationFor(PackageArtifact artifact,
                                   PackageAssembler assembler) throws Exception {

        ResourceMap rem = createREM(assembler);

        /* Create the aggregation */
        Aggregation aggregation =
                rem.createAggregation(new URI(rem.getURI().toString()
                        + "#Aggregation"));
        aggregation.clearReMSerialisations();
        aggregation
                .removeTriple(OREFactory.createTriple(aggregation,
                        new Predicate(URI
                                .create(ResourceMapConstants.IS_DESCRIBED_BY_PROPERTY
                                        .getURI())),
                        rem));
        aggregation.addType(oreAggregationTypes.get(ArtifactType
                .valueOf(artifact.getType())));

        /* Track the mapping of artifact to aggregation, and ReM */
        oreIdentities.put(artifact, aggregation.getURI());
        oreRemForArtifact.put(artifact, rem.getURI());

        /* Add properties */
        addPropertiesTo(aggregation, artifact);

        /* Add Relationships */
        addRelationshipsFrom(artifact, aggregation, rem, assembler);

    }

    /*
     * DataFiles and MetadataFiles are the only Resources in ORE terms that
     * aren't aggregations
     */
    private void addResourceFor(PackageArtifact artifact,
                                Aggregation aggregation,
                                ResourceMap rem,
                                PackageAssembler assembler) throws Exception {
        /* Get file content */
        URI artifactFileURI = artifact.getArtifactRef().getResolvedAbsoluteRefPath(rootContentFile).toUri();
        URIBuilder urib = new URIBuilder(artifactFileURI);
        urib.setScheme("file");

        URL contentLocation = urib.build().toURL();

        /* If file path isn't set, use the artifact ref */
        String path = artifact.getArtifactRef().getRefString().replace(File.separatorChar, '/');

        URI resourceURI =
                assembler.createResource(path,
                                         PackageResourceType.DATA,
                                         contentLocation.openStream());

        AggregatedResource aggregatedResource =
                aggregation.createAggregatedResource(resourceURI);
        aggregation
                .addTriple(OREFactory.createTriple(aggregation,
                                                   new Predicate(URI
                                                           .create(ResourceMapConstants.AGGREGATES_PROPERTY
                                                                   .getURI())),
                                                   aggregatedResource));

        /* Track */
        oreIdentities.put(artifact, aggregatedResource.getURI());
        oreRemForArtifact.put(artifact, rem.getURI());

        /* Add properties */
        addPropertiesTo(aggregatedResource, artifact);

        /* Add Relationships */
        addRelationshipsFrom(artifact, aggregatedResource, rem, assembler);
    }

    private void addRelationshipsFrom(PackageArtifact fromArtifact,
                                      OREResource toOreResource,
                                      ResourceMap inRem,
                                      PackageAssembler assembler)
            throws Exception {

        if (!oreRelationships.containsKey(fromArtifact)) {
            return;
        }

        /*
         * Add it's outward pointing relationships. If the target of the
         * relationship doesn't exist anywhere, create is as appropriate.
         */
        for (Pair<URI, PackageArtifact> relationship : oreRelationships
                .get(fromArtifact)) {
            
            /* For relationships that don't point to a artifact, assume that they 
             * point to some external resource.
             */
            if (relationship.getValue() == null) {
                PackageRelationship artifactRelationship =  fromArtifact.getRelationshipByName(relationship.getKey().toString());
                for (String target : artifactRelationship.getTargets()) {
                    
                    /* We only care about relationship targets that *aren't* package artifacts here.*/
                    if (!artifactsById.containsKey(target)) {

                        if (artifactRelationship.requiresUriTargets()) {
                            /* Just put in a triple with the target (external resource) as a URI */
                            toOreResource.createTriple(relationship.getKey(), URI.create(target));
                        } else {
                            toOreResource.createTriple(relationship.getKey(), target);
                        }
                    }
                }
                continue;
            }

            /*
             * If it points to an artifact that isn't in a ReM yet, create it.
             */
            if (!oreIdentities.containsKey(relationship.getValue())) {
                if (oreAggregationTypes.containsKey(ArtifactType
                        .valueOf(relationship.getValue().getType()))) {
                    addAggregationFor(relationship.getValue(), assembler);
                } else {
                    addResourceFor(relationship.getValue(),
                            inRem.getAggregation(),
                            inRem,
                            assembler);
                }
            }


            /*
             * If it's defined in another ReM, then add a resource that
             * ore:isDescribedBy the other ReM, and point to that
             */
            if (!oreRemForArtifact.get(relationship.getValue())
                    .equals(inRem.getURI())) {
                AggregatedResource resource =
                        inRem.getAggregation()
                                .createAggregatedResource(oreIdentities.get(relationship
                                        .getValue()));
                resource.clearResourceMaps();
                resource.removeTriple(OREFactory.createTriple(resource,
                                                              new Predicate(URI
                                                                      .create(ResourceMapConstants.IS_DESCRIBED_BY_PROPERTY
                                                                              .getURI())),
                                                              toOreResource));
                resource.addResourceMap(oreRemForArtifact.get(relationship
                        .getValue()));

            }

            toOreResource.createTriple(relationship.getKey(), oreIdentities
                    .get(relationship.getValue()));

        }
    }

    /* Add property triples one by one */
    private void addPropertiesTo(OREResource oreResource,
                                 PackageArtifact artifact) throws Exception {

        /* Add properties */
        for (String key : artifact.getPropertyNames()) {

            URI predicate = getPropertyURI(key);

            if (artifact.hasSimpleProperty(key)) {
                for (String value : artifact.getSimplePropertyValues(key)) {
                    if (key.equals(DcsBoPackageOntology.PHONE) || key.equals(DcsBoPackageOntology.PAGE)
                            || key.equals(DcsBoPackageOntology.EMAIL)) {
                        oreResource.createTriple(predicate, URI.create(value));
                    } else {
                        oreResource.createTriple(predicate, value);
                    }
                }
            }

            else if (artifact.hasPropertyValueGroup(key)) {
                for (PropertyValueGroup group : artifact
                        .getPropertyValueGroups(key)) {
                    Agent groupAgent = new Agent(oreResource, predicate);

                    for (String subKey : group.getSubPropertyNames()) {
                        URI subPropPredicate = getPropertyURI(subKey);

                        for (String value : group.getSubPropertyValues(subKey)) {
                            if (subKey.equals(DcsBoPackageOntology.PHONE) || subKey.equals(DcsBoPackageOntology.PAGE)
                                    || subKey.equals(DcsBoPackageOntology.EMAIL)) {
                                groupAgent.addProperty(subPropPredicate, URI.create(value));
                            } else {
                                groupAgent.addProperty(subPropPredicate, value);
                            }
                        }
                    }
                    groupAgent.addTo(oreResource);
                }
            }
        }
    }

    /*
     * Foresite doesn't handle blank nodes that well, so this encapsulates the
     * ugliness of adding raw triples. We use NTriples format because RDF/XML is
     * a bit too complicated and nasty for this purpose.
     */
    private static class Agent {

        private static AtomicInteger blankNode = new AtomicInteger();

        private final StringBuilder rdf = new StringBuilder();

        private final int nodeid = blankNode.incrementAndGet();

        public Agent(OREResource resource, URI rel)
                throws OREException {
            rdf.append(String.format("<%s> <%s> _:%d .\n", resource.getURI()
                    .toString(), rel.toString(), nodeid));
        }

        public void addProperty(URI property, String object) {
            rdf.append(String.format("_:%d <%s> \"%s\" .\n",
                                     nodeid,
                                     property.toString(),
                                     object));
        }

        public void addProperty(URI property, URI object) {
            rdf.append(String.format("_:%d <%s> <%s> .\n",
                                                 nodeid,
                                                 property.toString(),
                                                 object.toString()));
        }

        private void addTo(OREResource resource) throws OREException {
            resource.addRDF(rdf.toString(), "N-TRIPLES");
        }
    }
}
