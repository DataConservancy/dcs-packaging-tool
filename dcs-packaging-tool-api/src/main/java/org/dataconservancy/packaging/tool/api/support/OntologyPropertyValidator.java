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
package org.dataconservancy.packaging.tool.api.support;

import org.dataconservancy.packaging.tool.model.PropertyValidationResult;

/**
 * This interface is used to validate different property types of an ontology. Each validator, validates one type of property, the ontology service
 * is responsible for making sure the correct validator is used.
 */
public interface OntologyPropertyValidator {

    /**
     * Validates the passed in property value.
     * @param propertyValue A string representing the property value to validate
     * @return A {@link org.dataconservancy.packaging.tool.model.PropertyValidationResult} with the results of the validation, never null.
     */
    public PropertyValidationResult validate(String propertyValue);
}
