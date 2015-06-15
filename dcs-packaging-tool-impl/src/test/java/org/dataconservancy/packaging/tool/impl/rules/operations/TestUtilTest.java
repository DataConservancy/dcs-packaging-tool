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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import org.dataconservancy.packaging.tool.model.description.TestParam;
import org.dataconservancy.packaging.tool.model.description.TestParams;
import org.dataconservancy.packaging.tool.model.description.TestSpec;
import org.dataconservancy.packaging.tool.model.description.ValueSpec;
import org.dataconservancy.packaging.tool.model.description.ValueType;

public class TestUtilTest {

    /* Verify that direct param value can be read */
    @Test
    public void directParamTest() {
        final String VALUE = "value#directParamTest";

        TestSpec spec = new TestSpec();

        /* We use 'matchType' param */
        spec.setMatchType(VALUE);

        assertEquals(VALUE, TestUtil.getParam("matchType", spec, null));
    }

    /* Verify that value op can be read */
    @Test
    public void valueOpParamTest() {
        final String VALUE = "value#valueOpParamTest";

        TestSpec spec = new TestSpec();

        spec.setParams(new TestParams());
        TestParam param = new TestParam();

        ValueSpec valueSpec = new ValueSpec();
        valueSpec.setType(ValueType.LITERAL);
        valueSpec.setSpecifier(VALUE);

        param.setValue(valueSpec);
        param.setName("matchType");

        spec.getParams().getParam().add(param);

        TestUtil.getParam("matchType", spec, null);

        assertEquals(VALUE, TestUtil.getParam("matchType", spec, null));
    }

    /* Verify that a direct param is preferred to valueOp */
    @Test
    public void directParamPreferenceTest() {
        final String VALUE_PREFERRED =
                "value/directParamPreferenceTest#Preferred";
        final String VALUE_OTHER = "value/directParamPreferenceTest#OTHER";

        TestSpec spec = new TestSpec();

        /* We use 'matchType' param */
        spec.setMatchType(VALUE_PREFERRED);

        spec.setParams(new TestParams());
        TestParam param = new TestParam();

        ValueSpec valueSpec = new ValueSpec();
        valueSpec.setType(ValueType.LITERAL);
        valueSpec.setSpecifier(VALUE_OTHER);

        param.setValue(valueSpec);
        param.setName("matchType");

        spec.getParams().getParam().add(param);

        TestUtil.getParam("matchType", spec, null);

        assertEquals(VALUE_PREFERRED,
                     TestUtil.getParam("matchType", spec, null));
    }
    
    /* Verify that null values don't crash, and just return null */
    @Test
    public void nullTest() {

        TestSpec spec = new TestSpec();

        assertEquals(null, TestUtil.getParam("matchType", spec, null));
    }
    
    /* Nonexistant params should simply return null */
    @Test
    public void nonexistantParamTest() {
        TestSpec spec = new TestSpec();

        assertEquals(null, TestUtil.getParam("thisDoesn'tExist", spec, null));
    }
}