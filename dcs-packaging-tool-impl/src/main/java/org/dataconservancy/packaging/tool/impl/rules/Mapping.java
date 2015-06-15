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

import java.net.URI;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.packaging.tool.model.description.ArtifactTypeSpec;

public interface Mapping {
    ArtifactTypeSpec getType();
    
    String getSpecifier();
    
    /**
     * Maps property name to property value
     *
     * @return A map of all the properties associated with this mapping.
     */
    Map<String, List<String>> getProperties();
 
    /**
     * Maps relationship name to the entity that is the target of the
     * relationship.
     * <p>
     * The implementation of PackageDescriptionCreator will need
     * to resolve the filesystem entity to substitute in corresponding packaging
     * artifact identifier when forming the resulting package artifact.
     * </p>
     *
     * @return  a Map of the relationship name to the entity that is the target of the
     * relationship.
     */
    Map<String, Set<URI>> getRelationships();
}
