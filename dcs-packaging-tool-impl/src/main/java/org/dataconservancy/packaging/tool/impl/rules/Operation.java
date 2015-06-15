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

import org.dataconservancy.packaging.tool.impl.rules.operations.OperationException;

/**
 * Operations operate in the context of a filesystem entity.
 *
 * @param R
 *        Return type when applying the operation to a given filesystem entity
 * @param C
 *        Additional operations that constrain the results returned by this
 *        operation.
 */
public interface Operation<R, C extends Operation<?, ?>> {
	/**
     * Set the constraints of this operation.
     * <p>
     * Constraint setup is intended to occur at initialization, in response to a
     * specific Package Description Preferences file that defines the
     * constraints.
     * </p>
     *
     * @param constraints The constraints to set on the operation
     */
    void setConstraints(C... constraints);
 
    /**
     * Evaluate the operation at runtime given a filesystem entity context.
     *
     * @param fileContext The file context to perform the operation on.
     * @return The result(s) of the performed operation.
     */
    R[] operate(FileContext fileContext) throws OperationException;
}
