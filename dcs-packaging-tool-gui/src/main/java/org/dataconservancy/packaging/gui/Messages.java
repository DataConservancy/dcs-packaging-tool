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
    public enum MessageKey {
        ERROR_CREATING_NEW_PACKAGE("createnewpackage.error"),
        WARNING_FILENAME_LENGTH("filenamelength.warning"),
        WARNING_INVALID_PROPERTY("invalidproperty.warning"),
        URL_VALIDATION_FAILURE("urlvalidation.failure"),
        PHONE_VALIDATION_FAILURE("phonevalidation.failure"),
        EMAIL_VALIDATION_FAILURE("emailvalidation.failure"),
        REFRESH_STATUS_MESSAGE("refreshstatus.message"),
        URI_VALIDATION_FAILURE("urivalidation.failure"),
        FILE_NAME_VALIDATION_FAILURE("filenamevalidation.failure"),
        ADD_NEW_MESSAGE("addnew.message");

        String property;

        MessageKey(String property) {
            this.property = property;
        }
    }

    private ResourceBundle bundle;

    public Messages(ResourceBundle bundle) {
        this.bundle = bundle;

        for (MessageKey messageKey : MessageKey.values()) {
            if (!bundle.containsKey(messageKey.property)) {
                throw new IllegalArgumentException("Missing resource in bundle: " +
                                                       messageKey);
            }
        }
    }

    public String get(MessageKey key) {
        if (!bundle.containsKey(key.property)) {
            throw new IllegalArgumentException("No such resource: " + key.property);
        }

        return bundle.getString(key.property);
    }

    public String format(MessageKey messageKey, Object... args) {
        if (!bundle.containsKey(messageKey.property)) {
            throw new IllegalArgumentException("No such resource: " +
                                                   messageKey);
        }

        return String.format(bundle.getLocale(), bundle.getString(messageKey.property), args);
    }
}
