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

import java.io.File;

/**
 * Operation that returns a series of filesystem entities.
 * <p>
 * Results of this operation may be further constrained by TestOperations that
 * operate in the context of each file returned by the FileOperation. Only files
 * for whom the constraining TestOperations sevaluate to True are returned.
 * </p>
 */
public interface FileOperation extends Operation<File, TestOperation<?>> {
    public static final String ANCESTORS = "ancestors";
    public static final String CHILDREN = "children";
    public static final String DESCENDENTS = "descendents";
    public static final String PARENT = "parent";
    public static final String SELF = "self";
}
