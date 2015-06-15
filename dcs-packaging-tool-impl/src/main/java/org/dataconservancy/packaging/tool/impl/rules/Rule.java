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

import java.util.List;

import org.dataconservancy.packaging.tool.model.description.Action;


public interface Rule {
    /**
     * Determine if this rule applies to a given filesystem entity
     *
     * @param candidate The FileContext to check to see if the rule applies to.
     * @return True if the rule applies, false otherwise.
     */
    public boolean select(FileContext candidate);
 
    /**
     * Include/Exclude
     * @return The action applied by this rule
     */
    public Action getAction();
 
    /**
     * Get the mapping to a package description Artifact for the given File
     * selected by this rule.
     *
     * @param candidate The FileContext to get the mapping for.
     * @return A list of the mappings from the FileContext to possible artifacts.
     */
    public List<Mapping> getMappings(FileContext candidate);
}
