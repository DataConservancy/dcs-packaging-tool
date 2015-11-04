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
package org.dataconservancy.packaging.gui;

import java.util.ResourceBundle;

/**
 * Wrapper to access bundled resource for messages. Messages are formatted and
 * localized using String.format.
 */
public class Messages {
    private enum Key {
        PACKAGE_DESCRIPTION_BUILDER_SUCCESS("packagedescriptionbuilder.success"),
        PACKAGE_DESCRIPTION_BUILDER_FAILURE("packagedescriptionbuilder.failure"),
        PACKAGE_DESCRIPTION_BUILDER_ERROR("packagedescriptionbuilder.error"),
        PACKAGE_GENERATION_SUCCESS("packagegeneration.success"),
        ERROR_CREATING_NEW_PACKAGE("createnewpackage.error"),
        WARNING_FILENAME_LENGTH("filenamelength.warning"),
        WARNING_INVALID_PROPERTY("invalidproperty.warning"),
        WARNING_PACKAGE_DESCRIPTION_MODIFICATION("packagedescriptionmodification.warning"),
        URL_VALIDATION_FAILURE("urlvalidation.failure"),
        PHONE_VALIDATION_FAILURE("phonevalidation.failure"),
        EMAIL_VALIDATION_FAILURE("emailvalidation.failure");

        String property;

        Key(String property) {
            this.property = property;
        }
    }

    private static ResourceBundle bundle;

    public Messages(ResourceBundle bundle) {
        this.bundle = bundle;

        for (Key key : Key.values()) {
            if (!bundle.containsKey(key.property)) {
                throw new IllegalArgumentException("Missing resource in bundle: " + key);
            }
        }
    }

    private static String format(Key key, Object... args) {
        if (!bundle.containsKey(key.property)) {
            throw new IllegalArgumentException("No such resource: " + key);
        }

        return String.format(bundle.getLocale(), bundle.getString(key.property), args);
    }

    public static String formatPackageDescriptionBuilderSuccess(String file) {
        return format(Key.PACKAGE_DESCRIPTION_BUILDER_SUCCESS, file);
    }

    public static String formatPackageDescriptionBuilderFailure(String file) {
        return format(Key.PACKAGE_DESCRIPTION_BUILDER_FAILURE, file);
    }

    public static String formatPackageDescriptionBuilderError(String file) {
        return format(Key.PACKAGE_DESCRIPTION_BUILDER_ERROR, file);
    }
    
    public static String formatPackageGenerationSuccess(String file) {
        return format(Key.PACKAGE_GENERATION_SUCCESS, file);
    }
    
    public static String formatErrorCreatingNewPackage(String error) {
        return format(Key.ERROR_CREATING_NEW_PACKAGE, error);
    }

    public static String formatFilenameLengthWarning(int length) {
        return format(Key.WARNING_FILENAME_LENGTH, length);
    }

    public static String formatInvalidPropertyWarning(String type, String properties) {
        return format(Key.WARNING_INVALID_PROPERTY, type, properties);
    }

    public static String formatPackageDescriptionModificationWarning(String artifactRef, String oldType, String newType) {
        return format(Key.WARNING_PACKAGE_DESCRIPTION_MODIFICATION, artifactRef, oldType, newType);
    }

    public static String formatUrlValidationFailure(String url) {
        return format(Key.URL_VALIDATION_FAILURE, url);
    }

    public static String formatPhoneValidationFailure(String phoneNumber) {
        return format(Key.PHONE_VALIDATION_FAILURE, phoneNumber);
    }

    public static String formatEmailValidationFailure(String emailAddress) {
        return format(Key.EMAIL_VALIDATION_FAILURE, emailAddress);
    }
}
