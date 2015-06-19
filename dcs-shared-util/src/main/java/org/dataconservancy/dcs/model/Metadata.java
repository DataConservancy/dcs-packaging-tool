/*
 * Copyright 2012 Johns Hopkins University
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
package org.dataconservancy.dcs.model;

/**
 * String constants used for metadata keys.
 */
public class Metadata {

    /**
     * A contact name.  For example, the name of the organization who is providing a BagIt serialization.
     */
    public static final String CONTACT_NAME = "Contact-Name";

    /**
     * A contact phone number.  For example, the phone number of the organization who is providing the BagIt
     * serialization.
     */
    public static final String CONTACT_PHONE = "Contact-Phone";

    /**
     * A contact email address.  For example, the email address of the organization who is providing the BagIt
     * serialization.
     */
    public static final String CONTACT_EMAIL = "Contact-Email";

    /**
     * Identifies a logical grouping of serializations.  For example, a single BagIt bag may be comprised of multiple,
     * discreet files.  Each discreet file in the group would contain the same value for {@code GROUP_IDENTIFIER}.
     */
    public static final String GROUP_IDENTIFIER = "Group-Identifier";

    /**
     * The logical sequence number of the serialization.  For example, the 2nd serialization file in the group.
     */
    public static final String GROUP_SEQUENCE_NO = "Group-Sequence-Number";

    /**
     * The total number of serialization files in the group.
     */
    public static final String GROUP_TOTAL_NO = "Group-Size";

    /**
     * The exact size of the serialization file, in bytes.
     * <br>
     * TODO: this is the size of the serialization when it is uncompressed
     * TODO: this size includes all serialization metadata files and description metadata files.
     * TODO: example
     */
    public static final String TOTAL_SIZE_BYTES = "Total-Size-Bytes";

    /**
     * The number of files in the serialization file.
     * <br>
     * TODO: this doesn't (?) count a directory as a file.  Consequence (?): empty directories aren't counted
     * TODO: this counts all serialization metadata files and description metadata files.
     * TODO: example
     */
    public static final String TOTAL_COUNT_FILES = "Total-File-Count";

    /**
     * The exact size of the payload of the serialization, in bytes.
     * <br>
     * TODO: this is the size of the payload when it is un-compressed
     * TODO: this size of the payload doesn't include serialization metadata files, or description metadata files.
     * TODO: example
     */
    public static final String PAYLOAD_SIZE_BYTES = "Payload-Size-Bytes";

    /**
     * The number of files in the payload.
     * <br>
     * TODO: this doesn't count a directory as a file.  Consequence (?): empty directories aren't counted.
     * TODO: this doesn't count serialization metadata files, or description metadata files.
     * TODO: example
     */
    public static final String PAYLOAD_COUNT_FILES = "Payload-File-Count";

    /**
     * An identifier that is supplied by the provider or sender of the serialization.  The author should take care to
     * provide reasonable guarantees that the identifier is unique.  The receiver may treat the identifier as
     * non-opaque.
     */
    public static final String EXTERNAL_IDENTIFIER = "External-Identifier";

    /**
     * An identifier that corresponds to the serialization specification.  For example, it would identify the
     * serialization as a BagIT serialization, or a simple {@code tar} file, etc.
     */
    public static final String SERIALIZATION_FORMAT_IDENTIFIER = "Serialization-Format-Identifier";

    /**
     * The version of the {@link #SERIALIZATION_FORMAT_IDENTIFIER serialization specification} in use.
     */
    public static final String SERIALIZATION_FORMAT_VERSION = "Serialization-Format-Version";

    /**
     * An identifier that corresponds to a profile of the {@link #SERIALIZATION_FORMAT_IDENTIFIER serialization
     * specification} in use.  For example, the Data Conservancy profile of BagIt.
     */
    public static final String SERIALIZATION_FORMAT_PROFILE_IDENTIFIER = "Serialization-Format-Profile-Identifier";

    
    /** Collection Identifier */
    public static final String COLLECTION_IDENTIFIER = "Collection-Identifier";

    /** Collection Citable locator */
    public static final String COLLECTION_CITABLE_LOCATOR = "Collection-Citable-Locator";

    /** Collection Title */
    public static final String COLLECTION_TITLE = "Collection-Title";

    /** Collection creator */
    public static final String COLLECTION_CREATOR = "Collection-Creator";

    public static final String COLLECTION_CREATOR_NAME = "Collection-Creator-Name";

    /** Collection creator email */
    public static final String COLLECTION_CREATOR_EMAIL = "Collection-Creator-Email";

    /** Collection creator phone number */
    public static final String COLLECTION_CREATOR_PHONE = "Collection-Creator-Phone";

    /** Collection description */
    public static final String COLLECTION_DESCRIPTION = "Collection-Description";

    /** Collection publisher */
    public static final String COLLECTION_PUBLISHER = "Collection-Publisher";

    /** Collection creation date */
    public static final String COLLECTION_CREATED = "Collection-Created";

    /** Collection publication date */
    public static final String COLLECTION_PUBLICATION_DATE = "Collection-Publication-Date";

    /** Collection last modified date */
    public static final String COLLECTION_MODIFIED = "Collection-Modified";

    /** Collection property */
    public static final String COLLECTION_PROPERTY = "Collection-Property";

    /** Collection aggregates collection relationship */
    public static final String COLLECTION_AGGREGATES_COLLECTION = "Collection-Aggregates-Collection";

    /** Collection aggregates DataItem relationship */
    public static final String COLLECTION_AGGREGATES_DATAITEM = "Collection-Aggregates-DataItem";

    /** Collection aggregates File relationship */
    public static final String COLLECTION_AGGREGATES_FILE = "Collection-Aggregates-File";
    
    /** Collection aggregatedBy Project relationship */
    public static final String COLLECTION_AGGREGATED_BY_PROJECT = "Collection-Aggregated-By-Project";
    
    /** Collection isPartOf Collection or Project relationship */
    public static final String COLLECTION_IS_PART_OF = "Collection-IsPartOf";
    
    
    /** Collection Contact info*/
    public static final String COLLECTION_CONTACT_INFO = "Collection-Contact-Info";

    public static final String COLLECTION_CONTACT_NAME = "Collection-Contact-Name";

    /** Collection Contact Info Phone */
    public static final String COLLECTION_CONTACT_PHONE = "Collection-Contact-Phone";

    /** Collection Contact Info Email */
    public static final String COLLECTION_CONTACT_EMAIL = "Collection-Contact-Email";

    /** Collection Contact Address */
    public static final String COLLECTION_CONTACT_ADDRESS = "Collection-Contact-Address";

    /** Collection Discipline*/
    public static final String COLLECTION_DISCIPLINE = "Collection-Discipline";
    
    /**  DataItem identifier */
    public static final String DATA_ITEM_IDENTIFIER = "DataItem-Identifier";

    public static final String DATA_ITEM_TITLE = "DataItem-Title";
    public static final String DATA_ITEM_CITABLE_LOCATOR = "DataItem-Citable-Locator";

    public static final String DATA_ITEM_CREATOR = "DataItem-Creator";

    public static final String DATA_ITEM_CREATOR_NAME = "DataItem-Creator-Name";

    public static final String DATA_ITEM_CREATOR_EMAIL = "DataItem-Creator-Email";

    public static final String DATA_ITEM_CREATOR_PHONE = "DataItem-Creator-Phone";

    public static final String DATA_ITEM_DESCRIPTION = "DataItem-Description";
    
    public static final String DATA_ITEM_CONTACT_INFO = "DataItem-Contact-Info";
    
    public static final String DATA_ITEM_CONTENT_MODEL= "DataItem-Content-Model";

    public static final String DATA_ITEM_CREATED = "DataItem-Created";

    public static final String DATA_ITEM_MODIFIED = "DataItem-Modified";

    public static final String DATA_ITEM_PUBLICATION_DATE = "DataItem-Publication-Date";

    public static final String DATA_ITEM_PROPERTY = "DataItem-Property";

    public static final String DATA_ITEM_AGGREGATES_FILE = "DataItem-Aggregates-File";

    /** DataItem isPartOf Collection relationship */
    public static final String DATA_ITEM_IS_PART_OF_COLLECTION = "DataItem-IsPartOf";

    public static final String DATA_ITEM_IS_VERSION_OF_DATA_ITEM = "DataItem-IsVersionOf-DataItem";

    /**
     * Identifies metadata attribute that describes a file format.
     */
    public static final String FILE_FORMAT = "File-Format";

    /**
     * Identifies metadata attribute that describes a file format.
     */
    public static final String FILE_CONFORMS_TO = "File-Conforms-To";

    /**
     * Identifies metadata attribute that describes a file name.
     */
    public static final String FILE_PATH= "File-Path";
    /**
     * Identifies metadata attribute that describes a file name.
     */
    public static final String FILE_NAME= "File-Name";

    public static final String FILE_IDENTIFIER= "File-Identifier";

    public static final String FILE_SIZE = "File-Size";

    public static final String FILE_TITLE = "File-Title";

    public static final String FILE_CREATED = "File-Created";
    
    public static final String FILE_MODIFIED = "File-Modified";
    
    public static final String FILE_CREATOR_NAME = "File-Creator-Name";

    public static final String FILE_CREATOR_EMAIL = "File-Creator-Email";

    public static final String FILE_CREATOR_PHONE = "File-Creator-Phone";

    public static final String FILE_DESCRIPTION = "File-Description";

    public static final String FILE_IMPORTED_DATE = "File-Imported-Date";

    public static final String FILE_PROPERTY = "File-Property";

    public static final String FILE_FORMAT_DETECTION_TOOL_NAME = "File-Format-Detection-Tool-Name";

    public static final String FILE_FORMAT_DETECTION_TOOL_VERSION = "File-Format-Detection-Tool-Version";
    
    public static final String FILE_IS_METADATA_FOR = "File-IsMetadataFor";

    /**
     * Identifies metadata attribute that describes a BagIt provided checksum, stored in manifest files.
     */
    public static final String BAGIT_CHECKSUM = "Manifest-Entry";

    /**
     * Identifies metadata attribute that describes checksum generated by the system.
     */
    public static final String CALCULATED_CHECKSUM = "Calculated-Checksum";
    
    /**
     * Attribute Names for Ore-ReM
     */
    public static final String TITLE = "Title";
    public static final String IDENTIFIER = "Identifier";
    public static final String CITABLE_LOCATOR = "Citable-Locator";
    public static final String CREATOR = "Creator";
    public static final String CREATOR_NAME = "Creator-Name";
    public static final String CREATOR_EMAIL = "Creator-Email";
    public static final String CREATOR_PHONE = "Creator-Phone";
    public static final String CONTACT_INFO = "Contact-Info";
    public static final String CONTACT_INFO_NAME = "Contact-Info-Name";
    public static final String CONTACT_INFO_EMAIL = "Contact-Info-Email";
    public static final String CONTACT_INFO_PHONE = "Contact-Info-Phone";
    public static final String PUBLISHER = "Publisher";
    public static final String DESCRIPTION = "Description";
    public static final String DISCIPLINE = "Discipline";
    public static final String CONTENT_MODEL = "Content-Model";
    public static final String CREATED = "Created";
    public static final String MODIFIED = "Modified";
    public static final String PUBLICATION_DATE = "Publication-Date";
    public static final String PROPERTY = "Property";
    public static final String AGGREGATES_PROJECT = "Aggregates-Project";
    public static final String AGGREGATES_COLLECTION = "Aggregates-Collection";
    public static final String AGGREGATES_DATAITEM = "Aggregates-DataItem";
    public static final String AGGREGATES_FILE = "Aggregates-File";
    public static final String IS_PART_OF_COLLECTION = "IsPartOf-Collection";
    public static final String IS_VERSION_OF_DATAITEM = "IsVersionOf-DataItem";
    public static final String IS_METADATA_FOR = "IsMetadataFor";
    public static final String AGGREGATED_BY_PROJECT = "Aggregated-By-Project";
    public static final String FORMAT = "Format";
    public static final String CONFORMS_TO = "Conforms-To";
    public static final String SIZE = "Size";
    public static final String PATH = "Path";
    public static final String NAME = "Name";
    public static final String PACKAGE_AGGREGATES_PROJECT = "Package-Aggregates-Project";
    public static final String PACKAGE_AGGREGATES_COLLECTION = "Package-Aggregates-Collection";
    public static final String PACKAGE_AGGREGATES_DATAITEM = "Package-Aggregates-DataItem";
    public static final String PACKAGE_AGGREGATES_FILE = "Package-Aggregates-File";
    public static final String PROJECT_IDENTIFIER = "Project-Identifier";
    public static final String PROJECT_AGGREGATES_FILE = "Project-Aggregates-File";
    public static final String PROJECT_AGGREGATES_COLLECTION = "Project-Aggregates-Collection";

    public static final String PACKAGE_RESOURCEID = "Package-ResourceId";
    public static final String PROJECT_RESOURCEID = "Project-ResourceId";
    public static final String COLLECTION_RESOURCEID = "Collection-ResourceId";
    public static final String DATAITEM_RESOURCEID = "DataItem-ResourceId";
    public static final String FILE_RESOURCEID = "File-ResourceId";
    public static final String IS_PART_OF ="IsPartOf";

    
    /**
     * BagIt and Manifest Metadata Constants
     */
    public static final String BAGIT_VERSION = "BagIt-Version";
    public static final String TAG_FILE_CHARACTER_ENCODING = "Tag-File-Character-Encoding";
    public static final String BAG_SIZE = "Bag-Size";
    public static final String PAYLOAD_OXUM = "Payload-Oxum";
    public static final String BAG_COUNT = "Bag-Count";
    public static final String BAG_GROUP_IDENTIFIER = "Bag-Group-Identifier";
    public static final String BAGGING_DATE = "Bagging-Date";
    public static final String BAGIT_PROFILE_IDENTIFIER = "BagIt-Profile-Identifier";
    public static final String PKG_BAG_DIR = "PKG-BAG-DIR";
    public static final String PKG_ORE_REM = "PKG-ORE-REM";
}
