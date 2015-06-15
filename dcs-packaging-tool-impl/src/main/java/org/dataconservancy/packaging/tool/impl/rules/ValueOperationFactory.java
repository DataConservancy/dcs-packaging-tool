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

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.packaging.tool.impl.rules.operations.Value_FileMetadata;
import org.dataconservancy.packaging.tool.impl.rules.operations.Value_Literal;
import org.dataconservancy.packaging.tool.impl.rules.operations.Value_SystemProperty;
import org.dataconservancy.packaging.tool.model.description.ValueSpec;
import org.dataconservancy.packaging.tool.model.description.ValueType;

/**
 * Produces fully configured {@link ValueOperation} based on the supplied
 * specification
 */
public class ValueOperationFactory {

    @SuppressWarnings("serial")
    private static final Map<ValueType, Class<? extends ValueOperation>> operationMap =
            new HashMap<ValueType, Class<? extends ValueOperation>>() {

                {
                    put(ValueType.FILE_METADATA, Value_FileMetadata.class);
                    put(ValueType.LITERAL, Value_Literal.class);
                    put(ValueType.PROPERTY, Value_SystemProperty.class);
                }
            };

    public static ValueOperation getOperation(ValueSpec spec) {

        try {
            ValueOperation valueOp =
                    operationMap.get(spec.getType()).newInstance();
            valueOp.setSpecifier(spec.getSpecifier());
            return valueOp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
