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

package org.dataconservancy.packaging.tool.impl.support;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * This is a phone number validating utility using google's libphonenumber. A local country code
 * can be set in the config_default.properties file to override the hard coded default of US. The mapping of
 * these codes can be found in google's CountryCodeToRegionCodeMap.java source file.
 */
public class PhoneNumberValidator implements C14NValidator {

    /* this is set in the config_default.properties file as the property "localCountryCode" */
    /* which will override the default set to US here */
    static String localCountryCode = "US";
    static String genericExitPrefix = "+";

    @Override
    public ValidatorResult isValid(String phoneNumber) {
        ValidatorResult vr = new ValidatorResult();

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumber numberProto;

        try {
            if(phoneNumber.startsWith(genericExitPrefix)){
                numberProto = phoneUtil.parse(phoneNumber,"");
            } else {
                numberProto = phoneUtil.parse(phoneNumber, localCountryCode);
            }

        } catch (NumberParseException e) {
            vr.setResult(false);
           return vr;
        }

        if(numberProto != null) {
            vr.setResult(phoneUtil.isValidNumber(numberProto));
            return vr;
        } else {
            vr.setResult(false);
            return vr;
        }
    }

    public void setLocalCountryCode(String code){
        localCountryCode = code;
    }

    public static String getLocalCountryCode() {
        return localCountryCode;
    }

    @Override
    public String canonicalize(String toCanonicalize) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String canonicalizedNumber = null;
        try {
            canonicalizedNumber = util.format(
                    util.parse(toCanonicalize, localCountryCode), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            throw new RuntimeException("Unexpected/invalid phone number: '" + toCanonicalize + "'");
        }

        return canonicalizedNumber;
    }

}
