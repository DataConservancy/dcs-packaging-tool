package org.dataconservancy.packaging.tool.impl;

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

public class PropertyFormatServiceImpl implements PropertyFormatService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // array of labels used to format file size into B, kB, MB, GB, TB, PB, EB, ZB or YB value
    //TODO these should be moved into a resource somewhere so they can be changed, internationalized, etc.
    private static final String[] sizeLabels = {" Bytes", " kB", " MB", " GB", " TB", " PB", " EB", " ZB", " YB"};

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

        switch(type.getPropertyValueHint()) {
            case PHONE_NUMBER:
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                try {
                    Phonenumber.PhoneNumber number = phoneUtil.parseAndKeepRawInput(rawPropertyValue, "US");
                    String regionCode = phoneUtil.getRegionCodeForNumber(number);
                    rawPropertyValue = phoneUtil.formatInOriginalFormat(number, regionCode);
                } catch (NumberParseException e) {
                    log.warn("Phone number wasn't properly formatted uri, using provided value as is: " + rawPropertyValue);
                }
                break;
            case EMAIL:
                if (rawPropertyValue.startsWith("mailto:")) {
                    rawPropertyValue = rawPropertyValue.substring(7);
                }
                break;
            case FILE_SIZE:
                final DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
                double doubleValue = Double.parseDouble(rawPropertyValue);
                int i=0;
                int test = 1;
                while(doubleValue >= test*1000 && i < sizeLabels.length - 1){
                    test *= 1000;
                    i++;
                }
                String sizeLabel = (doubleValue == 1) ? " Byte" : sizeLabels[i];
                rawPropertyValue = twoDecimalForm.format(doubleValue / test) + sizeLabel;
                break;
        }

        return rawPropertyValue;
    }

    @Override
    public Property parsePropertyValue(PropertyType type, String value) {
        Property formattedProperty = new Property(type);

        String formattedValue = value;
        switch(type.getPropertyValueHint()) {
            case PHONE_NUMBER:
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                try {
                    formattedValue = phoneUtil.format(phoneUtil.parseAndKeepRawInput(value, "US"), PhoneNumberUtil.PhoneNumberFormat.RFC3966);
                } catch (NumberParseException e) {
                    log.error("Phone number wasn't properly formed, unable to generate uri: " + formattedValue);
                }
                break;
            case EMAIL:
                if (!value.startsWith("mailto:")) {
                    formattedValue = "mailto:" + value;
                }
                break;
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
