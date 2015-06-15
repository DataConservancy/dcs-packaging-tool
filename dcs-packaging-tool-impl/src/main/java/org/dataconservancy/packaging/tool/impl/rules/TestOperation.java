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

import org.dataconservancy.packaging.tool.model.description.TestSpec;

/**
 * Returns true or false after applying some test operation to the results
 * returned by the supplied constraints.
 */
public interface TestOperation<C extends Operation<?, ?>>
        extends Operation<Boolean, C> {

    public static final String OR = "or";

    public static final String AND = "and";
    
    public static final String NOT = "not";

    public static final String MATCH = "match";

    public static final String COUNT = "count";    

    public void setParams(TestSpec spec);

}
