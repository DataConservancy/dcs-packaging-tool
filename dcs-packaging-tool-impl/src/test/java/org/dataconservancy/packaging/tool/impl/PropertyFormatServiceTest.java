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


import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertyFormatServiceTest {

    private PropertyFormatService underTest;

    @Before
    public void setup() {
        underTest = new PropertyFormatServiceImpl();
    }

    /**
     * Tests that properties are correctly formatted to string values.
     */
    @Test
    public void testFormatProperty() {
        //Tests that a long value is essentially passed through as is.
        PropertyType longType = new PropertyType();
        longType.setPropertyValueType(PropertyValueType.LONG);
        longType.setPropertyValueHint(PropertyValueHint.TEXT);
        Property longProperty = new Property(longType);
        longProperty.setLongValue(23582032);
        assertEquals("23582032", underTest.formatPropertyValue(longProperty));

        //Tests that size is correctly parsed to be presented to the user
        PropertyType sizeType = new PropertyType();
        sizeType.setPropertyValueType(PropertyValueType.LONG);
        sizeType.setPropertyValueHint(PropertyValueHint.FILE_SIZE);
        Property sizeProperty = new Property(sizeType);
        sizeProperty.setLongValue(4624327L);
        assertEquals("4.41 MiB", underTest.formatPropertyValue(sizeProperty));

        //Try the biggest allowable file size
        sizeType = new PropertyType();
        sizeType.setPropertyValueType(PropertyValueType.LONG);
        sizeType.setPropertyValueHint(PropertyValueHint.FILE_SIZE);
        sizeProperty = new Property(sizeType);
        sizeProperty.setLongValue(Long.parseUnsignedLong("9223372036854775807"));
        assertEquals("8.00 EiB", underTest.formatPropertyValue(sizeProperty));

        // Try the biggest allowable file size, plus 1
        // TODO: whoopsie
        sizeType = new PropertyType();
        sizeType.setPropertyValueType(PropertyValueType.LONG);
        sizeType.setPropertyValueHint(PropertyValueHint.FILE_SIZE);
        sizeProperty = new Property(sizeType);
        sizeProperty.setLongValue(Long.parseUnsignedLong("9223372036854775807") + 1);
        assertEquals("-9223372036854776000 Bytes", underTest.formatPropertyValue(sizeProperty));

        //Try some bytes
        sizeType = new PropertyType();
        sizeType.setPropertyValueType(PropertyValueType.LONG);
        sizeType.setPropertyValueHint(PropertyValueHint.FILE_SIZE);
        sizeProperty = new Property(sizeType);
        sizeProperty.setLongValue(Long.parseUnsignedLong("1024"));
        assertEquals("1.00 KiB", underTest.formatPropertyValue(sizeProperty));

        //Try some bytes
        sizeType = new PropertyType();
        sizeType.setPropertyValueType(PropertyValueType.LONG);
        sizeType.setPropertyValueHint(PropertyValueHint.FILE_SIZE);
        sizeProperty = new Property(sizeType);
        sizeProperty.setLongValue(Long.parseUnsignedLong("1023"));
        assertEquals("1023 Bytes", underTest.formatPropertyValue(sizeProperty));

        //Tests that phone numbers are correctly formatted to remove uri prefix.
        PropertyType phoneType = new PropertyType();
        phoneType.setPropertyValueType(PropertyValueType.STRING);
        phoneType.setPropertyValueHint(PropertyValueHint.PHONE_NUMBER);
        Property phoneProperty = new Property(phoneType);
        phoneProperty.setStringValue("tel:+1-888-651-5908");
        assertEquals("+1 888-651-5908", underTest.formatPropertyValue(phoneProperty));

        //Tests that email is correctly formatted to remove uri prefix.
        PropertyType emailType = new PropertyType();
        emailType.setPropertyValueType(PropertyValueType.STRING);
        emailType.setPropertyValueHint(PropertyValueHint.EMAIL);
        Property emailProperty = new Property(emailType);
        emailProperty.setStringValue("mailto:foo@email.com");
        assertEquals("foo@email.com", underTest.formatPropertyValue(emailProperty));

        //Tests that plain text is passed through unformatted.
        PropertyType textType = new PropertyType();
        textType.setPropertyValueType(PropertyValueType.STRING);
        textType.setPropertyValueHint(PropertyValueHint.TEXT);
        Property textProperty = new Property(textType);
        textProperty.setStringValue("cow");
        assertEquals("cow", underTest.formatPropertyValue(textProperty));

        //Tests that date time values are correctly parsed to ISO format.
        PropertyType dateType = new PropertyType();
        dateType.setPropertyValueType(PropertyValueType.DATE_TIME);
        dateType.setPropertyValueHint(PropertyValueHint.TEXT);
        Property dateProperty = new Property(dateType);
        dateProperty.setDateTimeValue(new DateTime(2015, 10, 31, 10, 18, 45, DateTimeZone.UTC));
        assertEquals("2015-10-31T10:18:45.000Z", underTest.formatPropertyValue(dateProperty));
    }

    /**
     * Tests that string values returned to the service are correctly converted back to property objects.
     */
    @Test
    public void testParseProperty() {
        //Tests that long values are correctly converted to longs
        PropertyType longType = new PropertyType();
        longType.setPropertyValueType(PropertyValueType.LONG);
        longType.setPropertyValueHint(PropertyValueHint.TEXT);
        Property longProperty = new Property(longType);
        longProperty.setLongValue(23582032);
        assertEquals(longProperty, underTest.parsePropertyValue(longType, "23582032"));

        //Checks that the size property is passed through unchanged
        PropertyType sizeType = new PropertyType();
        sizeType.setPropertyValueType(PropertyValueType.LONG);
        sizeType.setPropertyValueHint(PropertyValueHint.FILE_SIZE);
        Property sizeProperty = new Property(sizeType);
        sizeProperty.setLongValue(4624327L);
        assertEquals(sizeProperty, underTest.parsePropertyValue(sizeType, "4624327"));

        //Tests that phone is formatted as a URI
        PropertyType phoneType = new PropertyType();
        phoneType.setPropertyValueType(PropertyValueType.STRING);
        phoneType.setPropertyValueHint(PropertyValueHint.PHONE_NUMBER);
        Property phoneProperty = new Property(phoneType);
        phoneProperty.setStringValue("tel:+1-888-651-5908");
        assertEquals(phoneProperty, underTest.parsePropertyValue(phoneType, "+1 888-651-5908"));

        //Tests that email is correctly formatted as a URI
        PropertyType emailType = new PropertyType();
        emailType.setPropertyValueType(PropertyValueType.STRING);
        emailType.setPropertyValueHint(PropertyValueHint.EMAIL);
        Property emailProperty = new Property(emailType);
        emailProperty.setStringValue("mailto:foo@email.com");
        assertEquals(emailProperty, underTest.parsePropertyValue(emailType, "foo@email.com"));

        //Tests that text is passed through unformatted.
        PropertyType textType = new PropertyType();
        textType.setPropertyValueType(PropertyValueType.STRING);
        textType.setPropertyValueHint(PropertyValueHint.TEXT);
        Property textProperty = new Property(textType);
        textProperty.setStringValue("cow");
        assertEquals(textProperty, underTest.parsePropertyValue(textType, "cow"));

        //Tests that date strings are converted to date time objects.
        PropertyType dateType = new PropertyType();
        dateType.setPropertyValueType(PropertyValueType.DATE_TIME);
        dateType.setPropertyValueHint(PropertyValueHint.TEXT);
        Property dateProperty = new Property(dateType);
        dateProperty.setDateTimeValue(DateUtility.parseDateString("2015-10-31T10:18:45.000Z"));
        assertEquals(dateProperty, underTest.parsePropertyValue(dateType, "2015-10-31T10:18:45.000Z"));
    }
}
