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
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;
import org.dataconservancy.packaging.tool.model.description.TestSpec;

/**
 * Implements the "And" test operation.
 * <p>
 * Returns a single true value if all operands return true values, false if any
 * operand returns any false value.
 * </p>
 * 
 */
public class Test_And implements TestOperation<TestOperation<?>> {

	private TestOperation<?>[] operands;

	@Override
	public void setParams(TestSpec spec) {
		/* No params */
	}

	@Override
	public void setConstraints(TestOperation<?>... constraints) {
		this.operands = constraints;
	}

	@Override
	public Boolean[] operate(FileContext fileContext) {
		boolean truthValue = true;

		operandLoop: for (TestOperation<?> operand : operands) {
			for (boolean truth : operand.operate(fileContext)) {
				if (!truth) {
					truthValue = false;
					break operandLoop;
				}
			}
		}

		return new Boolean[] {truthValue};
	}

}
