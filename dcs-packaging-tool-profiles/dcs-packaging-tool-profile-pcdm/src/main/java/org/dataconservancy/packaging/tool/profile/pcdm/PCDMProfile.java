
package org.dataconservancy.packaging.tool.profile.pcdm;

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

import java.io.FileOutputStream;

import java.net.URI;

import java.util.Arrays;
import java.util.Collections;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

import org.dataconservancy.packaging.tool.impl.DomainProfileRdfTransformService;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.FileAssociation;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;

import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_DCS_PKGTOOL_PROFILE_PCDM;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_PCDM;

/** A reasonable PCDM profile */
public class PCDMProfile
        extends DomainProfile {

    private static final String PCDM_PROFILE_ID =
            "http://dataconservancy.org/ptg-profiles/PCDM-1.0";

    /* PCDM Objects */

    final NodeType administrativeSet = new NodeType();

    final NodeType collection = new NodeType();

    final NodeType object = new NodeType();

    final NodeType file = new NodeType();

    /* PCDM Properties */
    final PropertyType hasRelatedObject = new PropertyType();

    /* PCDM Structural Relations */

    final StructuralRelation fileRelation =
            new StructuralRelation(URI.create(NS_PCDM + "fileOf"),
                                   URI.create(NS_PCDM + "hasFile"));

    final StructuralRelation memberRelation =
            new StructuralRelation(URI.create(NS_PCDM + "memberOf"),
                                   URI.create(NS_PCDM + "hasMember"));

    /* PCDM node transforms */
    final NodeTransform administrativeSet_to_collection = new NodeTransform();

    final NodeTransform collection_to_administrativeSet = new NodeTransform();

    final NodeTransform collection_to_object = new NodeTransform();

    final NodeTransform object_to_collection = new NodeTransform();

    public PCDMProfile() {

        /* Prepare data to go into the profile */

        defineNodeTypes();

        defineNodeTransforms();

        definePropertyTypes();

        setPropertyConstraints();

        setRelationshipConstraints();

        setFileAssociations();

        setSuppliedValues();

        /*set the label to display in the GUI profile picker */
        setLabel("PCDM Profile");

        /* Now, populate the profile with our profile data */
        setDomainIdentifier(URI.create("http://example.org/myDomainIdentifier"));

        setIdentifier(URI.create(PCDM_PROFILE_ID));

        setNodeTypes(Arrays.asList(administrativeSet, collection, object, file));

        setNodeTransforms(Arrays.asList(administrativeSet_to_collection,
                                        collection_to_administrativeSet,
                                        collection_to_object,
                                        object_to_collection));

        setPropertyTypes(Collections.emptyList());
    }

    private void defineNodeTypes() {
        administrativeSet.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_PCDM
                + "AdministrativeSet"));
        administrativeSet.setLabel("AdministrativeSet");
        administrativeSet.setDescription("AdministrativeSet object");
        administrativeSet.setDomainTypes(Collections.singletonList(URI.create(
            NS_PCDM + "AdministrativeSet")));
        administrativeSet.setDomainProfile(this);

        collection.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_PCDM
                + "Collection"));
        collection.setLabel("Collection");
        collection.setDescription("Collection");
        collection.setDomainTypes(Collections.singletonList(URI.create(
            NS_PCDM + "Collection")));
        collection.setDomainProfile(this);

        object.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_PCDM + "Object"));
        object.setLabel("Object");
        object.setDescription("Object");
        object.setDomainTypes(Collections.singletonList(URI.create(
            NS_PCDM + "Object")));
        object.setDomainProfile(this);

        file.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_PCDM + "File"));
        file.setLabel("File");
        file.setDescription("File");
        file.setDomainTypes(Collections.singletonList(URI.create(
            NS_PCDM + "File")));
        file.setDomainProfile(this);

    }

    private void definePropertyTypes() {
        hasRelatedObject.setLabel("has related object");
        hasRelatedObject
                .setDescription("Links to a related Object that is not a component part, such as an object representing a donor agreement or policies that govern the resource.");
        hasRelatedObject.setDomainPredicate(URI.create(NS_PCDM
                + "hasRelatedObject"));
        hasRelatedObject.setPropertyValueType(PropertyValueType.URI);
        hasRelatedObject.setPropertyValueHint(PropertyValueHint.URL);

        setPropertyTypes(Collections.singletonList(hasRelatedObject));
    }

    private void setPropertyConstraints() {
        administrativeSet.setPropertyConstraints(Collections.singletonList(zeroOrMore(hasRelatedObject)));
        collection.setPropertyConstraints(Collections.singletonList(zeroOrMore(hasRelatedObject)));
        object.setPropertyConstraints(Collections.singletonList(zeroOrMore(hasRelatedObject)));
        file.setPropertyConstraints(Collections.emptyList());
    }

    private void setRelationshipConstraints() {

        administrativeSet.setParentConstraints(Collections.singletonList(allowNoParent()));

        collection
                .setParentConstraints(Arrays
                        .asList(allowNoParent(),
                                allowRelationshipTo(collection, memberRelation),
                                allowRelationshipTo(administrativeSet,
                                                    memberRelation)));

        object.setParentConstraints(Arrays
                .asList(allowNoParent(),
                        allowRelationshipTo(administrativeSet, memberRelation),
                        allowRelationshipTo(collection, memberRelation),
                        allowRelationshipTo(object, memberRelation)));

        file.setParentConstraints(Arrays
                .asList(allowNoParent(),
                        allowRelationshipTo(object, fileRelation)));
    }

    private void setFileAssociations() {
        administrativeSet.setFileAssociation(FileAssociation.DIRECTORY);
        collection.setFileAssociation(FileAssociation.DIRECTORY);
        object.setFileAssociation(FileAssociation.DIRECTORY);
        file.setFileAssociation(FileAssociation.REGULAR_FILE);
    }

    private void defineNodeTransforms() {

        /*
         * Collections can be transformed to AdministrativeSets if it has no
         * parents
         */
        collection_to_administrativeSet
                .setLabel("AdministrativeSet to Collection");
        collection_to_administrativeSet
                .setDescription("Transform a Collection to AdministrativeSet");
        collection_to_administrativeSet.setSourceNodeType(collection);
        collection_to_administrativeSet
                .setSourceParentConstraint(allowNoParent());
        collection_to_administrativeSet.setResultNodeType(administrativeSet);

        /* AdministrativeSets can always be transformed to collections */
        administrativeSet_to_collection
                .setLabel("AdministrativeSet to Collection");
        administrativeSet_to_collection
                .setDescription("Transforms an AdministrativeSet into a collection");
        administrativeSet_to_collection.setSourceNodeType(administrativeSet);
        administrativeSet_to_collection.setResultNodeType(collection);

        /*
         * Collection can always be transformed to object.
         */
        collection_to_object.setLabel("Collection to Object");
        collection_to_object
                .setDescription("Transforms a Collection into an Object");
        collection_to_object.setSourceNodeType(collection);
        collection_to_object.setResultNodeType(object);

        /* Object can be transformed to Collection if it has no file */
        object_to_collection.setLabel("Object to Collection");
        object_to_collection
                .setDescription("Transforms an Object into a Collection");
        object_to_collection.setSourceNodeType(object);
        object_to_collection.setSourceChildConstraints(Collections.singletonList(disallowRelationship(file, fileRelation)));
        object_to_collection.setResultNodeType(collection);

    }

    private void setSuppliedValues() {
        /* None! */
    }

    private static PropertyConstraint zeroOrMore(PropertyType prop) {
        PropertyConstraint constraint = new PropertyConstraint();
        constraint.setPropertyType(prop);
        constraint.setMin(0);
        constraint.setMax(-1);
        return constraint;
    }

    private static NodeConstraint allowRelationshipTo(NodeType parentType,
                                                      StructuralRelation rel) {
        NodeConstraint constraint = new NodeConstraint();
        constraint.setNodeType(parentType);
        constraint.setStructuralRelation(rel);
        return constraint;
    }

    private static NodeConstraint disallowRelationship(NodeType type,
                                                       StructuralRelation rel) {
        NodeConstraint constraint = new NodeConstraint();
        constraint.setNodeType(type);
        constraint.setMatchesNone(true);
        constraint.setStructuralRelation(rel);
        return constraint;
    }

    private static NodeConstraint allowNoParent() {
        NodeConstraint noParentConstraint = new NodeConstraint();
        noParentConstraint.setMatchesNone(true);

        return noParentConstraint;
    }

    /**
     * Serialize the profile to a file.
     * 
     * @param args
     *        Expects two arguments args[0] is the name of a file, and args[1]
     *        is a RIOT name for a serialization format (an {@link RDFFormat},
     *        e.g. TURTLE_PRETTY).
     * @throws Exception if we can't write the serialized profile to the file
     */
    public static void main(String[] args) throws Exception {
        DomainProfileRdfTransformService xform =
                new DomainProfileRdfTransformService();

        Model model = xform.transformToRdf(new PCDMProfile(), "");
        PrefixMap PREFIX_MAP = PrefixMapFactory.create(Ontologies.PREFIX_MAP);

        try (FileOutputStream out = new FileOutputStream(args[0])) {

            RDFDataMgr.createGraphWriter((RDFFormat) RDFFormat.class
                    .getDeclaredField(args[1]).get(null))
                    .write(out, model.getGraph(), PREFIX_MAP, null, null);
        }
    }

}
