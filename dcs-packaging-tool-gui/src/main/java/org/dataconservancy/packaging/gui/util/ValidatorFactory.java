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

package org.dataconservancy.packaging.gui.util;

import org.dataconservancy.packaging.tool.model.ValidationType;

/**
 * An enum of the different kinds of validation types for properties
 */
public class ValidatorFactory {
    public static Validator getValidator(ValidationType type){
        switch(type){
            case EMAIL:
                return new EmailValidator();
            case PHONE:
                return new PhoneNumberValidator();
            case URL:
                return new UrlValidator();
            default:
                return null;
        }
    }
}
