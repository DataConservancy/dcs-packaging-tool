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

package org.dataconservancy.packaging.tool.model;

import java.util.HashMap;
import java.util.Map;

/**
 * PackageDescription specification for DCS Business Objects.
 * <p>
 * Contains enumerations and constants used for representing DCS Business
 * Objects in PackageDescriptions.
 * </p>
 * 
 * @version $Id$
 */
public interface DcsPackageDescriptionSpec {

    public enum Property {

        /* Project.id */
        PROJECT_IDENTIFIER("Project.id"),

        /** Project.name */
        PROJECT_NAME("Project.name"),

        /** Project.description */
        PROJECT_DESCRIPTION("Project.description"),
        
        /** Collection.id */
        COLLECTION_ID("Collection.id"),

        /** Collection.title */
        COLLECTION_TITLE("Collection.title"),

        /** Collection.summary */
        COLLECTION_SUMMARY("Collection.Summary"),
        
        /**Collection.contact.name */
        COLLECTION_CONTACT_NAME("Collection.contact.name"),
        
        /**Collection.contact.phone */
        COLLECTION_CONTACT_PHONE("Collection.contact.phone"),
        
        /**Collection.contact.email*/
        COLLECTION_CONTACT_EMAIL("Collection.contact.email"),
        
        /**Collection.contact.address*/
        COLLECTION_CONTACT_ADDRESS("Collection.contact.address"),
        
        /**Collection.discipline*/
        COLLECTION_DISCIPLINE("Collection.discipline"),

        /** Collection.contactInfo.name */
        COLLECTION_CREATOR_NAME("Collection.creator.name"),
        
        /** Collection.contactInfo.email */
        COLLECTION_CREATOR_EMAIL("Collection.creator.email"),
        
        /** Collection.contactInfo.phone */
        COLLECTION_CREATOR_PHONE("Collection.creator.phone"),

        /** DataItem.id */
        DATA_ITEM_ID("DataItem.id"),
        
        /** DataItem.name */
        DATA_ITEM_NAME("DataItem.name"),

        /** DataItem.description */
        DATA_ITEM_DESCRIPTION("DataItem.description"),

        /** DataFile.name: Simple name of the file */
        FILE_NAME("File.name"),

        /** DataFile.format: File format identifier */
        FILE_FORMAT("File.format"),

        /** DataFile.path: Logical file path, relative to package, e.g. /path/to/file.jpg */
        FILE_PATH("File.path"),

        /** MetadataFile.formatId: Identifier of the metadata format used in a metadata file. */
        FILE_METADATA_FORMAT_ID("MetadataFile.formatId");

        private static final Map<String, Property> stringValues =
                new HashMap<String, DcsPackageDescriptionSpec.Property>();

        static {
            for (Property p : Property.values()) {
                stringValues.put(p.toString(), p);
            }
        }

        private String artifactPropertyName;

        private Property(String artifactPropertyName) {
            this.artifactPropertyName = artifactPropertyName;

        }

        public String toString() {
            return artifactPropertyName;
        }

        public static Property fromString(String name) {
            return stringValues.get(name);
        }

    }

    public enum Relationship {
        COLLECTION_AGGREGATED_BY_PROJECT("Collection-aggregatedBy-Project"),
        COLLECTION_IS_PART_OF_COLLECTION("Collection-isPartOf-Collection"),

        DATA_ITEM_IS_MEMBER_OF_COLLECTION("DataItem-isMemberOf-Collection"),
        METADATA_FILE_IS_METADATA_FOR_BUSINESS_OBJECT(
                "MetadataFile-isMetadataFor-BusinessObject"),
        DATA_FILE_IS_PART_OF_DATA_ITEM("DataFile-isPartOf-DataItem"),
        IS_MEMBER_OF("isMemberOf"),
        IS_METADATA_FOR("isMetadataFor");

        private static final Map<String, Relationship> stringValues =
                new HashMap<String, DcsPackageDescriptionSpec.Relationship>();

        static {
            for (Relationship r : Relationship.values()) {
                stringValues.put(r.toString(), r);
            }
        }

        private String artifactRelationshipName;

        private Relationship(String artifactRelationshipName) {
            this.artifactRelationshipName = artifactRelationshipName;
        }

        public String toString() {
            return artifactRelationshipName;
        }

        public static Relationship fromString(String name) {
            return stringValues.get(name);
        }
    }
}
