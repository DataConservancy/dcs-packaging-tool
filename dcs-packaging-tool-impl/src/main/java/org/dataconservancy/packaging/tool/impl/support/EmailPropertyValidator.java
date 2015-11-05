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

package org.dataconservancy.packaging.tool.impl.support;

import org.dataconservancy.packaging.tool.api.support.OntologyPropertyValidator;
import org.dataconservancy.packaging.tool.model.PropertyValidationResult;
import org.dataconservancy.packaging.tool.model.ValidationType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to test whether an email property value is valid
 */
public class EmailPropertyValidator implements OntologyPropertyValidator {

    private final static String emailRegex = "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$";
    private final static Pattern emailPattern = Pattern.compile(emailRegex);

    @Override
    public PropertyValidationResult validate(String propertyValue) {
        Matcher matcher =  emailPattern.matcher(propertyValue);
        return new PropertyValidationResult(matcher.matches(), ValidationType.EMAIL);
    }
}
