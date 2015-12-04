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

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for PhoneNumberValidator
 */
public class PhoneNumberValidatorTest {

    public PhoneNumberValidator pnv = new PhoneNumberValidator();

    @Test
    public void testValidNumbers(){
        assertTrue(pnv.isValid("410 555 1234"));
        assertTrue(pnv.isValid("410 555-1234"));
        assertTrue(pnv.isValid("(410) 555-1234"));
        assertTrue(pnv.isValid("4105551234"));
        assertTrue(pnv.isValid("+919769321013"));
        assertTrue(pnv.isValid("1 800 MOO COWW"));
        //international number as dialed from the US, which is the default
        assertTrue(pnv.isValid("011 41 44 668 1800"));
    }

    @Test
    public void testValidNumbersNoSpaces() throws Exception {
        assertTrue(pnv.isValid("4105551234"));
        assertTrue(pnv.isValid("410555-1234"));
        assertTrue(pnv.isValid("(410)555-1234"));
        assertTrue(pnv.isValid("4105551234"));
        assertTrue(pnv.isValid("+919769321013"));
        assertTrue(pnv.isValid("1800MOOCOWW"));
        //international number as dialed from the US, which is the default
        assertTrue(pnv.isValid("01141446681800"));
    }

    @Test
    public void testValidExtensions(){
        assertTrue(pnv.isValid("410 555 1234 x346"));
        assertTrue(pnv.isValid("410 555-1234 ext346"));
        assertTrue(pnv.isValid("(410) 555-1234 extension 346"));
        assertTrue(pnv.isValid("4105551234 ext 346"));
        assertTrue(pnv.isValid("4105551234 #346"));
        assertTrue(pnv.isValid("+919769321013 x 3467"));
        assertTrue(pnv.isValid("+919769321013 # 3467"));
    }

    @Test
    public void testInvalidNumbers(){
        assertFalse(pnv.isValid("444 555 1234"));
        assertFalse(pnv.isValid("444 555-1234"));
        assertFalse(pnv.isValid("(444) 555-1234"));
        assertFalse(pnv.isValid("4445551234"));
        assertFalse(pnv.isValid("410 555 1234 MOOOOOO"));
    }

    @Test
    public void testInvalidExtensions(){
        assertFalse(pnv.isValid("410 555 1234 m 123"));
        assertFalse(pnv.isValid("410 555 1234 m123"));
        assertTrue(pnv.isValid("6104589880z"));
    }

    /**
     * Explores the behavior of canonicalization of phone numbers by the phone number validation library for DC-2189.
     * @throws Exception
     */
    @Test
    public void testC14N() throws Exception {

        // A valid number entered with letters is canonicalized to numbers
        String phoneNumberAsEnteredByUser = "610-MOO-MOOO";
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumberAsEnteredByUser, "US");
        String canonicalizedNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

        assertTrue(phoneUtil.isValidNumber(numberProto));
        assertTrue(canonicalizedNumber.endsWith("6666"));

        // This number should be invalid, per my intuition, and per the phone company, but it is accepted as valid.
        phoneNumberAsEnteredByUser = "6104589880z";
        numberProto = phoneUtil.parse(phoneNumberAsEnteredByUser, "US");
        canonicalizedNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

        assertTrue(phoneUtil.isValidNumber(numberProto));
        assertTrue(canonicalizedNumber.endsWith("9880"));

        // A Vietnamese number
        phoneNumberAsEnteredByUser = "+84-901232323";
        assertTrue(pnv.isValid(phoneNumberAsEnteredByUser));
        assertEquals("+84 90 123 23 23", pnv.canonicalize(phoneNumberAsEnteredByUser));

        // USA number, no spaces
        phoneNumberAsEnteredByUser = "6104589880";
        assertTrue(pnv.isValid(phoneNumberAsEnteredByUser));
        assertEquals("+1 610-458-9880", pnv.canonicalize(phoneNumberAsEnteredByUser));

        // Our problematic exemplar, a USA number with a trailing z
        phoneNumberAsEnteredByUser = "6104589880z";
        assertTrue(pnv.isValid(phoneNumberAsEnteredByUser));
        assertEquals("+1 610-458-9880", pnv.canonicalize(phoneNumberAsEnteredByUser));
    }


}
