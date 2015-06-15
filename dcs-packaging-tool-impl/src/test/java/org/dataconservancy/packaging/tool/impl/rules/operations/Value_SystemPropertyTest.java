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

package org.dataconservancy.packaging.tool.impl.rules.operations;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Value_SystemPropertyTest {

    @Test
    public void systemPropertyValueTest() {
        final String PROPERTY_NAME = "test.Value_SystemPropertyTest.systemPropertyValueTest";
        final String PROPERTY_VALUE = "Value";
        Value_SystemProperty valueOp = new Value_SystemProperty();
        valueOp.setSpecifier(PROPERTY_NAME);

        System.setProperty(PROPERTY_NAME, PROPERTY_VALUE);

        assertTrue(Arrays.equals(new String[] {PROPERTY_VALUE},
                                 valueOp.operate(null)));
    }
    
    @Test
    public void nullSystemPropertyTest() {
        final String PROPERTY_NAME = "test.Value_SystemPropertyTest.nullSystemPropertyTest";
        
        Value_SystemProperty valueOp = new Value_SystemProperty();
        valueOp.setSpecifier(PROPERTY_NAME);
        
        assertEquals(0, valueOp.operate(null).length);
    }
    
    /* Verifies that empty strings are treated as null */
    @Test
    public void emptySystemPropertyTest() {
        final String PROPERTY_NAME = "test.Value_SystemPropertyTest.emptySystemPropertyTest";
        
        Value_SystemProperty valueOp = new Value_SystemProperty();
        valueOp.setSpecifier(PROPERTY_NAME);
        
        System.setProperty(PROPERTY_NAME, "");
        assertEquals(0, valueOp.operate(null).length);
    }
}
