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
package org.dataconservancy.packaging.tool.model;

public class GeneralParameterNames {

	public static final String PACKAGE_FORMAT_ID = "Package-Format-Id";
	public static final String PACKAGE_LOCATION = "Package-Location";
	public static final String PACKAGE_STAGING_LOCATION = "Package-Staging-Location";
	public static final String PACKAGE_NAME = "Package-Name";
	public static final String CHECKSUM_ALGORITHMS = "Checksum-Algs";
	public static final String COMPRESSION_FORMAT = "Compression-Format";

    /**
     * Parameter identifying the format used to serialized RDF-based resources in
     * the package.  Possible values are contained in {@code GeneralParameterNames#SERIALIZATION_FORMAT}.
     * The parameter name is a bit of a misnomer, because it is consulted when
     * serializing <em>all</em> RDF-based resources, not just the ORE-ReM.
     */
    public static final String REM_SERIALIZATION_FORMAT = "ReM-Serialization-Format";
	public static final String ARCHIVING_FORMAT = "Archiving-Format";
    public static final String EXTERNAL_PROJECT_ID = "External-Project-Id";
	public static final String DOMAIN_PROFILE = "Domain-Profile";

	/**
	 * Parameter to indicate whether a PackageDescription shall be validated
	 * before processing.
	 * <p>
	 * This is a boolean, so acceptable values are "true" or "false",
	 * non-case-sensitive (i.e. "True", "true", or "TRUE" are all valid values.
	 * </p>
	 */
	public static final String VALIDATE_PACKAGE_DESCRIPTION = "Validate-Package-Description";

    /**
     * Possible values for the {@link #REM_SERIALIZATION_FORMAT} parameter. This is a list
     * of supported serialization formats for RDF data (ORE-ReM, domain objects, etc)
     * serialized to the package.
     */
	public enum SERIALIZATION_FORMAT {
        /**
         * Pretty-printed RDF/XML
         */
		XML,

        /**
         * Pretty-printed JSON-LD
         */
        JSONLD,

        /**
         * Pretty-printed Turtle
         */
        TURTLE
	}
}