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

package org.dataconservancy.packaging.tool.impl.generator;

import org.dataconservancy.packaging.tool.api.generator.PackageModelBuilder;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

import java.util.Map;

/**
 * Provides instances of {@link PackageModelBuilder} for instantiating package
 * models.
 */
public class PackageModelBuilderFactory {

    private static Map<String, Class<? extends PackageModelBuilder>> builders;

    /**
     * Provides an instance of {@link PackageModelBuilder} for assembling
     * packages.
     * <p>
     * Although the behaviour of the PackageModelBuilder is largely determined
     * by the semantics of the package modeling ontology, it may still be
     * influenced by provided parameters. The Builder produced by this method
     * may be re-used repeatedly to build several unrelated packages, but they
     * will all have the same configuration parameters.
     * </p>
     * <p>
     * PackageModelBuilders are generally implemented for particular
     * PackageDescription specifications (e.g. the DCS Business Object model,
     * PLANETS model, etc), so providing the packaging format id via the
     * parameters will allow the factory to choose the correct PackageModelBuilder
     * for the given format, or throw an exception if the given
     * specification is unsupported.
     * </p>
     *
     * @param desc
     *        The package description to use for selecting the correct builder
     * @param params
     *        Package Generation Parameters
     * @return Fully configured PackageModelBulder.
     * @throws IllegalAccessException if no builders have been set on the factory
     * @throws InstantiationException if the params don't contain the package format id
     */
    public static PackageModelBuilder newBuilder(PackageDescription desc, PackageGenerationParameters params)
            throws IllegalAccessException, InstantiationException {

        if (builders == null || builders.size() == 0) {
            throw new IllegalStateException("No builders have been set.");
        }

        String formatId = null;
        if (params.getKeys().contains(GeneralParameterNames.PACKAGE_FORMAT_ID)) {
            formatId = params.getParam(GeneralParameterNames.PACKAGE_FORMAT_ID, 0);
        }
        if (formatId == null) {
            throw new IllegalArgumentException("The parameter list must contain a package format id");
        }

        for (String builderId : builders.keySet()) {
            if (builderId.equals(getModelBuilderId(desc.getPackageOntologyIdentifier(), formatId))) {
                Class<? extends PackageModelBuilder> builderClass = builders.get(builderId);
                PackageModelBuilder builder = builderClass.newInstance();
                builder.init(params);
                return builder;
            }
        }

        return null;
    }

    public static String getModelBuilderId(String ontologyId, String formatId) {
        return ontologyId + "-" + formatId;
    }

    public static void setBuilders(Map<String, Class<? extends PackageModelBuilder>> builders) {
        if (builders == null || builders.size() == 0) {
            throw new IllegalArgumentException("At least one builder must be provided");
        }

        PackageModelBuilderFactory.builders = builders;
    }

}
