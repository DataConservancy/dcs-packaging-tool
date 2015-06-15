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

import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.packaging.tool.api.support.OntologyPropertyValidator;
import org.dataconservancy.packaging.tool.model.PropertyValidationResult;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Simply checks that a url is proper uri format, and starts with either http:// or https://.
 */
public class UrlPropertyValidator implements OntologyPropertyValidator {
    @Override
    public PropertyValidationResult validate(String propertyValue) {
        boolean valid = UriUtility.isHttpUrl(propertyValue);

        return new PropertyValidationResult(valid, PropertyValidationResult.VALIDATION_HINT.URL);
    }
}
