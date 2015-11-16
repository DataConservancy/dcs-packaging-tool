/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.api.generator;

/**
 * Describes the type of resource being included in a package. The intended use of this is to
 * support the assigning of locations withing the package to resources of different types.
 */
public enum PackageResourceType {
    /** Package data files/content */
    DATA,

    /** The type for an ORE-ReM file */
    ORE_REM,

    /** Ontologies used in building the package */
    ONTOLOGY,

    /** Other package-related metadata (e.g.business objects, etc) */
    METADATA,

    /** The Package State file */
    PACKAGE_STATE;

}
