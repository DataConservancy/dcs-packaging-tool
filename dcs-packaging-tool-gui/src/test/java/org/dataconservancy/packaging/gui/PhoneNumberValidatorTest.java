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


package org.dataconservancy.packaging.gui;

import org.dataconservancy.packaging.gui.util.PhoneNumberValidator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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
    }
}
