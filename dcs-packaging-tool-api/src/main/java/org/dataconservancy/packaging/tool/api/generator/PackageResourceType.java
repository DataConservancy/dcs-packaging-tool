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

import java.util.Arrays;

/**
 * Describes the type of resource being included in a package, and their relative location inside the package. The
 * intended use of this is to support the assigning of locations withing the package to resources of different types.
 */
public enum PackageResourceType {

    /**
     * Custodial content of the package; domain objects and binary files are both considered to be the custodial
     * content of the package.
     */
    DATA("data"),

    /**
     * Domain objects which describe the binary data contained in package.  Considered to be part of the custodial content of the package.
     */
    DOMAIN_OBJECT("data/obj"),

    /**
     * Binary data contained in the package.  Considered to be part of the custodial content of the package.
     */
    BINARY_DATA("data/bin"),

    /**
     * Metadata describing the custodial content; the {@link #ORE_REM} is considered a form of metadata.
     */
    METADATA("META-INF/org.dataconservancy.packaging/PKG-INFO"),

    /**
     * The ORE-REM describing the graph of objects in the package
     */
    ORE_REM("META-INF/org.dataconservancy.packaging/PKG-INFO/ORE-REM"),

    /**
     * The ontologies used by the domain objects
     */
    ONTOLOGY("META-INF/org.dataconservancy.packaging/ONT"),

    /**
     * The package state, used by the DC Package Tool GUI
     */
    PACKAGE_STATE("META-INF/org.dataconservancy.packaging/STATE");

    /**
     * The relative location of the resource within the package.  Resources of a particular type will placed in the
     * package underneath this location.
     */
    private final String relativePackageLocation;

    private PackageResourceType(String relativePackageLocation) {
        this.relativePackageLocation = relativePackageLocation;
    }

    /**
     * The relative location of the resource within the package.  Resources of a particular type will be placed in the
     * package somewhere underneath this location.
     *
     * @return the relative location of a resource within the package
     */
    public String getRelativePackageLocation() {
        return relativePackageLocation;
    }

    public static PackageResourceType forRelativePackageLocation(String relativeLocation) {
        return Arrays.stream(PackageResourceType.values())
                .findFirst()
                .filter(candidate -> candidate.getRelativePackageLocation().equals(relativeLocation))
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unable to determine the package resource type for path '" + relativeLocation + "'"));
    }
}
