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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * Utility for parsing and converting dates.
 * <p>
 * Provides means to parse and convert <a
 * href="http://www.iso.org/iso/date_and_time_format">ISO 8601</a> and <a
 * href="http://www.rfc-editor.org/rfc/rfc822.txt">RFC 822</a> formatted dates
 * into miliseconds since the epoch.
 *
 */
public class DateUtility {


    private static final String RFC_822_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    private static final String RFC_850_FORMAT = "EEEE, dd-MMM-yy HH:mm:ss z";
    private static final String ASC_FORMAT = "EEE MMM dd HH:mm:ss yyyy";
    private static final String COMMON_US_DATE_FORMAT ="MM-dd-yyyy";

    /*
     * Relatively strict formatter for writing ISO 8601 dateTimes with
     * miliseconds.
     */
    private static final DateTimeFormatter iso8601_DateTime_Formatter =
            ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    /*
     * Relatively strict formatter for writing ISO 8601 dateTimes with
     * no miliseconds.
     */
    private static final DateTimeFormatter iso8601_DateTimeNoMillis_Formatter =
            ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

    /*
     * Relatively strict formatter for writing ISO 8601 basic date in format yyyyMMdd
     */
    private static final DateTimeFormatter iso8601_BasicDate_Formatter =
            ISODateTimeFormat.basicDate();

    /*
     * Relatively strict formatter for writing ISO 8601 year in format yyyy
     */
    private static final DateTimeFormatter iso8601_Year_Formatter =
            ISODateTimeFormat.year();

    /* Relatively liberal parser for ISO 8601 dateTimes */
    private static final DateTimeFormatter iso8601Parser =
            ISODateTimeFormat.dateTimeParser();

    private static final DateTimeFormatter commonUS_DateOnly_Formatter =
            DateTimeFormat.forPattern(COMMON_US_DATE_FORMAT);

    /*
      * Using java DateFormat instead of Joda Time here, as joda time parser
      * doesn't handle human-readable time zone names.
      *
      * Must use separate DateFormat instances for parsing because parsing can change the TimeZone.
      */
    private static final DateFormat rfc822_fmt;
    private static final DateFormat rfc850_fmt;
    private static final DateFormat asc_fmt;
    private static final DateFormat common_US_date_fmt;

    static {
        rfc822_fmt = getDateFormat(RFC_822_FORMAT);
        rfc850_fmt = getDateFormat(RFC_850_FORMAT);
        asc_fmt = getDateFormat(ASC_FORMAT);
        common_US_date_fmt = getDateFormat(COMMON_US_DATE_FORMAT);
    }
    
    private static DateFormat getDateFormat(String format) {
        DateFormat fmt =  new SimpleDateFormat(format);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return fmt;
    }
    
     /**
     * Parse a date string into milliseconds since the epoch.
     * <p>
     * Automatically determines date format in order to locate the correct
     * parser. Currently, the following date time string formats are supported
      * <ul>
      *     <li>ISO 8601 - year</li>
      *     <li>ISO 8601 - basic date with no separators: yyyyMMdd</li>
      *     <li>RFC 822</li>
      *     <li>RFC 850</li>
      *     <li>Common US date format: MM-dd-yyyy</li>
      * </ul>
     * <p>
     * 
     * @param date
     *        String containing date in RFC 822 or ISO 8601 format.
     * @return long containing time in milliseconds since the epoch. If the String could not be parsed,
      * -1 value is returned.
     */
    public static long parseDate(String date)
    {
        long time = -1;
        
        try {
            time = getDateFormat(RFC_822_FORMAT).parse(date).getTime();
        } catch (ParseException e) {
        }
        
        //If the date is null then it wasn't an rfc822 date
        if (time == -1) {
            try {
                time = getDateFormat(RFC_850_FORMAT).parse(date).getTime();
            } catch (ParseException e) {
            }
        }
        
        //If the date is still null try asc parsing
        if (time == -1) {
            try {
                time = getDateFormat(ASC_FORMAT).parse(date).getTime();
            } catch (ParseException e) {
            }
        }

        if (time == -1) {
            try {
                time = parseIso8601BasicDate(date).getMillis();
            } catch (IllegalArgumentException e) {
            }
        }

        if (time == -1) {
            try {
                time = iso8601Parser.parseMillis(date);
            } catch (IllegalArgumentException e){
            }
        }

        if (time == -1) {
            try {
                time = commonUS_DateOnly_Formatter.parseMillis(date);
            } catch (IllegalArgumentException e) {
            }
        }
        
        return time;
    }

    /**
     * Parses Iso8601 basic formatted date string (yyyyMMdd) into a DateTime object.
     * @param date The string in Iso8601 basic date format
     * @return A DateTime object representing the date in the String,
     * @throws IllegalArgumentException if the date could not be parsed.
     */
    public static DateTime parseIso8601BasicDate(String date) {
        return iso8601_BasicDate_Formatter.parseDateTime(date);

    }
    /**
     * Parses a Rfc822 formatted date string into a DateTime object
     * @param date The string Rfc822 format.
     * @return A DateTime object representing the date in the string, or null if the string isn't properly formatted. 
     */
    public static DateTime parseRfc822Date(String date) {
        DateTime dateTime = null;
        try {
            dateTime = new DateTime(getDateFormat(RFC_822_FORMAT).parse(date).getTime()).withZone(DateTimeZone.forID("GMT"));
        } catch (ParseException e) {
            //The string was not formatted correctly, ensure null is returned
            dateTime = null;
        }
        return dateTime;
    }
    
    /**
     * Parses a Rfc850 formatted date string into a DateTime object
     * @param date The string Rfc850 format.
     * @return A DateTime object representing the date in the string, or null if the string isn't properly formatted. 
     */
    public static DateTime parseRfc850Date(String date) {
        DateTime dateTime = null;
        try {
            dateTime = new DateTime(getDateFormat(RFC_850_FORMAT).parse(date).getTime()).withZone(DateTimeZone.forID("GMT"));
        } catch (ParseException e) {
            //The string was not formatted correctly, ensure null is returned
            dateTime = null;
        }
        return dateTime;
    }
    
    /**
     * Parses an ASC formatted date string into a DateTime object
     * @param date The string Asc format.
     * @return A DateTime object representing the date in the string, or null if the string isn't properly formatted. 
     */
    public static DateTime parseAscTime(String date) {
        DateTime dateTime = null;
        try {
            
            Date parsedDate = getDateFormat(ASC_FORMAT).parse(date);
            dateTime = new DateTime(parsedDate.getTime()).withZone(DateTimeZone.forID("GMT"));
        } catch (ParseException e) {
            //The string was not formatted correctly, ensure null is returned
            dateTime = null;
        }
        return dateTime;
    }

    /**
     * Parses a string of the following formats:
     * <ul>
     *     <li>Rfc822</li>
     *     <li>Rfc850</li>
     *     <li>Asc</li>
     *     <li>ISO8601</li>
     *     <li>Common US date format MM-dd-yyyy</li>
     * </ul>
     * into DateTime object with GMT timezone
     *
     * @param date The formatted string to parse
     * @return A date time object with the formatted string, or null if the string doesn't match any of the formats.
     */
    public static DateTime parseDateString(String date) {
        long millis = parseDate(date);
        if (millis != -1) {
            return new DateTime(millis).withZone(DateTimeZone.forID("GMT"));
        } else {
            return null;
        }
    }

    /**
     * Parses a string of the following formats:
     * <ul>
     *     <li>Rfc822</li>
     *     <li>Rfc850</li>
     *     <li>Asc</li>
     *     <li>ISO0861</li>
     *     <li>common US Date (MM-dd-yyyy)</li>
     * </ul>
     * into a DateTime object with timeoff set value.
     * @param date The formatted string to parse
     * @return A date time object with the formatted string, or null if the string doesn't match any of the formats.
     */
    public static DateTime parseDateString_WithTimeOffset(String date) {
        long millis = parseDate(date);
        if (millis != -1) {
            return new DateTime(millis);
        } else {
            return null;
        }
    }
    /**
     * Returns a dateTime value into ISO 8601 format.
     * <p>
     * Will return the UTC date to the millisecond precision. e.g.
     * <code>2010-06-22T07:31.005Z</code>
     * <p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toIso8601_DateTime(long timestamp) {
        return iso8601_DateTime_Formatter.print(timestamp);
    }

    /**
     * Returns a dateTime value into ISO 8601 format.
     * <p>
     * Will return the UTC date to the millisecond precision. e.g.
     * <code>2010-06-22T07:31.005Z</code>
     * <p>
     *
     * @param date the Date
     * @return formatted date string.
     */
    public static String toIso8601_DateTime(Date date) {
        return iso8601_DateTime_Formatter.print(new DateTime(date));
    }

    /**
     * Returns a dateTime value into ISO 8601 format.
     * <p>
     * Will return the UTC date to the second precision. e.g.
     * <code>2010-06-22T07:31Z</code>
     * <p>
     *
     * @param date the Date
     * @return formatted date string.
     */
    public static String toIso8601_DateTimeNoMillis(Date date) {
        return iso8601_DateTimeNoMillis_Formatter.print(new DateTime(date));
    }

    /**
     * Returns a dateTime value into ISO 8601 format.
     * <p>
     * Will return the UTC date to the second precision. e.g.
     * <code>2010-06-22T07:31Z</code>
     * <p>
     *
     * @param date the Date
     * @return formatted date string.
     */
    public static String toIso8601_DateTimeNoMillis(long date) {
        return iso8601_DateTimeNoMillis_Formatter.print(new DateTime(date));
    }

    /**
     * Returns a dateTime value into ISO 8601 Basic date format (yyyyMMdd).
     * <p>
     * Will return the UTC date
     * <code>20100617</code>
     * <p>
     *
     * @param date the Date
     * @return formatted date string.
     */
    public static String toIso8601_BasicDate(long date) {
        return iso8601_BasicDate_Formatter.print(new DateTime(date));
    }

    /**
     * Returns a dateTime value into ISO 8601 year format (yyyy).
     * <p>
     * Will return the UTC date
     * <code>2010</code>
     * <p>
     *
     * @param date the Date
     * @return formatted date string.
     */
    public static String toIso8601_year(long date) {
        return iso8601_Year_Formatter.print(new DateTime(date));
    }

    /**
     * Returns a dateTime value into RFC 822 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * <p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toRfc822(long timestamp) {
        return rfc822_fmt.format(new Date(timestamp));
    }
    
    /**
     * Returns a dateTime value into RFC 850 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * <p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toRfc850(long timestamp) {
        return rfc850_fmt.format(new Date(timestamp));
    }
    
    /**
     * Returns a dateTime value into Asc format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * <p>
     * 
     * @param timestamp
     *        miliseconds since the epoch.
     * @return formatted date string.
     */
    public static String toAsc(long timestamp) {
        return asc_fmt.format(new Date(timestamp));
    }

    /**
     * Returns a dateTime value into RFC 822 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * <p>
     * 
     * @param date object representing the date to format
     * @return formatted date string.
     */
    public static String toRfc822(DateTime date) {
        return toRfc822(date.getMillis());
    }
    
    /**
     * Returns a dateTime value into RFC 850 format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * <p>
     * 
     * @param date object representing the date to format
     * @return formatted date string.
     */
    public static String toRfc850(DateTime date) {
        return toRfc850(date.getMillis());
    }
    
    /**
     * Returns a dateTime value into Asc format.
     * <p>
     * Will return a date in <em>second</em> precision, truncating amy
     * fractional seconds.
     * </p>
     * 
     * @param date object representing the date to format
     * @return formatted date string.
     */
    public static String toAsc(DateTime date) {
        return toAsc(date.getMillis());
    }

    /**
     * Returns a date only value in common US date format.
     * <p>
     * Truncates any time value provided in the date object.
     * @param date {@link org.joda.time.DateTime} object presenting the date to be formatted
     * @return date string in format MM-dd-yyyy
     */
    public static String toCommonUSDate(DateTime date) {
        return common_US_date_fmt.format(new Date(date.getMillis()));
    }

    /**
     * Returns a date only value in common US date format.
     * <p>
     * Truncate any time value in the date object.
     * @param date {@link java.util.Date} object presenting the date to be formatted
     * @return date string in format MM-dd-yyyy
     */
    public static String toCommonUSDate(Date date) {
        return common_US_date_fmt.format(date.getTime());
    }

    /**
     * Returns a date only value in common US date format.
     * <p>
     * Truncates any time value provided along with the date.
     * @param date long value presenting the date to be formatted
     * @return date string in format MM-dd-yyyy
     */
    public static String toCommonUSDate(long date) {
        return common_US_date_fmt.format(date);
    }

    /**
     * Returns the current time in milliseconds.
     * 
     * @return long containing time in milliseconds since the epoch.
     */
    public static long now() {
        return new Instant().getMillis();
    }

}
