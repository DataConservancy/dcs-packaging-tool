/*
 * Copyright 2013 Johns Hopkins University
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

import org.dataconservancy.packaging.tool.model.PropertyValidationResult;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that the phone number property validator is working correctly.
 */
public class PhoneNumberPropertyValidatorTest {

    @Test
    public void testValidation() {
        PhoneNumberPropertyValidator underTest = new PhoneNumberPropertyValidator();

        PropertyValidationResult result = underTest.validate("1234");
        assertNotNull(result);
        assertFalse(result.isValid());

        result = underTest.validate("1234567890");
        assertNotNull(result);
        assertFalse(result.isValid());

        result = underTest.validate("18886515908");
        assertNotNull(result);
        assertTrue(result.isValid());

        result = underTest.validate("18886515908x1624306");
        assertNotNull(result);
        assertTrue(result.isValid());
    }
}
