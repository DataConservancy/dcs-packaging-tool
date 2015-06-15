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

import java.util.ArrayList;

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;
import org.dataconservancy.packaging.tool.impl.rules.ValueOperation;
import org.dataconservancy.packaging.tool.model.description.TestSpec;

import static org.dataconservancy.packaging.tool.impl.rules.operations.TestUtil.getParam;

/**
 * Implements the "Match" test operation.
 * <p>
 * Matches the value returned by a {@link ValueOperation} with a regex or simple
 * equality.
 * </p>
 * <h2>Params</h2> One of the following params must be provided:
 * <dl>
 * <dt>matchType</dt>
 * <dd>Type of match to perform, possible values are
 * {@link #PARAM_MATCHTYPE_REGEX} and {@link #PARAM_MATCHTYPE_VALUE}</dd>
 * <dt>specifier</dt>
 * <dd>Exact value or regular expression to match against. Regular expressions
 * are expressed in terms of the Java regular expression syntax</dd>
 * </dl>
 */
public class Test_Match
        implements TestOperation<ValueOperation> {

    public static final String PARAM_MATCHTYPE_REGEX = "regex";

    public static final String PARAM_MATCHTYPE_VALUE = "value";

    public static final String PARAM_SPECIFIER = "specifier";

    public static final String PARAM_MATCHTYPE = "matchType";

    private String regex = null;

    private String equals = null;

    private TestSpec params;

    private ValueOperation[] valueOps;

    @Override
    public void setParams(TestSpec params) {
        this.params = params;
    }

    @Override
    public void setConstraints(ValueOperation... constraints) {
        valueOps = constraints;
    }

    @Override
    public Boolean[] operate(FileContext fileContext) {

        parseParams(fileContext);

        ArrayList<Boolean> results = new ArrayList<Boolean>();
        for (ValueOperation valueOp : valueOps) {
            for (String value : valueOp.operate(fileContext)) {
                results.add((regex != null && value.matches(regex))
                        || (equals != null && value.equals(equals)));
            }
        }

        return results.toArray(new Boolean[0]);
    }

    private void parseParams(FileContext cxt) {
        if (PARAM_MATCHTYPE_REGEX.equals(params.getMatchType())) {
            regex = getParam(PARAM_SPECIFIER, params, cxt);
        } else {
            equals = getParam(PARAM_SPECIFIER, params, cxt);
        }
    }
}
