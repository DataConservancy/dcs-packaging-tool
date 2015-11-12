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
package org.dataconservancy.packaging.gui.model;


import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests that a PropertyType can be round tripped to and from json.
 */
public class UserDefinedPropertyTypeJSONBuilderTest {

    private PropertyType testPropertyType;
    private final String label = "label";
    private final String description = "description";
    private final String domainPredicate = "uri:relationship:test";

    @Before
    public void setup() throws URISyntaxException {
        testPropertyType = new PropertyType();
        testPropertyType.setLabel(label);
        testPropertyType.setDescription(description);
        testPropertyType.setDomainPredicate(new URI(domainPredicate));
        testPropertyType.setPropertyValueType(PropertyValueType.URI);
    }

    @Test
    public void testRoundTrip() {
        JSONObject propertyTypeObject = UserDefinedPropertyTypeJSONBuilder.serializeProperty(testPropertyType);
        assertNotNull(propertyTypeObject);

        PropertyType roundTrippedPropertyType = UserDefinedPropertyTypeJSONBuilder.deserializePropertyType(propertyTypeObject);
        assertNotNull(roundTrippedPropertyType);

        assertEquals(testPropertyType, roundTrippedPropertyType);
    }
}
