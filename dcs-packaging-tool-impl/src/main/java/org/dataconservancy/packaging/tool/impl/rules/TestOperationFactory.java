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

package org.dataconservancy.packaging.tool.impl.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.packaging.tool.impl.rules.operations.Test_And;
import org.dataconservancy.packaging.tool.impl.rules.operations.Test_Count;
import org.dataconservancy.packaging.tool.impl.rules.operations.Test_Match;
import org.dataconservancy.packaging.tool.impl.rules.operations.Test_Not;
import org.dataconservancy.packaging.tool.impl.rules.operations.Test_Or;
import org.dataconservancy.packaging.tool.model.description.TestSpec;

/** Produces fully configured {@link TestOperation} impls based on a given specification. */
public class TestOperationFactory {

    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends TestOperation<?>>> operationMap =
            new HashMap<String, Class<? extends TestOperation<?>>>() {

                {
                    put(TestOperation.AND, Test_And.class);
                    put(TestOperation.OR, Test_Or.class);
                    put(TestOperation.NOT, Test_Not.class);
                    put(TestOperation.MATCH, Test_Match.class);
                    put(TestOperation.COUNT, Test_Count.class);
                }
            };

    public static TestOperation<?> getOperation(TestSpec spec) {
        String opName = spec.getOperation();

        TestOperation<?> testOp = null;

        if (!operationMap.containsKey(opName)) {
            throw new RuntimeException("No such test operation: " + opName);
        }

        try {
            testOp = operationMap.get(opName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        populate(testOp, spec);

        return testOp;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void populate(TestOperation testOp, TestSpec spec) {
        testOp.setParams(spec);

        if (spec.getFile() != null) {
            ((TestOperation<FileOperation>) testOp)
                    .setConstraints(FileOperationFactory.getOperation(spec
                            .getFile()));
        } else if (spec.getValue() != null) {
            ValueOperation valOp =
                    ValueOperationFactory.getOperation(spec.getValue());

            ((TestOperation<ValueOperation>) testOp).setConstraints(valOp);
        } else if (spec.getTest() != null
                && !spec.getTest().isEmpty()) {
            ArrayList<TestOperation<?>> tests =
                    new ArrayList<TestOperation<?>>();
            for (TestSpec testSpec : spec.getTest()) {
                tests.add(TestOperationFactory.getOperation(testSpec));
            }
            ((TestOperation<TestOperation<?>>) testOp).setConstraints(tests
                    .toArray(new TestOperation<?>[0]));
        }
    }
}
