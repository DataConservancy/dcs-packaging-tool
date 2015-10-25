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
package org.dataconservancy.packaging.tool.model;


/**
 * Names of the elements (parameters) used in generation BagIt package
 */
public class BagItParameterNames extends GeneralParameterNames{

    /**
     * Reserved element names specified in BagIt specs
     */
    public static final String BAGIT_VERSION = "BagIt-Version";
    public static final String TAG_FILE_CHAR_ENCODING = "Tag-File-Character-Encoding";

    public static final String SOURCE_ORG = "Source-Organization";
    public static final String ORG_ADDRESS = "Organization-Address";
    public static final String CONTACT_NAME = "Contact-Name";
    public static final String CONTACT_EMAIL = "Contact-Email";
    public static final String CONTACT_PHONE = "Contact-Phone";
    public static final String DESCRIPTION = "External-Description";
    public static final String BAGGING_DATE = "Bagging-Date";
    public static final String EXTERNAL_IDENTIFIER = "External-Identifier";
    public static final String EXTERNAL_DESCRIPTION = "External-Description";
    public static final String BAG_SIZE = "Bag-Size";
    public static final String PAYLOAD_OXUM = "Payload-Oxum";
    public static final String BAG_GROUP_ID = "Bag-Group-Identifier";
    public static final String BAG_COUNT = "Bag-Count";
    public static final String INTERNAL_SENDER_IDENTIFIER = "Internal-Sender-Identifier";
    public static final String INTERNAL_SENDER_DESCRIPTION = "Internal-Sender-Description";
    public static final String PKG_BAG_DIR = "PKG-BAG-DIR";
    // FIXME: Please move these to GeneralParams if they're not appropriate for BagIt.
    public static final String KEYWORD = "Keyword";
    public static final String RIGHTS_STRING = "Rights-String";
    public static final String RIGHTS_URI = "Rights-Uri";

    /**
     * Addtional element names
     */
    public static final String BAGIT_PROFILE_ID = "BagIt-Profile-Identifier";
}
