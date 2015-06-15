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
package org.dataconservancy.packaging.tool.api;

import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

/**
 * This is the format-specific package generator.
 * <p>
 * Given a {@link PackageDescription} describing package contents, the role of
 * the PackageGenerator is to produce an appropriate serialized package in some
 * format.
 * </p>
 */
public interface PackageGenerator {

	/**
	 * Generate a package based upon the content specified in the
	 * PackageDescription.
	 * 
	 * @param desc
	 *            PackageDescription containing the content.
	 * @param params
	 *            User-selected Package Generation preferences, may also contain
	 *            ref to format-specific preferences/configuration.
	 * @throws RuntimeException
	 *             if there is a problem generating the package.
	 */
	public Package generatePackage(PackageDescription desc,
			PackageGenerationParameters params);
}