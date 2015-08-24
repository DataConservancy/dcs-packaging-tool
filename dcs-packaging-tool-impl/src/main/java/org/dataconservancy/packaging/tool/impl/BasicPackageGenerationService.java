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

import org.dataconservancy.packaging.tool.model.*;
import org.springframework.beans.factory.annotation.Required;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerator;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.validation.PackageValidationException;

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
 * {@link #generatePackage(PackageDescription, PackageGenerationParameters)}
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

	private PackageDescriptionValidator validator;

	@Required
	public void setPackageDescriptionValidator(
			PackageDescriptionValidator validator) {
		this.validator = validator;
	}

	@Required
	public void setGeneratorMap(Map<PackagingFormat, PackageGenerator> genMap) {
		this.generatorMap = genMap;
	}

	@Override
	public Package generatePackage(PackageDescription desc,
			PackageGenerationParameters params) throws PackageToolException {

		boolean validate = true;

		PackageGenerator generator;

		if (params != null
				&& params.getParam(BagItParameterNames.PACKAGE_FORMAT_ID, 0) != null) {
			generator = getGenerator(PackagingFormat.valueOf(params.getParam(
					BagItParameterNames.PACKAGE_FORMAT_ID, 0)));
		} else {
			generator = getGenerator(DEFAULT_FORMAT);
		}

        /* If an external project id was entered add it to the root of the description, before validating, so we ensure we've created
         * a valid description before continuing.
         */
        if (params != null) {
            String externalProjectId = params.getParam(GeneralParameterNames.EXTERNAL_PROJECT_ID, 0);
            if (externalProjectId != null
                && !params.getParam(GeneralParameterNames.EXTERNAL_PROJECT_ID, 0).isEmpty()) {
                PackageArtifact root = desc.getRootArtifact();
                if (root != null) {
                    root.getRelationships().add(new PackageRelationship("http://purl.org/dc/terms/isPartOf", true, externalProjectId));
                }
            }
        }

		/* Validate package */
        String validateParam = null;
        if (params != null) {
            validateParam = params.getParam(
                    GeneralParameterNames.VALIDATE_PACKAGE_DESCRIPTION, 0);
        }

        if (validateParam != null) {
			validate = Boolean.valueOf(validateParam);
		}

		if (validate && !Thread.currentThread().isInterrupted()) {
			try {
				validator.validate(desc);
			} catch (PackageValidationException e) {
				throw new PackageToolException(
						PackagingToolReturnInfo.PKG_VALIDATION_FAIL, e);
			}
		}

		return generator.generatePackage(desc, params);
	}

	private PackageGenerator getGenerator(PackagingFormat fmt) {

		if (!generatorMap.containsKey(fmt)) {
			throw new RuntimeException(String.format(
					"No generator defined for format %s", fmt.name()));
		}

		return generatorMap.get(fmt);
	}
}
