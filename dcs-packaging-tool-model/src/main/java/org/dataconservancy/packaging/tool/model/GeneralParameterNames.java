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
	public static final String CONTENT_ROOT_LOCATION = "Content-Root-Location";
	public static final String PACKAGE_NAME = "Package-Name";
	public static final String CHECKSUM_ALGORITHMS = "Checksum-Algs";
	public static final String COMPRESSION_FORMAT = "Compression-Format";
	public static final String ARCHIVING_FORMAT = "Archiving-Format";
    public static final String EXTERNAL_PROJECT_ID = "External-Project-Id";

	/**
	 * Parameter to indicate whether a PackageDescription shall be validated
	 * before processing.
	 * <p>
	 * This is a boolean, so acceptable values are "true" or "false",
	 * non-case-sensitive (i.e. "True", "true", or "TRUE" are all valid values.
	 * </p>
	 */
	public static final String VALIDATE_PACKAGE_DESCRIPTION = "Validate-Package-Description";
}