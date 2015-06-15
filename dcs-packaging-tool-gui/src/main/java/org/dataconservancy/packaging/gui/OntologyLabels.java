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
 * Wrapper to access bundled resource for property labels. These are labels that match ontology property names, to pretty labels. They 
 * are ontology specific, and the property file should be changed if the ontology is changed.
 * This is a simple class, if the property has a matching label it's returned otherwise the property name is just returned as the label.
 */
public class OntologyLabels {
    
    private ResourceBundle bundle;

    public OntologyLabels(ResourceBundle bundle) {
        this.bundle = bundle;

    }

    /**
     * Gets the label if one is found for the property name, otherwise returns the property name back to the user to use as the label.
     * @param propertyName The name of the property to retrieve the label for
     * @return The label for the given property name, or the the property name if no label is found.
     */
    public String get(String propertyName) {
        String propertyLabel = propertyName;
        if (bundle.containsKey(propertyName)) {
            propertyLabel = bundle.getString(propertyName);
        }

        return propertyLabel;
    }
}