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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.dataconservancy.packaging.tool.api.support.OntologyPropertyValidator;
import org.dataconservancy.packaging.tool.model.PropertyValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses google phone number lib to ensure that the string provided is a valid phone number.
 */
public class PhoneNumberPropertyValidator implements OntologyPropertyValidator {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public PropertyValidationResult validate(String propertyValue) {
        boolean valid = false;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;

        try {
            phoneNumber = phoneUtil.parse(propertyValue, "US");
        } catch (NumberParseException e) {
            log.debug("Phone number property value wasn't able to be parsed: " + propertyValue);
        }

        if(phoneNumber != null) {
            valid = phoneUtil.isValidNumber(phoneNumber);
        }

        return new PropertyValidationResult(valid, PropertyValidationResult.VALIDATION_HINT.PHONE);
    }
}
