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
 * A helper class for loading internal property strings from the resource bundle.
 */
public class InternalProperties {
    public enum InternalPropertyKey {

        PREFERENCES_NODE_NAME("preferencesnode.name"),
        HIDE_PROPERTY_WARNING_PREFERENCE("hidepropertywarning.preference");

        private String property;

        private InternalPropertyKey(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    private ResourceBundle bundle;

    public InternalProperties(ResourceBundle bundle) {
        this.bundle = bundle;

        for (InternalPropertyKey key : InternalPropertyKey.values()) {
            if (!bundle.containsKey(key.getProperty())) {
                throw new IllegalArgumentException("Missing resource in bundle: " + key.getProperty());
            }
        }
    }

    public String get(InternalPropertyKey key) {
        if (!bundle.containsKey(key.getProperty())) {
            throw new IllegalArgumentException("No such resource: " + key.getProperty());
        }

        return bundle.getString(key.getProperty());
    }

    public String get(String property) {
        if (!bundle.containsKey(property)) {
            return null;
        }
        return bundle.getString(property);
    }
}
