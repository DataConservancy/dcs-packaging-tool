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
import org.dataconservancy.packaging.tool.impl.rules.FileOperation;
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;
import org.dataconservancy.packaging.tool.model.description.TestSpec;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Test_CountTest {

    private static final FileContext whatever =
            new FileContextImpl(new File("/"), false);

    /* Number of files below minimum threshold should fail */
    @Test
    public void belowMinTest() {
        final Integer MIN = 3;

        Test_Count count = new Test_Count();
        TestSpec spec = new TestSpec();
        spec.setMin(MIN);
        count.setParams(spec);
        
        count.setConstraints(new NumberOfFiles(MIN - 1));
        assertTrue(Arrays
                .equals(count.operate(whatever), new Boolean[] {false}));
    }

    /* Number of files at minimum threshold should succeed */
    @Test
    public void atMinTest() {
        final Integer MIN = 3;

        Test_Count count = new Test_Count();
        TestSpec spec = new TestSpec();
        spec.setMin(MIN);
        count.setParams(spec);
        
        count.setConstraints(new NumberOfFiles(MIN));
        assertTrue(Arrays.equals(count.operate(whatever), new Boolean[] {true}));
    }

    /* Number of files above minimum threshold should succeed */
    @Test
    public void aboveMinTest() {
        final Integer MIN = 3;

        Test_Count count = new Test_Count();
        TestSpec spec = new TestSpec();
        spec.setMin(MIN);
        count.setParams(spec);
        
        count.setConstraints(new NumberOfFiles(MIN + 1));
        assertTrue(Arrays.equals(count.operate(whatever), new Boolean[] {true}));
    }

    /* Number of files at max should succeed */
    @Test
    public void atMaxTest() {
        final Integer MAX = 3;
        

        Test_Count count = new Test_Count();
        TestSpec spec = new TestSpec();
        spec.setMax(MAX);
        count.setParams(spec);
        
        count.setConstraints(new NumberOfFiles(MAX));
        assertTrue(Arrays.equals(count.operate(whatever), new Boolean[] {true}));
    }

    /* Number of files above max should fail */
    @Test
    public void atboveMaxTest() {
        final Integer MAX = 3;

        Test_Count count = new Test_Count();
        TestSpec spec = new TestSpec();
        spec.setMax(MAX);
        count.setParams(spec);
        
        count.setConstraints(new NumberOfFiles(MAX + 1));
        assertTrue(Arrays
                .equals(count.operate(whatever), new Boolean[] {false}));
    }

    @Test
    public void multipleOperandsAtMaxTest() {
        final Integer MAX = 3;

        Test_Count count = new Test_Count();
        TestSpec spec = new TestSpec();
        spec.setMax(MAX);
        count.setParams(spec);
        
        FileOperation[] operands = new FileOperation[MAX];
        for (int i = 0; i < operands.length; i++) {
            operands[i] = new NumberOfFiles(1);
        }

        count.setConstraints(operands);
        assertTrue(Arrays.equals(count.operate(whatever), new Boolean[] {true}));
    }

    @Test
    public void multipleOperandsAboveMaxTest() {
        final Integer MAX = 3;

        Test_Count count = new Test_Count();
        TestSpec spec = new TestSpec();
        spec.setMax(MAX);
        count.setParams(spec);
        
        FileOperation[] operands = new FileOperation[MAX + 1];
        for (int i = 0; i < operands.length; i++) {
            operands[i] = new NumberOfFiles(1);
        }

        count.setConstraints(operands);
        assertTrue(Arrays
                .equals(count.operate(whatever), new Boolean[] {false}));
    }

    private class NumberOfFiles
            implements FileOperation {

        private final int numberOfFiles;

        public NumberOfFiles(int number) {
            numberOfFiles = number;
        }

        @Override
        public void setConstraints(TestOperation<?>... constraints) {
            /* Nothing */
        }

        @Override
        public File[] operate(FileContext fileContext) {
            File[] result = new File[numberOfFiles];
            for (int i = 0; i < numberOfFiles; i++) {
                result[i] = new File("/");
            }

            return result;
        }
    }
}
