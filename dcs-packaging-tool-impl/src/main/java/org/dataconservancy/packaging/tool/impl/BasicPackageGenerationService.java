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
package org.dataconservancy.packaging.tool.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerator;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.model.BagItParameterNames;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.PackageToolException;

/**
 * Basic map-based package generation service.
 * <p>
 * Uses a provided/injected mapping between packaging formats and generator
 * impls in order to dispatch the package building request and params to the
 * correct package generator.
 * </p>
 * <h2>Parameters</h2>
 * <p>This class responds as follows when the following
 * parameters are found in the given {@link PackageGenerationParameters} during
 * {@link #generatePackage(PackageState, PackageGenerationParameters)}
 * </p>
 * <dl>
 * <dt>{@link GeneralParameterNames#VALIDATE_PACKAGE_DESCRIPTION}</dt>
 * <dd>If "True", the PackageDescription will be pre-validated before the
 * generator is run, if "False", it will not.</dd>
 * <dt>{@link GeneralParameterNames#PACKAGE_FORMAT_ID}</dt>
 * <dd>ID of the packaging format that shall be generated. An exception will be
 * thrown if there are no configured generators that produce the desired
 * packaging format.</dd>
 * </dl>
 * 
 */
public class BasicPackageGenerationService implements PackageGenerationService {

	/*
	 * XXX TODO: This should be defined in PackageGenerationPreferences, but the
	 * model has not been implemented yet
	 */
	private final PackagingFormat DEFAULT_FORMAT = PackagingFormat.BOREM;

	private Map<PackagingFormat, PackageGenerator> generatorMap;

	@Required
	public void setGeneratorMap(Map<PackagingFormat, PackageGenerator> genMap) {
		this.generatorMap = genMap;
	}

	@Override
	public Package generatePackage(PackageState packageState,
			PackageGenerationParameters params) throws PackageToolException {

		PackageGenerator generator;

		if (params != null
				&& params.getParam(BagItParameterNames.PACKAGE_FORMAT_ID, 0) != null) {
			generator = getGenerator(PackagingFormat.valueOf(params.getParam(
					BagItParameterNames.PACKAGE_FORMAT_ID, 0)));
		} else {
			generator = getGenerator(DEFAULT_FORMAT);
		}

		/* TODO:  Is there a means of validating PackageState? */
        if (params != null) {
            params.getParam(
                    GeneralParameterNames.VALIDATE_PACKAGE_DESCRIPTION, 0);
        }


		/* TODO:  Is there a means of validating PackageState? */

		return generator.generatePackage(packageState, params);
	}

	private PackageGenerator getGenerator(PackagingFormat fmt) {

		if (!generatorMap.containsKey(fmt)) {
			throw new RuntimeException(String.format(
					"No generator defined for format %s", fmt.name()));
		}

		return generatorMap.get(fmt);
	}
}
