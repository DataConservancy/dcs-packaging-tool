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

import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerator;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.impl.generator.BagItPackageAssembler;
import org.dataconservancy.packaging.tool.impl.generator.OrePackageModelBuilder;
import org.dataconservancy.packaging.tool.impl.generator.PackageAssemblerFactory;
import org.dataconservancy.packaging.tool.impl.generator.PackageModelBuilderFactory;
import org.dataconservancy.packaging.tool.model.*;

import java.net.URI;

/**
 * <p>Bagit + ORE ReM package generator.</p>
 *
 * <p>Returns a {@code Package} who's content what described in the {@code PackageDescription}, which is passed in.
 * The content is organized and described according to BagIt specification (specific version is determined by the
 * implementation of the {@code PackageAssembler} issued by the {@code PackageAssemblerFactory}.) The ontology
 * specific description of the content is expressed in ORE-ReM specification.</p>
 *
 */
public class BOREMPackageGenerator implements PackageGenerator {


    /**
     * <p>
     * Generates a {@code Package} based on given input.
     * </p>
     *
     * <p>
     * Archiving format, compression format and checksum algorithms can be optionally specified in the input
     * {@code PackageGenerationParams}. When not specified, default archiving format and compression format are determined
     * by the {@code PackageAssembler}.
     * </p>
     *
     * Mimetype of the content, expressed in the {@code Package}'s {@code contentType} field is determined based on the
     * specified archiving and compression formats in the input {@code PackageGenerationParameters}. If the specified
     * formats are not recognized, the default "application/octet-stream" will be used.
     *
     * @param desc
     *            PackageDescription containing the content.
     * @param params
     *            PackageGenerationParameters which are needed in or help inform the package generation process. Which
     *            parameters are required and which are optional depends on the implementations of the
     *            {@code PackageModelBuilder} and {@code PackageAssembler} used. {@code BOREMPackageAssembler} itself
     *            only require the "{@code package-format-id}" parameter to be passed in.
     * @return {@code Package} object containing the information described in {@code PackageDescription} in the format
     * specified in the {@code PackageGenerationParameters}. If the archiving format is "exploded," the returned package
     * will be null.
     */
    @Override
	public Package generatePackage(PackageDescription desc, PackageGenerationParameters params) {
        //check for format
        String formatId = params.getParam(GeneralParameterNames.PACKAGE_FORMAT_ID, 0);

        if (formatId == null) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_REQUIRED_PARAMS_MISSING,
                    GeneralParameterNames.PACKAGE_FORMAT_ID + " is required but missing.");
        }
        if (!formatId.equals(PackagingFormat.BOREM.toString())) {
             throw new PackageToolException(PackagingToolReturnInfo.PKG_UNEXPECTED_PACKAGING_FORMAT, "Expected " +
                     PackagingFormat.BOREM + ", but was given " + formatId + ".");
        }

        try {
            BagItPackageAssembler assembler = (BagItPackageAssembler)PackageAssemblerFactory.newAssembler(params);
            OrePackageModelBuilder builder = (OrePackageModelBuilder)PackageModelBuilderFactory.newBuilder(desc, params);

            if (assembler == null) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_OBJECT_INSTANTIATION_EXP, "Could not create " +
                        "an instance of the PackageAssembler for format " + formatId + ". One may not exist. ");
            }

            if (builder == null) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_OBJECT_INSTANTIATION_EXP, "Could not create " +
                        "an instance of PackageModelBuilder for format " + formatId + ". One may not exist. ");
            }

            builder.buildModel(desc, assembler);

            URI packageRemURI = builder.getPackageRemURI();

            assembler.addParameter(BoremParameterNames.PKG_ORE_REM, packageRemURI.toString());

            return assembler.assemblePackage();

        } catch (IllegalAccessException | InstantiationException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_OBJECT_INSTANTIATION_EXP, e);
        } catch (IllegalStateException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_GEN_MISSING_COMPONENTS, e);
        }
	}

}
