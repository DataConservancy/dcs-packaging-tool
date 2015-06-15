/*
 * Copyright 2012 Johns Hopkins University
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
package org.dataconservancy.dcs.util;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;
import org.junit.Test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test class for DateUtility class
 */
public class DateUtilityTest {

    /**
     * Tests that DateUtility can parse  the Iso8601 time string it generates from a long value of milliseconds of time.
     */
    @Test
    public void roundTripIso8601Test() {
        long time = new Date().getTime();
        String iso = DateUtility.toIso8601_DateTime(time);
        assertEquals(time, DateUtility.parseDate(iso));
    }


    /**
     * Tests that parseDate method deals with fractions of second, down to milliseconds correctly
     */
    @Test
    public void varyingPrecisionIso8601Test() {
        assertEquals(0, DateUtility.parseDate("1970-01-01T00:00:00.000Z"));
        assertEquals(10, DateUtility.parseDate("1970-01-01T00:00:00.010Z"));
        assertEquals(10, DateUtility.parseDate("1970-01-01T00:00:00.01Z"));
        assertEquals(100, DateUtility.parseDate("1970-01-01T00:00:00.1Z"));
        assertEquals(1000, DateUtility.parseDate("1970-01-01T00:00:01.0Z"));
        assertEquals(1000, DateUtility.parseDate("1970-01-01T00:00:01Z"));
    }

    /**
     * Tests format of the date string produced by different formatters
     */
    @Test
    public void defaultTimeZoneTest() {
        assertTrue(DateUtility.toIso8601_DateTime(0).endsWith("Z"));
        assertTrue(DateUtility.toRfc822(0).endsWith("GMT"));
    }

    /**
     * Test that DateUtility can parse Rfc822 datetime string it generates from a {@code long} value of time.
     */
    @Test
    public void roundTripRfc822Test() {

        long time = new Date().getTime();
        String rfc = DateUtility.toRfc822(time);
        long truncated = (time / 1000) * 1000;

        assertEquals(truncated, DateUtility.parseDate(rfc));
    }

    /**
     * Tests that DateUtility can parse various formats of RFC822 date time string
     */
    @Test
    public void timeZoneRfc822Test() {
        assertEquals(0, DateUtility.parseDate("Thu, 1 Jan 1970 00:00:00 GMT"));
        assertEquals(0, DateUtility.parseDate("Thu, 1 Jan 1970 00:00:00 UTC"));
        assertEquals(0, DateUtility.parseDate("Thu, 1 Jan 1970 00:00:00 +0000"));
        assertEquals((4 * 60 * 60 * 1000), DateUtility
                .parseDate("Thu, 1 Jan 1970 00:00:00 EDT"));
        assertEquals((4 * 60 * 60 * 1000), DateUtility
                .parseDate("Thu, 1 Jan 1970 00:00:00 -0400"));
    }

    /**
     * Test that DateUtility.parseDate can deal with leading zero in date time string.
     */
    @Test
    public void leadingZerosRfc822Test() {
        assertEquals(DateUtility.parseDate("Thu, 7 Jan 1943 00:00:00 EDT"),
                     DateUtility.parseDate("Thu, 07 Jan 1943 00:00:00 EDT"));
    }

    /**
     * Tests that DateUtility can parse datetime String formatted according to RFC822, as well as format string into
     * that format from a DateTime object.
     */
    @Test
    public void testFormatRfc822() {
        DateTime testDate = new DateTime(2012, 3, 24, 16, 04, 55, 0, DateTimeZone.forID("GMT"));
        String expectedDateString = "Sat, 24 Mar 2012 16:04:55 GMT";
        String resultString = DateUtility.toRfc822(testDate);
        assertTrue(expectedDateString.equalsIgnoreCase(resultString));

        DateTime resultDate = DateUtility.parseRfc822Date(expectedDateString);
        assertNotNull(resultDate);

        assertEquals(testDate, resultDate);
    }

    /**
     * Tests that DateUtility can parse datetime String formatted according to RFC850, as well as format string into
     * that format from a DateTime object.
     */
    @Test
    public void testFormatRfc850() {
        DateTime testDate = new DateTime(2012, 3, 24, 16, 04, 55, 0, DateTimeZone.forID("GMT"));
        String expectedDateString = "Saturday, 24-Mar-12 16:04:55 GMT";
        String resultString = DateUtility.toRfc850(testDate);
        assertTrue(expectedDateString.equalsIgnoreCase(resultString));

        DateTime resultDate = DateUtility.parseRfc850Date(expectedDateString);
        assertNotNull(resultDate);

        assertEquals(testDate, resultDate);
    }
    /**
     * Tests that DateUtility can parse datetime String formatted according to ASC, as well as format string into
     * that format from a DateTime object.
     */
    @Test
    public void testFormatAsc() {
        DateTime testDate = new DateTime(2012, 3, 24, 16, 04, 55, 0, DateTimeZone.forID("GMT"));
        String expectedDateString = "Sat Mar 24 16:04:55 2012";
        String resultString = DateUtility.toAsc(testDate);
        assertTrue(expectedDateString.equalsIgnoreCase(resultString));

        DateTime resultDate = DateUtility.parseAscTime(expectedDateString);
        assertNotNull(resultDate);

        assertEquals(testDate, resultDate);
    }

    /**
     * Tests that date time strings in rfc822, rfc850, asc formats can be parsed with parseDateString method.
     */
    @Test
    public void testParseDateString() {
        String rfc822String = "Mon, 26 Mar 2012 02:04:55 GMT";
        DateTime expectedRfc822Date = new DateTime(2012, 3, 26, 02, 04, 55, 0, DateTimeZone.forID("GMT"));

        String rfc850String = "Sunday, 25-Mar-12 20:54:55 GMT";
        DateTime expectedRfc850Date = new DateTime(2012, 3, 25, 20, 54, 55, 0, DateTimeZone.forID("GMT"));

        String ascString = "Tue Mar 27 12:59:55 2012";
        DateTime expectedAscDate = new DateTime(2012, 3, 27, 12, 59, 55, 0, DateTimeZone.forID("GMT"));

        DateTime rfc822Date = DateUtility.parseDateString(rfc822String);
        assertEquals(expectedRfc822Date, rfc822Date);

        DateTime rfc850Date = DateUtility.parseDateString(rfc850String);
        assertEquals(expectedRfc850Date, rfc850Date);

        DateTime ascDate = DateUtility.parseDateString(ascString);
        assertEquals(expectedAscDate, ascDate);
    }

    /**
     * Ensures the toIso8601_DateTime(...) methods produce the same string for a java.util.Date and a long.
     */
    @Test
    public void dateAndMillisIso8601Test() {
        final Calendar now = Calendar.getInstance();
        assertEquals(DateUtility.toIso8601_DateTime(now.getTime()), DateUtility.toIso8601_DateTime(now.getTimeInMillis()));
    }

    /**
     * Tests that parseDate can parse String with just year about and DateUtility provides a formatter is print just year
     * value
     */
    @Test
    public void testParseDate_Year_RoundTrip() {
        String dateString = "1955";
        long year = DateUtility.parseDate(dateString);
        String resultingDateString = DateUtility.toIso8601_year(year);
        assertEquals(dateString, resultingDateString);
    }

    /**
     * Tests that a DateTime object can be formatted to common us date format of: MM-dd-yyyy
     */
    @Test
    public void testToCommonUSDate() {
        DateTime testDate = new DateTime(2012, 3, 24, 0, 0, 0);
        assertEquals("03-24-2012", DateUtility.toCommonUSDate(testDate));
        assertEquals("03-24-2012", DateUtility.toCommonUSDate(testDate.getMillis()));
        assertEquals("03-24-2012", DateUtility.toCommonUSDate(new Date(testDate.getMillis())));
    }

    /**
     * Tests that parseDate and toCommonUSDate can be used perform a round trip to format and parse a date time object
     */
    @Test
    public void testRoundTripCommonUSDate() {
        DateTime testDate = new DateTime(2012, 3, 24, 0, 0, 0);
        assertEquals(testDate.getMillis(), DateUtility.parseDate(DateUtility.toCommonUSDate(testDate)));
    }

    /**
     * Tests that parseDate can parse ISO8601 basic date String (with no separator)
     * and DateUtility provide appropriate method to format datetime String in form of yyyyMMdd
     */
    @Test
    public void testParseDate_BasicISO8601Date() {
        String dateString = "20070826";
        long dateLong = DateUtility.parseDate(dateString);
        DateTime date = new DateTime(dateLong);
        assertEquals("2007", date.year().getAsString());
        assertEquals("8", date.monthOfYear().getAsString());
        assertEquals("26", date.dayOfMonth().getAsString());
        String resultingDateString = DateUtility.toIso8601_BasicDate(dateLong);
        assertEquals(dateString, resultingDateString);
    }

    /**
     * Tests that parseDateString_WithTimeOffset can parse date time string with time offset
     * (ie format: yyyy-MM-ddThh:mm:ss+hh:mm)
     */
    @Test
    public void testParseDateString_WithTimeOffset() {
        String dateTimeToParse = "2015-03-30T16:31:53+00:00";
        DateTime dateTimeResult = DateUtility.parseDateString_WithTimeOffset(dateTimeToParse);
    }
}
