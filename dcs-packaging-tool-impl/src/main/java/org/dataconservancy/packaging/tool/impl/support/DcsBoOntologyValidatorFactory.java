/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl.support;

import org.dataconservancy.packaging.tool.api.support.OntologyPropertyValidator;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;

/**
 * Provides a validator for a given ontology type, currently only for DcsBo Ontology Types, if we create base ontology types it should use that instead.
 */
public class DcsBoOntologyValidatorFactory {

    /**
     * Retrieves an OntologyPropertyValidator for the provided type or null, if none exists.
     * @param type The property type to get a validator for.
     * @return The validator that should be used to validate the property value, or null if none exists.
     */
    public static OntologyPropertyValidator getValidator(String type) {
        OntologyPropertyValidator validator = null;
        switch (type) {
            case DcsBoPackageOntology.PHONE_NUMBER_TYPE:
                validator = new PhoneNumberPropertyValidator();
                break;
            case DcsBoPackageOntology.URL_TYPE:
                validator = new UrlPropertyValidator();
                break;
        }

        return validator;
    }
}
