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

package org.dataconservancy.packaging.gui;

import java.util.ResourceBundle;

/**
 * Wrapper to access bundled resource for errors.
 */
public class Errors {

    public enum ErrorKey {

        PACKAGE_GENERATION_CREATION_ERROR("packagegeneration.packagecreation.error"),
        PACKAGE_GENERATION_SAVE("packagegeneration.save.error"),
        MISSING_REQUIRED_FIELDS("packagenamemissing.error"),
        BASE_DIRECTORY_NOT_SELECTED("basedirectorynotselected.error"),
        OUTPUT_DIRECTORY_MISSING("outputdirectorymissing.error"),
        OUTPUT_DIRECTORY_AND_PACKAGE_NAME_MISSING("outputdirectoryandpackagenamemissing.error"),
        PACKAGE_TREE_VALIDATION_ERROR("packagetreevalidation.error"),
        PACKAGE_STATE_SAVE_ERROR("packagestatesave.error"),
        HIERARCHICAL_RELATIONSHIP_ERROR("hierarchicalrelationship.error"),
        INVALID_RELATIONSHIP_ERROR("invalidrelationship.error"),
        PARAM_LOADING_ERROR("paramsloading.error"),
        PACKAGE_FILENAME_HAS_ILLEGAL_CHARACTERS("packageFilenameIllegalCharacters.error"),
        OUTPUT_DIR_NOT_CREATED_ERROR("outputDirNotCreated.error"),
        PROPERTY_LOSS_WARNING("propertyloss.warning"),
        PACKAGE_TREE_BUILD_ERROR("packagetreebuild.error"),
        INACCESSIBLE_CONTENT_DIR("inaccessiblepackagecontent.error"),
        ADD_CONTENT_ERROR("addcontent.error"),
        IO_CREATE_ERROR("iocreate.error");


        private String property;

        ErrorKey(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    private ResourceBundle bundle;

    public Errors(ResourceBundle bundle) {
        this.bundle = bundle;

        for (ErrorKey key : ErrorKey.values()) {
            if (!bundle.containsKey(key.getProperty())) {
                throw new IllegalArgumentException("Missing resource in bundle: " + key.getProperty());
            }
        }
    }

    public String get(ErrorKey key) {
        if (!bundle.containsKey(key.getProperty())) {
            throw new IllegalArgumentException("No such resource: " + key.getProperty());
        }

        return bundle.getString(key.getProperty());
    }

    public String format(ErrorKey key, Object... args) {
        if (!bundle.containsKey(key.property)) {
            throw new IllegalArgumentException("No such resource: " +
                                                   key);
        }

        return String.format(bundle.getLocale(), bundle.getString(key.property), args);
    }
}
