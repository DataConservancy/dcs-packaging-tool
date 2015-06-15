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
import org.dataconservancy.packaging.tool.model.PackageToolException;

/**
 * Manages the package generation workflow.
 * <p>
 * Upon {@link #generatePackage(PackageDescription, PackageGenerationParameters)},
 * this service will invoke an appropriate instance of PackageGenerator to
 * generate a package of the requested form.
 * </p>
 */
public interface PackageGenerationService extends PackageGenerator {
	/**
	 * Invoke the corresponding PackageGenerator to generate the requested
	 * package.
	 * 
	 * @param desc
	 *            PackageDescription containing the content.
	 * @param params
	 *            Execution parameters that specify user's choices among the
	 *            package generation preferences. An appropriate
	 *            PackageGeneration implementation will be selected based upon
	 *            the contents of these params
	 * @throws RuntimeException
	 *             if there is a problem generating the package.
	 */
	@Override
	public Package generatePackage(PackageDescription desc,
			PackageGenerationParameters params) throws PackageToolException;
}