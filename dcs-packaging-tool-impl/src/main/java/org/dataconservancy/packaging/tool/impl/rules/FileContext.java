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

/** Represents a file and an optional root directory for context */
public interface FileContext {

    /** Root directory of a hierarchy, may be null
     *
     * @return The file representing the root of the file.
     */
    File getRoot();
    
    /** The file name itself
     *
     * @return The file representing the file context.
     */
    File getFile();

    /** Boolean flag that denotes if the context lives on a file path that is currently marked ignored by the rules.
     *
     * @return True if the file path is ignored, false otherwise
     */
    boolean isIgnored();

    void setIgnored(boolean ignored);
}
