package org.dataconservancy.packaging.tool.impl;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

import static java.lang.Math.pow;

public class PropertyFormatServiceImpl implements PropertyFormatService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // array of labels used to format file size into B, kB, MB, GB, TB, PB, EB, ZB or YB value
    //TODO these should be moved into a resource somewhere so they can be changed, internationalized, etc.
    static final String[] sizeLabels = {" Bytes", " KiB", " MiB", " GiB", " TiB", " PiB", " EiB", " ZiB", " YiB"};

    @Override
    public String formatPropertyValue(Property value) {
        String rawPropertyValue = "";

        PropertyType type = value.getPropertyType();
        switch (type.getPropertyValueType()) {
            case STRING:
                rawPropertyValue = value.getStringValue();
                break;
            case LONG:
                rawPropertyValue = String.valueOf(value.getLongValue());
                break;
            case DATE_TIME:
                rawPropertyValue = DateUtility.toIso8601_DateTime(value.getDateTimeValue().toDate());
                break;
        }

        if (type.getPropertyValueHint() != null) {
            switch (type.getPropertyValueHint()) {
                case PHONE_NUMBER:
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        Phonenumber.PhoneNumber number = phoneUtil.parseAndKeepRawInput(rawPropertyValue, "US");
                        String regionCode = phoneUtil.getRegionCodeForNumber(number);
                        rawPropertyValue = phoneUtil.formatInOriginalFormat(number, regionCode);
                    } catch (NumberParseException e) {
                        log.warn(
                            "Phone number wasn't properly formatted uri, using provided value as is: " +
                                rawPropertyValue);
                    }
                    break;
                case EMAIL:
                    if (rawPropertyValue.startsWith("mailto:")) {
                        rawPropertyValue = rawPropertyValue.substring(7);
                    }
                    break;
                case FILE_SIZE:
                    final DecimalFormat bytes = new DecimalFormat("#");
                    final DecimalFormat twoDecimalForm = new DecimalFormat("0.00");
                    double doubleValue = Double.parseDouble(rawPropertyValue);
                    for (int pow = 1; pow <= 9; pow++) {
                        String label = sizeLabels[pow - 1];
                        DecimalFormat decFormat;
                        if (pow == 1) {
                            decFormat = bytes;
                        } else {
                            decFormat = twoDecimalForm;
                        }

                        if (doubleValue / pow(1024, pow) < 1 || pow == 9) {
                            rawPropertyValue = String.format("%s%s",
                                    decFormat.format(doubleValue / (pow(1024, pow - 1))), label);
                            break;
                        }
                    }
                    break;
            }
        }

        return rawPropertyValue;
    }

    @Override
    public Property parsePropertyValue(PropertyType type, String value) {
        Property formattedProperty = new Property(type);

        String formattedValue = value;
        if (type.getPropertyValueHint() != null) {
            switch (type.getPropertyValueHint()) {
                case PHONE_NUMBER:
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        formattedValue = phoneUtil.format(phoneUtil.parseAndKeepRawInput(value, "US"), PhoneNumberUtil.PhoneNumberFormat.RFC3966);
                    } catch (NumberParseException e) {
                        log.error(
                            "Phone number wasn't properly formed, unable to generate uri: " +
                                formattedValue);
                    }
                    break;
                case EMAIL:
                    if (!value.startsWith("mailto:")) {
                        formattedValue = "mailto:" + value;
                    }
                    break;
            }
        }

        switch (type.getPropertyValueType()) {
            case STRING:
                formattedProperty.setStringValue(formattedValue);
                break;
            case LONG:
                formattedProperty.setLongValue(Long.valueOf(formattedValue));
                break;
            case DATE_TIME:
                formattedProperty.setDateTimeValue(DateUtility.parseDateString(formattedValue));
                break;
        }
        return formattedProperty;
    }

}
