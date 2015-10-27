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

package org.dataconservancy.packaging.tool.profile;

import java.io.FileOutputStream;

import java.net.URI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import org.dataconservancy.packaging.tool.model.dprofile.SuppliedProperty;

/** Default Data Conservancy business object profile */
public class DcsBOProfile
        extends DomainProfile {

    private static final String BO_PROFILE_ID =
            "http//dataconservancy.org/ptg-profiles/dcs-bo-1.0";

    private static final String BO_PROFILE_BASE = BO_PROFILE_ID + '#';

    private static final String BO_ONTOLOGY_BASE =
            "http://www.dataconservancy.org/business-object-model#";

    private static final String FOAF_BASE = "http://xmlns.com/foaf/0.1/";

    /* Business objects */

    private final NodeType project = new NodeType();

    private final NodeType collection = new NodeType();

    private final NodeType dataItem = new NodeType();

    private final NodeType file = new NodeType();

    private final NodeType person = new NodeType();

    /* Transforms */
    private final NodeTransform collection_to_project = new NodeTransform();

    private final NodeTransform project_to_collection = new NodeTransform();

    private final NodeTransform dataItem_to_collection = new NodeTransform();

    private final NodeTransform collection_to_dataItem = new NodeTransform();

    private final NodeTransform metadata_to_file = new NodeTransform();

    private final NodeTransform file_to_metadata = new NodeTransform();

    /* Properties */

    private final StructuralRelation metadataRel =
            new StructuralRelation(URI.create(BO_ONTOLOGY_BASE + "metadataFor"),
                                   URI.create(BO_ONTOLOGY_BASE + "hasMetadata"));

    private final StructuralRelation memberRel =
            new StructuralRelation(URI.create(BO_ONTOLOGY_BASE + "isMemberOf"),
                                   URI.create(BO_ONTOLOGY_BASE + "hasMember"));

    private final PropertyType hasBusinessID = new PropertyType();

    private final PropertyType hasAlternateId = new PropertyType();

    private final PropertyType hasTitle = new PropertyType();

    private final PropertyType hasDescription = new PropertyType();

    private final PropertyType hasCitableLocator = new PropertyType();

    private final PropertyType hasCreator = new PropertyType();

    private final PropertyType hasContact = new PropertyType();

    private final PropertyType hasCreateDate = new PropertyType();

    private final PropertyType hasModifiedDate = new PropertyType();

    private final PropertyType hasPublicationDate = new PropertyType();

    private final PropertyType hasDepositDate = new PropertyType();

    private final PropertyType hasDiscipline = new PropertyType();

    private final PropertyType hasContentModel = new PropertyType();

    private final PropertyType hasSize = new PropertyType();

    private final PropertyType hasPublisher = new PropertyType();

    private final PropertyType hasAlottedStorage = new PropertyType();

    private final PropertyType hasUsedStorage = new PropertyType();

    private final PropertyType hasStartDate = new PropertyType();

    private final PropertyType hasEndDate = new PropertyType();

    private final PropertyType hasFundingEntity = new PropertyType();

    private final PropertyType hasPrincipalInvestigator = new PropertyType();

    private final PropertyType hasAwardNumber = new PropertyType();

    // Maybe we don't do this
    //private final PropertyType hasFixity = new PropertyType();

    private final PropertyType hasFormat = new PropertyType();

    private final PropertyType name = new PropertyType();

    private final PropertyType phone = new PropertyType();

    private final PropertyType mbox = new PropertyType();

    private final PropertyType homepage = new PropertyType();

    /*
     * Maybe it makes sense to have a central location for prefix maps,
     * project-wide
     */
    @SuppressWarnings("serial")
    private static final PrefixMap PREFIX_MAP = PrefixMapFactory
            .create(new HashMap<String, String>() {

                {
                    put("prof", "http://www.dataconservancy.org/ptg-prof/");
                    put("datacons", "http://dataconservancy.org/ns/types/");
                    put("boprof", BO_PROFILE_BASE);
                    put("bom", BO_ONTOLOGY_BASE);
                    put("foaf", FOAF_BASE);
                }
            });

    public DcsBOProfile() {

        /* Prepare data to go into the profile */

        defineNodeTypes();

        defineNodeTransforms();

        definePropertyTypes();

        setPropertyConstraints();

        setRelationshipConstraints();;

        setFileAssociations();

        setSuppliedValues();

        /* Now, populate the profile with our profile data */
        setDomainIdentifier(URI.create("http://example.org/myDomainIdentifier"));

        setIdentifier(URI.create(BO_PROFILE_ID));

        setNodeTypes(Arrays.asList(project, collection, dataItem, file));

        setNodeTransforms(Arrays.asList(project_to_collection,
                                        collection_to_project,
                                        collection_to_dataItem,
                                        dataItem_to_collection,
                                        metadata_to_file,
                                        file_to_metadata));

        setPropertyTypes(Arrays.asList(hasBusinessID,
                                       hasAlternateId,
                                       hasCitableLocator,
                                       hasContact,
                                       hasContentModel,
                                       hasCreateDate,
                                       hasCreator,
                                       hasDepositDate,
                                       hasDescription,
                                       hasDiscipline,
                                       hasFormat,
                                       hasModifiedDate,
                                       hasPublicationDate,
                                       hasSize,
                                       hasTitle,
                                       homepage,
                                       name,
                                       mbox,
                                       phone));
    }

    private void defineNodeTypes() {
        project.setIdentifier(URI.create(BO_PROFILE_BASE + "Project"));
        project.setLabel("Project");
        project.setDescription("Project business object");
        project.setDomainTypes(Arrays.asList(URI.create(BO_ONTOLOGY_BASE
                + "Project")));
        project.setDomainProfile(this);

        collection.setIdentifier(URI.create(BO_PROFILE_BASE + "Collection"));
        collection.setLabel("Collection");
        collection.setDescription("Collection business object");
        collection.setDomainTypes(Arrays.asList(URI.create(BO_ONTOLOGY_BASE
                + "Collection")));
        collection.setDomainProfile(this);

        dataItem.setIdentifier(URI.create(BO_PROFILE_BASE + "DataItem"));
        dataItem.setLabel("DataItem");
        dataItem.setDescription("DataItem business object");
        dataItem.setDomainTypes(Arrays.asList(URI.create(BO_ONTOLOGY_BASE
                + "DataItem")));
        dataItem.setDomainProfile(this);

        file.setIdentifier(URI.create(BO_PROFILE_BASE + "File"));
        file.setLabel("File");
        file.setDescription("File business object");
        file.setDomainTypes(Arrays.asList(URI.create(BO_ONTOLOGY_BASE + "File")));
        file.setDomainProfile(this);

        person.setIdentifier(URI.create(BO_PROFILE_BASE + "Person"));
        person.setLabel("Person");
        person.setDescription("Person business object");
        person.setDomainTypes(Arrays.asList(URI.create(BO_ONTOLOGY_BASE
                + "Person")));
        person.setDomainProfile(this);

    }

    private void definePropertyTypes() {
        hasBusinessID.setLabel("Business ID");
        hasBusinessID
                .setDescription("A data property specifying a business identifier for the BusinessObject");
        hasBusinessID.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasBusinessID"));
        hasBusinessID.setPropertyValueType(PropertyValueType.LONG);

        hasAlternateId.setLabel("Alternate ID");
        hasAlternateId
                .setDescription("A data property specifying an alternate identifier for the BusinessObject");
        hasAlternateId.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasAlternateId"));
        hasAlternateId.setPropertyValueType(PropertyValueType.STRING);

        hasTitle.setLabel("Title");
        hasTitle.setDescription("A data property specifying a title for a Project, Collection, DataItem or File");
        hasTitle.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE + "hasTitle"));
        hasTitle.setPropertyValueType(PropertyValueType.STRING);

        hasDescription.setLabel("Description");
        hasDescription
                .setDescription("A data property specifying a description for a Project, Collection, DataItem or File");
        hasDescription.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasDescription"));
        hasDescription.setPropertyValueType(PropertyValueType.STRING);
        hasDescription.setPropertyValueHint(PropertyValueHint.TEXT);

        hasCitableLocator.setLabel("Citable Locator");
        hasCitableLocator
                .setDescription("A data property specifying a citable locator for the Collection or DataItem.");
        hasCitableLocator.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasCitableLocator"));
        hasCitableLocator.setPropertyValueType(PropertyValueType.STRING);

        hasCreateDate.setLabel("Create Date");
        hasCreateDate
                .setDescription("A data property specifying the create date for a Collection, DataItem or File");
        hasCreateDate.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasCreateDate"));
        hasCreateDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasModifiedDate.setLabel("Modified Date");
        hasModifiedDate
                .setDescription("A data property specifying the modified date for a Collection, DataItem or File");
        hasModifiedDate.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasModifiedDate"));
        hasModifiedDate.setPropertyValueType(PropertyValueType.DATE_TIME);;

        hasDepositDate.setLabel("Deposit Date");
        hasDepositDate
                .setDescription("A data property specifying the deposit date for a Collection, DataItem or File");
        hasDepositDate.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasDepositDate"));
        hasDepositDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasPublicationDate.setLabel("Publication Date");
        hasPublicationDate
                .setDescription("A data property specifying the publication date for a Collection, DataItem or File");
        hasPublicationDate.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasPublicationDate"));
        hasPublicationDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasDiscipline.setLabel("Discipline");
        hasDiscipline
                .setDescription("A data property specifying a discipline for a Collection");
        hasDiscipline.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasDiscipline"));
        hasDiscipline.setPropertyValueType(PropertyValueType.STRING);

        hasContentModel.setLabel("Content Model");
        hasContentModel
                .setDescription("A data property specifying the content model for a DataItem.");
        hasContentModel.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasContentModel"));
        hasContentModel.setPropertyValueType(PropertyValueType.STRING);

        hasSize.setLabel("File size");
        hasSize.setDescription("A data property specifying the size of a File");
        hasSize.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE + "hasSize"));
        hasSize.setPropertyValueType(PropertyValueType.LONG);
        hasSize.setPropertyValueHint(PropertyValueHint.FILE_SIZE);
        hasSize.setReadOnly(true);

        /*
         * hasFixity.setLabel("Fixity"); hasFixity
         * .setDescription("A data property specifying the fixity of a File");
         * hasFixity.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE +
         * hasFixity));
         * hasFixity.setPropertyValueType(PropertyValueType.STRING);
         * hasFixity.setReadOnly(true);
         */

        hasFormat.setLabel("Fixity");
        hasFormat
                .setDescription("A data property specifying the format of a File");
        hasFormat.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE + "hasFormat"));
        hasFormat.setPropertyValueType(PropertyValueType.STRING);

        hasContact.setLabel("Contact Info");
        hasContact
                .setDescription("A data property specifying a contact for the Collection or DataItem");
        hasContact.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasContact"));
        hasContact.setPropertyValueType(PropertyValueType.COMPLEX);
        hasContact.setPropertyValueHint(PropertyValueHint.CONTACT_INFO);

        hasCreator.setLabel("Contact Info");
        hasCreator
                .setDescription("A data property specifying a creator for the Collection or DataItem");
        hasCreator.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasCreator"));
        hasCreator.setPropertyValueType(PropertyValueType.COMPLEX);
        hasCreator.setPropertyValueHint(PropertyValueHint.CONTACT_INFO);

        hasPublisher.setLabel("Publisher");
        hasPublisher
                .setDescription("A data property specifying a publisher for a Project");
        hasPublisher.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasPublisher"));

        hasAlottedStorage.setLabel("Alotted Storage");
        hasAlottedStorage
                .setDescription("A data property specifying alotted storage of a Project");
        hasAlottedStorage.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasAlottedStorage"));
        hasAlottedStorage.setPropertyValueType(PropertyValueType.LONG);
        hasAlottedStorage.setPropertyValueHint(PropertyValueHint.FILE_SIZE);

        hasUsedStorage.setLabel("Used Storage");
        hasUsedStorage
                .setDescription("A data property specifying Used storage of a Project");
        hasUsedStorage.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasUsedStorage"));
        hasUsedStorage.setPropertyValueType(PropertyValueType.LONG);
        hasUsedStorage.setPropertyValueHint(PropertyValueHint.FILE_SIZE);

        hasStartDate.setLabel("Start Date");
        hasStartDate
                .setDescription("A data property specifying the Start date for a Project");
        hasStartDate.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasStartDate"));
        hasStartDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasEndDate.setLabel("End Date");
        hasEndDate
                .setDescription("A data property specifying the End date for a Project");
        hasEndDate.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasEndDate"));
        hasEndDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasFundingEntity.setLabel("Funding Entity");
        hasFundingEntity
                .setDescription("A data property specifying a FundingEntity for a Project");
        hasFundingEntity.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasFundingEntity"));
        hasFundingEntity.setPropertyValueType(PropertyValueType.STRING);

        hasAwardNumber.setLabel("Award number");
        hasAwardNumber
                .setDescription("A data property specifying a Award Number for a Project");
        hasAwardNumber.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasAwardNumber"));
        hasAwardNumber.setPropertyValueType(PropertyValueType.STRING);

        hasPrincipalInvestigator.setLabel("Principal Investigator");
        hasPrincipalInvestigator
                .setDescription("A data property specifying Principal Investigators for a Project");
        hasPrincipalInvestigator.setDomainPredicate(URI.create(BO_ONTOLOGY_BASE
                + "hasPrincipalInvestigator"));
        hasPrincipalInvestigator.setPropertyValueType(PropertyValueType.STRING);

        hasPublisher.setPropertyValueType(PropertyValueType.STRING);
        name.setLabel("Name");
        name.setDescription("Person name");
        name.setDomainPredicate(URI.create(FOAF_BASE + "name"));
        name.setPropertyValueType(PropertyValueType.STRING);
        name.setPropertyValueHint(PropertyValueHint.PERSON_NAME);

        phone.setLabel("Phone");
        phone.setDescription("Phone number");
        phone.setDomainPredicate(URI.create(FOAF_BASE + "phone"));
        phone.setPropertyValueType(PropertyValueType.STRING);
        phone.setPropertyValueHint(PropertyValueHint.PHONE_NUMBER);

        mbox.setLabel("Eail");
        mbox.setDescription("E-mail address");
        mbox.setDomainPredicate(URI.create(FOAF_BASE + "mbox"));
        mbox.setPropertyValueType(PropertyValueType.STRING);
        mbox.setPropertyValueHint(PropertyValueHint.EMAIL);

        homepage.setLabel("Web Page");
        homepage.setDescription("Web Page");
        homepage.setDomainPredicate(URI.create(FOAF_BASE + "homepage"));
        // TODO:  resource properties
        homepage.setPropertyValueHint(PropertyValueHint.URL);

    }

    private void setPropertyConstraints() {

        /* First for all the domain objects */

        project.setPropertyConstraints(Arrays
                .asList(exactlyOne(hasTitle),
                        exactlyOne(hasDescription),
                        exactlyOne(hasStartDate),
                        atMostOne(hasEndDate),
                        exactlyOne(hasAlottedStorage),
                        exactlyOne(hasUsedStorage),
                        exactlyOne(hasPublisher),
                        atMostOne(hasFundingEntity),
                        zeroOrMore(hasAwardNumber),
                        zeroOrMore(hasPrincipalInvestigator)));

        collection.setPropertyConstraints(Arrays
                .asList(exactlyOne(hasTitle),
                        exactlyOne(hasDescription),
                        exactlyOne(hasCreateDate),
                        // No hasModifiedDate for collections in ontology, but PTG has it
                        zeroOrMore(hasCitableLocator),
                        zeroOrMore(hasContact),
                        zeroOrMore(hasCreator),
                        zeroOrMore(hasDiscipline),
                        // No hasPublisher for collections in ontology, but PTG has it
                        atMostOne(hasPublicationDate)));

        dataItem.setPropertyConstraints(Arrays
                .asList(exactlyOne(hasTitle),
                        exactlyOne(hasDescription),
                        exactlyOne(hasCreateDate),
                        exactlyOne(hasModifiedDate),
                        // Intentionally not including hasDepositDate
                        // Intentionally not including hasDepositor
                        zeroOrMore(hasCitableLocator),
                        zeroOrMore(hasContact),
                        zeroOrMore(hasCreator),
                        zeroOrMore(hasContentModel)));

        file.setPropertyConstraints(Arrays.asList(exactlyOne(hasTitle),
                                                  exactlyOne(hasDescription),
                                                  exactlyOne(hasCreateDate),
                                                  exactlyOne(hasModifiedDate),
                                                  exactlyOne(hasFormat),
                                                  exactlyOne(hasSize)));

        /* Now we do "complex properties */

        /* XXX No properties defined in ontology */
        hasContact.setPropertySubTypes(Arrays.asList(exactlyOne(name),
                                                     zeroOrMore(phone),
                                                     zeroOrMore(mbox)));

        /* XXX No properties defined in the ontology */
        hasCreator.setPropertySubTypes(Arrays.asList(exactlyOne(name),
                                                     zeroOrMore(phone),
                                                     zeroOrMore(homepage),
                                                     zeroOrMore(mbox)));
    }

    private void setRelationshipConstraints() {

        collection.setParentConstraints(Arrays
                .asList(allowRelationshipTo(collection, memberRel),
                        allowRelationshipTo(project, memberRel)));

        dataItem.setParentConstraints(Arrays
                .asList(allowRelationshipTo(collection, memberRel)));

        file.setParentConstraints(Arrays.asList(allowRelationshipTo(dataItem,
                                                                    memberRel),
                                                allowAll(metadataRel)));
    }

    private void setFileAssociations() {

        project.setFileAssociation(FileAssociation.DIRECTORY);
        collection.setFileAssociation(FileAssociation.DIRECTORY);
        dataItem.setFileAssociation(FileAssociation.DIRECTORY);
        file.setFileAssociation(FileAssociation.REGULAR_FILE);

    }

    private void defineNodeTransforms() {

        /*
         * Collections can be transformed to projects only if they have no
         * parent
         */
        collection_to_project.setLabel("Collection to Project");
        collection_to_project
                .setDescription("Transform a Collection to a Project");
        collection_to_project.setInsertParent(false);
        collection_to_project.setRemoveEmptyParent(false);
        collection_to_project.setSourceNodeType(collection);
        collection_to_project
                .setSourceParentConstraint(disallowRelationshipTo(collection,
                                                                  memberRel));
        collection_to_project.setResultNodeType(project);

        /* Projects can always be transformed to collections */
        project_to_collection.setLabel("Project to Collection");
        project_to_collection
                .setDescription("Transforms a project into a collection");
        project_to_collection.setInsertParent(false);
        project_to_collection.setRemoveEmptyParent(false);
        project_to_collection.setSourceNodeType(project);
        project_to_collection.setResultNodeType(collection);

        /*
         * Collection can be transformed to a DataItem if it doesn't contain any
         * child collections, and isn't a direct member of a project.
         */
        collection_to_dataItem.setLabel("Collection to DataItem");
        collection_to_dataItem
                .setDescription("Transforms a Collection into a DataItem");
        collection_to_dataItem.setInsertParent(false);
        collection_to_dataItem.setRemoveEmptyParent(false);
        collection_to_dataItem.setSourceNodeType(collection);
        collection_to_dataItem
                .setSourceParentConstraint(disallowRelationshipTo(project,
                                                                  memberRel));
        collection_to_dataItem
                .setSourceChildConstraint(disallowRelationshipTo(collection,
                                                                 memberRel));

        /* DataItem can always be changed to Collection */
        dataItem_to_collection.setLabel("DataItem to Collection");
        dataItem_to_collection
                .setDescription("Transforms a DataItem into a Collection");
        dataItem_to_collection.setInsertParent(false);
        dataItem_to_collection.setRemoveEmptyParent(false);

        /* MetadataFile can be changed to a DataFile */
        metadata_to_file.setLabel("MetadataFile to DataFile");
        metadata_to_file.setDescription("MetadataFile to DataFile");
        metadata_to_file.setInsertParent(false);
        metadata_to_file.setRemoveEmptyParent(false);
        metadata_to_file.setSourceNodeType(file);
        metadata_to_file.setSourceParentConstraint(allowAll(metadataRel));
        metadata_to_file.setResultNodeType(file);
        metadata_to_file
                .setResultParentConstraint(allowRelationshipTo(dataItem,
                                                               memberRel));

        /* DataFile can be changed to MetadataFile */
        file_to_metadata.setLabel("DataFile to MetadataFile");
        file_to_metadata.setDescription("DataFile to MetadataFile");
        file_to_metadata.setInsertParent(false);
        file_to_metadata.setRemoveEmptyParent(false);
        file_to_metadata.setSourceNodeType(file);
        file_to_metadata
                .setSourceChildConstraint(allowRelationshipTo(dataItem,
                                                              memberRel));
        file_to_metadata.setResultNodeType(file);
        file_to_metadata.setResultParentConstraint(allowAll(metadataRel));

    }

    private void setSuppliedValues() {
        Map<PropertyType, SuppliedProperty> supplied = new HashMap<>();

        supplied.put(hasCreateDate, SuppliedProperty.FILE_CREATED_DATE);
        supplied.put(hasModifiedDate, SuppliedProperty.FILE_MODIFIED_DATE);
        supplied.put(hasTitle, SuppliedProperty.FILE_NAME);
        supplied.put(hasSize, SuppliedProperty.FILE_SIZE);
        supplied.put(hasFormat, SuppliedProperty.FILE_FORMAT_URI);

        project.setSuppliedProperties(supplied);
        collection.setSuppliedProperties(supplied);
        dataItem.setSuppliedProperties(supplied);
        file.setSuppliedProperties(supplied);
    }

    private static PropertyConstraint exactlyOne(PropertyType prop) {
        PropertyConstraint constraint = new PropertyConstraint();
        constraint.setPropertyType(prop);
        constraint.setMin(1);
        constraint.setMax(1);
        return constraint;
    }

    private static PropertyConstraint atMostOne(PropertyType prop) {
        PropertyConstraint constraint = new PropertyConstraint();
        constraint.setPropertyType(prop);
        constraint.setMin(0);
        constraint.setMax(1);
        return constraint;
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

    private static NodeConstraint disallowRelationshipTo(NodeType parentType,
                                                         StructuralRelation rel) {
        NodeConstraint constraint = new NodeConstraint();
        constraint.setNodeType(parentType);
        constraint.setStructuralRelation(rel);
        constraint.setMatchesNone(true);
        return constraint;
    }

    private static NodeConstraint allowAll(StructuralRelation rel) {
        NodeConstraint constraint = new NodeConstraint();
        constraint.setMatchesAny(true);
        constraint.setStructuralRelation(rel);
        return constraint;
    }

    /**
     * Serialize the profile to a file.
     * 
     * @param args
     *        Expects two arguments args[0] is the name of a file, and args[1]
     *        is a RIOT name for a serialization format (an {@link RDFFormat},
     *        e.g. TURTLE_PRETTY).
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        DomainProfileRdfTransformService xform =
                new DomainProfileRdfTransformService();

        Model model = xform.transformToRdf(new DcsBOProfile(), "");

        try (FileOutputStream out = new FileOutputStream(args[0])) {

            RDFDataMgr.createGraphWriter((RDFFormat) RDFFormat.class
                    .getDeclaredField(args[1]).get(null))
                    .write(out, model.getGraph(), PREFIX_MAP, null, null);

        }
    }

}
