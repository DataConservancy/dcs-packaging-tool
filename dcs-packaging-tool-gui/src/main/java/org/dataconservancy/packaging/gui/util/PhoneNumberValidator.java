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

package org.dataconservancy.packaging.gui.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * This is a phone number validating utility using google's libphonenumber. A local country code
 * can be set in the config_default.properties file to override the hard coded default of US. The mapping of
 * these codes can be found in google's CountryCodeToRegionCodeMap.java source file.
 */
public class PhoneNumberValidator {

    /* this is set in the config_default.properties file as the property "localCountryCode" */
    /* which will override the default set to US here */
    private static String localCountryCode = "US";
    private static String genericExitPrefix = "+";

    public static boolean isValid(String phoneNumber) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumber numberProto = null;

        try {
            if(phoneNumber.startsWith(genericExitPrefix)){
                numberProto = phoneUtil.parse(phoneNumber,"");
            } else {
                numberProto = phoneUtil.parse(phoneNumber, localCountryCode);
            }

        } catch (NumberParseException e) {
           return false;
        }

        if(numberProto != null) {
            return phoneUtil.isValidNumber(numberProto);
        } else {
            return false;
        }
    }

    public void setLocalCountryCode(String code){
        localCountryCode = code;
    }
}
