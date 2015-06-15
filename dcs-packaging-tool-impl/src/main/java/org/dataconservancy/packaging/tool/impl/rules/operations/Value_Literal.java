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
import org.dataconservancy.packaging.tool.impl.rules.FileOperation;
import org.dataconservancy.packaging.tool.impl.rules.ValueOperation;

public class Value_Literal
        implements ValueOperation {

    private String value;

    private final String[] NOTHING = new String[0];

    @Override
    public void setSpecifier(String spec) {
        value = spec;
    }

    @Override
    public void setConstraints(FileOperation... constraints) {
        /* Do nothing, don't care */
    }

    @Override
    public String[] operate(FileContext fileContext) {

        return value != null ? new String[] {value} : NOTHING;

    }

}
