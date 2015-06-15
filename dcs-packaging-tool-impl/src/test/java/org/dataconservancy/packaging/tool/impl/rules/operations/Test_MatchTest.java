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

import java.io.File;

import java.util.Arrays;

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.FileContextImpl;
import org.dataconservancy.packaging.tool.impl.rules.ValueOperation;
import org.dataconservancy.packaging.tool.model.description.TestSpec;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Test_MatchTest {

    private static final FileContext whatever =
            new FileContextImpl(new File("/"), false);

    @Test
    public void matchTest() {
        final String MATCH_STRING = "String";

        Test_Match match = new Test_Match();
        TestSpec params = new TestSpec();
        params.setSpecifier(MATCH_STRING);
        match.setParams(params);
        
        
        ValueOperation valueOp = mock(ValueOperation.class);
        when(valueOp.operate(any(FileContext.class)))
                .thenReturn(new String[] {MATCH_STRING});

        match.setConstraints(new ValueOperation[] {valueOp});

        assertTrue(Arrays.equals(match.operate(whatever), new Boolean[] {true}));

    }

    @Test
    public void nonMatchTest() {

        final String MATCH_STRING = "String";
        final String SOURCE_STRING = "blah";

        Test_Match match = new Test_Match();
        TestSpec params = new TestSpec();
        params.setSpecifier(MATCH_STRING);
        match.setParams(params);
        
        ValueOperation valueOp = mock(ValueOperation.class);
        when(valueOp.operate(any(FileContext.class)))
                .thenReturn(new String[] {SOURCE_STRING});

        match.setConstraints(new ValueOperation[] {valueOp});

        assertTrue(Arrays
                .equals(match.operate(whatever), new Boolean[] {false}));
    }

    @Test
    public void regexTest() {
        final String MATCH_STRING = "String";
        final String REGEX_STRING = "S.?ring";

        Test_Match match = new Test_Match();
        TestSpec params = new TestSpec();
        params.setMatchType(Test_Match.PARAM_MATCHTYPE_REGEX);
        params.setSpecifier(REGEX_STRING);
        match.setParams(params);
        
        ValueOperation valueOp = mock(ValueOperation.class);
        when(valueOp.operate(any(FileContext.class)))
                .thenReturn(new String[] {MATCH_STRING});

        match.setConstraints(new ValueOperation[] {valueOp});

        assertTrue(MATCH_STRING.matches(REGEX_STRING));
        assertTrue(Arrays.equals(match.operate(whatever), new Boolean[] {true}));
    }

    @Test
    public void nonRegexTest() {
        final String MATCH_STRING = "String";
        final String REGEX_STRING = "S.?wrong";

        Test_Match match = new Test_Match();
        TestSpec params = new TestSpec();
        params.setMatchType(Test_Match.PARAM_MATCHTYPE_REGEX);
        params.setSpecifier(REGEX_STRING);
        match.setParams(params);
        
        ValueOperation valueOp = mock(ValueOperation.class);
        when(valueOp.operate(any(FileContext.class)))
                .thenReturn(new String[] {MATCH_STRING});

        match.setConstraints(new ValueOperation[] {valueOp});

        assertFalse(MATCH_STRING.matches(REGEX_STRING));
        assertTrue(Arrays
                .equals(match.operate(whatever), new Boolean[] {false}));
    }
}
