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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

import org.dataconservancy.packaging.tool.impl.DomainProfileRdfTransformService;
import org.dataconservancy.packaging.tool.model.dprofile.CardinalityConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.FileAssociation;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyCategory;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.dprofile.SuppliedProperty;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;

import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_DCS_PKGTOOL_PROFILE_BOM;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_DCS_ONTOLOGY_BOM;
import static org.dataconservancy.packaging.tool.ontologies.Ontologies.NS_FOAF;

/** Default Data Conservancy business object profile */
public class DcsBOProfile
        extends DomainProfile {

    private static final String BO_PROFILE_ID =
            "http://dataconservancy.org/ptg-profiles/dcs-bo-1.0";

    /* Business objects */

    private final NodeType project = new NodeType();

    private final NodeType collection = new NodeType();

    private final NodeType dataItem = new NodeType();

    private final NodeType file = new NodeType();

    private final NodeType person = new NodeType();

    private final NodeType metadata = new NodeType();

    /* Transforms */
    private final NodeTransform collection_to_project = new NodeTransform();

    private final NodeTransform project_to_collection = new NodeTransform();

    private final NodeTransform dataItem_to_collection = new NodeTransform();

    private final NodeTransform collection_to_dataItem = new NodeTransform();

    private final NodeTransform metadata_to_file = new NodeTransform();

    private final NodeTransform file_to_metadata = new NodeTransform();

    private final NodeTransform collectionMetadataFileToDataFile = new NodeTransform();

    private final NodeTransform dataFileToCollectionMetadataFile = new NodeTransform();

    private final NodeTransform rootCollectionToDataItem = new NodeTransform();

    private final NodeTransform rootDataItemToProject = new NodeTransform();

    private final NodeTransform projectToDataItem = new NodeTransform();

    /* Properties */

    private final StructuralRelation metadataRel =
            new StructuralRelation(URI.create(NS_DCS_ONTOLOGY_BOM
                    + "metadataFor"), URI.create(NS_DCS_ONTOLOGY_BOM
                    + "hasMetadata"));

    private final StructuralRelation memberRel =
            new StructuralRelation(URI.create(NS_DCS_ONTOLOGY_BOM
                    + "isMemberOf"), URI.create(NS_DCS_ONTOLOGY_BOM
                    + "hasMember"));

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

    private final PropertyCategory creatorCategory = new PropertyCategory();

    private static final PrefixMap PREFIX_MAP = PrefixMapFactory
            .create(Ontologies.PREFIX_MAP);

    public DcsBOProfile() {

        /* Prepare data to go into the profile */

        defineNodeTypes();

        defineNodeTransforms();

        definePropertyTypes();

        setPropertyConstraints();

        setRelationshipConstraints();

        setFileAssociations();

        setSuppliedValues();

        /* set the label to display in the GUI Proflie picker */
        setLabel("DCS Business Object Profile");

        /* Now, populate the profile with our profile data */
        setDomainIdentifier(URI.create("http://example.org/myDomainIdentifier"));

        setIdentifier(URI.create(BO_PROFILE_ID));

        setNodeTypes(Arrays.asList(project,
                                   collection,
                                   dataItem,
                                   file,
                                   metadata));

        setNodeTransforms(Arrays.asList(project_to_collection,
                                        collection_to_project,
                                        collection_to_dataItem,
                                        dataItem_to_collection,
                                        metadata_to_file,
                                        file_to_metadata,
                                        collectionMetadataFileToDataFile,
                                        dataFileToCollectionMetadataFile,
                                        rootCollectionToDataItem,
                                        rootDataItemToProject,
                                        projectToDataItem));

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

        setPropertyCategories(Collections.singletonList(creatorCategory));
    }

    private void defineNodeTypes() {
        project.setIdentifier(URI
                .create(NS_DCS_PKGTOOL_PROFILE_BOM + "Project"));
        project.setLabel("Project");
        project.setDescription("Project business object");
        project.setDomainTypes(Collections.singletonList(URI.create(
            NS_DCS_ONTOLOGY_BOM + "Project")));
        project.setDomainProfile(this);
        project.setInheritableProperties(Arrays.asList(hasDescription, hasPublisher));

        collection.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_BOM
                + "Collection"));
        collection.setLabel("Collection");
        collection.setDescription("Collection business object");
        collection.setDomainTypes(Collections.singletonList(URI.create(
            NS_DCS_ONTOLOGY_BOM + "Collection")));
        collection.setDomainProfile(this);
        collection.setInheritableProperties(Arrays.asList(hasDescription, hasDiscipline, hasCreator, hasContact, hasPublisher, hasPublicationDate));
        //This sets the preference of collection assignment to a single file, this is to mirror the existing package tool behavior
        //Note this is just a preference the type can still be changed by the user.
        CardinalityConstraint collectionFilePreference = new CardinalityConstraint();
        collectionFilePreference.setMin(0);
        collectionFilePreference.setMax(1);
        collection.setChildFileConstraint(collectionFilePreference);

        dataItem.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_BOM
                + "DataItem"));
        dataItem.setLabel("DataItem");
        dataItem.setDescription("DataItem business object");
        dataItem.setDomainTypes(Collections.singletonList(URI.create(
            NS_DCS_ONTOLOGY_BOM + "DataItem")));
        dataItem.setDomainProfile(this);
        //This sets the preference of data item assignment to nodes with more than 1 file, this is to mirror the existing package tool behavior
        //Note this is just a preference the type can still be changed by the user.
        CardinalityConstraint dataItemPreferences = new CardinalityConstraint();
        dataItemPreferences.setMin(2);
        dataItemPreferences.setMax(-1);
        dataItem.setChildFileConstraint(dataItemPreferences);
        dataItem.setInheritableProperties(Collections.singletonList(hasDescription));

        file.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_BOM + "File"));
        file.setLabel("DataFile");
        file.setDescription("File business object");
        file.setDomainTypes(Collections.singletonList(URI.create(
            NS_DCS_ONTOLOGY_BOM + "File")));
        file.setDomainProfile(this);
        file.setPreferredParentType(dataItem);

        metadata.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_BOM + "Metadata"));
        metadata.setLabel("Metadata File");
        metadata.setDescription("File business object representing metadata");
        metadata.setDomainTypes(Collections.singletonList(URI.create(
            NS_DCS_ONTOLOGY_BOM + "Metadata")));
        metadata.setDomainProfile(this);

        person.setIdentifier(URI.create(NS_DCS_PKGTOOL_PROFILE_BOM + "Person"));
        person.setLabel("Person");
        person.setDescription("Person business object");
        person.setDomainTypes(Collections.singletonList(URI.create(
            NS_DCS_ONTOLOGY_BOM + "Person")));
        person.setDomainProfile(this);

    }

    private void definePropertyTypes() {

        creatorCategory.setLabel("Creators");
        creatorCategory.setDescription("The creators of a domain object.");

        hasBusinessID.setLabel("Business ID");
        hasBusinessID
                .setDescription("A data property specifying a business identifier for the BusinessObject");
        hasBusinessID.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasBusinessID"));
        hasBusinessID.setPropertyValueType(PropertyValueType.LONG);

        hasAlternateId.setLabel("Alternate ID");
        hasAlternateId
                .setDescription("A data property specifying an alternate identifier for the BusinessObject");
        hasAlternateId.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasAlternateId"));
        hasAlternateId.setPropertyValueType(PropertyValueType.STRING);

        hasTitle.setLabel("Title");
        hasTitle.setDescription("A data property specifying a title for a Project, Collection, DataItem or File");
        hasTitle.setDomainPredicate(URI
                .create(NS_DCS_ONTOLOGY_BOM + "hasTitle"));
        hasTitle.setPropertyValueType(PropertyValueType.STRING);

        hasDescription.setLabel("Description");
        hasDescription
                .setDescription("A data property specifying a description for a Project, Collection, DataItem or File");
        hasDescription.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasDescription"));
        hasDescription.setPropertyValueType(PropertyValueType.STRING);
        hasDescription.setPropertyValueHint(PropertyValueHint.MULTI_LINE_TEXT);

        hasCitableLocator.setLabel("Citable Locator");
        hasCitableLocator
                .setDescription("A data property specifying a citable locator for the Collection or DataItem.");
        hasCitableLocator.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasCitableLocator"));
        hasCitableLocator.setPropertyValueType(PropertyValueType.STRING);

        hasCreateDate.setLabel("Create Date");
        hasCreateDate
                .setDescription("A data property specifying the create date for a Collection, DataItem or File");
        hasCreateDate.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasCreateDate"));
        hasCreateDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasModifiedDate.setLabel("Modified Date");
        hasModifiedDate
                .setDescription("A data property specifying the modified date for a Collection, DataItem or File");
        hasModifiedDate.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasModifiedDate"));
        hasModifiedDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasDepositDate.setLabel("Deposit Date");
        hasDepositDate
                .setDescription("A data property specifying the deposit date for a Collection, DataItem or File");
        hasDepositDate.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasDepositDate"));
        hasDepositDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasPublicationDate.setLabel("Publication Date");
        hasPublicationDate
                .setDescription("A data property specifying the publication date for a Collection, DataItem or File");
        hasPublicationDate.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasPublicationDate"));
        hasPublicationDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasDiscipline.setLabel("Discipline");
        hasDiscipline
                .setDescription("A data property specifying a discipline for a Collection");
        hasDiscipline.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasDiscipline"));
        hasDiscipline.setPropertyValueType(PropertyValueType.STRING);
        hasDiscipline.setPropertyValueHint(PropertyValueHint.DCS_DISCIPLINE);

        hasContentModel.setLabel("Content Model");
        hasContentModel
                .setDescription("A data property specifying the content model for a DataItem.");
        hasContentModel.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasContentModel"));
        hasContentModel.setPropertyValueType(PropertyValueType.STRING);

        hasSize.setLabel("File size");
        hasSize.setDescription("A data property specifying the size of a File");
        hasSize.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM + "hasSize"));
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

        hasFormat.setLabel("Format");
        hasFormat
                .setDescription("A data property specifying the format of a File");
        hasFormat.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasFormat"));
        hasFormat.setPropertyValueType(PropertyValueType.STRING);

        hasContact.setLabel("Contact Info");
        hasContact
                .setDescription("A data property specifying a contact for the Collection or DataItem");
        hasContact.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasContact"));
        hasContact.setPropertyValueType(PropertyValueType.COMPLEX);
        hasContact.setPropertyValueHint(PropertyValueHint.CONTACT_INFO);

        hasCreator.setLabel("Creator");
        hasCreator
                .setDescription("A data property specifying a creator for the Collection or DataItem");
        hasCreator.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasCreator"));
        hasCreator.setPropertyValueType(PropertyValueType.COMPLEX);
        hasCreator.setPropertyValueHint(PropertyValueHint.CONTACT_INFO);
        hasCreator.setCategory(creatorCategory);

        hasPublisher.setLabel("Publisher");
        hasPublisher
                .setDescription("A data property specifying a publisher for a Project");
        hasPublisher.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasPublisher"));
        hasPublisher.setPropertyValueType(PropertyValueType.STRING);

        hasAlottedStorage.setLabel("Alotted Storage");
        hasAlottedStorage
                .setDescription("A data property specifying alotted storage of a Project");
        hasAlottedStorage.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasAlottedStorage"));
        hasAlottedStorage.setPropertyValueType(PropertyValueType.LONG);

        hasUsedStorage.setLabel("Used Storage");
        hasUsedStorage
                .setDescription("A data property specifying Used storage of a Project");
        hasUsedStorage.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasUsedStorage"));
        hasUsedStorage.setPropertyValueType(PropertyValueType.LONG);

        hasStartDate.setLabel("Start Date");
        hasStartDate
                .setDescription("A data property specifying the Start date for a Project");
        hasStartDate.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasStartDate"));
        hasStartDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasEndDate.setLabel("End Date");
        hasEndDate
                .setDescription("A data property specifying the End date for a Project");
        hasEndDate.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasEndDate"));
        hasEndDate.setPropertyValueType(PropertyValueType.DATE_TIME);

        hasFundingEntity.setLabel("Funding Entity");
        hasFundingEntity
                .setDescription("A data property specifying a FundingEntity for a Project");
        hasFundingEntity.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasFundingEntity"));
        hasFundingEntity.setPropertyValueType(PropertyValueType.STRING);

        hasAwardNumber.setLabel("Award number");
        hasAwardNumber
                .setDescription("A data property specifying a Award Number for a Project");
        hasAwardNumber.setDomainPredicate(URI.create(NS_DCS_ONTOLOGY_BOM
                + "hasAwardNumber"));
        hasAwardNumber.setPropertyValueType(PropertyValueType.STRING);

        hasPrincipalInvestigator.setLabel("Principal Investigator");
        hasPrincipalInvestigator
                .setDescription("A data property specifying Principal Investigators for a Project");
        hasPrincipalInvestigator.setDomainPredicate(URI
                .create(NS_DCS_ONTOLOGY_BOM + "hasPrincipalInvestigator"));
        hasPrincipalInvestigator.setPropertyValueType(PropertyValueType.STRING);

        name.setLabel("Name");
        name.setDescription("Person name");
        name.setDomainPredicate(URI.create(NS_FOAF + "name"));
        name.setPropertyValueType(PropertyValueType.STRING);
        name.setPropertyValueHint(PropertyValueHint.PERSON_NAME);

        phone.setLabel("Phone");
        phone.setDescription("Phone number");
        phone.setDomainPredicate(URI.create(NS_FOAF + "phone"));
        phone.setPropertyValueType(PropertyValueType.STRING);
        phone.setPropertyValueHint(PropertyValueHint.PHONE_NUMBER);

        mbox.setLabel("Email");
        mbox.setDescription("E-mail address");
        mbox.setDomainPredicate(URI.create(NS_FOAF + "mbox"));
        mbox.setPropertyValueType(PropertyValueType.STRING);
        mbox.setPropertyValueHint(PropertyValueHint.EMAIL);

        homepage.setLabel("Web Page");
        homepage.setDescription("Web Page");
        homepage.setDomainPredicate(URI.create(NS_FOAF + "homepage"));
        homepage.setPropertyValueType(PropertyValueType.STRING);
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
                                                  atLeastOne(hasFormat),
                                                  exactlyOne(hasSize)));

        metadata.setPropertyConstraints(Arrays
                .asList(exactlyOne(hasTitle),
                        exactlyOne(hasDescription),
                        exactlyOne(hasCreateDate),
                        exactlyOne(hasModifiedDate),
                        atLeastOne(hasFormat),
                        exactlyOne(hasSize)));

        /* Now we do "complex properties */

        /* XXX No properties defined in ontology */

        hasContact.setComplexDomainTypes(Collections.singletonList(URI.create(
            NS_FOAF + "person")));
        hasContact.setComplexPropertyConstraints(Arrays
                .asList(exactlyOne(name), zeroOrMore(phone), zeroOrMore(mbox)));

        /* XXX No properties defined in the ontology */
        hasCreator.setComplexDomainTypes(Collections.singletonList(URI.create(
            NS_FOAF + "person")));
        hasCreator.setComplexPropertyConstraints(Arrays
                .asList(exactlyOne(name),
                        zeroOrMore(phone),
                        zeroOrMore(homepage),
                        zeroOrMore(mbox)));
    }

    private void setRelationshipConstraints() {

        project.setParentConstraints(Collections
                .singletonList(noNodeConstraint()));

        collection.setParentConstraints(Arrays
                .asList(noNodeConstraint(),
                        allowRelationshipTo(collection, memberRel),
                        allowRelationshipTo(project, memberRel)));

        dataItem.setParentConstraints(Arrays
                .asList(noNodeConstraint(),
                        allowRelationshipTo(collection, memberRel)));

        file.setParentConstraints(Collections
                .singletonList(allowRelationshipTo(dataItem, memberRel)));

        metadata.setParentConstraints(Collections
                .singletonList(allowAll(metadataRel)));
    }

    private void setFileAssociations() {
        project.setFileAssociation(FileAssociation.DIRECTORY);
        collection.setFileAssociation(FileAssociation.DIRECTORY);
        dataItem.setFileAssociation(FileAssociation.DIRECTORY);
        file.setFileAssociation(FileAssociation.REGULAR_FILE);
        metadata.setFileAssociation(FileAssociation.REGULAR_FILE);
    }

    private void defineNodeTransforms() {

        /*
         * Collections can be transformed to projects only if they have no
         * parent
         */
        collection_to_project.setLabel("Collection to Project");
        collection_to_project
                .setDescription("Transform a Collection to a Project");
        collection_to_project.setSourceNodeType(collection);
        collection_to_project
                .setSourceParentConstraint(noNodeConstraint());
        collection_to_project.setSourceChildConstraints(Arrays.asList(allowRelationshipTo(collection, memberRel), allowRelationshipTo(dataItem, memberRel),
                                                                      allowRelationshipTo(metadata, metadataRel), noNodeConstraint()));
        collection_to_project.setResultNodeType(project);
        collection_to_project.setResultChildTransforms(Collections.singletonList(dataItem_to_collection));

        /* Projects can always be transformed to collections */
        project_to_collection.setLabel("Project to Collection");
        project_to_collection
                .setDescription("Transforms a project into a collection");
        project_to_collection.setSourceNodeType(project);
        project_to_collection.setResultNodeType(collection);

        /*
         * Collection can be transformed to a DataItem if it doesn't contain any
         * child collections, and isn't a direct member of a project.
         */
        collection_to_dataItem.setLabel("Collection to DataItem");
        collection_to_dataItem
                .setDescription("Transforms a Collection into a DataItem");
        collection_to_dataItem.setSourceNodeType(collection);
        collection_to_dataItem
                .setSourceParentConstraint(allowRelationshipTo(collection,
                                                               memberRel));
        collection_to_dataItem
                .setSourceChildConstraints(Arrays.asList(allowRelationshipTo(metadata,
                                                              metadataRel), noNodeConstraint()));
        collection_to_dataItem.setResultNodeType(dataItem);

        /*
         * Collection can be transformed to a DataItem if it doesn't contain any
         * child collections, and it is root
         */
        rootCollectionToDataItem.setLabel("Collection to DataItem");
        rootCollectionToDataItem
                .setDescription("Transforms a Collection into a DataItem");
        rootCollectionToDataItem.setSourceNodeType(collection);
        rootCollectionToDataItem
                .setSourceParentConstraint(noNodeConstraint());
        rootCollectionToDataItem
                .setSourceChildConstraints(Arrays.asList(allowRelationshipTo(metadata,
                                                              metadataRel), noNodeConstraint()));
        rootCollectionToDataItem.setResultNodeType(dataItem);

        /*
         * Root data items can be converted to projects
         */
        rootDataItemToProject.setLabel("DataItem to Project");
        rootDataItemToProject.setDescription("Transforms a DataItem into a Project");
        rootDataItemToProject.setSourceNodeType(dataItem);
        rootDataItemToProject.setResultNodeType(project);
        rootDataItemToProject.setSourceParentConstraint(noNodeConstraint());
        rootDataItemToProject.setResultChildTransforms(Collections.singletonList(file_to_metadata));

        projectToDataItem.setLabel("Project to DataItem");
        projectToDataItem.setDescription("Transforms a project into a Data Item");
        projectToDataItem.setSourceNodeType(project);
        projectToDataItem.setResultNodeType(dataItem);
        projectToDataItem.setSourceChildConstraints(Arrays.asList(allowRelationshipTo(metadata, metadataRel), noNodeConstraint()));

        /* DataItem can always be changed to Collection */
        dataItem_to_collection.setLabel("DataItem to Collection");
        dataItem_to_collection
                .setDescription("Transforms a DataItem into a Collection");
        dataItem_to_collection.setSourceNodeType(dataItem);
        dataItem_to_collection.setResultNodeType(collection);
        dataItem_to_collection.setResultChildTransforms(Collections.singletonList(file_to_metadata));

        /* MetadataFile can be changed to a DataFile */
        metadata_to_file.setLabel("MetadataFile to DataFile");
        metadata_to_file.setDescription("MetadataFile to DataFile");
        metadata_to_file.setSourceNodeType(metadata);
        metadata_to_file.setResultNodeType(file);
        metadata_to_file.setSourceParentConstraint(allowRelationshipTo(dataItem, metadataRel));

        /* DataFile can be changed to MetadataFile */
        file_to_metadata.setLabel("DataFile to MetadataFile");
        file_to_metadata.setDescription("DataFile to MetadataFile");
        file_to_metadata.setSourceNodeType(file);
        file_to_metadata.setResultNodeType(metadata);

        /* Collection metadata file becomes a data file with a data item inserted as the parent */
        collectionMetadataFileToDataFile.setLabel("MetadataFile to DataFile");
        collectionMetadataFileToDataFile.setDescription("MetadataFile to DataFile, inserting a data item.");
        collectionMetadataFileToDataFile.setSourceNodeType(metadata);
        collectionMetadataFileToDataFile.setSourceParentConstraint(allowRelationshipTo(collection, metadataRel));
        collectionMetadataFileToDataFile.setInsertParentNodeType(dataItem);
        collectionMetadataFileToDataFile.setResultNodeType(file);

        /* DataFile becomes a metadata file about the collection */
        dataFileToCollectionMetadataFile.setLabel("DataFile to Collection Metadata");
        dataFileToCollectionMetadataFile.setDescription("DataFile to Collection Metadata");
        //This transform operates on the data item and moves it's children to parent.
        //So the data item remains unchanged unless empty then it's deleted.
        dataFileToCollectionMetadataFile.setSourceNodeType(dataItem);
        dataFileToCollectionMetadataFile.setResultNodeType(dataItem);
        //Data Item can't be root
        dataFileToCollectionMetadataFile.setSourceParentConstraint(allowRelationshipTo(collection, memberRel));
        dataFileToCollectionMetadataFile.setSourceChildConstraints(Arrays.asList(allowRelationshipTo(file, memberRel), allowRelationshipTo(metadata, metadataRel)));
        dataFileToCollectionMetadataFile.setMoveChildrenToParent(true);
        dataFileToCollectionMetadataFile.setRemoveEmptyResult(true);
        dataFileToCollectionMetadataFile.setResultChildTransforms(Collections.singletonList(file_to_metadata));
    }

    private void setSuppliedValues() {
        // File and  metadata
        
        {
            Map<PropertyType, SuppliedProperty> supplied = new HashMap<>();
        
            supplied.put(hasSize, SuppliedProperty.FILE_SIZE);
            supplied.put(hasCreateDate, SuppliedProperty.FILE_CREATED_DATE);
            supplied.put(hasModifiedDate, SuppliedProperty.FILE_MODIFIED_DATE);        
            supplied.put(hasFormat, SuppliedProperty.FILE_FORMAT_URI);
            supplied.put(hasTitle, SuppliedProperty.FILE_NAME);
            
            file.setSuppliedProperties(supplied);
            metadata.setSuppliedProperties(supplied);
        }
        
        // Collection
        
        {
            Map<PropertyType, SuppliedProperty> supplied = new HashMap<>();
        
            supplied.put(hasCreateDate, SuppliedProperty.FILE_CREATED_DATE);
            supplied.put(hasTitle, SuppliedProperty.FILE_NAME);
            
            collection.setSuppliedProperties(supplied);            
        }
        
        // Data item
        
        {
            Map<PropertyType, SuppliedProperty> supplied = new HashMap<>();
        
            supplied.put(hasCreateDate, SuppliedProperty.FILE_CREATED_DATE);
            supplied.put(hasModifiedDate, SuppliedProperty.FILE_MODIFIED_DATE);        
            supplied.put(hasTitle, SuppliedProperty.FILE_NAME);
            
            dataItem.setSuppliedProperties(supplied);
        }
        
        // Project
        
        {
            Map<PropertyType, SuppliedProperty> supplied = new HashMap<>();
        
            supplied.put(hasTitle, SuppliedProperty.FILE_NAME);
            
            project.setSuppliedProperties(supplied);
        }
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

    private static PropertyConstraint atLeastOne(PropertyType prop) {
        PropertyConstraint constraint = new PropertyConstraint();
        constraint.setPropertyType(prop);
        constraint.setMin(1);
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

    private static NodeConstraint allowAll(StructuralRelation rel) {
        NodeConstraint constraint = new NodeConstraint();
        constraint.setMatchesAny(true);
        constraint.setStructuralRelation(rel);
        return constraint;
    }

    private static NodeConstraint noNodeConstraint() {
        NodeConstraint noParentConstraint = new NodeConstraint();
        noParentConstraint.setMatchesNone(true);

        return noParentConstraint;
    }

    public NodeType getProjectNodeType() {
        return project;
    }

    public NodeType getCollectionNodeType() {
        return collection;
    }

    public NodeType getDataItemNodeType() {
        return dataItem;
    }

    public NodeType getFileNodeType() {
        return file;
    }

    public NodeType getMetadataNodeType() {
        return metadata;
    }

    public NodeTransform getCollectionToProjectTransform() {
        return collection_to_project;
    }

    public NodeTransform getProjectToCollectionTransform() {
        return project_to_collection;
    }

    public NodeTransform getDataItemToCollectionTransform() {
        return dataItem_to_collection;
    }

    public NodeTransform getCollectionToDataItemTransform() {
        return collection_to_dataItem;
    }

    public NodeTransform getMetadataToFileTransform() {
        return metadata_to_file;
    }

    public NodeTransform getFileToMetadataTransform() {
        return file_to_metadata;
    }

    public NodeTransform getCollectionMetadataFileToDataFileTransform() {
        return collectionMetadataFileToDataFile;
    }

    public NodeTransform getDataFileToCollectionMetadataFileTransform() {
        return dataFileToCollectionMetadataFile;
    }

    public NodeTransform getRootCollectionToDataItemTransform() {
        return rootCollectionToDataItem;
    }

    public NodeTransform getRootDataItemToProjectTransform() {
        return rootDataItemToProject;
    }

    public NodeTransform getProjectToDataItemTransform() {
        return projectToDataItem;
    }

    public PropertyType getHasAlottedStorage() {
        return hasAlottedStorage;
    }

    public PropertyType getHasAlternateId() {
        return hasAlternateId;
    }

    public PropertyType getHasAwardNumber() {
        return hasAwardNumber;
    }

    public PropertyType getHasBusinessID() {
        return hasBusinessID;
    }

    public PropertyType getHasCitableLocator() {
        return hasCitableLocator;
    }

    public PropertyType getHasContact() {
        return hasContact;
    }

    public PropertyType getHasContentModel() {
        return hasContentModel;
    }

    public PropertyType getHasCreateDate() {
        return hasCreateDate;
    }

    public PropertyType getHasCreator() {
        return hasCreator;
    }

    public PropertyType getHasDepositDate() {
        return hasDepositDate;
    }

    public PropertyType getHasDescription() {
        return hasDescription;
    }

    public PropertyType getHasDiscipline() {
        return hasDiscipline;
    }

    public PropertyType getHasEndDate() {
        return hasEndDate;
    }

    public PropertyType getHasFormat() {
        return hasFormat;
    }

    public PropertyType getHasFundingEntity() {
        return hasFundingEntity;
    }

    public PropertyType getHasModifiedDate() {
        return hasModifiedDate;
    }

    public PropertyType getHasPrincipalInvestigator() {
        return hasPrincipalInvestigator;
    }

    public PropertyType getHasPublicationDate() {
        return hasPublicationDate;
    }

    public PropertyType getHasPublisher() {
        return hasPublisher;
    }

    public PropertyType getHasSize() {
        return hasSize;
    }

    public PropertyType getHasStartDate() {
        return hasStartDate;
    }

    public PropertyType getHasTitle() {
        return hasTitle;
    }

    public PropertyType getHasUsedStorage() {
        return hasUsedStorage;
    }

    public PropertyType getHomepage() {
        return homepage;
    }

    public PropertyType getMbox() {
        return mbox;
    }

    public PropertyType getPhone() {
        return phone;
    }

    public PropertyType getName() {
        return name;
    }

    /**
     * Serialize the profile to a file.
     * 
     * @param args
     *        Expects two arguments args[0] is the name of a file, and args[1]
     *        is a RIOT name for a serialization format (an {@link RDFFormat},
     *        e.g. TURTLE_PRETTY).
     * @throws Exception if we can't write teh serialized profile to the file
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
