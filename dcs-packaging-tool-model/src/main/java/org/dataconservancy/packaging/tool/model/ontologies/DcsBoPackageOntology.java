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
package org.dataconservancy.packaging.tool.model.ontologies;

import org.dataconservancy.packaging.tool.model.PackageOntology;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides setup and create an instance of DcsBoPackageOntology. It is intended to use to create a serialized
 * ontology to be used with Package Tool, needs to be updated according to the most current ontology.
 */
public class DcsBoPackageOntology {

    public static final String PROJECT = "Project";
    public static final String COLLECTION = "Collection";
    public static final String DATAITEM = "DataItem";
    public static final String METADATAFILE = "MetadataFile";
    public static final String DATAFILE = "DataFile";

    public static final String HAS_MEMBER = "hasMember";
    public static final String IS_MEMBER_OF = "isMemberOf";
    public static final String HAS_METADATA = "hasMetadata";
    public static final String IS_METADATA_FOR = "isMetadataFor";


    public static final String ID = "id";
    public static final String ALTERNATE_ID = "alternateId";
    public static final String TITLE = "title";
    public static final String NAME = "name";
    public static final String DISCIPLINE = "discipline";
    public static final String DESCRIPTION = "description";
    public static final String CITABLE_LOCATOR = "citableLocator";
    public static final String CREATOR = "creator";
    public static final String CONTACT_INFO = "contactInfo";
    public static final String PUBLICATION_DATE = "publicationDate";
    public static final String CREATE_DATE = "createDate";
    public static final String MODIFIED_DATE = "modifiedDate";
    public static final String CONTENT_MODEL = "contentModel";
    public static final String FORMAT = "format";
    public static final String FUNDING_ENTITY = "fundingEntity";
    public static final String NUMBER = "number";
    public static final String PI= "pi";
    public static final String PUBLISHER= "publisher";
    public static final String START_DATE= "startDate";
    public static final String END_DATE= "endDate";
    public static final String STORAGE_ALLOCATED = "storageAllocated";
    public static final String STORAGE_USED = "storageUsed";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String PAGE = "page";
    public static final String SIZE = "size";
    public static final String STRING_TYPE = "String";
    public static final String CONTACT_INFO_TYPE = "ContactInfoType";
    public static final String CREATOR_TYPE = "CreatorType";
    public static final String DATETIME_TYPE = "DateTime";
    public static final String PHONE_NUMBER_TYPE = "PhoneNumberType";
    public static final String EMAIL_TYPE = "EmailType";
    public static final String URL_TYPE = "URLType";
    public static final String LONG_TYPE = "Long";
    public static final String PERSON_NAME = "personName";
    public static final String FILE_NAME = "fileName";
    
    public static final Set<String> relationshipsToParent = new HashSet<String>(Arrays.asList(IS_MEMBER_OF, IS_METADATA_FOR));
    public static final Set<String> relationshipsToChildren = new HashSet<String>(Arrays.asList(HAS_MEMBER, HAS_METADATA));

    public static PackageOntology getInstance() {
        PackageOntology dcsBoOntology = new PackageOntology();
        dcsBoOntology.setId("package.ontology:dcs.bo.1");


        dcsBoOntology.addArtifactType(PROJECT);
        dcsBoOntology.addArtifactProperty(PROJECT, ID, STRING_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, NAME, STRING_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, DESCRIPTION, STRING_TYPE, 1, 1, false, true);
        dcsBoOntology.addArtifactProperty(PROJECT, FUNDING_ENTITY, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, NUMBER, STRING_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, PI, STRING_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, PUBLISHER, STRING_TYPE, 1, 1, false, true);
        dcsBoOntology.addArtifactProperty(PROJECT, START_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, END_DATE, DATETIME_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, STORAGE_ALLOCATED, LONG_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(PROJECT, STORAGE_USED, LONG_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactRelationship(PROJECT, HAS_MEMBER, COLLECTION, true, 0, Integer.MAX_VALUE);

        /**
         * SETTING UP COLLECTION TYPE
         */
        dcsBoOntology.addArtifactType(COLLECTION);
        //Commenting out id since we currently don't support the user entering the id.
        //dcsBoOntology.addArtifactProperty(COLLECTION, ID, STRING_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, ALTERNATE_ID, STRING_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, TITLE, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, DESCRIPTION, STRING_TYPE, 0, 1, false, true);
        dcsBoOntology.addArtifactProperty(COLLECTION, DISCIPLINE, STRING_TYPE, 0, Integer.MAX_VALUE, false, true);
        dcsBoOntology.addArtifactProperty(COLLECTION, CITABLE_LOCATOR, STRING_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, CREATOR, CREATOR_TYPE, 0, Integer.MAX_VALUE, false, true);
        dcsBoOntology.addArtifactProperty(COLLECTION, CONTACT_INFO, CONTACT_INFO_TYPE, 0, Integer.MAX_VALUE, false, true);
        dcsBoOntology.addArtifactProperty(COLLECTION, PUBLICATION_DATE, DATETIME_TYPE, 0, 1, false, true);
        dcsBoOntology.addArtifactProperty(COLLECTION, CREATE_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, MODIFIED_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(COLLECTION, PUBLISHER, STRING_TYPE, 0, 1, false, true);
        /**
         * the following 2 relationships affects one another.
         */
        dcsBoOntology.addArtifactRelationship(COLLECTION, IS_MEMBER_OF, PROJECT, true, 0, 1);
        dcsBoOntology.addArtifactRelationship(COLLECTION, IS_MEMBER_OF, COLLECTION, true, 0, 1);

        dcsBoOntology.addArtifactRelationship(COLLECTION, HAS_MEMBER, COLLECTION, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, HAS_MEMBER, DATAITEM, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(COLLECTION, HAS_METADATA, METADATAFILE, true, 0, Integer.MAX_VALUE);

        /**
         * SETTING UP DATA ITEM TYPE
         */
        dcsBoOntology.addArtifactType(DATAITEM);
        //Commenting out the id since we currently don't support the user entering the id
        //dcsBoOntology.addArtifactProperty(DATAITEM, ID, STRING_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, NAME, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, DESCRIPTION, STRING_TYPE, 0, 1, false, true);
        dcsBoOntology.addArtifactProperty(DATAITEM, ALTERNATE_ID, STRING_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, CITABLE_LOCATOR, STRING_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, CONTENT_MODEL, STRING_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, CREATOR, CREATOR_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, CONTACT_INFO, CONTACT_INFO_TYPE, 0, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, CREATE_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAITEM, MODIFIED_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactRelationship(DATAITEM, IS_MEMBER_OF, COLLECTION, true, 1, 1);
        dcsBoOntology.addArtifactRelationship(DATAITEM, HAS_MEMBER, DATAFILE, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(DATAITEM, HAS_METADATA, METADATAFILE, true, 0, Integer.MAX_VALUE);

        /**
         * SETTING UP DATA FILE TYPE
         */
        dcsBoOntology.addArtifactType(DATAFILE);
        //Commenting out the id since we currently don't support the user entering the id
        //dcsBoOntology.addArtifactProperty(DATAFILE, ID, STRING_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAFILE, FILE_NAME, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAFILE, TITLE, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAFILE, DESCRIPTION, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAFILE, CREATE_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAFILE, MODIFIED_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(DATAFILE, FORMAT, STRING_TYPE, 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(DATAFILE, SIZE, STRING_TYPE, 0, 1, true, false);
        dcsBoOntology.addArtifactRelationship(DATAFILE, IS_MEMBER_OF, DATAITEM, true, 1, 1);
        dcsBoOntology.addArtifactRelationship(DATAFILE, HAS_METADATA, METADATAFILE, true, 0, Integer.MAX_VALUE);

        /**
         * SETTING UP METADATA FILE TYPE
         */
        dcsBoOntology.addArtifactType(METADATAFILE);
        //Commenting out the id since we currently don't support the user entering the id
        //dcsBoOntology.addArtifactProperty(METADATAFILE, ID, STRING_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(METADATAFILE, FILE_NAME, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(METADATAFILE, TITLE, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(METADATAFILE, DESCRIPTION, STRING_TYPE, 0, 1, false, false);
        dcsBoOntology.addArtifactProperty(METADATAFILE, CREATE_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(METADATAFILE, MODIFIED_DATE, DATETIME_TYPE, 1, 1, false, false);
        dcsBoOntology.addArtifactProperty(METADATAFILE, FORMAT, STRING_TYPE, 1, Integer.MAX_VALUE, false, false);
        dcsBoOntology.addArtifactProperty(METADATAFILE, SIZE, STRING_TYPE, 0, 1, true, false);
        dcsBoOntology.addArtifactRelationship(METADATAFILE, IS_METADATA_FOR, DATAITEM, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(METADATAFILE, IS_METADATA_FOR, COLLECTION, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(METADATAFILE, IS_METADATA_FOR, DATAFILE, true, 0, Integer.MAX_VALUE);
        dcsBoOntology.addArtifactRelationship(METADATAFILE, IS_METADATA_FOR, METADATAFILE, true, 0, Integer.MAX_VALUE);

        PackageOntology.PropertyType contactInfoType =  dcsBoOntology. new PropertyType();
        contactInfoType.setName(CONTACT_INFO_TYPE);
        contactInfoType.addField(dcsBoOntology.new Property(PERSON_NAME, STRING_TYPE, 1, 1, false, false));
        contactInfoType.addField(dcsBoOntology.new Property(PHONE, PHONE_NUMBER_TYPE, 0, Integer.MAX_VALUE, false, false));
        contactInfoType.addField(dcsBoOntology.new Property(EMAIL, EMAIL_TYPE, 0, Integer.MAX_VALUE, false, false));

        dcsBoOntology.getCustomPropertyTypes().add(contactInfoType);
        
        PackageOntology.PropertyType creatorType = dcsBoOntology. new PropertyType();
        creatorType.setName(CREATOR_TYPE);
        creatorType.addField(dcsBoOntology.new Property(PERSON_NAME, STRING_TYPE, 1, 1, false, false));
        creatorType.addField(dcsBoOntology.new Property(PHONE, PHONE_NUMBER_TYPE, 0, Integer.MAX_VALUE, false, false));
        creatorType.addField(dcsBoOntology.new Property(EMAIL, EMAIL_TYPE, 0, Integer.MAX_VALUE, false, false));
        creatorType.addField(dcsBoOntology.new Property(PAGE, URL_TYPE, 0, Integer.MAX_VALUE, false, false));
        dcsBoOntology.getCustomPropertyTypes().add(creatorType);

        return dcsBoOntology;
    }
}
