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

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.Operation;
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;
import org.dataconservancy.packaging.tool.model.description.TestSpec;

/**
 * Implements the "Count" test operation.
 * <p>
 * Counts the number of items returned by the given {@link Operation} and
 * determines if that number fits between a minimum and maximum. Returns a
 * single boolean value.
 * </p>
 * <h2>Params</h2> One of the following params must be provided:
 * <dl>
 * <dt>min</dt>
 * <dd>Minimum number of files that must be encountered in order for a true
 * return value</dd>
 * <dt>max</dt>
 * <dd>Maximum number of files that may be encountered in order for a true
 * return value</dd>
 * </dl>
 * 
 */
public class Test_Count implements TestOperation<Operation<?,?>> {
    
	public static final String PARAM_MIN = "min";
	public static final String PARAM_MAX = "max";
	
	private TestSpec params;

	private int min = 0;
	private int max = Integer.MAX_VALUE;

	private Operation<?, ?>[] ops;

	@Override
	public void setParams(TestSpec spec) {
	    params = spec;
	    min = getParam(PARAM_MIN, min);
	    max = getParam(PARAM_MAX, max);
	}
	
	
	@Override
	public Boolean[] operate(FileContext fileContext) {
		int count = 0;
		for (Operation<?,?> op :ops) {
			count += op.operate(fileContext).length;
		}
		
		return (new Boolean[] { count >= min && count <= max });
	}

	@Override
	public void setConstraints(Operation<?, ?>... constraints) {
		ops = constraints;
	}
	
	private int getParam(String paramName, int defaultValue) {
	    String param = TestUtil.getParam(paramName, params, null);
	    if (param != null) {
	        return Integer.valueOf(param);
	    }
	    
	    return defaultValue;
	}

}
