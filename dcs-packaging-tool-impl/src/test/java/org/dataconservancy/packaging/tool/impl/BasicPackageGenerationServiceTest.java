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

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.api.PackageGenerator;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;
import org.dataconservancy.packaging.validation.PackageValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.PACKAGE_FORMAT_ID;

public class BasicPackageGenerationServiceTest {

	private static final PackageDescriptionValidator ALWAYS_VALIDATE = mock(PackageDescriptionValidator.class);

	private static final Map<PackagingFormat, PackageGenerator> MOCK_GENERATOR_MAP = new HashMap<PackagingFormat, PackageGenerator>() {
		{
			final Package PACKAGE = mock(Package.class);
			final PackageGenerator GENERATOR = mock(PackageGenerator.class);
			when(
					GENERATOR.generatePackage(any(PackageDescription.class),
							any(PackageGenerationParameters.class)))
					.thenReturn(PACKAGE);
			put(PackagingFormat.TEST, GENERATOR);
		}
	};

	/*
	 * Verifies that the correct generator will be invoked when properly
	 * configured.
	 */
	@Test
	public void validGeneratorTest() {
		BasicPackageGenerationService svc = new BasicPackageGenerationService();

		Map<PackagingFormat, PackageGenerator> generatorMap = new HashMap<PackagingFormat, PackageGenerator>();

		/* Define our params, we 'll use the TEST format */
		final PackageGenerationParameters PARAMS = mock(PackageGenerationParameters.class);
		when(PARAMS.getParam(PACKAGE_FORMAT_ID, 0)).thenReturn(
				PackagingFormat.TEST.name());

		/* Define the behaviour of the generator */
		final PackageGenerator GENERATOR = mock(PackageGenerator.class);
		final Package PACKAGE = mock(Package.class);
		final PackageDescription DESC = mock(PackageDescription.class);
		when(GENERATOR.generatePackage(DESC, PARAMS)).thenReturn(PACKAGE);

		/* Configure the service with the generator */
		generatorMap.put(PackagingFormat.TEST, GENERATOR);
		svc.setGeneratorMap(generatorMap);
		svc.setPackageDescriptionValidator(ALWAYS_VALIDATE);

		/* Verify that we get the expected package the way we expect */
		assertEquals(PACKAGE, svc.generatePackage(DESC, PARAMS));
		verify(GENERATOR, times(1)).generatePackage(DESC, PARAMS);

	}

	/*
	 * Verifies that a valid format with a non-configured generator will return
	 * an exception
	 */
	@Test(expected = Exception.class)
	public void noGeneratorTest() {
		BasicPackageGenerationService svc = new BasicPackageGenerationService();

		Map<PackagingFormat, PackageGenerator> generatorMap = new HashMap<PackagingFormat, PackageGenerator>();

		/* Define our params, we'll try to use the BOREM format */
		final PackageGenerationParameters PARAMS = mock(PackageGenerationParameters.class);
		when(PARAMS.getParam(PACKAGE_FORMAT_ID, 0)).thenReturn(
				PackagingFormat.BOREM.name());

		/* Define the behaviour of the generator */
		final PackageGenerator GENERATOR = mock(PackageGenerator.class);
		final Package PACKAGE = mock(Package.class);
		final PackageDescription DESC = mock(PackageDescription.class);
		when(GENERATOR.generatePackage(DESC, PARAMS)).thenReturn(PACKAGE);

		/*
		 * Configure the service with the generator. Note, we're only defining a
		 * TEST generator, not BOREM as requested.
		 */
		generatorMap.put(PackagingFormat.TEST, GENERATOR);
		svc.setGeneratorMap(generatorMap);
		svc.setPackageDescriptionValidator(ALWAYS_VALIDATE);

		svc.generatePackage(DESC, PARAMS);
	}

	/* Verifies that validation occurs when it's required by params */
	@Test
	public void validationOccursTest() throws PackageValidationException {
		PackageDescriptionValidator validator = mock(PackageDescriptionValidator.class);
		final PackageDescription DESC = mock(PackageDescription.class);
		final PackageGenerationParameters params = new PackageGenerationParameters();

		/* Require validation */
		params.addParam(GeneralParameterNames.VALIDATE_PACKAGE_DESCRIPTION,
				"True");
		params.addParam(PACKAGE_FORMAT_ID, PackagingFormat.TEST.name());

		BasicPackageGenerationService svc = new BasicPackageGenerationService();
		svc.setGeneratorMap(MOCK_GENERATOR_MAP);
		svc.setPackageDescriptionValidator(validator);

		svc.generatePackage(DESC, params);

		verify(validator).validate(DESC);
	}

	/* Verifies that validation DOES NOT occur when it's disabled by params */
	@Test
	public void validationNotOccurWhenDisabledTest()
			throws PackageValidationException {
		PackageDescriptionValidator validator = mock(PackageDescriptionValidator.class);
		final PackageDescription DESC = mock(PackageDescription.class);
		final PackageGenerationParameters params = new PackageGenerationParameters();

		/* Disable validation */
		params.addParam(GeneralParameterNames.VALIDATE_PACKAGE_DESCRIPTION,
				"False");
		params.addParam(PACKAGE_FORMAT_ID, PackagingFormat.TEST.name());

		BasicPackageGenerationService svc = new BasicPackageGenerationService();
		svc.setGeneratorMap(MOCK_GENERATOR_MAP);
		svc.setPackageDescriptionValidator(validator);

		svc.generatePackage(DESC, params);

		/* verify that validation not called */
		verify(validator, times(0)).validate(DESC);
	}

	/*
	 * Verifies that validation falure will cause a PackageToolException with
	 * the right code to be thrown
	 */
	@Test
	public void validationExceptionTest() throws Exception {
		PackageDescriptionValidator validator = mock(PackageDescriptionValidator.class);

		Mockito.doThrow(mock(PackageValidationException.class)).when(validator)
				.validate(any(PackageDescription.class));

		final PackageDescription DESC = mock(PackageDescription.class);
		final PackageGenerationParameters params = new PackageGenerationParameters();

		/* Require validation */
		params.addParam(GeneralParameterNames.VALIDATE_PACKAGE_DESCRIPTION,
				"True");
		params.addParam(PACKAGE_FORMAT_ID, PackagingFormat.TEST.name());

		BasicPackageGenerationService svc = new BasicPackageGenerationService();
		svc.setGeneratorMap(MOCK_GENERATOR_MAP);
		svc.setPackageDescriptionValidator(validator);

		try {
			svc.generatePackage(DESC, params);
			Assert.fail("Exception should have been thrown due to bad validation");
		} catch (PackageToolException e) {
			assertEquals(
					PackagingToolReturnInfo.PKG_VALIDATION_FAIL.returnCode(),
					e.getCode());
		}
	}
}
